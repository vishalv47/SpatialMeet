// API Configuration
const API_BASE_URL = 'http://localhost:8080/api';
let authToken = localStorage.getItem('authToken');
let currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');
let currentRoom = null;
let stompClient = null;
let userPosition = { x: 50, y: 50, z: 0 };
let participants = new Map();

// Audio settings
let microphoneEnabled = true;
let speakerEnabled = true;
let volume = 50;

// Initialize the application
document.addEventListener('DOMContentLoaded', function() {
    if (authToken && currentUser) {
        showApp();
        loadRooms();
    } else {
        showAuth();
    }
    
    setupEventListeners();
});

// Event Listeners
function setupEventListeners() {
    // Auth forms
    document.getElementById('login-form').addEventListener('submit', handleLogin);
    document.getElementById('register-form').addEventListener('submit', handleRegister);
    document.getElementById('create-room-form').addEventListener('submit', handleCreateRoom);
    
    // Spatial canvas
    const canvas = document.getElementById('spatial-canvas');
    canvas.addEventListener('click', handleCanvasClick);
    canvas.addEventListener('mousemove', handleCanvasMouseMove);
    
    // Make self avatar draggable
    const selfAvatar = document.getElementById('self-avatar');
    makeDraggable(selfAvatar);
}

// Authentication Functions
async function handleLogin(event) {
    event.preventDefault();
    
    const username = document.getElementById('login-username').value;
    const password = document.getElementById('login-password').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signin`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, password })
        });
        
        if (response.ok) {
            const data = await response.json();
            authToken = data.token;
            currentUser = {
                id: data.id,
                username: data.username,
                email: data.email,
                displayName: data.displayName
            };
            
            localStorage.setItem('authToken', authToken);
            localStorage.setItem('currentUser', JSON.stringify(currentUser));
            
            showToast('Login successful!', 'success');
            showApp();
            loadRooms();
        } else {
            const error = await response.json();
            showToast(error.message || 'Login failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
        console.error('Login error:', error);
    }
}

async function handleRegister(event) {
    event.preventDefault();
    
    const username = document.getElementById('register-username').value;
    const email = document.getElementById('register-email').value;
    const displayName = document.getElementById('register-displayname').value;
    const password = document.getElementById('register-password').value;
    
    try {
        const response = await fetch(`${API_BASE_URL}/auth/signup`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({ username, email, displayName, password })
        });
        
        if (response.ok) {
            showToast('Registration successful! Please login.', 'success');
            showLogin();
        } else {
            const error = await response.json();
            showToast(error.message || 'Registration failed', 'error');
        }
    } catch (error) {
        showToast('Network error. Please try again.', 'error');
        console.error('Registration error:', error);
    }
}

function logout() {
    authToken = null;
    currentUser = null;
    currentRoom = null;
    
    localStorage.removeItem('authToken');
    localStorage.removeItem('currentUser');
    
    if (stompClient) {
        stompClient.disconnect();
        stompClient = null;
    }
    
    showToast('Logged out successfully', 'info');
    showAuth();
}

// UI Navigation Functions
function showAuth() {
    document.getElementById('auth-container').style.display = 'block';
    document.getElementById('app-container').style.display = 'none';
    document.getElementById('nav-user').style.display = 'none';
}

function showApp() {
    document.getElementById('auth-container').style.display = 'none';
    document.getElementById('app-container').style.display = 'flex';
    document.getElementById('nav-user').style.display = 'flex';
    document.getElementById('user-display-name').textContent = currentUser.displayName;
    
    showWelcomeScreen();
}

function showWelcomeScreen() {
    document.getElementById('welcome-screen').style.display = 'flex';
    document.getElementById('room-interface').style.display = 'none';
}

function showRoomInterface() {
    document.getElementById('welcome-screen').style.display = 'none';
    document.getElementById('room-interface').style.display = 'flex';
}

function showLogin() {
    document.getElementById('login-form').style.display = 'block';
    document.getElementById('register-form').style.display = 'none';
    document.querySelector('.tab-btn:first-child').classList.add('active');
    document.querySelector('.tab-btn:last-child').classList.remove('active');
}

function showRegister() {
    document.getElementById('login-form').style.display = 'none';
    document.getElementById('register-form').style.display = 'block';
    document.querySelector('.tab-btn:first-child').classList.remove('active');
    document.querySelector('.tab-btn:last-child').classList.add('active');
}

// Room Management Functions
async function loadRooms() {
    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, {
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            const rooms = await response.json();
            displayRooms(rooms);
        } else {
            showToast('Failed to load rooms', 'error');
        }
    } catch (error) {
        showToast('Network error loading rooms', 'error');
        console.error('Load rooms error:', error);
    }
}

function displayRooms(rooms) {
    const roomList = document.getElementById('room-list');
    roomList.innerHTML = '';
    
    rooms.forEach(room => {
        const roomElement = document.createElement('div');
        roomElement.className = 'room-item';
        roomElement.onclick = () => joinRoom(room.id);
        
        roomElement.innerHTML = `
            <h4>${room.name}</h4>
            <p>${room.description || 'No description'}</p>
            <div class="room-participants">
                <i class="fas fa-users"></i> 
                ${room.currentParticipants || 0}/${room.maxParticipants} participants
            </div>
        `;
        
        roomList.appendChild(roomElement);
    });
}

async function handleCreateRoom(event) {
    event.preventDefault();
    
    const name = document.getElementById('room-name').value;
    const description = document.getElementById('room-description').value;
    const maxParticipants = parseInt(document.getElementById('max-participants').value);
    const isPrivate = document.getElementById('room-private').checked;
    
    try {
        const response = await fetch(`${API_BASE_URL}/rooms`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${authToken}`
            },
            body: JSON.stringify({
                name,
                description,
                maxParticipants,
                isPrivate
            })
        });
        
        if (response.ok) {
            const room = await response.json();
            showToast(`Room "${room.name}" created successfully!`, 'success');
            closeCreateRoomModal();
            loadRooms();
            joinRoom(room.id);
        } else {
            const error = await response.json();
            showToast(error.message || 'Failed to create room', 'error');
        }
    } catch (error) {
        showToast('Network error creating room', 'error');
        console.error('Create room error:', error);
    }
}

