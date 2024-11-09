package com.budgetai.templates.components

import kotlinx.html.*
import io.ktor.server.html.*

class ScriptsComponent : Template<HTML> {
    override fun HTML.apply() {
        // Common scripts
        script(src = "/static/js/app.js") {}
    }
}