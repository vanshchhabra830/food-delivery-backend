package com.example.fooddelivery.service.impl;

import com.example.fooddelivery.dto.request.AddressRequest;
import com.example.fooddelivery.dto.response.AddressResponse;
import com.example.fooddelivery.entity.Address;
import com.example.fooddelivery.entity.User;
import com.example.fooddelivery.exception.ResourceNotFoundException;
import com.example.fooddelivery.mapper.AddressMapper;
import com.example.fooddelivery.repository.AddressRepository;
import com.example.fooddelivery.repository.UserRepository;
import com.example.fooddelivery.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class AddressServiceImpl implements AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressServiceImpl(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public AddressResponse addAddress(AddressRequest request) {
        User user = getCurrentUser();

        boolean isFirstAddress = addressRepository.countByUserId(user.getId()) == 0;
        boolean isDefault = request.getIsDefault();

        if (isFirstAddress) {
            isDefault = true;
        } else if (isDefault) {
            clearExistingDefault(user.getId());
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .landmark(request.getLandmark())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(isDefault)
                .build();

        Address savedAddress = addressRepository.save(address);
        log.info("Address created successfully with id: {} for user: {}", savedAddress.getId(), user.getEmail());

        return AddressMapper.toAddressResponse(savedAddress);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressResponse> getAllAddresses() {
        User user = getCurrentUser();
        List<Address> addresses = addressRepository.findByUserId(user.getId());
        return addresses.stream()
                .map(AddressMapper::toAddressResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressResponse getAddressById(Long addressId) {
        User user = getCurrentUser();
        Address address = findUserAddress(addressId, user.getId());
        return AddressMapper.toAddressResponse(address);
    }

    @Override
    @Transactional
    public AddressResponse updateAddress(Long addressId, AddressRequest request) {
        User user = getCurrentUser();
        Address address = findUserAddress(addressId, user.getId());

        if (request.getIsDefault() && !address.getIsDefault()) {
            clearExistingDefault(user.getId());
        } else if (!request.getIsDefault() && address.getIsDefault()) {
            // Cannot unset default address directly via update unless making another one default
            log.warn("Attempt to unset default address via update by user: {}", user.getEmail());
            throw new IllegalArgumentException("Cannot manually unset default address. Set another address as default instead.");
        }

        address.setFullName(request.getFullName());
        address.setPhoneNumber(request.getPhoneNumber());
        address.setAddressLine1(request.getAddressLine1());
        address.setAddressLine2(request.getAddressLine2());
        address.setLandmark(request.getLandmark());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setPostalCode(request.getPostalCode());
        address.setCountry(request.getCountry());
        
        if (request.getIsDefault()) {
             address.setIsDefault(true);
        }

        Address updatedAddress = addressRepository.save(address);
        log.info("Address updated successfully with id: {}", updatedAddress.getId());

        return AddressMapper.toAddressResponse(updatedAddress);
    }

    @Override
    @Transactional
    public void deleteAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = findUserAddress(addressId, user.getId());

        boolean wasDefault = address.getIsDefault();
        addressRepository.delete(address);
        log.info("Address deleted successfully with id: {}", addressId);

        if (wasDefault) {
            addressRepository.findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(user.getId(), addressId)
                    .ifPresent(newDefault -> {
                        newDefault.setIsDefault(true);
                        addressRepository.save(newDefault);
                        log.info("Default address reassigned to address id: {}", newDefault.getId());
                    });
        }
    }

    @Override
    @Transactional
    public AddressResponse setDefaultAddress(Long addressId) {
        User user = getCurrentUser();
        Address address = findUserAddress(addressId, user.getId());

        if (address.getIsDefault()) {
            return AddressMapper.toAddressResponse(address);
        }

        clearExistingDefault(user.getId());

        address.setIsDefault(true);
        Address updatedAddress = addressRepository.save(address);
        log.info("Default address changed to id: {} for user: {}", addressId, user.getEmail());

        return AddressMapper.toAddressResponse(updatedAddress);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private Address findUserAddress(Long addressId, Long userId) {
        return addressRepository.findByIdAndUserId(addressId, userId)
                .orElseThrow(() -> {
                    log.warn("Unauthorized access attempt or address not found. AddressId: {}, UserId: {}", addressId, userId);
                    return new ResourceNotFoundException("Address not found or does not belong to user");
                });
    }

    private void clearExistingDefault(Long userId) {
        addressRepository.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(existingDefault -> {
                    existingDefault.setIsDefault(false);
                    addressRepository.save(existingDefault);
                });
    }

}