async function joinRoom(roomId) {
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${roomId}/join`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            // Get room details
            const roomResponse = await fetch(`${API_BASE_URL}/rooms/${roomId}`, {
                headers: {
                    'Authorization': `Bearer ${authToken}`
                }
            });
            
            if (roomResponse.ok) {
                currentRoom = await roomResponse.json();
                showRoomInterface();
                updateRoomInfo();
                connectWebSocket();
                showToast(`Joined room "${currentRoom.name}"`, 'success');
            }
        } else {
            const error = await response.json();
            showToast(error.message || 'Failed to join room', 'error');
        }
    } catch (error) {
        showToast('Network error joining room', 'error');
        console.error('Join room error:', error);
    }
}

async function leaveRoom() {
    if (!currentRoom) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/rooms/${currentRoom.id}/leave`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${authToken}`
            }
        });
        
        if (response.ok) {
            showToast(`Left room "${currentRoom.name}"`, 'info');
            currentRoom = null;
            participants.clear();
            
            if (stompClient) {
                stompClient.disconnect();
                stompClient = null;
            }
            
            showWelcomeScreen();
            loadRooms();
        }
    } catch (error) {
        showToast('Error leaving room', 'error');
        console.error('Leave room error:', error);
    }
}

function updateRoomInfo() {
    if (!currentRoom) return;
    
    document.getElementById('current-room-name').textContent = currentRoom.name;
    document.getElementById('current-room-code').textContent = currentRoom.roomCode;
}

// WebSocket Functions
function connectWebSocket() {
    const socket = new SockJS(`${API_BASE_URL}/ws`);
    stompClient = Stomp.over(socket);
    
    stompClient.connect({
        'Authorization': `Bearer ${authToken}`
    }, function(frame) {
        console.log('Connected to WebSocket');
        
        // Subscribe to room messages
        stompClient.subscribe(`/topic/room/${currentRoom.id}`, function(message) {
            const data = JSON.parse(message.body);
            handleWebSocketMessage(data);
        });
        
        // Send initial position
        sendPositionUpdate();
    }, function(error) {
        console.error('WebSocket connection error:', error);
        showToast('Failed to connect to real-time messaging', 'error');
    });
}

function handleWebSocketMessage(data) {
    switch (data.type) {
        case 'USER_JOINED':
            addParticipant(data.user);
            showToast(`${data.user.displayName} joined the room`, 'info');
            break;
        case 'USER_LEFT':
            removeParticipant(data.userId);
            showToast(`${data.user.displayName} left the room`, 'info');
            break;
        case 'POSITION_UPDATE':
            updateParticipantPosition(data.userId, data.position);
            break;
        case 'AUDIO_STATE_CHANGE':
            updateParticipantAudioState(data.userId, data.audioState);
            break;
    }
}

function sendPositionUpdate() {
    if (stompClient && currentRoom) {
        stompClient.send(`/app/room/${currentRoom.id}/position`, {}, JSON.stringify({
            userId: currentUser.id,
            position: userPosition,
            audioState: {
                microphoneEnabled,
                speakerEnabled,
                volume
            }
        }));
    }
}

// Spatial Audio Functions
function handleCanvasClick(event) {
    const rect = event.target.getBoundingClientRect();
    const x = ((event.clientX - rect.left) / rect.width) * 100;
    const y = ((event.clientY - rect.top) / rect.height) * 100;
    
    moveToPosition(x, y);
}

function handleCanvasMouseMove(event) {
    // Add hover effects or preview positioning here if needed
}

function moveToPosition(x, y) {
    userPosition.x = Math.max(0, Math.min(100, x));
    userPosition.y = Math.max(0, Math.min(100, y));
    
    const selfAvatar = document.getElementById('self-avatar');
    selfAvatar.style.left = userPosition.x + '%';
    selfAvatar.style.top = userPosition.y + '%';
    
    sendPositionUpdate();
}

function makeDraggable(element) {
    let isDragging = false;
    let startX, startY;
    
    element.addEventListener('mousedown', function(e) {
        isDragging = true;
        startX = e.clientX - element.offsetLeft;
        startY = e.clientY - element.offsetTop;
        element.style.cursor = 'grabbing';
    });
    
    document.addEventListener('mousemove', function(e) {
        if (!isDragging) return;
        
        const canvas = document.getElementById('spatial-canvas');
        const rect = canvas.getBoundingClientRect();
        
        const x = ((e.clientX - rect.left) / rect.width) * 100;
        const y = ((e.clientY - rect.top) / rect.height) * 100;
        
        if (x >= 0 && x <= 100 && y >= 0 && y <= 100) {
            moveToPosition(x, y);
        }
    });
    
    document.addEventListener('mouseup', function() {
        if (isDragging) {
            isDragging = false;
            element.style.cursor = 'move';
        }
    });
}

// Participant Management
function addParticipant(user) {
    participants.set(user.id, user);
    updateParticipantsList();
    createParticipantAvatar(user);
}

function removeParticipant(userId) {
    participants.delete(userId);
    updateParticipantsList();
    
    const avatar = document.getElementById(`participant-${userId}`);
    if (avatar) {
        avatar.remove();
    }
}

function updateParticipantPosition(userId, position) {
    const avatar = document.getElementById(`participant-${userId}`);
    if (avatar) {
        avatar.style.left = position.x + '%';
        avatar.style.top = position.y + '%';
    }
}

function updateParticipantAudioState(userId, audioState) {
    const participant = participants.get(userId);
    if (participant) {
        participant.audioState = audioState;
        updateParticipantsList();
    }
}

function createParticipantAvatar(user) {
    const canvas = document.getElementById('spatial-canvas');
    const avatar = document.createElement('div');
    avatar.id = `participant-${user.id}`;
    avatar.className = 'user-avatar other';
    avatar.style.left = '20%';
    avatar.style.top = '20%';
    
    avatar.innerHTML = `
        <i class="fas fa-user"></i>
        <span class="avatar-name">${user.displayName}</span>
    `;
    
    canvas.appendChild(avatar);
}

function updateParticipantsList() {
    const participantsList = document.getElementById('participants-list');
    const participantCount = document.getElementById('participant-count');
    
    participantsList.innerHTML = '';
    
    // Add self
    const selfBadge = document.createElement('div');
    selfBadge.className = 'participant-badge';
    selfBadge.innerHTML = `<i class="fas fa-user"></i> ${currentUser.displayName} (You)`;
    participantsList.appendChild(selfBadge);
    
    // Add other participants
    participants.forEach(participant => {
        const badge = document.createElement('div');
        badge.className = 'participant-badge';
        
        const micIcon = participant.audioState?.microphoneEnabled ? 'fa-microphone' : 'fa-microphone-slash';
        const speakerIcon = participant.audioState?.speakerEnabled ? 'fa-volume-up' : 'fa-volume-mute';
        
        badge.innerHTML = `
            <i class="fas fa-user"></i> ${participant.displayName}
            <i class="fas ${micIcon}"></i>
            <i class="fas ${speakerIcon}"></i>
        `;
        
        participantsList.appendChild(badge);
    });
    
    participantCount.textContent = participants.size + 1; // +1 for self
}

// Audio Controls
function toggleMicrophone() {
    microphoneEnabled = !microphoneEnabled;
    const micBtn = document.getElementById('mic-btn');
    
    if (microphoneEnabled) {
        micBtn.classList.remove('muted');
        micBtn.innerHTML = '<i class="fas fa-microphone"></i>';
    } else {
        micBtn.classList.add('muted');
        micBtn.innerHTML = '<i class="fas fa-microphone-slash"></i>';
    }
    
    sendPositionUpdate();
    showToast(microphoneEnabled ? 'Microphone enabled' : 'Microphone muted', 'info');
}

function toggleSpeaker() {
    speakerEnabled = !speakerEnabled;
    const speakerBtn = document.getElementById('speaker-btn');
    
    if (speakerEnabled) {
        speakerBtn.classList.remove('muted');
        speakerBtn.innerHTML = '<i class="fas fa-volume-up"></i>';
    } else {
        speakerBtn.classList.add('muted');
        speakerBtn.innerHTML = '<i class="fas fa-volume-mute"></i>';
    }
    
    sendPositionUpdate();
    showToast(speakerEnabled ? 'Speaker enabled' : 'Speaker muted', 'info');
}

function adjustVolume(value) {
    volume = parseInt(value);
    sendPositionUpdate();
}

// Modal Functions
function showCreateRoomModal() {
    document.getElementById('create-room-modal').classList.add('show');
}

function closeCreateRoomModal() {
    document.getElementById('create-room-modal').classList.remove('show');
    document.getElementById('create-room-form').reset();
}

// Utility Functions
function showToast(message, type = 'info') {
    const toastContainer = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    
    toastContainer.appendChild(toast);
    
    setTimeout(() => {
        toast.remove();
    }, 5000);
}

// Close modal when clicking outside
document.getElementById('create-room-modal').addEventListener('click', function(e) {
    if (e.target === this) {
        closeCreateRoomModal();
    }
});

// Keyboard shortcuts
document.addEventListener('keydown', function(e) {
    if (e.key === 'Escape') {
        closeCreateRoomModal();
    }
    
    // Mute/unmute with 'M' key
    if (e.key === 'm' || e.key === 'M') {
        if (currentRoom && !e.target.matches('input, textarea')) {
            toggleMicrophone();
        }
    }
});
