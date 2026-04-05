package com.codingshuttle.projects.airBnbApp.dto;

import java.math.BigDecimal;

public class HotelPriceDto {

    private final Long id;
    private final String name;
    private final Double price;

    // Primary constructor for MIN/AVG
    public HotelPriceDto(Long id, String name, Double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }

    // Backup constructor in case Hibernate passes BigDecimal
    public HotelPriceDto(Long id, String name, BigDecimal price) {
        this(id, name, price != null ? price.doubleValue() : null);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Double getPrice() {
        return price;
    }
}