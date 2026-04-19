package com.melikyldrm.hesap.domain.engine

import com.melikyldrm.hesap.domain.model.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class FinanceCalculatorTest {

    private lateinit var calculator: FinanceCalculator

    @Before
    fun setup() {
        calculator = FinanceCalculator()
    }

    // ========== KDV ==========

    @Test
    fun `kdv hariç hesaplama - rate 18`() {
        val result = calculator.calculateKdv(1000.0, KdvRate.RATE_18, isKdvIncluded = false)
        assertEquals(1000.0, result.baseAmount, 0.01)
        assertEquals(180.0, result.kdvAmount, 0.01)
        assertEquals(1180.0, result.totalAmount, 0.01)
    }

    @Test
    fun `kdv dahil hesaplama - rate 18`() {
        val result = calculator.calculateKdv(1180.0, KdvRate.RATE_18, isKdvIncluded = true)
        assertEquals(1000.0, result.baseAmount, 0.01)
        assertEquals(180.0, result.kdvAmount, 0.01)
        assertEquals(1180.0, result.totalAmount, 0.01)
    }

    @Test
    fun `kdv hariç hesaplama - rate 10`() {
        val result = calculator.calculateKdv(500.0, KdvRate.RATE_10, isKdvIncluded = false)
        assertEquals(500.0, result.baseAmount, 0.01)
        assertEquals(50.0, result.kdvAmount, 0.01)
        assertEquals(550.0, result.totalAmount, 0.01)
    }

    // ========== Basit Faiz ==========

    @Test
    fun `basit faiz hesaplama`() {
        val result = calculator.calculateSimpleInterest(
            principal = 10000.0,
            annualRate = 10.0,
            timeInYears = 2.0
        )
        assertEquals(10000.0, result.principal, 0.01)
        assertEquals(2000.0, result.interest, 0.01)
        assertEquals(12000.0, result.totalAmount, 0.01)
        assertFalse(result.isCompound)
    }

    // ========== Bilesik Faiz ==========

    @Test
    fun `bilesik faiz hesaplama - yillik`() {
        val result = calculator.calculateCompoundInterest(
            principal = 10000.0,
            annualRate = 10.0,
            timeInYears = 2.0,
            frequency = CompoundFrequency.ANNUALLY
        )
        assertEquals(10000.0, result.principal, 0.01)
        assertEquals(12100.0, result.totalAmount, 0.01)
        assertEquals(2100.0, result.interest, 0.01)
        assertTrue(result.isCompound)
    }

    // ========== Kar/Zarar ==========

    @Test
    fun `kar hesaplama`() {
        val result = calculator.calculateProfitLoss(100.0, 150.0)
        assertTrue(result.isProfit)
        assertEquals(50.0, result.profitOrLoss, 0.01)
        assertEquals(50.0, result.percentage, 0.01)
    }

    @Test
    fun `zarar hesaplama`() {
        val result = calculator.calculateProfitLoss(100.0, 80.0)
        assertFalse(result.isProfit)
        assertEquals(-20.0, result.profitOrLoss, 0.01)
        assertEquals(-20.0, result.percentage, 0.01)
    }

    @Test
    fun `sifir maliyet ile kar zarar`() {
        val result = calculator.calculateProfitLoss(0.0, 50.0)
        assertEquals(0.0, result.percentage, 0.01)
    }

    // ========== Taksit ==========

    @Test
    fun `faizsiz taksit hesaplama`() {
        val result = calculator.calculateInstallment(1200.0, 12)
        assertEquals(100.0, result.installmentAmount, 0.01)
        assertEquals(0.0, result.totalInterest, 0.01)
    }

    @Test
    fun `faizli taksit hesaplama`() {
        val result = calculator.calculateInstallmentWithInterest(
            principal = 10000.0,
            monthlyRate = 2.0,
            numberOfInstallments = 12
        )
        assertTrue(result.installmentAmount > 10000.0 / 12)
        assertTrue(result.totalInterest > 0)
    }

    @Test
    fun `sifir faizli taksit hesaplama`() {
        val result = calculator.calculateInstallmentWithInterest(
            principal = 12000.0,
            monthlyRate = 0.0,
            numberOfInstallments = 12
        )
        assertEquals(1000.0, result.installmentAmount, 0.01)
    }

    // ========== Indirim ==========

    @Test
    fun `indirim hesaplama`() {
        val (discountAmount, finalPrice) = calculator.calculateDiscount(200.0, 25.0)
        assertEquals(50.0, discountAmount, 0.01)
        assertEquals(150.0, finalPrice, 0.01)
    }

    // ========== Satis ve Maliyet ==========

    @Test
    fun `satis fiyati hesaplama`() {
        assertEquals(120.0, calculator.calculateSellingPrice(100.0, 20.0), 0.01)
    }

    @Test
    fun `maliyet fiyati hesaplama`() {
        assertEquals(100.0, calculator.calculateCostPrice(120.0, 20.0), 0.01)
    }

    // ========== Format ==========

    @Test
    fun `formatCurrency works`() {
        val formatted = calculator.formatCurrency(1234.56)
        assertTrue(formatted.contains("1"))
        assertTrue(formatted.contains("234"))
    }

    @Test
    fun `formatPercentage works`() {
        val formatted = calculator.formatPercentage(25.5)
        assertTrue(formatted.contains("25.50") || formatted.contains("25,50"))
    }
}

