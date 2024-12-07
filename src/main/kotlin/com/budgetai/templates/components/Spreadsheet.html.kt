package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    script { src = "/static/scripts/spreadsheet.js"; defer = true }

    form(classes = "auth-form") {
        div(classes = "spreadsheet-wrapper") {
            div(classes = "form-group") {
                label { +"Total Income" }
                input(type = InputType.number, classes = "input-field") {
                    placeholder = "Enter your total income"
                    name = "totalIncome"
                    id = "totalIncome"
                    required = true
                }
            }

            // Add row button
            div(classes = "add-row") {
                button(type = ButtonType.button, classes = "spreadsheet-add-row") {
                    attributes["data-action"] = "add-row"
                    +"Add Row"
                }
            }

            // Spreadsheet table
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
        }

        // Submit button at the bottom
        button(type = ButtonType.submit, classes = "submit-button") {
            +"Save Budget"
        }
    }
}