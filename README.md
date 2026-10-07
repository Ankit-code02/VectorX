# VectorX

VectorX is a vector search and document retrieval system converted from an existing C++ implementation to Java.

The project preserves the original vector database functionality while providing a redesigned web interface for vector search, indexing algorithms, benchmarking, HNSW inspection, document retrieval, and Ollama-powered document Q&A.

> **Project goal:** Convert the existing C++ implementation to Java without adding new functionality, while improving the frontend interface and user experience.

---

## Features

- Vector insertion and deletion
- Vector similarity search
- Multiple search algorithms:
    - Brute Force
    - KD-Tree
    - HNSW
- Multiple distance metrics:
    - Cosine
    - Euclidean
    - Manhattan
- Search benchmarking
- HNSW graph inspection
- Document insertion and deletion
- Document chunking and embedding
- Semantic document search
- Retrieval-Augmented Generation (RAG)
- Ollama integration
- Interactive web dashboard
- REST API backend
- In-memory vector and document storage

---

## Tech Stack

### Backend

- Java 21
- Spring Boot
- Spring Web
- Maven
- REST APIs

### Frontend

- Next.js
- React
- TypeScript
- Tailwind CSS
- Lucide React

### AI / Embeddings

- Ollama
- `nomic-embed-text`
- `llama3.2`

### Algorithms

- Brute Force Search
- KD-Tree
- HNSW

---

## Architecture

```text
                         VectorX
                            │
              ┌─────────────┴─────────────┐
              │                           │
          Frontend                    Backend
        Next.js / React            Spring Boot / Java
              │                           │
              │                  ┌────────┴────────┐
              │                  │                 │
              │             Vector Database   Document Database
              │                  │                 │
              │          ┌───────┼───────┐         │
              │          │       │       │         │
              │      Brute     KD-Tree  HNSW       │
              │      Force                         │
              │                                    │
              │                              Ollama
              │                           ┌────────┴────────┐
              │                           │                 │
              │                    Embeddings          Generation
              │                 nomic-embed-text       llama3.2
              │
              └──────────── REST API ────────────────┘
```

---

## Vector Search

VectorX supports three search algorithms.

### Brute Force

Brute Force compares the query vector against every stored vector.

It provides a straightforward baseline for comparing the performance of the other algorithms.

### KD-Tree

KD-Tree organizes vectors into a tree structure and uses spatial partitioning during search.

### HNSW

HNSW (Hierarchical Navigable Small World) uses a layered graph structure for approximate nearest-neighbor search.

The implementation uses:

- `M = 16`
- `efConstruction = 200`
- `M0 = 32`
- Random seed `42`

The vector dimension used by the demo database is:

```text
16
```

---

## Distance Metrics

VectorX supports three distance metrics.

### Cosine Distance

Cosine distance is calculated as:

```text
1 - cosine similarity
```

Lower distance represents a closer match.

### Euclidean Distance

Euclidean distance measures the straight-line distance between two vectors.

```text
sqrt(sum((a[i] - b[i])²))
```

### Manhattan Distance

Manhattan distance is calculated as:

```text
sum(abs(a[i] - b[i]))
```

---

## Document Search and RAG

VectorX supports document-based retrieval.

The document pipeline is:

```text
Document
   │
   ▼
Text Chunking
   │
   ▼
Ollama Embedding
   │
   ▼
Vector Storage
   │
   ▼
Semantic Search
   │
   ▼
Relevant Context
   │
   ▼
Ollama Generation
   │
   ▼
Answer
```

Documents are split into chunks before generating embeddings.

The current chunking configuration uses:

```text
Chunk size: 250 words
Overlap: 30 words
```

For smaller document collections, document search uses Brute Force. Larger collections use HNSW.

---

## Ollama Models

VectorX communicates with a locally running Ollama instance.

### Embedding model

```text
nomic-embed-text
```

### Generation model

```text
llama3.2
```

Default Ollama endpoint:

```text
http://127.0.0.1:11434
```

---

## REST API

### Vector APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/search` | Search vectors |
| POST | `/insert` | Insert a vector |
| DELETE | `/delete/{id}` | Delete a vector |
| GET | `/items` | List vectors |
| GET | `/benchmark` | Benchmark search algorithms |
| GET | `/stats` | Get vector database statistics |
| GET | `/hnsw-info` | Inspect HNSW structure |

### Document APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/doc/insert` | Insert a document |
| DELETE | `/doc/delete/{id}` | Delete a document |
| GET | `/doc/list` | List documents |
| POST | `/doc/search` | Search documents |
| POST | `/doc/ask` | Ask a question using document retrieval |

### System API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/status` | Check system and Ollama status |

---

## Example Vector Search

Example request:

```text
GET /search?v=0.1,0.2,0.3,...&k=5&metric=cosine&algo=hnsw
```

The response contains:

- Search results
- Vector IDs
- Distances
- Search latency
- Selected algorithm
- Selected metric

---

