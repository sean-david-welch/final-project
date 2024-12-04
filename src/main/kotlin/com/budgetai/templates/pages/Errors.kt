package com.budgetai.templates.pages

import com.budgetai.templates.layout.BaseTemplate
import kotlinx.html.*

// Common error template for both 403 and 404
fun ErrorTemplate(contentFn: DIV.() -> Unit) = BaseTemplate {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container flex items-center justify-center min-h-[80vh]") {
            div(classes = "content-wrapper text-center max-w-lg mx-auto") {
                contentFn()
            }
        }
    }
}

fun create403Page() = ErrorTemplate {
    div(classes = "error-content") {
        div(classes = "error-header") {
            h1(classes = "error-code") { +"403" }
            p(classes = "error-title") { +"Access Forbidden" }
        }

        p(classes = "error-message") {
            +"You don't have permission to access this page. Please log in or contact support if you believe this is a mistake."
        }

        div(classes = "error-buttons") {
            a(href = "/auth", classes = "error-button mr-4") {
                +"Log In"
            }
            a(href = "/", classes = "error-button") {
                +"Return Home"
            }
        }
    }
}

fun create404Page() = ErrorTemplate {
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