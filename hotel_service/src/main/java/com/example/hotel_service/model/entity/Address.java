package com.example.hotel_service.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Address {

    @Column(name = "house_number")
    private Integer houseNumber;

    private String street;
    private String city;
    private String country;

    @Column(name = "post_code")
    private String postCode;

}
