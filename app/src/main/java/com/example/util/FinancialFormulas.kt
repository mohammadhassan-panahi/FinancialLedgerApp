package com.example.util

import kotlin.math.pow

object FinancialFormulas {

    // --- 1. SIMPLE INTEREST ---
    data class SimpleInterestResult(
        val principal: Double,
        val dailyInterest: Double,
        val monthlyInterest: Double,
        val yearlyInterest: Double,
        val totalInterest: Double,
        val totalAmount: Double
    )

    fun calculateSimpleInterest(
        principal: Double,
        annualRatePercent: Double,
        durationValue: Double,
        durationType: String // "days", "months", "years"
    ): SimpleInterestResult {
        val r = annualRatePercent / 100.0
        val years = when (durationType) {
            "days" -> durationValue / 365.0
            "months" -> durationValue / 12.0
            else -> durationValue
        }
        val totalInterest = principal * r * years
        val dailyInterest = (principal * r) / 365.0
        val monthlyInterest = (principal * r) / 12.0
        val yearlyInterest = principal * r
        val totalAmount = principal + totalInterest

        return SimpleInterestResult(
            principal = principal,
            dailyInterest = dailyInterest,
            monthlyInterest = monthlyInterest,
            yearlyInterest = yearlyInterest,
            totalInterest = totalInterest,
            totalAmount = totalAmount
        )
    }

    // --- 2. COMPOUND INTEREST ---
    data class CompoundYearRow(
        val year: Int,
        val totalDeposited: Double,
        val grossInterestEarned: Double,
        val endingBalance: Double,
        val realValueInflationAdjusted: Double
    )

    data class CompoundInterestResult(
        val initialPrincipal: Double,
        val monthlyDeposit: Double,
        val totalDeposited: Double,
        val grossInterest: Double,
        val netInterestAfterTax: Double,
        val finalNominalValue: Double,
        val finalRealValueInflationAdjusted: Double,
        val yearlyBreakdown: List<CompoundYearRow>
    )

    fun calculateCompoundInterest(
        initialPrincipal: Double,
        monthlyDeposit: Double,
        annualRatePercent: Double,
        years: Int,
        compoundingFrequency: String, // "daily", "monthly", "yearly"
        inflationRatePercent: Double = 0.0,
        taxRatePercent: Double = 0.0
    ): CompoundInterestResult {
        val r = annualRatePercent / 100.0
        val n = when (compoundingFrequency) {
            "daily" -> 365
            "monthly" -> 12
            else -> 1
        }
        val inf = inflationRatePercent / 100.0
        val tax = taxRatePercent / 100.0

        var currentBalance = initialPrincipal
        var totalDeposited = initialPrincipal
        var totalGrossInterest = 0.0
        val yearlyBreakdown = mutableListOf<CompoundYearRow>()

        for (year in 1..years) {
            val startYearBalance = currentBalance
            var yearDeposited = 0.0

            for (month in 1..12) {
                currentBalance += monthlyDeposit
                yearDeposited += monthlyDeposit
                // compound monthly approximation or discrete period
                val periodRate = r / n
                val compoundPeriodsPerMonth = n / 12.0
                currentBalance *= (1 + periodRate).pow(compoundPeriodsPerMonth)
            }

            totalDeposited += yearDeposited
            val yearGrossInterest = currentBalance - (startYearBalance + yearDeposited)
            totalGrossInterest += yearGrossInterest

            val realVal = currentBalance / (1 + inf).pow(year.toDouble())

            yearlyBreakdown.add(
                CompoundYearRow(
                    year = year,
                    totalDeposited = totalDeposited,
                    grossInterestEarned = totalGrossInterest,
                    endingBalance = currentBalance,
                    realValueInflationAdjusted = realVal
                )
            )
        }

        val netInterest = totalGrossInterest * (1 - tax)
        val finalNominal = totalDeposited + netInterest
        val finalReal = finalNominal / (1 + inf).pow(years.toDouble())

        return CompoundInterestResult(
            initialPrincipal = initialPrincipal,
            monthlyDeposit = monthlyDeposit,
            totalDeposited = totalDeposited,
            grossInterest = totalGrossInterest,
            netInterestAfterTax = netInterest,
            finalNominalValue = finalNominal,
            finalRealValueInflationAdjusted = finalReal,
            yearlyBreakdown = yearlyBreakdown
        )
    }

