package com.example.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FinancialFormulasTest {
    @Test fun loanInstallment_valid() {
        val result = FinancialFormulas.calculateLoan(100_000_000.0, 18.0, 12)
        assertEquals(9_168_000.0, result.monthlyPayment, 2.0)
        assertEquals(12, result.schedule.size)
        assertEquals(0.0, result.schedule.last().remainingBalance, 0.01)
    }

    @Test fun loanInstallment_zeroRate() {
        assertEquals(1_000_000.0, FinancialFormulas.calculateLoanInstallment(12_000_000.0, 0.0, 12), 0.01)
        assertEquals(0.0, FinancialFormulas.calculateLoanInstallment(0.0, 18.0, 12), 0.0)
        assertEquals(0.0, FinancialFormulas.calculateLoanInstallment(100_000.0, 18.0, 0), 0.0)
    }

    @Test fun compoundInterest_monthly_matchesFormula() {
        val result = FinancialFormulas.calculateCompoundInterest(
            initialPrincipal = 50_000_000.0,
            monthlyDeposit = 0.0,
            annualRatePercent = 20.0,
            years = 3,
            compoundingFrequency = "monthly"
        )
        val expected = 50_000_000.0 * Math.pow(1.0 + 0.20 / 12.0, 36.0)
        assertEquals(expected, result.finalNominalValue, 0.01)
    }

    @Test fun requiredMonthlyDeposit_hitsTargetApproximately() {
        val deposit = FinancialFormulas.calculateRequiredMonthlyDepositForTarget(
            initialPrincipal = 10_000_000.0,
            targetFinalValue = 50_000_000.0,
            annualRatePercent = 12.0,
            years = 3,
            compoundingFrequency = "monthly"
        )
        assertTrue(deposit > 0.0)
    }

    @Test fun pnlPercentage_safeAndCorrect() {
        assertEquals(0.0, FinancialFormulas.calculatePnLPercentage(0.0, 500_000.0), 0.0)
        assertFalse(FinancialFormulas.calculatePnLPercentage(0.0, 500_000.0).isNaN())
        assertEquals(25.0, FinancialFormulas.calculatePnLPercentage(100_000_000.0, 125_000_000.0), 0.01)
        assertEquals(-10.0, FinancialFormulas.calculatePnLPercentage(100_000_000.0, 90_000_000.0), 0.01)
    }
}
