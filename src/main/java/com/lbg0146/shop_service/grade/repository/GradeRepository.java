package com.lbg0146.shop_service.grade.repository;

import com.lbg0146.shop_service.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    Optional<Grade> findByGradeCode(String gradeCode);

}
