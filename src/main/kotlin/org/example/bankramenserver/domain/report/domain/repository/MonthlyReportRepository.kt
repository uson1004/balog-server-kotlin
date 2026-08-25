package org.example.bankramenserver.domain.report.domain.repository

import org.example.bankramenserver.domain.report.domain.MonthlyReport
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface MonthlyReportRepository : JpaRepository<MonthlyReport, UUID>, MonthlyReportRepositoryCustom
