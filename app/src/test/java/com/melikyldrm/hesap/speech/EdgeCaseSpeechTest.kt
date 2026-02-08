package com.melikyldrm.hesap.speech

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Edge case testleri - sesli hesap makinası için kritik senaryolar
 */
class EdgeCaseSpeechTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
    }

    // ==================== ÇOK BÜYÜK SAYILAR ====================

    @Test
    fun `buyuk - milyon carpma`() {
        val result = commandParser.parseExpression("bir milyon çarpı iki")
        println("Input: bir milyon çarpı iki -> Output: $result")
        assertEquals("1000000*2", result)
    }

    @Test
    fun `buyuk - milyar bolme`() {
        val result = commandParser.parseExpression("bir milyar bölü bin")
        println("Input: bir milyar bölü bin -> Output: $result")
        assertEquals("1000000000/1000", result)
    }

    @Test
    fun `buyuk - trilyon toplama`() {
        val result = commandParser.parseExpression("bir trilyon artı bir milyar")
        println("Input: bir trilyon artı bir milyar -> Output: $result")
        assertEquals("1000000000000+1000000000", result)
    }

    @Test
    fun `buyuk - karmasik buyuk sayi`() {
        val result = commandParser.parseExpression("üçyüzelliiki milyon dörtyüzaltmışbeşbin yediyüzseksenüç çarpı iki")
        println("Input: 352465783 çarpı iki -> Output: $result")
        assertEquals("352465783*2", result)
    }

    // ==================== ÇOK KÜÇÜK SAYILAR ====================

    @Test
    fun `kucuk - sifir toplama`() {
        val result = commandParser.parseExpression("sıfır artı bir")
        println("Input: sıfır artı bir -> Output: $result")
        assertEquals("0+1", result)
    }

    @Test
    fun `kucuk - bir bolme`() {
        val result = commandParser.parseExpression("bir bölü bir")
        println("Input: bir bölü bir -> Output: $result")
        assertEquals("1/1", result)
    }

    @Test
    fun `kucuk - sifir carpma`() {
        val result = commandParser.parseExpression("sıfır çarpı bin")
        println("Input: sıfır çarpı bin -> Output: $result")
        assertEquals("0*1000", result)
    }

    // ==================== KESİRLİ SAYILAR ====================

    @Test
    fun `kesirli - virgul ile`() {
        val result = commandParser.parseExpression("üç virgül beş çarpı iki")
        println("Input: üç virgül beş çarpı iki -> Output: $result")
        assertEquals("3.5*2", result)
    }

    @Test
    fun `kesirli - nokta ile`() {
        val result = commandParser.parseExpression("yedi nokta yirmibeş artı iki nokta elli")
        println("Input: yedi nokta yirmibeş artı iki nokta elli -> Output: $result")
        assertEquals("7.25+2.50", result)
    }

    @Test
    fun `kesirli - numeric virgul`() {
        val result = commandParser.parseExpression("3,14159 çarpı iki")
        println("Input: 3,14159 çarpı iki -> Output: $result")
        assertEquals("3.14159*2", result)
    }

    @Test
    fun `kesirli - numeric nokta`() {
        val result = commandParser.parseExpression("2.5 bölü 0.5")
        println("Input: 2.5 bölü 0.5 -> Output: $result")
        assertEquals("2.5/0.5", result)
    }

    @Test
    fun `kesirli - karisik`() {
        val result = commandParser.parseExpression("yüz virgül elli çarpı iki")
        println("Input: yüz virgül elli çarpı iki -> Output: $result")
        assertEquals("100.50*2", result)
    }

    // ==================== 4 İŞLEM - TOPLAMA ====================

    @Test
    fun `toplama - arti kelimesi`() {
        val result = commandParser.parseExpression("yüzyirmiüç artı dörtyüzellialtı")
        println("Input: yüzyirmiüç artı dörtyüzellialtı -> Output: $result")
        assertEquals("123+456", result)
    }

    @Test
    fun `toplama - ekle kelimesi`() {
        val result = commandParser.parseExpression("yüz ekle elli")
        println("Input: yüz ekle elli -> Output: $result")
        assertEquals("100+50", result)
    }

    @Test
    fun `toplama - topla kelimesi`() {
        val result = commandParser.parseExpression("bin topla beşyüz")
        println("Input: bin topla beşyüz -> Output: $result")
        assertEquals("1000+500", result)
    }

    // ==================== 4 İŞLEM - ÇIKARMA ====================

    @Test
    fun `cikarma - eksi kelimesi`() {
        val result = commandParser.parseExpression("bin eksi yüz")
        println("Input: bin eksi yüz -> Output: $result")
        assertEquals("1000-100", result)
    }

    @Test
    fun `cikarma - cikar kelimesi`() {
        val result = commandParser.parseExpression("beşyüz çıkar yüz")
        println("Input: beşyüz çıkar yüz -> Output: $result")
        assertEquals("500-100", result)
    }

    @Test
    fun `cikarma - cikart kelimesi`() {
        val result = commandParser.parseExpression("bin çıkart ikiyüz")
        println("Input: bin çıkart ikiyüz -> Output: $result")
        assertEquals("1000-200", result)
    }

    @Test
    fun `cikarma - fark kelimesi`() {
        val result = commandParser.parseExpression("yüz fark elli")
        println("Input: yüz fark elli -> Output: $result")
        assertEquals("100-50", result)
    }

    // ==================== 4 İŞLEM - ÇARPMA ====================

    @Test
    fun `carpma - carpi kelimesi`() {
        val result = commandParser.parseExpression("yirmibeş çarpı dört")
        println("Input: yirmibeş çarpı dört -> Output: $result")
        assertEquals("25*4", result)
    }

    @Test
    fun `carpma - kere kelimesi`() {
        val result = commandParser.parseExpression("on kere on")
        println("Input: on kere on -> Output: $result")
        assertEquals("10*10", result)
    }

    @Test
    fun `carpma - kez kelimesi`() {
        val result = commandParser.parseExpression("beş kez altı")
        println("Input: beş kez altı -> Output: $result")
        assertEquals("5*6", result)
    }

    @Test
    fun `carpma - defa kelimesi`() {
        val result = commandParser.parseExpression("yedi defa sekiz")
        println("Input: yedi defa sekiz -> Output: $result")
        assertEquals("7*8", result)
    }

    @Test
    fun `carpma - kat kelimesi`() {
        val result = commandParser.parseExpression("üç kat beş")
        println("Input: üç kat beş -> Output: $result")
        assertEquals("3*5", result)
    }

    // ==================== 4 İŞLEM - BÖLME ====================

    @Test
    fun `bolme - bolu kelimesi`() {
        val result = commandParser.parseExpression("yüz bölü dört")
        println("Input: yüz bölü dört -> Output: $result")
        assertEquals("100/4", result)
    }

    @Test
    fun `bolme - bol kelimesi`() {
        val result = commandParser.parseExpression("elli böl on")
        println("Input: elli böl on -> Output: $result")
        assertEquals("50/10", result)
    }

    // ==================== TÜRKÇE KARAKTERSİZ ====================

    @Test
    fun `turkce_karaktersiz - carpma`() {
        val result = commandParser.parseExpression("yuzdoksanbes carpi elliiki")
        println("Input: yuzdoksanbes carpi elliiki -> Output: $result")
        assertEquals("195*52", result)
    }

    @Test
    fun `turkce_karaktersiz - bolme`() {
        val result = commandParser.parseExpression("yuz bolu bes")
        println("Input: yuz bolu bes -> Output: $result")
        assertEquals("100/5", result)
    }

    @Test
    fun `turkce_karaktersiz - cikarma`() {
        val result = commandParser.parseExpression("bin cikar yuz")
        println("Input: bin cikar yuz -> Output: $result")
        assertEquals("1000-100", result)
    }

    @Test
    fun `turkce_karaktersiz - toplama`() {
        val result = commandParser.parseExpression("elli arti yirmi")
        println("Input: elli arti yirmi -> Output: $result")
        assertEquals("50+20", result)
    }

    // ==================== KARMAŞIK İFADELER ====================

    @Test
    fun `karmasik - uc islem`() {
        val result = commandParser.parseExpression("on artı yirmi çarpı üç eksi beş")
        println("Input: on artı yirmi çarpı üç eksi beş -> Output: $result")
        assertEquals("10+20*3-5", result)
    }

    @Test
    fun `karmasik - dort islem`() {
        val result = commandParser.parseExpression("yüz artı elli eksi yirmi çarpı iki bölü dört")
        println("Input: yüz artı elli eksi yirmi çarpı iki bölü dört -> Output: $result")
        assertEquals("100+50-20*2/4", result)
    }

    // ==================== GOOGLE SPEECH VARYASYONLARI ====================

    @Test
    fun `google_speech - bosluklarla`() {
        val result = commandParser.parseExpression("yüz doksan beş çarpı elli iki")
        println("Input: yüz doksan beş çarpı elli iki -> Output: $result")
        assertEquals("195*52", result)
    }

    @Test
    fun `google_speech - ekstra bosluk`() {
        val result = commandParser.parseExpression("  on   artı   yirmi  ")
        println("Input: '  on   artı   yirmi  ' -> Output: $result")
        assertEquals("10+20", result)
    }
}

