package com.example.fooddelivery.service;

import com.example.fooddelivery.dto.request.AddressRequest;
import com.example.fooddelivery.dto.response.AddressResponse;

import java.util.List;

public interface AddressService {

    AddressResponse addAddress(AddressRequest request);

    List<AddressResponse> getAllAddresses();

    AddressResponse getAddressById(Long addressId);

    AddressResponse updateAddress(Long addressId, AddressRequest request);

    void deleteAddress(Long addressId);

    AddressResponse setDefaultAddress(Long addressId);

}
