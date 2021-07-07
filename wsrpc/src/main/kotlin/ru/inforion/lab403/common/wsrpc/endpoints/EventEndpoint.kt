package ru.inforion.lab403.common.wsrpc.endpoints

import ru.inforion.lab403.common.concurrent.events.Event
import ru.inforion.lab403.common.wsrpc.annotations.WebSocketRpcMethod
import ru.inforion.lab403.common.wsrpc.interfaces.WebSocketRpcEndpoint
import java.util.concurrent.TimeUnit

class EventEndpoint(
    private val event: Event,
    override val name: String = "Event"
) : WebSocketRpcEndpoint {

    @WebSocketRpcMethod
    fun signal() = event.signal()

    @WebSocketRpcMethod
    fun awaitForever() = event.await()

    @WebSocketRpcMethod
    fun awaitFor(millis: Long) = event.await(millis, TimeUnit.MILLISECONDS)

    // signal event when destroy endpoint to unblock possible waiting thread
    override fun destroy() = event.signal()
}