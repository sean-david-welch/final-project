package com.budgetai.templates.pages

import kotlinx.html.*
import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.templates.components.Navbar
import com.budgetai.templates.components.Footer
import com.budgetai.templates.components.NavItem
import com.budgetai.templates.components.SimpleFooterLink

fun DashboardTemplate(
    title: String,
    contentFn: DIV.() -> Unit
): String {
    return BaseTemplate {
        attributes["x-data"] = "{darkMode: false, mobileMenuOpen: false}"
        attributes["x-bind"] = "{'class': darkMode ? 'dark' : ''}"

        // Navbar
        Navbar(
            brandName = "BudgetAI",
            navItems = listOf(
                NavItem("Dashboard", "/", true),
                NavItem("Reports", "/reports"),
                NavItem("Settings", "/settings")
            )
        )

        // Main content area
        main {
            classes = setOf(
                "min-h-screen",
                "bg-gray-50",
                "dark:bg-gray-800",
                "py-6"
            )

            div {
                classes = setOf("mx-auto", "max-w-7xl", "px-4", "sm:px-6", "lg:px-8")

                // Page header
                div {
                    classes = setOf("mb-6")
                    h1 {
                        classes = setOf(
                            "text-2xl",
                            "font-semibold",
                            "text-gray-900",
                            "dark:text-white"
                        )
                        +title
                    }
                }

                // Main content container
                div {
                    classes = setOf(
                        "bg-white",
                        "dark:bg-gray-900",
                        "rounded-lg",
                        "shadow",
                        "p-6"
                    )
                    contentFn()
                }
            }
        }

        // Footer
        Footer(
            companyName = "BudgetAI",
            links = listOf(
                SimpleFooterLink("Privacy", "/privacy"),
                SimpleFooterLink("Terms", "/terms")
            ),
            showThemeToggle = true
        )
    }
}

// Example usage - create a dashboard page
fun createDashboardPage(): String {
    return DashboardTemplate("Dashboard Overview") {
        // Simple dashboard grid
        div {
            classes = setOf("grid", "gap-6", "md:grid-cols-2", "lg:grid-cols-3")

            // Stats cards
            repeat(3) { index ->
                div {
                    classes = setOf(
                        "bg-gray-50",
                        "dark:bg-gray-800",
                        "rounded-lg",
                        "p-6",
                        "shadow-sm"
                    )

                    // Stat label
                    div {
                        classes = setOf(
                            "text-sm",
                            "font-medium",
                            "text-gray-600",
                            "dark:text-gray-400"
                        )
                        +when(index) {
                            0 -> "Total Revenue"
                            1 -> "Active Users"
                            else -> "Growth Rate"
                        }
                    }

                    // Stat value
                    div {
                        classes = setOf(
                            "mt-2",
                            "text-3xl",
                            "font-semibold",
                            "text-gray-900",
                            "dark:text-white"
                        )
                        +when(index) {
                            0 -> "$24,500"
                            1 -> "2,345"
                            else -> "+12.3%"
                        }
                    }
                }
            }
        }

        // Recent activity section
        div {
            classes = setOf("mt-6")

            h2 {
                classes = setOf(
                    "text-lg",
                    "font-medium",
                    "text-gray-900",
                    "dark:text-white",
                    "mb-4"
                )
                +"Recent Activity"
            }

            div {
                classes = setOf(
                    "border",
                    "dark:border-gray-700",
                    "rounded-lg",
                    "divide-y",
                    "dark:divide-gray-700"
                )

                // Activity items
                repeat(3) {
                    div {
                        classes = setOf("p-4")
                        div {
                            classes = setOf(
                                "text-sm",
                                "text-gray-900",
                                "dark:text-white"
                            )
                            +"User completed action ${it + 1}"
                        }
                        div {
                            classes = setOf(
                                "text-sm",
                                "text-gray-500",
                                "dark:text-gray-400",
                                "mt-1"
                            )
                            +"2 minutes ago"
                        }
                    }
                }
            }
        }
    }
}