# VectorX

VectorX is a Java-based vector search and document retrieval platform built with Spring Boot and Next.js.

It provides multiple vector indexing and search algorithms, configurable distance metrics, document embedding and semantic retrieval, benchmarking tools, HNSW graph inspection, and Ollama-powered RAG capabilities through a unified web dashboard.

Live Link - https://vector-x-mu.vercel.app/?utm_source=chatgpt.com [Frontend] /
Live Link - https://vectorx-backend-f0y0.onrender.com/status [Backend]

---

## Overview

VectorX combines vector search, document retrieval, embeddings, and AI-powered question answering into a single application.

The system allows users to:

- Store and manage vectors
- Search vectors using different algorithms
- Compare search performance
- Select different distance metrics
- Inspect the HNSW graph
- Add and manage documents
- Generate document embeddings
- Perform semantic document search
- Ask questions using retrieved document context
- Use Ollama for local embeddings and text generation

The backend is implemented with Java and Spring Boot, while the frontend is built with Next.js, React, TypeScript, and Tailwind CSS.

---

## Features

### Vector Search

- Insert vectors
- Delete vectors
- Search nearest vectors
- Retrieve stored vectors
- Configurable result count
- Multiple search algorithms
- Multiple distance metrics

### Search Algorithms

- Brute Force
- KD-Tree
- HNSW

### Distance Metrics

- Cosine Distance
- Euclidean Distance
- Manhattan Distance

### Benchmarking

- Compare Brute Force, KD-Tree, and HNSW
- Measure search latency
- Run the same query across different algorithms
- View benchmark results directly from the dashboard

### HNSW Graph

- View total nodes
- View maximum graph layer
- View nodes per layer
- View edges per layer
- Inspect graph nodes
- Inspect graph edges

### Document Retrieval

- Add documents
- Delete documents
- List stored documents
- Automatically split documents into chunks
- Generate embeddings for document chunks
- Perform semantic document search

### RAG

- Ask questions against stored documents
- Retrieve relevant document chunks
- Generate answers using Ollama
- Display retrieved contexts and similarity distances

### Dashboard

- Interactive vector search interface
- Algorithm selection
- Metric selection
- Vector management
- Benchmark dashboard
- HNSW inspection
- Document management
- RAG question answering
- System status monitoring

---

## Technology Stack

### Backend

| Technology | Purpose |
|---|---|
| Java 21 | Backend development |
| Spring Boot | Application framework |
| Spring Web | REST API development |
| Maven | Dependency and build management |

### Frontend

| Technology | Purpose |
|---|---|
| Next.js | Frontend framework |
| React | UI development |
| TypeScript | Type-safe frontend development |
| Tailwind CSS | Styling |
| Lucide React | Interface icons |

### AI and Embeddings

| Technology | Purpose |
|---|---|
| Ollama | Local AI runtime |
| nomic-embed-text | Text embeddings |
| llama3.2 | Text generation |

### Algorithms

| Algorithm | Purpose |
|---|---|
| Brute Force | Exact nearest-neighbor baseline |
| KD-Tree | Tree-based vector search |
| HNSW | Graph-based approximate nearest-neighbor search |

---

## Architecture

```text
                         VectorX
                            │
            ┌───────────────┴───────────────┐
            │                               │
        Next.js                         Spring Boot
        Frontend                          Backend
            │                               │
            │                    ┌──────────┴──────────┐
            │                    │                     │
            │              Vector Database       Document Database
            │                    │                     │
            │          ┌─────────┼─────────┐           │
            │          │         │         │           │
            │      Brute Force KD-Tree   HNSW          │
            │                                          │
            │                                      Ollama
            │                                   ┌──────┴──────┐
            │                                   │             │
            │                              Embeddings     Generation
            │                              nomic-embed     llama3.2
            │
            └────────────── REST APIs ─────────────────────┘
```

---

# Vector Search

VectorX provides three vector search implementations.

## Brute Force

Brute Force compares the query vector with every stored vector.

It provides an exact search baseline and is useful for evaluating the performance of other search algorithms.

## KD-Tree

KD-Tree organizes vectors using a multidimensional tree structure.

During search, the tree structure is used to reduce unnecessary distance calculations.

## HNSW

HNSW stands for **Hierarchical Navigable Small World**.

It uses a layered graph structure for approximate nearest-neighbor search.

The current configuration uses:

```text
M = 16
efConstruction = 200
M0 = 32
Random Seed = 42
```

The demo vector database uses:

```text
Dimensions = 16
```

---

# Distance Metrics

VectorX supports three distance metrics.

## Cosine Distance

Cosine distance is calculated using:

```text
1 - cosine similarity
```

A smaller distance represents a closer match.

## Euclidean Distance

