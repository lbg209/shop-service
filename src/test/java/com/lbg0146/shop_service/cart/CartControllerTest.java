package com.lbg0146.shop_service.cart;

import com.lbg0146.shop_service.cart.dto.request.CartItemCreateRequest;
import com.lbg0146.shop_service.cart.dto.request.CartItemUpdateRequest;
import com.lbg0146.shop_service.cart.entity.Cart;
import com.lbg0146.shop_service.cart.entity.CartItem;
import com.lbg0146.shop_service.cart.repository.CartItemRepository;
import com.lbg0146.shop_service.cart.repository.CartRepository;
import com.lbg0146.shop_service.member.entity.Member;
import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class CartControllerTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    void 장바구니에_상품을_추가() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        CartItemCreateRequest request = new CartItemCreateRequest(
                product.getId(),
                2
        );

        mockMvc.perform(
                        post("/api/carts/items")
                                .param("memberId", member.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 장바구니_상품_수량을_변경() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        Cart cart = Cart.createCart(member);
        cartRepository.save(cart);

        CartItem cartItem = CartItem.createCartItem(
                cart,
                product,
                2
        );

        cartItemRepository.save(cartItem);

        CartItemUpdateRequest request = new CartItemUpdateRequest(10);

        mockMvc.perform(
                        patch("/api/carts/items/" + cartItem.getId())
                                .param("memberId", member.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk());
    }

    @Test
    void 장바구니_상품을_삭제() throws Exception {

        Member member = testDataFactory.createMember();
        Product product = testDataFactory.createProduct();

        Cart cart = Cart.createCart(member);
        cartRepository.save(cart);

        CartItem cartItem = CartItem.createCartItem(
                cart,
                product,
                2
        );

        cartItemRepository.save(cartItem);

        mockMvc.perform(
                        delete("/api/carts/items/" + cartItem.getId())
                                .param("memberId", member.getId().toString())
                )
                .andExpect(status().isNoContent());
    }
}
