@file:Suppress("NOTHING_TO_INLINE", "unused")

package ru.inforion.lab403.common.kafka

import org.apache.kafka.clients.admin.*
import org.apache.kafka.clients.admin.OffsetSpec
import org.apache.kafka.clients.consumer.OffsetAndMetadata
import org.apache.kafka.common.KafkaFuture
import org.apache.kafka.common.Node
import org.apache.kafka.common.TopicPartition
import ru.inforion.lab403.common.extensions.*
import ru.inforion.lab403.common.logging.logger
import java.io.Closeable


class KafkaAdminTool constructor(val brokers: String, val timeout: Long) : Closeable {
    companion object {
        val log = logger()
    }

    private val client = AdminClient.create(
        mapOf(
            AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG to brokers
        )
    )

    fun nodes(): Collection<Node> = client.describeCluster().nodes().getOrThrow(timeout)

    fun clusterId(): String = client.describeCluster().clusterId().getOrThrow(timeout)

    fun check(): Boolean = client.describeCluster().clusterId().getOrNull(timeout) != null

    fun listTopics(): Set<String> = client.listTopics().names().getOrThrow(timeout)

    fun deleteTopics(topics: Collection<String>) { client.deleteTopics(topics).all().getOrThrow() }

    fun createTopics(topics: Collection<NewTopic>) { client.createTopics(topics).all().getOrThrow(timeout) }

    fun describeTopics(topics: Collection<String>) = client.describeTopics(topics).all().getOrThrow(timeout).values

    fun describeGroups(groups: Collection<String>): MutableMap<String, KafkaFuture<ConsumerGroupDescription>> =
        client.describeConsumerGroups(groups).describedGroups()

    fun listOffsets(offsets: Collection<TopicPartition>, offsetSpec: OffsetSpec): Map<TopicPartition, Long> {
        val parameters = offsets.associateWith { offsetSpec }
        val result = client.listOffsets(parameters).all().getOrThrow(timeout)
        return result.associate { it.key to it.value.offset() }
    }

    fun listConsumerGroupOffsets(group: String): Map<TopicPartition, Long> =
        client.listConsumerGroupOffsets(group)
            .partitionsToOffsetAndMetadata()
            .getOrThrow(timeout)
            .associate {
                it.key to it.value.offset()
            }

    fun topicsInfo(topics: Collection<String>) = describeTopics(topics).map { topic ->
        val parameters = topic.toTopicPartitions()
        val partitions = listOffsets(parameters, OffsetSpec.latest()).map { (partition, offset) ->
            PartitionInfo(partition.partition(), -1, offset)
        }
        TopicInfo(topic.name(), partitions)
    }

    fun consumersInfo(groups: Collection<String>) = groups.associateWith { group ->
        val offsets = listConsumerGroupOffsets(group)

        // assume group for single topic
        offsets.keys.firstOrNull() ifNotNull {
            val partitions = listOffsets(offsets.keys, OffsetSpec.latest()).map { (partition, size) ->
                val offset = offsets.getValue(partition)
                PartitionInfo(partition.partition(), offset, size)
            }

            TopicInfo(topic(), partitions)
        }
    }

    private fun createAndConfigureTopic(topic: String, config: Map<String, String>?, partitions: Int, replicationFactor: Short = 1) {
        val newTopic = config
            .ifItNotNull { NewTopic(topic, partitions, replicationFactor).configs(it) }
            .either { NewTopic(topic, partitions, replicationFactor) }
        createTopics(setOf(newTopic))
    }

    fun resetOffsets(group: String, topic: String, config: Map<String, String>, partitions: Int, replicationFactor: Short = 1) {
        deleteTopics(setOf(topic))
        createAndConfigureTopic(topic, config, partitions, replicationFactor)

        describeGroups(setOf(group)).map { (group, groupDesc) ->
            when (val state = groupDesc.get().state().toString()) {
                in "Empty", "Dead" -> {
                    val partitionsToReset = describeTopics(setOf(topic)).flatMap { it.toTopicPartitions() }
                    val preparedOffsets = prepareOffsetsToReset(partitionsToReset, OffsetSpec.earliest())
                    client.alterConsumerGroupOffsets(group, preparedOffsets)
                }

                else -> throw IllegalStateException("Assignments can only be reset if the group '$group' is inactive, but the current state is $state")
            }
        }
    }

    private fun prepareOffsetsToReset(partitions: Collection<TopicPartition>, offsetSpec: OffsetSpec): Map<TopicPartition, OffsetAndMetadata> {
        val logStartOffsets = listOffsets(partitions, offsetSpec)
        return partitions.associateWith {
            val offset = logStartOffsets[it]
            require(offset != null) { "Error getting starting offset of topic partition: $it" }
            OffsetAndMetadata(offset)
        }
    }

    /**
     * Returns true if kafka is available and all topics have been read by consumer, otherwise returns false
     */
    fun kafkaIsAvailable(groups: Collection<String>) = with(consumersInfo(groups)) {
        !any { (_, topicInfo) ->
            topicInfo == null
        } and !any { (_, topicInfo) ->
            topicInfo!!.partitions.any { it.size != it.offset }
        }
    }

    override fun close() = client.close()
}