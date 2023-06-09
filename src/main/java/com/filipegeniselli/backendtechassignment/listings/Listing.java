package com.filipegeniselli.backendtechassignment.listings;

import com.filipegeniselli.backendtechassignment.dealer.Dealer;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table
public class Listing {

    @Id
    private UUID id;
    @ManyToOne
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;
    private String vehicle;
    private VehicleCondition condition;
    private BigDecimal price;
    private String color;
    private VehicleTransmission transmission;
    private Integer mileage;
    private VehicleFuelType fuelType;
    private ListingStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private LocalDateTime removedAt;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Dealer getDealer() {
        return dealer;
    }

    public void setDealer(Dealer dealer) {
        this.dealer = dealer;
    }

    public String getVehicle() {
        return vehicle;
    }

    public void setVehicle(String vehicle) {
        this.vehicle = vehicle;
    }

    public VehicleCondition getCondition() {
        return condition;
    }

    public void setCondition(VehicleCondition condition) {
        this.condition = condition;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public VehicleTransmission getTransmission() {
        return transmission;
    }

    public void setTransmission(VehicleTransmission transmission) {
        this.transmission = transmission;
    }

    public Integer getMileage() {
        return mileage;
    }

    public void setMileage(Integer mileage) {
        this.mileage = mileage;
    }

    public VehicleFuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(VehicleFuelType fuelType) {
        this.fuelType = fuelType;
    }

    public ListingStatus getStatus() {
        return status;
    }

    public void setStatus(ListingStatus status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getRemovedAt() {
        return removedAt;
    }

    public void setRemovedAt(LocalDateTime removedAt) {
        this.removedAt = removedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Listing listing = (Listing) o;
        return Objects.equals(dealer, listing.dealer) &&
                Objects.equals(vehicle, listing.vehicle) &&
                condition == listing.condition &&
                Objects.equals(mileage, listing.mileage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dealer, vehicle, condition, mileage);
    }

    public static final class ListingBuilder {
        private UUID id;
        private Dealer dealer;
        private String vehicle;
        private VehicleCondition condition;
        private BigDecimal price;
        private String color;
        private VehicleTransmission transmission;
        private Integer mileage;
        private VehicleFuelType fuelType;
        private ListingStatus status;
        private LocalDateTime createdAt;
        private LocalDateTime publishedAt;
        private LocalDateTime removedAt;

        private ListingBuilder() {
        }

        public static ListingBuilder aListing() {
            return new ListingBuilder();
        }

        public ListingBuilder id(UUID id) {
            this.id = id;
            return this;
        }

        public ListingBuilder dealer(Dealer dealer) {
            this.dealer = dealer;
            return this;
        }

        public ListingBuilder vehicle(String vehicle) {
            this.vehicle = vehicle;
            return this;
        }

        public ListingBuilder condition(VehicleCondition condition) {
            this.condition = condition;
            return this;
        }

        public ListingBuilder price(BigDecimal price) {
            this.price = price;
            return this;
        }

        public ListingBuilder color(String color) {
            this.color = color;
            return this;
        }

        public ListingBuilder transmission(VehicleTransmission transmission) {
            this.transmission = transmission;
            return this;
        }

        public ListingBuilder mileage(Integer mileage) {
            this.mileage = mileage;
            return this;
        }

        public ListingBuilder fuelType(VehicleFuelType fuelType) {
            this.fuelType = fuelType;
            return this;
        }

        public ListingBuilder status(ListingStatus status) {
            this.status = status;
            return this;
        }

        public ListingBuilder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ListingBuilder publishedAt(LocalDateTime publishedAt) {
            this.publishedAt = publishedAt;
            return this;
        }

        public ListingBuilder removedAt(LocalDateTime removedAt) {
            this.removedAt = removedAt;
            return this;
        }

        public Listing build() {
            Listing listing = new Listing();
            listing.setId(id);
            listing.setDealer(dealer);
            listing.setVehicle(vehicle);
            listing.setCondition(condition);
            listing.setPrice(price);
            listing.setColor(color);
            listing.setTransmission(transmission);
            listing.setMileage(mileage);
            listing.setFuelType(fuelType);
            listing.setStatus(status);
            listing.setCreatedAt(createdAt);
            listing.setPublishedAt(publishedAt);
            listing.setRemovedAt(removedAt);
            return listing;
        }
    }
}
