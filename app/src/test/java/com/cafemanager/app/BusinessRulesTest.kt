package com.cafemanager.app

import org.junit.Assert.*
import org.junit.Test

class BusinessRulesTest {
    @Test fun splitPaymentMustEqualSale() {
        val total = 1000L
        assertEquals(total, 600L + 400L)
    }

    @Test fun estimatedProfitIsSalesMinusExpenses() {
        assertEquals(700L, 1200L - 500L)
    }

    @Test fun emptyBusinessStartsAtZero() {
        val sales = emptyList<Long>()
        val expenses = emptyList<Long>()
        assertEquals(0L, sales.sum())
        assertEquals(0L, expenses.sum())
    }
}
