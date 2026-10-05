package com.example.room.service;

import com.example.room.payload.req.RoomRequest;
import com.example.room.payload.res.RoomResponse;
import com.example.room.entity.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface RoomService {
    RoomResponse createRoom(RoomRequest request);

    RoomResponse updateRoom(Long id, RoomRequest request);

    RoomResponse uploadRoomImages(Long roomId, java.util.List<org.springframework.web.multipart.MultipartFile> files);

    RoomResponse getRoomById(Long roomId);

    Page<RoomResponse> getAllRooms(Pageable pageable);

    Page<RoomResponse> getRoomByStatus(RoomStatus status, Pageable pageable);

    void deleteRoom(Long roomId);

    RoomResponse changeRoomStatus(Long roomId, RoomStatus newStatus);

    RoomResponse getAvailableRoom(Long roomId);

    boolean isRoomAvailable(Long roomId);

    void markAsUnderMaintenance(Long roomId);

    void markAsOccupied(Long roomId);

    void markAsAvailable(Long roomId);

    RoomResponse occupyRoom(Long roomId);

    RoomResponse vacateRoom(Long roomId);

    Long countRooms();

Long countAvailableRooms();
}
