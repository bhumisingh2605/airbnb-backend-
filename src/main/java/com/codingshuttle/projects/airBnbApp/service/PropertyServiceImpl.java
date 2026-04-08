package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.Property;
import com.codingshuttle.projects.airBnbApp.repository.PropertyRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final PropertyRepository propertyRepository;

    // ✅ GET ALL PROPERTIES
    @Override
    public List<Property> getAllProperties() {
        return propertyRepository.findAll();
    }

    // ✅ CREATE PROPERTY
    @Override
    public Property createProperty(Property property) {
        return propertyRepository.save(property);
    }

    // ✅ UPDATE PROPERTY
    @Override
    public Property updateProperty(Long id, Property updatedProperty) {

        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Property not found"));

        existing.setName(updatedProperty.getName());
        existing.setLocation(updatedProperty.getLocation());
        existing.setPrice(updatedProperty.getPrice());
        existing.setImage(updatedProperty.getImage());
        existing.setDescription(updatedProperty.getDescription());
        existing.setRating(updatedProperty.getRating());
        existing.setReviews(updatedProperty.getReviews());
        existing.setGuests(updatedProperty.getGuests());
        existing.setBedrooms(updatedProperty.getBedrooms());
        existing.setBathrooms(updatedProperty.getBathrooms());
        existing.setAmenities(updatedProperty.getAmenities());

        return propertyRepository.save(existing);
    }

    // ✅ DELETE PROPERTY
    @Override
    public void deleteProperty(Long id) {
        propertyRepository.deleteById(id);



    }
}