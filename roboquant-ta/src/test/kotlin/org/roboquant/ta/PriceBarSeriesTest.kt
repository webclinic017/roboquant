/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.ta

import org.junit.jupiter.api.Test
import org.roboquant.common.Asset
import org.roboquant.common.plus
import org.roboquant.feeds.Event
import org.roboquant.feeds.PriceBar
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceBarSeriesTest {

    private val asset = Asset("DEMO")
    private val pb = PriceBar(asset, 10, 11, 9, 10, 100)

    private fun getPBS(size: Int): PriceBarSeries {
        val pbs = PriceBarSeries(size)
        val ohlvc = doubleArrayOf(100.0, 101.0, 99.0, 100.0, 10000.0)
        repeat(size) {
            val newOhlcv = ohlvc + it
            newOhlcv[4] = 10000.0
            val pb = PriceBar(Asset("ABC"), newOhlcv)
            pbs.add(pb)
        }
        return pbs
    }

    @Test
    fun test() {
        val pbs = PriceBarSeries(10)
        repeat(5) { pbs.add(pb) }
        assertFalse(pbs.isFull())
        assertEquals(5, pbs.size)

        repeat(5) { pbs.add(pb) }
        assertTrue(pbs.isFull())
        assertEquals(10, pbs.size)

        repeat(5) { pbs.add(pb) }
        assertTrue(pbs.isFull())
        assertEquals(10, pbs.size)

        assertEquals(10, pbs.open.size)
        assertEquals(10, pbs.typical.size)

        pbs.clear()
        assertFalse(pbs.isFull())
        assertEquals(0, pbs.open.size)
        assertTrue { pbs.close.all { it.isNaN() } }
    }

    @Test
    fun test2() {
        val pbs = AssetPriceBarSeries(10)
        repeat(5) { pbs.add(pb) }
        assertEquals(1, pbs.size)

        var priceBarSerie = pbs.getValue(asset)
        assertFalse(priceBarSerie.isFull())

        val event = Event(listOf(pb), Instant.now())
        repeat(5) {
            pbs.addAll(event)
        }
        assertEquals(1, pbs.size)
        priceBarSerie = pbs.getValue(asset)
        assertTrue(priceBarSerie.isFull())
    }

    @Test
    fun agg() {
        val pbs = getPBS(93)
        assertTrue(pbs.isFull())

        // an aggregate of one is just an expensive copy ;)
        val agg1 = pbs.aggregate(1)
        assertEquals(agg1.high.toList(), pbs.high.toList())
        assertEquals(agg1.volume.toList(), pbs.volume.toList())
        assertEquals(agg1[1].toList(), pbs[1].toList())

        // aggregate 10 entries
        val pbs2 = pbs.aggregate(10)
        assertTrue(pbs2.isFull())
        assertEquals(9, pbs2.size) // should not have processed the last incomplete set

        assertEquals(100.0, pbs2.open.first())
        assertEquals(109.0, pbs2.close.first())
        assertEquals(100000.0, pbs2.volume.first())

        assertEquals(110.0, pbs2.high.first())
        assertEquals(99.0, pbs2.low.first())

        assertEquals(190.0, pbs2.high.last())
        assertEquals(179.0, pbs2.low.last())
    }

    @Test
    fun agg2() {
        val pbs = getPBS(100)
        assertTrue(pbs.isFull())

        // aggregate 10 entries
        val pbs2 = pbs.aggregate(10)
        assertTrue(pbs2.isFull())
        assertEquals(10, pbs2.size) // should have processed the last set
    }



}