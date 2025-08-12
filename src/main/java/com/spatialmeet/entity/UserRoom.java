package com.spatialmeet.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_rooms")
public class UserRoom {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id")
    private Room room;
    
    // Spatial coordinates for 3D audio positioning
    private Double positionX = 0.0;
    private Double positionY = 0.0;
    private Double positionZ = 0.0;
    
    // Audio settings
    private boolean microphoneEnabled = true;
    private boolean speakerEnabled = true;
    private Double volume = 1.0;
    
    private boolean isConnected = false;
    
    private LocalDateTime joinedAt;
    
    private LocalDateTime lastPositionUpdate;
    
    public UserRoom() {}
    
    public UserRoom(User user, Room room) {
        this.user = user;
        this.room = room;
        this.joinedAt = LocalDateTime.now();
        this.lastPositionUpdate = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public Room getRoom() { return room; }
    public void setRoom(Room room) { this.room = room; }
    
    public Double getPositionX() { return positionX; }
    public void setPositionX(Double positionX) { this.positionX = positionX; }
    
    public Double getPositionY() { return positionY; }
    public void setPositionY(Double positionY) { this.positionY = positionY; }
    
    public Double getPositionZ() { return positionZ; }
    public void setPositionZ(Double positionZ) { this.positionZ = positionZ; }
    
    public boolean isMicrophoneEnabled() { return microphoneEnabled; }
    public void setMicrophoneEnabled(boolean microphoneEnabled) { this.microphoneEnabled = microphoneEnabled; }
    
    public boolean isSpeakerEnabled() { return speakerEnabled; }
    public void setSpeakerEnabled(boolean speakerEnabled) { this.speakerEnabled = speakerEnabled; }
    
    public Double getVolume() { return volume; }
    public void setVolume(Double volume) { this.volume = volume; }
    
    public boolean isConnected() { return isConnected; }
    public void setConnected(boolean connected) { isConnected = connected; }
    
    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
    
    public LocalDateTime getLastPositionUpdate() { return lastPositionUpdate; }
    public void setLastPositionUpdate(LocalDateTime lastPositionUpdate) { this.lastPositionUpdate = lastPositionUpdate; }
}
