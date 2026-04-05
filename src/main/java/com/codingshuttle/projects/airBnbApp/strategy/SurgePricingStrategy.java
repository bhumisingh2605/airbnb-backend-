package com.codingshuttle.projects.airBnbApp.strategy;

import com.codingshuttle.projects.airBnbApp.entity.Inventory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service("surgePricing")
//@RequiredArgsConstructor
public class SurgePricingStrategy implements PricingStrategy{

   // @Qualifier("urgencyPricing")
    private final PricingStrategy wrapped;

    public SurgePricingStrategy(@Qualifier("holidayPricing") PricingStrategy wrapped) {  // or whichever previous in chain
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
    BigDecimal price = wrapped.calculatePrice(inventory);
    return price.multiply(inventory.getSurgeFactor());
    }
}
