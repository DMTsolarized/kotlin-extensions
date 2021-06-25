package ru.inforion.lab403.common.extensions.concurrent.events

import java.util.concurrent.TimeUnit

interface Event {
    fun await()

    fun await(time: Long, unit: TimeUnit): Boolean

    fun signal()
}