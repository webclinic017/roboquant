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

package org.roboquant.jupyter

import org.icepear.echarts.Option
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.io.TempDir
import org.roboquant.feeds.random.RandomWalkFeed
import java.io.File
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

internal class ChartTest {

    @TempDir
    lateinit var folder: File

    private class MyChart : Chart() {

        init {
            javascriptFunction("return p;")
        }

        override fun getOption(): Option {
            return Option()
        }

    }

    @Test
    fun test() {
        val f = RandomWalkFeed.lastYears(1, 1, generateBars = true)
        val asset = f.assets.first()
        val chart = PriceBarChart(f, asset)
        assertTrue(chart.asHTML().isNotBlank())
        assertEquals(700, chart.height)
        assertContains(chart.asHTML(), asset.symbol)

        val file = File(folder, "test.html")
        chart.toHTMLFile(file.toString())
        assertTrue(file.exists())

    }


    @Test
    fun testCodeGeneration() {
        val chart = MyChart()
        chart.height = 123
        Chart.debug = true

        assertDoesNotThrow {
            chart.getOption().renderJson()
        }

        assertDoesNotThrow {
            chart.render()
        }

        val code = chart.asHTML()
        assertContains(code, "123px")
        assertContains(code, "console.log")
        assertContains(code, "new Function")

        Chart.debug = false
        val code2 = chart.asHTML()
        assertFalse(code2.contains("console.log"))

    }

}