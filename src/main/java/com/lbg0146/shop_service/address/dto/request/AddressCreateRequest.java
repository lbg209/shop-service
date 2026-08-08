package com.lbg0146.shop_service.address.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AddressCreateRequest(
        @NotBlank(message = "배송지 이름은 필수입니다.")
        @Size(max = 50, message = "배송지 이름은 50자 이하입니다.")
        String addressName,

        @NotBlank(message = "받는 사람은 필수입니다.")
        @Size(max = 50, message = "받는 사람은 50자 이하입니다.")
        String receiverName,

        @NotBlank(message = "전화번호는 필수입니다.")
        @Pattern(regexp = "^010\\d{8}$", message = "전화번호 형식이 올바르지 않습니다.")
        String phone,

        @NotBlank(message = "우편번호는 필수입니다.")
        @Size(max = 10, message = "우편번호는 10자 이하입니다.")
        String zipcode,

        @NotBlank(message = "주소는 필수입니다.")
        @Size(max = 255, message = "주소는 255자 이하입니다.")
        String address,

        @Size(max = 255, message = "상세주소는 255자 이하입니다.")
        String detailAddress,

        boolean isDefault
) {
}
