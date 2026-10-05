package com.example.room.repository;

import com.example.room.entity.RoomFacilityEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface RoomFacilityRepository extends JpaRepository<RoomFacilityEntity, Long> {

    List<RoomFacilityEntity> findByRoomId(Long roomId);
 
    @Modifying
    @Transactional
    void deleteByRoomId(Long roomId);

}
