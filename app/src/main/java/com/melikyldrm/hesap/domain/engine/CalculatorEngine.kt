package com.melikyldrm.hesap.domain.engine

import net.objecthunter.exp4j.ExpressionBuilder
import net.objecthunter.exp4j.function.Function
import net.objecthunter.exp4j.operator.Operator
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class CalculatorEngine @Inject constructor() {

    // Custom functions for scientific calculations
    private val factorial = object : Function("factorial", 1) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toLong()
            if (n < 0) throw IllegalArgumentException("Faktöriyel negatif sayılar için tanımsızdır")
            if (n > 20) throw IllegalArgumentException("Faktöriyel çok büyük (max 20!)")
            return (1..n).fold(1L) { acc, i -> acc * i }.toDouble()
        }
    }

    private val permutation = object : Function("perm", 2) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toLong()
            val r = args[1].toLong()
            if (n < 0 || r < 0) throw IllegalArgumentException("Değerler negatif olamaz")
            if (r > n) throw IllegalArgumentException("r, n'den büyük olamaz")
            return ((n - r + 1)..n).fold(1L) { acc, i -> acc * i }.toDouble()
        }
    }

    private val combination = object : Function("comb", 2) {
        override fun apply(vararg args: Double): Double {
            val n = args[0].toLong()
            val r = args[1].toLong()
            if (n < 0 || r < 0) throw IllegalArgumentException("Değerler negatif olamaz")
            if (r > n) throw IllegalArgumentException("r, n'den büyük olamaz")
            val numerator = ((n - r + 1)..n).fold(1L) { acc, i -> acc * i }
            val denominator = (1..r).fold(1L) { acc, i -> acc * i }
            return (numerator / denominator).toDouble()
        }
    }

    private val logBase10 = object : Function("log10", 1) {
        override fun apply(vararg args: Double): Double {
            return log10(args[0])
        }
    }

    private val ln = object : Function("ln", 1) {
        override fun apply(vararg args: Double): Double {
            return ln(args[0])
        }
    }

    private val sqrtFunc = object : Function("sqrt", 1) {
        override fun apply(vararg args: Double): Double {
            if (args[0] < 0) throw IllegalArgumentException("Negatif sayının karekökü alınamaz")
            return sqrt(args[0])
        }
    }

    private val cbrt = object : Function("cbrt", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.cbrt(args[0])
        }
    }

    private val abs = object : Function("abs", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.abs(args[0])
        }
    }

    private val sinDeg = object : Function("sind", 1) {
        override fun apply(vararg args: Double): Double {
            return sin(Math.toRadians(args[0]))
        }
    }

    private val cosDeg = object : Function("cosd", 1) {
        override fun apply(vararg args: Double): Double {
            return cos(Math.toRadians(args[0]))
        }
    }

    private val tanDeg = object : Function("tand", 1) {
        override fun apply(vararg args: Double): Double {
            return tan(Math.toRadians(args[0]))
        }
    }

    private val asinDeg = object : Function("asind", 1) {
        override fun apply(vararg args: Double): Double {
            return Math.toDegrees(asin(args[0]))
        }
    }

    private val acosDeg = object : Function("acosd", 1) {
        override fun apply(vararg args: Double): Double {
            return Math.toDegrees(acos(args[0]))
        }
    }

    private val atanDeg = object : Function("atand", 1) {
        override fun apply(vararg args: Double): Double {
            return Math.toDegrees(atan(args[0]))
        }
    }

    private val exp = object : Function("exp", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.exp(args[0])
        }
    }

    private val round = object : Function("round", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.round(args[0])
        }
    }

    private val floor = object : Function("floor", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.floor(args[0])
        }
    }

    private val ceil = object : Function("ceil", 1) {
        override fun apply(vararg args: Double): Double {
            return kotlin.math.ceil(args[0])
        }
    }

    // Percentage operator
    private val percentOperator = object : Operator("%", 1, true, Operator.PRECEDENCE_POWER + 1) {
        override fun apply(vararg args: Double): Double {
            return args[0] / 100.0
        }
    }

    /**
     * Evaluate a mathematical expression
     */
    fun evaluate(expression: String): Result<Double> {
        return try {
            if (expression.isBlank()) {
                return Result.success(0.0)
            }

            // Pre-process expression
            val processedExpression = preprocessExpression(expression)

            val result = ExpressionBuilder(processedExpression)
                .functions(
                    factorial, permutation, combination,
                    logBase10, ln, sqrtFunc, cbrt, abs,
                    sinDeg, cosDeg, tanDeg,
                    asinDeg, acosDeg, atanDeg,
                    exp, round, floor, ceil
                )
                .operator(percentOperator)
                .build()
                .evaluate()

            if (result.isNaN() || result.isInfinite()) {
                Result.failure(ArithmeticException("Geçersiz sonuç"))
            } else {
                Result.success(result)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Pre-process the expression to handle special cases
     */
    private fun preprocessExpression(expression: String): String {
        var result = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("π", "3.14159265358979323846")
            .replace("e", "2.71828182845904523536")
            .replace("²", "^2")
            .replace("³", "^3")
            .replace("√", "sqrt")

        // Handle implicit multiplication (e.g., "2(3)" -> "2*(3)", "2sin" -> "2*sin")
        result = result.replace(Regex("(\\d)\\(")) { "${it.groupValues[1]}*(" }
        result = result.replace(Regex("\\)(\\d)")) { ")*${it.groupValues[1]}" }
        result = result.replace(Regex("(\\d)(sin|cos|tan|log|ln|sqrt|abs|exp)")) {
            "${it.groupValues[1]}*${it.groupValues[2]}"
        }

        return result
    }

    /**
     * Format the result for display
     */
    fun formatResult(value: Double, maxDecimals: Int = 10): String {
        return when {
            value == value.toLong().toDouble() -> {
                value.toLong().toString()
            }
            kotlin.math.abs(value) >= 1e10 || (kotlin.math.abs(value) < 1e-6 && value != 0.0) -> {
                String.format(Locale.US, "%.${maxDecimals}E", value).trimEnd('0').trimEnd('.')
            }
            else -> {
                String.format(Locale.US, "%.${maxDecimals}f", value)
                    .trimEnd('0')
                    .trimEnd('.')
            }
        }
    }

    /**
     * Validate if expression is complete and can be evaluated
     */
    fun isExpressionComplete(expression: String): Boolean {
        if (expression.isBlank()) return false

        val trimmed = expression.trim()
        val operators = listOf('+', '-', '*', '/', '^', '×', '÷', '−')

        // Check if ends with operator
        if (trimmed.last() in operators) return false

        // Check balanced parentheses
        var parenCount = 0
        for (char in trimmed) {
            when (char) {
                '(' -> parenCount++
                ')' -> parenCount--
            }
            if (parenCount < 0) return false
        }

        return parenCount == 0
    }

    /**
     * Get the last number in the expression (for percentage calculations)
     */
    fun getLastNumber(expression: String): Double? {
        val regex = Regex("([+-]?\\d+\\.?\\d*)$")
        val match = regex.find(expression)
        return match?.groupValues?.get(1)?.toDoubleOrNull()
    }

    /**
     * Calculate percentage of a value
     */
    fun calculatePercentage(value: Double, percentage: Double): Double {
        return value * (percentage / 100.0)
    }

    /**
     * Calculate square root
     */
    fun squareRoot(value: Double): Result<Double> {
        return if (value < 0) {
            Result.failure(IllegalArgumentException("Negatif sayının karekökü alınamaz"))
        } else {
            Result.success(sqrt(value))
        }
    }

    /**
     * Calculate power
     */
    fun power(base: Double, exponent: Double): Double {
        return base.pow(exponent)
    }

    /**
     * Calculate factorial
     */
    fun factorial(n: Long): Result<Long> {
        return when {
            n < 0 -> Result.failure(IllegalArgumentException("Faktöriyel negatif sayılar için tanımsızdır"))
            n > 20 -> Result.failure(IllegalArgumentException("Faktöriyel çok büyük (max 20!)"))
            else -> Result.success((1..n).fold(1L) { acc, i -> acc * i })
        }
    }

    /**
     * Calculate permutation P(n,r) = n! / (n-r)!
     */
    fun permutation(n: Long, r: Long): Result<Long> {
        return when {
            n < 0 || r < 0 -> Result.failure(IllegalArgumentException("Değerler negatif olamaz"))
            r > n -> Result.failure(IllegalArgumentException("r, n'den büyük olamaz"))
            else -> Result.success(((n - r + 1)..n).fold(1L) { acc, i -> acc * i })
        }
    }

    /**
     * Calculate combination C(n,r) = n! / (r! * (n-r)!)
     */
    fun combination(n: Long, r: Long): Result<Long> {
        return when {
            n < 0 || r < 0 -> Result.failure(IllegalArgumentException("Değerler negatif olamaz"))
            r > n -> Result.failure(IllegalArgumentException("r, n'den büyük olamaz"))
            else -> {
                val numerator = ((n - r + 1)..n).fold(1L) { acc, i -> acc * i }
                val denominator = (1..r).fold(1L) { acc, i -> acc * i }
                Result.success(numerator / denominator)
            }
        }
    }
}

