package com.booking.service.domain.accommodation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 숙박 상품(숙소) 정보를 나타내는 도메인 모델.
 */
@Entity
@Table(name = "accommodations")
public class Accommodation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    @Column(columnDefinition = "text")
    private String description;

    @Column
    private String phoneNumber;

    @Column
    private Double latitude;

    @Column
    private Double longitude;

    @Column
    private String region;

    @Column
    private String category;

    @Column
    private LocalTime checkInTime;

    @Column
    private LocalTime checkOutTime;

    @Column
    private Integer maximumCapacity;

    @Column(precision = 15, scale = 2)
    private BigDecimal lowestPrice;

    @Column
    private String imageUrl;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    protected Accommodation() {
    }

    public Accommodation(String name, String address, String description) {
        this.name = name;
        this.address = address;
        this.description = description == null ? "" : description;
    }

    public Accommodation(String name, String address, String description, String imageUrl) {
        this(name, address, description);
        this.imageUrl = imageUrl;
    }

    public Accommodation(String name,
                         String address,
                         String description,
                         String phoneNumber,
                         Double latitude,
                         Double longitude,
                         String region,
                         String category,
                         LocalTime checkInTime,
                         LocalTime checkOutTime,
                         Integer maximumCapacity,
                         BigDecimal lowestPrice,
                         String imageUrl,
                         LocalDateTime createdAt,
                         LocalDateTime updatedAt) {
        this(name, address, description);
        this.phoneNumber = phoneNumber;
        this.latitude = latitude;
        this.longitude = longitude;
        this.region = region;
        this.category = category;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.maximumCapacity = maximumCapacity;
        this.lowestPrice = lowestPrice;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getDescription() {
        return description;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public String getRegion() {
        return region;
    }

    public String getCategory() {
        return category;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public Integer getMaximumCapacity() {
        return maximumCapacity;
    }

    public BigDecimal getLowestPrice() {
        return lowestPrice;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
