package com.codingshuttle.projects.airBnbApp.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
public class Property {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private String location;
    private Double price;

    private String image;
    private String description;
    private Double rating;
    private Integer reviews;
    private Integer guests;
    private Integer bedrooms;
    private Integer bathrooms;

    @ElementCollection
    private List<String> amenities;

}