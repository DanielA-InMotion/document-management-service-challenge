# 📄 Document Management API Challenge

## Overview 🚀

In this challenge, you will build a backend API service to manage **large PDF documents**. The service must allow users to upload, search, and download PDF documents while efficiently handling resources, given a **memory limitation of 50MB assigned to the document management service container**.
This challenge is designed for a mid-senior engineer to demonstrate advanced skills in **Spring Boot, Java, REST API development, testing, containerization, and cloud storage integration**.

## Functional Requirements ✅

### 1. Upload Endpoint ⬆️

- **Functionality:**  
  Allow uploading a PDF document along with the following metadata:
  - **User:** A string identifying the user associated with the document.
  - **Document Name:** The name provided in the request will be used as the file name.
  - **Tags:** A list of tags associated with the document.
- **Technical Constraints:**
  - The service must handle PDF uploads of up to 500MB.
  - The uploaded PDF should be stored in an bucket (simulated via MinIO) with the following directory structure:

    ```
    document-bucket/
      ├─ user1/
      │  ├─ doc1.pdf
      │  ├─ doc2.pdf
      ├─ user2/
      │  ├─ doc3.pdf
    ```
  - Metadata must be persisted in a PostgreSQL database with the following fields:
    - **User**
    - **Document Name**
    - **Tags**
    - **MinIO Path**
    - **File Size**
    - **File Type**
    - **Created At**
    - **Include any additional fields you deem necessary**

**📌 Storage Requirement: Uploading Documents to MinIO**

All uploaded documents must be stored in MinIO to ensure scalability and efficient storage management. The service will interact with MinIO to handle file uploads and generate temporary access URLs for retrieval. For detailed instructions on how to set up and use MinIO locally, please refer to the following document:
📄 [MinIO Local Setup Guide](docs/minio-local-setup.md).

### 2. Search Endpoint 🔍

- **Functionality:**  
  Allow querying documents with optional filters:
  - **Filters:** User, Document Name, and Tags.
  - If no filters are provided, return all documents.
  - Results should be ordered by `created_at` in descending order.
  - The endpoint must support pagination using `page` and `size` parameters.
- **Note:**  
  This endpoint should not return any download URL.

### 3. Download Endpoint ⬇️

- **Functionality:**  
  Allow downloading a document using its ID. The endpoint should return a temporary download URL that enables secure access to the document stored in MinIO.

- **Implementation:**  
  Generate a temporary download URL using MinIO’s pre-signed URL functionality. The service will utilize MinIO to generate a temporary download link based on the document's ID, allowing the document to be securely accessed without exposing direct storage paths.

### Note:

For more details on how to use MinIO, refer to the documentation:
📄 [MinIO Local Setup Guide](docs/minio-local-setup.md).

## Technical Requirements ⚙️

- **Memory Limitation:**  
  The service memory is limited to 50MB. You must design your solution to efficiently manage memory during file upload and processing, even when handling uploads of files up to 500MB.

- **Concurrent Uploads:**  
  The system must be capable of handling up to 10 documents being uploaded in parallel, with each document having a size of up to 500MB.

- **Upload time limit:**  
  There are no restrictions on the time it takes to upload files. Only, ensure that the service can handle uploads of up to 500MB without exceeding the memory limitation.

