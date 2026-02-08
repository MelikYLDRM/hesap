package com.melikyldrm.hesap.domain.engine

import com.melikyldrm.hesap.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.pow

@Singleton
class FinanceCalculator @Inject constructor() {

    /**
     * KDV Hesaplama
     * @param amount Tutar
     * @param kdvRate KDV oranı (enum)
     * @param isKdvIncluded Girilen tutar KDV dahil mi?
     */
    fun calculateKdv(amount: Double, kdvRate: KdvRate, isKdvIncluded: Boolean): KdvResult {
        return if (isKdvIncluded) {
            // KDV dahil tutardan hesaplama
            val baseAmount = amount / (1 + kdvRate.rate)
            val kdvAmount = amount - baseAmount
            KdvResult(
                baseAmount = baseAmount,
                kdvRate = kdvRate.rate,
                kdvAmount = kdvAmount,
                totalAmount = amount,
                isKdvIncluded = true
            )
        } else {
            // KDV hariç tutardan hesaplama
            val kdvAmount = amount * kdvRate.rate
            val totalAmount = amount + kdvAmount
            KdvResult(
                baseAmount = amount,
                kdvRate = kdvRate.rate,
                kdvAmount = kdvAmount,
                totalAmount = totalAmount,
                isKdvIncluded = false
            )
        }
    }

    /**
     * Tevkifat Hesaplama
     * Tevkifat: Alıcının, satıcı yerine KDV'nin bir kısmını doğrudan vergi dairesine ödemesi
     * @param baseAmount Matrah (KDV hariç tutar)
     * @param kdvRate KDV oranı
     * @param tevkifatRate Tevkifat oranı
     */
    fun calculateTevkifat(
        baseAmount: Double,
        kdvRate: KdvRate,
        tevkifatRate: TevkifatRate
    ): TevkifatResult {
        val kdvAmount = baseAmount * kdvRate.rate
        val tevkifatAmount = kdvAmount * tevkifatRate.rate // Alıcının vergi dairesine ödeyeceği
        val sellerKdv = kdvAmount - tevkifatAmount // Satıcının tahsil edeceği KDV
        val totalAmount = baseAmount + kdvAmount

        return TevkifatResult(
            baseAmount = baseAmount,
            kdvRate = kdvRate.rate,
            kdvAmount = kdvAmount,
            tevkifatRate = tevkifatRate.displayName,
            tevkifatAmount = tevkifatAmount,
            sellerKdv = sellerKdv,
            totalAmount = totalAmount
        )
    }

    /**
     * Basit Faiz Hesaplama
     * Formül: A = P(1 + rt)
     * @param principal Ana para (P)
     * @param annualRate Yıllık faiz oranı (r) - yüzde olarak (örn: 15 = %15)
     * @param timeInYears Süre yıl olarak (t)
     */
    fun calculateSimpleInterest(
        principal: Double,
        annualRate: Double,
        timeInYears: Double
    ): FaizResult {
        val rate = annualRate / 100.0
        val interest = principal * rate * timeInYears
        val totalAmount = principal + interest

        return FaizResult(
            principal = principal,
            rate = annualRate,
            time = timeInYears,
            compoundFrequency = null,
            interest = interest,
            totalAmount = totalAmount,
            isCompound = false
        )
    }

    /**
     * Bileşik Faiz Hesaplama
     * Formül: A = P(1 + r/n)^(nt)
     * @param principal Ana para (P)
     * @param annualRate Yıllık faiz oranı (r) - yüzde olarak
     * @param timeInYears Süre yıl olarak (t)
     * @param frequency Bileşik faiz sıklığı (n)
     */
    fun calculateCompoundInterest(
        principal: Double,
        annualRate: Double,
        timeInYears: Double,
        frequency: CompoundFrequency
    ): FaizResult {
        val rate = annualRate / 100.0
        val n = frequency.periodsPerYear
        val totalAmount = principal * (1 + rate / n).pow(n * timeInYears)
        val interest = totalAmount - principal

        return FaizResult(
            principal = principal,
            rate = annualRate,
            time = timeInYears,
            compoundFrequency = n,
            interest = interest,
            totalAmount = totalAmount,
            isCompound = true
        )
    }

