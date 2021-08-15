package ru.inforion.lab403.common.json.serializers

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import ru.inforion.lab403.common.json.deserialize
import java.lang.reflect.Type

object MapDeserializer : JsonDeserializer<Map<*, *>> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext) =
        json.asJsonObject.deserialize(context)
}