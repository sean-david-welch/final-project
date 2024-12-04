package com.budgetai.utils

import io.ktor.server.application.*
import io.ktor.util.*

// template context plugin
val TemplateContextKey = AttributeKey<BaseTemplateContext>("TemplateContext")

val TemplateContext = createRouteScopedPlugin("TemplateContext") {
    onCall { call ->
        val context = call.createTemplateContext()
        call.attributes.put(TemplateContextKey, context)
    }
}

val ApplicationCall.templateContext: BaseTemplateContext get() = attributes[TemplateContextKey]