"use client";
import { useEffect, useState } from "react";
import {
  Activity,
  BarChart3,
  BookOpen,
  ChevronRight,
  Database,
  Gauge,
  GitBranch,
  Layers3,
  List,
  Network,
  Plus,
  Search,
  Settings2,
  Sparkles,
  Trash2,
  Upload,
} from "lucide-react";

const algorithms = [
  { label: "Brute Force", value: "bruteforce" },
  { label: "KD-Tree", value: "kdtree" },
  { label: "HNSW", value: "hnsw" },
];

const metrics = [
  { label: "Cosine", value: "cosine" },
  { label: "Euclidean", value: "euclidean" },
  { label: "Manhattan", value: "manhattan" },
];

type DocumentSummary = {
  id: number;
  title: string;
  preview: string;
  words: number;
};

export default function Home() {
    const [documentTitle, setDocumentTitle] = useState("");
    const [documentText, setDocumentText] = useState("");
      const [vectorCount, setVectorCount] = useState(0);
      const [dimensions, setDimensions] = useState(16);
      const [selectedAlgorithm, setSelectedAlgorithm] = useState("hnsw");
      const [selectedMetric, setSelectedMetric] = useState("cosine");
      const [documents, setDocuments] = useState<DocumentSummary[]>([]);
      const [queryVector, setQueryVector] = useState("");
      const [activeTab, setActiveTab] = useState("results");
      const [searchResults, setSearchResults] = useState<
        { id: number; distance: number }[]
      >([]);
      const [searchLatency, setSearchLatency] = useState<number | null>(null);
      const [searching, setSearching] = useState(false);
      const [documentCount, setDocumentCount] = useState(0);
      const [hnswInfo, setHnswInfo] = useState<{
        topLayer: number;
        nodeCount: number;
      } | null>(null);
      const [benchmark, setBenchmark] = useState<{
        bruteforceUs: number;
        kdtreeUs: number;
        hnswUs: number;
        itemCount: number;
      } | null>(null);

      const [benchmarking, setBenchmarking] = useState(false);
      const [ragQuestion, setRagQuestion] = useState("");
      const [ragAnswer, setRagAnswer] = useState("");
      const [asking, setAsking] = useState(false);
      const [vectorMetadata, setVectorMetadata] = useState("");
      const [vectorCategory, setVectorCategory] = useState("math");
      const [vectorEmbedding, setVectorEmbedding] = useState("");
      const [inserting, setInserting] = useState(false);

      const normalizeDocuments = (payload: unknown): DocumentSummary[] => {
        const rawDocuments = Array.isArray(payload)
          ? payload
          : payload && typeof payload === "object" && "documents" in payload && Array.isArray(payload.documents)
            ? payload.documents
            : [];

        return rawDocuments.map((document) => {
          const item = document as {
            id?: number;
            title?: string;
            preview?: string;
            words?: number;
            text?: string;
          };

          const text = typeof item.text === "string" ? item.text : "";
          const preview =
            typeof item.preview === "string"
              ? item.preview
              : text.length > 120
                ? `${text.slice(0, 120)}…`
                : text;

          const words =
            typeof item.words === "number"
              ? item.words
              : text.trim().length === 0
                ? 0
                : text.trim().split(/\s+/).length;

          return {
            id: Number(item.id ?? 0),
            title: item.title ?? "Untitled document",
            preview,
            words,
          };
        });
      };

      const loadDocuments = async () => {
        const response = await fetch(
          `${process.env.NEXT_PUBLIC_API_URL}/doc/list`
        );

        if (!response.ok) {
          throw new Error("Unable to load documents");
        }

        const payload = await response.json();
        const normalizedDocuments = normalizeDocuments(payload);

        setDocuments(normalizedDocuments);
        setDocumentCount(normalizedDocuments.length);
      };

      useEffect(() => {
        Promise.all([
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/stats`)
            .then((response) => response.json()),
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/status`)
            .then((response) => response.json()),
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/hnsw-info`)
            .then((response) => response.json()),
          fetch(`${process.env.NEXT_PUBLIC_API_URL}/doc/list`)
            .then((response) => response.json()),
        ])
          .then(([stats, status, hnsw, documentList]) => {
            const normalizedDocuments = normalizeDocuments(documentList);

            setVectorCount(stats.count);
            setDimensions(stats.dims);
            setDocumentCount(
              normalizedDocuments.length > 0
                ? normalizedDocuments.length
                : status.docCount ?? 0
            );
            setHnswInfo(hnsw);
            setDocuments(normalizedDocuments);
          })
          .catch(() => {
            // Keep the UI usable if the backend is temporarily unavailable.
          });
      }, []);
  return (
    <main className="vectorx-enter min-h-screen bg-[#171613] text-[#e8e2d8]">
      {/* Top navigation */}
      <header className="border-b border-[#35322c] bg-[#1c1a17]">
        <div className="flex h-16 items-center justify-between px-7">
          <div className="flex items-center gap-4">
            <div className="flex h-9 w-9 items-center justify-center border border-[#b56a3b] text-[#d78b57]">
              <GitBranch size={18} />
            </div>

            <div>
              <div className="text-sm font-semibold tracking-[0.22em]">
                VECTORX
              </div>
              <div className="text-[10px] uppercase tracking-[0.18em] text-[#817a70]">
                Vector database laboratory
              </div>
            </div>
          </div>

          <div className="flex items-center gap-3">
            <div className="flex items-center gap-2 border border-[#35322c] px-3 py-1.5 text-[11px] text-[#aaa196]">
              <span className="h-1.5 w-1.5 rounded-full bg-[#b56a3b]" />
                {vectorCount} vectors
            </div>

            <div className="flex items-center gap-2 border border-[#35322c] px-3 py-1.5 text-[11px] text-[#aaa196]">
              <Activity size={13} />
                {dimensions} dimensions
            </div>
          </div>
        </div>
      </header>

      {/* Main workspace */}
      <div className="grid min-h-[calc(100vh-64px)] grid-cols-[260px_minmax(0,1fr)_360px]">
        {/* Left control rail */}
        <aside className="border-r border-[#35322c] bg-[#1c1a17] p-5">
          <div className="mb-7">
            <SectionLabel>Search</SectionLabel>

            <div className="relative mt-3">
              <Search
                size={15}
                className="absolute left-3 top-3.5 text-[#817a70]"
              />
              <input
                className="h-11 w-full border border-[#35322c] bg-[#141310] pl-10 pr-3 text-xs outline-none transition focus:border-[#b56a3b]"
                placeholder="Query vector..."
                value={queryVector}
                onChange={(e) => setQueryVector(e.target.value)}
              />
            </div>

            <button
              onClick={async () => {
                if (!queryVector.trim()) return;

                setSearching(true);

                try {
                  const response = await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL}/search?v=${encodeURIComponent(
                      queryVector
                    )}&k=5&metric=${selectedMetric}&algo=${selectedAlgorithm}`
                  );

                  const data = await response.json();

                  setSearchResults(data.results ?? []);
                  setSearchLatency(data.latencyUs ?? null);
                } catch {
                  setSearchResults([]);
                  setSearchLatency(null);
                } finally {
                  setSearching(false);
                }
              }}
              className="mt-2 flex h-10 w-full items-center justify-center gap-2 bg-[#b56a3b] text-xs font-medium text-[#171613] transition hover:bg-[#d78b57]"
            >
              <Search size={14} />
              {searching ? "Searching..." : "Search"}
            </button>
            </div>

            <div className="mb-7">
              <SectionLabel>Algorithm</SectionLabel>

            <div className="mt-3 space-y-1.5">
              {algorithms.map((algorithm) => (
                <button
                  key={algorithm.value}
                  onClick={() => setSelectedAlgorithm(algorithm.value)}
                  className={`flex w-full items-center justify-between border px-3 py-2.5 text-left text-xs transition ${
                    selectedAlgorithm === algorithm.value
                      ? "border-[#b56a3b] bg-[#2a211b] text-[#d78b57]"
                      : "border-transparent text-[#817a70] hover:border-[#35322c] hover:text-[#d0c8bc]"
                  }`}
                >
                  <span>{algorithm.label}</span>

                  {selectedAlgorithm === algorithm.value && (
                    <ChevronRight size={13} />
                  )}
                </button>
              ))}
            </div>
          </div>

          <div className="mb-7">
            <SectionLabel>Distance metric</SectionLabel>

            <div className="mt-3 space-y-1.5">
              {metrics.map((metric) => (
                <button
                  key={metric.value}
                  onClick={() => setSelectedMetric(metric.value)}
                  className={`w-full border px-3 py-2 text-left text-xs transition ${
                    selectedMetric === metric.value
                      ? "border-[#5e574d] bg-[#25221d] text-[#e8e2d8]"
                      : "border-transparent text-[#817a70] hover:border-[#35322c]"
                  }`}
                >
                  {metric.label}
                </button>
              ))}
            </div>
          </div>

          <div className="border-t border-[#35322c] pt-5">
            <SectionLabel>Insert vector</SectionLabel>

            <input
              className="mt-3 h-10 w-full border border-[#35322c] bg-[#141310] px-3 text-xs outline-none focus:border-[#b56a3b]"
              placeholder="Metadata"
              value={vectorMetadata}
              onChange={(e) => setVectorMetadata(e.target.value)}
            />

            <select
              value={vectorCategory}
              onChange={(e) => setVectorCategory(e.target.value)}
              className="mt-2 h-10 w-full border border-[#35322c] bg-[#141310] px-3 text-xs text-[#aaa196] outline-none"
            >
              <option>math</option>
              <option>cs</option>
              <option>food</option>
              <option>sports</option>
            </select>
            <input
              className="mt-2 h-10 w-full border border-[#35322c] bg-[#141310] px-3 text-xs outline-none focus:border-[#b56a3b]"
              placeholder="16D vector, comma-separated"
              value={vectorEmbedding}
              onChange={(e) => setVectorEmbedding(e.target.value)}
            />

            <button
              onClick={async () => {
                if (!vectorEmbedding.trim()) return;

                setInserting(true);

                try {
                  const embedding = vectorEmbedding
                    .split(",")
                    .map((value) => Number(value.trim()));

                  if (embedding.length !== 16 || embedding.some(Number.isNaN)) {
                    return;
                  }

                  const response = await fetch(
                    `${process.env.NEXT_PUBLIC_API_URL}/insert`,
                    {
                      method: "POST",
                      headers: {
                        "Content-Type": "application/json",
                      },
                      body: JSON.stringify({
                        meta: vectorMetadata,
                        cat: vectorCategory,
                        emb: embedding,
                      }),
                    }
                  );

                  const data = await response.json();

                  if (data.id) {
                    const statsResponse = await fetch(
                      `${process.env.NEXT_PUBLIC_API_URL}/stats`
                    );

                    const stats = await statsResponse.json();

                    setVectorCount(stats.count);
                    setVectorMetadata("");
                    setVectorEmbedding("");
                  }
                } finally {
                  setInserting(false);
                }
              }}
              className="mt-2 flex h-10 w-full items-center justify-center gap-2 bg-[#b56a3b] text-xs font-medium text-[#171613] transition hover:bg-[#d78b57]"
            >
              <Plus size={15} />
              {inserting ? "Adding..." : "Add vector"}
            </button>
          </div>
        </aside>

        {/* Visualization */}
        <section className="relative overflow-hidden bg-[#141310]">
          <div className="absolute left-7 top-6 z-10">
            <div className="flex items-center gap-2 text-[10px] uppercase tracking-[0.2em] text-[#817a70]">
              <Database size={13} />
              Semantic space
            </div>

            <h1 className="mt-2 text-2xl font-medium tracking-tight text-[#e8e2d8]">
              Vector map
            </h1>
          </div>

          <div className="absolute right-7 top-6 z-10 flex gap-2">
            <InfoPill
              icon={<Layers3 size={13} />}
              label={selectedAlgorithm.toUpperCase()}
            />
            <InfoPill
              icon={<Gauge size={13} />}
              label={`Layer ${hnswInfo?.topLayer ?? 0}`}
            />
          </div>

          {/* Graph */}
          <div className="absolute inset-0 flex items-center justify-center">
            <div className="relative h-[70%] w-[75%]">
              <div className="absolute inset-0 opacity-40 [background-image:linear-gradient(#35322c_1px,transparent_1px),linear-gradient(90deg,#35322c_1px,transparent_1px)] [background-size:42px_42px]" />

              {Array.from({ length: Math.min(vectorCount, 20) }).map((_, index) => {
                const positions = [
                  [15, 28],
                  [23, 61],
                  [31, 42],
                  [39, 70],
                  [47, 35],
                  [55, 54],
                  [64, 25],
                  [70, 67],
                  [78, 44],
                  [84, 57],
                  [61, 78],
                  [28, 79],
                  [18, 48],
                  [36, 18],
                  [52, 75],
                  [67, 40],
                  [80, 22],
                  [44, 62],
                  [73, 55],
                  [57, 30],
                ];

                const [left, top] = positions[index];

                return (
                  <div
                    key={index}
                    className="vectorx-node absolute h-2.5 w-2.5 rounded-full border border-[#d78b57] bg-[#171613] transition duration-500 hover:scale-150"
                    style={{ left: `${left}%`, top: `${top}%` }}
                  />
                );
              })}

              <div className="absolute left-[55%] top-[52%] h-5 w-5 -translate-x-1/2 -translate-y-1/2 animate-pulse border border-[#d78b57] bg-[#b56a3b]" />

              <div className="absolute bottom-0 left-0 text-[10px] text-[#5f5a52]">
                PCA projection
              </div>
            </div>
          </div>

          <div className="absolute bottom-6 left-7 right-7 flex items-end justify-between">
            <div>
              <div className="text-[10px] uppercase tracking-[0.18em] text-[#817a70]">
                Query latency
              </div>
              <div className="mt-1 text-3xl font-light text-[#d78b57]">
                {searchLatency ?? "--"}
                <span className="ml-2 text-xs text-[#817a70]">μs</span>
              </div>
            </div>

            <div className="text-right text-[10px] leading-5 text-[#817a70]">
              {selectedAlgorithm} · {selectedMetric}
              <br />
              {hnswInfo?.nodeCount ?? vectorCount} nodes · layer{" "}
              {hnswInfo?.topLayer ?? 0}
            </div>
          </div>
        </section>

        {/* Right information panel */}
        <aside className="flex flex-col border-l border-[#35322c] bg-[#1c1a17]">
          <div className="flex border-b border-[#35322c]">
            <Tab
              icon={<List size={13} />}
              label="Results"
              active={activeTab === "results"}
              onClick={() => setActiveTab("results")}
            />

            <Tab
              icon={<BarChart3 size={13} />}
              label="Benchmark"
              active={activeTab === "benchmark"}
              onClick={() => setActiveTab("benchmark")}
            />

            <Tab
              icon={<Network size={13} />}
              label="HNSW"
              active={activeTab === "hnsw"}
              onClick={() => setActiveTab("hnsw")}
            />
          </div>

          <div className="flex-1 overflow-y-auto p-5">
            {activeTab === "results" && (
              <>
                <SectionLabel>Nearest results</SectionLabel>

                <div className="mt-4 space-y-2">
                  {searchResults.length === 0 ? (
                    <div className="border border-[#35322c] bg-[#141310] p-4 text-center text-[10px] text-[#625d55]">
                      Run a search to see nearest vectors.
                    </div>
                  ) : (
                    searchResults.map((result, index) => (
                      <div
                        key={result.id}
                        className="border border-[#35322c] bg-[#141310] p-3 transition hover:border-[#5e574d]"
                      >
                        <div className="mb-2 text-[9px] uppercase tracking-[0.15em] text-[#625d55]">
                          Result 0{index + 1}
                        </div>

                        <div className="text-xs text-[#d0c8bc]">
                          Demo vector #{result.id}
                        </div>

                        <div className="mt-3 flex items-center justify-between">
                          <span className="border border-[#4b4037] px-2 py-1 text-[9px] text-[#b9825e]">
                            vector
                          </span>

                          <span className="text-[10px] text-[#817a70]">
                            distance {result.distance.toFixed(4)}
                          </span>
                        </div>
                      </div>
                    ))
                  )}
                </div>
              </>
            )}

            {activeTab === "benchmark" && (
              <>
                <SectionLabel>Algorithm benchmark</SectionLabel>

                <div className="mt-3 border border-[#35322c] bg-[#141310] p-4">
                  <div className="text-[10px] leading-5 text-[#817a70]">
                    Compare the search latency of the three available algorithms
                    using the current query vector.
                  </div>

                  <button
                    onClick={async () => {
                      if (!queryVector.trim()) return;

                      setBenchmarking(true);

                      try {
                        const response = await fetch(
                          `${process.env.NEXT_PUBLIC_API_URL}/benchmark?v=${encodeURIComponent(
                            queryVector
                          )}&k=5&metric=${selectedMetric}`
                        );

                        if (!response.ok) {
                          throw new Error("Benchmark request failed");
                        }

                        const data = await response.json();
                        setBenchmark(data);
                      } catch {
                        setBenchmark(null);
                      } finally {
                        setBenchmarking(false);
                      }
                    }}
                    disabled={benchmarking || !queryVector.trim()}
                    className="mt-4 flex w-full items-center justify-center gap-2 border border-[#5e574d] py-2.5 text-[10px] uppercase tracking-[0.12em] transition hover:border-[#b56a3b] hover:text-[#d78b57] disabled:cursor-not-allowed disabled:opacity-40"
                  >
                    <BarChart3 size={13} />
                    {benchmarking ? "Running benchmark..." : "Run benchmark"}
                  </button>
                </div>

                {benchmark && (
                  <div className="mt-3 border border-[#35322c] bg-[#141310] p-4">
                    <div className="mb-3 text-[9px] uppercase tracking-[0.15em] text-[#625d55]">
                      Benchmark results
                    </div>

                    <div className="space-y-2 text-[10px]">
                      <div className="flex justify-between">
                        <span className="text-[#817a70]">Brute Force</span>
                        <span className="text-[#d0c8bc]">
                          {benchmark.bruteforceUs} μs
                        </span>
                      </div>

                      <div className="flex justify-between">
                        <span className="text-[#817a70]">KD-Tree</span>
                        <span className="text-[#d0c8bc]">
                          {benchmark.kdtreeUs} μs
                        </span>
                      </div>

                      <div className="flex justify-between">
                        <span className="text-[#817a70]">HNSW</span>
                        <span className="text-[#d0c8bc]">
                          {benchmark.hnswUs} μs
                        </span>
                      </div>

                      <div className="mt-3 flex justify-between border-t border-[#35322c] pt-3">
                        <span className="text-[#625d55]">Vectors tested</span>
                        <span className="text-[#b9825e]">
                          {benchmark.itemCount}
                        </span>
                      </div>
                    </div>
                  </div>
                )}
              </>
            )}

            {activeTab === "hnsw" && hnswInfo && (
              <>
                <SectionLabel>HNSW structure</SectionLabel>

                <div className="mt-3 border border-[#35322c] bg-[#141310] p-4">
                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <div className="text-[9px] text-[#625d55]">Nodes</div>
                      <div className="mt-1 text-sm text-[#d0c8bc]">
                        {hnswInfo.nodeCount}
                      </div>
                    </div>

                    <div>
                      <div className="text-[9px] text-[#625d55]">Top layer</div>
                      <div className="mt-1 text-sm text-[#d0c8bc]">
                        {hnswInfo.topLayer}
                      </div>
                    </div>
                  </div>
                </div>
              </>
            )}

            <div className="my-7 border-t border-[#35322c]" />

            <SectionLabel>Documents</SectionLabel>

            <div className="mt-3 border border-[#35322c] bg-[#141310] p-4">
              <div className="flex items-center gap-2 text-xs">
                <BookOpen size={14} className="text-[#b9825e]" />
                Document collection
              </div>

              <input
                className="mt-3 h-9 w-full border border-[#35322c] bg-[#1c1a17] px-3 text-[10px] outline-none focus:border-[#b56a3b]"
                placeholder="Document title"
                value={documentTitle}
                onChange={(e) => setDocumentTitle(e.target.value)}
              />

              <textarea
                className="mt-2 min-h-24 w-full resize-none border border-[#35322c] bg-[#1c1a17] p-3 text-[10px] outline-none focus:border-[#b56a3b]"
                placeholder="Document text..."
                value={documentText}
                onChange={(e) => setDocumentText(e.target.value)}
              />

              <div className="mt-2 text-[10px] leading-5 text-[#817a70]">
                {documentCount} document{documentCount === 1 ? "" : "s"} in
                collection. Manage embedded documents and retrieve relevant
                chunks.
              </div>

              {documents.length > 0 && (
                <div className="mt-3 space-y-2">
                  {documents.map((document) => (
                    <div
                      key={document.id}
                      className="border border-[#35322c] bg-[#1c1a17] p-3"
                    >
                      <div className="flex items-center justify-between gap-3">
                        <div className="text-xs text-[#d0c8bc]">
                          {document.title}
                        </div>

                        <button
                          onClick={async () => {
                            try {
                              const response = await fetch(
                                `${process.env.NEXT_PUBLIC_API_URL}/doc/delete/${document.id}`,
                                {
                                  method: "DELETE",
                                }
                              );

                              if (response.ok) {
                                const documentsResponse = await fetch(
                                  `${process.env.NEXT_PUBLIC_API_URL}/doc/list`
                                );

                                const documentList =
                                  await documentsResponse.json();
                                const normalizedDocuments =
                                  normalizeDocuments(documentList);

                                setDocuments(normalizedDocuments);
                                setDocumentCount(normalizedDocuments.length);
                              }
                            } catch {
                              // Keep the UI usable if the backend is unavailable.
                            }
                          }}
                          className="text-[#817a70] transition hover:text-[#b56a3b]"
                          title="Delete document"
                        >
                          <Trash2 size={13} />
                        </button>
                      </div>

                      <div className="mt-1 text-[9px] leading-4 text-[#817a70]">
                        {document.preview}
                      </div>

                      <div className="mt-2 text-[9px] text-[#625d55]">
                        {document.words} words
                      </div>
                    </div>
                  ))}
                </div>
              )}

              <button
                onClick={async () => {
                  if (!documentTitle.trim() || !documentText.trim()) return;

                  try {
                    const response = await fetch(
                      `${process.env.NEXT_PUBLIC_API_URL}/doc/insert`,
                      {
                        method: "POST",
                        headers: {
                          "Content-Type": "application/json",
                        },
                        body: JSON.stringify({
                          title: documentTitle,
                          text: documentText,
                        }),
                      }
                    );

                    const data = await response.json();

                    if (response.ok) {
                      setDocumentTitle("");
                      setDocumentText("");

                      try {
                        await loadDocuments();
                      } catch {
                        setDocumentCount(
                          (count) => count + (data.chunks ?? 1)
                        );
                      }
                    }
                  } catch {
                    // Keep the UI usable if the backend is unavailable.
                  }
                }}
                className="mt-4 flex w-full items-center justify-center gap-2 border border-[#5e574d] py-2 text-[10px] uppercase tracking-[0.12em] transition hover:border-[#b56a3b] hover:text-[#d78b57]"
              >
                <Upload size={13} />
                Add document
              </button>
            </div>

            <div className="my-7 border-t border-[#35322c]" />

            <SectionLabel>RAG assistant</SectionLabel>

            <div className="mt-3 border border-[#35322c] bg-[#141310] p-4">
              <div className="flex items-center gap-2 text-xs">
                <Sparkles size={14} className="text-[#d78b57]" />
                Ask about your documents
              </div>

              <textarea
                className="mt-3 min-h-24 w-full resize-none border border-[#35322c] bg-[#1c1a17] p-3 text-xs outline-none focus:border-[#b56a3b]"
                placeholder="Ask a question..."
                value={ragQuestion}
                onChange={(e) => setRagQuestion(e.target.value)}
              />

              <button
                onClick={async () => {
                  if (!ragQuestion.trim()) return;

                  setAsking(true);
                  setRagAnswer("");

                  try {
                    const response = await fetch(
                      `${process.env.NEXT_PUBLIC_API_URL}/doc/ask`,
                      {
                        method: "POST",
                        headers: {
                          "Content-Type": "application/json",
                        },
                        body: JSON.stringify({
                          question: ragQuestion,
                          k: 3,
                        }),
                      }
                    );

                    const data = await response.json();
                    setRagAnswer(data.answer ?? "");
                  } catch {
                    setRagAnswer("Unable to reach the document assistant.");
                  } finally {
                    setAsking(false);
                  }
                }}
                className="mt-2 flex w-full items-center justify-center gap-2 bg-[#b56a3b] py-2.5 text-xs font-medium text-[#171613] transition hover:bg-[#d78b57]"
              >
                <Sparkles size={14} />
                {asking ? "Thinking..." : "Ask AI"}
              </button>

              {ragAnswer && (
                <div className="mt-3 border border-[#35322c] bg-[#1c1a17] p-3 text-[10px] leading-5 text-[#aaa196]">
                  {ragAnswer}
                </div>
              )}
            </div>
          </div>
        </aside>
      </div>
    </main>
  );
}

function SectionLabel({ children }: { children: React.ReactNode }) {
  return (
    <div className="text-[9px] font-medium uppercase tracking-[0.22em] text-[#817a70]">
      {children}
    </div>
  );
}

function InfoPill({
  icon,
  label,
}: {
  icon: React.ReactNode;
  label: string;
}) {
  return (
    <div className="flex items-center gap-2 border border-[#35322c] bg-[#1c1a17] px-3 py-1.5 text-[10px] uppercase tracking-[0.12em] text-[#aaa196]">
      {icon}
      {label}
    </div>
  );
}

function Tab({
  icon,
  label,
  active = false,
  onClick,
}: {
  icon: React.ReactNode;
  label: string;
  active?: boolean;
  onClick?: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={`flex flex-1 items-center justify-center gap-2 border-b-2 py-3 text-[10px] uppercase tracking-[0.12em] transition ${
        active
          ? "border-[#b56a3b] text-[#d78b57]"
          : "border-transparent text-[#625d55] hover:text-[#aaa196]"
      }`}
    >
      {icon}
      {label}
    </button>
  );
}