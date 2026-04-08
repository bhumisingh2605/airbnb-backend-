package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.dto.HotelDto;
import com.codingshuttle.projects.airBnbApp.dto.HotelInfoDto;
import com.codingshuttle.projects.airBnbApp.dto.RoomDto;
import com.codingshuttle.projects.airBnbApp.entity.Hotel;
import com.codingshuttle.projects.airBnbApp.entity.Room;
import com.codingshuttle.projects.airBnbApp.entity.User;
import com.codingshuttle.projects.airBnbApp.exception.ResourceNotFoundException;
import com.codingshuttle.projects.airBnbApp.exception.UnAuthorisedException;
import com.codingshuttle.projects.airBnbApp.repository.HotelRepository;
import com.codingshuttle.projects.airBnbApp.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final ModelMapper modelMapper;
    private final InventoryService inventoryService;
    private final RoomRepository roomRepository;

    // ✅ CREATE HOTEL (FIXED)
    @Override
    @Transactional
    public HotelDto createNewHotel(HotelDto hotelDto) {

        log.info("Creating new hotel: {}", hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        // 🔥 TEMP FIX (SET DUMMY USER)
        User user = new User();
        user.setId(2L); // ⚠️ Make sure this user exists in DB

        System.out.println("🔥 DEBUG: Using dummy user ID = 2");
        hotel.setOwner(user);

        hotel = hotelRepository.save(hotel);

        log.info("Hotel created successfully - ID: {}", hotel.getId());

        return modelMapper.map(hotel, HotelDto.class);
    }

    // ✅ GET HOTEL
    @Override
    public HotelDto getHotelById(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        return modelMapper.map(hotel, HotelDto.class);
    }

    // ✅ UPDATE HOTEL
    @Override
    @Transactional
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        modelMapper.map(hotelDto, hotel);
        hotel.setId(id);

        hotel = hotelRepository.save(hotel);

        return modelMapper.map(hotel, HotelDto.class);
    }

    // ✅ DELETE HOTEL
    @Override
    @Transactional
    public void deleteHotelById(Long id) {

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        for (Room room : hotel.getRooms()) {
            inventoryService.deleteFutureInventories(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(id);
    }

    // ✅ ACTIVATE HOTEL
    @Override
    @Transactional
    public void activateHotel(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        if (hotel.getActive()) {
            return;
        }

        hotel.setActive(true);
        hotelRepository.save(hotel);

        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }
    }

    // ✅ PUBLIC HOTEL INFO
    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found"));

        List<RoomDto> rooms = hotel.getRooms().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    @Override
    public List<HotelDto> getAllHotels() {
        return List.of();
    }
}