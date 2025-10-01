import { useEffect, useState } from "react";

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

    // Form state
    const [form, setForm] = useState({
        originalFilename: "",
        contentType: "",
        size: "",
    });
    const [showForm, setShowForm] = useState<null | "create" | "update">(null);

    useEffect(() => {
        fetch("/api/documents")
            .then((res) => res.json())
            .then(setDocs)
            .catch((err) => console.error("Error loading docs:", err));
    }, []);

    const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setForm({ ...form, [e.target.name]: e.target.value });
    };

    const createDoc = () => {
        fetch("/api/documents", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                originalFilename: form.originalFilename,
                contentType: form.contentType,
                size: Number(form.size),
            }),
        })
            .then((res) => res.json())
            .then((doc) => {
                setDocs([...docs, doc]);
                closeForm();
            });
    };

    const updateDoc = () => {
        if (!selectedDoc) return;
        fetch(`/api/documents/${selectedDoc.id}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
                originalFilename: form.originalFilename,
                contentType: form.contentType,
                size: Number(form.size),
            }),
        })
            .then((res) => res.json())
            .then((doc) => {
                setDocs(docs.map((d) => (d.id === doc.id ? doc : d)));
                setSelectedDoc(doc);
                closeForm();
            });
    };

    const deleteDoc = () => {
        if (!selectedDoc) return;
        fetch(`/api/documents/${selectedDoc.id}`, { method: "DELETE" }).then(() => {
            setDocs(docs.filter((d) => d.id !== selectedDoc.id));
            setSelectedDoc(null);
        });
    };

    const openForm = (mode: "create" | "update") => {
        if (mode === "update" && selectedDoc) {
            setForm({
                originalFilename: selectedDoc.originalFilename,
                contentType: selectedDoc.contentType,
                size: selectedDoc.size.toString(),
            });
        } else {
            setForm({ originalFilename: "", contentType: "", size: "" });
        }
        setShowForm(mode);
    };

    const closeForm = () => {
        setShowForm(null);
        setForm({ originalFilename: "", contentType: "", size: "" });
    };

    return (
        <div className="min-h-screen bg-gray-100 flex flex-col">
            {/* Header */}
            <header className="bg-white shadow p-6 text-center">
                <h1 className="text-4xl font-bold text-gray-800">Documentmanager</h1>
                <p className="text-gray-500 mt-2">by Brandtner Niklas, Lechner Sonja and Vakorin Denis</p>
            </header>

            {/* Content Area */}
            <main className="flex-1 p-8 flex gap-6">
                {/* Table Area */}
                <div className="flex-1 bg-white rounded-lg shadow p-6 overflow-x-auto">
                    <div className="flex justify-between items-center mb-4 border-b pb-2">
                        <h2 className="text-xl font-semibold text-gray-700">Documents</h2>
                        <div className="space-x-2">
                            <button
                                onClick={() => openForm("create")}
                                className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                            >
                                Create
                            </button>
                            <button
                                disabled={!selectedDoc}
                                onClick={() => openForm("update")}
                                className="px-3 py-1 bg-yellow-500 text-white rounded hover:bg-yellow-600 disabled:opacity-50"
                            >
                                Update
                            </button>
                            <button
                                disabled={!selectedDoc}
                                onClick={deleteDoc}
                                className="px-3 py-1 bg-red-600 text-white rounded hover:bg-red-700 disabled:opacity-50"
                            >
                                Delete
                            </button>
                        </div>
                    </div>

                    {/* Documents Table */}
                    <table className="w-full text-left border-collapse">
                        <thead>
                        <tr className="bg-gray-50">
                            <th className="p-3 border-b">Filename</th>
                            <th className="p-3 border-b">Type</th>
                            <th className="p-3 border-b">Size</th>
                            <th className="p-3 border-b">Uploaded</th>
                        </tr>
                        </thead>
                        <tbody>
                        {docs.map((doc) => (
                            <tr
                                key={doc.id}
                                onClick={() => setSelectedDoc(doc)}
                                className={`cursor-pointer hover:bg-gray-100 ${
                                    selectedDoc?.id === doc.id ? "bg-blue-50" : ""
                                }`}
                            >
                                <td className="p-3 border-b">{doc.originalFilename}</td>
                                <td className="p-3 border-b">{doc.contentType}</td>
                                <td className="p-3 border-b">{(doc.size / 1024).toFixed(2)} KB</td>
                                <td className="p-3 border-b">{new Date(doc.uploadedAt).toLocaleString()}</td>
                            </tr>
                        ))}
                        </tbody>
                    </table>
                </div>

                {/* Details Pane */}
                {selectedDoc && (
                    <aside className="w-80 bg-white rounded-lg shadow p-6 relative">
                        <button
                            onClick={() => setSelectedDoc(null)}
                            className="absolute top-3 right-3 text-gray-400 hover:text-gray-600"
                        >
                            ✕
                        </button>
                        <h3 className="text-lg font-semibold mb-4">Details</h3>
                        <p><span className="font-medium">ID:</span> {selectedDoc.id}</p>
                        <p><span className="font-medium">Filename:</span> {selectedDoc.originalFilename}</p>
                        <p><span className="font-medium">Type:</span> {selectedDoc.contentType}</p>
                        <p><span className="font-medium">Size:</span> {(selectedDoc.size / 1024).toFixed(2)} KB</p>
                        <p><span className="font-medium">Uploaded:</span> {new Date(selectedDoc.uploadedAt).toLocaleString()}</p>
                    </aside>
                )}

                {/* Modal Form */}
                {showForm && (
                    <div className="fixed inset-0 flex items-center justify-center bg-black bg-opacity-50 z-50">
                        <div className="bg-white p-6 rounded-lg shadow-lg w-96 relative">
                            <button
                                onClick={closeForm}
                                className="absolute top-3 right-3 text-gray-400 hover:text-gray-600"
                            >
                                ✕
                            </button>
                            <h3 className="text-xl font-semibold mb-4">
                                {showForm === "create" ? "Create Document" : "Update Document"}
                            </h3>
                            <input
                                name="originalFilename"
                                placeholder="Filename"
                                value={form.originalFilename}
                                onChange={handleChange}
                                className="w-full mb-2 p-2 border rounded"
                            />
                            <input
                                name="contentType"
                                placeholder="Content Type"
                                value={form.contentType}
                                onChange={handleChange}
                                className="w-full mb-2 p-2 border rounded"
                            />
                            <input
                                name="size"
                                placeholder="Size (bytes)"
                                value={form.size}
                                onChange={handleChange}
                                className="w-full mb-4 p-2 border rounded"
                            />

                            <div className="flex justify-end space-x-2">
                                <button
                                    onClick={closeForm}
                                    className="px-3 py-1 bg-gray-300 rounded hover:bg-gray-400"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={showForm === "create" ? createDoc : updateDoc}
                                    className="px-3 py-1 bg-blue-600 text-white rounded hover:bg-blue-700"
                                >
                                    {showForm === "create" ? "Create" : "Update"}
                                </button>
                            </div>
                        </div>
                    </div>
                )}
            </main>
        </div>
    );
}
