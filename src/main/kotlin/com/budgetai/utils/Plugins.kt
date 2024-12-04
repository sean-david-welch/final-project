package com.budgetai.utils

import com.budgetai.utils.BaseTemplateContext
import com.budgetai.utils.createTemplateContext
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.util.*

val TemplateContextKey = AttributeKey<BaseTemplateContext>("TemplateContext")

val TemplateContext = createRouteScopedPlugin("TemplateContext") {
    onCall { call ->
        val context = call.createTemplateContext()
        call.attributes.put(TemplateContextKey, context)
    }
}