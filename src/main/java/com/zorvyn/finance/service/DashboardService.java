package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.dashboard.*;
import com.zorvyn.finance.dto.record.FinancialRecordResponse;
import com.zorvyn.finance.entity.RecordType;
import com.zorvyn.finance.repository.FinancialRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final FinancialRecordRepository recordRepository;

    public SummaryResponse getSummary() {
        BigDecimal totalIncome = recordRepository.sumByType(RecordType.INCOME);
        BigDecimal totalExpense = recordRepository.sumByType(RecordType.EXPENSE);
        BigDecimal netBalance = totalIncome.subtract(totalExpense);

        long incomeCount = recordRepository.countByType(RecordType.INCOME);
        long expenseCount = recordRepository.countByType(RecordType.EXPENSE);
        long totalRecords = recordRepository.countByDeletedFalse();

        return SummaryResponse.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .netBalance(netBalance)
                .totalRecords(totalRecords)
                .incomeCount(incomeCount)
                .expenseCount(expenseCount)
                .build();
    }

    public List<CategorySummary> getCategoryWiseSummary() {
        List<Object[]> results = recordRepository.getCategoryWiseSummary();

        // Group by category
        Map<String, CategorySummary> categoryMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            String category = (String) row[0];
            RecordType type = (RecordType) row[1];
            BigDecimal sum = (BigDecimal) row[2];
            long count = (Long) row[3];

            CategorySummary summary = categoryMap.computeIfAbsent(category, k ->
                    CategorySummary.builder()
                            .category(k)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO)
                            .net(BigDecimal.ZERO)
                            .recordCount(0)
                            .build());

            if (type == RecordType.INCOME) {
                summary.setTotalIncome(sum);
            } else {
                summary.setTotalExpense(sum);
            }
            summary.setRecordCount(summary.getRecordCount() + count);
            summary.setNet(summary.getTotalIncome().subtract(summary.getTotalExpense()));
        }

        return new ArrayList<>(categoryMap.values());
    }

    public List<MonthlyTrend> getMonthlyTrends() {
        List<Object[]> results = recordRepository.getMonthlyTrends();

        
        Map<String, MonthlyTrend> trendMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            int year = ((Number) row[0]).intValue();
            int month = ((Number) row[1]).intValue();
            RecordType type = (RecordType) row[2];
            BigDecimal sum = (BigDecimal) row[3];
            long count = (Long) row[4];

            String key = year + "-" + month;
            String monthName = Month.of(month).getDisplayName(TextStyle.FULL, Locale.ENGLISH);

            MonthlyTrend trend = trendMap.computeIfAbsent(key, k ->
                    MonthlyTrend.builder()
                            .year(year)
                            .month(month)
                            .monthName(monthName)
                            .totalIncome(BigDecimal.ZERO)
                            .totalExpense(BigDecimal.ZERO)
                            .netBalance(BigDecimal.ZERO)
                            .recordCount(0)
                            .build());

            if (type == RecordType.INCOME) {
                trend.setTotalIncome(sum);
            } else {
                trend.setTotalExpense(sum);
            }
            trend.setRecordCount(trend.getRecordCount() + count);
            trend.setNetBalance(trend.getTotalIncome().subtract(trend.getTotalExpense()));
        }

        return new ArrayList<>(trendMap.values());
    }

    public List<FinancialRecordResponse> getRecentActivity(int limit) {
        return recordRepository.findRecentRecords(PageRequest.of(0, limit)).stream()
                .map(record -> FinancialRecordResponse.builder()
                        .id(record.getId())
                        .amount(record.getAmount())
                        .type(record.getType())
                        .category(record.getCategory())
                        .date(record.getDate())
                        .notes(record.getNotes())
                        .createdById(record.getCreatedBy().getId())
                        .createdByName(record.getCreatedBy().getName())
                        .createdAt(record.getCreatedAt())
                        .updatedAt(record.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}