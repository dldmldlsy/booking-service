package com.booking.service.domain.accommodation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalTime;

/**
 * 숙소의 개별 객실 정보를 나타내는 도메인 모델.
 */
@Entity
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "accommodation_id", nullable = false)
    private Accommodation accommodation;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private BigDecimal basePrice;

    @Column
    private String description;

    @Column
    private Integer maxHeadCount;

    @Column
    private LocalTime checkInTime;

    @Column
    private LocalTime checkOutTime;

    @Column
    private String imageUrl;

    protected Room() {
    }

    public Room(Accommodation accommodation, String name, int capacity, BigDecimal basePrice) {
        this.accommodation = accommodation;
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
    }

    public Room(Accommodation accommodation,
                String name,
                int capacity,
                BigDecimal basePrice,
                String description,
                Integer maxHeadCount,
                LocalTime checkInTime,
                LocalTime checkOutTime,
                String imageUrl) {
        this(accommodation, name, capacity, basePrice);
        this.description = description;
        this.maxHeadCount = maxHeadCount;
        this.checkInTime = checkInTime;
        this.checkOutTime = checkOutTime;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public BigDecimal getBasePrice() {
        return basePrice;
    }

    public String getDescription() {
        return description;
    }

    public Integer getMaxHeadCount() {
        return maxHeadCount;
    }

    public LocalTime getCheckInTime() {
        return checkInTime;
    }

    public LocalTime getCheckOutTime() {
        return checkOutTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
