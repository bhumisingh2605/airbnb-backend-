package com.codingshuttle.projects.airBnbApp.repository;

import com.codingshuttle.projects.airBnbApp.dto.HotelPriceDto;
import com.codingshuttle.projects.airBnbApp.entity.Hotel;
import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import com.codingshuttle.projects.airBnbApp.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    // Delete future inventories for a room
    void deleteByRoom(Room room);

    // Find hotels with available inventory for a date range and room count
    @Query("""
           SELECT DISTINCT i.hotel
           FROM Inventory i
           WHERE i.city = :city
             AND i.date BETWEEN :startDate AND :endDate
             AND i.closed = false
             AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
           GROUP BY i.hotel, i.room
           HAVING COUNT(i.date) = :dateCount
           """)
    Page<Hotel> findHotelsWithAvailableInventory(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );

    // Find and lock available inventory for a room in a date range
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.room.id = :roomId " +
            "AND i.date BETWEEN :startDate AND :endDate " +
            "AND i.closed = false " +
            "AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount")
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount
    );

    // Find inventory for a specific room in a date range
    @Query("SELECT i FROM Inventory i WHERE i.room.id = :roomId AND i.date BETWEEN :startDate AND :endDate")
    List<Inventory> findByRoomIdAndDateRange(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    // Find all inventory for a hotel in a date range
    List<Inventory> findByHotelAndDateBetween(Hotel hotel, LocalDate startDate, LocalDate endDate);

    // ✅ New method: Find all rooms with availability on a specific date
    @Query("SELECT i FROM Inventory i " +
            "WHERE i.date = :date " +
            "AND (i.totalCount - i.bookedCount - i.reservedCount) > 0 " +
            "AND i.closed = false")
    List<Inventory> findAvailableByDate(@Param("date") LocalDate date);

    // ✅ Fixed method
    @Query("""
            SELECT new com.codingshuttle.projects.airBnbApp.dto.HotelPriceDto(
                h.id,
                h.name,
                MIN(i.price)
            )
            FROM Inventory i
            JOIN i.hotel h
            GROUP BY h.id, h.name
            """)
    Page<HotelPriceDto> findAllHotels(Pageable pageable);
}