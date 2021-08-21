package ru.inforion.lab403.common.json

import com.google.gson.*
import org.junit.Test
import ru.inforion.lab403.common.json.sysinfo.FullSystemInfo
import java.lang.reflect.Type
import java.util.*
import kotlin.test.assertEquals

internal class GsonJsonTest {

    data class Testik(val data: UInt)

    @Test
    fun unsignedTest() {
        val expected = Testik(0xFFFF_FFFFu)
        val json = expected.toJson()

        println(json)

        val actual = json.fromJson<Testik>()

        println(actual)
        assertEquals(expected, actual)
    }

    @Test
    fun unsignedOverflowTest() {
//        val actual = """{"data":${UInt.MAX_VALUE}}""".fromJson<Testik>()
//        assertEquals(Testik(UInt.MAX_VALUE), actual)
    }

    @Test
    fun anySerializationTest() {
        fun getAny(): Any = Testik(100u)

        val obj1 = getAny()
        val json = obj1.toJson()
        println(json)
        val obj2 = json.fromJson<Testik>()

        assertEquals(obj2, obj1)
    }

    class ObjectSerializer : JsonSerializer<Iterator<*>> {
        data class ObjectDescription(val __rpc__: String, val endpoint: UUID)

        override fun serialize(src: Iterator<*>, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
//            val holder = server.register(apiGen(src))
            return context.serialize(ObjectDescription("111", UUID.randomUUID()))
        }
    }

    abstract class AbstractAnimal(val name: String, val type: String)

    data class Cat(val cat: String) : AbstractAnimal("pushok", "Cat")

    data class Dog(val dog: String) : AbstractAnimal("bobik", "Dog")

    object CatInstanceCreator : InstanceCreator<Cat> {
        override fun createInstance(type: Type): Cat {
            return Cat("")
        }
    }

    @Test
    fun polymorphicDeserializationTest() {
        val gson = defaultJsonBuilder()
//            .registerTypeAdapter(Cat::class.java, CatInstanceCreator)
            .registerPolymorphicAdapter(listOf(Cat::class.java, Dog::class.java))
            .create()

        val string = """ { "cat": "meow", "type": "Cat" } """

//        val string = expected.toJson(gson)

        println(string)

        val cat = string.fromJson<AbstractAnimal>(gson) as Cat

        println(cat.type)
        println(cat.name)
        println(cat.cat)
    }

    @Test
    fun sequenceCustomSerializerTest() {
        val gson = defaultJsonBuilder()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(Iterator::class.java, ObjectSerializer())
            .create()

        var sequence = List(100) { it }.asIterable()

        sequence = sequence.filter {
            println("1. it = $it")
            it < 100
        }

        sequence = sequence.map {
            println("2. it = $it")
            it + it
        }

        val json = sequence.toJson(gson)
        println(json)
        println(sequence.joinToString())
    }

    @Test
    fun listOfObjectsDeserializeTest() {
        val gson = defaultJsonBuilder().create()
        val json = "[1, 2, 3, 4]"
        val actual = json.fromJson<List<Any>>(gson)
        assertEquals(listOf(1, 2, 3, 4), actual)
    }

    @Test
    fun deserializeJsonElements() {
        val json = """
           {
              "obj": {
                "name": "cat",
                "action": "meow",
                "value": 10
               }
           }
        """

        val result = json.fromJson<Map<String, JsonElement>>()


        println(result)
    }

    @Test
    fun mapOfLiteralsDeserializeTest() {
        val gson = defaultJsonBuilder().create()
        val json = "{keyField = valueField}"
        val actual = json.fromJson<Map<String, String>>(gson)
        assertEquals(mapOf("keyField" to "valueField"), actual)
    }

    @Test
    fun mapOfJsonElementsDeserializeTest() {
        val gson = defaultJsonBuilder().create()
        val json = "{keyField = valueField}"
        val actual = json.fromJson<Map<String, JsonElement>>(gson)
    }

    @Test
    fun deserializeJsonSerializedStringShouldValid() {
        val fingerprint = FullSystemInfo()

        val json = fingerprint.toJson()
        val actual = json.fromJson<FullSystemInfo>()

        assertEquals(fingerprint.toString(), actual.toString())
    }
}