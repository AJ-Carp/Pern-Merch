package com.ajcarpinello.Pern_Merch_Website.service;

import com.ajcarpinello.Pern_Merch_Website.dto.AddressDTO;
import com.ajcarpinello.Pern_Merch_Website.entity.Address;
import com.ajcarpinello.Pern_Merch_Website.entity.User;
import com.ajcarpinello.Pern_Merch_Website.exception.AppException;
import com.ajcarpinello.Pern_Merch_Website.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public AddressDTO getDefaultAddress(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        Address address = user.getDefaultShippingAddress();
        if (address == null) return null;
        return toAddressDTO(address);
    }

    @Transactional
    public AddressDTO saveDefaultAddress(String username, AddressDTO dto) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "User not found"));
        user.setDefaultShippingAddress(toEntity(dto));
        return dto;
    }

    public AddressDTO toAddressDTO(Address a) {
        return AddressDTO.builder()
                .recipientName(a.getRecipientName())
                .line1(a.getLine1())
                .line2(a.getLine2())
                .city(a.getCity())
                .state(a.getState())
                .postalCode(a.getPostalCode())
                .country(a.getCountry())
                .phone(a.getPhone())
                .build();
    }

    private Address toEntity(AddressDTO d) {
        return Address.builder()
                .recipientName(d.getRecipientName())
                .line1(d.getLine1())
                .line2(d.getLine2())
                .city(d.getCity())
                .state(d.getState())
                .postalCode(d.getPostalCode())
                .country(d.getCountry())
                .phone(d.getPhone())
                .build();
    }
}
