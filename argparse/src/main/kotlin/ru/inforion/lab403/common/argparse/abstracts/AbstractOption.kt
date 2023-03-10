package ru.inforion.lab403.common.argparse.abstracts

import net.sourceforge.argparse4j.inf.Argument
import net.sourceforge.argparse4j.inf.ArgumentParser
import net.sourceforge.argparse4j.inf.Namespace
import ru.inforion.lab403.common.argparse.ApplicationOptions
import ru.inforion.lab403.common.argparse.ValueGetter
import kotlin.reflect.KProperty

abstract class AbstractOption<T>(
    val help: String?,
    val required: Boolean,
    val default: ValueGetter<T>?
) {
    private var nameOrFlags: Array<out String> = emptyArray()

    private var value: T? = null

    fun nameOrFlags(vararg nameOrFlags: String) = run { this.nameOrFlags = nameOrFlags }

    open fun inject(parser: ArgumentParser): Argument = parser
        .addArgument(*nameOrFlags)
        .help(help)
        .required(required)

    fun extract(namespace: Namespace, argument: Argument) = run { value = namespace[argument.dest] }

    @Suppress("UNCHECKED_CAST")
    operator fun getValue(thisRef: ApplicationOptions, property: KProperty<*>) = when {
        !thisRef.internals.initialized -> throw IllegalStateException("${thisRef::class.simpleName} was not initialized!")
        value != null -> value!!
        default != null -> default.invoke()
        property.returnType.isMarkedNullable -> null as T  // it's ok... Kotlin eats this
        else -> throw NullPointerException("Property '${property.name}' in '${thisRef::class.simpleName}' " +
                "marked as non-nullable but has no default value and was not set!")
    }
}