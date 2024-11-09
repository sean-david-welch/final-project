package com.budgetai.templates.components

import kotlinx.html.*
import io.ktor.server.html.*
import kotlinx.css.*

class NavbarComponent : Template<HTML> {
    override fun HTML.apply() {
        div {
            attributes["class"] = "bg-white shadow"
            nav {
                attributes["class"] = "container mx-auto px-4"
                div {
                    attributes["class"] = "flex justify-between items-center h-16"

                    // Logo
                    a("/") {
                        attributes["class"] = "text-xl font-bold"
                        +"BudgetAI"
                    }

                    // Navigation items
                    div {
                        attributes["class"] = "flex space-x-4"
                        attributes["x-data"] = "{open: false}"

                        navLink("/dashboard", "Dashboard")
                        navLink("/reports", "Reports")
                        navLink("/settings", "Settings")
                    }
                }
            }
        }
    }

    private fun FlowContent.navLink(href: String, text: String) {
        a(href) {
            attributes["class"] = "text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
            +text
        }
    }
}