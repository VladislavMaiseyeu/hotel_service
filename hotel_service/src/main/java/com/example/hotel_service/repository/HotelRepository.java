package com.example.hotel_service.repository;

import com.example.hotel_service.model.entity.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    @Query("""
            select distinct h
            from Hotel h
            left join fetch h.amenities
            where h.id = :id
            """)
    Optional<Hotel> findByIdWithAmenities(Long id);

    boolean existsByNameIgnoreCaseAndAddress_HouseNumberAndAddress_StreetIgnoreCaseAndAddress_CityIgnoreCaseAndAddress_PostCode(
            String name,
            Integer houseNumber,
            String street,
            String city,
            String postCode
    );

}
