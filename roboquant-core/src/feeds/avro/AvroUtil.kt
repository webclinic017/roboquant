/*
 * Copyright 2021 Neural Layer
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

package org.roboquant.feeds.avro

import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.avro.Schema
import org.apache.avro.file.CodecFactory
import org.apache.avro.file.DataFileWriter
import org.apache.avro.generic.GenericData
import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.roboquant.common.Asset
import org.roboquant.common.Logging
import org.roboquant.common.TimeFrame
import org.roboquant.feeds.*
import java.io.File

/**
 * Utility to create an Avro file based on feed. It only records actions of the type [PriceBar].
 *
 * Typical use case is to turn a large set of CSV files into a single Avro file and use that for future back-tests.
 */
object AvroUtil {

    private val logger = Logging.getLogger(this::class)

    private const val schemaDef = """
            {
             "namespace": "org.roboquant",
             "type": "record",
             "name": "priceBars",
             "fields": [
                 {"name": "time", "type": "long"},
                 {"name": "asset", "type": "string"},
                 {"name": "type", "type": "int"},
                 {"name": "values",  "type": {"type": "array", "items" : "double"}}
             ] 
            }  
            """

    /**
     * Record the price actions in a feed and store them in a avro file that can be used with the AvroFeed
     */
    fun record(feed: Feed, fileName: String, timeFrame: TimeFrame = TimeFrame.FULL, compressionLevel: Int = 1) =
        runBlocking {

            val channel = EventChannel(timeFrame = timeFrame)
            val file = File(fileName)
            val schema = Schema.Parser().parse(schemaDef)
            val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(schema)
            val dataFileWriter = DataFileWriter(datumWriter)
            dataFileWriter.setCodec(CodecFactory.deflateCodec(compressionLevel))

            val tf = feed.timeFrame.intersect(timeFrame)
            dataFileWriter.setMeta("roboquant.start", tf.start.toEpochMilli())
            dataFileWriter.setMeta("roboquant.end", tf.end.toEpochMilli())

            dataFileWriter.create(schema, file)

            val cache = mutableMapOf<Asset, String>()

            val job = launch {
                feed.play(channel)
                channel.close()
            }

            val arraySchema = Schema.createArray(Schema.create(Schema.Type.DOUBLE))
            try {
                val record = GenericData.Record(schema)
                while (true) {
                    val event = channel.receive()
                    val now = event.now.toEpochMilli()
                    for (action in event.actions.filterIsInstance<PriceAction>()) {
                        val assetStr = cache.getOrPut(action.asset) { action.asset.serialize() }
                        record.put(0, now)
                        record.put(1, assetStr)
                        val values : List<Double> = when (action) {
                            is PriceBar -> { record.put(2, 1); action.values() }
                            is TradePrice -> { record.put(2, 2); action.values() }
                            is PriceQuote -> { record.put(2, 3); action.values() }
                            is OrderBook -> { record.put(2, 4); action.values() }
                            else -> {
                                logger.warning("Unsupported price action encountered $action")
                                continue
                            }
                        }

                        val arr = GenericData.Array<Double>(values.size, arraySchema)
                        arr.addAll(values)
                        record.put(3, arr)
                        dataFileWriter.append(record)
                    }

                    // We sync after each event, so we can later create an index if required
                    dataFileWriter.sync()
                }

            } catch (e: ClosedReceiveChannelException) {
                // On purpose left empty, expected exception
            } finally {
                channel.close()
                if (job.isActive) job.cancel()
                dataFileWriter.sync()
                dataFileWriter.close()
            }
        }
}