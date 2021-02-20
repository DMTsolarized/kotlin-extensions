package ru.inforion.lab403.common.extensions.tree

import org.junit.Test
import ru.inforion.lab403.common.extensions.tree.DepthFirstIterator.Companion.dfs
import ru.inforion.lab403.common.extensions.tree.Flow.Companion.toFlow
import ru.inforion.lab403.common.extensions.writeJson
import kotlin.test.assertEquals
import kotlin.test.assertTrue

internal class DepthFirstRecursiveTest {

    private val shallowNode: Node<String>
    private val middleNode: Node<String>
    private val deepNode: Node<String>

    private val flowRoot = Node(54).apply {
        create(12).apply {
            create(3)
            create(6).apply {
                create(1)
                create(5)
            }
            create(3)
        }

        create(4).apply {
            create(1)
            create(3).apply {
                create(1)
                create(2)
            }
        }

        create(38).apply {
            create(13)
            create(25).apply {
                create(10)
                create(15)
            }
        }
    }

    private val root = Node("0").apply {
        shallowNode = create("00").apply {
            create("000")
            create("001").apply {
                create("0010")
                create("0011")
            }
            create("002")
        }

        create("01").apply {
            create("010")
            middleNode = create("011").apply {
                create("0110")
                create("0111")
            }
        }

        create("02").apply {
            create("020")
            create("021").apply {
                deepNode = create("0210")
                create("0211")
            }
        }
    }

    private val expectedDepthFirstFull = listOf(
        "0",
        "00",
        "000",
        "001",
        "0010",
        "0011",
        "002",
        "01",
        "010",
        "011",
        "0110",
        "0111",
        "02",
        "020",
        "021",
        "0210",
        "0211"
    )

    @Test
    fun toStringTest() {
        val actual = root.dfs.toString(ident = 2, separator = '-') { it.content }
        val expected = """0
--00
----000
----001
------0010
------0011
----002
--01
----010
----011
------0110
------0111
--02
----020
----021
------0210
------0211
"""
        assertEquals(expected, actual)
    }

    @Test
    fun joinToStringTest() {
        val actual = root.dfs.joinToString { it.content }
        val expected = expectedDepthFirstFull.joinToString()
        assertEquals(expected, actual)
    }

    @Test
    fun shallowDepthTest() = assertEquals(1, shallowNode.depth())

    @Test
    fun middleDepthTest() = assertEquals(2, middleNode.depth())

    @Test
    fun deepDepthTest() = assertEquals(3, deepNode.depth())

    @Test
    fun filterTest() {
        val actual = root.dfs.filter { it.content.length == 4 }
        val expected = listOf("0010", "0011", "0110", "0111", "0210", "0211")
        assertEquals(expected, actual.map { it.content })
        assertTrue { actual.all { it.depth() == 3 } }
    }

    private fun findTest(expected: Node<String>) {
        val content = expected.content
        val actual = root.dfs.find { it.content == content }
        assertEquals(expected, actual)
    }

    @Test
    fun findShallowTest() = findTest(shallowNode)

    @Test
    fun findMiddleTest() = findTest(middleNode)

    @Test
    fun findDeepTest() = findTest(deepNode)

    @Test
    fun forEachTest() {
        val actual = mutableListOf<String>()
        root.dfs.forEach { actual.add(it.content) }
        assertEquals(expectedDepthFirstFull, actual)
    }

    @Test
    fun mapTest() {
        val actual = root.dfs.map { it.content }
        assertEquals(expectedDepthFirstFull, actual)
    }

    @Test
    fun trackTest() {
        val actual = root.dfs.track { it.content[it.depth()] == '0' }.map { it.content }
        val expected = listOf("0", "00", "000")
        assertEquals(expected, actual)
    }

    @Test
    fun findLast() {
        val actual = root.dfs.findLast {
            println("$it -> ${it.content}")
            it.content[it.depth()] == '0'
        }
        println(root.dfs.toString { "$it -> ${it.content}" })
        println(actual!!.content)
    }

    @Test
    fun unlinkAddDepthTest() {
        assertEquals(3, deepNode.depth())
        assertEquals(3, deepNode.depth())
        deepNode.unlink()
        assertEquals(0, deepNode.depth())
        assertEquals(0, deepNode.depth())
        root.add(deepNode)
        assertEquals(1, deepNode.depth())
        assertEquals(1, deepNode.depth())
        deepNode.unlink()
        assertEquals(0, deepNode.depth())
        assertEquals(0, deepNode.depth())
        root.add(deepNode)
        assertEquals(1, deepNode.depth())
        assertEquals(1, deepNode.depth())
    }

    @Test
    fun sankeyTest() {
        val expected = """{
  "sources" : [ 0, 0, 0, 1, 1, 1, 3, 3, 7, 7, 9, 9, 12, 12, 14, 14 ],
  "targets" : [ 1, 7, 12, 2, 3, 6, 4, 5, 8, 9, 10, 11, 13, 14, 15, 16 ],
  "values" : [ 12, 4, 38, 3, 6, 3, 1, 5, 1, 3, 1, 2, 13, 25, 10, 15 ]
}"""
        val actual = flowRoot.toFlow().value { it.content }.toSankey().writeJson()
        assertEquals(expected, actual)
    }

    @Test
    fun notTest_print() {
        val node1 = Node("Q1")
        val node2 = Node("Q2")

        root.dfs.print { "depth=${it.depth()} $it -> ${it.content} parent=${it.parent}" }

        println()

        middleNode.map { it }.forEach { node1.add(it) }
        middleNode.add(node2)
        middleNode.add(node1)

        root.dfs.print { "depth=${it.depth()} $it -> ${it.content} parent=${it.parent}" }
    }
}