import { useEffect, useState } from "react";
import { toast, ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";

type Tag = {
    id: string;
    name: string;
    color?: string;
};

type Document = {
    id: string;
    originalFilename: string;
    contentType: string;
    size: number;
    uploadedAt: string;
    summary?: string;
    tags: Tag[];
};

type SearchResult = {
    id: string;
    filename: string;
    contentType: string;
    uploadedAt: string;
    snippet: string;
    score: number;
};

const formatSize = (size: number) => `${(size / 1024).toFixed(2)} KB`;

export default function App() {
    const [docs, setDocs] = useState<Document[]>([]);
    const [tags, setTags] = useState<Tag[]>([]);
    const [selectedDoc, setSelectedDoc] = useState<Document | null>(null);
    const [searchTerm, setSearchTerm] = useState("");
    const [searchResults, setSearchResults] = useState<SearchResult[]>([]);
    const [newTag, setNewTag] = useState({ name: "", color: "#6b7280" });
    const [attachTagId, setAttachTagId] = useState("");

    useEffect(() => {
        loadDocs();
        loadTags();
    }, []);

    const loadDocs = async () => {
        try {
            const res = await fetch("/api/documents");
            if (!res.ok) throw new Error("Failed to load documents");
            const data = await res.json();
            setDocs(data);
            if (selectedDoc) {
                const refreshed = data.find((d: Document) => d.id === selectedDoc.id);
                if (refreshed) {
                    setSelectedDoc(refreshed);
                }
            }
        } catch (e) {
            console.error(e);
            toast.error("Could not load documents");
        }
    };

    const loadTags = async () => {
        try {
            const res = await fetch("/api/tags");
            if (!res.ok) throw new Error("Failed to load tags");
            setTags(await res.json());
        } catch (e) {
            console.error(e);
            toast.error("Could not load tags");
        }
    };

    const uploadDoc = async (file: File) => {
        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch("/api/documents/upload", {
                method: "POST",
                body: formData,
            });
            if (!res.ok) throw new Error("Upload failed");
            const doc: Document = await res.json();
            setDocs((prev) => [...prev, doc]);
            toast.success(`Uploaded ${file.name}`);
        } catch (e) {
            toast.error(`Upload failed: ${(e as Error).message}`);
        }
    };

    const replaceDoc = async (id: string, file: File) => {
        const formData = new FormData();
        formData.append("file", file);

        try {
            const res = await fetch(`/api/documents/${id}/replace`, {
                method: "PUT",
                body: formData,
            });
            if (!res.ok) throw new Error("Replace failed");
            const doc: Document = await res.json();
            setDocs((prev) => prev.map((d) => (d.id === doc.id ? doc : d)));
            setSelectedDoc(doc);
            toast.success(`Replaced with ${file.name}`);
        } catch (e) {
            toast.error(`Replace failed: ${(e as Error).message}`);
        }
    };

    const deleteDoc = async () => {
        if (!selectedDoc) return;
        try {
            const res = await fetch(`/api/documents/${selectedDoc.id}`, { method: "DELETE" });
            if (!res.ok) throw new Error("Delete failed");
            setDocs((prev) => prev.filter((d) => d.id !== selectedDoc.id));
            setSelectedDoc(null);
            toast.success("Document deleted");
        } catch (e) {
            toast.error(`Delete failed: ${(e as Error).message}`);
        }
    };

    const downloadDoc = async (id: string) => {
        try {
            const res = await fetch(`/api/documents/download/${id}`);
            if (!res.ok) throw new Error("Download failed");

            const disposition = res.headers.get("Content-Disposition");
            const match = disposition && disposition.match(/filename="(.+)"/);
            const filename = match ? match[1] : "document";
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);

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
            toast.error(`Download failed: ${(e as Error).message}`);
        }
    };

    const handleSearch = async () => {
        if (!searchTerm.trim()) {
            setSearchResults([]);
            return;
        }
        try {
            const res = await fetch(`/api/search?q=${encodeURIComponent(searchTerm)}`);
            if (!res.ok) throw new Error("Search failed");
            setSearchResults(await res.json());
        } catch (e) {
            toast.error(`Search failed: ${(e as Error).message}`);
        }
    };

    const selectDocumentById = async (id: string) => {
        const cached = docs.find((d) => d.id === id);
        if (cached) {
            setSelectedDoc(cached);
            return;
        }
        try {
            const res = await fetch(`/api/documents/${id}`);
            if (!res.ok) throw new Error("Document not found");
            const doc: Document = await res.json();
            setSelectedDoc(doc);
            setDocs((prev) => [...prev, doc]);
        } catch (e) {
            toast.error((e as Error).message);
        }
    };

    const createTag = async () => {
        if (!newTag.name.trim()) {
            toast.warn("Tag name required");
            return;
        }
        try {
            const res = await fetch("/api/tags", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newTag),
            });
            if (!res.ok) throw new Error("Could not create tag");
            const created: Tag = await res.json();
            setTags((prev) => [...prev, created]);
            setNewTag({ name: "", color: "#6b7280" });
            toast.success(`Tag '${created.name}' created`);
        } catch (e) {
            toast.error((e as Error).message);
        }
    };

    const attachTag = async () => {
        if (!selectedDoc || !attachTagId) return;
        try {
            const res = await fetch(`/api/documents/${selectedDoc.id}/tags/${attachTagId}`, {
                method: "POST",
            });
            if (!res.ok) throw new Error("Could not attach tag");
            const updated: Document = await res.json();
            setDocs((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
            setSelectedDoc(updated);
            toast.success("Tag added");
        } catch (e) {
            toast.error((e as Error).message);
        }
    };

    const removeTag = async (tagId: string) => {
        if (!selectedDoc) return;
        try {
            const res = await fetch(`/api/documents/${selectedDoc.id}/tags/${tagId}`, {
                method: "DELETE",
            });
            if (!res.ok) throw new Error("Could not remove tag");
            const updated: Document = await res.json();
            setDocs((prev) => prev.map((d) => (d.id === updated.id ? updated : d)));
            setSelectedDoc(updated);
        } catch (e) {
            toast.error((e as Error).message);
        }
    };

    return (
        <div className="min-h-screen bg-gradient-to-br from-neutral-950 via-neutral-900 to-slate-900 text-gray-50">
            <ToastContainer position="top-right" autoClose={3000} />
            <header className="border-b border-neutral-800 px-8 py-6 flex items-center justify-between">
                <div>
                    <p className="text-sm uppercase tracking-[0.25rem] text-gray-400">Document Manager</p>
                    <h1 className="text-3xl font-bold text-white mt-1">Search, tag and ship files</h1>
                </div>
                <div className="flex gap-2">
                    <button
                        onClick={() => document.getElementById("uploadInput")?.click()}
                        className="rounded-md bg-indigo-500 px-3 py-2 text-sm font-semibold text-white hover:bg-indigo-400"
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
                    <button
                        disabled={!selectedDoc}
                        onClick={() => document.getElementById("replaceInput")?.click()}
                        className="rounded-md border border-indigo-300/30 px-3 py-2 text-sm font-semibold text-indigo-200 disabled:opacity-30"
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
                    <button
                        disabled={!selectedDoc}
                        onClick={deleteDoc}
                        className="rounded-md border border-rose-400/40 px-3 py-2 text-sm font-semibold text-rose-200 disabled:opacity-30"
                    >
                        Delete
                    </button>
                </div>
            </header>

            <main className="grid grid-cols-1 lg:grid-cols-[2fr_1fr] gap-6 p-8">
                <section className="rounded-2xl border border-neutral-800 bg-neutral-900/70 p-6 shadow-xl">
                    <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                        <div className="flex items-center gap-2">
                            <input
                                value={searchTerm}
                                onChange={(e) => setSearchTerm(e.target.value)}
                                placeholder="Search in OCR text, summaries, filenames..."
                                className="w-80 rounded-md border border-neutral-700 bg-neutral-800 px-3 py-2 text-sm text-white focus:border-indigo-400 focus:outline-none"
                            />
                            <button
                                onClick={handleSearch}
                                className="rounded-md bg-indigo-500 px-3 py-2 text-sm font-semibold text-white hover:bg-indigo-400"
                            >
                                Search
                            </button>
                        </div>
                        <div className="text-sm text-gray-400">
                            {docs.length} document{docs.length === 1 ? "" : "s"} indexed
                        </div>
                    </div>

                    {searchResults.length > 0 && (
                        <div className="mt-4 rounded-xl border border-indigo-500/30 bg-indigo-950/30 p-4">
                            <div className="flex items-center justify-between">
                                <h3 className="text-indigo-200 font-semibold">Search results</h3>
                                <button
                                    className="text-xs text-indigo-200 hover:underline"
                                    onClick={() => setSearchResults([])}
                                >
                                    Clear
                                </button>
                            </div>
                            <div className="mt-3 space-y-2">
                                {searchResults.map((hit) => (
                                    <button
                                        key={hit.id}
                                        onClick={() => selectDocumentById(hit.id)}
                                        className="w-full rounded-lg border border-transparent bg-neutral-800 px-3 py-2 text-left hover:border-indigo-400/60"
                                    >
                                        <div className="flex items-center justify-between">
                                            <div className="font-semibold text-white">{hit.filename}</div>
                                            <div className="text-xs text-gray-400">{hit.score.toFixed(2)}</div>
                                        </div>
                                        <p className="text-sm text-gray-300 line-clamp-2">{hit.snippet}</p>
                                    </button>
                                ))}
                            </div>
                        </div>
                    )}

                    <div className="mt-6 overflow-x-auto rounded-xl border border-neutral-800">
                        <table className="w-full text-left text-sm">
                            <thead className="bg-neutral-800 text-gray-400 uppercase tracking-wide">
                                <tr>
                                    <th className="px-3 py-3">Filename</th>
                                    <th className="px-3 py-3">Type</th>
                                    <th className="px-3 py-3">Size</th>
                                    <th className="px-3 py-3">Uploaded</th>
                                </tr>
                            </thead>
                            <tbody>
                                {docs.map((doc) => (
                                    <tr
                                        key={doc.id}
                                        onClick={() => setSelectedDoc(doc)}
                                        className={`cursor-pointer border-b border-neutral-800 hover:bg-neutral-800/70 ${
                                            selectedDoc?.id === doc.id ? "bg-indigo-900/30" : ""
                                        }`}
                                    >
                                        <td className="px-3 py-3 text-white">{doc.originalFilename}</td>
                                        <td className="px-3 py-3 text-gray-300">{doc.contentType}</td>
                                        <td className="px-3 py-3 text-gray-300">{formatSize(doc.size)}</td>
                                        <td className="px-3 py-3 text-gray-400">
                                            {new Date(doc.uploadedAt).toLocaleString()}
                                        </td>
                                    </tr>
                                ))}
                            </tbody>
                        </table>
                    </div>
                </section>

                <aside className="rounded-2xl border border-neutral-800 bg-neutral-900/80 p-6 shadow-xl">
                    {!selectedDoc ? (
                        <p className="text-gray-400">Select a document to see details.</p>
                    ) : (
                        <>
                            <div className="flex items-start justify-between">
                                <div>
                                    <p className="text-xs uppercase tracking-widest text-gray-500">Selected</p>
                                    <h3 className="text-xl font-semibold text-white">{selectedDoc.originalFilename}</h3>
                                    <p className="text-sm text-gray-400">{selectedDoc.contentType}</p>
                                </div>
                                <button
                                    onClick={() => setSelectedDoc(null)}
                                    className="rounded-full border border-neutral-700 px-2 py-1 text-xs text-gray-300 hover:border-indigo-400 hover:text-white"
                                >
                                    Close
                                </button>
                            </div>

                            <div className="mt-4 space-y-1 text-sm text-gray-300">
                                <div>ID: {selectedDoc.id}</div>
                                <div>Size: {formatSize(selectedDoc.size)}</div>
                                <div>Uploaded: {new Date(selectedDoc.uploadedAt).toLocaleString()}</div>
                            </div>

                            <div className="mt-4 flex gap-2">
                                <button
                                    onClick={() => downloadDoc(selectedDoc.id)}
                                    className="rounded-md bg-indigo-500 px-3 py-2 text-sm font-semibold text-white hover:bg-indigo-400"
                                >
                                    Download
                                </button>
                                <button
                                    onClick={() => document.getElementById("replaceInput")?.click()}
                                    className="rounded-md border border-indigo-300/30 px-3 py-2 text-sm font-semibold text-indigo-200"
                                >
                                    Replace
                                </button>
                            </div>

                            <div className="mt-5">
                                <p className="text-sm font-semibold text-gray-200">Summary</p>
                                <p className="mt-2 rounded-lg border border-neutral-800 bg-neutral-950/60 p-3 text-sm text-gray-200 whitespace-pre-wrap">
                                    {selectedDoc.summary && selectedDoc.summary.trim().length > 0
                                        ? selectedDoc.summary
                                        : "Summary not generated yet."}
                                </p>
                            </div>

                            <div className="mt-6 space-y-3">
                                <div className="flex items-center justify-between">
                                    <p className="text-sm font-semibold text-gray-200">Tags</p>
                                    <div className="flex items-center gap-2">
                                        <select
                                            value={attachTagId}
                                            onChange={(e) => setAttachTagId(e.target.value)}
                                            className="rounded-md border border-neutral-700 bg-neutral-800 px-2 py-1 text-sm text-white"
                                        >
                                            <option value="">Attach tag...</option>
                                            {tags.map((tag) => (
                                                <option value={tag.id} key={tag.id}>
                                                    {tag.name}
                                                </option>
                                            ))}
                                        </select>
                                        <button
                                            onClick={attachTag}
                                            className="rounded-md bg-indigo-500 px-2 py-1 text-xs font-semibold text-white hover:bg-indigo-400"
                                        >
                                            Add
                                        </button>
                                    </div>
                                </div>

                                <div className="flex flex-wrap gap-2">
                                    {selectedDoc.tags?.length ? (
                                        selectedDoc.tags.map((tag) => (
                                            <span
                                                key={tag.id}
                                                className="inline-flex items-center gap-2 rounded-full border border-neutral-700 bg-neutral-800 px-3 py-1 text-xs text-white"
                                                style={{ borderColor: tag.color ?? "#4b5563" }}
                                            >
                                                <span
                                                    className="h-2 w-2 rounded-full"
                                                    style={{ backgroundColor: tag.color ?? "#4b5563" }}
                                                ></span>
                                                {tag.name}
                                                <button
                                                    onClick={() => removeTag(tag.id)}
                                                    className="text-gray-400 hover:text-white"
                                                >
                                                    ×
                                                </button>
                                            </span>
                                        ))
                                    ) : (
                                        <span className="text-sm text-gray-400">No tags yet.</span>
                                    )}
                                </div>

                                <div className="rounded-lg border border-neutral-800 bg-neutral-950/50 p-3">
                                    <p className="text-xs uppercase tracking-wide text-gray-400">Create tag</p>
                                    <div className="mt-2 flex flex-col gap-2">
                                        <input
                                            value={newTag.name}
                                            onChange={(e) => setNewTag({ ...newTag, name: e.target.value })}
                                            placeholder="Name"
                                            className="rounded-md border border-neutral-700 bg-neutral-800 px-3 py-2 text-sm text-white focus:border-indigo-400 focus:outline-none"
                                        />
                                        <div className="flex items-center gap-2">
                                            <input
                                                type="color"
                                                value={newTag.color}
                                                onChange={(e) => setNewTag({ ...newTag, color: e.target.value })}
                                                className="h-10 w-16 rounded-md border border-neutral-700 bg-neutral-800"
                                            />
                                            <button
                                                onClick={createTag}
                                                className="rounded-md bg-indigo-500 px-3 py-2 text-sm font-semibold text-white hover:bg-indigo-400"
                                            >
                                                Save tag
                                            </button>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </aside>
            </main>
        </div>
    );
}
