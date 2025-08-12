package com.spatialmeet.controller;

import com.spatialmeet.dto.request.CreateRoomRequest;
import com.spatialmeet.dto.response.MessageResponse;
import com.spatialmeet.entity.Room;
import com.spatialmeet.entity.User;
import com.spatialmeet.entity.UserRoom;
import com.spatialmeet.repository.RoomRepository;
import com.spatialmeet.repository.UserRepository;
import com.spatialmeet.repository.UserRoomRepository;
import com.spatialmeet.security.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserRoomRepository userRoomRepository;

    @PostMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> createRoom(@Valid @RequestBody CreateRoomRequest createRoomRequest,
                                       Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Generate unique room code
        String roomCode;
        do {
            roomCode = generateRoomCode();
        } while (roomRepository.existsByRoomCode(roomCode));

        Room room = new Room();
        room.setName(createRoomRequest.getName());
        room.setDescription(createRoomRequest.getDescription());
        room.setRoomCode(roomCode);
        room.setPrivate(createRoomRequest.isPrivate());
        room.setMaxParticipants(createRoomRequest.getMaxParticipants());
        room.setCreatedBy(user);

        Room savedRoom = roomRepository.save(room);

        // Add creator to the room
        UserRoom userRoom = new UserRoom(user, savedRoom);
        userRoomRepository.save(userRoom);

        return ResponseEntity.ok(savedRoom);
    }

    @GetMapping
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Room>> getPublicRooms() {
        List<Room> publicRooms = roomRepository.findByIsPrivateFalse();
        return ResponseEntity.ok(publicRooms);
    }

    @GetMapping("/public")
    public ResponseEntity<List<Room>> getPublicRoomsForGuests() {
        // Public endpoint for guest users to see available public rooms
        List<Room> publicRooms = roomRepository.findByIsPrivateFalse();
        return ResponseEntity.ok(publicRooms);
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<Room>> getMyRooms(Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Room> myRooms = roomRepository.findByCreatedBy(user);
        return ResponseEntity.ok(myRooms);
    }

    @GetMapping("/{roomCode}")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> getRoomByCode(@PathVariable String roomCode) {
        Optional<Room> room = roomRepository.findByRoomCode(roomCode);
        if (room.isPresent()) {
            return ResponseEntity.ok(room.get());
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/{roomCode}/join")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> joinRoom(@PathVariable String roomCode,
                                     Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        // Check if room is full
        Long currentParticipants = userRoomRepository.countConnectedUsersInRoom(room.getId());
        if (currentParticipants >= room.getMaxParticipants()) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Room is full"));
        }

        // Check if user is already in the room
        Optional<UserRoom> existingUserRoom = userRoomRepository.findByUserAndRoom(user, room);
        UserRoom userRoom;
        if (existingUserRoom.isPresent()) {
            userRoom = existingUserRoom.get();
            userRoom.setConnected(true);
        } else {
            userRoom = new UserRoom(user, room);
            userRoom.setConnected(true);
        }

        userRoomRepository.save(userRoom);
        return ResponseEntity.ok(new MessageResponse("Successfully joined room"));
    }

    @PostMapping("/{roomCode}/leave")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<?> leaveRoom(@PathVariable String roomCode,
                                      Authentication authentication) {
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        Optional<UserRoom> userRoom = userRoomRepository.findByUserAndRoom(user, room);
        if (userRoom.isPresent()) {
            userRoom.get().setConnected(false);
            userRoomRepository.save(userRoom.get());
        }

        return ResponseEntity.ok(new MessageResponse("Successfully left room"));
    }

    @GetMapping("/{roomCode}/participants")
    @PreAuthorize("hasRole('USER') or hasRole('MODERATOR') or hasRole('ADMIN')")
    public ResponseEntity<List<UserRoom>> getRoomParticipants(@PathVariable String roomCode) {
        Room room = roomRepository.findByRoomCode(roomCode)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        List<UserRoom> participants = userRoomRepository.findConnectedUsersInRoom(room.getId());
        return ResponseEntity.ok(participants);
    }

    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
