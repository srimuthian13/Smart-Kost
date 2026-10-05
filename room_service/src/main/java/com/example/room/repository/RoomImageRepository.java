package com.example.room.repository;
import com.example.room.entity.RoomImageEntity;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

public interface RoomImageRepository extends JpaRepository<RoomImageEntity, Long> {
    List<RoomImageEntity> findByRoomId(Long roomId);

    @Modifying
    @Transactional
        void deleteByRoomId(Long roomId);
}