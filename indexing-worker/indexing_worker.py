import json
import os
import sys
import pika
from elasticsearch import Elasticsearch


RABBIT_HOST = os.getenv("RABBIT_HOST", "rabbitmq")
RABBIT_USER = os.getenv("RABBIT_USER", "appuser")
RABBIT_PASS = os.getenv("RABBIT_PASS", "supersecret123")
INDEX_QUEUE = os.getenv("INDEX_QUEUE", "indexing-tasks")

ELASTIC_URL = os.getenv("ELASTIC_URL", "http://elasticsearch:9200")
ELASTIC_INDEX = os.getenv("ELASTIC_INDEX", "documents")


def create_es_client():
    """
    Create a minimally configured Elasticsearch client.
    Security is disabled in docker-compose, so certificates are not required.
    """
    return Elasticsearch(ELASTIC_URL, verify_certs=False)


def ensure_index(es_client, index_name):
    """
    Make sure the target index exists with a mapping that keeps filename keywords searchable.
    """
    if es_client.indices.exists(index=index_name):
        return

    es_client.indices.create(
        index=index_name,
        mappings={
            "properties": {
                "documentId": {"type": "keyword"},
                "filename": {
                    "type": "text",
                    "fields": {"keyword": {"type": "keyword"}}
                },
                "contentType": {"type": "keyword"},
                "uploadedAt": {"type": "date"},
                "size": {"type": "long"},
                "text": {"type": "text"},
                "summary": {"type": "text"}
            }
        }
    )


def index_document(es_client, index_name, payload):
    """
    Index a single OCR text payload.
    """
    document_body = {
        "documentId": payload["documentId"],
        "filename": payload.get("filename"),
        "contentType": payload.get("contentType"),
        "uploadedAt": payload.get("uploadedAt"),
        "size": payload.get("size"),
        "text": payload.get("text"),
        "summary": payload.get("summary")
    }
    es_client.index(index=index_name, id=payload["documentId"], document=document_body)
    return document_body


def on_message(es_client, ch, method, properties, body):
    """
    RabbitMQ callback that indexes OCR text into Elasticsearch.
    """
    payload = json.loads(body)
    doc_id = payload.get("documentId")
    filename = payload.get("filename")
    print(f"[indexer] Indexing document {doc_id} ({filename})", flush=True)

    try:
        index_document(es_client, ELASTIC_INDEX, payload)
        ch.basic_ack(delivery_tag=method.delivery_tag)
        print(f"[indexer] Indexed {doc_id}", flush=True)
    except Exception as exc:  # pragma: no cover - defensive logging
        print(f"[indexer] Failed to index {doc_id}: {exc}", file=sys.stderr, flush=True)
        ch.basic_ack(delivery_tag=method.delivery_tag)


def main():
    es_client = create_es_client()
    ensure_index(es_client, ELASTIC_INDEX)

    credentials = pika.PlainCredentials(RABBIT_USER, RABBIT_PASS)
    params = pika.ConnectionParameters(host=RABBIT_HOST, credentials=credentials)
    connection = pika.BlockingConnection(params)
    channel = connection.channel()
    channel.queue_declare(queue=INDEX_QUEUE, durable=True)

    channel.basic_consume(
        queue=INDEX_QUEUE,
        on_message_callback=lambda ch, method, props, body: on_message(es_client, ch, method, props, body)
    )
    print(f"[indexer] Waiting for messages in '{INDEX_QUEUE}'...", flush=True)
    channel.start_consuming()


if __name__ == "__main__":
    main()
