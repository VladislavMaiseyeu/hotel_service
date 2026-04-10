package com.example.hotel_service.service;

import com.example.hotel_service.model.dto.request.CreateHotelRequest;
import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Map;

public interface HotelService {

    List<HotelShortDTO> getAllHotels();

    HotelDetailsDTO getHotelById(Long id);

    List<HotelShortDTO> searchHotels(String name, String brand, String city, String country, List<String> amenities);

    HotelShortDTO createHotel(CreateHotelRequest request);

    void addAmenities(Long hotelId, List<String> amenities);

    Map<String, Long> getHistogram(String param);
}
