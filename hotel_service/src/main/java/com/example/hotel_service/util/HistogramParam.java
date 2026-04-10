package com.example.hotel_service.util;

import com.example.hotel_service.model.exception.BadRequestException;

public enum HistogramParam {
    BRAND,
    CITY,
    COUNTRY,
    AMENITIES;

    public static HistogramParam from(String value) {
        try {
            return HistogramParam.valueOf(value.toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Unsupported histogram parameter: " + value);
        }
    }
}
