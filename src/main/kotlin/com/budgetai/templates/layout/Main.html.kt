package com.budgetai.templates.layout

import kotlinx.html.*
import kotlinx.html.stream.createHTML

fun BaseTemplate(bodyFn: BODY.() -> Unit) = "<!DOCTYPE html>" + createHTML().html {
   lang = "en"
   head {
       meta { charset = "UTF-8" }
       meta { name = "viewport"; content = "width=device-width, initial-scale=1.0" }
       script { src = "/webjars/alpinejs/3.14.3/dist/cdn.js"; defer = true }
       script { src = "/webjars/htmx.org/2.0.3/dist/htmx.min.js"; defer = true }
       link { href = "/static/styles/output.css"; rel = "stylesheet" }
       link {
           href = "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap"
           rel = "stylesheet"
       }
   }
   body(classes = "font-inter flex align-center") { bodyFn() }
}