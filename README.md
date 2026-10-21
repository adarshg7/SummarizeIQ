# SummarizeIQ (AI PDF Summarizer)

A Spring Boot application that allows users to upload PDF documents, extract text, generate structured AI summaries using Google Gemini, perform RAG (Retrieval-Augmented Generation) queries against documents, and chat in real-time over WebSockets.

---

## What This Project Actually Does

1. **User Authentication**:
   - Registration and login with password hashing via BCrypt and stateless JWT tokens.
   - Roles supported: `CUSTOMER`, `SUPPORT_AGENT`, `ADMIN`.

2. **PDF Upload & Text Extraction**:
   - Accepts PDF files up to 10 MB via `/api/documents/upload`.
   - Files are saved locally to the `uploads/` directory.
   - Apache PDFBox extracts the raw text from the PDF and stores it in the MySQL `documents` table.

3. **Asynchronous AI Summarization**:
   - When a user requests a summary, an `AiJob` is placed onto an internal `BlockingQueue` (`AiJobQueue`).
   - A pool of 4 background workers processes the queue and calls the Google Gemini REST API.
   - The summary record is created in the database with status `PENDING`, and updated to `COMPLETED` or `FAILED` when the LLM finishes.
   - The frontend polls `/api/summary/{documentId}` until the summary is complete and renders it with markdown formatting.

4. **Multi-Model Auto-Fallback (Gemini)**:
   - Primary model is set to `gemini-3.5-flash` (fast, free-tier accessible).
   - If Google returns an HTTP 503 (high demand), 429 (rate limit), or 404 (model deprecated), the client automatically tries fallback models (`gemini-flash-lite-latest`, `gemini-3.8-flash`) before failing.

5. **Semantic Search / RAG**:
   - Document text is indexed into vector embeddings using NVIDIA NIM's `nemotron-3-embed-1b`.
   - Users can query their uploaded PDFs through `/api/rag/ask?question=...` to get context-grounded answers.

6. **Real-time Chat**:
   - STOMP over WebSocket (`/ws` with SockJS) allows real-time interactive messaging with an AI bot or human support agent.

7. **Support Ticketing**:
   - Customers can create support tickets (`/api/tickets`).
   - Support agents can assign and close tickets.
   - Summarization of past tickets is available via `/api/rag/summarize-tickets`.

---

## Tech Stack & Dependencies

- **Language & Runtime**: Java 21
- **Backend Framework**: Spring Boot 3.4.1 (Web, Security, Data JPA, WebSocket)
- **Database**: MySQL 8.x
- **Cache / Sessions**: Redis
- **PDF Extraction**: Apache PDFBox 3.0.3
- **AI Providers**:
  - **Google Gemini REST API**: for document summarization (`gemini-3.5-flash`)
  - **NVIDIA NIM API**: for text embeddings (`nemotron-3-embed-1b`)
- **Frontend**: HTML5, Vanilla CSS3, JavaScript (ES6+), SockJS & StompJS for WebSocket chat

---

## Complete `application.properties` Guide

Below is the complete configuration required in `src/main/resources/application.properties`.

```properties
# ===================================================================
# 1. APPLICATION
# ===================================================================
spring.application.name=aiPdfSummerizer
server.port=8080

# ===================================================================
# 2. MYSQL DATABASE
# ===================================================================
# Replace 'localhost:3306' and 'pdf_summarizer_db' with your MySQL host and DB name.
# 'createDatabaseIfNotExist=true' automatically creates the database if it doesn't exist yet.
spring.datasource.url=jdbc:mysql://localhost:3306/pdf_summarizer_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=root
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate automatically updates table schemas based on entity classes
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.jpa.properties.hibernate.format_sql=true

# ===================================================================
# 3. FILE UPLOADS
# ===================================================================
# Maximum allowed size for uploaded PDF files
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# ===================================================================
# 4. BACKGROUND WORKER THREAD POOL
# ===================================================================
# Controls the thread pool for async tasks and AI worker threads
app.async.core-pool-size=5
app.async.max-pool-size=100
app.async.queue-capacity=100

# ===================================================================
# 5. JWT SECURITY
# ===================================================================
# Secret key used for signing JWT tokens (must be at least 256 bits, base64 encoded)
jwt.secret=qkL5n/gYdUbiGmCZ92DkhzcS64HygILVEYUWmUSj4u5SpTnC37SBTEMHK9tUVUtcEkU4axiYswpvib5JLZ+0XQ==
# Token validity duration in milliseconds: 604800000 = 7 days (86400000 = 24 hours)
jwt.expiration=604800000

# ===================================================================
# 6. GOOGLE GEMINI AI (Summaries)
# ===================================================================
# Get your free key at: https://aistudio.google.com/app/apikey
gemini.api-key=YOUR_GEMINI_API_KEY
# Primary model. Recommended: gemini-3.5-flash (stable free-tier, low latency)
# If a model experiences high demand (503), the client auto-falls back to other models
gemini.model=gemini-3.5-flash

# ===================================================================
# 7. REDIS
# ===================================================================
# Required for Spring Data Redis caching and chat session state
spring.data.redis.host=localhost
spring.data.redis.port=6379

# ===================================================================
# 8. NVIDIA NIM (Embeddings for RAG)
# ===================================================================
# Used by RagService and EmbeddingIndexService for vector search
# Get an API key from https://build.nvidia.com
spring.ai.openai.api-key=YOUR_NVIDIA_API_KEY
spring.ai.openai.base-url=https://integrate.api.nvidia.com
spring.ai.openai.chat.options.model=nvidia/llama-3.1-nemotron-70b-instruct

embedding.provider.base-url=https://integrate.api.nvidia.com/v1/embeddings
embedding.provider.api-key=YOUR_NVIDIA_API_KEY
embedding.provider.model=nvidia/nemotron-3-embed-1b

# ===================================================================
# 9. EMAIL NOTIFICATIONS (Optional SMTP)
# ===================================================================
# Used for ticket updates and welcome emails. Use a Gmail App Password if using Gmail:
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=your-email@gmail.com
spring.mail.password=your-app-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
```

