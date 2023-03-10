@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package ru.inforion.lab403.common.swarm.implementations

import ru.inforion.lab403.common.extensions.mutableListOfNulls
import ru.inforion.lab403.common.swarm.abstracts.IRealm
import ru.inforion.lab403.common.swarm.common.Mail
import ru.inforion.lab403.common.swarm.common.Response
import ru.inforion.lab403.common.swarm.tasks.IndexedCommonTask
import java.io.Serializable
import java.util.*

val IRealm.all get() = (0 until total)
val IRealm.others get() = all.filter { it != rank }

inline fun IRealm.recvFromMaster(): Mail = recv(0)
inline fun IRealm.recvFromSlave(src: Int): Mail = recv(src)

inline fun IRealm.recvFromAny(): Mail = recv(-1)

inline fun IRealm.recvFromOthersWhile(predicate: (data: Mail) -> Boolean) {
    do {
        val parcel = recvFromAny()
        val cont = predicate(parcel)
    } while (cont)
}

inline fun IRealm.recvCountFromOthers(count: Int, action: (data: Mail) -> Unit) {
    var remain = count

    recvFromOthersWhile { mail ->
        action(mail)
        --remain != 0
    }
}

inline fun IRealm.recvFromOthers(action: (data: Mail) -> Unit) =
    recvFromOthersWhile { parcel -> action(parcel); false }

inline fun IRealm.sendToMaster(obj: Serializable, blocked: Boolean = true) = send(pack(obj), 0, blocked)
inline fun IRealm.sendToSlave(obj: Serializable, dst: Int, blocked: Boolean = true) = send(pack(obj), dst, blocked)

/**
 * Sends item from iterable or collection or sequence to slave
 *
 * @param index iterable item index
 * @param item iterable data to send
 * @param blocked wait until received or not
 * @param transform make a transformation of [item] before send
 */
inline fun <T> IRealm.sendIterableItem(index: Int, item: T, blocked: Boolean, transform: (Int, T) -> Serializable) {
    // TODO: Add not 0 exclusion but rank
    val slave = index % (total - 1) + 1
    val obj = transform(index, item)
    send(pack(obj), slave, blocked)
}

inline fun <T> IRealm.sendToAllEvenly(iterator: Iterator<T>, blocked: Boolean, transform: (Int, T) -> Serializable): Int {
    var total = 0
    while (iterator.hasNext()) {
        sendIterableItem(total++, iterator.next(), blocked, transform)
    }
    return total
}

inline fun IRealm.sendToOthers(obj: Serializable, blocked: Boolean = true) {
    val data = pack(obj)
    others.forEach { send(data, it, blocked) }
}

inline fun IRealm.sendToAll(obj: Serializable, blocked: Boolean = true) = all.forEach { send(pack(obj), it, blocked) }

internal inline fun <T, C : Collection<T>, R> IRealm.fold(
    size: Int,
    initial: C,
    block: (acc: C, response: Response<R>) -> Unit
): C {
    recvCountFromOthers(size) { mail ->
        val response = mail.objectAs<Response<R>>()
        block(initial, response)
    }

    return initial
}

internal inline fun <T> IRealm.receiveFiltered(
    size: Int,
    tasks: List<IndexedCommonTask<T, *>>,
    action: (index: Int) -> Unit): List<T>
{
    val result = fold(size, mutableListOfNulls<T?>(size)) { acc, response: Response<Boolean> ->
        if (response.value) {
            action(response.index)
            acc[response.index] = tasks[response.index].value
        }
    }
    val notNulls = ArrayList<T>(size / 2)
    result.forEach { if (it != null) notNulls.add(it) }
    return notNulls
}

internal inline fun <T> IRealm.receiveOrderedAll(size: Int, offset: Int, action: (index: Int) -> Unit) =
    fold(size, mutableListOfNulls<T?>(size)) { acc, response: Response<T> ->
        action(response.index)
        acc[response.index + offset] = response.value
    } as List<T>


internal inline fun <T> IRealm.receiveOrderedWhile(predicate: (received: Int, index: Int) -> Boolean): List<T> {
    var received = 0
    val cache = LinkedList<Response<T>>()

    recvFromOthersWhile { mail ->
        val response = mail.objectAs<Response<T>>()
        cache.add(response)
        predicate(++received, response.index)
    }

    return mutableListOfNulls<T>(received).apply {
        cache.forEach { this[it.index] = it.value }
    } as List<T>
}
