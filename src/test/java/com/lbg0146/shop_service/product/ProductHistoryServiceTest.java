package com.lbg0146.shop_service.product;

import com.lbg0146.shop_service.product.entity.Product;
import com.lbg0146.shop_service.product.entity.ProductHistory;
import com.lbg0146.shop_service.product.repository.ProductHistoryRepository;
import com.lbg0146.shop_service.product.service.ProductHistoryService;
import com.lbg0146.shop_service.support.TestDataFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class ProductHistoryServiceTest {

    @Autowired
    private TestDataFactory testDataFactory;

    @Autowired
    private ProductHistoryService productHistoryService;

    @Autowired
    private ProductHistoryRepository productHistoryRepository;

    @Test
    void 상품_이력을_저장하면_상품정보가_스냅샷으로_저장() {

        Product product = testDataFactory.createProduct();

        productHistoryService.saveHistory(
                product,
                "CREATE",
                null
        );

        List<ProductHistory> histories = productHistoryRepository.findAllByProductId(product.getId());

        assertThat(histories).hasSize(1);

        ProductHistory history = histories.get(0);

        assertThat(history.getProduct().getId()).isEqualTo(product.getId());

        assertThat(history.getProductName()).isEqualTo(product.getProductName());

        assertThat(history.getPrice()).isEqualTo(product.getPrice());

        assertThat(history.getStockQuantity()).isEqualTo(product.getStockQuantity());

        assertThat(history.getChangeType().getCodeValue()).isEqualTo("CREATE");

        assertThat(history.getChangedBy()).isNull();

        assertThat(history.getValidFrom()).isNotNull();

        assertThat(history.getValidTo()).isNull();
    }

    @Test
    void 현재_상품_이력을_종료할_수_있다() {

        Product product = testDataFactory.createProduct();

        productHistoryService.saveHistory(
                product,
                "CREATE",
                null
        );

        LocalDateTime validTo = LocalDateTime.now();

        productHistoryService.closeCurrentHistory(
                product.getId(),
                validTo
        );

        List<ProductHistory> histories = productHistoryRepository.findAllByProductId(product.getId());

        assertThat(histories).hasSize(1);

        ProductHistory history = histories.get(0);

        assertThat(history.getValidFrom()).isNotNull();

        assertThat(history.getValidTo()).isNotNull();

        assertThat(history.getValidTo()).isEqualTo(validTo);
    }
}
