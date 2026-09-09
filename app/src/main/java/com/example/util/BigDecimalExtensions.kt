package com.example.util

import java.math.BigDecimal
import java.math.RoundingMode

fun Iterable<BigDecimal>.sum(): BigDecimal {
    var sum = BigDecimal.ZERO
    for (element in this) {
        sum += element
    }
    return sum
}

fun <T> Iterable<T>.sumOf(selector: (T) -> BigDecimal): BigDecimal {
    var sum = BigDecimal.ZERO
    for (element in this) {
        sum += selector(element)
    }
    return sum
}

fun BigDecimal.safeDiv(other: BigDecimal, scale: Int = 8): BigDecimal {
    return if (other.compareTo(BigDecimal.ZERO) == 0) {
        BigDecimal.ZERO
    } else {
        this.divide(other, scale, RoundingMode.HALF_UP)
    }
}
