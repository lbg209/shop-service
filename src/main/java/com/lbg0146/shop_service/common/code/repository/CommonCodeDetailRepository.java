package com.lbg0146.shop_service.common.code.repository;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CommonCodeDetailRepository extends JpaRepository<CommonCodeDetail, Long> {

    Optional<CommonCodeDetail> findByGroupGroupCodeAndCodeValue(String groupCode, String codeValue);
}