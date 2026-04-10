package com.example.hotel_service.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreateHotelRequest {

    @NotBlank
    private String name;

    private String description;

    @NotBlank
    private String brand;

    @NotNull
    private AddressDTO address;

    @NotNull
    private ContactsDTO contacts;

    @NotNull
    private ArrivalTimeDTO arrivalTime;

}
