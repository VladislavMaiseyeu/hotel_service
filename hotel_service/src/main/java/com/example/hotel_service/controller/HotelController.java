package com.example.hotel_service.controller;

import com.example.hotel_service.model.dto.request.CreateHotelRequest;
import com.example.hotel_service.model.dto.response.HotelDetailsDTO;
import com.example.hotel_service.model.dto.response.HotelShortDTO;
import com.example.hotel_service.service.HotelService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${end.point.context-path}")
@RequiredArgsConstructor
@Validated
public class HotelController {

    private final HotelService hotelService;

    @GetMapping("${end.point.hotels}")
    public List<HotelShortDTO> getAllHotels() {
        return hotelService.getAllHotels();
    }

    @GetMapping("${end.point.id}")
    public HotelDetailsDTO getHotelById(@PathVariable Long id) {
        return hotelService.getHotelById(id);
    }

    @GetMapping("${end.point.search}")
    public List<HotelShortDTO> searchHotels(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String country,
            @RequestParam(required = false) List<String> amenities
    ) {
        return hotelService.searchHotels(name, brand, city, country, amenities);
    }

    @PostMapping("${end.point.hotels}")
    @ResponseStatus(HttpStatus.CREATED)
    public HotelShortDTO createHotel(@Valid @RequestBody CreateHotelRequest request) {
        return hotelService.createHotel(request);
    }

    @PostMapping("${end.point.amenities}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addAmenities(@PathVariable Long id, @RequestBody List<String> amenities) {
        hotelService.addAmenities(id, amenities);
    }

    @GetMapping("${end.point.histogram}")
    public Map<String, Long> getHistogram(@PathVariable String param) {
        return hotelService.getHistogram(param);
    }
}
