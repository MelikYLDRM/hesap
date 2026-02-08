package com.melikyldrm.hesap.speech

import com.melikyldrm.hesap.domain.engine.CalculatorEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Gerçek kullanıcı senaryoları için test
 * Google Speech API'den gelebilecek farklı formatları simüle eder
 */
class RealWorldSpeechTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser
    private lateinit var calculatorEngine: CalculatorEngine

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
        calculatorEngine = CalculatorEngine()
    }

    private fun testSpeechToResult(spokenText: String, expectedResult: Double, tolerance: Double = 0.0001) {
        val expression = commandParser.parseExpression(spokenText)
        println("Speech: '$spokenText'")
        println("Expression: '$expression'")

        if (expression.isEmpty()) {
            fail("Expression is empty for: '$spokenText'")
            return
        }

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Result: $value (expected: $expectedResult)")
                assertEquals("Result mismatch for: '$spokenText'", expectedResult, value, tolerance)
            },
            onFailure = { error ->
                fail("Calculation failed for: '$spokenText' with error: ${error.message}")
            }
        )
        println("---")
    }

    // ==================== GOOGLE SPEECH VARYASYONLARI ====================

    @Test
    fun `google - basit toplama varyasyonlari`() {
        // Google bazen kelimeleri birleşik, bazen ayrı verebilir
        testSpeechToResult("iki artı üç", 5.0)
        testSpeechToResult("2 artı 3", 5.0)
        testSpeechToResult("iki + üç", 5.0)
    }

    @Test
    fun `google - carpma varyasyonlari`() {
        testSpeechToResult("beş çarpı altı", 30.0)
        testSpeechToResult("5 çarpı 6", 30.0)
        testSpeechToResult("beş kere altı", 30.0)
        testSpeechToResult("5 kere 6", 30.0)
        testSpeechToResult("beş x altı", 30.0) // x harfi çarpma
    }

    @Test
    fun `google - bolme varyasyonlari`() {
        testSpeechToResult("on bölü iki", 5.0)
        testSpeechToResult("10 bölü 2", 5.0)
        testSpeechToResult("on böl iki", 5.0)
    }

    @Test
    fun `google - cikarma varyasyonlari`() {
        testSpeechToResult("on eksi beş", 5.0)
        testSpeechToResult("10 eksi 5", 5.0)
        testSpeechToResult("on çıkar beş", 5.0)
        testSpeechToResult("on - beş", 5.0)
    }

    // ==================== BUYUK SAYILAR ====================

    @Test
    fun `buyuk - milyon islemleri`() {
        testSpeechToResult("bir milyon artı bir milyon", 2000000.0)
        testSpeechToResult("beş milyon bölü beş", 1000000.0)
        testSpeechToResult("iki milyon çarpı üç", 6000000.0)
    }

    @Test
    fun `buyuk - milyar islemleri`() {
        testSpeechToResult("bir milyar bölü bin", 1000000.0)
        testSpeechToResult("iki milyar artı bir milyar", 3000000000.0)
    }

    @Test
    fun `buyuk - karisik buyuk sayilar`() {
        testSpeechToResult("beşyüzbin artı yüzbin", 600000.0)
        testSpeechToResult("bir milyon beşyüzbin çıkar beşyüzbin", 1000000.0)
    }

    // ==================== KÜÇÜK SAYILAR ====================

    @Test
    fun `kucuk - tek basamakli`() {
        testSpeechToResult("bir artı bir", 2.0)
        testSpeechToResult("iki çarpı iki", 4.0)
        testSpeechToResult("dokuz bölü üç", 3.0)
    }

    @Test
    fun `kucuk - sifirli islemler`() {
        testSpeechToResult("sıfır artı beş", 5.0)
        testSpeechToResult("beş çarpı sıfır", 0.0)
        testSpeechToResult("sıfır bölü beş", 0.0)
    }

    // ==================== KESİRLİ SAYILAR ====================

    @Test
    fun `kesirli - virgul ile`() {
        testSpeechToResult("üç virgül beş artı iki virgül beş", 6.0)
        testSpeechToResult("beş virgül beş çarpı iki", 11.0)
    }

    @Test
    fun `kesirli - nokta ile`() {
        testSpeechToResult("üç nokta beş artı iki nokta beş", 6.0)
    }

    @Test
    fun `kesirli - numeric`() {
        testSpeechToResult("3.5 artı 2.5", 6.0)
        testSpeechToResult("3,5 artı 2,5", 6.0)
    }

    @Test
    fun `kesirli - bolme sonucu`() {
        testSpeechToResult("on bölü üç", 3.3333, 0.001)
        testSpeechToResult("yedi bölü iki", 3.5)
    }

    // ==================== KARMAŞIK İFADELER ====================

    @Test
    fun `karmasik - operator onceligi`() {
        // Matematik kuralları: çarpma/bölme önce
        testSpeechToResult("on artı beş çarpı iki", 20.0) // 10 + (5*2) = 20
        testSpeechToResult("yirmi eksi on bölü iki", 15.0) // 20 - (10/2) = 15
    }

    @Test
    fun `karmasik - coklu islem`() {
        testSpeechToResult("beş artı beş artı beş", 15.0)
        testSpeechToResult("yüz eksi elli eksi yirmibeş", 25.0)
    }

    // ==================== TÜRKÇE KARAKTERSİZ ====================

    @Test
    fun `turkce_karaktersiz - tum islemler`() {
        testSpeechToResult("uc arti bes", 8.0)
        testSpeechToResult("on eksi uc", 7.0)
        testSpeechToResult("dort carpi dort", 16.0)
        testSpeechToResult("yirmi bolu dort", 5.0)
    }

    // ==================== SINIR DURUMLAR ====================

    @Test
    fun `sinir - bir ile bolme`() {
        testSpeechToResult("bin bölü bir", 1000.0)
        testSpeechToResult("milyon bölü bir", 1000000.0)
    }

    @Test
    fun `sinir - sifir ile carpma`() {
        testSpeechToResult("milyar çarpı sıfır", 0.0)
    }

    @Test
    fun `sinir - kendisiyle cikarma`() {
        testSpeechToResult("yüz eksi yüz", 0.0)
    }

    @Test
    fun `sinir - kendisiyle bolme`() {
        testSpeechToResult("elli bölü elli", 1.0)
    }

    // ==================== ÖZEL DURUMLAR ====================

    @Test
    fun `ozel - yuz doksan bes carpi elli iki`() {
        // Bu kullanıcının belirttiği senaryo
        testSpeechToResult("yüz doksan beş çarpı elli iki", 10140.0)
        testSpeechToResult("yüzdoksanbeş çarpı elliiki", 10140.0)
        testSpeechToResult("195 çarpı 52", 10140.0)
    }

    @Test
    fun `ozel - bin yedi yuz elli carpi on`() {
        testSpeechToResult("binyediyüzelli çarpı on", 17500.0)
        testSpeechToResult("bin yedi yüz elli çarpı on", 17500.0)
        testSpeechToResult("1750 çarpı 10", 17500.0)
    }

    // ==================== BÖLME SEMBOLLERİ ====================

    @Test
    fun `bolme_sembolu - slash ile`() {
        // Google Speech bazen "/" sembolünü direkt verebilir
        testSpeechToResult("1000/4", 250.0)
        testSpeechToResult("100/5", 20.0)
        testSpeechToResult("27/3", 9.0)
    }

    @Test
    fun `bolme_sembolu - bosluklu slash`() {
        testSpeechToResult("1000 / 4", 250.0)
        testSpeechToResult("100 / 5", 20.0)
    }
}

