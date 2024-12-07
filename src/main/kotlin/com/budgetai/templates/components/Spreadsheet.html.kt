package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    script { src = "/static/scripts/spreadsheet.js"; defer = true }
    div(classes = "spreadsheet-wrapper") {
        div {
            h2(classes = "spreadsheet-title") { +"Data Entry" }
        }

        table(classes = "spreadsheet-table") {
            thead {
                tr {
                    columns.forEach { columnName ->
                        th(classes = "spreadsheet-header") { +columnName }
                    }
                }
            }
            tbody {
                tr {
                    columns.forEach { _ ->
                        td(classes = "spreadsheet-cell") {
                            contentEditable = true
                            +""
                        }
                    }
                }
            }
        }

        button(classes = "spreadsheet-add-row") {
            attributes["data-action"] = "add-row"
            +"Add Row"
        }
    }
}