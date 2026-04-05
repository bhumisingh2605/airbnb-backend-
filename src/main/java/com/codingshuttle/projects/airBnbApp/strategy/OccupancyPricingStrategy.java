package com.codingshuttle.projects.airBnbApp.strategy;

import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
//@RequiredArgsConstructor
public class OccupancyPricingStrategy implements PricingStrategy{

   // @Qualifier("holidayPricing")
    private final PricingStrategy wrapped;

    public OccupancyPricingStrategy(@Qualifier("holidayPricing") PricingStrategy wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
       BigDecimal price = wrapped.calculatePrice(inventory);

       double occupancyRate =(double) inventory.getBookedCount() / inventory.getTotalCount();
       if(occupancyRate > 0.8) {
           price = price.multiply(BigDecimal.valueOf(1.2));
       }
       return price;
    }
}
