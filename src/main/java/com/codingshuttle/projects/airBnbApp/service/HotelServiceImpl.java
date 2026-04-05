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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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

    @Override
    @Transactional
    public HotelDto createNewHotel(HotelDto hotelDto) {
        log.info("Creating new hotel: {}", hotelDto.getName());

        Hotel hotel = modelMapper.map(hotelDto, Hotel.class);
        hotel.setActive(false);

        User currentUser = getCurrentUser();
        hotel.setOwner(currentUser);

        hotel = hotelRepository.save(hotel);

        log.info("Hotel created successfully - ID: {}", hotel.getId());
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    public HotelDto getHotelById(Long id) {
        log.info("Fetching hotel ID: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));

        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("You do not own hotel with ID: " + id);
        }

        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public HotelDto updateHotelById(Long id, HotelDto hotelDto) {
        log.info("Updating hotel ID: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));

        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("You do not own hotel with ID: " + id);
        }

        // Update fields (modelMapper will overwrite existing fields)
        modelMapper.map(hotelDto, hotel);
        hotel.setId(id); // ensure ID is not changed

        hotel = hotelRepository.save(hotel);

        log.info("Hotel updated successfully - ID: {}", id);
        return modelMapper.map(hotel, HotelDto.class);
    }

    @Override
    @Transactional
    public void deleteHotelById(Long id) {
        log.info("Deleting hotel ID: {}", id);

        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + id));

        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("You do not own hotel with ID: " + id);
        }

        // Clean up rooms and inventory first
        for (Room room : hotel.getRooms()) {
            inventoryService.deleteFutureInventories(room);
            roomRepository.deleteById(room.getId());
        }

        hotelRepository.deleteById(id);
        log.info("Hotel deleted successfully - ID: {}", id);
    }

    @Override
    @Transactional
    public void activateHotel(Long hotelId) {
        log.info("Activating hotel ID: {}", hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        User currentUser = getCurrentUser();

        if (!currentUser.getId().equals(hotel.getOwner().getId())) {
            throw new UnAuthorisedException("You do not own hotel with ID: " + hotelId);
        }

        if (hotel.getActive()) {
            log.warn("Hotel {} is already active", hotelId);
            return;
        }

        hotel.setActive(true);
        hotelRepository.save(hotel);

        // Initialize inventory for rooms (only once)
        for (Room room : hotel.getRooms()) {
            inventoryService.initializeRoomForAYear(room);
        }

        log.info("Hotel activated successfully - ID: {}", hotelId);
    }

    @Override
    public HotelInfoDto getHotelInfoById(Long hotelId) {
        log.info("Fetching hotel info for ID: {}", hotelId);

        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with ID: " + hotelId));

        // No ownership check here — assuming public info endpoint
        // If you want to restrict, add the check like in other methods

        List<RoomDto> rooms = hotel.getRooms().stream()
                .map(room -> modelMapper.map(room, RoomDto.class))
                .collect(Collectors.toList());

        return new HotelInfoDto(modelMapper.map(hotel, HotelDto.class), rooms);
    }

    // ────────────────────────────────────────────────
    //  Helper method - safe user extraction
    // ────────────────────────────────────────────────

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
            throw new UnAuthorisedException("No authenticated user found");
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        log.error("Principal is not User type. Found: {}", principal.getClass().getName());
        throw new IllegalStateException("Authentication principal is not the expected User entity");
    }
}