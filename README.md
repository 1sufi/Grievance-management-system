# ResolveIT Smart Grievance and Feedback Management System

A comprehensive grievance management system built with Spring Boot backend and Vue.js frontend.

## Features

### Module 1: User Authentication and Submission Options
- **Login/Registration**: JWT-based authentication with role-based access
- **Anonymous Submission**: Option to submit complaints without creating an account
- **Role Management**: Support for Citizens, Officers, and Admins

### Module 2: Complaint Management
- **Complaint Creation**: Submit complaints with categories, priorities, and file attachments
- **Category Selection**: Sanitation, Traffic, Water, Electricity, Roads, etc.
- **Priority Levels**: Low, Medium, High, Urgent
- **File Upload**: Support for images and documents as evidence

### Module 3: Status Tracking and Timeline
- **Status Flow**: New → Under Review → In Progress → Resolved
- **Timeline View**: Chronological log of all status changes
- **Admin Comments**: Visible updates with timestamps

### Module 4: Escalation and Admin Panel
- **Admin Dashboard**: Overview of all complaints and statistics
- **Officer Assignment**: Assign complaints to specific officers
- **Escalation Management**: Automatic escalation for unresolved complaints
- **Officer Management**: Add, edit, and manage officer accounts

## Technology Stack

### Backend
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **H2 Database** (development) / MySQL (production)
- **Maven** for dependency management

### Frontend
- **Vue.js 3**
- **Vue Router** for navigation
- **Vuex** for state management
- **Element Plus** for UI components
- **Axios** for HTTP requests

## Project Structure

```
ResolveIT Management System/
├── src/main/java/com/resolveit/grievancemanagement/
│   ├── controller/          # REST API controllers
│   ├── dto/                 # Data Transfer Objects
│   ├── entity/              # JPA entities
│   ├── repository/          # Data repositories
│   ├── security/            # Security configuration
│   └── service/             # Business logic services
├── src/main/resources/
│   └── application.yml      # Application configuration
├── frontend/
│   ├── src/
│   │   ├── components/      # Vue components
│   │   ├── views/           # Page components
│   │   ├── router/          # Vue Router configuration
│   │   └── store/           # Vuex store
│   └── package.json
└── pom.xml                  # Maven configuration
```

## Getting Started

### Prerequisites
- Java 21 (LTS) - ✅ You have Java 21.0.9
- Node.js 16 or higher
- Maven 3.6 or higher - ✅ You have Maven 3.9.11

### Backend Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd "ResolveIT Management System"
   ```

2. **Install dependencies**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - API: http://localhost:8080
   - H2 Console: http://localhost:8080/h2-console

### Frontend Setup

1. **Navigate to frontend directory**
   ```bash
   cd frontend
   ```

2. **Install dependencies**
   ```bash
   npm install
   ```

3. **Run the development server**
   ```bash
   npm run serve
   ```

4. **Access the application**
   - Frontend: http://localhost:3000

## API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signout` - User logout

### Complaints
- `GET /api/complaints` - Get user's complaints
- `POST /api/complaints` - Create new complaint
- `GET /api/complaints/{id}` - Get complaint details
- `PUT /api/complaints/{id}` - Update complaint
- `POST /api/complaints/anonymous` - Submit anonymous complaint

### Admin
- `GET /api/admin/complaints` - Get all complaints (Admin)
- `PUT /api/admin/complaints/{id}/assign` - Assign complaint to officer
- `PUT /api/admin/complaints/{id}/escalate` - Escalate complaint
- `GET /api/admin/officers` - Get all officers
- `POST /api/admin/officers` - Create new officer

## Database Schema

### Users Table
- id, username, email, password, role, first_name, last_name, phone_number, is_enabled, created_at, updated_at

### Complaints Table
- id, title, description, category, priority, status, is_anonymous, anonymous_email, anonymous_phone, user_id, assigned_officer_id, due_date, resolved_at, created_at, updated_at

### Complaint Comments Table
- id, comment, complaint_id, user_id, is_internal, created_at

### Complaint Status History Table
- id, status, comment, complaint_id, changed_by, changed_at

### File Uploads Table
- id, file_name, file_path, file_type, file_size, complaint_id, uploaded_by, uploaded_at

## Configuration

### Application Properties
- Database configuration (H2 for development)
- JWT secret and expiration
- File upload limits
- CORS settings
- Email configuration for password reset

### Security Configuration
- JWT token authentication
- Role-based access control
- CORS enabled for frontend communication
- Password encryption with BCrypt

## Development Notes

1. **Database**: Currently configured with H2 in-memory database for development
2. **Authentication**: JWT tokens are used for stateless authentication
3. **File Upload**: Configured to handle files up to 10MB
4. **CORS**: Configured to allow requests from localhost:3000 (Vue.js dev server)

## Next Steps

1. Complete complaint management controllers and services
2. Implement file upload functionality
3. Create admin panel components
4. Add email notifications
5. Implement escalation logic
6. Add comprehensive testing
7. Deploy to production environment

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License.
