from minio import Minio
import os
import re

# --- Configuration ---
MINIO_URL = os.getenv("MINIO_ENDPOINT", "http://minio:9000").replace("localhost", "minio")
ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "paperless")
SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "paperless")
BUCKET = os.getenv("MINIO_BUCKET", "documentmanager")

client = Minio(
    MINIO_URL.replace("http://", "").replace("https://", ""),
    access_key=ACCESS_KEY,
    secret_key=SECRET_KEY,
    secure=False
)

# --- Helper: sanitize filename like backend ---
def sanitize_filename(filename: str) -> str:
    """Remove unsafe characters and preserve extension."""
    if not filename:
        return "unnamed"
    base, ext = os.path.splitext(filename)
    base = re.sub(r'[^a-zA-Z0-9-_]', "_", base)
    return f"{base}{ext}"


# --- File download ---
def download_file(doc_id: str, original_filename: str):
    """
    Downloads a file from MinIO using the backend naming scheme:
    <uuid>-<cleanedOriginalFilename>.<ext>
    """
    cleaned = sanitize_filename(original_filename)
    object_name = f"{doc_id}-{cleaned}"
    local_path = f"/tmp/{object_name}"

    print(f"Downloading '{object_name}' from bucket '{BUCKET}'", flush=True)
    client.fget_object(BUCKET, object_name, local_path)
    return local_path


# --- Upload OCR text result ---
def upload_text(doc_id: str, text: str):
    """
    Upload OCR text result to 'ocr/<uuid>.txt'
    """
    txt_path = f"/tmp/{doc_id}.txt"
    with open(txt_path, "w", encoding="utf-8") as f:
        f.write(text)
    client.fput_object(BUCKET, f"ocr/{doc_id}.txt", txt_path)
