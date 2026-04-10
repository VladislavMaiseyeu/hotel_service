package com.example.hotel_service.model.dto.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelShortDTO {

    private Long id;
    private String name;
    private String description;
    private String address;
    private String phone;

}