Euclidean distance measures the straight-line distance between two vectors.

```text
sqrt(sum((a[i] - b[i])²))
```

## Manhattan Distance

Manhattan distance is calculated as:

```text
sum(abs(a[i] - b[i]))
```

---

# Document Retrieval

VectorX supports document-based semantic retrieval.

Documents are processed through the following pipeline:

```text
Document
    │
    ▼
Text Chunking
    │
    ▼
Embedding Generation
    │
    ▼
Vector Storage
    │
    ▼
Semantic Search
    │
    ▼
Relevant Chunks
```

Documents are divided into smaller text chunks before embeddings are generated.

Current chunk configuration:

```text
Chunk Size: 250 words
Overlap: 30 words
```

Document search uses:

```text
< 10 chunks  → Brute Force
≥ 10 chunks  → HNSW
```

---

# RAG Pipeline

VectorX provides Retrieval-Augmented Generation functionality.

The RAG workflow is:

```text
User Question
      │
      ▼
Question Embedding
      │
      ▼
Vector Search
      │
      ▼
Relevant Document Chunks
      │
      ▼
Context Construction
      │
      ▼
Ollama Generation
      │
      ▼
Generated Answer
```

The system retrieves relevant document chunks before generating an answer.

---

# Ollama

VectorX uses Ollama for local AI functionality.

## Embedding Model

```text
nomic-embed-text
```

Used to generate vector embeddings for document chunks and search questions.

## Generation Model

```text
llama3.2
```

Used to generate answers for the RAG workflow.

## Ollama Endpoint

```text
http://127.0.0.1:11434
```

---

# REST API

## Vector APIs

| Method | Endpoint | Description |
|---|---|---|
| GET | `/search` | Search vectors |
| POST | `/insert` | Insert a vector |
| DELETE | `/delete/{id}` | Delete a vector |
| GET | `/items` | List vectors |
| GET | `/benchmark` | Benchmark search algorithms |
| GET | `/stats` | Get vector database statistics |
| GET | `/hnsw-info` | Get HNSW graph information |

## Document APIs

| Method | Endpoint | Description |
|---|---|---|
| POST | `/doc/insert` | Insert a document |
| DELETE | `/doc/delete/{id}` | Delete a document |
| GET | `/doc/list` | List documents |
| POST | `/doc/search` | Search documents |
| POST | `/doc/ask` | Ask a question using document retrieval |

## System API

| Method | Endpoint | Description |
|---|---|---|
| GET | `/status` | Get system and Ollama status |

---

# Vector Search API

Example:

```text
GET /search?v=0.1,0.2,0.3,...&k=5&metric=cosine&algo=hnsw
```

### Parameters

| Parameter | Description |
|---|---|
| `v` | Comma-separated query vector |
| `k` | Number of results |
| `metric` | Distance metric |
| `algo` | Search algorithm |

Supported algorithms:

```text
bruteforce
kdtree
hnsw
```

Supported metrics:

```text
cosine
euclidean
manhattan
```

The response includes:

- Search results
- Vector IDs
- Distances
- Search latency
- Algorithm
- Metric

---

# Benchmarking

VectorX provides a benchmark endpoint for comparing vector search implementations.

The same query can be evaluated using:

```text
Brute Force
KD-Tree
HNSW
```

Example response structure:

```json
{
  "bruteforceUs": 77,
  "kdtreeUs": 51,
  "hnswUs": 124,
  "itemCount": 20
}
```

The latency values represent the measured search time for each algorithm.

---

# HNSW Inspection

The HNSW dashboard exposes internal graph information.

Available information includes:

```text
Node Count
Top Layer
Nodes Per Layer
Edges Per Layer
Graph Nodes
Graph Edges
```

Each graph node contains information such as:

```text
ID
Metadata
Category
Maximum Layer
```

Graph edges contain:

```text
Source
Destination
Layer
```

---

# Frontend Dashboard

The VectorX dashboard provides a unified interface for the backend functionality.

The interface includes:

### Results

Search vectors and view nearest-neighbor results.

### Benchmark

Run and compare search performance across:

- Brute Force
- KD-Tree
- HNSW

### HNSW

Inspect:

- Node count
- Layers
- Graph structure
- Edges

### Documents

Manage documents and perform semantic retrieval.

### RAG

Ask questions and receive AI-generated answers using retrieved document information.

---

# Project Structure

