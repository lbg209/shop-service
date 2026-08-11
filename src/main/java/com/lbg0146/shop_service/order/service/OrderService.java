package com.lbg0146.shop_service.order.service;

import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.CouponStatus;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.coupon.repository.MemberCouponRepository;
import com.lbg0146.shop_service.exception.BusinessException;
import com.lbg0146.shop_service.exception.ErrorCode;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.entity.OrderItem;
import com.lbg0146.shop_service.order.repository.OrderItemRepository;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberCouponRepository memberCouponRepository;

    @Transactional
    public Long createOrder(Long memberId, OrderCreateRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 주문 상품이 비어있는지 확인
        if (request.items() == null || request.items().isEmpty()) {
            throw new BusinessException(ErrorCode.ORDER_ITEM_EMPTY);
        }

        // 3. 주문 상태 조회
        CommonCodeDetail orderStatus = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                                "ORDER_STATUS",
                                "ORDERED"
                ).orElseThrow(() -> new BusinessException(ErrorCode.ORDER_STATUS_NOT_FOUND));

        // 4. 상품 조회 + 검증 + 총액 계산
        List<Product> products = new ArrayList<>();

        long totalPrice = 0L;

        for (OrderItemRequest itemRequest : request.items()) {

            Product product = productRepository.findByIdAndDeletedAtIsNull(itemRequest.productId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.PRODUCT_NOT_FOUND));

            // 주문 수량 검증
            if (itemRequest.quantity() <= 0) {

                throw new BusinessException(ErrorCode.INVALID_QUANTITY);
            }

            // 재고 확인
            if (product.getStockQuantity() < itemRequest.quantity()) {

                throw new BusinessException(ErrorCode.INSUFFICIENT_STOCK);
            }

            // 총 주문 금액 계산
            totalPrice += product.getPrice() * itemRequest.quantity();

            // 조회한 Product 저장
            products.add(product);
        }

        // 5. 할인 계산
        long gradeDiscountAmount = member.getGrade().calculateDiscount(totalPrice);

        // 쿠폰 할인
        long couponDiscountAmount = 0L;

        MemberCoupon memberCoupon = null;

        if (request.memberCouponId() != null) {

            memberCoupon = memberCouponRepository.findByIdAndMemberId(request.memberCouponId(), memberId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.COUPON_NOT_FOUND));

            // 사용 가능한 쿠폰인지 확인
            if (memberCoupon.getStatus() != CouponStatus.ISSUED) {

                throw new BusinessException(ErrorCode.COUPON_NOT_USABLE);
            }

            Coupon coupon = memberCoupon.getCoupon();

            // 최소 주문 금액 확인
            if (totalPrice < coupon.getMinOrderAmount()) {

                throw new BusinessException(ErrorCode.COUPON_MIN_ORDER_AMOUNT_NOT_MET);
            }

            String discountType = coupon.getDiscountType().getCodeValue();

            // 정률 할인
            if ("RATE".equals(discountType)) {

                couponDiscountAmount = totalPrice * coupon.getDiscountValue() / 100;

                // 최대 할인 금액 제한
                if (coupon.getMaxDiscountAmount() != null) {

                    couponDiscountAmount = Math.min(couponDiscountAmount, coupon.getMaxDiscountAmount());
                }
            // 정액 할인
            } else if ("AMOUNT".equals(discountType)) {

                couponDiscountAmount = coupon.getDiscountValue();

            } else {

                throw new BusinessException(ErrorCode.INVALID_DISCOUNT_TYPE);
            }

            // 할인 금액이 주문 금액을 초과하지 않도록 처리
            couponDiscountAmount  = Math.min(couponDiscountAmount , totalPrice);

            // 쿠폰 사용 처리
            memberCoupon.use();
        }
        // 전체 할인 금액
        long discountAmount = gradeDiscountAmount + couponDiscountAmount;

        // 전체 할인 금액이 주문 금액을 초과하지 않도록 처리
        discountAmount = Math.min(discountAmount, totalPrice);

        // 최종 결제 금액
        long finalPrice = totalPrice - discountAmount;

        // 7. 주문 번호 생성
        String orderNumber = "ORD-" + UUID.randomUUID();

// 8. Order 생성
        Order order = Order.createOrder(
                orderNumber,
                member,
                orderStatus,
                memberCoupon,
                request.receiverName(),
                request.receiverPhone(),
                request.zipCode(),
                request.address(),
                request.detailAddress(),
                totalPrice,
                gradeDiscountAmount,
                couponDiscountAmount,
                discountAmount,
                finalPrice
        );

        orderRepository.save(order);

        // 9. OrderItem 생성 + 재고 차감
        for (int i = 0; i < request.items().size(); i++) {

            OrderItemRequest itemRequest = request.items().get(i);

            Product product = products.get(i);

            // 재고 차감
            product.decreaseStock(itemRequest.quantity());

            // 주문 상품 생성
            OrderItem orderItem = OrderItem.createOrderItem(
                    order,
                    product,
                    product.getProductName(),
                    product.getPrice(),
                    itemRequest.quantity()
            );

            orderItemRepository.save(orderItem);
        }

        // 10. 주문 ID 반환
        return order.getId();
    }

    @Transactional
    public Long createCartOrder(Long memberId, OrderCreateRequest request) {

        // 1. 회원 조회
        Member member = memberRepository.findByIdAndDeletedAtIsNull(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 2. 회원의 장바구니 조회
        Cart cart = cartRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.CART_NOT_FOUND));

        // 3. 장바구니 상품 조회
        List<CartItem> cartItems = cartItemRepository.findAllByCartId(cart.getId());

        if (cartItems.isEmpty()) {

            throw new BusinessException(ErrorCode.CART_EMPTY);
        }

        // 4. CartItem → OrderItemRequest 변환
        List<OrderItemRequest> orderItems = cartItems.stream()
                .map(cartItem -> new OrderItemRequest(
                        cartItem.getProduct().getId(),
                        cartItem.getQuantity()
                ))
                .toList();

        // 5. 장바구니 주문 요청 생성
        // 쿠폰 ID를 그대로 전달하여 createOrder()에서
        // 등급 할인 + 쿠폰 할인 계산을 처리한다.
        OrderCreateRequest orderRequest = new OrderCreateRequest(
                orderItems,
                request.receiverName(),
                request.receiverPhone(),
                request.zipCode(),
                request.address(),
                request.detailAddress(),
                request.memberCouponId()
        );

        // 6. 기존 주문 생성 로직 재사용
        Long orderId = createOrder(member.getId(), orderRequest);

        // 7. 주문 완료 후 장바구니 비우기
        cartItems.forEach(cartItem -> cartItemRepository.delete(cartItem));

        return orderId;
    }
}
