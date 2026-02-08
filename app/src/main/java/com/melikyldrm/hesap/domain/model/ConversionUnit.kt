package com.melikyldrm.hesap.domain.model

// Unit conversion types
enum class UnitCategory(val displayName: String) {
    LENGTH("Uzunluk"),
    WEIGHT("Ağırlık"),
    AREA("Alan"),
    VOLUME("Hacim"),
    TEMPERATURE("Sıcaklık"),
    DATA("Veri Boyutu"),
    TIME("Zaman"),
    SPEED("Hız")
}

// Length Units
enum class LengthUnit(val symbol: String, val displayName: String, val toMeter: Double) {
    MILLIMETER("mm", "Milimetre", 0.001),
    CENTIMETER("cm", "Santimetre", 0.01),
    METER("m", "Metre", 1.0),
    KILOMETER("km", "Kilometre", 1000.0),
    INCH("in", "İnç", 0.0254),
    FOOT("ft", "Fit", 0.3048),
    YARD("yd", "Yarda", 0.9144),
    MILE("mi", "Mil", 1609.344),
    NAUTICAL_MILE("nmi", "Deniz Mili", 1852.0)
}

// Weight Units
enum class WeightUnit(val symbol: String, val displayName: String, val toGram: Double) {
    MILLIGRAM("mg", "Miligram", 0.001),
    GRAM("g", "Gram", 1.0),
    KILOGRAM("kg", "Kilogram", 1000.0),
    TON("t", "Ton", 1_000_000.0),
    OUNCE("oz", "Ons", 28.3495),
    POUND("lb", "Libre", 453.592),
    STONE("st", "Stone", 6350.29)
}

// Area Units
enum class AreaUnit(val symbol: String, val displayName: String, val toSquareMeter: Double) {
    SQUARE_MILLIMETER("mm²", "Milimetrekare", 0.000001),
    SQUARE_CENTIMETER("cm²", "Santimetrekare", 0.0001),
    SQUARE_METER("m²", "Metrekare", 1.0),
    SQUARE_KILOMETER("km²", "Kilometrekare", 1_000_000.0),
    HECTARE("ha", "Hektar", 10_000.0),
    ACRE("ac", "Akre", 4046.86),
    SQUARE_FOOT("ft²", "Fitkare", 0.092903),
    SQUARE_YARD("yd²", "Yardakare", 0.836127),
    DONUM("dönüm", "Dönüm", 1000.0)
}

// Volume Units
enum class VolumeUnit(val symbol: String, val displayName: String, val toLiter: Double) {
    MILLILITER("mL", "Mililitre", 0.001),
    LITER("L", "Litre", 1.0),
    CUBIC_METER("m³", "Metreküp", 1000.0),
    CUBIC_CENTIMETER("cm³", "Santimetreküp", 0.001),
    GALLON_US("gal", "Galon (US)", 3.78541),
    GALLON_UK("gal UK", "Galon (UK)", 4.54609),
    QUART("qt", "Quart", 0.946353),
    PINT("pt", "Pint", 0.473176),
    CUP("cup", "Fincan", 0.236588),
    FLUID_OUNCE("fl oz", "Sıvı Ons", 0.0295735)
}

// Temperature Units (special handling needed)
enum class TemperatureUnit(val symbol: String, val displayName: String) {
    CELSIUS("°C", "Santigrat"),
    FAHRENHEIT("°F", "Fahrenheit"),
    KELVIN("K", "Kelvin")
}

// Data Units
enum class DataUnit(val symbol: String, val displayName: String, val toBytes: Double) {
    BIT("bit", "Bit", 0.125),
    BYTE("B", "Bayt", 1.0),
    KILOBYTE("KB", "Kilobayt", 1024.0),
    MEGABYTE("MB", "Megabayt", 1024.0 * 1024.0),
    GIGABYTE("GB", "Gigabayt", 1024.0 * 1024.0 * 1024.0),
    TERABYTE("TB", "Terabayt", 1024.0 * 1024.0 * 1024.0 * 1024.0),
    PETABYTE("PB", "Petabayt", 1024.0 * 1024.0 * 1024.0 * 1024.0 * 1024.0),
    KIBIBIT("Kibit", "Kibibit", 128.0),
    MEBIBIT("Mibit", "Mebibit", 128.0 * 1024.0),
    GIBIBIT("Gibit", "Gibibit", 128.0 * 1024.0 * 1024.0)
}

// Time Units
enum class TimeUnit(val symbol: String, val displayName: String, val toSeconds: Double) {
    MILLISECOND("ms", "Milisaniye", 0.001),
    SECOND("s", "Saniye", 1.0),
    MINUTE("min", "Dakika", 60.0),
    HOUR("h", "Saat", 3600.0),
    DAY("gün", "Gün", 86400.0),
    WEEK("hafta", "Hafta", 604800.0),
    MONTH("ay", "Ay (30 gün)", 2592000.0),
    YEAR("yıl", "Yıl (365 gün)", 31536000.0)
}

// Speed Units
enum class SpeedUnit(val symbol: String, val displayName: String, val toMeterPerSecond: Double) {
    METER_PER_SECOND("m/s", "Metre/Saniye", 1.0),
    KILOMETER_PER_HOUR("km/h", "Kilometre/Saat", 0.277778),
    MILE_PER_HOUR("mph", "Mil/Saat", 0.44704),
    KNOT("kn", "Knot", 0.514444),
    FOOT_PER_SECOND("ft/s", "Fit/Saniye", 0.3048)
}

// Conversion Result
data class ConversionResult(
    val fromValue: Double,
    val fromUnit: String,
    val toValue: Double,
    val toUnit: String,
    val category: UnitCategory
)

