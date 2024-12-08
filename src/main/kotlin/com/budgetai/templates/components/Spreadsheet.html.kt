package com.budgetai.templates.components

import com.budgetai.models.CategoryType
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

private val columns = listOf("Name", "Amount", "Category")

fun FlowContent.SpreadsheetComponent(context: BaseTemplateContext) {
    script { src = "/static/scripts/spreadsheet.js"; defer = true }

    div {
        attributes["id"] = "income-response-div"
        attributes["class"] = "response-div"
    }

    form(classes = "auth-form") {
        attributes["hx-post"] = "api/budgets"
        attributes["hx-target"] = "#income-response-div"
        attributes["hx-on::after-request"] = """
            if(event.detail.successful) {
                this.reset();
                window.spreadsheetTable.clear();
            }
        """.trimIndent()

        input(type = InputType.hidden) {
            name = "userId"
            value = context.auth.user?.id!!
        }

        div(classes = "spreadsheet-wrapper") {
            div(classes = "form-group") {
                label { +"Budget Name" }
                input(type = InputType.text, classes = "input-field") {
                    placeholder = "Enter the name for your budget"
                    name = "budgetName"
                    id = "budgetName"
                    required = true
                }
            }

            div(classes = "form-group") {
                label { +"Budget Description" }
                input(type = InputType.text, classes = "input-field") {
                    placeholder = "Description of budget and purpose"
                    name = "description"
                    id = "description"
                    required = true
                }
            }

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
                        td(classes = "spreadsheet-cell") {
                            contentEditable = true
                            +""
                        }
                        td(classes = "spreadsheet-cell") {
                            contentEditable = true
                            +""
                        }
                        td(classes = "spreadsheet-cell") {
                            select(classes = "role-select") {
                                option { value = ""; +"Select category" }
                                CategoryType.entries.forEach { category ->
                                    option {
                                        value = category.toString()
                                        +category.name
                                        if (category == CategoryType.FIXED) {
                                            selected = true
                                        }
                                    }
                                }
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