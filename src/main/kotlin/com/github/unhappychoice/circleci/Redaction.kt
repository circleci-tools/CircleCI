package com.github.unhappychoice.circleci

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
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
 * The per-field metadata [redactedToString] needs, resolved once per [KClass] and reused across
 * every instance of that class.
 */
internal class RedactionField(val name: String, val property: KProperty1<*, *>, val isRedacted: Boolean)

/**
 * Caches the result of [resolveRedactionFields] per [KClass], so the reflective walk over a
 * class' member properties and primary constructor only happens once per class.
 */
internal val redactionMetadataCache = ConcurrentHashMap<KClass<*>, List<RedactionField>>()

private fun resolveRedactionFields(kClass: KClass<*>): List<RedactionField> {
    val properties = kClass.memberProperties.associateBy { it.name }
    val parameterNames = kClass.primaryConstructor?.parameters?.mapNotNull { it.name } ?: properties.keys

    return parameterNames.map { name ->
        val property = properties.getValue(name)
        property.isAccessible = true
        RedactionField(name, property, property.annotations.any { it is Redacted })
    }
}

/**
 * Renders [instance] the same way a data class' generated `toString()` would, except that any
 * primary constructor property annotated with [Redacted] is rendered as `[REDACTED]`.
 */
fun redactedToString(instance: Any): String {
    val kClass = instance::class
    val fields = redactionMetadataCache.getOrPut(kClass) { resolveRedactionFields(kClass) }

    val rendered = fields.joinToString(", ") { field ->
        val value = if (field.isRedacted) "[REDACTED]" else field.property.call(instance)
        "${field.name}=$value"
    }

    return "${kClass.simpleName}($rendered)"
}
