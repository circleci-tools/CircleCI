package com.github.unhappychoice.circleci

import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.jvm.isAccessible

/**
 * Marks a primary constructor property as sensitive so [redactedToString] replaces its value
 * with `[REDACTED]` instead of printing it.
 */
@Target(AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class Redacted

/**
 * Renders [instance] the same way a data class' generated `toString()` would, except that any
 * primary constructor property annotated with [Redacted] is rendered as `[REDACTED]`.
 */
fun redactedToString(instance: Any): String {
    val kClass = instance::class
    val properties = kClass.memberProperties.associateBy { it.name }
    val parameterNames = kClass.primaryConstructor?.parameters?.mapNotNull { it.name } ?: properties.keys

    val fields = parameterNames.joinToString(", ") { name ->
        val property = properties.getValue(name)
        property.isAccessible = true
        val value = if (property.annotations.any { it is Redacted }) "[REDACTED]" else property.call(instance)
        "$name=$value"
    }

    return "${kClass.simpleName}($fields)"
}
