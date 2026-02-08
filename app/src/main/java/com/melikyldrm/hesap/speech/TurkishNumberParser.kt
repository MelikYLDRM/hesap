package com.melikyldrm.hesap.speech

import javax.inject.Inject
import javax.inject.Singleton

/**
 * Türkçe sayı kelimelerini sayısal değerlere dönüştürür
 * Örnek: "yüz yirmi üç" -> 123
 * Örnek: "binyediyüzelli" -> 1750
 */
@Singleton
class TurkishNumberParser @Inject constructor() {

    // Türkçe karakter normalizasyonu
    private fun normalizeText(text: String): String {
        return text.lowercase()
            .replace("ı", "i")
            .replace("ğ", "g")
            .replace("ü", "u")
            .replace("ş", "s")
            .replace("ö", "o")
            .replace("ç", "c")
    }

    private val ones = mapOf(
        "sifir" to 0L, "sıfır" to 0L,
        "bir" to 1L,
        "iki" to 2L,
        "uc" to 3L, "üç" to 3L,
        "dort" to 4L, "dört" to 4L,
        "bes" to 5L, "beş" to 5L,
        "alti" to 6L, "altı" to 6L,
        "yedi" to 7L,
        "sekiz" to 8L,
        "dokuz" to 9L
    )

    private val tens = mapOf(
        "on" to 10L,
        "yirmi" to 20L,
        "otuz" to 30L,
        "kirk" to 40L, "kırk" to 40L,
        "elli" to 50L,
        "altmis" to 60L, "altmış" to 60L,
        "yetmis" to 70L, "yetmiş" to 70L,
        "seksen" to 80L,
        "doksan" to 90L
    )

    private val multipliers = mapOf(
        "yuz" to 100L, "yüz" to 100L,
        "bin" to 1_000L,
        "milyon" to 1_000_000L,
        "milyar" to 1_000_000_000L,
        "trilyon" to 1_000_000_000_000L
    )

    // İşlem kelimeleri (uzun olanlar önce - çıkart/çıkar sıralaması önemli)
    private val operatorWords = listOf(
        // Ondalık ayırıcılar (replaceNumberWords sırasında sayıyı flush etmek için)
        "virgül", "virgul", "nokta",
        // Çıkarma (uzun olanlar önce)
        "çıkart", "cikart", "çıkar", "cikar",
        // Toplama
        "toplam", "topla", "artı", "arti", "ekle",
        // Diğer çıkarma
        "eksi", "fark",
        // Çarpma
        "çarpı", "carpi", "çarp", "carp", "kere", "defa", "kat", "kez",
        // Bölme
        "bölü", "bolu", "böl", "bol",
        // Matematiksel semboller
        "+", "-", "x", "*", "/"
    )

    // Tüm sayı kelimeleri (uzundan kısaya sıralı)
    private val allNumberWords = listOf(
        // Büyük çarpanlar
        "trilyon", "milyar", "milyon",
        // Onlar (uzun olanlar önce - altmış/altı, yetmiş/yedi, seksen/sekiz çakışması için)
        "doksan", "seksen",
        "yetmis", "yetmiş",
        "altmis", "altmış",
        "elli",
        "kirk", "kırk",
        "otuz", "yirmi",
        // Çarpanlar
        "bin",
        "yuz", "yüz",
        // On (tek başına)
        "on",
        // Birler (uzun olanlar önce)
        "sifir", "sıfır",
        "dokuz", "sekiz", "yedi",
        "alti", "altı",
        "bes", "beş",
        "dort", "dört",
        "uc", "üç",
        "iki", "bir"
    )

    private val decimalRegex = Regex("^\\d+(?:[.,]\\d+)?")

    /**
     * Birleşik yazılmış metni token'lara ayırır (soldan sağa tarama)
     */
    private fun tokenize(text: String): List<String> {
        var remaining = text.lowercase().trim()
        val tokens = mutableListOf<String>()

        while (remaining.isNotEmpty()) {
            if (remaining.startsWith(" ")) {
                remaining = remaining.substring(1)
                continue
            }

            var found = false

            // 1) Önce sayısal (ondalıklı dahil) yakala: 3.5 / 3,5 / 195
            val digitMatch = decimalRegex.find(remaining)
            if (digitMatch != null) {
                tokens.add(digitMatch.value)
                remaining = remaining.substring(digitMatch.value.length)
                found = true
            }

            // 2) Operatör kelimelerini kontrol et (uzundan kısaya sıralı)
            if (!found) {
                for (op in operatorWords) {
                    if (remaining.startsWith(op)) {
                        tokens.add(op)
                        remaining = remaining.substring(op.length)
                        found = true
                        break
                    }
                }
            }

            // 3) Sayı kelimelerini kontrol et (uzundan kısaya sıralı)
            if (!found) {
                for (word in allNumberWords) {
                    if (remaining.startsWith(word)) {
                        tokens.add(word)
                        remaining = remaining.substring(word.length)
                        found = true
                        break
                    }
                }
            }

            if (!found && remaining.isNotEmpty()) {
                // Bilinmeyen karakter - atla
                remaining = remaining.substring(1)
            }
        }

        return tokens
    }

