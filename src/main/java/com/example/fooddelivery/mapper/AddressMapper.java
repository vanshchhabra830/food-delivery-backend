package com.example.fooddelivery.mapper;

import com.example.fooddelivery.dto.response.AddressResponse;
import com.example.fooddelivery.entity.Address;

public class AddressMapper {

    private AddressMapper() {
    }

    public static AddressResponse toAddressResponse(Address address) {
        if (address == null) {
            return null;
        }

        return AddressResponse.builder()
                .id(address.getId())
                .fullName(address.getFullName())
                .phoneNumber(address.getPhoneNumber())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .landmark(address.getLandmark())
                .city(address.getCity())
                .state(address.getState())
                .postalCode(address.getPostalCode())
                .country(address.getCountry())
                .isDefault(address.getIsDefault())
                .build();
    }

}
