package com.lbg0146.shop_service.order.service;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderStatusHistory;
import com.lbg0146.shop_service.order.repository.OrderStatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderStatusHistoryService {

    private final OrderStatusHistoryRepository orderStatusHistoryRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;

    @Transactional
    public void recordCreated(Order order, CommonCodeDetail initialStatus, Member changedBy) {
        CommonCodeDetail changeType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "HISTORY_CHANGE_TYPE",
                "CREATE")
                .orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        OrderStatusHistory history = OrderStatusHistory.create(
                order,
                initialStatus,
                changeType,
                changedBy
        );

        orderStatusHistoryRepository.save(history);
    }

    @Transactional
    public void changeStatus(Order order, CommonCodeDetail newStatus, Member changedBy) {
        CommonCodeDetail changeType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                                "HISTORY_CHANGE_TYPE",
                                "UPDATE"
        ).orElseThrow(() -> new BusinessException(ErrorCode.HISTORY_CHANGE_TYPE_NOT_FOUND));

        order.changeStatus(newStatus);

        OrderStatusHistory history = OrderStatusHistory.create(order, newStatus, changeType, changedBy);

        orderStatusHistoryRepository.save(history);
    }
}