    /**
     * Token listesini sayıya dönüştürür
     */
    private fun parseTokens(tokens: List<String>): Long {
        if (tokens.isEmpty()) return 0L

        var result = 0L
        var current = 0L

        for (token in tokens) {
            when {
                ones.containsKey(token) -> current += ones[token]!!
                tens.containsKey(token) -> current += tens[token]!!
                token == "yüz" || token == "yuz" -> {
                    if (current == 0L) current = 1L
                    current *= 100L
                }
                token == "bin" -> {
                    if (current == 0L) current = 1L
                    current *= 1000L
                    result += current
                    current = 0L
                }
                token == "milyon" -> {
                    if (current == 0L) current = 1L
                    current *= 1_000_000L
                    result += current
                    current = 0L
                }
                token == "milyar" -> {
                    if (current == 0L) current = 1L
                    current *= 1_000_000_000L
                    result += current
                    current = 0L
                }
                token == "trilyon" -> {
                    if (current == 0L) current = 1L
                    current *= 1_000_000_000_000L
                    result += current
                    current = 0L
                }
                else -> token.toLongOrNull()?.let { current += it }
            }
        }

        return result + current
    }

    /**
     * Türkçe sayı ifadesini sayısal değere dönüştürür
     */
    fun parseNumber(text: String): Long? {
        val cleanText = text.lowercase().trim()
        cleanText.toLongOrNull()?.let { return it }

        val tokens = tokenize(cleanText)
        if (tokens.any { operatorWords.contains(it) }) return null
        if (tokens.isEmpty()) return null

        return try {
            val result = parseTokens(tokens.filterNot { it.contains('.') || it.contains(',') })
            if (result >= 0) result else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Ondalıklı sayıları parse eder
     */
    fun parseDecimal(text: String): Double? {
        val cleanText = text.lowercase().trim()
        cleanText.replace(",", ".").toDoubleOrNull()?.let { return it }

        val parts = cleanText.split("virgül", "virgul", "nokta").map { it.trim() }

        if (parts.size == 1) return parseNumber(parts[0])?.toDouble()
        if (parts.size == 2) {
            val integerPart = parseNumber(parts[0]) ?: return null
            val decimalValue = parseDecimalPart(parts[1]) ?: return null
            return integerPart + decimalValue
        }

        return null
    }

    private fun parseDecimalPart(text: String): Double? {
        val tokens = tokenize(text)
        val digits = tokens.mapNotNull { token ->
            ones[token]?.toString() ?: token.toLongOrNull()?.toString()
        }

        if (digits.size == tokens.size && digits.isNotEmpty()) {
            val decimalString = "0." + digits.joinToString("")
            return decimalString.toDoubleOrNull()
        }

        val number = parseNumber(text) ?: return null
        val decimalPlaces = number.toString().length
        return number.toDouble() / Math.pow(10.0, decimalPlaces.toDouble())
    }

    /**
     * Metindeki Türkçe sayı kelimelerini rakama çevirir
     * Örnek: "binyediyüzelli çarpı kırkbeş" -> "1750 çarpı 45"
     */
    fun replaceNumberWords(text: String): String {
        val tokens = tokenize(text.lowercase().trim())
        if (tokens.isEmpty()) return text

        val resultParts = mutableListOf<String>()
        val currentNumberTokens = mutableListOf<String>()

        fun flushCurrentNumberTokens() {
            if (currentNumberTokens.isEmpty()) return

            // Eğer token'lar içinde ondalık bir sayı varsa, Long parse etmeyelim
            if (currentNumberTokens.size == 1 && (currentNumberTokens[0].contains('.') || currentNumberTokens[0].contains(','))) {
                resultParts.add(currentNumberTokens[0].replace(",", "."))
                currentNumberTokens.clear()
                return
            }

            val number = parseTokens(currentNumberTokens)
            if (number > 0) {
                resultParts.add(number.toString())
            } else if (currentNumberTokens.any { it == "sıfır" || it == "sifir" }) {
                resultParts.add("0")
            }
            currentNumberTokens.clear()
        }

        for (token in tokens) {
            if (operatorWords.contains(token)) {
                flushCurrentNumberTokens()
                resultParts.add(token)
            } else if (isNumberToken(token)) {
                // ondalık token geldi ise önce biriken sayıyı flush et
                if (token.contains('.') || token.contains(',')) {
                    flushCurrentNumberTokens()
                    resultParts.add(token.replace(",", "."))
                } else {
                    currentNumberTokens.add(token)
                }
            } else {
                flushCurrentNumberTokens()
                resultParts.add(token)
            }
        }

        flushCurrentNumberTokens()
        return resultParts.joinToString(" ")
    }

    private fun isNumberToken(token: String): Boolean {
        return ones.containsKey(token) ||
            tens.containsKey(token) ||
            multipliers.containsKey(token) ||
            token.toLongOrNull() != null ||
            token.replace(",", ".").toDoubleOrNull() != null
    }

    fun containsNumber(text: String): Boolean {
        val cleanText = text.lowercase()
        return ones.keys.any { cleanText.contains(it) } ||
            tens.keys.any { cleanText.contains(it) } ||
            multipliers.keys.any { cleanText.contains(it) } ||
            cleanText.contains(Regex("\\d+"))
    }
}
