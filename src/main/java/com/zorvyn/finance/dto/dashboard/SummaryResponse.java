package com.zorvyn.finance.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SummaryResponse {

    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal netBalance = BigDecimal.ZERO;

    private long totalRecords;
    private long incomeCount;
    private long expenseCount;
}