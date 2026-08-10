package com.lbg0146.shop_service.support;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import com.lbg0146.shop_service.common.enums.Role;
import com.lbg0146.shop_service.coupon.entity.Coupon;
import com.lbg0146.shop_service.coupon.repository.CouponRepository;
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
import com.lbg0146.shop_service.payment.service.PaymentService;
import com.lbg0146.shop_service.product.entity.Category;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.repository.CategoryRepository;
import com.lbg0146.shop_service.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
                "01012345678",
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
                100,
                "테스트 상품"
        );

        return productRepository.save(product);
    }

    public Order createOrder() {

        Member member = createMember();
        Product product = createProduct();

        OrderCreateRequest request = new OrderCreateRequest(
                List.of(
                        new OrderItemRequest(
                                product.getId(),
                                1
                        )
                ),
                "테스터",
                "01012345678",
                "12345",
                "서울시 강남구",
                "101호"
        );

        Long orderId = orderService.createOrder(
                member.getId(),
                request
        );

        return orderRepository.findById(orderId)
                .orElseThrow();
    }

    public Order createPaidOrder() {

        Order order = createOrder();

        paymentService.createPayment(
                new PaymentCreateRequest(
                        order.getId(),
                        "CARD"
                )
        );

        return orderRepository.findById(order.getId())
                .orElseThrow();
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
}
