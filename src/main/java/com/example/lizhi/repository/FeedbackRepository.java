// FeedbackRepository.java
package com.example.lizhi.repository;

import com.example.lizhi.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    // 根据状态查询反馈
    Page<Feedback> findByStatus(Integer status, Pageable pageable);

    // 根据提交者ID查询反馈
    Page<Feedback> findBySubmitterId(Long submitterId, Pageable pageable);

    // 搜索反馈内容
    @Query("SELECT f FROM Feedback f WHERE f.content LIKE %:keyword% OR f.contactInfo LIKE %:keyword%")
    Page<Feedback> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // 根据状态和关键词搜索
    @Query("SELECT f FROM Feedback f WHERE f.status = :status AND (f.content LIKE %:keyword% OR f.contactInfo LIKE %:keyword%)")
    Page<Feedback> searchByStatusAndKeyword(@Param("status") Integer status, @Param("keyword") String keyword, Pageable pageable);

    // 统计各状态反馈数量
    @Query("SELECT f.status, COUNT(f) FROM Feedback f GROUP BY f.status")
    List<Object[]> countByStatus();
}