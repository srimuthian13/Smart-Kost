package com.example.room.repository;

import com.example.room.entity.RoomEntity;
import com.example.room.entity.RoomStatus;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<RoomEntity, Long> {

    Optional<RoomEntity> findByRoomNumber(String roomNumber);
    boolean existsByRoomNumber(String roomNumber);

    List<RoomEntity> findByStatus(RoomStatus status);

    List<RoomEntity> findByRoomType(String roomType);

  List<RoomEntity> findByPriceLessThanEqual(BigDecimal price);

  List<RoomEntity> findByCapacityGreaterThanEqual(Integer capacity);

  long countByStatus(RoomStatus status);

  Page<RoomEntity> findByStatus(RoomStatus status, Pageable pageable);

   @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE r.status = 'AVAILABLE'
            """)
    List<RoomEntity> findAvailableRooms();

    @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE lower(r.roomNumber)
            LIKE lower(concat('%', :keyword, '%'))
            """)
    List<RoomEntity> searchByRoomNumber(
            @Param("keyword")
            String keyword
    );

    @Query("""
            SELECT r
            FROM RoomEntity r
            WHERE r.price BETWEEN :minPrice
            AND :maxPrice
            """)
    List<RoomEntity> findByPriceRange(
            @Param("minPrice")
            BigDecimal minPrice,

            @Param("maxPrice")
            BigDecimal maxPrice
    );






}
