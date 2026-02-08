package com.melikyldrm.hesap.speech

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class TurkishNumberParserTest {

    private lateinit var parser: TurkishNumberParser

    @Before
    fun setup() {
        parser = TurkishNumberParser()
    }

    // ==================== TEMEL SAYILAR ====================

    @Test
    fun `parse birler - bir iki uc dort bes alti yedi sekiz dokuz`() {
        assertEquals(0L, parser.parseNumber("sıfır"))
        assertEquals(0L, parser.parseNumber("sifir")) // Türkçe karaktersiz
        assertEquals(1L, parser.parseNumber("bir"))
        assertEquals(2L, parser.parseNumber("iki"))
        assertEquals(3L, parser.parseNumber("üç"))
        assertEquals(3L, parser.parseNumber("uc")) // Türkçe karaktersiz
        assertEquals(4L, parser.parseNumber("dört"))
        assertEquals(4L, parser.parseNumber("dort")) // Türkçe karaktersiz
        assertEquals(5L, parser.parseNumber("beş"))
        assertEquals(5L, parser.parseNumber("bes")) // Türkçe karaktersiz
        assertEquals(6L, parser.parseNumber("altı"))
        assertEquals(6L, parser.parseNumber("alti")) // Türkçe karaktersiz
        assertEquals(7L, parser.parseNumber("yedi"))
        assertEquals(8L, parser.parseNumber("sekiz"))
        assertEquals(9L, parser.parseNumber("dokuz"))
    }

    @Test
    fun `parse onlar - on yirmi otuz kirk elli altmis yetmis seksen doksan`() {
        assertEquals(10L, parser.parseNumber("on"))
        assertEquals(20L, parser.parseNumber("yirmi"))
        assertEquals(30L, parser.parseNumber("otuz"))
        assertEquals(40L, parser.parseNumber("kırk"))
        assertEquals(40L, parser.parseNumber("kirk")) // Türkçe karaktersiz
        assertEquals(50L, parser.parseNumber("elli"))
        assertEquals(60L, parser.parseNumber("altmış"))
        assertEquals(60L, parser.parseNumber("altmis")) // Türkçe karaktersiz
        assertEquals(70L, parser.parseNumber("yetmiş"))
        assertEquals(70L, parser.parseNumber("yetmis")) // Türkçe karaktersiz
        assertEquals(80L, parser.parseNumber("seksen"))
        assertEquals(90L, parser.parseNumber("doksan"))
    }

    @Test
    fun `parse bilesik sayilar - onbes kirkiki doksanbes`() {
        assertEquals(15L, parser.parseNumber("onbeş"))
        assertEquals(15L, parser.parseNumber("onbes")) // Türkçe karaktersiz
        assertEquals(15L, parser.parseNumber("on beş"))
        assertEquals(42L, parser.parseNumber("kırkiki"))
        assertEquals(42L, parser.parseNumber("kirkiki")) // Türkçe karaktersiz
        assertEquals(42L, parser.parseNumber("kırk iki"))
        assertEquals(95L, parser.parseNumber("doksanbeş"))
        assertEquals(95L, parser.parseNumber("doksanbes")) // Türkçe karaktersiz
        assertEquals(95L, parser.parseNumber("doksan beş"))
        assertEquals(52L, parser.parseNumber("elliiki"))
        assertEquals(52L, parser.parseNumber("elli iki"))
    }

    @Test
    fun `parse yuzler - yuz ikiyuz doksandokuz`() {
        assertEquals(100L, parser.parseNumber("yüz"))
        assertEquals(100L, parser.parseNumber("yuz")) // Türkçe karaktersiz
        assertEquals(200L, parser.parseNumber("ikiyüz"))
        assertEquals(200L, parser.parseNumber("ikiyuz")) // Türkçe karaktersiz
        assertEquals(200L, parser.parseNumber("iki yüz"))
        assertEquals(195L, parser.parseNumber("yüzdoksanbeş"))
        assertEquals(195L, parser.parseNumber("yuzdoksanbes")) // Türkçe karaktersiz
        assertEquals(195L, parser.parseNumber("yüz doksan beş"))
        assertEquals(199L, parser.parseNumber("yüzdoksandokuz"))
        assertEquals(352L, parser.parseNumber("üçyüzelliiki"))
    }

    @Test
    fun `parse binler - bin ikibinüçyüz`() {
        assertEquals(1000L, parser.parseNumber("bin"))
        assertEquals(2000L, parser.parseNumber("ikibin"))
        assertEquals(2300L, parser.parseNumber("ikibinüçyüz"))
        assertEquals(1750L, parser.parseNumber("binyediyüzelli"))
        assertEquals(5432L, parser.parseNumber("beşbindörtyüzotuziki"))
    }

    // ==================== REPLACE NUMBER WORDS ====================

    @Test
    fun `replaceNumberWords - basit carpma`() {
        val result = parser.replaceNumberWords("yüzdoksanbeş çarpı elliiki")
        println("Input: yüzdoksanbeş çarpı elliiki")
        println("Output: $result")
        assertEquals("195 çarpı 52", result)
    }

    @Test
    fun `replaceNumberWords - turkce karaktersiz carpma`() {
        val result = parser.replaceNumberWords("yuzdoksanbes carpi elliiki")
        println("Input: yuzdoksanbes carpi elliiki")
        println("Output: $result")
        assertEquals("195 carpi 52", result)
    }

    @Test
    fun `replaceNumberWords - basit toplama`() {
        val result = parser.replaceNumberWords("yüz artı elli")
        assertEquals("100 artı 50", result)
    }

    @Test
    fun `replaceNumberWords - turkce karaktersiz toplama`() {
        val result = parser.replaceNumberWords("yuz arti elli")
        assertEquals("100 arti 50", result)
    }

    @Test
    fun `replaceNumberWords - basit cikarma`() {
        val result = parser.replaceNumberWords("bin eksi yüz")
        assertEquals("1000 eksi 100", result)
    }

    @Test
    fun `replaceNumberWords - basit bolme`() {
        val result = parser.replaceNumberWords("yüz bölü beş")
        assertEquals("100 bölü 5", result)
    }

    @Test
    fun `replaceNumberWords - turkce karaktersiz bolme`() {
        val result = parser.replaceNumberWords("yuz bolu bes")
        assertEquals("100 bolu 5", result)
    }

    @Test
    fun `replaceNumberWords - karmasik ifade`() {
        val result = parser.replaceNumberWords("onbeş çarpı yirmi artı beş")
        assertEquals("15 çarpı 20 artı 5", result)
    }
}

