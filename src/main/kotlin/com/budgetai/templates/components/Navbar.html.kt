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

fun FlowContent.Navbar(brandName: String = "Your Brand", navItems: List<NavItem> = listOf(), showMobileMenu: String = "false") {
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
                div {
                    classes = setOf("navbar-mobile-toggle")
                    button {
                        attributes["type"] = "button"
                        attributes["data-x-on:click"] = "mobileMenuOpen = !mobileMenuOpen"
                        classes = setOf("mobile-menu-button")
                        span {
                            classes = setOf("sr-now")
                            +"Toggle menu"
                        }
                        div {
                            classes = setOf("mobile-menu-icon")
                            unsafe {
                                +"""
                                <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16"/>
                                </svg>
                                """.trimIndent()
                            }
                        }
                    }
                }
            }
        }
        div {
            classes = setOf("mobile-menu-container")
            attributes["data-x-show"] = "mobileMenuOpen"
            attributes["style"] = "display: none;"
            div {
                classes = setOf("mobile-menu-content")
                ul {
                    renderNavItems(navItems)
                }
            }
        }
    }
}