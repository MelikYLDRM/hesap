package com.melikyldrm.hesap.speech

import com.melikyldrm.hesap.domain.engine.CalculatorEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * End-to-end sesli hesap makinası testleri
 * Sesli komut -> Expression -> Hesaplama -> Sonuç
 */
class E2ESpeechCalculatorTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser
    private lateinit var calculatorEngine: CalculatorEngine

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
        calculatorEngine = CalculatorEngine()
    }

    private fun calculateFromSpeech(spokenText: String): Double? {
        val expression = commandParser.parseExpression(spokenText)
        println("Speech: '$spokenText' -> Expression: '$expression'")

        if (expression.isEmpty()) return null

        val result = calculatorEngine.evaluate(expression)
        return result.getOrNull()?.also {
            println("Result: $it")
        }
    }

    // ==================== TOPLAMA TESTLERİ ====================

    @Test
    fun `e2e toplama - basit`() {
        val result = calculateFromSpeech("iki artı üç")
        assertEquals(5.0, result!!, 0.0001)
    }

    @Test
    fun `e2e toplama - buyuk sayi`() {
        val result = calculateFromSpeech("bir milyon artı beşyüzbin")
        assertEquals(1500000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e toplama - kesirli`() {
        val result = calculateFromSpeech("üç virgül beş artı iki virgül beş")
        assertEquals(6.0, result!!, 0.0001)
    }

    // ==================== ÇIKARMA TESTLERİ ====================

    @Test
    fun `e2e cikarma - basit`() {
        val result = calculateFromSpeech("on eksi üç")
        assertEquals(7.0, result!!, 0.0001)
    }

    @Test
    fun `e2e cikarma - buyuk sayi`() {
        val result = calculateFromSpeech("bir milyon çıkar yüzbin")
        assertEquals(900000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e cikarma - kesirli`() {
        val result = calculateFromSpeech("beş nokta beş eksi iki nokta beş")
        assertEquals(3.0, result!!, 0.0001)
    }

    // ==================== ÇARPMA TESTLERİ ====================

    @Test
    fun `e2e carpma - basit`() {
        val result = calculateFromSpeech("beş çarpı altı")
        assertEquals(30.0, result!!, 0.0001)
    }

    @Test
    fun `e2e carpma - yuz doksan bes carpi elli iki`() {
        val result = calculateFromSpeech("yüzdoksanbeş çarpı elliiki")
        assertEquals(10140.0, result!!, 0.0001) // 195 * 52 = 10140
    }

    @Test
    fun `e2e carpma - buyuk sayi`() {
        val result = calculateFromSpeech("bin çarpı bin")
        assertEquals(1000000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e carpma - kesirli`() {
        val result = calculateFromSpeech("üç virgül beş çarpı iki")
        assertEquals(7.0, result!!, 0.0001)
    }

    @Test
    fun `e2e carpma - kere kelimesi`() {
        val result = calculateFromSpeech("on kere on")
        assertEquals(100.0, result!!, 0.0001)
    }

    // ==================== BÖLME TESTLERİ ====================

    @Test
    fun `e2e bolme - basit`() {
        val result = calculateFromSpeech("on bölü iki")
        assertEquals(5.0, result!!, 0.0001)
    }

    @Test
    fun `e2e bolme - buyuk sayi`() {
        val result = calculateFromSpeech("bir milyon bölü bin")
        assertEquals(1000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e bolme - kesirli sonuc`() {
        val result = calculateFromSpeech("on bölü üç")
        assertEquals(3.3333, result!!, 0.001)
    }

    @Test
    fun `e2e bolme - kesirli sayilar`() {
        val result = calculateFromSpeech("beş nokta beş bölü iki nokta iki")
        assertEquals(2.5, result!!, 0.0001)
    }

    // ==================== KARMAŞIK İFADELER ====================

    @Test
    fun `e2e karmasik - operator onceligi`() {
        // 10 + 20 * 3 = 10 + 60 = 70 (çarpma öncelikli)
        val result = calculateFromSpeech("on artı yirmi çarpı üç")
        assertEquals(70.0, result!!, 0.0001)
    }

    @Test
    fun `e2e karmasik - uc islem`() {
        // 100 - 50 + 25 = 75
        val result = calculateFromSpeech("yüz eksi elli artı yirmibeş")
        assertEquals(75.0, result!!, 0.0001)
    }

    @Test
    fun `e2e karmasik - dort islem`() {
        // 100 / 2 * 5 - 10 = 50 * 5 - 10 = 250 - 10 = 240
        val result = calculateFromSpeech("yüz bölü iki çarpı beş eksi on")
        assertEquals(240.0, result!!, 0.0001)
    }

    // ==================== EDGE CASES ====================

    @Test
    fun `e2e edge - sifir carpma`() {
        val result = calculateFromSpeech("sıfır çarpı bin")
        assertEquals(0.0, result!!, 0.0001)
    }

    @Test
    fun `e2e edge - bir bolme`() {
        val result = calculateFromSpeech("bin bölü bir")
        assertEquals(1000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e edge - cok buyuk sayi`() {
        val result = calculateFromSpeech("bir milyar çarpı iki")
        assertEquals(2000000000.0, result!!, 0.0001)
    }

    @Test
    fun `e2e edge - cok kucuk kesir`() {
        val result = calculateFromSpeech("bir bölü bin")
        assertEquals(0.001, result!!, 0.0000001)
    }

    // ==================== TÜRKÇE KARAKTERSİZ ====================

    @Test
    fun `e2e turkce_karaktersiz - carpma`() {
        val result = calculateFromSpeech("yuzdoksanbes carpi elliiki")
        assertEquals(10140.0, result!!, 0.0001)
    }

    @Test
    fun `e2e turkce_karaktersiz - bolme`() {
        val result = calculateFromSpeech("yuz bolu on")
        assertEquals(10.0, result!!, 0.0001)
    }

    // ==================== FORMAT SONUÇ TESTLERİ ====================

    @Test
    fun `format - tam sayi sonucu`() {
        val expression = commandParser.parseExpression("on çarpı on")
        val result = calculatorEngine.evaluate(expression).getOrNull()!!
        val formatted = calculatorEngine.formatResult(result)
        assertEquals("100", formatted)
    }

    @Test
    fun `format - kesirli sonuc`() {
        val expression = commandParser.parseExpression("on bölü dört")
        val result = calculatorEngine.evaluate(expression).getOrNull()!!
        val formatted = calculatorEngine.formatResult(result)
        assertEquals("2.5", formatted)
    }

    @Test
    fun `format - cok buyuk sayi`() {
        val expression = commandParser.parseExpression("bir milyar çarpı yüz")
        val result = calculatorEngine.evaluate(expression).getOrNull()!!
        val formatted = calculatorEngine.formatResult(result)
        // 100,000,000,000 = 1E11 formatında olmalı
        println("Formatted big number: $formatted")
        assertTrue(formatted.contains("E") || formatted == "100000000000")
    }
}

