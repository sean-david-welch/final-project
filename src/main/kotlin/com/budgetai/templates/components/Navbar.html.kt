package com.budgetai.templates.components

import kotlinx.html.*

data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

private fun UL.renderNavItems(items: List<NavItem>) {
    items.forEach { item ->
        li {
            a(href = item.href) {
                classes = if (item.isActive) {
                    setOf("nav-item-active")
                } else {
                    setOf("nav-item-inactive")
                }
                +item.text
            }
        }
    }
}

fun FlowContent.Navbar(brandName: String = "Your Brand", navItems: List<NavItem> = listOf()) {
    nav {
        classes = setOf("navbar")
        div {
            classes = setOf("navbar-container")
            div {
                classes = setOf("navbar-content")
                div {
                    classes = setOf("navbar-brand-container")
                    div {
                        classes = setOf("navbar-brand-logo")
                        span {
                            classes = setOf("navbar-brand-text")
                            +brandName
                        }
                    }
                    div {
                        classes = setOf("navbar-desktop-menu")
                        div {
                            classes = setOf("navbar-menu-container")
                            ul {
                                classes = setOf("navbar-menu-list")
                                renderNavItems(navItems)
                            }
                        }
                    }
                }
            }
        }
    }
}