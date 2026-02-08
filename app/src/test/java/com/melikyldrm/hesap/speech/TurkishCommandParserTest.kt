package com.melikyldrm.hesap.speech

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TurkishCommandParserTest {

    private lateinit var parser: TurkishCommandParser
    private lateinit var numberParser: TurkishNumberParser

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        parser = TurkishCommandParser(numberParser)
    }

    // ==================== ÇARPMA İŞLEMİ ====================

    @Test
    fun `parseExpression - yuzdoksanbes carpi elliiki`() {
        val result = parser.parseExpression("yüzdoksanbeş çarpı elliiki")
        println("Input: yüzdoksanbeş çarpı elliiki")
        println("Output: $result")
        assertEquals("195*52", result)
    }

    @Test
    fun `parseExpression - yuz carpi elli`() {
        val result = parser.parseExpression("yüz çarpı elli")
        assertEquals("100*50", result)
    }

    @Test
    fun `parseExpression - on carpi on`() {
        val result = parser.parseExpression("on çarpı on")
        assertEquals("10*10", result)
    }

    @Test
    fun `parseExpression - bes kere alti`() {
        val result = parser.parseExpression("beş kere altı")
        assertEquals("5*6", result)
    }

    // ==================== TOPLAMA İŞLEMİ ====================

    @Test
    fun `parseExpression - yuz arti elli`() {
        val result = parser.parseExpression("yüz artı elli")
        assertEquals("100+50", result)
    }

    @Test
    fun `parseExpression - onbes arti yirmi`() {
        val result = parser.parseExpression("onbeş artı yirmi")
        assertEquals("15+20", result)
    }

    @Test
    fun `parseExpression - bin ekle ikiyuz`() {
        val result = parser.parseExpression("bin ekle ikiyüz")
        assertEquals("1000+200", result)
    }

    // ==================== ÇIKARMA İŞLEMİ ====================

    @Test
    fun `parseExpression - yuz eksi elli`() {
        val result = parser.parseExpression("yüz eksi elli")
        assertEquals("100-50", result)
    }

    @Test
    fun `parseExpression - bin cikar yuz`() {
        val result = parser.parseExpression("bin çıkar yüz")
        assertEquals("1000-100", result)
    }

    @Test
    fun `parseExpression - elli fark on`() {
        val result = parser.parseExpression("elli fark on")
        assertEquals("50-10", result)
    }

    // ==================== BÖLME İŞLEMİ ====================

    @Test
    fun `parseExpression - yuz bolu bes`() {
        val result = parser.parseExpression("yüz bölü beş")
        assertEquals("100/5", result)
    }

    @Test
    fun `parseExpression - elli bolu on`() {
        val result = parser.parseExpression("elli bölü on")
        assertEquals("50/10", result)
    }

    @Test
    fun `parseExpression - bin bol yuz`() {
        val result = parser.parseExpression("bin böl yüz")
        assertEquals("1000/100", result)
    }

    // ==================== KARMAŞIK İFADELER ====================

    @Test
    fun `parseExpression - on arti yirmi carpi iki`() {
        val result = parser.parseExpression("on artı yirmi çarpı iki")
        assertEquals("10+20*2", result)
    }

    @Test
    fun `parseExpression - yuz bolu iki arti elli`() {
        val result = parser.parseExpression("yüz bölü iki artı elli")
        assertEquals("100/2+50", result)
    }

    // ==================== SAYISAL GİRİŞLER ====================

    @Test
    fun `parseExpression - numeric 195 carpi 52`() {
        val result = parser.parseExpression("195 çarpı 52")
        assertEquals("195*52", result)
    }

    @Test
    fun `parseExpression - mixed yuz carpi 5`() {
        val result = parser.parseExpression("yüz çarpı 5")
        assertEquals("100*5", result)
    }

    // ==================== PARSE COMMAND ====================

    @Test
    fun `parseCommand - yuzdoksanbes carpi elliiki returns Calculate`() {
        val command = parser.parseCommand("yüzdoksanbeş çarpı elliiki")
        assertTrue(command is SpeechCommand.Calculate)
        assertEquals("195*52", (command as SpeechCommand.Calculate).expression)
    }

    @Test
    fun `parseCommand - temizle returns Clear`() {
        val command = parser.parseCommand("temizle")
        assertTrue(command is SpeechCommand.Clear)
    }

    @Test
    fun `parseCommand - sil returns Delete`() {
        val command = parser.parseCommand("sil")
        assertTrue(command is SpeechCommand.Delete)
    }

    @Test
    fun `parseCommand - hesapla returns Equals`() {
        val command = parser.parseCommand("hesapla")
        assertTrue(command is SpeechCommand.Equals)
    }
}

class DebugTests {
    private val numberParser = TurkishNumberParser()
    private val parser = TurkishCommandParser(numberParser)
    @org.junit.Test
    fun `debug - what happens with spaces`() {
        // Google bazen kelimeleri ayrı verebilir
        println("=== DEBUG TESTS ===")
        val testCases = listOf(
            "yüzdoksanbeş çarpı elliiki",
            "yüz doksan beş çarpı elli iki",
            "195 çarpı 52",
            "195 carpi 52",
            "195 x 52",
            "195 kere 52",
            "yüzdoksanbeş kere elliiki",
            "yuz doksan bes carpi elli iki"  // Türkçe karaktersiz
        )
        for (input in testCases) {
            val result = parser.parseExpression(input)
            println("'$input' -> '$result'")
        }
    }
}
