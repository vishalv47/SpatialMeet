package com.spatialmeet.controller;

import com.spatialmeet.entity.UserRoom;
import com.spatialmeet.repository.UserRoomRepository;
import com.spatialmeet.repository.RoomRepository;
import com.spatialmeet.repository.UserRepository;
import com.spatialmeet.entity.Room;
import com.spatialmeet.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Controller
public class WebRTCController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRoomRepository userRoomRepository;

    @MessageMapping("/room/{roomCode}/offer")
    public void handleOffer(@DestinationVariable String roomCode, 
                           @Payload Map<String, Object> offer,
                           SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        String targetUsername = (String) offer.get("target");
        
        // Send offer to specific user in the room
        messagingTemplate.convertAndSendToUser(
            targetUsername, 
            "/queue/room/" + roomCode + "/offer", 
            Map.of(
                "offer", offer.get("offer"),
                "sender", username
            )
        );
    }

    @MessageMapping("/room/{roomCode}/answer")
    public void handleAnswer(@DestinationVariable String roomCode,
                            @Payload Map<String, Object> answer,
                            SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        String targetUsername = (String) answer.get("target");
        
        // Send answer to specific user in the room
        messagingTemplate.convertAndSendToUser(
            targetUsername,
            "/queue/room/" + roomCode + "/answer",
            Map.of(
                "answer", answer.get("answer"),
                "sender", username
            )
        );
    }

    @MessageMapping("/room/{roomCode}/ice-candidate")
    public void handleIceCandidate(@DestinationVariable String roomCode,
                                  @Payload Map<String, Object> candidate,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        String targetUsername = (String) candidate.get("target");
        
        // Send ICE candidate to specific user in the room
        messagingTemplate.convertAndSendToUser(
            targetUsername,
            "/queue/room/" + roomCode + "/ice-candidate",
            Map.of(
                "candidate", candidate.get("candidate"),
                "sender", username
            )
        );
    }

    @MessageMapping("/room/{roomCode}/position")
    public void updatePosition(@DestinationVariable String roomCode,
                              @Payload Map<String, Object> positionData,
                              SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Room> roomOpt = roomRepository.findByRoomCode(roomCode);
        
        if (userOpt.isPresent() && roomOpt.isPresent()) {
            User user = userOpt.get();
            Room room = roomOpt.get();
            
            Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserAndRoom(user, room);
            if (userRoomOpt.isPresent()) {
                UserRoom userRoom = userRoomOpt.get();
                
                // Update spatial position
                userRoom.setPositionX((Double) positionData.get("x"));
                userRoom.setPositionY((Double) positionData.get("y"));
                userRoom.setPositionZ((Double) positionData.get("z"));
                userRoom.setLastPositionUpdate(LocalDateTime.now());
                
                userRoomRepository.save(userRoom);
                
                // Broadcast position update to all users in the room
                messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode + "/position",
                    Map.of(
                        "username", username,
                        "position", Map.of(
                            "x", userRoom.getPositionX(),
                            "y", userRoom.getPositionY(),
                            "z", userRoom.getPositionZ()
                        )
                    )
                );
            }
        }
    }

    @MessageMapping("/room/{roomCode}/audio-settings")
    public void updateAudioSettings(@DestinationVariable String roomCode,
                                   @Payload Map<String, Object> audioSettings,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        
        Optional<User> userOpt = userRepository.findByUsername(username);
        Optional<Room> roomOpt = roomRepository.findByRoomCode(roomCode);
        
        if (userOpt.isPresent() && roomOpt.isPresent()) {
            User user = userOpt.get();
            Room room = roomOpt.get();
            
            Optional<UserRoom> userRoomOpt = userRoomRepository.findByUserAndRoom(user, room);
            if (userRoomOpt.isPresent()) {
                UserRoom userRoom = userRoomOpt.get();
                
                // Update audio settings
                Boolean micEnabled = (Boolean) audioSettings.get("microphoneEnabled");
                Boolean speakerEnabled = (Boolean) audioSettings.get("speakerEnabled");
                Double volume = (Double) audioSettings.get("volume");
                
                if (micEnabled != null) userRoom.setMicrophoneEnabled(micEnabled);
                if (speakerEnabled != null) userRoom.setSpeakerEnabled(speakerEnabled);
                if (volume != null) userRoom.setVolume(volume);
                
                userRoomRepository.save(userRoom);
                
                // Broadcast audio settings update to all users in the room
                messagingTemplate.convertAndSend(
                    "/topic/room/" + roomCode + "/audio-settings",
                    Map.of(
                        "username", username,
                        "microphoneEnabled", userRoom.isMicrophoneEnabled(),
                        "speakerEnabled", userRoom.isSpeakerEnabled(),
                        "volume", userRoom.getVolume()
                    )
                );
            }
        }
    }

    @MessageMapping("/room/{roomCode}/join")
    public void handleJoinRoom(@DestinationVariable String roomCode,
                              SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        
        // Notify all users in the room that a new user joined
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/user-joined",
            Map.of("username", username)
        );
    }

    @MessageMapping("/room/{roomCode}/leave")
    public void handleLeaveRoom(@DestinationVariable String roomCode,
                               SimpMessageHeaderAccessor headerAccessor) {
        String username = headerAccessor.getUser().getName();
        
        // Notify all users in the room that a user left
        messagingTemplate.convertAndSend(
            "/topic/room/" + roomCode + "/user-left",
            Map.of("username", username)
        );
    }
}
