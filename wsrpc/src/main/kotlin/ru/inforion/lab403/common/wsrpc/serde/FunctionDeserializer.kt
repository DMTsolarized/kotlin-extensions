package ru.inforion.lab403.common.wsrpc.serde

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ru.inforion.lab403.common.extensions.dictionaryOf
import ru.inforion.lab403.common.json.deserialize
import ru.inforion.lab403.common.logging.logger
import ru.inforion.lab403.common.scripts.GenericScriptEngine
import ru.inforion.lab403.common.wsrpc.ResourceManager
import ru.inforion.lab403.common.wsrpc.WebSocketRpcServer
import ru.inforion.lab403.common.wsrpc.interfaces.Callable
import java.lang.reflect.Type


class FunctionDeserializer(val resources: ResourceManager) : JsonDeserializer<Callable<*>> {
    companion object {
        val log = logger()
    }

    enum class FunctionType { FUNCTION, LAMBDA }

    internal data class FunctionDescription(
        val engine: String,
        val code: String,
        val type: FunctionType,
        val closure: Map<String, ByteArray>
    )

    private var lambdaIndex = 0

    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): Callable<*> {
        val desc = json.deserialize<FunctionDescription>(context)

        val engine = resources.checkoutScriptEngine(desc.engine)

        val name = when (desc.type) {
            FunctionType.LAMBDA -> {
                desc.closure.forEach { (name, data) -> engine.deserializeAndSet(name, data) }
                engine.evalAndSet("anonymous${lambdaIndex++}", desc.code)
            }
            FunctionType.FUNCTION -> {
                log.severe { "Function closure variables currently not implemented!" }
                engine.evalGetNames(desc.code).first()
            }
        }

        return Callable<Any> {
            engine.invocable.invokeFunction(name, *it)
            resources.checkinScriptEngine(engine)
        }
    }
}