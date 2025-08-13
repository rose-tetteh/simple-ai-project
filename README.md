# Simple AI Project

A Spring Boot application that integrates OpenAI's GPT models for intelligent document analysis and processing. This project provides a RESTful API for file upload, AI-powered content analysis, PDF generation, and user management with JWT authentication.

## 🚀 Features

- **AI-Powered Document Analysis**: Upload documents and get intelligent analysis using OpenAI's GPT models
- **PDF Generation**: Automatically generate PDF reports from AI analysis
- **User Authentication**: JWT-based authentication and authorization
- **File Storage**: AWS S3 integration for secure file storage
- **Chat History**: Track and retrieve user interaction history
- **RESTful API**: Clean REST endpoints for all operations
- **Database Integration**: PostgreSQL for persistent data storage

## 🏗️ Architecture Overview

The application follows a layered architecture pattern:

```
┌─────────────────────────────────────────────────────────────┐
│                     CLIENT LAYER                            │
│                  Web/Mobile Client                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                      API LAYER                              │
│              ChatController  │  UserController              │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                   SERVICE LAYER                             │
│    AiService  │  UserService  │  PDFGenerator  │  S3Service │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│               EXTERNAL SERVICES                             │
│              OpenAI API  │  AWS S3                          │
└─────────────────────────┬───────────────────────────────────┘
                          │
┌─────────────────────────▼───────────────────────────────────┐
│                    DATA LAYER                               │
│              PostgreSQL Database                            │
│            User Entity  │  ChatHistory Entity               │
└─────────────────────────────────────────────────────────────┘
```

### Component Responsibilities:

- **Controllers**: Handle HTTP requests and responses
- **Services**: Contain business logic and orchestrate operations
- **Repositories**: Data access layer for database operations
- **External Services**: Integration with OpenAI and AWS S3
- **Models**: JPA entities representing database tables

## 🛠️ Technology Stack

```
┌─────────────────────────────────────────────────────────────┐
│                    TECHNOLOGY STACK                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  Backend Framework:     Spring Boot 3.5.3                  │
│  Programming Language:  Java 21                            │
│  Build Tool:           Maven                               │
│                                                             │
│  Database:             PostgreSQL                          │
│  ORM:                  Spring Data JPA                     │
│                                                             │
│  Security:             Spring Security + JWT               │
│  Authentication:       JSON Web Tokens                     │
│                                                             │
│  AI Integration:       Spring AI Framework                 │
│  AI Provider:          OpenAI GPT Models                   │
│                                                             │
│  File Storage:         AWS S3                              │
│  PDF Generation:       iText Library                       │
│                                                             │
│  Utilities:            Lombok, Jakarta Validation          │
│  Development:          Spring Boot DevTools                │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

## 📋 Prerequisites

- Java 21 or higher
- Maven 3.6+
- PostgreSQL database
- OpenAI API key
- AWS S3 bucket (optional, for file storage)

## ⚙️ Configuration

Create a `.env` file or set the following environment variables:

```bash
# Server Configuration
SERVER_PORT=8080

# Database Configuration
DB_PORT=5432
DB_NAME=simple_ai_db
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password

# JWT Configuration
JWT_SECRET=your_jwt_secret_key
JWT_EXP=86400000
JWT_REFRESH_EXP=604800000

# OpenAI Configuration
OPENAI_API_KEY=your_openai_api_key

# AWS S3 Configuration (Optional)
S3_BUCKET_NAME=your_s3_bucket_name
```

## 🚀 Getting Started

### 1. Clone the Repository
```bash
git clone <repository-url>
cd simple-ai-project
```

### 2. Set Up Database
```sql
CREATE DATABASE simple_ai_db;
```

### 3. Configure Environment Variables
Set up your environment variables as described in the Configuration section.

### 4. Build and Run
```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

The application will start on `http://localhost:8080`

## 📚 API Documentation

### API Endpoint Overview

```
Authentication Endpoints:
├── POST /api/v1/auth/register    → User Registration
├── POST /api/v1/auth/login       → User Login
└── POST /api/v1/auth/refresh     → Token Refresh

Chat/AI Endpoints:
├── POST /api/v1/chat/upload      → File Upload & Analysis
└── GET  /api/v1/chat/user-chats  → Get Chat History

User Management:
├── GET  /api/v1/users/profile    → Get User Profile
└── PUT  /api/v1/users/profile    → Update User Profile
```

### Authentication Endpoints

#### Register User
```http
POST /api/v1/auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "userName": "username"
}
```

#### Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

### Chat/AI Endpoints

#### Upload and Analyze File
```http
POST /api/v1/chat/upload
Authorization: Bearer <jwt-token>
Content-Type: multipart/form-data

file: <your-file>
```

#### Get User Chat History
```http
GET /api/v1/chat/user-chats
Authorization: Bearer <jwt-token>
```

## 🔄 Application Flow

Here's how the file processing workflow works:

```
Client                API               AiService           OpenAI            S3              Database         PDFGen
  │                    │                    │                 │                │                  │               │
  │──── Upload File ──▶│                    │                 │                │                  │               │
  │                    │─── Process File ──▶│                 │                │                  │               │
  │                    │                    │─── Store File ─▶│                │                  │               │
  │                    │                    │                 │                │◀─── File Key ───│               │
  │                    │                    │─── Analyze ────▶│                │                  │               │
  │                    │                    │◀── Analysis ────│                │                  │               │
  │                    │                    │─── Generate PDF ──────────────────────────────────────────────────▶│
  │                    │                    │◀── PDF Data ───────────────────────────────────────────────────────│
  │                    │                    │─── Store PDF ──▶│                │                  │               │
  │                    │                    │                 │                │◀─── PDF Key ────│               │
  │                    │                    │─── Save History ────────────────────────────────────▶│               │
  │                    │◀── Response ───────│                 │                │                  │               │
  │◀── Result ─────────│                    │                 │                │                  │               │
```

### Step-by-Step Process:

1. **File Upload**: Client uploads a document via REST API
2. **File Storage**: Original file is stored in AWS S3
3. **AI Analysis**: File content is sent to OpenAI for intelligent analysis
4. **PDF Generation**: Analysis results are formatted into a PDF report
5. **PDF Storage**: Generated PDF is stored in AWS S3
6. **History Tracking**: Chat history is saved to PostgreSQL database
7. **Response**: Client receives processing results and file URLs

## 📁 Project Structure

```
src/
├── main/
│   ├── java/com/example/simple_ai_project/
│   │   ├── config/           # Configuration classes
│   │   ├── controller/       # REST controllers
│   │   │   ├── ChatController.java
│   │   │   └── UserController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   ├── model/            # JPA entities
│   │   │   ├── User.java
│   │   │   └── ChatHistory.java
│   │   ├── repository/       # Data repositories
│   │   ├── security/         # Security configuration
│   │   ├── service/          # Business logic
│   │   │   ├── AiService.java
│   │   │   ├── UserService.java
│   │   │   ├── PDFGenerator.java
│   │   │   └── S3Service.java
│   │   ├── util/             # Utility classes
│   │   └── SimpleAiProjectApplication.java
│   └── resources/
│       └── application.properties
└── test/                     # Test classes
```

## 🔒 Security Features

- JWT-based authentication
- Password encryption
- Secure API endpoints
- File upload validation
- Request size limitations

## 📊 Database Schema

The application uses PostgreSQL with the following entity relationships:

```
┌─────────────────────────────────┐         ┌─────────────────────────────────┐
│             USER                │         │         CHAT_HISTORY            │
├─────────────────────────────────┤         ├─────────────────────────────────┤
│ id (PK)              : Long     │         │ id (PK)              : Long     │
│ email (UK)           : String   │◀────────│ userId (FK)          : Long     │
│ password             : String   │   1:N   │ originalFileName     : String   │
│ userName             : String   │         │ fileKey              : String   │
│ createdAt            : DateTime │         │ analysis             : String   │
│ updatedAt            : DateTime │         │ pdfKey               : String   │
│ isActive             : Boolean  │         │ createdAt            : DateTime │
└─────────────────────────────────┘         └─────────────────────────────────┘
```

### Entity Descriptions:

**User Entity:**
- Stores user authentication and profile information
- Each user can have multiple chat histories
- Email serves as unique identifier for login

**ChatHistory Entity:**
- Tracks all file processing interactions
- Links to original files and generated PDFs via S3 keys
- Stores AI analysis results for future reference
- Maintains timestamp for chronological ordering

## 🧪 Testing

Run the test suite:
```bash
./mvnw test
```

## 📝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🤝 Support

For support, email your-email@example.com or create an issue in the repository.

## 🔮 Future Enhancements

- [ ] Support for more file formats
- [ ] Real-time chat interface
- [ ] Advanced AI model selection
- [ ] Batch file processing
- [ ] API rate limiting
- [ ] Comprehensive logging and monitoring
