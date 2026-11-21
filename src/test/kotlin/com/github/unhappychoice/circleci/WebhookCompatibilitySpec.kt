package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.request.CreateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.UpdateWebhookRequest
import com.github.unhappychoice.circleci.v2.request.WebhookScopeRequest
import com.github.unhappychoice.circleci.v2.response.Webhook
import com.github.unhappychoice.circleci.v2.response.WebhookScope
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object WebhookCompatibilitySpec : Spek({
    test("retains the original create webhook JVM constructors and copy methods") {
        val scope = WebhookScopeRequest("project", "project-id")
        val events = listOf("workflow-completed")
        val request = CreateWebhookRequest("name", "url", "secret", scope, events, verifyTls = false)
        val parameterTypes = arrayOf(
            String::class.java, String::class.java, String::class.java, WebhookScopeRequest::class.java,
            List::class.java, Boolean::class.javaPrimitiveType!!
        )
        val arguments = arrayOf("name", "url", "secret", scope, events, false)

        assertLegacyMethods(request, request.copy(isEnabled = false), parameterTypes, arguments)
        assertLegacyDefaultConstructor(request.copy(verifyTls = true), parameterTypes, arguments, 0b100000)
    }

    test("retains the original update webhook JVM constructors and copy methods") {
        val request = UpdateWebhookRequest("name", "url", "secret", emptyList(), false)
        val parameterTypes = arrayOf(
            String::class.java, String::class.java, String::class.java, List::class.java, Boolean::class.javaObjectType
        )
        val arguments = arrayOf("name", "url", "secret", emptyList<String>(), false)

        assertLegacyMethods(request, request.copy(isEnabled = false), parameterTypes, arguments)
        assertLegacyDefaultConstructor(UpdateWebhookRequest(), parameterTypes, arguments, 0b11111)
        assertEquals(UpdateWebhookRequest(), request.javaClass.getConstructor().newInstance())
    }

    test("retains the original webhook response JVM constructor and copy methods") {
        val scope = WebhookScope("project", "project-id")
        val events = listOf("workflow-completed")
        val webhook = Webhook("id", "name", "url", "created", "updated", "secret", scope, events)
        val parameterTypes = arrayOf(
            String::class.java, String::class.java, String::class.java, String::class.java, String::class.java,
            String::class.java, WebhookScope::class.java, List::class.java
        )
        val arguments = arrayOf("id", "name", "url", "created", "updated", "secret", scope, events)

        assertLegacyMethods(
            webhook, webhook.copy(isEnabled = false, verifyTls = true, autoDisabledAt = "disabled"),
            parameterTypes, arguments
        )
    }
})

private fun assertLegacyMethods(
    original: Any,
    withSettings: Any,
    parameterTypes: Array<out Class<*>>,
    arguments: Array<out Any>
) {
    val type = original.javaClass
    val copy = type.getMethod("copy", *parameterTypes)
    val copyWithDefaults = type.getMethod(
        "copy\$default", type, *parameterTypes, Int::class.javaPrimitiveType, Any::class.java
    )
    assertEquals(original, type.getConstructor(*parameterTypes).newInstance(*arguments))
    assertEquals(withSettings, copy.invoke(withSettings, *arguments))
    assertEquals(
        withSettings,
        copyWithDefaults.invoke(null, withSettings, "ignored", *arguments.drop(1).toTypedArray(), 0b1, null)
    )
}

private fun assertLegacyDefaultConstructor(
    expected: Any,
    parameterTypes: Array<out Class<*>>,
    arguments: Array<out Any>,
    mask: Int
) {
    val constructor = expected.javaClass.getConstructor(
        *parameterTypes, Int::class.javaPrimitiveType, Class.forName("kotlin.jvm.internal.DefaultConstructorMarker")
    )
    assertEquals(expected, constructor.newInstance(*arguments, mask, null))
}
