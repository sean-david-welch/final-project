package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    script { src = "/static/scripts/spreadsheet.js"; defer = true }

    div {
        attributes["id"] = "income-response-div"
        attributes["class"] = "response-div"
    }

    form(classes = "auth-form") {
        attributes["hx-post"] = "/budget/save"
        attributes["hx-target"] = "#income-response-div"

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

            // Hidden input for spreadsheet data
            input(type = InputType.hidden) {
                name = "spreadsheetData"
                id = "spreadsheetData"
                value = ""
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

        button(type = ButtonType.submit, classes = "submit-button") {
            +"Save Budget"
        }
    }
}