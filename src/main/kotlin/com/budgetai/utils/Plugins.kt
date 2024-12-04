package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.util.*

val TemplateContextKey = AttributeKey<BaseTemplateContext>("TemplateContext")

val TemplateContext = createRouteScopedPlugin("TemplateContext") {
    onCall { call ->
        val context = call.createTemplateContext()
        call.attributes.put(TemplateContextKey, context)
    }
}