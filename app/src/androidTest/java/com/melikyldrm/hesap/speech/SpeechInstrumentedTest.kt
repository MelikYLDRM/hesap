package com.melikyldrm.hesap.speech

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Instrumented test - gerçek cihaz/emülatörde çalışır
 */
@RunWith(AndroidJUnit4::class)
class SpeechInstrumentedTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
    }

    @Test
    fun testCarpma_YuzDoksanBes_Carpi_ElliIki() {
        val result = commandParser.parseExpression("yüzdoksanbeş çarpı elliiki")
        println("TEST: 'yüzdoksanbeş çarpı elliiki' -> '$result'")
        assertEquals("195*52", result)
    }

    @Test
    fun testCarpma_Bin_Carpi_Bin() {
        val result = commandParser.parseExpression("bin çarpı bin")
        println("TEST: 'bin çarpı bin' -> '$result'")
        assertEquals("1000*1000", result)
    }

    @Test
    fun testToplama_Yuz_Arti_Elli() {
        val result = commandParser.parseExpression("yüz artı elli")
        println("TEST: 'yüz artı elli' -> '$result'")
        assertEquals("100+50", result)
    }

    @Test
    fun testCikarma_Bin_Eksi_Yuz() {
        val result = commandParser.parseExpression("bin eksi yüz")
        println("TEST: 'bin eksi yüz' -> '$result'")
        assertEquals("1000-100", result)
    }

    @Test
    fun testBolme_Yuz_Bolu_Bes() {
        val result = commandParser.parseExpression("yüz bölü beş")
        println("TEST: 'yüz bölü beş' -> '$result'")
        assertEquals("100/5", result)
    }

    @Test
    fun testTurkceKaraktersiz_Carpi() {
        val result = commandParser.parseExpression("yuzdoksanbes carpi elliiki")
        println("TEST: 'yuzdoksanbes carpi elliiki' -> '$result'")
        assertEquals("195*52", result)
    }

    @Test
    fun testKere_Kelimesi() {
        val result = commandParser.parseExpression("beş kere altı")
        println("TEST: 'beş kere altı' -> '$result'")
        assertEquals("5*6", result)
    }

    @Test
    fun testNumberParser_YuzDoksanBes() {
        val result = numberParser.parseNumber("yüzdoksanbeş")
        println("TEST NUMBER: 'yüzdoksanbeş' -> '$result'")
        assertEquals(195L, result)
    }

    @Test
    fun testNumberParser_ElliIki() {
        val result = numberParser.parseNumber("elliiki")
        println("TEST NUMBER: 'elliiki' -> '$result'")
        assertEquals(52L, result)
    }

    @Test
    fun testReplaceNumberWords_CarpiKorunuyor() {
        val result = numberParser.replaceNumberWords("yüzdoksanbeş çarpı elliiki")
        println("TEST REPLACE: 'yüzdoksanbeş çarpı elliiki' -> '$result'")
        assertTrue("Result should contain çarpı", result.contains("çarpı"))
        assertTrue("Result should contain 195", result.contains("195"))
        assertTrue("Result should contain 52", result.contains("52"))
    }
}

