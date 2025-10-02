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
        <div className="min-h-screen bg-neutral-900 text-gray-100 flex flex-col">
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
                            <button
                                onClick={() => openForm("create")}
                                className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow"
                            >
                                Create
                            </button>
                            <button
                                disabled={!selectedDoc}
                                onClick={() => openForm("update")}
                                className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 text-purple-400 rounded shadow disabled:opacity-40"
                            >
                                Update
                            </button>
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
                    </aside>
                )}

                {/* Modal Form */}
                {showForm && (
                    <div className="fixed inset-0 bg-neutral-800/70 flex items-center justify-center  z-50">
                        <div className="bg-neutral-800 p-6 rounded-lg shadow-xl w-96 relative border border-neutral-700">
                            <button
                                onClick={closeForm}
                                className="absolute top-3 right-3 text-neutral-400 hover:text-purple-500"
                            >
                                ✕
                            </button>
                            <h3 className="text-xl font-semibold mb-4 text-purple-400">
                                {showForm === "create" ? "Create Document" : "Update Document"}
                            </h3>
                            <input
                                name="originalFilename"
                                placeholder="Filename"
                                value={form.originalFilename}
                                onChange={handleChange}
                                className="w-full mb-2 p-2 border border-neutral-600 rounded bg-neutral-900 text-neutral-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
                            />
                            <input
                                name="contentType"
                                placeholder="Content Type"
                                value={form.contentType}
                                onChange={handleChange}
                                className="w-full mb-2 p-2 border border-neutral-600 rounded bg-neutral-900 text-neutral-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
                            />
                            <input
                                name="size"
                                placeholder="Size (bytes)"
                                value={form.size}
                                onChange={handleChange}
                                className="w-full mb-4 p-2 border border-neutral-600 rounded bg-neutral-900 text-neutral-100 focus:outline-none focus:ring-2 focus:ring-purple-500"
                            />

                            <div className="flex justify-end space-x-2">
                                <button
                                    onClick={closeForm}
                                    className="px-3 py-1 bg-neutral-700 hover:bg-neutral-600 rounded text-neutral-200"
                                >
                                    Cancel
                                </button>
                                <button
                                    onClick={showForm === "create" ? createDoc : updateDoc}
                                    className="px-3 py-1 bg-purple-600 hover:bg-purple-700 text-white rounded"
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