    /**
     * Kâr/Zarar Hesaplama
     * @param costPrice Maliyet fiyatı
     * @param sellingPrice Satış fiyatı
     */
    fun calculateProfitLoss(costPrice: Double, sellingPrice: Double): KarZararResult {
        val profitOrLoss = sellingPrice - costPrice
        val percentage = if (costPrice != 0.0) {
            (profitOrLoss / costPrice) * 100
        } else {
            0.0
        }

        return KarZararResult(
            costPrice = costPrice,
            sellingPrice = sellingPrice,
            profitOrLoss = profitOrLoss,
            percentage = percentage,
            isProfit = profitOrLoss >= 0
        )
    }

    /**
     * Satış Fiyatı Hesaplama (Kâr yüzdesinden)
     * @param costPrice Maliyet fiyatı
     * @param profitPercentage İstenen kâr yüzdesi
     */
    fun calculateSellingPrice(costPrice: Double, profitPercentage: Double): Double {
        return costPrice * (1 + profitPercentage / 100)
    }

    /**
     * Maliyet Fiyatı Hesaplama (Satış fiyatı ve kâr yüzdesinden)
     * @param sellingPrice Satış fiyatı
     * @param profitPercentage Kâr yüzdesi
     */
    fun calculateCostPrice(sellingPrice: Double, profitPercentage: Double): Double {
        return sellingPrice / (1 + profitPercentage / 100)
    }

    /**
     * Vade/Taksit Hesaplama (Faizsiz)
     * @param totalAmount Toplam tutar
     * @param numberOfInstallments Taksit sayısı
     */
    fun calculateInstallment(totalAmount: Double, numberOfInstallments: Int): VadeResult {
        val installmentAmount = totalAmount / numberOfInstallments

        return VadeResult(
            totalAmount = totalAmount,
            numberOfInstallments = numberOfInstallments,
            installmentAmount = installmentAmount,
            interestRate = null,
            totalInterest = 0.0
        )
    }

    /**
     * Vade/Taksit Hesaplama (Faizli)
     * Aylık eşit taksit formülü (Annuity)
     * @param principal Ana para
     * @param monthlyRate Aylık faiz oranı (yüzde)
     * @param numberOfInstallments Taksit sayısı
     */
    fun calculateInstallmentWithInterest(
        principal: Double,
        monthlyRate: Double,
        numberOfInstallments: Int
    ): VadeResult {
        val rate = monthlyRate / 100.0

        val installmentAmount = if (rate == 0.0) {
            principal / numberOfInstallments
        } else {
            principal * (rate * (1 + rate).pow(numberOfInstallments)) /
                ((1 + rate).pow(numberOfInstallments) - 1)
        }

        val totalAmount = installmentAmount * numberOfInstallments
        val totalInterest = totalAmount - principal

        return VadeResult(
            totalAmount = totalAmount,
            numberOfInstallments = numberOfInstallments,
            installmentAmount = installmentAmount,
            interestRate = monthlyRate,
            totalInterest = totalInterest
        )
    }

    /**
     * İndirim Hesaplama
     * @param originalPrice Orijinal fiyat
     * @param discountPercentage İndirim yüzdesi
     */
    fun calculateDiscount(originalPrice: Double, discountPercentage: Double): Pair<Double, Double> {
        val discountAmount = originalPrice * (discountPercentage / 100)
        val finalPrice = originalPrice - discountAmount
        return Pair(discountAmount, finalPrice)
    }

    /**
     * Vergi Dilimi Hesaplama (Gelir Vergisi 2024)
     * Not: Bu oranlar değişebilir, güncel tutulmalı
     */
    fun calculateIncomeTax(annualIncome: Double): Double {
        // 2024 Türkiye gelir vergisi dilimleri (örnek)
        val brackets = listOf(
            110_000.0 to 0.15,
            230_000.0 to 0.20,
            580_000.0 to 0.27,
            3_000_000.0 to 0.35,
            Double.MAX_VALUE to 0.40
        )

        var remainingIncome = annualIncome
        var totalTax = 0.0
        var previousLimit = 0.0

        for ((limit, rate) in brackets) {
            if (remainingIncome <= 0) break

            val taxableInThisBracket = minOf(remainingIncome, limit - previousLimit)
            totalTax += taxableInThisBracket * rate
            remainingIncome -= taxableInThisBracket
            previousLimit = limit
        }

        return totalTax
    }

    /**
     * Format currency for display (Turkish Lira)
     */
    fun formatCurrency(amount: Double, currencySymbol: String = "₺"): String {
        return String.format("%,.2f %s", amount, currencySymbol)
    }

    /**
     * Format percentage for display
     */
    fun formatPercentage(value: Double): String {
        return String.format("%.2f%%", value)
    }
}