    fun calculateRequiredMonthlyDepositForTarget(
        initialPrincipal: Double,
        targetFinalValue: Double,
        annualRatePercent: Double,
        years: Int,
        compoundingFrequency: String = "monthly"
    ): Double {
        if (years <= 0) return 0.0
        val r = annualRatePercent / 100.0
        val n = when (compoundingFrequency) {
            "daily" -> 365
            "monthly" -> 12
            else -> 1
        }
        val totalMonths = years * 12
        val periodRate = r / n
        val periodsPerMonth = n / 12.0

        val fvPrincipal = initialPrincipal * (1 + periodRate).pow(n.toDouble() * years)
        val remainingTarget = targetFinalValue - fvPrincipal
        if (remainingTarget <= 0) return 0.0

        // Geometric series multiplier for monthly deposits
        var factorSum = 0.0
        var currentMult = 1.0
        val monthlyMultiplier = (1 + periodRate).pow(periodsPerMonth)
        for (m in 1..totalMonths) {
            currentMult *= monthlyMultiplier
            factorSum += currentMult
        }

        return if (factorSum > 0) remainingTarget / factorSum else 0.0
    }

    // --- 3. LOAN & INSTALLMENTS ---
    data class AmortizationRow(
        val month: Int,
        val paymentAmount: Double,
        val principalPart: Double,
        val interestPart: Double,
        val remainingBalance: Double
    )

    data class LoanResult(
        val loanAmount: Double,
        val monthlyPayment: Double,
        val totalRepayment: Double,
        val totalInterest: Double,
        val initialFeeAmount: Double,
        val schedule: List<AmortizationRow>,
        // Early settlement details if specified
        val earlySettlementMonth: Int = 0,
        val remainingBalanceAtSettlement: Double = 0.0,
        val penaltyAmount: Double = 0.0,
        val totalPayoffAmount: Double = 0.0,
        val totalInterestSaved: Double = 0.0
    )

    fun calculateLoan(
        loanAmount: Double,
        annualRatePercent: Double,
        durationMonths: Int,
        initialFeePercent: Double = 0.0,
        earlySettlementMonth: Int = 0,
        penaltyPercent: Double = 0.0
    ): LoanResult {
        if (loanAmount <= 0 || durationMonths <= 0) {
            return LoanResult(0.0, 0.0, 0.0, 0.0, 0.0, emptyList())
        }

        val i = (annualRatePercent / 100.0) / 12.0
        val pmt = if (i > 0) {
            loanAmount * (i * (1 + i).pow(durationMonths)) / ((1 + i).pow(durationMonths) - 1)
        } else {
            loanAmount / durationMonths
        }

        var balance = loanAmount
        val schedule = mutableListOf<AmortizationRow>()
        var totalInterest = 0.0

        for (m in 1..durationMonths) {
            val interestPart = balance * i
            val principalPart = pmt - interestPart
            balance -= principalPart
            if (balance < 0) balance = 0.0
            totalInterest += interestPart

            schedule.add(
                AmortizationRow(
                    month = m,
                    paymentAmount = pmt,
                    principalPart = principalPart,
                    interestPart = interestPart,
                    remainingBalance = balance
                )
            )
        }

        val totalRepayment = pmt * durationMonths
        val initialFee = loanAmount * (initialFeePercent / 100.0)

        // Early settlement calculations
        var remBalAtSettlement = 0.0
        var penaltyAmt = 0.0
        var totalPayoff = 0.0
        var interestSaved = 0.0

        if (earlySettlementMonth in 1 until durationMonths) {
            remBalAtSettlement = schedule[earlySettlementMonth - 1].remainingBalance
            penaltyAmt = remBalAtSettlement * (penaltyPercent / 100.0)
            totalPayoff = remBalAtSettlement + penaltyAmt
            val remainingOriginalPayments = pmt * (durationMonths - earlySettlementMonth)
            interestSaved = remainingOriginalPayments - totalPayoff
            if (interestSaved < 0) interestSaved = 0.0
        }

        return LoanResult(
            loanAmount = loanAmount,
            monthlyPayment = pmt,
            totalRepayment = totalRepayment,
            totalInterest = totalInterest,
            initialFeeAmount = initialFee,
            schedule = schedule,
            earlySettlementMonth = earlySettlementMonth,
            remainingBalanceAtSettlement = remBalAtSettlement,
            penaltyAmount = penaltyAmt,
            totalPayoffAmount = totalPayoff,
            totalInterestSaved = interestSaved
        )
    }

