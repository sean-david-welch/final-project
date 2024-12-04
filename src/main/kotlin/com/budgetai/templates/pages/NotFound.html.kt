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
    div(classes = "space-y-6") {
        div {
            h1(classes = "text-6xl font-bold text-gray-900 dark:text-white") { +"404" }
            p(classes = "mt-2 text-xl text-gray-600 dark:text-gray-400") { +"Page Not Found" }
        }

        p(classes = "text-gray-500 dark:text-gray-300") {
            +"The page you're looking for doesn't exist or has been moved."
        }

        a(href = "/", classes = "inline-block mt-4 px-4 py-2 bg-gray-50 dark:bg-gray-800 text-gray-900 dark:text-white rounded-lg hover:bg-gray-100 dark:hover:bg-gray-700 transition-colors") {
            +"Return Home"
        }
    }
}