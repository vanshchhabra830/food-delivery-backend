package com.example.fooddelivery.repository;

import com.example.fooddelivery.entity.Address;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserId(Long userId);

    Optional<Address> findByIdAndUserId(Long id, Long userId);

    Optional<Address> findByUserIdAndIsDefaultTrue(Long userId);

    Optional<Address> findFirstByUserIdAndIdNotOrderByUpdatedAtDesc(Long userId, Long excludedId);

    long countByUserId(Long userId);

}
