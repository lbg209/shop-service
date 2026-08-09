package com.lbg0146.shop_service.order.entity;

import com.lbg0146.shop_service.common.entity.BaseEntity;
import com.lbg0146.shop_service.product.entity.Product;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, length = 100)
    private String productName;

    @Column(nullable = false)
    private Long orderPrice;

    @Column(nullable = false)
    private Integer quantity;

    public static OrderItem createOrderItem(
            Order order,
            Product product,
            String productName,
            Long orderPrice,
            Integer quantity
    ) {
        OrderItem orderItem = new OrderItem();

        orderItem.order = order;
        orderItem.product = product;
        orderItem.productName = productName;
        orderItem.orderPrice = orderPrice;
        orderItem.quantity = quantity;

        return orderItem;
    }
}
