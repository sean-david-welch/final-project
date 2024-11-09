package com.budgetai.templates.layout

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun BaseTemplate(bodyFn: BODY.() -> Unit): String {
    return "<!DOCTYPE html>" + createHTML().html {
        lang = "en"
        head {
            script { src = "https://unpkg.com/htmx.org@1.9.10"; defer = true }
            script { src = "https://unpkg.com/alpinejs@3.13.3/dist/cdn.min.js"; defer = true }
            link { href = "https://cdn.tailwindcss.com"; rel="stylesheet" }
            link { href = "/index.css"; rel="stylesheet" }
        }
        body {
            bodyFn()
        }
    }
}