package com.melikyldrm.hesap.domain.model

data class ExchangeRate(
    val baseCurrency: String,
    val targetCurrency: String,
    val rate: Double,
    val timestamp: Long
)

data class ExchangeRatesResponse(
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)

enum class Currency(val code: String, val symbol: String, val displayName: String) {
    TRY("TRY", "₺", "Türk Lirası"),
    USD("USD", "$", "Amerikan Doları"),
    EUR("EUR", "€", "Euro"),
    GBP("GBP", "£", "İngiliz Sterlini"),
    JPY("JPY", "¥", "Japon Yeni"),
    CHF("CHF", "CHF", "İsviçre Frangı"),
    CAD("CAD", "C$", "Kanada Doları"),
    AUD("AUD", "A$", "Avustralya Doları"),
    SAR("SAR", "﷼", "Suudi Arabistan Riyali"),
    AED("AED", "د.إ", "BAE Dirhemi"),
    CNY("CNY", "¥", "Çin Yuanı"),
    RUB("RUB", "₽", "Rus Rublesi"),
    INR("INR", "₹", "Hint Rupisi"),
    KRW("KRW", "₩", "Güney Kore Wonu"),
    SEK("SEK", "kr", "İsveç Kronu"),
    NOK("NOK", "kr", "Norveç Kronu"),
    DKK("DKK", "kr", "Danimarka Kronu"),
    PLN("PLN", "zł", "Polonya Zlotisi"),
    BGN("BGN", "лв", "Bulgar Levası"),
    RON("RON", "lei", "Romen Leyi")
}

