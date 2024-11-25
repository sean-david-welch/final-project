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

// New helper functions for forms and buttons
fun DIV.formField(label: String, id: String, block: INPUT.() -> Unit) {
    label(classes = "label-base") {
        htmlFor = id
        +label
    }
    input(classes = "input-base") {
        this.id = id
        block()
    }
}

fun DIV.primaryButton(text: String, block: BUTTON.() -> Unit = {}) {
    button(classes = "button-primary") {
        +text
        block()
    }
}

fun DIV.secondaryButton(text: String, block: BUTTON.() -> Unit = {}) {
    button(classes = "button-secondary") {
        +text
        block()
    }
}