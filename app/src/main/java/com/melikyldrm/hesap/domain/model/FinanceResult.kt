package com.melikyldrm.hesap.domain.model

// KDV Calculation Result
data class KdvResult(
    val baseAmount: Double,         // KDV hariç tutar
    val kdvRate: Double,            // KDV oranı (örn: 0.18)
    val kdvAmount: Double,          // KDV tutarı
    val totalAmount: Double,        // KDV dahil toplam
    val isKdvIncluded: Boolean      // Girilen tutar KDV dahil mi?
)

// Tevkifat Calculation Result
data class TevkifatResult(
    val baseAmount: Double,         // Matrah
    val kdvRate: Double,            // KDV oranı
    val kdvAmount: Double,          // Hesaplanan KDV
    val tevkifatRate: String,       // Tevkifat oranı (örn: "5/10")
    val tevkifatAmount: Double,     // Tevkifat tutarı (Alıcının ödeyeceği KDV)
    val sellerKdv: Double,          // Satıcının tahsil edeceği KDV
    val totalAmount: Double         // Toplam tutar
)

// Interest Calculation Result
data class FaizResult(
    val principal: Double,          // Ana para
    val rate: Double,               // Faiz oranı (yıllık)
    val time: Double,               // Süre (yıl)
    val compoundFrequency: Int?,    // Bileşik faiz için dönem sayısı (null = basit faiz)
    val interest: Double,           // Faiz tutarı
    val totalAmount: Double,        // Toplam (ana para + faiz)
    val isCompound: Boolean         // Bileşik faiz mi?
)

// Profit/Loss Calculation Result
data class KarZararResult(
    val costPrice: Double,          // Maliyet fiyatı
    val sellingPrice: Double,       // Satış fiyatı
    val profitOrLoss: Double,       // Kâr veya zarar tutarı
    val percentage: Double,         // Kâr/Zarar yüzdesi
    val isProfit: Boolean           // true = kâr, false = zarar
)

// Installment/Term Calculation Result
data class VadeResult(
    val totalAmount: Double,        // Toplam tutar
    val numberOfInstallments: Int,  // Taksit sayısı
    val installmentAmount: Double,  // Taksit tutarı
    val interestRate: Double?,      // Faiz oranı (varsa)
    val totalInterest: Double       // Toplam faiz
)

// KDV Rates available in Turkey
enum class KdvRate(val rate: Double, val displayName: String) {
    RATE_1(0.01, "%1"),
    RATE_8(0.08, "%8"),
    RATE_10(0.10, "%10"),
    RATE_18(0.18, "%18"),
    RATE_20(0.20, "%20")
}

// Tevkifat Rates
enum class TevkifatRate(val numerator: Int, val denominator: Int, val displayName: String) {
    RATE_1_10(1, 10, "1/10"),
    RATE_2_10(2, 10, "2/10"),
    RATE_4_10(4, 10, "4/10"),
    RATE_5_10(5, 10, "5/10"),
    RATE_7_10(7, 10, "7/10"),
    RATE_9_10(9, 10, "9/10");

    val rate: Double get() = numerator.toDouble() / denominator.toDouble()
}

// Compound Interest Frequency
enum class CompoundFrequency(val periodsPerYear: Int, val displayName: String) {
    ANNUALLY(1, "Yıllık"),
    SEMI_ANNUALLY(2, "6 Aylık"),
    QUARTERLY(4, "3 Aylık"),
    MONTHLY(12, "Aylık"),
    DAILY(365, "Günlük")
}

