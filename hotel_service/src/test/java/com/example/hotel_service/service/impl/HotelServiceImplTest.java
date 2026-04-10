package com.example.hotel_service.service.impl;

import com.example.hotel_service.mapper.HotelMapper;
import com.example.hotel_service.model.dto.request.CreateHotelRequest;
import com.example.hotel_service.model.dto.request.AddressDTO;
import com.example.hotel_service.model.dto.request.ArrivalTimeDTO;
import com.example.hotel_service.model.dto.request.ContactsDTO;
import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import com.example.hotel_service.model.entity.Address;
import com.example.hotel_service.model.entity.ArrivalTime;
import com.example.hotel_service.model.entity.Contacts;
import com.example.hotel_service.model.entity.Hotel;
import com.example.hotel_service.model.exception.HotelAlreadyExistsException;
import com.example.hotel_service.model.exception.NotFoundException;
import com.example.hotel_service.repository.HotelRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotelServiceImplTest {

    @Mock
    private HotelRepository hotelRepository;

    @Mock
    private HotelMapper hotelMapper;

    @InjectMocks
    private HotelServiceImpl hotelService;

    @Test
    void createHotelShouldSaveHotelAndReturnShortDto() {
        CreateHotelRequest request = buildCreateHotelRequest();

        Hotel savedHotel = buildHotel(1L);
        HotelShortDTO expectedDto = HotelShortDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .build();

        when(hotelRepository.existsByNameIgnoreCaseAndAddress_HouseNumberAndAddress_StreetIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_PostCode(
                eq("Hilton Minsk"),
                eq(10),
                eq("Pobediteley Avenue"),
                eq("Minsk"),
                eq("220004")
        )).thenReturn(false);

        when(hotelRepository.save(any(Hotel.class))).thenReturn(savedHotel);
        when(hotelMapper.toShortDto(savedHotel)).thenReturn(expectedDto);

        HotelShortDTO result = hotelService.createHotel(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Hilton Minsk", result.getName());

        ArgumentCaptor<Hotel> captor = ArgumentCaptor.forClass(Hotel.class);
        verify(hotelRepository).save(captor.capture());

        Hotel hotelToSave = captor.getValue();
        assertEquals("Hilton Minsk", hotelToSave.getName());
        assertEquals("Hilton", hotelToSave.getBrand());
        assertEquals("Minsk", hotelToSave.getAddress().getCity());
        assertEquals("220004", hotelToSave.getAddress().getPostCode());
        assertEquals(LocalTime.of(14, 0), hotelToSave.getArrivalTime().getCheckIn());
        assertEquals(LocalTime.of(12, 0), hotelToSave.getArrivalTime().getCheckOut());
    }

    @Test
    void createHotelShouldThrowExceptionWhenHotelAlreadyExists() {
        CreateHotelRequest request = buildCreateHotelRequest();

        when(hotelRepository.existsByNameIgnoreCaseAndAddress_HouseNumberAndAddress_StreetIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_PostCode(
                eq("Hilton Minsk"),
                eq(10),
                eq("Pobediteley Avenue"),
                eq("Minsk"),
                eq("220004")
        )).thenReturn(true);

        assertThrows(HotelAlreadyExistsException.class, () -> hotelService.createHotel(request));

        verify(hotelRepository, never()).save(any(Hotel.class));
        verify(hotelMapper, never()).toShortDto(any());
    }

    @Test
    void getHotelByIdShouldReturnHotelDetailsDtoWhenHotelExists() {
        Hotel hotel = buildHotel(1L);

        HotelDetailsDTO expectedDto = HotelDetailsDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .brand("Hilton")
                .build();

        when(hotelRepository.findByIdWithAmenities(1L)).thenReturn(Optional.of(hotel));
        when(hotelMapper.toDetailsDto(hotel)).thenReturn(expectedDto);

        HotelDetailsDTO result = hotelService.getHotelById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Hilton Minsk", result.getName());
        assertEquals("Hilton", result.getBrand());

        verify(hotelRepository).findByIdWithAmenities(1L);
        verify(hotelMapper).toDetailsDto(hotel);
    }

    @Test
    void getHotelByIdShouldThrowNotFoundExceptionWhenHotelDoesNotExist() {
        when(hotelRepository.findByIdWithAmenities(999L)).thenReturn(Optional.empty());

        NotFoundException exception = assertThrows(
                NotFoundException.class,
                () -> hotelService.getHotelById(999L)
        );

        assertEquals("Hotel not found with id: 999", exception.getMessage());

        verify(hotelRepository).findByIdWithAmenities(999L);
        verify(hotelMapper, never()).toDetailsDto(any());
    }

    @Test
    void getAllHotelsShouldReturnListOfShortDto() {
        Hotel firstHotel = buildHotel(1L);
        Hotel secondHotel = Hotel.builder()
                .id(2L)
                .name("Marriott Warsaw")
                .description("Business hotel")
                .brand("Marriott")
                .address(Address.builder()
                        .houseNumber(15)
                        .street("Central Street")
                        .city("Warsaw")
                        .country("Poland")
                        .postCode("00-001")
                        .build())
                .contacts(Contacts.builder()
                        .phone("+48123123123")
                        .email("info@marriott.com")
                        .build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(15, 0))
                        .checkOut(LocalTime.of(11, 0))
                        .build())
                .build();

        HotelShortDTO firstDto = HotelShortDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .build();

        HotelShortDTO secondDto = HotelShortDTO.builder()
                .id(2L)
                .name("Marriott Warsaw")
                .description("Business hotel")
                .build();

        when(hotelRepository.findAll()).thenReturn(List.of(firstHotel, secondHotel));
        when(hotelMapper.toShortDto(firstHotel)).thenReturn(firstDto);
        when(hotelMapper.toShortDto(secondHotel)).thenReturn(secondDto);

        List<HotelShortDTO> result = hotelService.getAllHotels();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Hilton Minsk", result.get(0).getName());
        assertEquals("Marriott Warsaw", result.get(1).getName());
    }

    @Test
    void searchHotelsShouldReturnFilteredHotelsWhenFiltersProvided() {
        Hotel hotel = buildHotel(1L);

        HotelShortDTO dto = HotelShortDTO.builder()
                .id(1L)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .build();

        when(hotelRepository.findAll(any(Specification.class))).thenReturn(List.of(hotel));
        when(hotelMapper.toShortDto(hotel)).thenReturn(dto);

        List<HotelShortDTO> result = hotelService.searchHotels(
                "Hilton",
                "Hilton",
                "Minsk",
                "Belarus",
                List.of("WiFi", "Spa")
        );

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Hilton Minsk", result.get(0).getName());

        verify(hotelRepository).findAll(any(Specification.class));
        verify(hotelMapper).toShortDto(hotel);
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

    private Hotel buildHotel(Long id) {
        return Hotel.builder()
                .id(id)
                .name("Hilton Minsk")
                .description("Modern hotel")
                .brand("Hilton")
                .address(Address.builder()
                        .houseNumber(10)
                        .street("Pobediteley Avenue")
                        .city("Minsk")
                        .country("Belarus")
                        .postCode("220004")
                        .build())
                .contacts(Contacts.builder()
                        .phone("+375291234567")
                        .email("info@hilton.com")
                        .build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(LocalTime.of(14, 0))
                        .checkOut(LocalTime.of(12, 0))
                        .build())
                .build();
    }
}