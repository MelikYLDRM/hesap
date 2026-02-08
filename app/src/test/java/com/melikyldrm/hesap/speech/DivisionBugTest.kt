package com.melikyldrm.hesap.speech

import com.melikyldrm.hesap.domain.engine.CalculatorEngine
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Bölme işlemi hata tespiti testi
 */
class DivisionBugTest {

    private lateinit var numberParser: TurkishNumberParser
    private lateinit var commandParser: TurkishCommandParser
    private lateinit var calculatorEngine: CalculatorEngine

    @Before
    fun setup() {
        numberParser = TurkishNumberParser()
        commandParser = TurkishCommandParser(numberParser)
        calculatorEngine = CalculatorEngine()
    }

    @Test
    fun `debug - yirmibin sayi parse`() {
        val result = numberParser.parseNumber("yirmibin")
        println("yirmibin -> $result")
        assertEquals(20000L, result)
    }

    @Test
    fun `debug - yirmi bin sayi parse`() {
        val result = numberParser.parseNumber("yirmi bin")
        println("yirmi bin -> $result")
        assertEquals(20000L, result)
    }

    @Test
    fun `debug - yirmiyedi sayi parse`() {
        val result = numberParser.parseNumber("yirmiyedi")
        println("yirmiyedi -> $result")
        assertEquals(27L, result)
    }

    @Test
    fun `debug - yirmi yedi sayi parse`() {
        val result = numberParser.parseNumber("yirmi yedi")
        println("yirmi yedi -> $result")
        assertEquals(27L, result)
    }

    @Test
    fun `debug - replaceNumberWords yirmibin bolu yirmiyedi`() {
        val result = numberParser.replaceNumberWords("yirmibin bölü yirmiyedi")
        println("yirmibin bölü yirmiyedi -> $result")
        assertEquals("20000 bölü 27", result)
    }

    @Test
    fun `debug - replaceNumberWords yirmi bin bolu yirmi yedi`() {
        val result = numberParser.replaceNumberWords("yirmi bin bölü yirmi yedi")
        println("yirmi bin bölü yirmi yedi -> $result")
        assertEquals("20000 bölü 27", result)
    }

    @Test
    fun `debug - parseExpression yirmibin bolu yirmiyedi`() {
        val result = commandParser.parseExpression("yirmibin bölü yirmiyedi")
        println("parseExpression: yirmibin bölü yirmiyedi -> $result")
        assertEquals("20000/27", result)
    }

    @Test
    fun `debug - parseExpression yirmi bin bolu yirmi yedi`() {
        val result = commandParser.parseExpression("yirmi bin bölü yirmi yedi")
        println("parseExpression: yirmi bin bölü yirmi yedi -> $result")
        assertEquals("20000/27", result)
    }

    @Test
    fun `debug - 20000 bolu 27 numeric`() {
        val result = commandParser.parseExpression("20000 bölü 27")
        println("parseExpression: 20000 bölü 27 -> $result")
        assertEquals("20000/27", result)
    }

    // E2E test - tam hesaplama
    @Test
    fun `e2e - yirmibin bolu yirmiyedi sonuc`() {
        val expression = commandParser.parseExpression("yirmibin bölü yirmiyedi")
        println("Expression: $expression")

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Sonuç: $value")
                // 20000 / 27 = 740.7407...
                assertEquals(740.7407, value, 0.001)
            },
            onFailure = { error ->
                fail("Hesaplama hatası: ${error.message}")
            }
        )
    }

    @Test
    fun `e2e - yirmi bin bolu yirmi yedi sonuc`() {
        val expression = commandParser.parseExpression("yirmi bin bölü yirmi yedi")
        println("Expression: $expression")

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Sonuç: $value")
                assertEquals(740.7407, value, 0.001)
            },
            onFailure = { error ->
                fail("Hesaplama hatası: ${error.message}")
            }
        )
    }

    // Google Speech varyasyonları - Türkçe binlik ayırıcı
    @Test
    fun `google - 20000 nokta ile binlik ayirici`() {
        // Google bazen binlik ayırıcı olarak nokta kullanabilir: 20.000 = 20000
        val expression = commandParser.parseExpression("20.000 bölü 27")
        println("20.000 bölü 27 -> $expression")
        assertEquals("20000/27", expression)

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Sonuç: $value")
                assertEquals(740.7407, value, 0.001)
            },
            onFailure = { error ->
                fail("Hesaplama hatası: ${error.message}")
            }
        )
    }

    @Test
    fun `google - 1000000 binlik ayirici`() {
        // 1.000.000 = 1000000
        val expression = commandParser.parseExpression("1.000.000 bölü 100")
        println("1.000.000 bölü 100 -> $expression")
        assertEquals("1000000/100", expression)

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Sonuç: $value")
                assertEquals(10000.0, value, 0.001)
            },
            onFailure = { error ->
                fail("Hesaplama hatası: ${error.message}")
            }
        )
    }

    @Test
    fun `google - 20000 slash 27`() {
        val expression = commandParser.parseExpression("20000/27")
        println("20000/27 -> Expression: $expression")

        val result = calculatorEngine.evaluate(expression)
        result.fold(
            onSuccess = { value ->
                println("Sonuç: $value")
                assertEquals(740.7407, value, 0.001)
            },
            onFailure = { error ->
                fail("Hesaplama hatası: ${error.message}")
            }
        )
    }
}

