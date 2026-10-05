package com.example.room.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.room.payload.req.RoomRequest;
import com.example.room.payload.res.RoomResponse;
import com.example.room.service.RoomService;
import com.example.room.entity.RoomStatus;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PutMapping;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));

    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @GetMapping("/count")
public ResponseEntity<Long> countRooms() {

    return ResponseEntity.ok(
            roomService.countRooms());

}

@GetMapping("/available/count")
public ResponseEntity<Long> countAvailableRooms() {

    return ResponseEntity.ok(
            roomService.countAvailableRooms());

}


    @GetMapping
    public ResponseEntity<Page<RoomResponse>> getAllRooms(Pageable pageable) {

        return ResponseEntity.ok(
                roomService.getAllRooms(pageable));
    }

    @GetMapping("/status")
    public ResponseEntity<Page<RoomResponse>> getRoomByStatus(
            @RequestParam RoomStatus status,
            Pageable pageable) {

        return ResponseEntity.ok(
                roomService.getRoomByStatus(status, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest request) {

        return ResponseEntity.ok(roomService.updateRoom(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> changeRoomStatus(@PathVariable Long id, @RequestParam RoomStatus status) {
        return ResponseEntity.ok(roomService.changeRoomStatus(id, status));
    }

    @PostMapping("/{id}/occupy")
    public RoomResponse occupyRoom(@PathVariable Long id) {
        return roomService.occupyRoom(id);
    }

    @PostMapping("/{id}/vacate")
    public RoomResponse vacateRoom(@PathVariable Long id) {
        return roomService.vacateRoom(id);
    }

    @PostMapping(value = "/{id}/images", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RoomResponse> uploadRoomImages(
            @PathVariable Long id,
            @RequestParam("images") java.util.List<org.springframework.web.multipart.MultipartFile> files) {
        return ResponseEntity.ok(roomService.uploadRoomImages(id, files));
    }

}
