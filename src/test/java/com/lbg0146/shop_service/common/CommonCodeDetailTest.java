package com.lbg0146.shop_service.common;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.common.code.repository.CommonCodeDetailRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class CommonCodeDetailTest {

    @Autowired
    CommonCodeDetailRepository commonCodeDetailRepository;

    @Test
    void 주문상태_조회() {

        Optional<CommonCodeDetail> result =
                commonCodeDetailRepository
                        .findByGroupGroupCodeAndCodeValue(
                                "ORDER_STATUS",
                                "ORDERED"
                        );

        assertThat(result).isPresent();
        assertThat(result.get().getCodeValue()).isEqualTo("ORDERED");
    }
}
