package com.zorvyn.finance.repository;

import com.zorvyn.finance.entity.FinancialRecord;
import com.zorvyn.finance.entity.RecordType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

   
    default Optional<FinancialRecord> findByIdAndDeletedFalse(Long id) {
        return findById(id);
    }

    
    @Query("SELECT fr FROM FinancialRecord fr WHERE " +
            "(:type IS NULL OR fr.type = :type) " +
            "AND (:category IS NULL OR LOWER(CAST(fr.category AS text)) LIKE LOWER(CONCAT('%', CAST(:category AS text), '%'))) " +
            "AND (:startDate IS NULL OR fr.date >= :startDate) " +
            "AND (:endDate IS NULL OR fr.date <= :endDate)")
    Page<FinancialRecord> findAllWithFilters(
            @Param("type") RecordType type,
            @Param("category") String category,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable);

    
    @Query("SELECT COALESCE(SUM(fr.amount), 0) FROM FinancialRecord fr " +
            "WHERE fr.type = :type")
    BigDecimal sumByType(@Param("type") RecordType type);

    
    @Query("SELECT COUNT(fr) FROM FinancialRecord fr " +
            "WHERE fr.type = :type")
    long countByType(@Param("type") RecordType type);

    
    long count();

    default long countByDeletedFalse() {
        return count();
    }

    
    @Query("SELECT fr.category, fr.type, COALESCE(SUM(fr.amount), 0), COUNT(fr) " +
            "FROM FinancialRecord fr " +
            "GROUP BY fr.category, fr.type ORDER BY fr.category")
    List<Object[]> getCategoryWiseSummary();

    
    @Query("SELECT EXTRACT(YEAR FROM fr.date), EXTRACT(MONTH FROM fr.date), fr.type, " +
            "COALESCE(SUM(fr.amount), 0), COUNT(fr) " +
            "FROM FinancialRecord fr " +
            "GROUP BY EXTRACT(YEAR FROM fr.date), EXTRACT(MONTH FROM fr.date), fr.type " +
            "ORDER BY EXTRACT(YEAR FROM fr.date), EXTRACT(MONTH FROM fr.date)")
    List<Object[]> getMonthlyTrends();

    
    @Query("SELECT fr FROM FinancialRecord fr ORDER BY fr.createdAt DESC")
    List<FinancialRecord> findRecentRecords(Pageable pageable);
}