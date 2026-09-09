package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode
import kotlin.math.pow

object FinancialFormulas {

    // --- 1. SIMPLE INTEREST ---
    data class SimpleInterestResult(
        val principal: BigDecimal,
        val dailyInterest: BigDecimal,
        val monthlyInterest: BigDecimal,
        val yearlyInterest: BigDecimal,
        val totalInterest: BigDecimal,
        val totalAmount: BigDecimal
    )

    fun calculateSimpleInterest(
        principal: BigDecimal,
        annualRatePercent: BigDecimal,
        durationValue: BigDecimal,
        durationType: String // "days", "months", "years"
    ): SimpleInterestResult {
        val r = annualRatePercent.safeDiv(BigDecimal("100"))
        val years = when (durationType) {
            "days" -> durationValue.safeDiv(BigDecimal("365"))
            "months" -> durationValue.safeDiv(BigDecimal("12"))
            else -> durationValue
        }
        val totalInterest = principal.multiply(r).multiply(years)
        val dailyInterest = principal.multiply(r).safeDiv(BigDecimal("365"))
        val monthlyInterest = principal.multiply(r).safeDiv(BigDecimal("12"))
        val yearlyInterest = principal.multiply(r)
        val totalAmount = principal.add(totalInterest)

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
        val totalDeposited: BigDecimal,
        val grossInterestEarned: BigDecimal,
        val endingBalance: BigDecimal,
        val realValueInflationAdjusted: BigDecimal
    )

    data class CompoundInterestResult(
        val initialPrincipal: BigDecimal,
        val monthlyDeposit: BigDecimal,
        val totalDeposited: BigDecimal,
        val grossInterest: BigDecimal,
        val netInterestAfterTax: BigDecimal,
        val finalNominalValue: BigDecimal,
        val finalRealValueInflationAdjusted: BigDecimal,
        val yearlyBreakdown: List<CompoundYearRow>
    )

