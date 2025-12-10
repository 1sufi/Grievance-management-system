# ResolveIT Smart Grievance and Feedback Management System

A full-stack grievance management platform built with Spring Boot & Vue.j

## Overview
ResolveIT is a modern Grievance & Feedback Management System designed to help citizens submit complaints online, track real-time status, and ensure transparent resolution through Admin and Officer workflows.
It supports:
✅ Role-based access
✅ Complaint escalation
✅ Analytics dashboards
✅ PDF/CSV exports
✅ Ratings & feedback
✅ Anonymous complaints
✅ Email notifications
✅ JWT & Google OAuth login

This system is suitable for:
Smart Cities, Municipal Corporations, Universities, IT Helpdesk Systems, Government Services, and Corporate Support Platforms.

## Key Features
👤-**Citizen**
   Register/Login with JWT authentication
   -**Submit complaints with:**
         Category 
         Priority
         File attachments

   Track complaint status
   Download complaint history as PDF & CSV
   Submit rating & feedback after resolution
   Submit anonymous complaints
   Receive email notifications

🛠️ -**Admin**
View all complaints
Assign complaints to officers
Set deadlines
Monitor escalated complaints
View analytics dashboards
Export data
Manage officers and citizens

🧑‍💼 -**Officer**
View assigned complaints
Update complaint status
Add remarks
Resolve complaints
Track deadlines

📊-**Analytics**
Total / Open / Resolved KPIs
Status distribution
Complaints by category
Resolution progress (Pie & Bar charts)
Upcoming deadlines

🔐-**Security**
JWT authentication
Google OAuth login
## Role-based authorization:
-**CITIZEN**
-**ADMIN**
-**OFFICER**


## Technology Stack

### Backend
- **Spring Boot 3.2.0**
- **Spring Security** with JWT authentication
- **Spring Data JPA** for database operations
- **H2 Database** (development) / MySQL (production)
- **Maven** for dependency management
- **Java mail sender**

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

## Installation & Setup

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
     
5. **Demo Workflow**
 1️⃣ Login as Citizen
 2️⃣ Submit Complaint
 3️⃣ Show Complaint in List
 4️⃣ Admin assigns Officer
 5️⃣ Officer updates status
 6️⃣ Escalation if unresolved
 7️⃣ Complaint resolved
 8️⃣ Citizen downloads PDF/CSV
 9️⃣ Citizen submits rating & feedback

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

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

⭐ Support
If you like this project, please give it a ⭐ on GitHub!
