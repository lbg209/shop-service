package com.lbg0146.shop_service.member.repository;

import com.lbg0146.shop_service.common.code.entity.CommonCodeDetail;
import com.lbg0146.shop_service.member.entity.Member;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class MemberHistoryBulkRepository {

    private final JdbcTemplate jdbcTemplate;

    public void saveAll(
            List<? extends Member> members,
            CommonCodeDetail changeType,
            Member changedBy
    ) {
        String sql = """
                INSERT INTO member_history (
                    member_id,
                    change_type_code_id,
                    login_id,
                    name,
                    email,
                    phone,
                    role,
                    grade_id,
                    changed_by,
                    valid_from,
                    valid_to,
                    created_at
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        LocalDateTime now = LocalDateTime.now();

        jdbcTemplate.batchUpdate(
                sql,
                members,
                members.size(),
                (ps, member) -> {
                    ps.setLong(1, member.getId());
                    ps.setLong(2, changeType.getId());
                    ps.setString(3, member.getLoginId());
                    ps.setString(4, member.getName());
                    ps.setString(5, member.getEmail());
                    ps.setString(6, member.getPhone());
                    ps.setString(7, member.getRole().name());
                    ps.setLong(8, member.getGrade().getId());

                    if (changedBy == null) {
                        ps.setNull(9, Types.BIGINT);
                    } else {
                        ps.setLong(9, changedBy.getId());
                    }

                    ps.setObject(10, now);
                    ps.setNull(11, Types.TIMESTAMP);
                    ps.setObject(12, now);
                }
        );
    }
}
