package com.budgetai.templates.layout

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun BaseTemplate(bodyFn: BODY.() -> Unit): String {
    return "<!DOCTYPE html>" + createHTML().html {
        lang = "en"
        head {
            meta { charset = "UTF-8" }
            meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
            script { src = "https://unpkg.com/alpinejs@3.13.3/dist/cdn.min.js"; defer = true }
            script { src = "https://unpkg.com/htmx.org@1.9.10"; defer = true }
            link { href = "/static/styles/output.css"; rel = "stylesheet" }
            style { unsafe { +""" @layer base { html { font-family: system-ui, sans-serif; } } """ } }
        }
        body {
            bodyFn()
        }
    }
}