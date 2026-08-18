package com.lbg0146.shop_service.support;

import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.entity.MemberCoupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
import com.lbg0146.shop_service.coupon.repository.MemberCouponRepository;
import com.lbg0146.shop_service.delivery.entity.Delivery;
import com.lbg0146.shop_service.delivery.repository.DeliveryRepository;
import com.lbg0146.shop_service.grade.entity.Grade;
import com.lbg0146.shop_service.grade.repository.GradeRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.member.repository.MemberRepository;
import com.lbg0146.shop_service.order.dto.request.OrderCreateRequest;
import com.lbg0146.shop_service.order.dto.request.OrderItemRequest;
import com.lbg0146.shop_service.order.entity.Order;
import com.lbg0146.shop_service.order.repository.OrderRepository;
import com.lbg0146.shop_service.order.service.OrderService;
import com.lbg0146.shop_service.payment.dto.request.PaymentCreateRequest;
import com.lbg0146.shop_service.payment.entity.Payment;
import com.lbg0146.shop_service.payment.repository.PaymentRepository;
import com.lbg0146.shop_service.payment.service.PaymentService;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TestDataFactory {

    private final GradeRepository gradeRepository;
    private final MemberRepository memberRepository;
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final CouponRepository couponRepository;
    private final CommonCodeDetailRepository commonCodeDetailRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final DeliveryRepository deliveryRepository;
    private final PaymentRepository paymentRepository;

    public Member createMember() {

        Grade grade = gradeRepository.findByGradeCode("BASIC")
                .orElseThrow();

        Member member = Member.createMember(
                grade,
                "paymentUser",
                "1234",
                "테스트회원",
                "테스트닉네임",
                "payment@test.com",
                "01012375678",
                Role.USER
        );

        return memberRepository.save(member);
    }

    public Product createProduct() {

        Category category = Category.createCategory(
                "테스트카테고리",
                null
        );

        categoryRepository.save(category);

        Product product = Product.createProduct(
                category,
                "테스트상품",
                10000L,
                5,
                "테스트 상품"
        );

        return productRepository.save(product);
    }

    public Order createOrder(Member member) {

        Product product = createProduct();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(new OrderItemRequest(product.getId(), 1)),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호",
                null
        );

        Long orderId = orderService.createOrder(member.getId(), request);

        return orderRepository.findById(orderId).orElseThrow();
    }

    public Order createPaidOrder(Member member) {

        Order order = createOrder(member);

        paymentService.createPayment(
                new PaymentCreateRequest(
                        order.getId(),
                        "CARD"
                )
        );

        return orderRepository.findById(order.getId())
                .orElseThrow();
    }

    public Delivery createDelivery(Order order) {

        CommonCodeDetail deliveryStatus =
                commonCodeDetailRepository
                        .findByGroupGroupCodeAndCodeValue(
                                "DELIVERY_STATUS",
                                "READY"
                        )
                        .orElseThrow();

        Delivery delivery = Delivery.createDelivery(order, deliveryStatus);

        return deliveryRepository.save(delivery);
    }

    public Payment createPayment(Order order) {

        CommonCodeDetail paymentStatus =
                commonCodeDetailRepository
                        .findByGroupGroupCodeAndCodeValue(
                                "PAYMENT_STATUS",
                                "PAID"
                        )
                        .orElseThrow();

        CommonCodeDetail paymentMethod =
                commonCodeDetailRepository
                        .findByGroupGroupCodeAndCodeValue(
                                "PAYMENT_METHOD",
                                "CARD"
                        )
                        .orElseThrow();

        Payment payment = Payment.createPayment(
                order,
                paymentStatus,
                paymentMethod,
                order.getFinalPrice(),
                "test-payment-key",
                LocalDateTime.now()
        );

        return paymentRepository.save(payment);
    }

    public Cart createCart(Member member) {

        Cart cart = Cart.createCart(member);

        return cartRepository.save(cart);
    }

    public CartItem createCartItem(Cart cart, Product product, int quantity) {

        CartItem cartItem =
                CartItem.createCartItem(
                        cart,
                        product,
                        quantity
                );

        return cartItemRepository.save(cartItem);
    }

    public Coupon createRateCoupon() {
        CommonCodeDetail discountType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                        "DISCOUNT_TYPE",
                        "RATE"
                ).orElseThrow();

        Coupon coupon = Coupon.createCoupon(
                discountType,
                "테스트 정률 쿠폰",
                10,
                10000L,
                5000L,
                30
        );

        return couponRepository.save(coupon);
    }

    public Coupon createAmountCoupon() {

        CommonCodeDetail discountType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                "DISCOUNT_TYPE",
                "AMOUNT"
        ).orElseThrow();

        Coupon coupon = Coupon.createCoupon(
                discountType,
                "테스트 정액 쿠폰",
                5000,
                10000L,
                null,
                30
        );

        return couponRepository.save(coupon);
    }

    public Coupon createRateCouponWithMinOrderAmount(Long minOrderAmount) {

        CommonCodeDetail discountType = commonCodeDetailRepository.findByGroupGroupCodeAndCodeValue(
                    "DISCOUNT_TYPE",
                        "RATE"
                ).orElseThrow();

        Coupon coupon = Coupon.createCoupon(
                discountType,
                "테스트 최소주문금액 쿠폰",
                10,
                minOrderAmount,
                5000L,
                30
        );

        return couponRepository.save(coupon);
    }

    public MemberCoupon createMemberCoupon(Member member, Coupon coupon) {

        MemberCoupon memberCoupon = MemberCoupon.createMemberCoupon(member, coupon);

        return memberCouponRepository.save(memberCoupon);
    }

    public Member createVipMember() {

        Grade grade = gradeRepository.findByGradeCode("VIP").orElseThrow();

        Member member = Member.createMember(
                grade,
                "vipUser",
                "1234",
                "VIP테스트",
                "VIP닉네임",
                "vip@test.com",
                "01099998888",
                Role.USER
        );

        return memberRepository.save(member);
    }
}
