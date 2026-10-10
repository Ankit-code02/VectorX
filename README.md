# VectorX

VectorX is a Java and Spring Boot project with a Next.js dashboard for experimenting with vector search algorithms, inspecting an HNSW graph, and exploring document retrieval with local AI models.

Vector and document data are held in memory in the documented implementation. Runtime-added data is cleared when the backend restarts.

## Features

### Vector Search
- Insert, list, retrieve, and delete vectors
- Search for nearest vectors
- Select a search algorithm and distance metric
- Configure the number of results returned

### Search Algorithms
- Brute-force nearest-neighbor search
- KD-Tree search
- HNSW graph-based approximate nearest-neighbor search

### Distance Metrics
- Cosine distance
- Euclidean distance
- Manhattan distance

### Benchmarking and HNSW Inspection
- Compare search algorithms using the same query
- View reported search latency
- Inspect HNSW node and layer information exposed by the backend

Benchmark results depend on the data, query, runtime, and machine. This README does not claim fixed performance numbers.

### Document Retrieval and RAG
- Add, list, and delete documents
- Split document text into chunks
- Generate embeddings through Ollama
- Search document chunks by semantic similarity
- Ask questions using retrieved document context and a local language model

## Technology Stack

**Backend**
- Java 21
- Spring Boot and Spring Web
- Maven

**Frontend**
- Next.js
- React
- TypeScript
- Tailwind CSS
- Lucide React

**Local AI**
- Ollama
- `nomic-embed-text` for embeddings
- `llama3.2` for answer generation

## Architecture

```text
Next.js dashboard
       |
    REST/JSON
       |
       v
Spring Boot backend
  |             |
  v             v
Vector search   Document retrieval
  |             |
  +-- Brute Force
  +-- KD-Tree    +-- Chunking
  +-- HNSW       +-- Embeddings via Ollama
                 +-- Similarity search
                 +-- Answer generation via Ollama
```

## Vector Search Notes

- **Brute Force:** compares the query against stored vectors and provides a baseline for exact nearest-neighbor search.
- **KD-Tree:** organizes vectors in a tree structure to reduce search work for supported data and distance behavior.
- **HNSW:** uses a layered graph for approximate nearest-neighbor search.

The original README lists HNSW parameters `M = 16`, `efConstruction = 200`, `M0 = 32`, and random seed `42`, with a demo vector dimension of `16`. Confirm these values against the current source if the configuration has changed.

The listed distance metrics are cosine distance, Euclidean distance, and Manhattan distance. Smaller distance values represent closer matches under the corresponding metric.

## Document Retrieval Flow

```text
Document text
     |
     v
Chunking
     |
     v
Embedding generation (Ollama)
     |
     v
In-memory vector storage
     |
     v
Semantic retrieval
     |
     v
Retrieved chunks -> Ollama generation -> Answer
```

The supplied README describes chunks of 250 words with a 30-word overlap and uses brute force for fewer than 10 chunks and HNSW at 10 or more. Confirm these settings against the current source before relying on them.

## API Overview

### Vector Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/search` | Search vectors |
| `POST` | `/insert` | Insert a vector |
| `DELETE` | `/delete/{id}` | Delete a vector |
| `GET` | `/items` | List vectors |
| `GET` | `/benchmark` | Compare search algorithms |
| `GET` | `/stats` | Get vector database statistics |
| `GET` | `/hnsw-info` | Get HNSW graph information |

Example query shape:

```text
GET /search?v=0.1,0.2,0.3,...&k=5&metric=cosine&algo=hnsw
```

Documented algorithm names: `bruteforce`, `kdtree`, `hnsw`.

Documented metric names: `cosine`, `euclidean`, `manhattan`.

### Document Endpoints

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/doc/insert` | Insert a document |
| `DELETE` | `/doc/delete/{id}` | Delete a document |
| `GET` | `/doc/list` | List documents |
| `POST` | `/doc/search` | Search documents |
| `POST` | `/doc/ask` | Ask a question using retrieved context |

### System Endpoint

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/status` | Report system and Ollama status |

Confirm request fields and response schemas against the current controller classes before relying on these endpoints in another application.

## Requirements

- Java 21
- Maven
- Node.js and npm
- Ollama

Check installations:

```powershell
java -version
mvn -version
node -v
npm -v
ollama --version
```

## Run Locally

### 1. Clone the repository

```powershell
git clone https://github.com/Ankit-code02/VectorX.git
cd VectorX
```

### 2. Start Ollama and pull the models

```powershell
ollama pull nomic-embed-text
ollama pull llama3.2
ollama list
```

Ollama is expected at `http://127.0.0.1:11434` for the documented local setup.

### 3. Start the backend

```powershell
cd vectorx-backend
mvn clean compile
mvn spring-boot:run
```

The documented local backend URL is `http://localhost:8080`.

### 4. Start the frontend

Open a second terminal:

```powershell
cd vectorx-frontend
npm install
```

Create `vectorx-frontend/.env.local`:

```dotenv
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Start the frontend:

```powershell
npm run dev
```

Open `http://localhost:3000`.

### 5. Build

Frontend:

```powershell
npm run build -- --webpack
```

Backend:

```powershell
mvn clean package -DskipTests
```

Run the commands from their respective frontend or backend directories.

## Storage and Limitations

- **In-memory data:** vectors and documents are stored in memory. Runtime inserts and deletes are not durable across backend restarts; demo data may be loaded again on startup.
- **Local AI dependency:** embedding and generation workflows require a running Ollama service with the specified models.
- **No production database:** persistent storage and recovery are outside the documented implementation scope.
- **No distributed deployment claim:** VectorX is an algorithm and retrieval demo, not a distributed vector database.
- **Benchmark interpretation:** local results depend on the environment and should not be presented as general performance guarantees.
- **AI output:** generated answers may be incomplete or incorrect; inspect retrieved context for important decisions.

Authentication, durable storage, cloud model hosting, and production deployment hardening are outside the documented current scope.

## Author

**Ankit Maurya**

- GitHub: https://github.com/Ankit-code02
- LinkedIn: https://www.linkedin.com/in/ankit0209/
- Portfolio: https://ankit-portfolio-two-brown.vercel.app
