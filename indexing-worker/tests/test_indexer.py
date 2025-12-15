from unittest.mock import MagicMock

from indexing_worker import index_document, ensure_index


def test_index_document_builds_payload_and_calls_es():
    es = MagicMock()
    payload = {
        "documentId": "123",
        "filename": "report.pdf",
        "contentType": "application/pdf",
        "uploadedAt": "2024-01-01T00:00:00Z",
        "size": 42,
        "text": "hello world",
        "summary": "short"
    }

    stored_doc = index_document(es, "documents", payload)

    es.index.assert_called_once()
    call_kwargs = es.index.call_args.kwargs
    assert call_kwargs["index"] == "documents"
    assert call_kwargs["id"] == "123"
    assert call_kwargs["document"]["text"] == "hello world"
    assert stored_doc["filename"] == "report.pdf"


def test_ensure_index_creates_when_missing():
    es = MagicMock()
    es.indices.exists.return_value = False

    ensure_index(es, "documents")

    es.indices.create.assert_called_once()
    assert es.indices.exists.call_args.kwargs["index"] == "documents"
