import { useEffect, useState } from "react";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

interface Document {
    id: string;
    originalFilename: string;
    contentType: string;
    size: number;
    uploadedAt: string;
}

export default function App() {
    const [docs, setDocs] = useState<Document[]>([]);
    const [selectedDoc, setSelectedDoc] = useState<Document | null>(null);

    useEffect(() => {
        fetch("/api/documents")
            .then((res) => {
                if (!res.ok) throw new Error("Failed to load documents");
                return res.json();
            })
            .then(setDocs)
            .catch((err) => {
                console.error("Error loading docs:", err);
                toast.error("Failed to load documents");
            });
    }, []);

    // Upload new document
    const uploadDoc = async (file: File) => {
        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch("/api/documents/upload", {
                method: "POST",
                body: formData,
            });
            if (!res.ok) throw new Error("Upload failed");
            const doc = await res.json();
            setDocs((prev) => [...prev, doc]);
            toast.success(`Uploaded ${file.name}`);
        } catch (e) {
            toast.error(`Upload failed: ${(e as Error).message}`);
        }
    };

    // Replace existing document
    const replaceDoc = async (id: string, file: File) => {
        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch(`/api/documents/${id}/replace`, {
                method: "PUT",
                body: formData,
            });
            if (!res.ok) throw new Error("Replace failed");
            const doc = await res.json();
            setDocs((prev) => prev.map((d) => (d.id === doc.id ? doc : d)));
            setSelectedDoc(doc);
            toast.success(`Replaced with ${file.name}`);
        } catch (e) {
            toast.error(`Replace failed: ${(e as Error).message}`);
        }
    };

    // Delete document
    const deleteDoc = async () => {
        if (!selectedDoc) return;
        try {
            const res = await fetch(`/api/documents/${selectedDoc.id}`, { method: "DELETE" });
            if (!res.ok) throw new Error("Delete failed");
            setDocs((prev) => prev.filter((d) => d.id !== selectedDoc.id));
            toast.success(`Deleted ${selectedDoc.originalFilename}`);
            setSelectedDoc(null);
        } catch (e) {
            toast.error(`Delete failed: ${(e as Error).message}`);
        }
    };

    // Download file directly (streamed from backend)
    const downloadDoc = async (id: string) => {
        try {
            const res = await fetch(`/api/documents/download/${id}`);
            if (!res.ok) throw new Error("Download failed");

            // Extract filename from Content-Disposition header
            const disposition = res.headers.get("Content-Disposition");
            const match = disposition && disposition.match(/filename="(.+)"/);
            const filename = match ? match[1] : "document";

            // Convert response to blob
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);

            // Trigger download
            const a = document.createElement("a");
            a.href = url;
            a.download = filename;
            a.style.display = "none";
            document.body.appendChild(a);
            a.click();
            document.body.removeChild(a);
            window.URL.revokeObjectURL(url);

            toast.success(`Downloaded ${filename}`);
        } catch (e) {
            console.error(e);
            toast.error(`Download failed: ${(e as Error).message}`);
        }
    };



    return (
        <div className="min-h-screen bg-neutral-900 text-gray-100 flex flex-col">
            {/* Toast notifications */}
            <ToastContainer position="top-right" autoClose={3000} />

            {/* Header */}
            <header className="bg-neutral-800 border-b border-neutral-700 shadow p-6 text-center">
                <h1 className="text-4xl font-bold text-purple-800">Documentmanager</h1>
                <p className="text-neutral-300 mt-2">
                    by Brandtner Niklas, Lechner Sonja and Vakorin Denis
                </p>
            </header>

            {/* Content Area */}
            <main className="flex-1 p-8 flex gap-6">
                {/* Table Area */}
                <div className="flex-1 bg-neutral-800 rounded-lg shadow-lg p-6 overflow-x-auto">
                    <div className="flex justify-between items-center mb-4 border-b border-neutral-700 pb-2">
                        <h2 className="text-xl font-semibold text-neutral-200">Documents</h2>
                        <div className="space-x-2">
                            {/* Upload */}
                            <button
                                onClick={() => document.getElementById("uploadInput")?.click()}
                                className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow"
                            >
                                Upload
                            </button>
                            <input
                                type="file"
                                id="uploadInput"
                                className="hidden"
                                onChange={(e) => {
                                    const file = e.target.files?.[0];
                                    if (file) uploadDoc(file);
                                }}
                            />

                            {/* Replace */}
                            <button
                                disabled={!selectedDoc}
                                onClick={() => document.getElementById("replaceInput")?.click()}
                                className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow disabled:opacity-40"
                            >
                                Replace
                            </button>
                            <input
                                type="file"
                                id="replaceInput"
                                className="hidden"
                                onChange={(e) => {
                                    const file = e.target.files?.[0];
                                    if (file && selectedDoc) replaceDoc(selectedDoc.id, file);
                                }}
                            />

                            {/* Delete */}
                            <button
                                disabled={!selectedDoc}
                                onClick={deleteDoc}
                                className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow disabled:opacity-40"
                            >
                                Delete
                            </button>
                        </div>
                    </div>

                    {/* Documents Table */}
                    <table className="w-full text-left border-collapse">
                        <thead>
                        <tr className="bg-neutral-700 text-neutral-300 uppercase text-sm">
                            <th className="p-3 border-b border-neutral-600">Filename</th>
                            <th className="p-3 border-b border-neutral-600">Type</th>
                            <th className="p-3 border-b border-neutral-600">Size</th>
                            <th className="p-3 border-b border-neutral-600">Uploaded</th>
                        </tr>
                        </thead>
                        <tbody>
                        {docs.map((doc) => (
                            <tr
                                key={doc.id}
                                onClick={() => setSelectedDoc(doc)}
                                className={`cursor-pointer transition-colors ${
                                    selectedDoc?.id === doc.id
                                        ? "bg-purple-900/40"
                                        : "hover:bg-neutral-700"
                                }`}
                            >
                                <td className="p-3 border-b border-neutral-700">
                                    {doc.originalFilename}
                                </td>
                                <td className="p-3 border-b border-neutral-700">
                                    {doc.contentType}
                                </td>
                                <td className="p-3 border-b border-neutral-700">
                                    {(doc.size / 1024).toFixed(2)} KB
                                </td>
                                <td className="p-3 border-b border-neutral-700">
                                    {new Date(doc.uploadedAt).toLocaleString()}
                                </td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {/* Details Pane */}
                {selectedDoc && (
                    <aside className="w-80 bg-neutral-800 rounded-lg shadow-lg p-6 relative border border-neutral-700">
                        <button
                            onClick={() => setSelectedDoc(null)}
                            className="absolute top-3 right-3 text-neutral-400 hover:text-purple-500"
                        >
                            ✕
                        </button>
                        <h3 className="text-lg font-semibold mb-4 text-purple-400">Details</h3>
                        <div className="space-y-2">
                            <p>
                                <span className="font-medium text-neutral-300">ID:</span>{" "}
                                {selectedDoc.id}
                            </p>
                            <p>
                                <span className="font-medium text-neutral-300">Filename:</span>{" "}
                                {selectedDoc.originalFilename}
                            </p>
                            <p>
                                <span className="font-medium text-neutral-300">Type:</span>{" "}
                                {selectedDoc.contentType}
                            </p>
                            <p>
                                <span className="font-medium text-neutral-300">Size:</span>{" "}
                                {(selectedDoc.size / 1024).toFixed(2)} KB
                            </p>
                            <p>
                                <span className="font-medium text-neutral-300">Uploaded:</span>{" "}
                                {new Date(selectedDoc.uploadedAt).toLocaleString()}
                            </p>
                        </div>
                        {/* Download */}
                        <button
                            onClick={() => downloadDoc(selectedDoc.id)}
                            className="inline-block mt-4 px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow"
                        >
                            Download
                        </button>

                    </aside>
                )}
            </main>
        </div>
    );
}