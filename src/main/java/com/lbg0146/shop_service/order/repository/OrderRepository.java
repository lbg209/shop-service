package com.lbg0146.shop_service.order.repository;

import com.lbg0146.shop_service.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByIdAndMemberId(Long orderId, Long memberId);

    List<Order> findAllByMemberIdOrderByOrderedAtDesc(Long memberId);

    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN FETCH o.orderItems
    LEFT JOIN FETCH o.delivery
    WHERE o.member.id = :memberId
    ORDER BY o.orderedAt DESC
    """)
    List<Order> findAllByMemberIdWithOrderItemsAndDelivery(
            @Param("memberId") Long memberId
    );

    /*
    @Query("""
    SELECT DISTINCT o
    FROM Order o
    JOIN FETCH o.orderItems
    LEFT JOIN FETCH o.delivery
    LEFT JOIN FETCH o.payment
    WHERE o.id = :orderId
      AND o.member.id = :memberId
    """)
    Optional<Order> findByIdWithDetail(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );*/

    @Query("""
        SELECT DISTINCT o
        FROM Order o
        JOIN FETCH o.orderItems
        LEFT JOIN FETCH o.delivery d
        LEFT JOIN FETCH d.deliveryStatus
        LEFT JOIN FETCH o.payment p
        LEFT JOIN FETCH p.paymentStatus
        LEFT JOIN FETCH p.paymentMethod
        WHERE o.id = :orderId
          AND o.member.id = :memberId
        """)
    Optional<Order> findByIdWithOrderItemsDeliveryAndPayment(
            @Param("orderId") Long orderId,
            @Param("memberId") Long memberId
    );
}
