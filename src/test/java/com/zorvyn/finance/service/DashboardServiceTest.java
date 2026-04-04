package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.dashboard.*;
import com.zorvyn.finance.entity.RecordType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void getSummary_ShouldReturnCorrectTotals() {
        when(recordRepository.sumByType(RecordType.INCOME)).thenReturn(new BigDecimal("10000"));
        when(recordRepository.sumByType(RecordType.EXPENSE)).thenReturn(new BigDecimal("3500"));
        when(recordRepository.countByType(RecordType.INCOME)).thenReturn(5L);
        when(recordRepository.countByType(RecordType.EXPENSE)).thenReturn(3L);
        when(recordRepository.countByDeletedFalse()).thenReturn(8L);

        SummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("10000"));
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(new BigDecimal("3500"));
        assertThat(summary.getNetBalance()).isEqualByComparingTo(new BigDecimal("6500"));
        assertThat(summary.getTotalRecords()).isEqualTo(8);
        assertThat(summary.getIncomeCount()).isEqualTo(5);
        assertThat(summary.getExpenseCount()).isEqualTo(3);
    }

    @Test
    void getSummary_WithNoRecords_ShouldReturnZeros() {
        when(recordRepository.sumByType(RecordType.INCOME)).thenReturn(BigDecimal.ZERO);
        when(recordRepository.sumByType(RecordType.EXPENSE)).thenReturn(BigDecimal.ZERO);
        when(recordRepository.countByType(RecordType.INCOME)).thenReturn(0L);
        when(recordRepository.countByType(RecordType.EXPENSE)).thenReturn(0L);
        when(recordRepository.countByDeletedFalse()).thenReturn(0L);

        SummaryResponse summary = dashboardService.getSummary();

        assertThat(summary.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalExpense()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getNetBalance()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(summary.getTotalRecords()).isEqualTo(0);
    }

    @Test
    void getCategoryWiseSummary_ShouldGroupCorrectly() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{"Food", RecordType.EXPENSE, new BigDecimal("500"), 3L});
        mockResults.add(new Object[]{"Salary", RecordType.INCOME, new BigDecimal("5000"), 1L});

        when(recordRepository.getCategoryWiseSummary()).thenReturn(mockResults);

        List<CategorySummary> summaries = dashboardService.getCategoryWiseSummary();

        assertThat(summaries).hasSize(2);

        CategorySummary food = summaries.stream()
                .filter(s -> s.getCategory().equals("Food")).findFirst().orElseThrow();
        assertThat(food.getTotalExpense()).isEqualByComparingTo(new BigDecimal("500"));
        assertThat(food.getTotalIncome()).isEqualByComparingTo(BigDecimal.ZERO);

        CategorySummary salary = summaries.stream()
                .filter(s -> s.getCategory().equals("Salary")).findFirst().orElseThrow();
        assertThat(salary.getTotalIncome()).isEqualByComparingTo(new BigDecimal("5000"));
    }

    @Test
    void getMonthlyTrends_ShouldReturnTrends() {
        List<Object[]> mockResults = new ArrayList<>();
        mockResults.add(new Object[]{2026, 3, RecordType.INCOME, new BigDecimal("5000"), 2L});
        mockResults.add(new Object[]{2026, 3, RecordType.EXPENSE, new BigDecimal("1500"), 3L});

        when(recordRepository.getMonthlyTrends()).thenReturn(mockResults);

        List<MonthlyTrend> trends = dashboardService.getMonthlyTrends();

        assertThat(trends).hasSize(1);
        MonthlyTrend march = trends.get(0);
        assertThat(march.getYear()).isEqualTo(2026);
        assertThat(march.getMonth()).isEqualTo(3);
        assertThat(march.getMonthName()).isEqualTo("March");
        assertThat(march.getTotalIncome()).isEqualByComparingTo(new BigDecimal("5000"));
        assertThat(march.getTotalExpense()).isEqualByComparingTo(new BigDecimal("1500"));
        assertThat(march.getNetBalance()).isEqualByComparingTo(new BigDecimal("3500"));
        assertThat(march.getRecordCount()).isEqualTo(5);
    }
}