    fun calculateLoanInstallment(
        loanAmount: Double,
        annualRatePercent: Double,
        durationMonths: Int
    ): Double = calculateLoan(loanAmount, annualRatePercent, durationMonths).monthlyPayment

    fun calculateMaxLoanFromPayment(
        desiredPayment: Double,
        annualRatePercent: Double,
        durationMonths: Int
    ): Double {
        if (desiredPayment <= 0 || durationMonths <= 0) return 0.0
        val i = (annualRatePercent / 100.0) / 12.0
        return if (i > 0) {
            desiredPayment * ((1 + i).pow(durationMonths) - 1) / (i * (1 + i).pow(durationMonths))
        } else {
            desiredPayment * durationMonths
        }
    }

    // --- 4. BANK DEPOSIT ---
    data class BankDepositResult(
        val principal: Double,
        val annualRatePercent: Double,
        val dailyInterest: Double,
        val monthlyInterest: Double,
        val yearlyInterest: Double,
        val netMonthlyInterest: Double,
        val realInterestAfterInflation: Double
    )

    fun calculateBankDeposit(
        principal: Double,
        annualRatePercent: Double,
        taxRatePercent: Double = 0.0,
        inflationRatePercent: Double = 0.0
    ): BankDepositResult {
        val yearlyInterest = principal * (annualRatePercent / 100.0)
        val monthlyInterest = yearlyInterest / 12.0
        val dailyInterest = yearlyInterest / 365.0

        val tax = taxRatePercent / 100.0
        val netMonthlyInterest = monthlyInterest * (1 - tax)

        val realInterestRate = annualRatePercent - inflationRatePercent
        val realInterestAfterInflation = principal * (realInterestRate / 100.0)

        return BankDepositResult(
            principal = principal,
            annualRatePercent = annualRatePercent,
            dailyInterest = dailyInterest,
            monthlyInterest = monthlyInterest,
            yearlyInterest = yearlyInterest,
            netMonthlyInterest = netMonthlyInterest,
            realInterestAfterInflation = realInterestAfterInflation
        )
    }

    // --- 5. INVESTMENT COMPARISON ---
    data class ComparisonScenarioInput(
        val name: String,
        val initialAmount: Double,
        val monthlyDeposit: Double,
        val annualRatePercent: Double,
        val durationYears: Int
    )

    data class ComparisonYearPoint(
        val year: Int,
        val nominalValue: Double,
        val realValue: Double
    )

    data class ComparisonScenarioResult(
        val name: String,
        val initialAmount: Double,
        val totalDeposited: Double,
        val finalNominalValue: Double,
        val finalRealValue: Double,
        val totalProfit: Double,
        val yearPoints: List<ComparisonYearPoint>
    )

