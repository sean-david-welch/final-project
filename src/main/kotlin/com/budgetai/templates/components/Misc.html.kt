package com.budgetai.templates.components

import kotlinx.html.*

fun BODY.pageHeader(title: String, block: (DIV.() -> Unit)? = null) {
    div(classes = "mb-6") {
        h1(classes = "heading-large") { +title }
        block?.invoke(this)
    }
}

fun DIV.contentSection(block: DIV.() -> Unit) {
    div(classes = "content-section") {
        block()
    }
}

fun DIV.statGrid(block: DIV.() -> Unit) {
    div(classes = "grid-stats") {
        block()
    }
}

fun DIV.statCard(label: String, value: String) {
    div(classes = "card-base") {
        div(classes = "text-base") { +label }
        div(classes = "mt-2 heading-medium") { +value }
    }
}

fun DIV.activityList(block: DIV.() -> Unit) {
    div(classes = "list-container") {
        block()
    }
}

fun DIV.activityItem(text: String, time: String) {
    div(classes = "list-item") {
        div(classes = "text-base") { +text }
        div(classes = "text-muted mt-1") { +time }
    }
}

