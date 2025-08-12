package com.spatialmeet.repository;

import com.spatialmeet.entity.Room;
import com.spatialmeet.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    Optional<Room> findByRoomCode(String roomCode);
    
    List<Room> findByCreatedBy(User createdBy);
    
    List<Room> findByIsPrivateFalse();
    
    Boolean existsByRoomCode(String roomCode);
    
    List<Room> findByNameContainingIgnoreCase(String name);
}
