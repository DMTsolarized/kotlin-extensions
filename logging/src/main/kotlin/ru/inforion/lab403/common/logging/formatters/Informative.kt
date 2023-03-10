@file:Suppress("NOTHING_TO_INLINE")

package ru.inforion.lab403.common.logging.formatters

import ru.inforion.lab403.common.extensions.stretch
import ru.inforion.lab403.common.logging.Caller
import ru.inforion.lab403.common.logging.abbreviation
import ru.inforion.lab403.common.logging.logger.Record
import java.text.SimpleDateFormat
import java.util.*

class Informative(val painter: Formatter = ColorMultiline):
    Formatter {

    companion object {
        var locationLength = 50
        var dateFormat = "HH:mm:ss"
        var messageFormat = "%(time) %(level) %(location): %(message)\n"
    }

    private inline fun stretch(string: String, maxlen: Int) = if (string.length <= maxlen)
        string.stretch(maxlen, false)
    else {
        val stretched = string.stretch(maxlen - 3, false)
        "...$stretched"
    }

    private inline fun formatLocation(caller: Caller): String {
        // TODO: Wait while JB fix wrong regex pattern for stack trace element in console
        //   see parseStackTraceLine in KotlinExceptionFilter.kt at Kotlin repo
        val location = caller.toString()
        return if (locationLength != -1) stretch(location, locationLength) else location
    }

    private inline fun formatDate(millis: Long) = SimpleDateFormat(dateFormat).format(Date(millis))

    override fun format(message: String, record: Record): String {
        val location = formatLocation(record.caller)
        val level = record.level.abbreviation
        val time = formatDate(record.millis)
        // TODO: Make more efficient way
        val formatted = messageFormat
            .replace("%(time)", time)
            .replace("%(level)", level)
            .replace("%(location)", location)
            .replace("%(message)", message)
        return painter.format(formatted, record)
    }
}