package com.zorvyn.finance.dto.dashboard;

import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategorySummary {

    private String category;

    @Builder.Default
    private BigDecimal totalIncome = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal totalExpense = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal net = BigDecimal.ZERO;

    private long recordCount;
}