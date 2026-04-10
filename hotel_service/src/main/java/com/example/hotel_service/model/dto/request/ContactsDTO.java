package com.example.hotel_service.model.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContactsDTO {

    @NotBlank
    private String phone;
    @Email
    @NotBlank
    private String email;

}
