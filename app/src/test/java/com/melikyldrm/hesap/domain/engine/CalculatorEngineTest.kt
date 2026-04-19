package com.melikyldrm.hesap.domain.engine

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CalculatorEngineTest {

    private lateinit var engine: CalculatorEngine

    @Before
    fun setup() {
        engine = CalculatorEngine()
    }

    // ========== Basic Operations ==========

    @Test
    fun `addition works correctly`() {
        assertEquals(5.0, engine.evaluate("2+3").getOrThrow(), 0.0001)
    }

    @Test
    fun `subtraction works correctly`() {
        assertEquals(7.0, engine.evaluate("10-3").getOrThrow(), 0.0001)
    }

    @Test
    fun `multiplication works correctly`() {
        assertEquals(15.0, engine.evaluate("3*5").getOrThrow(), 0.0001)
    }

    @Test
    fun `division works correctly`() {
        assertEquals(4.0, engine.evaluate("20/5").getOrThrow(), 0.0001)
    }

    @Test
    fun `division by zero returns failure`() {
        assertTrue(engine.evaluate("1/0").isFailure)
    }

    @Test
    fun `order of operations is correct`() {
        assertEquals(11.0, engine.evaluate("2+3*3").getOrThrow(), 0.0001)
    }

    @Test
    fun `parentheses override order of operations`() {
        assertEquals(15.0, engine.evaluate("(2+3)*3").getOrThrow(), 0.0001)
    }

    @Test
    fun `negative numbers work`() {
        assertEquals(-5.0, engine.evaluate("-2-3").getOrThrow(), 0.0001)
    }

    @Test
    fun `decimal numbers work`() {
        assertEquals(3.5, engine.evaluate("1.5+2").getOrThrow(), 0.0001)
    }

    @Test
    fun `blank expression returns 0`() {
        assertEquals(0.0, engine.evaluate("").getOrThrow(), 0.0001)
    }

    // ========== Display Operator Replacement ==========

    @Test
    fun `unicode multiply operator is replaced`() {
        assertEquals(15.0, engine.evaluate("3\u00D75").getOrThrow(), 0.0001) // ×
    }

    @Test
    fun `unicode divide operator is replaced`() {
        assertEquals(4.0, engine.evaluate("20\u00F75").getOrThrow(), 0.0001) // ÷
    }

    // ========== Scientific Functions ==========

    @Test
    fun `sqrt function works`() {
        assertEquals(3.0, engine.evaluate("sqrt(9)").getOrThrow(), 0.0001)
    }

    @Test
    fun `sqrt of negative returns failure`() {
        assertTrue(engine.evaluate("sqrt(-1)").isFailure)
    }

    @Test
    fun `log10 function works`() {
        assertEquals(2.0, engine.evaluate("log10(100)").getOrThrow(), 0.0001)
    }

    @Test
    fun `ln function works`() {
        assertEquals(1.0, engine.evaluate("ln(2.71828182845904523536)").getOrThrow(), 0.0001)
    }

    @Test
    fun `sin function works in radians`() {
        assertEquals(0.0, engine.evaluate("sin(0)").getOrThrow(), 0.0001)
    }

    @Test
    fun `sind function works in degrees`() {
        assertEquals(1.0, engine.evaluate("sind(90)").getOrThrow(), 0.0001)
    }

    @Test
    fun `cosd function works in degrees`() {
        assertEquals(0.0, engine.evaluate("cosd(90)").getOrThrow(), 0.0001)
    }

    @Test
    fun `tand function works in degrees`() {
        assertEquals(1.0, engine.evaluate("tand(45)").getOrThrow(), 0.0001)
    }

    @Test
    fun `power operator works`() {
        assertEquals(8.0, engine.evaluate("2^3").getOrThrow(), 0.0001)
    }

    @Test
    fun `factorial works`() {
        assertEquals(120.0, engine.evaluate("factorial(5)").getOrThrow(), 0.0001)
    }

    @Test
    fun `cbrt function works`() {
        assertEquals(3.0, engine.evaluate("cbrt(27)").getOrThrow(), 0.0001)
    }

    @Test
    fun `abs function works`() {
        assertEquals(5.0, engine.evaluate("abs(-5)").getOrThrow(), 0.0001)
    }

    @Test
    fun `exp function works`() {
        assertEquals(Math.E, engine.evaluate("exp(1)").getOrThrow(), 0.0001)
    }

    @Test
    fun `ceil function works`() {
        assertEquals(4.0, engine.evaluate("ceil(3.2)").getOrThrow(), 0.0001)
    }

    @Test
    fun `floor function works`() {
        assertEquals(3.0, engine.evaluate("floor(3.9)").getOrThrow(), 0.0001)
    }

    @Test
    fun `round function works`() {
        assertEquals(4.0, engine.evaluate("round(3.6)").getOrThrow(), 0.0001)
    }

    // ========== "e" constant bug fix verification ==========

    @Test
    fun `standalone e constant is replaced correctly`() {
        val result = engine.evaluate("e").getOrThrow()
        assertEquals(2.71828, result, 0.001)
    }

    @Test
    fun `exp function is NOT broken by e replacement`() {
        val result = engine.evaluate("exp(1)").getOrThrow()
        assertEquals(Math.E, result, 0.0001)
    }

    @Test
    fun `ceil function is NOT broken by e replacement`() {
        assertEquals(4.0, engine.evaluate("ceil(3.1)").getOrThrow(), 0.0001)
    }

    @Test
    fun `cbrt function is NOT broken by e replacement`() {
        assertEquals(2.0, engine.evaluate("cbrt(8)").getOrThrow(), 0.0001)
    }

    // ========== Implicit Multiplication ==========

    @Test
    fun `implicit multiplication with parentheses works`() {
        assertEquals(6.0, engine.evaluate("2(3)").getOrThrow(), 0.0001)
    }

    @Test
    fun `implicit multiplication with function works`() {
        assertEquals(0.0, engine.evaluate("2*sin(0)").getOrThrow(), 0.0001)
    }

    // ========== Format Result ==========

    @Test
    fun `formatResult integer display`() {
        assertEquals("42", engine.formatResult(42.0))
    }

    @Test
    fun `formatResult decimal display`() {
        assertEquals("3.14", engine.formatResult(3.14))
    }

    @Test
    fun `formatResult removes trailing zeros`() {
        assertEquals("3.5", engine.formatResult(3.50))
    }

    @Test
    fun `formatResult large integer display`() {
        val result = engine.formatResult(1e15)
        assertEquals("1000000000000000", result)
    }

    @Test
    fun `formatResult scientific notation for very small numbers`() {
        val result = engine.formatResult(0.0000001)
        assertTrue(result.contains("E"))
    }

    // ========== Expression Validation ==========

    @Test
    fun `blank expression is not complete`() {
        assertFalse(engine.isExpressionComplete(""))
    }

    @Test
    fun `expression ending with operator is not complete`() {
        assertFalse(engine.isExpressionComplete("2+"))
    }

    @Test
    fun `simple expression is complete`() {
        assertTrue(engine.isExpressionComplete("2+3"))
    }

    @Test
    fun `unbalanced parentheses are not complete`() {
        assertFalse(engine.isExpressionComplete("(2+3"))
    }

    @Test
    fun `balanced parentheses are complete`() {
        assertTrue(engine.isExpressionComplete("(2+3)"))
    }

    // ========== Utility Functions ==========

    @Test
    fun `getLastNumber returns correct value`() {
        assertEquals(42.0, engine.getLastNumber("10+42"))
    }

    @Test
    fun `calculatePercentage works`() {
        assertEquals(20.0, engine.calculatePercentage(100.0, 20.0), 0.0001)
    }

    @Test
    fun `squareRoot works`() {
        assertEquals(5.0, engine.squareRoot(25.0).getOrThrow(), 0.0001)
    }

    @Test
    fun `squareRoot negative fails`() {
        assertTrue(engine.squareRoot(-1.0).isFailure)
    }

    @Test
    fun `power works`() {
        assertEquals(16.0, engine.power(2.0, 4.0), 0.0001)
    }

    @Test
    fun `factorial works for 0`() {
        assertEquals(1L, engine.factorial(0).getOrThrow())
    }

    @Test
    fun `factorial fails for negative`() {
        assertTrue(engine.factorial(-1).isFailure)
    }

    @Test
    fun `factorial fails for too large`() {
        assertTrue(engine.factorial(21).isFailure)
    }

    @Test
    fun `permutation works`() {
        assertEquals(60L, engine.permutation(5, 3).getOrThrow())
    }

    @Test
    fun `combination works`() {
        assertEquals(10L, engine.combination(5, 3).getOrThrow())
    }
}

