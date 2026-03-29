package com.booking.service.domain.accommodation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "images")
public class Image {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String url;

    @Column
    private Integer sortOrder;

    @ManyToOne
    @JoinColumn(name = "accommodation_id")
    private Accommodation accommodation;

    @ManyToOne
    @JoinColumn(name = "room_id")
    private Room room;

    protected Image() {
    }

    public Image(Accommodation accommodation, String url, Integer sortOrder) {
        this.accommodation = accommodation;
        this.url = url;
        this.sortOrder = sortOrder;
    }

    public Image(Room room, String url, Integer sortOrder) {
        this.room = room;
        this.url = url;
        this.sortOrder = sortOrder;
    }

    public Image(Accommodation accommodation, Room room, String url, Integer sortOrder) {
        this.accommodation = accommodation;
        this.room = room;
        this.url = url;
        this.sortOrder = sortOrder;
    }

    public Long getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public Integer getSortOrder() {
        return sortOrder;
    }

    public Accommodation getAccommodation() {
        return accommodation;
    }

    public Room getRoom() {
        return room;
    }
}
