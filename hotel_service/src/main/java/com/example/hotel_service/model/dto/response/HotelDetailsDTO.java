package com.example.hotel_service.model.dto.response;

import com.example.hotel_service.model.dto.request.AddressDTO;
import com.example.hotel_service.model.dto.request.ArrivalTimeDTO;
import com.example.hotel_service.model.dto.request.ContactsDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelDetailsDTO {

    private Long id;
    private String name;
    private String description;
    private String brand;
    private AddressDTO address;
    private ContactsDTO contacts;
    private ArrivalTimeDTO arrivalTime;
    private List<String> amenities;

}
