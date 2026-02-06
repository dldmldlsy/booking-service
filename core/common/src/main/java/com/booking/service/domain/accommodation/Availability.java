package com.booking.service.domain.accommodation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * 객실별 일자별 남은 수량을 나타내는 도메인 모델.
 */
@Entity
@Table(name = "availabilities")
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false)
    private int availableCount;

    protected Availability() {
    }

    public Availability(Room room, LocalDate date, int availableCount) {
        this.room = room;
        this.date = date;
        this.availableCount = availableCount;
    }

    public Long getId() {
        return id;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getDate() {
        return date;
    }

    public int getAvailableCount() {
        return availableCount;
    }

    public void decrement() {
        if (availableCount <= 0) {
            throw new IllegalStateException("No availability left");
        }
        availableCount--;
    }

    public void increment() {
        availableCount++;
    }
}
