import json
import pika
import os
from utils.minio_client import download_file, upload_text
from utils.ocr import perform_ocr

# --- RabbitMQ Configuration ---
RABBIT_HOST = os.getenv("SPRING_RABBITMQ_HOST", "rabbitmq")
RABBIT_USER = os.getenv("SPRING_RABBITMQ_USERNAME", "appuser")
RABBIT_PASS = os.getenv("SPRING_RABBITMQ_PASSWORD", "supersecret123")
QUEUE_NAME = os.getenv("APP_MQ_QUEUE", "docs.uploaded.q")
INDEX_QUEUE = os.getenv("INDEX_QUEUE", "indexing-tasks")

print(f"Connecting to RabbitMQ at {RABBIT_HOST} ...")

connection = pika.BlockingConnection(
    pika.ConnectionParameters(
        host=RABBIT_HOST,
        credentials=pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    )
)
channel = connection.channel()
channel.queue_declare(
    queue=QUEUE_NAME,
    durable=True,
    arguments={
        "x-dead-letter-exchange": "docs.dlx",
        "x-dead-letter-routing-key": "document.uploaded.dlq"
    }
)
# Declare GenAI queue (for sending OCR results)
channel.queue_declare(queue="genai-tasks", durable=True)
# Declare Indexer queue (for sending OCR text to Elasticsearch indexer)
channel.queue_declare(queue=INDEX_QUEUE, durable=True)


print(f"Connected - waiting for messages in '{QUEUE_NAME}'")

# --- Message handler ---
def callback(ch, method, properties, body):
    event = json.loads(body)
    doc_id = event["documentId"]
    filename = event["filename"]
    content_type = event.get("contentType")
    uploaded_at = event.get("uploadedAt")
    size = event.get("size")

    print(f"Received: {filename} ({doc_id})")


    try:
        # Download file from MinIO
        path = download_file(doc_id, filename)

        # Perform OCR
        text = perform_ocr(path)

        # Upload OCR text result back to MinIO
        upload_text(doc_id, text)

        print(f"OCR complete for {filename}")

        # Send message to GenAI queue
        channel.basic_publish(
            exchange="",
            routing_key="genai-tasks",
            body=json.dumps({
                "documentId": doc_id,
                "text": text
            })
        )
        print(f"Sent OCR result to GenAI worker for {filename}")

        # Send message to Indexing worker (Elasticsearch)
        channel.basic_publish(
            exchange="",
            routing_key=INDEX_QUEUE,
            body=json.dumps({
                "documentId": doc_id,
                "filename": filename,
                "contentType": content_type,
                "size": size,
                "uploadedAt": uploaded_at,
                "text": text
            })
        )
        print(f"Sent OCR text to indexing worker for {filename}")
    except Exception as e:
        print(f"Error processing {filename}: {e}")

    ch.basic_ack(delivery_tag=method.delivery_tag)


channel.basic_consume(queue=QUEUE_NAME, on_message_callback=callback)
channel.start_consuming()
