package com.budgetai.templates.components

import kotlinx.html.*

fun FlowContent.SpreadsheetComponent(id: String = "spreadsheet") {
    link { rel = "stylesheet"; href = "https://cdn.jsdelivr.net/npm/handsontable@latest/dist/handsontable.full.min.css" }
    script { src = "https://cdn.jsdelivr.net/npm/handsontable@latest/dist/handsontable.full.min.js"; defer = true }
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