---

## How to Set Up & Run

### Step 1: Start MySQL and Redis

You can run them natively or using Docker:

```bash
# Start MySQL in Docker
docker run -d --name mysql-container -p 3306:3306 -e MYSQL_ROOT_PASSWORD=root mysql:8.0

# Start Redis in Docker
docker run -d --name redis-container -p 6379:6379 redis:latest
```

### Step 2: Configure Keys

Open `src/main/resources/application.properties` and replace:
- `spring.datasource.password` with your MySQL password.
- `gemini.api-key` with your Google Gemini key from [Google AI Studio](https://aistudio.google.com/app/apikey).
- (Optional) `embedding.provider.api-key` with your NVIDIA NIM key if you plan to use RAG.

### Step 3: Build & Run

Using Maven:
```powershell
mvn clean compile -DskipTests
mvn spring-boot:run
```

Once started, the backend runs at `http://localhost:8080`.

### Step 4: Open the Frontend

- Open your browser to: `http://localhost:8080/index.html`
- Sign up for a new account (or login).
- You will be redirected to the dashboard (`http://localhost:8080/app.html`).
- Drag and drop any PDF file, click **Upload & Extract**, and then **Generate Summary**.

---

## Main API Endpoints

### Auth (`/api/auth`)
- `POST /api/auth/register` — Create account (`email`, `password`, `fullName`, `role`)
- `POST /api/auth/login` — Login and receive Bearer JWT token

### Documents (`/api/documents`)
- `POST /api/documents/upload` — Upload PDF file (multipart `file`), extracts text and returns document ID.
- `GET /api/documents` — List documents uploaded by the currently authenticated user.

### Summaries (`/api/summary`)
- `POST /api/summary/{documentId}` — Queues summarization job, returns status `PENDING`.
- `GET /api/summary/{documentId}` — Fetches latest summary for the document (`PENDING`, `COMPLETED`, `FAILED`).

### RAG & Semantic Search (`/api/rag`)
- `GET /api/rag/ask?question={text}` — Answers question using text from user's indexed PDFs.
- `GET /api/rag/summarize-tickets?question={text}` — Answers questions based on past support tickets.

### WebSocket Chat (`/ws`)
- Handshake endpoint: `ws://localhost:8080/ws` (SockJS fallback enabled)
- Client publishes to: `/app/chat.send`
- Client subscribes to: `/user/queue/messages`

### Tickets (`/api/tickets`)
- `POST /api/tickets` — Create a support ticket
- `PATCH /api/tickets/{id}/assign` — Assign support agent
- `PATCH /api/tickets/{id}/close` — Close a ticket
- `GET /api/tickets?status=OPEN` — Get tickets by status

---

## Known Gotchas & Troubleshooting

1. **Gemini HTTP 503 "High Demand"**:
   Google's public free-tier Gemini API occasionally throttles requests during traffic spikes. The application includes automatic retry logic and fallback models (`gemini-3.5-flash` ➔ `gemini-flash-lite-latest` ➔ `gemini-3.8-flash`) to handle this without crashing.

2. **Gemini HTTP 404 "Model no longer available"**:
   Older models like `gemini-2.5-flash` are retired for new API keys. Keep `gemini.model` set to `gemini-3.5-flash`.

3. **Redis Connection Refused**:
   If Redis is not running, Spring Boot will fail at startup. Ensure Redis is running on port 6379 (`docker start redis-container` or local service).

4. **Scanned / Image-only PDFs**:
   Apache PDFBox extracts digital text streams. Scanned PDFs with image-only pages contain no extractable text, which will return a `BadRequestException: Document has no extractable text`. Ensure PDFs have selectable text.
