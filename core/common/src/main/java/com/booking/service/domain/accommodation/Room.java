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

    protected Room() {
    }

    public Room(Accommodation accommodation, String name, int capacity, BigDecimal basePrice) {
        this.accommodation = accommodation;
        this.name = name;
        this.capacity = capacity;
        this.basePrice = basePrice;
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
}