    fun calculateCompoundInterest(
        initialPrincipal: BigDecimal,
        monthlyDeposit: BigDecimal,
        annualRatePercent: BigDecimal,
        years: Int,
        compoundingFrequency: String, // "daily", "monthly", "yearly"
        inflationRatePercent: BigDecimal = BigDecimal.ZERO,
        taxRatePercent: BigDecimal = BigDecimal.ZERO
    ): CompoundInterestResult {
        val r = annualRatePercent.safeDiv(BigDecimal("100"))
        val n = when (compoundingFrequency) {
            "daily" -> BigDecimal("365")
            "monthly" -> BigDecimal("12")
            else -> BigDecimal.ONE
        }
        val inf = inflationRatePercent.safeDiv(BigDecimal("100"))
        val tax = taxRatePercent.safeDiv(BigDecimal("100"))

        var currentBalance = initialPrincipal
        var totalDeposited = initialPrincipal
        var totalGrossInterest = BigDecimal.ZERO
        val yearlyBreakdown = mutableListOf<CompoundYearRow>()

        for (year in 1..years) {
            val startYearBalance = currentBalance
            var yearDeposited = BigDecimal.ZERO

            for (month in 1..12) {
                currentBalance = currentBalance.add(monthlyDeposit)
                yearDeposited = yearDeposited.add(monthlyDeposit)
                
                val periodRate = r.safeDiv(n)
                val compoundPeriodsPerMonth = n.toDouble() / 12.0
                // Use Double for pow then back to BigDecimal
                val multiplier = (BigDecimal.ONE.add(periodRate).toDouble().pow(compoundPeriodsPerMonth)).toBigDecimal()
                currentBalance = currentBalance.multiply(multiplier)
            }

            totalDeposited = totalDeposited.add(yearDeposited)
            val yearGrossInterest = currentBalance.subtract(startYearBalance.add(yearDeposited))
            totalGrossInterest = totalGrossInterest.add(yearGrossInterest)

            val realVal = currentBalance.safeDiv(BigDecimal.ONE.add(inf).toDouble().pow(year.toDouble()).toBigDecimal())

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

        val netInterest = totalGrossInterest.multiply(BigDecimal.ONE.subtract(tax))
        val finalNominal = totalDeposited.add(netInterest)
        val finalReal = finalNominal.safeDiv(BigDecimal.ONE.add(inf).toDouble().pow(years.toDouble()).toBigDecimal())

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
        initialPrincipal: BigDecimal,
        targetFinalValue: BigDecimal,
        annualRatePercent: BigDecimal,
        years: Int,
        compoundingFrequency: String = "monthly"
    ): BigDecimal {
        if (years <= 0) return BigDecimal.ZERO
        val r = annualRatePercent.safeDiv(BigDecimal("100"))
        val n = when (compoundingFrequency) {
            "daily" -> BigDecimal("365")
            "monthly" -> BigDecimal("12")
            else -> BigDecimal.ONE
        }
        val totalMonths = years * 12
        val periodRate = r.safeDiv(n)
        val periodsPerMonth = n.toDouble() / 12.0

        val fvPrincipal = initialPrincipal.multiply(BigDecimal.ONE.add(periodRate).toDouble().pow(n.toDouble() * years).toBigDecimal())
        val remainingTarget = targetFinalValue.subtract(fvPrincipal)
        if (remainingTarget <= BigDecimal.ZERO) return BigDecimal.ZERO

        // Geometric series multiplier for monthly deposits
        var factorSum = BigDecimal.ZERO
        var currentMult = BigDecimal.ONE
        val monthlyMultiplier = BigDecimal.ONE.add(periodRate).toDouble().pow(periodsPerMonth).toBigDecimal()
        for (m in 1..totalMonths) {
            currentMult = currentMult.multiply(monthlyMultiplier)
            factorSum = factorSum.add(currentMult)
        }

        return if (factorSum > BigDecimal.ZERO) remainingTarget.safeDiv(factorSum) else BigDecimal.ZERO
    }

    // --- 3. LOAN & INSTALLMENTS ---
    data class AmortizationRow(
        val month: Int,
        val paymentAmount: BigDecimal,
        val principalPart: BigDecimal,
        val interestPart: BigDecimal,
        val remainingBalance: BigDecimal
    )

    data class LoanResult(
        val loanAmount: BigDecimal,
        val monthlyPayment: BigDecimal,
        val totalRepayment: BigDecimal,
        val totalInterest: BigDecimal,
        val initialFeeAmount: BigDecimal,
        val schedule: List<AmortizationRow>,
        // Early settlement details if specified
        val earlySettlementMonth: Int = 0,
        val remainingBalanceAtSettlement: BigDecimal = BigDecimal.ZERO,
        val penaltyAmount: BigDecimal = BigDecimal.ZERO,
        val totalPayoffAmount: BigDecimal = BigDecimal.ZERO,
        val totalInterestSaved: BigDecimal = BigDecimal.ZERO
    )

    fun calculateLoan(
        loanAmount: BigDecimal,
        annualRatePercent: BigDecimal,
        durationMonths: Int,
        initialFeePercent: BigDecimal = BigDecimal.ZERO,
        earlySettlementMonth: Int = 0,
        penaltyPercent: BigDecimal = BigDecimal.ZERO
    ): LoanResult {
        if (loanAmount <= BigDecimal.ZERO || durationMonths <= 0) {
            return LoanResult(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, emptyList())
        }

        val i = annualRatePercent.safeDiv(BigDecimal("100")).safeDiv(BigDecimal("12"))
        val pmt = if (i > BigDecimal.ZERO) {
            val iPlus1PowN = BigDecimal.ONE.add(i).toDouble().pow(durationMonths.toDouble()).toBigDecimal()
            loanAmount.multiply(i.multiply(iPlus1PowN)).safeDiv(iPlus1PowN.subtract(BigDecimal.ONE))
        } else {
            loanAmount.safeDiv(durationMonths.toBigDecimal())
        }

        var balance = loanAmount
        val schedule = mutableListOf<AmortizationRow>()
        var totalInterest = BigDecimal.ZERO

        for (m in 1..durationMonths) {
            val interestPart = balance.multiply(i)
            val principalPart = pmt.subtract(interestPart)
            balance = balance.subtract(principalPart)
            if (balance < BigDecimal.ZERO) balance = BigDecimal.ZERO
            totalInterest = totalInterest.add(interestPart)

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

        val totalRepayment = pmt.multiply(durationMonths.toBigDecimal())
        val initialFee = loanAmount.multiply(initialFeePercent.safeDiv(BigDecimal("100")))

        // Early settlement calculations
        var remBalAtSettlement = BigDecimal.ZERO
        var penaltyAmt = BigDecimal.ZERO
        var totalPayoff = BigDecimal.ZERO
        var interestSaved = BigDecimal.ZERO

        if (earlySettlementMonth in 1 until durationMonths) {
            remBalAtSettlement = schedule[earlySettlementMonth - 1].remainingBalance
            penaltyAmt = remBalAtSettlement.multiply(penaltyPercent.safeDiv(BigDecimal("100")))
            totalPayoff = remBalAtSettlement.add(penaltyAmt)
            val remainingOriginalPayments = pmt.multiply((durationMonths - earlySettlementMonth).toBigDecimal())
            interestSaved = remainingOriginalPayments.subtract(totalPayoff)
            if (interestSaved < BigDecimal.ZERO) interestSaved = BigDecimal.ZERO
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
        loanAmount: BigDecimal,
        annualRatePercent: BigDecimal,
        durationMonths: Int
    ): BigDecimal = calculateLoan(loanAmount, annualRatePercent, durationMonths).monthlyPayment

    fun calculateMaxLoanFromPayment(
        desiredPayment: BigDecimal,
        annualRatePercent: BigDecimal,
        durationMonths: Int
    ): BigDecimal {
        if (desiredPayment <= BigDecimal.ZERO || durationMonths <= 0) return BigDecimal.ZERO
        val i = annualRatePercent.safeDiv(BigDecimal("100")).safeDiv(BigDecimal("12"))
        return if (i > BigDecimal.ZERO) {
            val iPlus1PowN = BigDecimal.ONE.add(i).toDouble().pow(durationMonths.toDouble()).toBigDecimal()
            desiredPayment.multiply(iPlus1PowN.subtract(BigDecimal.ONE)).safeDiv(i.multiply(iPlus1PowN))
        } else {
            desiredPayment.multiply(durationMonths.toBigDecimal())
        }
    }

    // --- 4. BANK DEPOSIT ---
    data class BankDepositResult(
        val principal: BigDecimal,
        val annualRatePercent: BigDecimal,
        val dailyInterest: BigDecimal,
        val monthlyInterest: BigDecimal,
        val yearlyInterest: BigDecimal,
        val netMonthlyInterest: BigDecimal,
        val realInterestAfterInflation: BigDecimal
    )

    fun calculateBankDeposit(
        principal: BigDecimal,
        annualRatePercent: BigDecimal,
        taxRatePercent: BigDecimal = BigDecimal.ZERO,
        inflationRatePercent: BigDecimal = BigDecimal.ZERO
    ): BankDepositResult {
        val yearlyInterest = principal.multiply(annualRatePercent.safeDiv(BigDecimal("100")))
        val monthlyInterest = yearlyInterest.safeDiv(BigDecimal("12"))
        val dailyInterest = yearlyInterest.safeDiv(BigDecimal("365"))

        val tax = taxRatePercent.safeDiv(BigDecimal("100"))
        val netMonthlyInterest = monthlyInterest.multiply(BigDecimal.ONE.subtract(tax))

        val realInterestRate = annualRatePercent.subtract(inflationRatePercent)
        val realInterestAfterInflation = principal.multiply(realInterestRate.safeDiv(BigDecimal("100")))

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
        val initialAmount: BigDecimal,
        val monthlyDeposit: BigDecimal,
        val annualRatePercent: BigDecimal,
        val durationYears: Int
    )

    data class ComparisonYearPoint(
        val year: Int,
        val nominalValue: BigDecimal,
        val realValue: BigDecimal
    )

    data class ComparisonScenarioResult(
        val name: String,
        val initialAmount: BigDecimal,
        val totalDeposited: BigDecimal,
        val finalNominalValue: BigDecimal,
        val finalRealValue: BigDecimal,
        val totalProfit: BigDecimal,
        val yearPoints: List<ComparisonYearPoint>
    )

    fun compareScenarios(
        scenarios: List<ComparisonScenarioInput>,
        inflationRatePercent: BigDecimal = BigDecimal.ZERO
    ): List<ComparisonScenarioResult> {
        val inf = inflationRatePercent.safeDiv(BigDecimal("100"))
        return scenarios.map { sc ->
            var current = sc.initialAmount
            var totalDep = sc.initialAmount
            val yearPoints = mutableListOf<ComparisonYearPoint>()
            val r = sc.annualRatePercent.safeDiv(BigDecimal("100"))

            for (y in 1..sc.durationYears) {
                for (m in 1..12) {
                    current = current.add(sc.monthlyDeposit)
                    totalDep = totalDep.add(sc.monthlyDeposit)
                    current = current.multiply(BigDecimal.ONE.add(r.safeDiv(BigDecimal("12"))))
                }
                val realVal = current.safeDiv(BigDecimal.ONE.add(inf).toDouble().pow(y.toDouble()).toBigDecimal())
                yearPoints.add(ComparisonYearPoint(y, current, realVal))
            }

            val finalReal = if (sc.durationYears > 0) current.safeDiv(BigDecimal.ONE.add(inf).toDouble().pow(sc.durationYears.toDouble()).toBigDecimal()) else current
            ComparisonScenarioResult(
                name = sc.name,
                initialAmount = sc.initialAmount,
                totalDeposited = totalDep,
                finalNominalValue = current,
                finalRealValue = finalReal,
                totalProfit = current.subtract(totalDep),
                yearPoints = yearPoints
            )
        }
    }

    // --- 6. INFLATION & PURCHASING POWER ---
    data class InflationYearRow(
        val year: Int,
        val purchasingPowerValue: BigDecimal,
        val lossPercentage: BigDecimal
    )

    data class InflationResult(
        val currentAmount: BigDecimal,
        val annualInflationPercent: BigDecimal,
        val years: Int,
        val futureRealPurchasingPower: BigDecimal,
        val percentageLoss: BigDecimal,
        val futureAmountNeededToMatchToday: BigDecimal,
        val yearlyLossBreakdown: List<InflationYearRow>
    )

    fun calculateInflation(
        currentAmount: BigDecimal,
        annualInflationPercent: BigDecimal,
        years: Int
    ): InflationResult {
        val inf = annualInflationPercent.safeDiv(BigDecimal("100"))
        val multiplier = BigDecimal.ONE.add(inf).toDouble().pow(years.toDouble()).toBigDecimal()
        val futureAmountNeeded = currentAmount.multiply(multiplier)
        val futureRealValue = if (multiplier > BigDecimal.ZERO) currentAmount.safeDiv(multiplier) else BigDecimal.ZERO
        val percentageLoss = if (currentAmount > BigDecimal.ZERO) ((currentAmount.subtract(futureRealValue)).safeDiv(currentAmount)).multiply(BigDecimal("100")) else BigDecimal.ZERO

        val yearlyBreakdown = mutableListOf<InflationYearRow>()
        for (y in 1..years) {
            val multY = BigDecimal.ONE.add(inf).toDouble().pow(y.toDouble()).toBigDecimal()
            val realValY = currentAmount.safeDiv(multY)
            val lossPctY = ((currentAmount.subtract(realValY)).safeDiv(currentAmount)).multiply(BigDecimal("100"))
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
        val buyPrice: BigDecimal,
        val sellPrice: BigDecimal,
        val quantity: BigDecimal,
        val totalBuyValue: BigDecimal,
        val totalSellValue: BigDecimal,
        val profitLossAmount: BigDecimal,
        val profitLossPercentage: BigDecimal,
        val isProfit: Boolean
    )

    fun calculateTradeProfit(
        assetName: String,
        buyPrice: BigDecimal,
        sellPrice: BigDecimal,
        quantity: BigDecimal
    ): TradeProfitResult {
        val totalBuy = buyPrice.multiply(quantity)
        val totalSell = sellPrice.multiply(quantity)
        val profit = totalSell.subtract(totalBuy)
        val profitPct = if (totalBuy > BigDecimal.ZERO) (profit.safeDiv(totalBuy)).multiply(BigDecimal("100")) else BigDecimal.ZERO

        return TradeProfitResult(
            assetName = assetName,
            buyPrice = buyPrice,
            sellPrice = sellPrice,
            quantity = quantity,
            totalBuyValue = totalBuy,
            totalSellValue = totalSell,
            profitLossAmount = profit,
            profitLossPercentage = profitPct,
            isProfit = profit >= BigDecimal.ZERO
        )
    }

    fun calculateRequiredSellingPrice(
        buyPrice: BigDecimal,
        targetProfitPercent: BigDecimal
    ): BigDecimal {
        return buyPrice.multiply(BigDecimal.ONE.add(targetProfitPercent.safeDiv(BigDecimal("100"))))
    }

    /** Simple profit/loss percentage of currentBalance vs. initialCapital, e.g. for a dashboard header. */
    fun calculatePnLPercentage(initialCapital: BigDecimal, currentBalance: BigDecimal): BigDecimal {
        if (initialCapital.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO
        return currentBalance.subtract(initialCapital).safeDiv(initialCapital, 4).multiply(BigDecimal("100"))
    }
}
