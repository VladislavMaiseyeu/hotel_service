package com.example.hotel_service.service.impl;

import com.example.hotel_service.mapper.HotelMapper;
import com.example.hotel_service.model.dto.request.CreateHotelRequest;
import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import com.example.hotel_service.model.entity.*;
import com.example.hotel_service.model.exception.BadRequestException;
import com.example.hotel_service.model.exception.HotelAlreadyExistsException;
import com.example.hotel_service.model.exception.NotFoundException;
import com.example.hotel_service.repository.AmenityRepository;
import com.example.hotel_service.repository.HotelRepository;
import com.example.hotel_service.service.HotelService;
import com.example.hotel_service.specification.HotelSpecification;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final AmenityRepository amenityRepository;
    private final HotelMapper hotelMapper;


    @Override
    public List<HotelShortDTO> getAllHotels() {
        return hotelRepository.findAll().stream()
                .map(hotelMapper::toShortDto)
                .toList();
    }

    @Override
    public HotelDetailsDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findByIdWithAmenities(id)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id: " + id));

        return hotelMapper.toDetailsDto(hotel);
    }

    @Override
    public List<HotelShortDTO> searchHotels(
            String name,
            String brand,
            String city,
            String country,
            List<String> amenities
    ) {
        Specification<Hotel> specification = HotelSpecification.byFilters(
                name, brand, city, country, amenities
        );

        return hotelRepository.findAll(specification).stream()
                .map(hotelMapper::toShortDto)
                .toList();
    }

    @Override
    @Transactional
    public HotelShortDTO createHotel(CreateHotelRequest request) {
        boolean exists = hotelRepository
                .existsByNameIgnoreCaseAndAddress_HouseNumberAndAddress_StreetIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_PostCode(
                        request.getName(),
                        request.getAddress().getHouseNumber(),
                        request.getAddress().getStreet(),
                        request.getAddress().getCity(),
                        request.getAddress().getPostCode()
                );

        if (exists) {
            throw new HotelAlreadyExistsException("Hotel with the same name and address already exists");
        }

        Set<Amenity> amenities = new HashSet<>();

        if (request.getAmenities() != null && !request.getAmenities().isEmpty()) {
            amenities = request.getAmenities().stream()
                    .map(String::trim)
                    .filter(name -> !name.isBlank())
                    .map(name -> amenityRepository.findByNameIgnoreCase(name)
                            .orElseGet(() -> amenityRepository.save(
                                    Amenity.builder()
                                            .name(name)
                                            .build()
                            )))
                    .collect(Collectors.toSet());
        }

        Hotel hotel = Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .brand(request.getBrand())
                .address(Address.builder()
                        .houseNumber(request.getAddress().getHouseNumber())
                        .street(request.getAddress().getStreet())
                        .city(request.getAddress().getCity())
                        .country(request.getAddress().getCountry())
                        .postCode(request.getAddress().getPostCode())
                        .build())
                .contacts(Contacts.builder()
                        .phone(request.getContacts().getPhone())
                        .email(request.getContacts().getEmail())
                        .build())
                .arrivalTime(ArrivalTime.builder()
                        .checkIn(request.getArrivalTime().getCheckIn())
                        .checkOut(request.getArrivalTime().getCheckOut())
                        .build())
                .amenities(amenities)
                .build();

        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toShortDto(saved);
    }

    @Override
    @Transactional
    public void addAmenities(Long hotelId, List<String> amenityNames) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new NotFoundException("Hotel not found with id=" + hotelId));

        Set<Amenity> amenitiesToAdd = amenityNames.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .map(name -> amenityRepository.findByNameIgnoreCase(name)
                        .orElseGet(() -> amenityRepository.save(Amenity.builder().name(name).build())))
                .collect(Collectors.toSet());

        hotel.getAmenities().addAll(amenitiesToAdd);
        hotelRepository.save(hotel);
    }

    @Override
    public Map<String, Long> getHistogram(String param) {
        List<Hotel> hotels = hotelRepository.findAll();

        return switch (param.toLowerCase()) {
            case "brand" -> hotels.stream()
                    .filter(hotel -> hotel.getBrand() != null && !hotel.getBrand().isBlank())
                    .collect(Collectors.groupingBy(
                            Hotel::getBrand,
                            Collectors.counting()
                    ));

            case "city" -> hotels.stream()
                    .filter(hotel -> hotel.getAddress() != null)
                    .map(hotel -> hotel.getAddress().getCity())
                    .filter(city -> city != null && !city.isBlank())
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            Collectors.counting()
                    ));

            case "country" -> hotels.stream()
                    .filter(hotel -> hotel.getAddress() != null)
                    .map(hotel -> hotel.getAddress().getCountry())
                    .filter(country -> country != null && !country.isBlank())
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            Collectors.counting()
                    ));

            case "amenities" -> hotels.stream()
                    .flatMap(hotel -> hotel.getAmenities().stream())
                    .map(Amenity::getName)
                    .filter(name -> name != null && !name.isBlank())
                    .collect(Collectors.groupingBy(
                            Function.identity(),
                            Collectors.counting()
                    ));

            default -> throw new BadRequestException("Unsupported histogram parameter: " + param);
        };
    }
}