    fun compareScenarios(
        scenarios: List<ComparisonScenarioInput>,
        inflationRatePercent: Double = 0.0
    ): List<ComparisonScenarioResult> {
        val inf = inflationRatePercent / 100.0
        return scenarios.map { sc ->
            var current = sc.initialAmount
            var totalDep = sc.initialAmount
            val yearPoints = mutableListOf<ComparisonYearPoint>()
            val r = sc.annualRatePercent / 100.0

            for (y in 1..sc.durationYears) {
                for (m in 1..12) {
                    current += sc.monthlyDeposit
                    totalDep += sc.monthlyDeposit
                    current *= (1 + r / 12.0)
                }
                val realVal = current / (1 + inf).pow(y.toDouble())
                yearPoints.add(ComparisonYearPoint(y, current, realVal))
            }

            val finalReal = if (sc.durationYears > 0) current / (1 + inf).pow(sc.durationYears.toDouble()) else current
            ComparisonScenarioResult(
                name = sc.name,
                initialAmount = sc.initialAmount,
                totalDeposited = totalDep,
                finalNominalValue = current,
                finalRealValue = finalReal,
                totalProfit = current - totalDep,
                yearPoints = yearPoints
            )
        }
    }

    // --- 6. INFLATION & PURCHASING POWER ---
    data class InflationYearRow(
        val year: Int,
        val purchasingPowerValue: Double,
        val lossPercentage: Double
    )

    data class InflationResult(
        val currentAmount: Double,
        val annualInflationPercent: Double,
        val years: Int,
        val futureRealPurchasingPower: Double,
        val percentageLoss: Double,
        val futureAmountNeededToMatchToday: Double,
        val yearlyLossBreakdown: List<InflationYearRow>
    )

    fun calculateInflation(
        currentAmount: Double,
        annualInflationPercent: Double,
        years: Int
    ): InflationResult {
        val inf = annualInflationPercent / 100.0
        val multiplier = (1 + inf).pow(years.toDouble())
        val futureAmountNeeded = currentAmount * multiplier
        val futureRealValue = if (multiplier > 0) currentAmount / multiplier else 0.0
        val percentageLoss = if (currentAmount > 0) ((currentAmount - futureRealValue) / currentAmount) * 100.0 else 0.0

        val yearlyBreakdown = mutableListOf<InflationYearRow>()
        for (y in 1..years) {
            val multY = (1 + inf).pow(y.toDouble())
            val realValY = currentAmount / multY
            val lossPctY = ((currentAmount - realValY) / currentAmount) * 100.0
            yearlyBreakdown.add(InflationYearRow(y, realValY, lossPctY))
        }

        return InflationResult(
            currentAmount = currentAmount,
            annualInflationPercent = annualInflationPercent,
            years = years,
            futureRealPurchasingPower = futureRealValue,
            percentageLoss = percentageLoss,
            futureAmountNeededToMatchToday = futureAmountNeeded,
            yearlyLossBreakdown = yearlyBreakdown
        )
    }

    // --- 7. GOLD, DOLLAR & FX PROFIT ---
    data class TradeProfitResult(
        val assetName: String,
        val buyPrice: Double,
        val sellPrice: Double,
        val quantity: Double,
        val totalBuyValue: Double,
        val totalSellValue: Double,
        val profitLossAmount: Double,
        val profitLossPercentage: Double,
        val isProfit: Boolean
    )

    fun calculateTradeProfit(
        assetName: String,
        buyPrice: Double,
        sellPrice: Double,
        quantity: Double
    ): TradeProfitResult {
        val totalBuy = buyPrice * quantity
        val totalSell = sellPrice * quantity
        val profit = totalSell - totalBuy
        val profitPct = if (totalBuy > 0) (profit / totalBuy) * 100.0 else 0.0

        return TradeProfitResult(
            assetName = assetName,
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            quantity = quantity,
            totalBuyValue = totalBuy,
            totalSellValue = totalSell,
            profitLossAmount = profit,
            profitLossPercentage = profitPct,
            isProfit = profit >= 0
        )
    }

    fun calculateRequiredSellingPrice(
        buyPrice: Double,
        targetProfitPercent: Double
    ): Double {
        return buyPrice * (1 + targetProfitPercent / 100.0)
    }

    /** Simple profit/loss percentage of currentBalance vs. initialCapital, e.g. for a dashboard header. */
    fun calculatePnLPercentage(initialCapital: Double, currentBalance: Double): Double {
        if (initialCapital == 0.0) return 0.0
        return ((currentBalance - initialCapital) / initialCapital) * 100.0
    }
}
