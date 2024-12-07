package com.budgetai.templates.components

import kotlinx.html.*

fun FlowContent.SpreadsheetComponent() {
    div(classes = "spreadsheet-wrapper") {
        div {
            h2(classes = "spreadsheet-title") { +"Data Entry" }
        }

        table(classes = "spreadsheet-container") {
            thead {
                tr {
                    th(classes = "spreadsheet-header") {
                        +"Name"
                    }
                    th(classes = "spreadsheet-header") {
                        +"Amount"
                    }
                    th(classes = "spreadsheet-header") {
                        +"Category"
                    }
                }
            }
            tbody {
                tr {
                    td(classes = "spreadsheet-cell") {
                        contentEditable = true
                        +""
                    }
                    td(classes = "spreadsheet-cell") {
                        contentEditable = true
                        +""
                    }
                    td(classes = "spreadsheet-cell") {
                        contentEditable = true
                        +""
                    }
                }
            }
        }

        button(classes = "spreadsheet-add-row") {
            onClick = "new EditableTable('spreadsheet-container')"
            +"Add Row"
        }
    }
}