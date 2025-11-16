import os
import json
import pika
from google import genai

# ---- RabbitMQ config ----
RABBIT_HOST = os.getenv("RABBIT_HOST", "rabbitmq")
RABBIT_USER = os.getenv("RABBIT_USER", "appuser")
RABBIT_PASS = os.getenv("RABBIT_PASS", "supersecret123")
QUEUE = os.getenv("RABBIT_QUEUE", "genai-tasks")

# ---- Gemini client ----
API_KEY = os.getenv("GEMINI_API_KEY")
client = genai.Client(api_key=API_KEY)

def summarize(text):

    response = client.models.generate_content(
        model="gemini-2.5-flash",
        contents=f"Summarize the following document:\n\n{text}",
    )

    # The SDK exposes response.text directly!
    return response.text


def on_message(ch, method, properties, body):
    message = json.loads(body)
    doc_id = message["documentId"]
    extracted_text = message["text"]

    print(f"[GenAI] Received task for document {doc_id}")

    try:
        summary = summarize(extracted_text)

        ch.basic_publish(
            exchange="",
            routing_key="summary-results",
            body=json.dumps({
                "documentId": doc_id,
                "summary": summary
            })
        )

        ch.basic_ack(delivery_tag=method.delivery_tag)
        print(f"[GenAI] Summary generated for doc {doc_id}")

    except Exception as e:
        print(f"[GenAI] ERROR: {e}")

        # fallback summary
        ch.basic_publish(
            exchange="",
            routing_key="summary-results",
            body=json.dumps({
                "documentId": doc_id,
                "summary": "Summary unavailable due to processing error."
            })
        )
        ch.basic_ack(delivery_tag=method.delivery_tag)


# ---- RabbitMQ connection ----
connection = pika.BlockingConnection(
    pika.ConnectionParameters(
        host=RABBIT_HOST,
        credentials=pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    )
)
channel = connection.channel()

channel.queue_declare(queue=QUEUE, durable=True)
channel.queue_declare(queue="summary-results", durable=True)

print("[GenAI] Worker running – waiting for messages...")
channel.basic_consume(queue=QUEUE, on_message_callback=on_message)
channel.start_consuming()
