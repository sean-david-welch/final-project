package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    div(classes = "spreadsheet-wrapper") {
        // Initialize Alpine component with x-data
        div {
            attributes["x-data"] = """{ 
                rows: [${columns.joinToString(", ") { "{}" }}],
                addRow() {
                    this.rows.push(${columns.joinToString(", ") { "{}" }})
                }
            }"""

            h2(classes = "spreadsheet-title") { +"Data Entry" }

            table(classes = "spreadsheet-table") {
                thead {
                    tr {
                        columns.forEach { columnName ->
                            th(classes = "spreadsheet-header") { +columnName }
                        }
                    }
                }
                tbody {
                    // Template for dynamic rows
                    attributes["x-ref"] = "tbody"
                    // Initial row
                    tr {
                        attributes["x-for"] = "(row, index) in rows"
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
                attributes["x-on:click"] = "addRow"
                +"Add Row"
            }
        }
    }
}