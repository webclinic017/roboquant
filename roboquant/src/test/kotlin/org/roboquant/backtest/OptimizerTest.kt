package org.roboquant.backtest

import org.junit.jupiter.api.Test
import org.roboquant.Roboquant
import org.roboquant.common.months
import org.roboquant.feeds.random.RandomWalkFeed
import org.roboquant.loggers.LastEntryLogger
import org.roboquant.metrics.AccountMetric
import org.roboquant.strategies.EMAStrategy
import kotlin.test.assertTrue

class OptimizerTest {

    @Test
    fun basic() {
        val space = GridSearch()
        space.add("x", 3..15)
        space.add("y", 2..10)

        val opt = Optimizer(space, "account.equity") { params ->
            val x = params.getInt("x")
            val y = x + params.getInt("y")
            val s = EMAStrategy(x, y)
            Roboquant(s, AccountMetric(), logger = LastEntryLogger())
        }

        val feed = RandomWalkFeed.lastYears(1, nAssets = 1)

        val r1 = opt.train(feed, feed.timeframe)
        assertTrue(r1.isNotEmpty())

        val r2 = opt.walkForward(feed, 6.months, 2.months)
        assertTrue(r2.isNotEmpty())

        val r3 = opt.monteCarlo(feed, 6.months, 3.months, 5)
        assertTrue(r3.isNotEmpty())

    }

    @Test
    fun complete() {
        val space = GridSearch()
        space.add("x", 3..15)
        space.add("y", 2..10)

        val logger = LastEntryLogger()
        val opt = Optimizer(space, "account.equity") { params ->
            val x = params.getInt("x")
            val y = x + params.getInt("y")
            val s = EMAStrategy(x, y)
            Roboquant(s, AccountMetric(), logger = logger)
        }

        val feed = RandomWalkFeed.lastYears(3, nAssets = 2)
        val r2 = opt.walkForward(feed, 9.months, 3.months, 0.months, false)
        assertTrue(r2.isNotEmpty())
    }

    @Test
    fun noParams() {
        val space = EmptySearchSpace()
        val opt = Optimizer(space, "account.equity") {
            Roboquant(EMAStrategy(), AccountMetric(), logger = LastEntryLogger())
        }

        val feed = RandomWalkFeed.lastYears(1, nAssets = 1)
        val r1 = opt.train(feed, feed.timeframe)
        assertTrue(r1.isNotEmpty())

    }


}