/*
 * Copyright 2020-2023 Neural Layer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.roboquant.server

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.runBlocking
import org.roboquant.Roboquant
import org.roboquant.common.TimeSpan
import org.roboquant.common.Timeframe
import org.roboquant.feeds.Feed
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set

// Store all the instantiated runs
internal val runs = ConcurrentHashMap<String, RunInfo>()

/**
 * Create a server with credentials. The website will be protected using digest authentication.
 */
class WebServer(username: String, password: String, port: Int = 8080, host: String = "127.0.01") {

    /**
     * Create server instance without credentials.
     * So, anybody can see the runs, pause them and stop the server.
     */
    constructor(port: Int = 8080, host: String = "127.0.01") : this("", "", port, host)

    private var runCounter = 0
    private val server: NettyApplicationEngine = embeddedServer(
        Netty,
        port = port,
        host = host,
        module = {
            if (username.isNotEmpty()) secureSetup(username, password) else setup()
        }
    ).start(wait = false)

    init {
        logger.info { "web server started on $host:$port" }
    }

    /**
     * Stop the web server and close all feeds
     */
    fun stop() {
        logger.info { "stopping web server" }
        server.stop()

        // Close the feeds
        runs.forEach {
            logger.info { "closing feed run=${it.key} " }
            it.value.feed.close()
        }
    }

    @Synchronized
    fun getRunName(): String {
        return "run-${runCounter++}"
    }

    fun run(
        roboquant: Roboquant,
        feed: Feed,
        timeframe: Timeframe,
        name: String = getRunName(),
        warmup: TimeSpan = TimeSpan.ZERO
    ) = runBlocking {
        runAsync(roboquant, feed, timeframe, name, warmup)
    }

    /**
     * Start a new run and make core metrics available to the webserver. You can start multiple runs in the same
     * webserver instance. Each run will have its unique name.
     */
    suspend fun runAsync(
        roboquant: Roboquant,
        feed: Feed,
        timeframe: Timeframe,
        name: String = getRunName(),
        warmup: TimeSpan = TimeSpan.ZERO
    ) {
        require(! runs.contains(name)) { "run name has to be unique, name=$name is already in use by another run."}
        val rq = roboquant.copy(policy = PausablePolicy(roboquant.policy))
        runs[name] = RunInfo(rq, feed, timeframe, warmup)
        logger.info { "Starting new run name=$name timeframe=$timeframe"}
        rq.runAsync(feed, timeframe, name, warmup)
    }

}

