package com.example.bakersrecipes.data.datatypes

import java.math.BigDecimal
import java.math.RoundingMode

class Percentage(private val value: BigDecimal) : Comparable<Percentage> {
    constructor(value: String) : this(parsePercentageStringToDecimal(value))

    private fun formatValue(): String {
        return value.multiply(BigDecimal(100)).setScale(2, RoundingMode.HALF_UP)
            .stripTrailingZeros().toPlainString()
    }

    fun toBigDecimal(): BigDecimal {
        return value
    }

    override fun toString(): String {
        return formatValue()
    }

    fun toUnformattedString() : String {
        return value.setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString()
    }

    operator fun plus(other: Percentage): Percentage {
        return Percentage(value.add(other.value))
    }

    operator fun minus(other: Percentage): Percentage {
        return Percentage(value.subtract(other.value))
    }

    operator fun times(other: Percentage): Percentage {
        return Percentage(value.multiply(other.value))
    }

    operator fun div(other: Percentage): Percentage {
        return Percentage(value.divide(other.value, 10, RoundingMode.HALF_UP))
    }

    operator fun div(other: Int): Percentage {
        return Percentage(value.divide(BigDecimal(other), 10, RoundingMode.HALF_UP))
    }

    override operator fun compareTo(other: Percentage): Int {
        return value.compareTo(other.value)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Percentage) return false
        return value.compareTo(other.value) == 0
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }

    companion object {
        fun fromString(input: String): Percentage {
            val decimalValue = BigDecimal(input)
            return Percentage(decimalValue)
        }

        fun fromFromPercentageString(input: String): Percentage {
            return Percentage(parsePercentageStringToDecimal(input))
        }

        private fun parsePercentageStringToDecimal(percentageString: String): BigDecimal {
            val cleanedString = percentageString.replace("%", "").trim()
            return BigDecimal(cleanedString).divide(BigDecimal(100), 10, RoundingMode.HALF_UP)
        }
    }
}

inline fun <reified T> Iterable<T>.sumOf(selector: (T) -> Percentage): Percentage {
    return this.map(selector).fold(Percentage(BigDecimal.ZERO)) { accumulator, percentage ->
        accumulator + percentage
    }
}

inline fun <R : Comparable<R>> Iterable<Percentage>.maxOf(selector: (Percentage) -> R): R {
    return maxOfOrNull(selector) ?: error("Empty collection")
}

fun Int.toPercentage(): Percentage {
    return Percentage(this.toBigDecimal())
}

fun Long.toPercentage(): Percentage {
    return Percentage(this.toBigDecimal())
}

fun Float.toPercentage(): Percentage {
    return Percentage(this.toBigDecimal())
}

fun Double.toPercentage(): Percentage {
    return Percentage(this.toBigDecimal())
}

fun BigDecimal.toPercentage(): Percentage {
    return Percentage(this)
}

fun String.toPercentage(isPercentageString: Boolean): Percentage {
    return if(isPercentageString)
        Percentage.fromFromPercentageString(this)
    else
        Percentage.fromString(this)
}