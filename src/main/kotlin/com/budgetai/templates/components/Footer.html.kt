package com.budgetai.templates.components

import kotlinx.html.*

data class SimpleFooterLink(
    val text: String,
    val href: String
)

fun FlowContent.Footer(
    companyName: String = "Your Company",
    links: List<SimpleFooterLink> = listOf(),
    showThemeToggle: Boolean = false
) {
    footer {
        classes = setOf("border-t", "bg-white", "dark:bg-gray-900")

        div {
            classes = setOf("mx-auto", "max-w-7xl", "px-4", "py-6", "flex", "items-center", "justify-between")

            div {
                classes = setOf("text-sm", "text-gray-600", "dark:text-gray-400")
                +"© ${java.time.Year.now().value} $companyName"
            }

            div {
                classes = setOf("flex", "items-center", "space-x-6")

                links.forEach { link ->
                    a(href = link.href) {
                        classes = setOf("text-sm", "text-gray-600", "hover:text-gray-900", "dark:text-gray-400", "dark:hover:text-white")
                        +link.text
                    }
                }

                if (showThemeToggle) {
                    button {
                        attributes["data-x-on:click"] = "darkMode = !darkMode"
                        classes = setOf("text-gray-600", "hover:text-gray-900", "dark:text-gray-400", "dark:hover:text-white")

                        span {
                            attributes["data-x-show"] = "darkMode"
                            unsafe {
                                +"""
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                                </svg>
                                """
                            }
                        }

                        span {
                            attributes["data-x-show"] = "!darkMode"
                            unsafe {
                                +"""
                                <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                                </svg>
                                """
                            }
                        }
                    }
                }
            }
        }
    }
}