```text
VectorX/
│
├── vectorx-backend/
│   │
│   ├── src/
│   │   └── main/
│   │       │
│   │       ├── java/
│   │       │   └── com/vectorx/backend/
│   │       │
│   │       │       ├── algorithm/
│   │       │       │   ├── BruteForce.java
│   │       │       │   ├── DistanceMetrics.java
│   │       │       │   ├── HNSW.java
│   │       │       │   └── KDTree.java
│   │       │       │
│   │       │       ├── controller/
│   │       │       │   ├── DocumentController.java
│   │       │       │   ├── StatusController.java
│   │       │       │   └── VectorController.java
│   │       │       │
│   │       │       ├── model/
│   │       │       │   ├── DocumentItem.java
│   │       │       │   └── VectorItem.java
│   │       │       │
│   │       │       └── service/
│   │       │           ├── DemoData.java
│   │       │           ├── DocumentDatabase.java
│   │       │           ├── OllamaClient.java
│   │       │           ├── VectorDatabase.java
│   │       │           └── VectorXService.java
│   │       │
│   │       └── resources/
│   │           └── application.properties
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

# Requirements

Install:

- Java 21
- Maven
- Node.js
- npm
- Ollama

Check Java:

```powershell
java -version
```

Check Maven:

```powershell
mvn -version
```

Check Node.js:

```powershell
node -v
```

Check npm:

```powershell
npm -v
```

Check Ollama:

```powershell
ollama --version
```

---

# Ollama Setup

Pull the required embedding model:

```powershell
ollama pull nomic-embed-text
```

Pull the generation model:

```powershell
ollama pull llama3.2
```

Verify:

```powershell
ollama list
```

Expected models:

```text
llama3.2
nomic-embed-text
```

Ollama should be available at:

```text
http://127.0.0.1:11434
```

---

# Running the Backend

Open PowerShell:

```powershell
cd D:\Projects\VectorXectorx-backend
```

Compile:

```powershell
mvn clean compile
```

Run:

```powershell
mvn spring-boot:run
```

Backend:

```text
http://localhost:8080
```

---

# Running the Frontend

Open another PowerShell window:

```powershell
cd D:\Projects\VectorXectorx-frontend
```

Install dependencies:

```powershell
npm install
```

Start development server:

```powershell
npm run dev
```

Frontend:

```text
http://localhost:3000
```

The frontend connects to the backend using:

```text
NEXT_PUBLIC_API_URL=http://localhost:8080
```

---

# Environment Configuration

Create:

```text
vectorx-frontend/.env.local
```

with:

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
```

Environment files are excluded from Git.

---

# Production Build

## Frontend

```powershell
npm run build -- --webpack
```

## Backend

```powershell
mvn clean package -DskipTests
```

---

# Storage

VectorX currently uses in-memory storage for vectors and documents.

The application does not require an external database for vector or document storage.

When the backend restarts:

- Demo vectors are loaded again
- Runtime-inserted vectors are cleared
- Runtime-inserted documents are cleared

---

# API Examples

## Insert Vector

```http
POST /insert
Content-Type: application/json
```

Example body:

```json
{
  "meta": "Example vector",
  "cat": "demo",
  "emb": [
    0.1,
    0.2,
    0.3
  ]
}
```

The vector must contain the required number of dimensions.

## List Vectors

```http
GET /items
```

## Delete Vector

```http
DELETE /delete/{id}
```

## Insert Document

```http
POST /doc/insert
Content-Type: application/json
```

Example:

```json
{
  "title": "Vector Search",
  "text": "Vector search allows systems to find information based on numerical representations of data."
}
```

## Search Documents

```http
POST /doc/search
Content-Type: application/json
```

Example:

```json
{
  "question": "What is vector search?",
  "k": 3
}
```

## Ask AI

```http
POST /doc/ask
Content-Type: application/json
```

Example:

```json
{
  "question": "What is VectorX?",
  "k": 3
}
```

---

# Development

### Backend

```text
Java 21
Spring Boot
Spring Web
Maven
REST APIs
```

### Frontend

```text
Next.js
React
TypeScript
Tailwind CSS
Lucide React
```

### AI

```text
Ollama
nomic-embed-text
llama3.2
```

### Search Algorithms

```text
Brute Force
KD-Tree
HNSW
```

---

# Design Goals

VectorX focuses on:

- Clear vector search implementations
- Practical algorithm comparison
- Simple REST APIs
- Local AI integration
- Semantic document retrieval
- Interactive visualization
- Clean developer experience
- Lightweight in-memory operation

---

# Current Scope

The current implementation focuses on vector search, document retrieval, benchmarking, HNSW inspection, and local AI-powered RAG.

The application is intentionally lightweight and uses in-memory storage.

Features such as distributed vector storage, authentication, cloud model hosting, and persistent production databases are outside the current scope.

---

# Screenshots

Suggested screenshot structure:

```text
docs/
├── dashboard.png
├── benchmark.png
├── hnsw.png
└── documents.png
```

Example:

```markdown
![VectorX Dashboard](docs/dashboard.png)
```

---

# License

This project is intended for development, demonstration, and portfolio purposes.