- **Provided Artifacts:**

  - OpenAPI specification that includes the contract for the endpoints.
    - Reference: [document-management-open-api.yml](docs/document-management-open-api.yml).
    - You can visualize the content using [Swagger Editor](https://editor-next.swagger.io/).
  - A docker-compose stack that includes PostgreSQL, and the Document Management Service.
  - Integrated tools:
    - **Spring Boot:** The project is pre-configured with Spring Boot.
    - **Spring Data JPA:** For database operations.
    - **MinIO:** For simulating bucket operations services locally.
    - **Lombok:** For reducing boilerplate code.
    - **JUnit 5:** For unit and integration testing.
    - **Mockito:** For mocking dependencies in tests.
    - **AssertJ:** For fluent assertions in tests.
    - **Jacoco:** for code coverage (run `./mvnw jacoco:report` to generate the report).
    - **Spotless:** for code formatting (run `./mvnw spotless:apply` to format your code).
- **Java Version:**  
  The project is configured with Java 17, but you may restrict your solution to features available in Java 8 if necessary.
- **Schema Management:**  
  Provide a script for creating the database schema, ensuring efficient handling of multiple tags per document.
- **Documentation:**  
  (Optional) Include OpenAPI documentation for the API endpoints.

## Implementation Instructions 🛠️

1. Use this repository as the starting point for your solution. If possible, create a fork of the repository.
2. Implement the endpoints as per the provided OpenAPI specification.
3. Configure an MinIO client.
4. Configure a connection to PostgreSQL.
5. Include your database schema script in `docker/init-scripts/schema-init.sql`.
6. Create the Dockerfile for the `document-management-service`.
7. Modify the docker-compose.yml file to add the necessary configuration for including the document-management-service in the stack. Ensure that the service correctly connects to PostgreSQL and MinIO.
8. Implement the required functionality for the Document Management Service.
9. Once your functionality is ready, validate it using Postman. Please note that you must start the stack using `docker-compose up --build`.
10. Commit your changes. It is recommended to maintain a clean commit history, ideally using [Conventional Commits](https://www.conventionalcommits.org/en/v1.0.0-beta.4/).
11. Push your changes to a personal GitHub account and share the URL of your solution.

**⚠️ Note:**
All configurations (database credentials, MinIO/S3 settings, etc.) must be externalized using environment variables and configuration files. Avoid hardcoding sensitive information in the source code.

## Evaluation Criteria 🏆

- **Database Schema and Indexing:**  
  Evaluate the efficiency of your database schema, including the creation of indices and the management of multiple tags per document.

- **Design Patterns and Best Practices:**  
  Assess the use of design patterns (e.g., Controller-Service-Repository or Hexagonal Architecture) and adherence to SOLID principles and clean code practices.

- **Code Quality:**  
  Review for readability, maintainability, proper exception handling, and overall coding standards.

- **Testing:**  
  Evaluate the quality and coverage of unit and integration tests. While no specific coverage percentage is required, tests should cover the most critical functionalities and edge cases.

- **Spring Boot and Java Proficiency:**  
  Demonstrate effective use of Spring Boot features and Java (preferably Java 17, though Java 8+ is acceptable).

- **Additional Considerations:**

  - Overall robustness and efficiency under concurrent file uploads.
  - Validations on models and DTOs (e.g., non-null constraints).
  - (Optional) OpenAPI documentation.

## Challenge Priorities 🎯

1. **Upload Service:**
   - Primary focus on implementing a robust upload endpoint that efficiently handles large file (**up to 500MB of size**) uploads within the 50MB memory constraint.
2. **Search Service:**
   - Implement a flexible and efficient search endpoint with filtering, sorting, and pagination.
3. **Download Service:**
   - Provide document download functionality via temporary AWS S3 URLs.

> **Note:** It is acceptable to implement a subset of the endpoints. However, the more complete your solution, the better.

## Submission Instructions 📤

Ensure that your solution includes the Dockerfile and database schema script, and that it adheres to the challenge requirements.

### Additional Comments 💬

#### How to Run the Project 🐳

**Prerequisites:** Docker and Docker Compose installed.

```bash
# Clone the repo and start the full stack (builds the app image automatically)
cd docker
docker compose up --build
```

This starts three containers:
- **document-management-service** → `http://localhost:8080`
- **PostgreSQL 15** → `localhost:5432`
- **MinIO** → API `http://localhost:9000` · Console `http://localhost:9001` (user: `minioadmin` / pass: `minioadmin`)

The database schema is initialized automatically via `docker/init-scripts/schema-init.sql`. The MinIO bucket (`document-bucket`) is created automatically on startup.

To stop:
```bash
docker compose down        # stop containers
docker compose down -v     # stop + wipe all data
```

---

#### Demo Walkthrough 🎬

A Postman collection is provided for easy validation:
📄 [document-management.postman_collection.json](docs/document-management.postman_collection.json)

**Import steps:**
1. Open Postman → **Import** → select `docs/document-management.postman_collection.json`
2. The collection has a `baseUrl` variable set to `http://localhost:8080` — no extra setup needed

---

**1. Upload a PDF document**

Use the **Upload Document** request in the collection:
- Set `user`, `name`, and `tags` fields in the form-data body
- Select a PDF file for the `file` field
- Send → expect `201 Created`

```bash
# curl equivalent
curl -X POST http://localhost:8080/document-management/upload \
  -F "user=john" \
  -F "name=invoice-march.pdf" \
  -F "tags=invoice" \
  -F "tags=finance" \
  -F "file=@/path/to/your/file.pdf"
```

---

**2. Search documents**

Use **Search Documents (no filters)** or **Search Documents (with filters)** from the collection.

```bash
# curl equivalent — with filters
curl -X POST "http://localhost:8080/document-management/search?page=0&size=10" \
  -H "Content-Type: application/json" \
  -d '{"user": "john", "tags": ["invoice"]}'
```

Example response:
```json
{
  "metadata": {
    "currentPage": 0,
    "itemsPerPage": 10,
    "currentItems": 1,
    "totalPages": 1,
    "totalItems": 1
  },
  "documents": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "user": "john",
      "name": "invoice-march.pdf",
      "tags": ["invoice", "finance"],
      "size": 204800,
      "type": "application/pdf",
      "createdAt": "2026-03-30T23:00:00Z"
    }
  ]
}
```

---

**3. Download a document (presigned URL)**

Use the **Download Document** request — copy an `id` from the search response and set it as the `:documentId` path variable.

```bash
# curl equivalent
curl http://localhost:8080/document-management/download/550e8400-e29b-41d4-a716-446655440000
```

Example response:
```json
{
  "url": "http://localhost:9000/document-bucket/john/invoice-march.pdf?X-Amz-Algorithm=..."
}
```

The returned URL is valid for **60 minutes** and can be opened directly in a browser to download the file.

---

#### Implementation Notes 📝

**Memory constraint (50MB heap):**
The upload endpoint accepts `multipart/form-data` and streams `MultipartFile.getInputStream()` directly into MinIO's `putObject()` call. Files are never fully loaded into the JVM heap — Spring spools large uploads to a temp disk file above the multipart threshold, and MinIO reads from that stream. This makes it possible to handle 500MB uploads within the 50MB heap limit.

**Storage layout:**
Files are stored in MinIO under `document-bucket/{user}/{documentName}`, matching the directory structure defined in the requirements.

**Database schema:**
Tags are stored in a separate `document_tags` table (one-to-many) rather than as an array column to ensure proper relational integrity, cascade deletes, and efficient tag-based filtering via SQL subqueries. Indices are created on `user_name`, `document_name`, `created_at` (DESC), and `tag` columns to support the expected query patterns.

**Search:**
Dynamic filtering is implemented using Spring Data JPA `Specification` — predicates are composed at runtime based on which filters are present. Tag filtering uses an `EXISTS` subquery to match documents that have **all** requested tags. Results are always ordered by `created_at DESC`.

**Error handling:**
A `@RestControllerAdvice` global handler maps all exceptions to structured `ApiError` responses with HTTP status, message, and timestamp. Validation errors include per-field details.

**Assumptions:**
- No authentication/authorization is required (not in the spec).
- Document names are not required to be unique per user — the same user can upload multiple files with the same name (they overwrite in MinIO but get separate DB records).
- The `file` field in the upload is required even though the OpenAPI spec shows the request as `application/json` — the actual contract is `multipart/form-data` since a binary file must be transferred.

---

**⚠️ Important Note About the Challenge Completion ⚠️**

Even if you are unable to complete the challenge 100%, please explain why you couldn't proceed, what doubts you had, and any blockers you encountered. We will review each case individually to determine how it impacts the evaluation.

### **Note: Your approach, problem-solving skills, and reasoning are just as important as the final implementation.**

---

