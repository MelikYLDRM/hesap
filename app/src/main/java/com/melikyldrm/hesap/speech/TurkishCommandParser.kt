package com.melikyldrm.hesap.speech

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Türkçe sesli komutları matematiksel ifadelere dönüştürür
 */
@Singleton
class TurkishCommandParser @Inject constructor(
    private val numberParser: TurkishNumberParser
) {

    // Safe logging - Unit testlerde crash olmaz
    private fun logDebug(message: String) {
        try {
            android.util.Log.d("TurkishParser", message)
        } catch (_: Exception) {
            // Unit testlerde android.util.Log mevcut değil
            println("TurkishParser: $message")
        }
    }

    private fun logWarning(message: String) {
        try {
            android.util.Log.w("TurkishParser", message)
        } catch (_: Exception) {
            println("TurkishParser WARNING: $message")
        }
    }

    // İşlem kelimeleri
    private val operatorMappings = mapOf(
        // Toplama
        "artı" to "+", "ekle" to "+", "toplam" to "+", "ve" to "+",
        // Çıkarma
        "eksi" to "-", "çıkar" to "-", "çıkart" to "-", "fark" to "-",
        // Çarpma
        "çarpı" to "×", "kere" to "×", "defa" to "×", "kat" to "×",
        // Bölme
        "bölü" to "÷", "böl" to "÷",
        // Diğer
        "mod" to "%", "yüzde" to "%"
    )

    // Fonksiyon kelimeleri
    private val functionMappings = mapOf(
        "karekök" to "√", "kökü" to "√", "kök" to "√",
        "karesi" to "²", "kare" to "²",
        "küpü" to "³", "küp" to "³",
        "üssü" to "^", "üzeri" to "^",
        "sinüs" to "sin(", "sin" to "sin(",
        "kosinüs" to "cos(", "cos" to "cos(",
        "tanjant" to "tan(", "tan" to "tan(",
        "logaritma" to "log(", "log" to "log(",
        "doğal logaritma" to "ln(", "ln" to "ln(",
        "faktöriyel" to "!", "faktoriyel" to "!"
    )

    // Özel komutlar
    private val specialCommands = mapOf(
        "temizle" to "CLEAR",
        "sıfırla" to "CLEAR",
        "sil" to "DELETE",
        "geri" to "DELETE",
        "son işlemi sil" to "DELETE",
        "eşittir" to "EQUALS",
        "sonuç" to "EQUALS",
        "hesapla" to "EQUALS"
    )

    /**
     * Ana parse fonksiyonu - sesli komutu işler
     */
    fun parseCommand(spokenText: String): SpeechCommand {
        val cleanText = spokenText.lowercase().trim()

        // Önce özel komutları kontrol et
        specialCommands.forEach { (phrase, command) ->
            if (cleanText.contains(phrase)) {
                return when (command) {
                    "CLEAR" -> SpeechCommand.Clear
                    "DELETE" -> SpeechCommand.Delete
                    "EQUALS" -> SpeechCommand.Equals
                    else -> SpeechCommand.Unknown(spokenText)
                }
            }
        }

        // KDV komutunu kontrol et
        parseKdvCommand(cleanText)?.let { return it }

        // Tevkifat komutunu kontrol et
        parseTevkifatCommand(cleanText)?.let { return it }

        // Normal matematiksel ifadeyi parse et
        val expression = parseExpression(cleanText)

        // Geçerli bir matematiksel ifade mi kontrol et (en az bir operatör içermeli)
        val hasOperator = expression.contains(Regex("[+\\-*/^]"))

        return if (expression.isNotEmpty() && hasOperator) {
            SpeechCommand.Calculate(expression)
        } else {
            SpeechCommand.Unknown(spokenText)
        }
    }

    /**
     * Türkçe binlik ayırıcıları temizler (20.000 -> 20000, 1.000.000 -> 1000000)
     * Dikkat: Ondalık sayıları (3.5) korur
     */
    private fun removeTurkishThousandsSeparator(text: String): String {
        // Pattern: 1-3 basamak, ardından (.3basamak) grupları
        // Örnek: 20.000 -> 20000, 1.000.000 -> 1000000
        // Ama: 3.5 -> 3.5 (ondalık, 3 basamak değil)
        return text.replace(Regex("(\\d{1,3})(\\.\\d{3})+(?!\\d)")) { matchResult ->
            matchResult.value.replace(".", "")
        }
    }

    /**
     * "üç virgül beş" -> "3.5" gibi ondalık ifadeleri normalize eder.
     * Bu adım replaceNumberWords'ten SONRA çalışmalı.
     */
    private fun normalizeDecimals(text: String): String {
        var result = text

        // 1) "3 virgül 5" / "3,5" gibi formlar -> ondalık ayırıcı
        result = result
            .replace(Regex("\\s*(virgül|virgul)\\s*", RegexOption.IGNORE_CASE), ".")
            .replace(",", ".")

        // "nokta" kelimesi - eğer sayıdan sonra geliyorsa ondalık
        result = result.replace(Regex("\\s*nokta\\s*", RegexOption.IGNORE_CASE), ".")

        // 2) "3. 5" -> "3.5" gibi araya boşluk kaçarsa
        result = result.replace(Regex("(\\d+)\\.\\s+(\\d+)") , "$1.$2")

        return result
    }

    /**
     * Sesli metni matematiksel ifadeye dönüştürür
     */
    fun parseExpression(spokenText: String): String {
        var result = spokenText.lowercase().trim()
        logDebug("Step 0 - Input: '$result'")

        // Önce Türkçe binlik ayırıcıları temizle (20.000 -> 20000, 1.000.000 -> 1000000)
        // Bu adım replaceNumberWords'den ÖNCE yapılmalı
        result = removeTurkishThousandsSeparator(result)
        logDebug("Step 0.5 - After removing thousands separator: '$result'")

        // Önce sayı kelimelerini rakamlara çevir (işlem kelimeleri korunur)
        result = numberParser.replaceNumberWords(result)
        logDebug("Step 1 - After replaceNumberWords: '$result'")

        // Ondalıkları normalize et (virgül/nokta)
        result = normalizeDecimals(result)
        logDebug("Step 1.5 - After normalizeDecimals: '$result'")

        // İşlem kelimelerini sembollere çevir - boşluk opsiyonel (\\s* kullan)
        // Bölme (tüm varyasyonlar)
        result = result.replace(Regex("\\s*bölü\\s*", RegexOption.IGNORE_CASE), "/")
        result = result.replace(Regex("\\s*bolu\\s*", RegexOption.IGNORE_CASE), "/")
        result = result.replace(Regex("\\s*böl\\s*", RegexOption.IGNORE_CASE), "/")
        result = result.replace(Regex("\\s*bol\\s*", RegexOption.IGNORE_CASE), "/")

        // Çarpma (tüm varyasyonlar - Türkçe karaktersiz dahil)
        result = result.replace(Regex("\\s*çarpı\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*carpi\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*çarp\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*carp\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*kere\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*kez\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*defa\\s*", RegexOption.IGNORE_CASE), "*")
        result = result.replace(Regex("\\s*kat\\s*", RegexOption.IGNORE_CASE), "*")
        // "x" harfi de çarpma olabilir (boşluklarla çevrili veya sayılar arasında)
        result = result.replace(Regex("(\\d)\\s*x\\s*(\\d)", RegexOption.IGNORE_CASE), "$1*$2")
        result = result.replace(Regex("\\s+x\\s+", RegexOption.IGNORE_CASE), "*")

        // Toplama (tüm varyasyonlar)
        result = result.replace(Regex("\\s*artı\\s*", RegexOption.IGNORE_CASE), "+")
        result = result.replace(Regex("\\s*arti\\s*", RegexOption.IGNORE_CASE), "+")
        result = result.replace(Regex("\\s*ekle\\s*", RegexOption.IGNORE_CASE), "+")
        result = result.replace(Regex("\\s*topla\\s*", RegexOption.IGNORE_CASE), "+")
        result = result.replace(Regex("\\s*toplam\\s*", RegexOption.IGNORE_CASE), "+")
        // "+" sembolü de toplama olarak işlenmeli (boşluklarla çevrili)
        result = result.replace(Regex("(\\d)\\s+\\+\\s+(\\d)"), "$1+$2")

        // Çıkarma (tüm varyasyonlar)
        result = result.replace(Regex("\\s*çıkart\\s*", RegexOption.IGNORE_CASE), "-")
        result = result.replace(Regex("\\s*cikart\\s*", RegexOption.IGNORE_CASE), "-")
        result = result.replace(Regex("\\s*çıkar\\s*", RegexOption.IGNORE_CASE), "-")
        result = result.replace(Regex("\\s*cikar\\s*", RegexOption.IGNORE_CASE), "-")
        result = result.replace(Regex("\\s*eksi\\s*", RegexOption.IGNORE_CASE), "-")
        result = result.replace(Regex("\\s*fark\\s*", RegexOption.IGNORE_CASE), "-")
        // "-" sembolü de çıkarma olarak işlenmeli (boşluklarla çevrili)
        result = result.replace(Regex("(\\d)\\s+-\\s+(\\d)"), "$1-$2")

        logDebug("Step 2 - After operator replacement: '$result'")

        // Fonksiyon kelimelerini çevir
        functionMappings.forEach { (word, symbol) ->
            result = result.replace(Regex("\\b$word\\b", RegexOption.IGNORE_CASE), symbol)
        }

        // Parantez kelimelerini çevir
        result = result.replace(Regex("\\bparantez aç\\b", RegexOption.IGNORE_CASE), "(")
        result = result.replace(Regex("\\baç parantez\\b", RegexOption.IGNORE_CASE), "(")
        result = result.replace(Regex("\\bparantez kapat\\b", RegexOption.IGNORE_CASE), ")")
        result = result.replace(Regex("\\bkapat parantez\\b", RegexOption.IGNORE_CASE), ")")

        // "x'in karekökü" formatını işle
        result = result.replace(Regex("(\\d+)'?[iıuü]?n?\\s*karekökü"), "√$1")
        result = result.replace(Regex("(\\d+)'?[iıuü]?n?\\s*karesi"), "$1²")
        result = result.replace(Regex("(\\d+)'?[iıuü]?n?\\s*küpü"), "$1³")

        // "x üssü y" formatını işle
        result = result.replace(Regex("(\\d+)\\s*üssü\\s*(\\d+)"), "$1^$2")
        result = result.replace(Regex("(\\d+)\\s*üzeri\\s*(\\d+)"), "$1^$2")

        // Gereksiz boşlukları temizle
        result = result.replace(Regex("\\s+"), "")
        logDebug("Step 3 - After space removal: '$result'")

        // Sadece geçerli karakterleri tut (sayılar, operatörler, nokta)
        result = result.filter { it.isDigit() || it in "+-*/.()^√²³" }
        logDebug("Step 4 - Final result: '$result'")

        // GÜVENLİK KONTROLÜ: operatörsüz bir şey geldiyse logla
        if (result.isNotEmpty() && !result.contains(Regex("[+\\-*/^]"))) {
            logWarning("No operator found in result: '$result'")
        }

        return result
    }

    /**
     * KDV komutunu parse eder
     * Örnek: "bin liraya yüzde on sekiz kdv hesapla"
     */
    private fun parseKdvCommand(text: String): SpeechCommand? {
        // KDV kelimesi var mı kontrol et
        if (!text.contains("kdv") && !text.contains("katma değer")) {
            return null
        }

        // Tutarı bul
        val amountPattern = Regex("(\\d+(?:[.,]\\d+)?|[\\wşçöğüı\\s]+)\\s*(?:lira|tl|₺)?")
        val amountMatch = amountPattern.find(text)
        val amount = amountMatch?.let {
            it.groupValues[1].replace(",", ".").toDoubleOrNull()
                ?: numberParser.parseDecimal(it.groupValues[1])
        } ?: return null

        // KDV oranını bul
        val ratePattern = Regex("yüzde\\s*(\\d+|[\\wşçöğüı\\s]+)")
        val rateMatch = ratePattern.find(text)
        val rate = rateMatch?.let {
            it.groupValues[1].toIntOrNull()
                ?: numberParser.parseNumber(it.groupValues[1])?.toInt()
        } ?: 18 // Varsayılan %18

        // KDV dahil mi?
        val isIncluded = text.contains("dahil") || text.contains("içinde")

        return SpeechCommand.KdvCalculate(amount, rate, isIncluded)
    }

    /**
     * Tevkifat komutunu parse eder
     */
    private fun parseTevkifatCommand(text: String): SpeechCommand? {
        if (!text.contains("tevkifat")) {
            return null
        }

        // Tutarı bul
        val amountPattern = Regex("(\\d+(?:[.,]\\d+)?)")
        val amountMatch = amountPattern.find(text)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", ".")?.toDoubleOrNull()
            ?: return null

        // Tevkifat oranını bul (örn: "5/10", "beşte on")
        val tevkifatRate = when {
            text.contains("2/10") || text.contains("iki on") -> "2/10"
            text.contains("4/10") || text.contains("dört on") -> "4/10"
            text.contains("5/10") || text.contains("beş on") || text.contains("yarım") -> "5/10"
            text.contains("7/10") || text.contains("yedi on") -> "7/10"
            text.contains("9/10") || text.contains("dokuz on") -> "9/10"
            else -> "5/10" // Varsayılan
        }

        // KDV oranını bul
        val kdvRatePattern = Regex("yüzde\\s*(\\d+)")
        val kdvRateMatch = kdvRatePattern.find(text)
        val kdvRate = kdvRateMatch?.groupValues?.get(1)?.toIntOrNull() ?: 18

        return SpeechCommand.TevkifatCalculate(amount, kdvRate, tevkifatRate)
    }

    /**
     * İfadenin geçerli olup olmadığını kontrol eder
     */
    fun isValidExpression(expression: String): Boolean {
        if (expression.isBlank()) return false

        // Temel karakter kontrolü
        val validChars = Regex("^[0-9+\\-*/^√²³().%sincotalgexp\\s]+$")
        if (!validChars.matches(expression)) return false

        // Parantez dengesi
        var parenCount = 0
        for (char in expression) {
            when (char) {
                '(' -> parenCount++
                ')' -> parenCount--
            }
            if (parenCount < 0) return false
        }

        return parenCount == 0
    }

    /**
     * İfadeyi görüntülenebilir formata çevirir
     */
    fun formatForDisplay(expression: String): String {
        return expression
            .replace("*", "×")
            .replace("/", "÷")
            .replace("-", "−")
    }
}