## Benchmarking

VectorX can compare the three vector search implementations using the same query:

```text
Brute Force
KD-Tree
HNSW
```

The benchmark reports search latency for each algorithm.

This makes it possible to compare the behavior of the different implementations using the same vector database.

---

## HNSW Inspection

The dashboard provides information about the HNSW graph, including:

- Node count
- Maximum layer
- Nodes per layer
- Edges per layer
- Graph nodes
- Graph edges

This provides a visual way to inspect the internal HNSW structure.

---

## Frontend

The VectorX frontend provides a single dashboard for interacting with the backend.

The interface includes:

- Vector search
- Algorithm selection
- Metric selection
- Vector insertion
- Vector deletion
- Search results
- Benchmarking
- HNSW inspection
- Document management
- Semantic document search
- RAG-based Q&A
- System status

The UI uses a dark technical/editorial visual style with subtle animations and a restrained accent palette.

---

## Project Structure

```text
VectorX/
│
├── vectorx-backend/
│   │
│   ├── src/
│   │   └── main/
│   │       ├── java/com/vectorx/backend/
│   │       │
│   │       ├── algorithm/
│   │       │   ├── BruteForce.java
│   │       │   ├── DistanceMetrics.java
│   │       │   ├── HNSW.java
│   │       │   └── KDTree.java
│   │       │
│   │       ├── controller/
│   │       │   ├── DocumentController.java
│   │       │   ├── StatusController.java
│   │       │   └── VectorController.java
│   │       │
│   │       ├── model/
│   │       │   ├── DocumentItem.java
│   │       │   └── VectorItem.java
│   │       │
│   │       └── service/
│   │           ├── DemoData.java
│   │           ├── DocumentDatabase.java
│   │           ├── OllamaClient.java
│   │           ├── VectorDatabase.java
│   │           └── VectorXService.java
│   │
│   └── pom.xml
│
├── vectorx-frontend/
│   │
│   ├── src/
│   │   └── app/
│   │       ├── globals.css
│   │       ├── layout.tsx
│   │       └── page.tsx
│   │
│   ├── public/
│   ├── package.json
│   └── next.config.ts
│
├── .gitignore
└── README.md
```

---

## Requirements

Before running VectorX, install:

- Java 21
- Maven
- Node.js
- npm
- Ollama

Verify Java:

```powershell
java -version
```

Verify Maven:

```powershell
mvn -version
```

Verify Node.js:

```powershell
node -v
```

Verify npm:

```powershell
npm -v
```

Verify Ollama:

```powershell
ollama --version
```

---

## Ollama Setup

Pull the required models:

```powershell
ollama pull nomic-embed-text
```

```powershell
ollama pull llama3.2
```

Verify the installed models:

```powershell
ollama list
```

Ollama should be available at:

```text
http://127.0.0.1:11434
```

---

## Running the Backend

Open PowerShell:

```powershell
cd D:\Projects\VectorXectorx-backend
```

Compile the backend:

```powershell
mvn clean compile
```

Run the application:

```powershell
mvn spring-boot:run
```

The backend runs on:

```text
http://localhost:8080
```

---

## Running the Frontend

Open another PowerShell window:

```powershell
cd D:\Projects\VectorXectorx-frontend
```

Install dependencies:

```powershell
npm install
```

Start the development server:

```powershell
npm run dev
```

The frontend is available at:

```text
http://localhost:3000
```

The frontend connects to the backend using:

```text
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

## Production Build

The frontend can be built using the Webpack production build:

```powershell
npm run build -- --webpack
```

The backend can be packaged using:

```powershell
mvn clean package -DskipTests
```

---

## C++ to Java Conversion

The original project was implemented in C++.

VectorX converts the existing implementation into Java while preserving its core behavior and API structure.

The conversion includes:

- Vector database
- Brute Force search
- KD-Tree
- HNSW
- Distance metrics
- Vector operations
- Document database
- Document chunking
- Ollama integration
- Semantic search
- RAG workflow
- REST endpoints

The project does not add a separate persistence layer because the original implementation uses in-memory storage.

---

## Storage

VectorX currently uses in-memory storage.

Vectors and documents are stored while the backend application is running.

Restarting the backend clears the current in-memory data and reloads the demo vectors.

PostgreSQL is not required for the current implementation.

---

## Current Scope

The project intentionally focuses on the functionality present in the original implementation.

It does not attempt to introduce additional database persistence, authentication, distributed storage, or other unrelated functionality.

---

## Screenshots

Screenshots of the VectorX dashboard can be added here.

Suggested structure:

```text
docs/
├── dashboard.png
├── benchmark.png
├── hnsw.png
└── documents.png
```

---

## Development

### Backend

```text
Java 21
Spring Boot
Maven
```

### Frontend

```text
Next.js
React
TypeScript
Tailwind CSS
```

### AI

```text
Ollama
nomic-embed-text
llama3.2
```

---

## License

This project is intended as a development and demonstration project.
