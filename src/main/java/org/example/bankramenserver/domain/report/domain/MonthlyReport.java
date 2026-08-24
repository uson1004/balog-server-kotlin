package org.example.bankramenserver.domain.report.domain;

import org.example.bankramenserver.domain.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "monthly_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "year", "month"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MonthlyReport {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(columnDefinition = "BINARY(16)")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "year", nullable = false)
    private int year;

    @Column(name = "month", nullable = false)
    private int month;

    @Column(name = "total_income", nullable = false)
    private Long totalIncome = 0L;

    @Column(name = "total_expense", nullable = false)
    private Long totalExpense = 0L;

    @Column(name = "prev_month_expense", nullable = false)
    private Long prevMonthExpense = 0L;

    @Column(name = "category_breakdown", columnDefinition = "JSON")
    private String categoryBreakdown;

    @CreatedDate
    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Builder
    public MonthlyReport(User user, int year, int month, Long totalIncome,
                         Long totalExpense, Long prevMonthExpense, String categoryBreakdown) {
        this.user = user;
        this.year = year;
        this.month = month;
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.prevMonthExpense = prevMonthExpense;
        this.categoryBreakdown = categoryBreakdown;
    }

    public void update(Long totalIncome, Long totalExpense, String categoryBreakdown) {
        this.totalIncome = totalIncome;
        this.totalExpense = totalExpense;
        this.categoryBreakdown = categoryBreakdown;
    }
}