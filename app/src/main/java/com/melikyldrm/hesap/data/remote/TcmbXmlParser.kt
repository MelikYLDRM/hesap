package com.melikyldrm.hesap.data.remote

import com.melikyldrm.hesap.domain.model.ExchangeRate
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * TCMB XML verilerini parse eden sınıf
 * TCMB kurları: 1 yabancı para = X TRY formatında
 */
@Singleton
class TcmbXmlParser @Inject constructor() {

    /**
     * TCMB XML verisini ExchangeRate listesine dönüştürür
     * @param xmlData TCMB'den alınan XML string
     * @return Döviz kurları listesi
     */
    fun parseRates(xmlData: String): List<ExchangeRate> {
        val rates = mutableListOf<ExchangeRate>()

        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = false
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xmlData))

            var eventType = parser.eventType
            var currentCurrencyCode: String? = null
            var currentUnit: Int = 1
            var forexBuying: Double? = null
            var forexSelling: Double? = null
            var banknoteBuying: Double? = null
            var banknoteSelling: Double? = null

            while (eventType != XmlPullParser.END_DOCUMENT) {
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        when (parser.name) {
                            "Currency" -> {
                                currentCurrencyCode = parser.getAttributeValue(null, "CurrencyCode")
                                currentUnit = 1
                                forexBuying = null
                                forexSelling = null
                                banknoteBuying = null
                                banknoteSelling = null
                            }
                            "Unit" -> {
                                currentUnit = parser.nextText().toIntOrNull() ?: 1
                            }
                            "ForexBuying" -> {
                                val text = parser.nextText().trim()
                                if (text.isNotEmpty()) {
                                    forexBuying = text.replace(",", ".").toDoubleOrNull()
                                }
                            }
                            "ForexSelling" -> {
                                val text = parser.nextText().trim()
                                if (text.isNotEmpty()) {
                                    forexSelling = text.replace(",", ".").toDoubleOrNull()
                                }
                            }
                            "BanknoteBuying" -> {
                                val text = parser.nextText().trim()
                                if (text.isNotEmpty()) {
                                    banknoteBuying = text.replace(",", ".").toDoubleOrNull()
                                }
                            }
                            "BanknoteSelling" -> {
                                val text = parser.nextText().trim()
                                if (text.isNotEmpty()) {
                                    banknoteSelling = text.replace(",", ".").toDoubleOrNull()
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "Currency" && currentCurrencyCode != null) {
                            // Öncelik: ForexSelling > ForexBuying > BanknoteSelling > BanknoteBuying
                            val rate = forexSelling ?: forexBuying ?: banknoteSelling ?: banknoteBuying

                            if (rate != null && rate > 0) {
                                // Unit'e göre normalize et (örn: JPY 100 birim için verilir)
                                val normalizedRate = rate / currentUnit

                                rates.add(
                                    ExchangeRate(
                                        baseCurrency = currentCurrencyCode,
                                        targetCurrency = "TRY",
                                        rate = normalizedRate,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                            }

                            currentCurrencyCode = null
                        }
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return rates
    }

    /**
     * İki para birimi arasındaki çapraz kuru hesaplar
     * @param rates TRY bazlı kurlar listesi
     * @param fromCurrency Kaynak para birimi
     * @param toCurrency Hedef para birimi
     * @return Çapraz kur (null ise hesaplanamadı)
     */
    fun calculateCrossRate(
        rates: List<ExchangeRate>,
        fromCurrency: String,
        toCurrency: String
    ): Double? {
        if (fromCurrency == toCurrency) return 1.0

        // TRY'den başka bir para birimine
        if (fromCurrency == "TRY") {
            val toRate = rates.find { it.baseCurrency == toCurrency }?.rate
            return if (toRate != null && toRate > 0) 1.0 / toRate else null
        }

        // Başka bir para biriminden TRY'ye
        if (toCurrency == "TRY") {
            return rates.find { it.baseCurrency == fromCurrency }?.rate
        }

        // İki yabancı para birimi arasında çapraz kur
        val fromRate = rates.find { it.baseCurrency == fromCurrency }?.rate ?: return null
        val toRate = rates.find { it.baseCurrency == toCurrency }?.rate ?: return null

        return if (toRate > 0) fromRate / toRate else null
    }
}

