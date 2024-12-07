package com.budgetai.templates.components

import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent() {
    form {
        script { src = "/static/scripts/spreadsheet.js"; defer = true }

        div(classes = "add-row") {
            button(classes = "spreadsheet-add-row") {
                attributes["data-action"] = "add-row"
                +"Add Row"
            }
        }
        div(classes = "spreadsheet-wrapper") {
            form(classes = "auth-form") {
                formField("Total Income", InputType.number, "Enter your total income") {
                    name = "totalIncome"
                    id = "totalIncome"
                    required = true
                }
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
        submitButton("Save Budget")
    }
}