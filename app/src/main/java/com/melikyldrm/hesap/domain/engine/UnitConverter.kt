package com.melikyldrm.hesap.domain.engine

import com.melikyldrm.hesap.domain.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitConverter @Inject constructor() {

    // ==================== LENGTH ====================
    fun convertLength(value: Double, from: LengthUnit, to: LengthUnit): Double {
        val valueInMeters = value * from.toMeter
        return valueInMeters / to.toMeter
    }

    // ==================== WEIGHT ====================
    fun convertWeight(value: Double, from: WeightUnit, to: WeightUnit): Double {
        val valueInGrams = value * from.toGram
        return valueInGrams / to.toGram
    }

    // ==================== AREA ====================
    fun convertArea(value: Double, from: AreaUnit, to: AreaUnit): Double {
        val valueInSquareMeters = value * from.toSquareMeter
        return valueInSquareMeters / to.toSquareMeter
    }

    // ==================== VOLUME ====================
    fun convertVolume(value: Double, from: VolumeUnit, to: VolumeUnit): Double {
        val valueInLiters = value * from.toLiter
        return valueInLiters / to.toLiter
    }

    // ==================== TEMPERATURE ====================
    fun convertTemperature(value: Double, from: TemperatureUnit, to: TemperatureUnit): Double {
        // First convert to Celsius
        val celsius = when (from) {
            TemperatureUnit.CELSIUS -> value
            TemperatureUnit.FAHRENHEIT -> (value - 32) * 5 / 9
            TemperatureUnit.KELVIN -> value - 273.15
        }

        // Then convert from Celsius to target
        return when (to) {
            TemperatureUnit.CELSIUS -> celsius
            TemperatureUnit.FAHRENHEIT -> celsius * 9 / 5 + 32
            TemperatureUnit.KELVIN -> celsius + 273.15
        }
    }

    // ==================== DATA ====================
    fun convertData(value: Double, from: DataUnit, to: DataUnit): Double {
        val valueInBytes = value * from.toBytes
        return valueInBytes / to.toBytes
    }

    // ==================== TIME ====================
    fun convertTime(value: Double, from: TimeUnit, to: TimeUnit): Double {
        val valueInSeconds = value * from.toSeconds
        return valueInSeconds / to.toSeconds
    }

    // ==================== SPEED ====================
    fun convertSpeed(value: Double, from: SpeedUnit, to: SpeedUnit): Double {
        val valueInMps = value * from.toMeterPerSecond
        return valueInMps / to.toMeterPerSecond
    }

    // ==================== GENERIC CONVERSION ====================
    fun convert(
        value: Double,
        fromUnit: String,
        toUnit: String,
        category: UnitCategory
    ): ConversionResult {
        val result = when (category) {
            UnitCategory.LENGTH -> {
                val from = LengthUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen uzunluk birimi: $fromUnit")
                val to = LengthUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen uzunluk birimi: $toUnit")
                convertLength(value, from, to)
            }
            UnitCategory.WEIGHT -> {
                val from = WeightUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen ağırlık birimi: $fromUnit")
                val to = WeightUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen ağırlık birimi: $toUnit")
                convertWeight(value, from, to)
            }
            UnitCategory.AREA -> {
                val from = AreaUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen alan birimi: $fromUnit")
                val to = AreaUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen alan birimi: $toUnit")
                convertArea(value, from, to)
            }
            UnitCategory.VOLUME -> {
                val from = VolumeUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen hacim birimi: $fromUnit")
                val to = VolumeUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen hacim birimi: $toUnit")
                convertVolume(value, from, to)
            }
            UnitCategory.TEMPERATURE -> {
                val from = TemperatureUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen sıcaklık birimi: $fromUnit")
                val to = TemperatureUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen sıcaklık birimi: $toUnit")
                convertTemperature(value, from, to)
            }
            UnitCategory.DATA -> {
                val from = DataUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen veri birimi: $fromUnit")
                val to = DataUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen veri birimi: $toUnit")
                convertData(value, from, to)
            }
            UnitCategory.TIME -> {
                val from = TimeUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen zaman birimi: $fromUnit")
                val to = TimeUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen zaman birimi: $toUnit")
                convertTime(value, from, to)
            }
            UnitCategory.SPEED -> {
                val from = SpeedUnit.entries.find { it.symbol == fromUnit || it.displayName == fromUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen hız birimi: $fromUnit")
                val to = SpeedUnit.entries.find { it.symbol == toUnit || it.displayName == toUnit }
                    ?: throw IllegalArgumentException("Bilinmeyen hız birimi: $toUnit")
                convertSpeed(value, from, to)
            }
        }

        return ConversionResult(
            fromValue = value,
            fromUnit = fromUnit,
            toValue = result,
            toUnit = toUnit,
            category = category
        )
    }

    /**
     * Format conversion result for display
     */
    fun formatResult(value: Double, decimals: Int = 6): String {
        return if (value == value.toLong().toDouble()) {
            value.toLong().toString()
        } else {
            String.format("%.${decimals}f", value).trimEnd('0').trimEnd('.')
        }
    }

    /**
     * Get all units for a category
     */
    fun getUnitsForCategory(category: UnitCategory): List<Pair<String, String>> {
        return when (category) {
            UnitCategory.LENGTH -> LengthUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.WEIGHT -> WeightUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.AREA -> AreaUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.VOLUME -> VolumeUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.TEMPERATURE -> TemperatureUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.DATA -> DataUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.TIME -> TimeUnit.entries.map { it.symbol to it.displayName }
            UnitCategory.SPEED -> SpeedUnit.entries.map { it.symbol to it.displayName }
        }
    }
}

