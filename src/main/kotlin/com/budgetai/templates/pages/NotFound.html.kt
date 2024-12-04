package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.*

fun NotFoundTemplate(contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container flex items-center justify-center min-h-[80vh]") {
            div(classes = "content-wrapper text-center max-w-lg mx-auto") {
                contentFn()
            }
        }
    }
}

fun create404Page() = NotFoundTemplate {
    div(classes = "error-content") {
        div(classes = "error-header") {
            h1(classes = "error-code") { +"404" }
            p(classes = "error-title") { +"Page Not Found" }
        }

        p(classes = "error-message") {
            +"The page you're looking for doesn't exist or has been moved."
        }

        a(href = "/", classes = "error-button") {
            +"Return Home"
        }
    }
}