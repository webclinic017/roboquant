/*
 * Copyright 2020-2022 Neural Layer
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
import org.roboquant.feeds.PriceBar
import org.roboquant.feeds.RandomWalkFeed
import org.roboquant.feeds.filter
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class PriceBarSeriesTest {

    private val feed = RandomWalkFeed.lastYears(1, 2)

    @Test
    fun test() {
        val feed = feed
        val asset = feed.assets.first()
        val pb = PriceBarSeries(20)
        assertFalse(pb.isFilled())
        val data = feed.filter<PriceBar> { it.asset === asset }
        for (entry in data) {
            pb.add(entry.second)
        }
        assertTrue(pb.isFilled())
        assertTrue(pb.typical.isNotEmpty())
        assertTrue(pb.volume.isNotEmpty())
        pb.clear()
        assertFalse(pb.isFilled())
    }

    @Test
    fun test2() {
        val feed = feed
        val asset1 = feed.assets.first()
        val pb = MultiAssetPriceBarSeries(10)
        assertFalse(pb.isFilled(asset1))
        val data = feed.filter<PriceBar> { it.asset === asset1 }
        for (entry in data) {
            pb.add(entry.second)
        }
        assertTrue(pb.isFilled(asset1))
    }

    @Test
    fun adjustedClose() {
        val asset = Asset("DEMO")
        val pb = PriceBar(asset, 10, 11, 9, 10, 100)
        pb.adjustClose(5)
        assertEquals(5.0, pb.open)
        assertEquals(5.0, pb.close)
        assertEquals(200.0, pb.volume)
    }




}