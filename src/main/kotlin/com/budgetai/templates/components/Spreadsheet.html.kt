package com.budgetai.templates.components

import kotlinx.html.*

fun FlowContent.SpreadsheetComponent(id: String = "spreadsheet") {
    script(src = "/static/scripts/spreadsheet.js") {}

    div(classes = "spreadsheet-wrapper") {
        div(classes = "spreadsheet-controls") {
            button(classes = "control-button") {
                attributes["onclick"] = "spreadsheetManager.addRow()"
                +"Add Row"
            }
            button(classes = "control-button") {
                attributes["onclick"] = "spreadsheetManager.addColumn()"
                +"Add Column"
            }
        }
        div {
            this.id = id
            classes = setOf("spreadsheet-container")
        }
    }
}