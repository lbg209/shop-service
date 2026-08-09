package com.lbg0146.shop_service.order.repository;

import com.lbg0146.shop_service.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    List<Order> findAllByMemberIdOrderByOrderedAtDesc(Long memberId);
}
