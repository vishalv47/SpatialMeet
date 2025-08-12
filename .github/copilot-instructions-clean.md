<!-- Use this file to provide workspace-specific custom instructions to Copilot. For more details, visit https://code.visualstudio.com/docs/copilot/copilot-customization#_use-a-githubcopilotinstructionsmd-file -->

# SpatialMeet - Spatial Audio Conferencing Platform

## Project Overview
A Java Spring Boot application for spatial audio conferencing with:
- User management system with JWT authentication
- WebRTC integration for real-time audio/video
- Spatial audio positioning (3D coordinates)
- RESTful APIs and WebSocket support
- Room creation and management
- Real-time messaging and presence tracking

## Technology Stack
- Java 17+ with Spring Boot 3.x
- Spring Security with JWT tokens
- Spring Data JPA with H2/MySQL database
- WebSocket/STOMP for real-time communication
- Maven build system

## Setup Instructions

### Prerequisites
1. Install Java 17 or higher
2. Install Maven 3.6 or higher
3. Clone/open this project in VS Code

### Building and Running
```bash
# Build the project
mvn clean compile

# Run the application
mvn spring-boot:run
```

### Access Points
- Application: http://localhost:8080
- H2 Console: http://localhost:8080/api/h2-console
- WebSocket: ws://localhost:8080/ws

## Key Features Implemented

### Authentication & Security
- JWT-based authentication
- Role-based access control (USER, MODERATOR, ADMIN)
- CORS configuration for cross-origin requests
- Secure password encoding

### Room Management
- Create public/private rooms with unique codes
- Join/leave rooms with participant limits
- Real-time participant tracking
- Room creator privileges

### Spatial Audio Features
- 3D positioning system (x, y, z coordinates)
- Real-time position updates via WebSocket
- Audio settings (microphone, speaker, volume)
- WebRTC signaling for peer-to-peer connections

### WebRTC Integration
- Offer/Answer exchange for connection establishment
- ICE candidate handling for NAT traversal
- Peer-to-peer audio/video streaming support

### Real-time Communication
- WebSocket connections using STOMP protocol
- Topic-based messaging for room broadcasts
- Queue-based messaging for direct user communication
- Live position and audio settings synchronization

## API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signout` - User logout

### Room Management
- `GET /api/rooms` - List public rooms
- `POST /api/rooms` - Create new room
- `GET /api/rooms/{roomCode}` - Get room details
- `POST /api/rooms/{roomCode}/join` - Join room
- `POST /api/rooms/{roomCode}/leave` - Leave room

### WebSocket Endpoints
- `/app/room/{roomCode}/offer` - WebRTC offer
- `/app/room/{roomCode}/answer` - WebRTC answer
- `/app/room/{roomCode}/ice-candidate` - ICE candidate
- `/app/room/{roomCode}/position` - Position update
- `/app/room/{roomCode}/audio-settings` - Audio settings

## Development Notes
- H2 in-memory database for development (check console at /h2-console)
- MySQL configuration available for production
- WebSocket security integrated with JWT authentication
- Spatial positioning stored in UserRoom entity
- Real-time updates broadcast to all room participants
