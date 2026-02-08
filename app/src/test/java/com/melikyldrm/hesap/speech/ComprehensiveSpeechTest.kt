package com.melikyldrm.hesap.speech

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Kapsamlı sesli hesap makinesi testleri
 * Büyük sayılar, küçük sayılar, kesirli sayılar ve 4 işlem
 */
class ComprehensiveSpeechTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
    }

    // ==================== BÜYÜK SAYILAR ====================

    @Test
    fun `buyuk sayi - bin carpma`() {
        val result = commandParser.parseExpression("bin çarpı bin")
        assertEquals("1000*1000", result)
    }

    @Test
    fun `buyuk sayi - milyon toplama`() {
        val result = commandParser.parseExpression("bir milyon artı beşyüzbin")
        assertEquals("1000000+500000", result)
    }

    @Test
    fun `buyuk sayi - onbin cikarma`() {
        val result = commandParser.parseExpression("onbin eksi beşbin")
        assertEquals("10000-5000", result)
    }

    @Test
    fun `buyuk sayi - yuzbin bolme`() {
        val result = commandParser.parseExpression("yüzbin bölü bin")
        assertEquals("100000/1000", result)
    }

    // ==================== KÜÇÜK SAYILAR ====================

    @Test
    fun `kucuk sayi - bir carpma`() {
        val result = commandParser.parseExpression("bir çarpı bir")
        assertEquals("1*1", result)
    }

    @Test
    fun `kucuk sayi - iki toplama`() {
        val result = commandParser.parseExpression("iki artı üç")
        assertEquals("2+3", result)
    }

    @Test
    fun `kucuk sayi - on cikarma`() {
        val result = commandParser.parseExpression("on eksi beş")
        assertEquals("10-5", result)
    }

    @Test
    fun `kucuk sayi - alti bolme`() {
        val result = commandParser.parseExpression("altı bölü iki")
        assertEquals("6/2", result)
    }

    // ==================== KESİRLİ SAYILAR ====================

    @Test
    fun `kesirli - virgullu sayi`() {
        val result = commandParser.parseExpression("üç virgül beş çarpı iki")
        assertEquals("actual=$result", "3.5*2", result)
    }

    @Test
    fun `kesirli - nokta kelimesi`() {
        val result = commandParser.parseExpression("üç nokta beş çarpı iki")
        assertEquals("actual=$result", "3.5*2", result)
    }

    @Test
    fun `kesirli - numeric nokta`() {
        val result = commandParser.parseExpression("3.5 çarpı 2")
        assertEquals("3.5*2", result)
    }

    @Test
    fun `kesirli - numeric virgul`() {
        val result = commandParser.parseExpression("3,5 çarpı 2")
        assertEquals("3.5*2", result)
    }

    // ==================== 4 İŞLEM DETAYLI ====================

    @Test
    fun `carpma - farkli kelimeler`() {
        val tests = listOf(
            "beş çarpı altı" to "5*6",
            "beş kere altı" to "5*6",
            "beş kez altı" to "5*6",
            "beş defa altı" to "5*6",
            "bes carpi alti" to "5*6" // Türkçe karaktersiz
        )

        for ((input, expected) in tests) {
            val result = commandParser.parseExpression(input)
            assertEquals("Failed for: $input", expected, result)
        }
    }

    @Test
    fun `toplama - farkli kelimeler`() {
        val tests = listOf(
            "beş artı altı" to "5+6",
            "beş ekle altı" to "5+6",
            "beş topla altı" to "5+6",
            "bes arti alti" to "5+6" // Türkçe karaktersiz
        )

        for ((input, expected) in tests) {
            val result = commandParser.parseExpression(input)
            assertEquals("Failed for: $input", expected, result)
        }
    }

    @Test
    fun `cikarma - farkli kelimeler`() {
        val tests = listOf(
            "on eksi beş" to "10-5",
            "on çıkar beş" to "10-5",
            "on fark beş" to "10-5",
            "on cikar bes" to "10-5" // Türkçe karaktersiz
        )

        for ((input, expected) in tests) {
            val result = commandParser.parseExpression(input)
            assertEquals("Failed for: $input", expected, result)
        }
    }

    @Test
    fun `bolme - farkli kelimeler`() {
        val tests = listOf(
            "on bölü iki" to "10/2",
            "on böl iki" to "10/2",
            "on bolu iki" to "10/2" // Türkçe karaktersiz
        )

        for ((input, expected) in tests) {
            val result = commandParser.parseExpression(input)
            assertEquals("Failed for: $input", expected, result)
        }
    }

    // ==================== KULLANICI SENARYOSU ====================

    @Test
    fun `kullanici senaryosu - 195 carpi 52`() {
        val result = commandParser.parseExpression("yüzdoksanbeş çarpı elliiki")
        assertEquals("195*52", result)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `edge case - sifir`() {
        val result = commandParser.parseExpression("sıfır artı beş")
        assertEquals("0+5", result)
    }

    @Test
    fun `edge case - bosluklarla`() {
        val result = commandParser.parseExpression("yüz doksan beş çarpı elli iki")
        assertEquals("195*52", result)
    }
}
