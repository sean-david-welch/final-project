package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    script { src = "/static/scripts/spreadsheet.js"; defer = true }

    // Add form section before spreadsheet
    div(classes = "income-form-wrapper") {
        form {
            attributes["onSubmit"] = "handleFormSubmit(event)"

            div(classes = "form-group") {
                label {
                    htmlFor = "totalIncome"
                    +"Total Income:"
                }
                input(type = InputType.number) {
                    id = "totalIncome"
                    name = "totalIncome"
                    classes = setOf("income-input")
                    required = true
                    placeholder = "Enter your total income"
                }
            }


        }
    }

    div(classes = "add-row") {
        button(classes = "spreadsheet-add-row") {
            attributes["data-action"] = "add-row"
            +"Add Row"
        }
    }
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
    }
    button(type = ButtonType.submit, classes = "submit-button") {
        +"Submit Budget"
    }
}