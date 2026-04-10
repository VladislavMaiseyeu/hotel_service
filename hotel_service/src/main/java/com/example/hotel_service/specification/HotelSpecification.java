package com.example.hotel_service.specification;

import com.example.hotel_service.model.entity.Amenity;
import com.example.hotel_service.model.entity.Hotel;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class HotelSpecification {

    private HotelSpecification() {
    }

    public static Specification<Hotel> byFilters(
            String name,
            String brand,
            String city,
            String country,
            List<String> amenities
    ) {
        return (root, query, cb) -> {
            query.distinct(true);

            List<Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("name")),
                                "%" + name.trim().toLowerCase() + "%"
                        )
                );
            }

            if (brand != null && !brand.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("brand")),
                                "%" + brand.trim().toLowerCase() + "%"
                        )
                );
            }

            if (city != null && !city.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("address").get("city")),
                                "%" + city.trim().toLowerCase() + "%"
                        )
                );
            }

            if (country != null && !country.isBlank()) {
                predicates.add(
                        cb.like(
                                cb.lower(root.get("address").get("country")),
                                "%" + country.trim().toLowerCase() + "%"
                        )
                );
            }

            if (amenities != null && !amenities.isEmpty()) {
                List<String> normalizedAmenities = amenities.stream()
                        .filter(Objects::nonNull)
                        .map(String::trim)
                        .filter(value -> !value.isBlank())
                        .map(String::toLowerCase)
                        .toList();

                if (!normalizedAmenities.isEmpty()) {
                    Join<Hotel, Amenity> amenityJoin = root.join("amenities", JoinType.LEFT);

                    CriteriaBuilder.In<String> inClause = cb.in(cb.lower(amenityJoin.get("name")));
                    normalizedAmenities.forEach(inClause::value);

                    predicates.add(inClause);
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
