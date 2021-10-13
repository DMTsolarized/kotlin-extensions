@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.common.spark

import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.spark.streaming.api.java.JavaInputDStream
import org.apache.spark.streaming.api.java.JavaStreamingContext
import org.apache.spark.streaming.kafka010.*
import ru.inforion.lab403.common.kafka.KafkaProperties

inline fun <K, V> kafkaSubscription(topic: String, properties: KafkaProperties) =
    Subscribe<K, V>(listOf(topic), properties, emptyMap())

inline fun <K, V> Subscribe<K, V>.stream(
    sc: JavaStreamingContext,
    strategy: LocationStrategy = LocationStrategies.PreferBrokers()
): JavaInputDStream<ConsumerRecord<K, V>> = KafkaUtils.createDirectStream(sc, strategy, this)

inline fun <T> JavaInputDStream<T>.commit(offsets: Array<OffsetRange>) =
    (this.inputDStream() as CanCommitOffsets).commitAsync(offsets)