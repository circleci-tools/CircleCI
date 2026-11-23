package com.github.unhappychoice.circleci

import com.github.unhappychoice.circleci.v2.response.Group
import com.github.unhappychoice.circleci.v2.response.GroupListResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.spekframework.spek2.Spek

object GroupListResponseSpec : Spek({
    val response = GroupListResponse(listOf(Group("id", "Editors")), "next", totalCount = 27)
    val replacement = emptyList<Group>()

    test("the legacy JVM copy signature preserves the total count") {
        val copy = GroupListResponse::class.java.getMethod("copy", List::class.java, String::class.java)
        assertEquals(GroupListResponse(replacement, null, 27), copy.invoke(response, replacement, null))
    }

    test("the legacy JVM copy default signature honors both default arguments") {
        val type = GroupListResponse::class.java
        val copyDefault = type.getMethod(
            "copy\$default", type, List::class.java, String::class.java, Int::class.javaPrimitiveType, Any::class.java
        )
        assertEquals(GroupListResponse(response.items, null, 27), copyDefault.invoke(null, response, null, null, 1, null))
        assertEquals(GroupListResponse(replacement, "next", 27), copyDefault.invoke(null, response, replacement, null, 2, null))
    }
})
