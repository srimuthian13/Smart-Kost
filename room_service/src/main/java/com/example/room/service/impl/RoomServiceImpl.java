package com.example.room.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.room.entity.RoomEntity;
import com.example.room.entity.RoomFacilityEntity;
import com.example.room.entity.RoomImageEntity;
import com.example.room.payload.req.RoomRequest;
import com.example.room.payload.res.RoomResponse;
import com.example.room.repository.RoomFacilityRepository;
import com.example.room.repository.RoomRepository;
import com.example.room.repository.RoomImageRepository;
import com.example.room.service.RoomService;
import com.example.room.service.FileStorageService;
import com.example.room.entity.RoomStatus;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.room.exception.ResourceNotFoundException;
import com.example.room.exception.BadRequestException;

@Service
@Transactional
@RequiredArgsConstructor
public class RoomServiceImpl implements RoomService {
    private final RoomRepository roomRepository;
    private final RoomFacilityRepository roomFacilityRepository;
    private final RoomImageRepository roomImageRepository;
    private final FileStorageService fileStorageService;

    @Override
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new BadRequestException("Room number already exists");
        }

        RoomEntity room = RoomEntity.builder()
                .roomNumber(request.roomNumber())
                .roomType(request.roomType())
                .price(request.price())
                .capacity(request.capacity())
                .status(RoomStatus.AVAILABLE)
                .description(request.description())
                .build();

        RoomEntity savedRoom = roomRepository.save(room);

        saveFacilities(savedRoom, request.facilities());
        saveImages(savedRoom, request.images());

        RoomEntity roomWithRelations = roomRepository.findById(savedRoom.getId())
                .orElseThrow();

        return mapToResponse(roomWithRelations);
    }

    @Override
    public RoomResponse updateRoom(Long roomId, RoomRequest request) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (!room.getRoomNumber().equals(request.roomNumber()) && roomRepository.existsByRoomNumber(request.roomNumber())) {
            throw new BadRequestException("Room number already exists");
        }

        // Update room details
        room.setRoomNumber(request.roomNumber());
        room.setRoomType(request.roomType());
        room.setPrice(request.price());
        room.setCapacity(request.capacity());
        room.setDescription(request.description());

        // Update facilities and images using orphanRemoval
        room.getFacilities().clear();
        room.getImages().clear();
        saveFacilities(room, request.facilities());
        saveImages(room, request.images());

        RoomEntity updated = roomRepository.save(room);
        return mapToResponse(updated);
    }

    @Override
    public RoomResponse uploadRoomImages(Long roomId, java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        if (files != null && !files.isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile file : files) {
                String imageUrl = fileStorageService.storeFile(file);
                RoomImageEntity roomImage = RoomImageEntity.builder()
                        .imageUrl(imageUrl)
                        .room(room)
                        .build();
                room.getImages().add(roomImage);
            }
            roomRepository.save(room);
        }
        return mapToResponse(room);
    }

    @Override

    @Transactional
    public RoomResponse getRoomById(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getAllRooms(Pageable pageable) {
        return roomRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomResponse> getRoomByStatus(RoomStatus status, Pageable pageable) {
        return roomRepository.findByStatus(status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public void deleteRoom(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        roomRepository.delete(room);
    }

    @Override
    public RoomResponse changeRoomStatus(Long roomId, RoomStatus status) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setStatus(status);
        roomRepository.save(room);

        return mapToResponse(room);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        return room.getStatus() == RoomStatus.AVAILABLE;
    }



    @Override
    public void markAsUnderMaintenance(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setStatus(RoomStatus.MAINTENANCE);
        roomRepository.save(room);
    }

    @Override
    public void markAsOccupied(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);
    }

    @Override
    public void markAsAvailable(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getAvailableRoom(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BadRequestException("Room is not available");
        }
        return mapToResponse(room);
    }

    @Override
    public RoomResponse occupyRoom(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (room.getStatus() != RoomStatus.AVAILABLE) {
            throw new BadRequestException("Room is not available for occupation");
        }
        room.setStatus(RoomStatus.OCCUPIED);
        roomRepository.save(room);
        return mapToResponse(room);
    }

    @Override
    public RoomResponse vacateRoom(Long roomId) {
        RoomEntity room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));
        if (room.getStatus() != RoomStatus.OCCUPIED) {
            throw new BadRequestException("Room is not currently occupied");
        }
        room.setStatus(RoomStatus.AVAILABLE);
        roomRepository.save(room);
        return mapToResponse(room);
    }

    @Override
public Long countRooms() {
    return roomRepository.count();
}

@Override
public Long countAvailableRooms() {
    return roomRepository.countByStatus(RoomStatus.AVAILABLE);
}
    private void saveFacilities(RoomEntity room, List<String> facilities) {
        if (facilities == null || facilities.isEmpty()) {
            return;
        }

        facilities.forEach(facility -> {
            RoomFacilityEntity roomFacility = RoomFacilityEntity.builder()
                    .facilityName(facility)
                    .room(room)
                    .build();
            room.getFacilities().add(roomFacility);
        });
    }

    private void saveImages(RoomEntity room, List<String> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        images.forEach(imageUrl -> {
            RoomImageEntity roomImage = RoomImageEntity.builder()
                    .imageUrl(imageUrl)
                    .room(room)
                    .build();
            room.getImages().add(roomImage);
        });
    }

    private RoomResponse mapToResponse(RoomEntity room) {
        List<String> facilities = room.getFacilities() == null
                ? List.of()
                : room.getFacilities()
                        .stream()
                        .map(RoomFacilityEntity::getFacilityName)
                        .toList();

        List<String> images = room.getImages() == null
                ? List.of()
                : room.getImages()
                        .stream()
                        .map(RoomImageEntity::getImageUrl)
                        .toList();

        return new RoomResponse(
                room.getId(),
                room.getRoomNumber(),
                room.getRoomType(),
                room.getPrice(),
                room.getCapacity(),
                room.getStatus(),
                room.getDescription(),
                facilities,
                images,
                room.getCreatedAt(),
                room.getUpdatedAt());

    }
}
