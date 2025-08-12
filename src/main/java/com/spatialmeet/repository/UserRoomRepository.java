package com.spatialmeet.repository;

import com.spatialmeet.entity.UserRoom;
import com.spatialmeet.entity.User;
import com.spatialmeet.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface UserRoomRepository extends JpaRepository<UserRoom, Long> {
    
    Optional<UserRoom> findByUserAndRoom(User user, Room room);
    
    List<UserRoom> findByRoom(Room room);
    
    List<UserRoom> findByUser(User user);
    
    List<UserRoom> findByRoomAndIsConnectedTrue(Room room);
    
    @Query("SELECT ur FROM UserRoom ur WHERE ur.room.id = :roomId AND ur.isConnected = true")
    List<UserRoom> findConnectedUsersInRoom(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(ur) FROM UserRoom ur WHERE ur.room.id = :roomId AND ur.isConnected = true")
    Long countConnectedUsersInRoom(@Param("roomId") Long roomId);
    
    void deleteByUserAndRoom(User user, Room room);
}
