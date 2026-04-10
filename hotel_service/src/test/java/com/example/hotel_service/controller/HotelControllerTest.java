package com.example.hotel_service.controller;

import com.example.hotel_service.advice.CommonControllerAdvice;
import com.example.hotel_service.model.dto.request.AddressDTO;
import com.example.hotel_service.model.dto.request.ArrivalTimeDTO;
import com.example.hotel_service.model.dto.request.ContactsDTO;
import com.example.hotel_service.model.dto.request.CreateHotelRequest;
import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import com.example.hotel_service.model.exception.HotelAlreadyExistsException;
import com.example.hotel_service.model.exception.NotFoundException;
import com.example.hotel_service.service.HotelService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HotelController.class)
@Import(CommonControllerAdvice.class)
@TestPropertySource(properties = {
        "end.point.context-path=/property-view",
        "end.point.hotels=/hotels",
        "end.point.id=/hotels/{id}",
        "end.point.search=/search",
        "end.point.amenities=/hotels/{id}/amenities",
        "end.point.histogram=/histogram/{param}"
})
class HotelControllerTest {

    private static final String BASE_PATH = "/property-view";
    private static final String HOTELS_PATH = BASE_PATH + "/hotels";
    private static final String SEARCH_PATH = BASE_PATH + "/search";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private HotelService hotelService;

    @Test
    void createHotelShouldReturnCreatedWhenRequestIsValid() throws Exception {
        CreateHotelRequest request = buildCreateHotelRequest();

        HotelShortDTO response = HotelShortDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .build();

        when(hotelService.createHotel(any(CreateHotelRequest.class))).thenReturn(response);

        mockMvc.perform(post(HOTELS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hilton Minsk"));
    }

    @Test
    void createHotelShouldReturnConflictWhenHotelAlreadyExists() throws Exception {
        CreateHotelRequest request = buildCreateHotelRequest();

        when(hotelService.createHotel(any(CreateHotelRequest.class)))
                .thenThrow(new HotelAlreadyExistsException("Hotel with the same name and address already exists"));

        mockMvc.perform(post(HOTELS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Hotel with the same name and address already exists"));
    }

    @Test
    void createHotelShouldReturnBadRequestWhenRequestIsInvalid() throws Exception {
        String invalidJson = """
                {
                  "name": "",
                  "description": "Modern hotel",
                  "brand": "Hilton",
                  "address": {
                    "houseNumber": 10,
                    "street": "Pobediteley Avenue",
                    "city": "Minsk",
                    "country": "Belarus",
                    "postCode": "220004"
                  },
                  "contacts": {
                    "phone": "+375291234567",
                    "email": "info@hilton.com"
                  },
                  "arrivalTime": {
                    "checkIn": "14:00",
                    "checkOut": "12:00"
                  }
                }
                """;

        mockMvc.perform(post(HOTELS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    void getHotelByIdShouldReturnOkWhenHotelExists() throws Exception {
        HotelDetailsDTO response = HotelDetailsDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .brand("Hilton")
                .build();

        when(hotelService.getHotelById(1L)).thenReturn(response);

        mockMvc.perform(get(HOTELS_PATH + "/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Hilton Minsk"))
                .andExpect(jsonPath("$.brand").value("Hilton"));
    }

    @Test
    void getHotelByIdShouldReturnNotFoundWhenHotelDoesNotExist() throws Exception {
        when(hotelService.getHotelById(999L))
                .thenThrow(new NotFoundException("Hotel not found"));

        mockMvc.perform(get(HOTELS_PATH + "/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Hotel not found"));
    }

    @Test
    void getAllHotelsShouldReturnOkAndList() throws Exception {
        List<HotelShortDTO> response = List.of(
                HotelShortDTO.builder()
                        .id(1L)
                        .name("Hilton Minsk")
                        .description("Modern hotel")
                        .build(),
                HotelShortDTO.builder()
                        .id(2L)
                        .name("Marriott Warsaw")
                        .description("Business hotel")
                        .build()
        );

        when(hotelService.getAllHotels()).thenReturn(response);

        mockMvc.perform(get(HOTELS_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hilton Minsk"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Marriott Warsaw"));
    }

    @Test
    void searchHotelsShouldReturnOkAndFilteredList() throws Exception {
        List<HotelShortDTO> response = List.of(
                HotelShortDTO.builder()
                        .id(1L)
                        .name("Hilton Minsk")
                        .description("Modern hotel")
                        .build()
        );

        when(hotelService.searchHotels(
                eq("Hilton"),
                eq("Hilton"),
                eq("Minsk"),
                eq("Belarus"),
                eq(List.of("WiFi", "Spa"))
        )).thenReturn(response);

        mockMvc.perform(get(SEARCH_PATH)
                        .param("name", "Hilton")
                        .param("brand", "Hilton")
                        .param("city", "Minsk")
                        .param("country", "Belarus")
                        .param("amenities", "WiFi", "Spa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Hilton Minsk"));
    }

    private CreateHotelRequest buildCreateHotelRequest() {
        return CreateHotelRequest.builder()
                .name("Hilton Minsk")
                .description("Modern hotel")
                .brand("Hilton")
                .address(AddressDTO.builder()
                        .houseNumber(10)
                        .street("Pobediteley Avenue")
                        .city("Minsk")
                        .country("Belarus")
                        .postCode("220004")
                        .build())
                .contacts(ContactsDTO.builder()
                        .phone("+375291234567")
                        .email("info@hilton.com")
                        .build())
                .arrivalTime(ArrivalTimeDTO.builder()
                        .checkIn(LocalTime.of(14, 0))
                        .checkOut(LocalTime.of(12, 0))
                        .build())
                .build();
    }
}