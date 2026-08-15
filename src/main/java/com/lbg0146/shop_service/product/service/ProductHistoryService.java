package com.lbg0146.shop_service.product.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductHistory;
import com.lbg0146.shop_service.product.repository.ProductHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductHistoryService {

    private final ProductHistoryRepository productHistoryRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional
    public void saveHistory(Product product, String changeTypeCode, Member changedBy) {

        CommonCodeDetail changeType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                        "HISTORY_CHANGE_TYPE",
                        changeTypeCode
                ).orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        LocalDateTime now = LocalDateTime.now();

        ProductHistory history = ProductHistory.create(
                product,
                changeType,
                changedBy,
                now,
                null
        );

        productHistoryRepository.save(history);
    }

    @Transactional
    public void closeCurrentHistory(Long productId, LocalDateTime validTo) {

        productHistoryRepository.findTopByProductIdAndValidToIsNullOrderByValidFromDesc(productId)
                .ifPresent(history -> history.close(validTo));
    }
}
