package bg.sofia.uni.fmi.mjt.itinerary;

import bg.sofia.uni.fmi.mjt.itinerary.vehicle.VehicleType;

import java.math.BigDecimal;

public record Journey(VehicleType vehicleType, City from, City to, BigDecimal price) {

    boolean hasCity(City city){
        return from.equals(city) || to.equals(city);
    }

    public BigDecimal computePrice() {
        return price.multiply(BigDecimal.valueOf(100).add(vehicleType.getGreenTax())).divide(BigDecimal.valueOf(100), BigDecimal.ROUND_HALF_UP);
    }

}