# SpatialMeet - Spatial Audio Conferencing Platform

A modern Java Spring Boot application for spatial audio conferencing with real-time communication capabilities.

## Features

- **User Management System**
  - User registration and authentication
  - JWT-based security
  - Role-based access control (USER, MODERATOR, ADMIN)
  - User presence tracking
  - **Guest Mode**: Quick access without registration

- **Room Management**
  - Create and join conference rooms
  - Room codes for easy access
  - Public and private rooms
  - Participant limits
  - Real-time participant tracking

- **Spatial Audio Support**
  - 3D positioning system for participants
  - Real-time position updates
  - Audio settings management (microphone, speaker, volume)

- **WebRTC Integration**
  - WebRTC signaling server
  - Peer-to-peer connection establishment
  - ICE candidate exchange
  - Offer/Answer model support

- **Real-time Communication**
  - WebSocket-based messaging
  - Real-time position updates
  - Audio settings synchronization
  - User join/leave notifications

## Technology Stack

- **Backend**: Java 17, Spring Boot 3.2.8
- **Security**: Spring Security, JWT
- **Database**: H2 (development), MySQL (production)
- **Real-time**: WebSocket, STOMP
- **Frontend**: HTML5, CSS3, JavaScript, Font Awesome
- **Build Tool**: Maven

## API Endpoints

### Authentication
- `POST /api/auth/signin` - User login
- `POST /api/auth/signup` - User registration
- `POST /api/auth/signout` - User logout

### Guest Access
- `POST /api/guest/enter` - Enter as guest with optional display name
- `GET /api/rooms/public` - Get public rooms (guest accessible)

### Room Management
- `GET /api/rooms` - Get public rooms
- `GET /api/rooms/my` - Get user's rooms
- `POST /api/rooms` - Create new room
- `GET /api/rooms/{roomCode}` - Get room details
- `POST /api/rooms/{roomCode}/join` - Join room
- `POST /api/rooms/{roomCode}/leave` - Leave room
- `GET /api/rooms/{roomCode}/participants` - Get room participants

### WebSocket Endpoints
- `/ws` - WebSocket connection endpoint
- `/app/room/{roomCode}/offer` - WebRTC offer
- `/app/room/{roomCode}/answer` - WebRTC answer
- `/app/room/{roomCode}/ice-candidate` - ICE candidate
- `/app/room/{roomCode}/position` - Position update
- `/app/room/{roomCode}/audio-settings` - Audio settings

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```

The application will start on `http://localhost:8083/api/`

### Accessing the Application
- **Main Interface**: http://localhost:8083/api/
- **Guest Mode**: Click "Continue as Guest" for quick access
- **H2 Database Console**: http://localhost:8083/api/h2-console

## Usage

### For Users
1. **Login**: Use existing credentials or register a new account
2. **Guest Mode**: Click "Continue as Guest" for immediate access
3. **Create Room**: Click the centered "Create New Room" button
4. **Join Room**: Enter a room code or select from public rooms
5. **Audio Settings**: Configure microphone, speaker, and volume settings
6. **Spatial Positioning**: Move around the virtual space for spatial audio

### For Developers
1. **API Testing**: Use tools like Postman to test REST endpoints
2. **WebSocket Testing**: Connect to `/ws` endpoint for real-time features
3. **Database Inspection**: Use H2 console to view data structure
4. **Frontend Development**: Modify files in `src/main/resources/static/`

### Database Console (Development)
Access H2 console at: `http://localhost:8083/api/h2-console`
- JDBC URL: `jdbc:h2:mem:spatialmeet`
- Username: `sa`
- Password: `password`

## Configuration

### Application Properties
Key configuration properties in `application.properties`:

```properties
# Server Configuration
server.port=8083
server.servlet.context-path=/api

# Database Configuration
spring.datasource.url=jdbc:h2:mem:spatialmeet
spring.datasource.username=sa
spring.datasource.password=password

# JWT Configuration
app.jwt.secret=mySecretKey
app.jwt.expiration=86400000

# WebSocket Configuration
app.websocket.allowed-origins=http://localhost:3000,http://localhost:8083
```

## Architecture

### Frontend Features
- **Responsive Design**: Mobile-friendly interface with CSS Grid and Flexbox
- **Guest Mode**: Modal-based guest entry with optional display name
- **Real-time UI**: Live updates for room participants and audio settings
- **Modern Styling**: Font Awesome icons and professional color scheme
- **WebSocket Integration**: SockJS and STOMP client for real-time communication

### Entity Model
- **User**: User account information and authentication
- **Role**: User roles and permissions
- **Room**: Conference room information
- **UserRoom**: Participant data with spatial positioning

### Security
- JWT token-based authentication
- Role-based authorization
- CORS configuration for cross-origin requests

### Real-time Features
- WebSocket connections using STOMP protocol
- Topic-based messaging for room-wide broadcasts
- Queue-based messaging for peer-to-peer communication

## WebRTC Integration

The platform provides WebRTC signaling through WebSocket messages:

1. **Connection Establishment**: Users exchange offers and answers
2. **ICE Candidate Exchange**: Network connectivity information
3. **Spatial Audio**: 3D positioning for audio spatialization

## Development

### Project Structure
```
src/main/java/com/spatialmeet/
├── config/           # Configuration classes
├── controller/       # REST and WebSocket controllers
├── dto/             # Data Transfer Objects
├── entity/          # JPA entities
├── repository/      # Data repositories
├── security/        # Security configuration
└── service/         # Business logic services

src/main/resources/
├── static/          # Frontend files (HTML, CSS, JS)
│   ├── index.html   # Main application interface
│   ├── styles.css   # Application styling
│   └── script.js    # Frontend JavaScript logic
└── application.properties

frontend/            # Development frontend files
├── index.html
├── styles.css
└── script.js
```

### Building
```bash
mvn clean compile
```

### Testing
```bash
mvn test
```

### Packaging
```bash
mvn clean package
```

## Troubleshooting

### Common Issues

1. **Port Already in Use**
   - Change the port in `application.properties`: `server.port=8084`
   - Or kill existing Java processes: `taskkill /f /im java.exe`

2. **404 Not Found**
   - Make sure to use the correct URL: `http://localhost:8083/api/`
   - The context path `/api` is required

3. **WebSocket Connection Issues**
   - Check CORS settings in `WebSocketConfig.java`
   - Verify allowed origins match your frontend URL

4. **Database Connection Issues**
   - H2 database is in-memory and recreated on each restart
   - Check H2 console URL: `http://localhost:8083/api/h2-console`

5. **Guest Mode Not Working**
   - Ensure guest endpoints are permitted in security configuration
   - Check browser console for JavaScript errors

### Performance Tips
- Use browser developer tools to monitor WebSocket connections
- Check network tab for failed API requests
- Monitor application logs for errors and warnings

## Preview

![image alt](https://github.com/vishalv47/SpatialMeet/blob/9d219b1fae9002e5607e05181b79a9f3eef92e04/Screenshot.png)

## License

This project is licensed under the MIT License.
