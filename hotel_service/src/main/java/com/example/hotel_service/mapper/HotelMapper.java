package com.example.hotel_service.mapper;

import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import com.example.hotel_service.model.entity.Address;
import com.example.hotel_service.model.entity.Amenity;
import com.example.hotel_service.model.entity.Hotel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface HotelMapper {

    @Mapping(target = "address", expression = "java(formatAddress(hotel.getAddress()))")
    @Mapping(target = "phone", source = "contacts.phone")
    HotelShortDTO toShortDto(Hotel hotel);

    @Mapping(target = "amenities", expression = "java(mapAmenities(hotel.getAmenities()))")
    HotelDetailsDTO toDetailsDto(Hotel hotel);

    default String formatAddress(Address address) {
        if (address == null) {
            return null;
        }
        return String.format("%d %s, %s, %s, %s",
                address.getHouseNumber(),
                address.getStreet(),
                address.getCity(),
                address.getPostCode(),
                address.getCountry());
    }

    default List<String> mapAmenities(Set<Amenity> amenities) {
        if (amenities == null) {
            return List.of();
        }
        return amenities.stream()
                .map(Amenity::getName)
                .sorted()
                .toList();
    }
}
