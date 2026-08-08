package com.lbg0146.shop_service.address.controller;

import com.lbg0146.shop_service.address.dto.request.AddressCreateRequest;
import com.lbg0146.shop_service.address.dto.request.AddressUpdateRequest;
import com.lbg0146.shop_service.address.dto.response.AddressResponse;
import com.lbg0146.shop_service.address.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members/{memberId}/addresses")
public class AddressController {

    private final AddressService addressService;


    @PostMapping
    public ResponseEntity<Long> createAddress(@PathVariable Long memberId, @Valid @RequestBody AddressCreateRequest request) {

        Long addressId = addressService.createAddress(memberId, request);

        return ResponseEntity.ok(addressId);
    }


    @GetMapping("/{addressId}")
    public ResponseEntity<AddressResponse> findAddress(@PathVariable Long memberId, @PathVariable Long addressId) {

        return ResponseEntity.ok(
                addressService.findAddress(memberId, addressId)
        );
    }


    @GetMapping
    public ResponseEntity<List<AddressResponse>> findAddresses(@PathVariable Long memberId) {

        return ResponseEntity.ok(
                addressService.findAddresses(memberId)
        );
    }


    @PutMapping("/{addressId}")
    public ResponseEntity<Void> updateAddress(@PathVariable Long memberId, @PathVariable Long addressId, @Valid @RequestBody AddressUpdateRequest request) {

        addressService.updateAddress(memberId, addressId, request);

        return ResponseEntity.ok().build();
    }


    @DeleteMapping("/{addressId}")
    public ResponseEntity<Void> deleteAddress(@PathVariable Long memberId, @PathVariable Long addressId) {

        addressService.deleteAddress(memberId, addressId);

        return ResponseEntity.ok().build();
    }


    @PatchMapping("/{addressId}/default")
    public ResponseEntity<Void> changeDefaultAddress(@PathVariable Long memberId, @PathVariable Long addressId) {

        addressService.changeDefaultAddress(memberId, addressId);

        return ResponseEntity.ok().build();
    }
}
