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
                        contentEditable = true
                        +"Header 1"
                    }
                    th(classes = "spreadsheet-header") {
                        contentEditable = true
                        +"Header 2"
                    }
                    th(classes = "spreadsheet-header") {
                        contentEditable = true
                        +"Header 3"
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