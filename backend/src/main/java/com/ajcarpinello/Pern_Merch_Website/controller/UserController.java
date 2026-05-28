package com.ajcarpinello.Pern_Merch_Website.controller;

import com.ajcarpinello.Pern_Merch_Website.dto.AddressDTO;
import com.ajcarpinello.Pern_Merch_Website.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/address")
    public ResponseEntity<AddressDTO> getDefaultAddress(@AuthenticationPrincipal UserDetails user) {
        AddressDTO dto = userService.getDefaultAddress(user.getUsername());
        if (dto == null) return ResponseEntity.noContent().build();
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/address")
    public ResponseEntity<AddressDTO> putDefaultAddress(
            @AuthenticationPrincipal UserDetails user,
            @Valid @RequestBody AddressDTO dto) {
        return ResponseEntity.ok(userService.saveDefaultAddress(user.getUsername(), dto));
    }
}
