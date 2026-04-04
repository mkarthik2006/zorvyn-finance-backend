package com.zorvyn.finance.service;

import com.zorvyn.finance.dto.record.*;
import com.zorvyn.finance.entity.*;
import com.zorvyn.finance.exception.ResourceNotFoundException;
import com.zorvyn.finance.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialRecordServiceTest {

    @Mock
    private FinancialRecordRepository recordRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FinancialRecordService recordService;

    private User testUser;
    private FinancialRecord testRecord;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .name("Admin User")
                .email("admin@example.com")
                .password("encoded")
                .role(Role.ADMIN)
                .status(UserStatus.ACTIVE)
                .build();

        testRecord = FinancialRecord.builder()
                .id(1L)
                .amount(new BigDecimal("5000.00"))
                .type(RecordType.INCOME)
                .category("Salary")
                .date(LocalDate.of(2026, 3, 15))
                .notes("March salary")
                .createdBy(testUser)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
    }

    @Test
    void getRecordById_WhenExists_ShouldReturn() {
        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testRecord));

        FinancialRecordResponse response = recordService.getRecordById(1L);

        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(response.getType()).isEqualTo(RecordType.INCOME);
        assertThat(response.getCategory()).isEqualTo("Salary");
    }

    @Test
    void getRecordById_WhenNotFound_ShouldThrow() {
        when(recordRepository.findByIdAndDeletedFalse(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> recordService.getRecordById(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createRecord_ShouldCreateSuccessfully() {
        // Mock SecurityContext
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("admin@example.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmailAndDeletedFalse("admin@example.com")).thenReturn(Optional.of(testUser));

        FinancialRecordRequest request = FinancialRecordRequest.builder()
                .amount(new BigDecimal("1500.00"))
                .type(RecordType.EXPENSE)
                .category("Rent")
                .date(LocalDate.of(2026, 3, 1))
                .notes("Monthly rent")
                .build();

        when(recordRepository.save(any(FinancialRecord.class))).thenAnswer(inv -> {
            FinancialRecord record = inv.getArgument(0);
            record.setId(2L);
            record.setCreatedAt(LocalDateTime.now());
            record.setUpdatedAt(LocalDateTime.now());
            return record;
        });

        FinancialRecordResponse response = recordService.createRecord(request);

        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("1500.00"));
        assertThat(response.getType()).isEqualTo(RecordType.EXPENSE);
        assertThat(response.getCategory()).isEqualTo("Rent");
        verify(recordRepository).save(any(FinancialRecord.class));
    }

    @Test
    void getAllRecords_WithFilters_ShouldReturnPage() {
        Page<FinancialRecord> page = new PageImpl<>(List.of(testRecord));
        when(recordRepository.findAllWithFilters(any(), any(), any(), any(), any())).thenReturn(page);

        Page<FinancialRecordResponse> result = recordService.getAllRecords(
                RecordType.INCOME, null, null, null, 0, 10, "date", "desc");

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getType()).isEqualTo(RecordType.INCOME);
    }

    @Test
    void deleteRecord_ShouldSoftDelete() {
        when(recordRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testRecord));
        when(recordRepository.save(any(FinancialRecord.class))).thenReturn(testRecord);

        recordService.deleteRecord(1L);

        assertThat(testRecord.isDeleted()).isTrue();
        verify(recordRepository).save(testRecord);
    }
}