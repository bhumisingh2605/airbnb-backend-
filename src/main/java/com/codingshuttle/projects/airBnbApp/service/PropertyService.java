package com.codingshuttle.projects.airBnbApp.service;

import com.codingshuttle.projects.airBnbApp.entity.Property;
import java.util.List;

public interface PropertyService {

    // ✅ GET ALL
    List<Property> getAllProperties();

    // ✅ CREATE
    Property createProperty(Property property);

    // ✅ UPDATE
    Property updateProperty(Long id, Property property);

    // ✅ DELETE
    void deleteProperty(Long id);
}