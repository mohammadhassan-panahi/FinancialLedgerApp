package com.example.util

import com.example.util.GoldMarketFormulas.GoldItemType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GoldMarketFormulasTest {

    @Test
    fun `coin bubble is market price minus intrinsic value`() {
        // Emami coin: 8.133g x 0.875 = 7.116375g pure gold
        val result = GoldMarketFormulas.calculateGoldBubble(
            itemType = GoldItemType.EMAAMI,
            ouncePriceUsd = 4000.0,
            dollarPriceToman = 100_000.0,
            marketPriceToman = 100_000_000.0
        )!!
        // intrinsic = 4000 / 31.1035 x 100000 x 7.116375 = ~91.52M
        assertEquals(91_517_900.0, result.intrinsicValueToman, 2_000.0)
        assertEquals(8_482_100.0, result.bubbleAmountToman, 2_000.0)
        assertEquals(9.27, result.bubblePercent, 0.1)
        assertTrue(result.bubbleAmountToman > 0)
    }

    @Test
    fun `negative bubble is detected for underpriced market`() {
        val result = GoldMarketFormulas.calculateGoldBubble(
            itemType = GoldItemType.GOLD_18K,
            ouncePriceUsd = 4000.0,
            dollarPriceToman = 100_000.0,
            marketPriceToman = 2_900_000.0 // intrinsic ~3.07M -> slightly below
        )!!
        assertTrue(result.bubbleAmountToman < 0)
    }

    @Test
    fun `bubble rejects non-positive inputs`() {
        assertNull(
            GoldMarketFormulas.calculateGoldBubble(GoldItemType.EMAAMI, 0.0, 100_000.0, 100_000_000.0)
        )
        assertNull(
            GoldMarketFormulas.calculateGoldBubble(GoldItemType.EMAAMI, 4000.0, 0.0, 100_000_000.0)
        )
        assertNull(
            GoldMarketFormulas.calculateGoldBubble(GoldItemType.EMAAMI, 4000.0, 100_000.0, 0.0)
        )
    }

    @Test
    fun `new gold price follows the union formula`() {
        // new: base + 7% wage + 7% profit + 9% VAT on (wage+profit)
        val result = GoldMarketFormulas.calculateGoldPurchasePrice(
            basePricePerGram = 50_000_000.0,
            weightGrams = 5.0,
            wagePercent = 7.0,
            sellerProfitPercent = 7.0,
            vatPercent = 9.0,
            isNew = true
        )!!
        assertEquals(3_500_000.0, result.wagePerGram, 0.01)
        assertEquals(3_500_000.0, result.sellerProfitPerGram, 0.01)
        assertEquals(630_000.0, result.vatPerGram, 0.01)
        assertEquals(57_630_000.0, result.finalPricePerGram, 0.01)
        assertEquals(288_150_000.0, result.finalPriceTotal, 0.01)
    }

    @Test
    fun `second-hand gold has no seller profit and vat only on wage`() {
        val result = GoldMarketFormulas.calculateGoldPurchasePrice(
            basePricePerGram = 50_000_000.0,
            weightGrams = 5.0,
            wagePercent = 7.0,
            sellerProfitPercent = 7.0,
            vatPercent = 9.0,
            isNew = false
        )!!
        assertEquals(0.0, result.sellerProfitPerGram, 0.01)
        assertEquals(315_000.0, result.vatPerGram, 0.01)
        assertEquals(53_815_000.0, result.finalPricePerGram, 0.01)
        assertEquals(269_075_000.0, result.finalPriceTotal, 0.01)
    }

    @Test
    fun `retrospective computes units bought and today value`() {
        val result = GoldMarketFormulas.calculateRetrospective(
            amountToman = 100_000_000.0,
            pastPriceToman = 50_000_000.0,
            currentPriceToman = 75_000_000.0
        )!!
        assertEquals(2.0, result.quantity, 0.0001)
        assertEquals(150_000_000.0, result.currentValue, 0.01)
        assertEquals(50_000_000.0, result.profitAmount, 0.01)
        assertEquals(50.0, result.profitPercent, 0.01)
    }

    @Test
    fun `retrospective rejects zero past price`() {
        assertNull(GoldMarketFormulas.calculateRetrospective(100.0, 0.0, 200.0))
    }

    @Test
    fun `scenario revalues portfolio legs`() {
        val result = GoldMarketFormulas.calculateScenario(
            listOf(
                Triple("طلا", 100_000_000.0, 20.0),
                Triple("دلار", 50_000_000.0, 0.0),
                Triple("سهام", 50_000_000.0, -10.0)
            )
        )!!
        assertEquals(200_000_000.0, result.currentValue, 0.01)
        assertEquals(215_000_000.0, result.simulatedValue, 0.01)
        assertEquals(15_000_000.0, result.changeAmount, 0.01)
        assertEquals(7.5, result.changePercent, 0.01)
        assertEquals(120_000_000.0, result.legs[0].simulatedValue, 0.01)
    }

    @Test
    fun `grams buyable divides amount by gram price`() {
        assertEquals(2.0, GoldMarketFormulas.gramsBuyable(100_000_000.0, 50_000_000.0)!!, 0.0001)
        assertNull(GoldMarketFormulas.gramsBuyable(100_000_000.0, 0.0))
    }

    @Test
    fun `coin pure gold weights match central bank specs`() {
        assertEquals(7.116375, GoldItemType.EMAAMI.pureGoldGrams, 0.0001)
        assertEquals(1.778350, GoldItemType.QUARTER_COIN.pureGoldGrams, 0.0001)
        assertEquals(0.75, GoldItemType.GOLD_18K.pureGoldGrams, 0.0001)
    }
}
