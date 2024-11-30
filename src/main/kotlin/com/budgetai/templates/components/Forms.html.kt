package com.budgetai.templates.components

import kotlinx.html.*

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