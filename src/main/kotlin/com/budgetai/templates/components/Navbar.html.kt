package com.budgetai.templates.components

import kotlinx.html.*

data class NavItem(val text: String, val href: String, val isActive: Boolean = false)

private fun UL.renderNavItems(items: List<NavItem>) {
    items.forEach { item ->
        li {
            a(href = item.href) {
                classes = if (item.isActive) {
                    setOf("px-3", "py-2", "text-sm", "font-medium", "text-white", "bg-gray-900", "rounded-md")
                } else {
                    setOf("px-3", "py-2", "text-sm", "font-medium", "text-gray-300", "hover:text-white", "hover:bg-gray-700", "rounded-md")
                }
                +item.text
            }
        }
    }
}

fun FlowContent.Navbar(brandName: String = "Your Brand", navItems: List<NavItem> = listOf(), showMobileMenu: String = "false") {
    nav {
        classes = setOf("bg-gray-800")
        div {
            classes = setOf("mx-auto", "max-w-7xl", "px-4", "sm:px-6", "lg:px-8")
            div {
                classes = setOf("flex", "h-16", "items-center", "justify-between")
                div {
                    classes = setOf("flex", "items-center")
                    div {
                        classes = setOf("flex-shrink-0")
                        span {
                            classes = setOf("text-white", "text-xl", "font-bold")
                            +brandName
                        }
                    }
                    div {
                        classes = setOf("hidden", "md:block", "ml-10")
                        div {
                            classes = setOf("flex", "items-baseline", "space-x-4")
                            ul {
                                classes = setOf("flex", "space-x-4")
                                renderNavItems(navItems)
                            }
                        }
                    }
                }
                div {
                    classes = setOf("md:hidden")
                    button {
                        attributes["type"] = "button"
                        attributes["data-x-on:click"] = "mobileMenuOpen = !mobileMenuOpen"
                        classes = setOf("inline-flex", "items-center", "justify-center", "rounded-md", "p-2", "text-gray-400", "hover:bg-gray-700", "hover:text-white")
                        span {
                            classes = setOf("sr-only")
                            +"Toggle menu"
                        }
                        div {
                            classes = setOf("h-6", "w-6")
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
            classes = setOf("md:hidden")
            attributes["data-x-show"] = "mobileMenuOpen"
            attributes["style"] = "display: none;"
            div {
                classes = setOf("space-y-1", "px-2", "pb-3", "pt-2")
                ul {
                    renderNavItems(navItems)
                }
            }
        }
    }
}
