package com.budgetai.templates.components

import com.budgetai.templates.layout.BaseTemplate
import com.budgetai.utils.BaseTemplateContext
import kotlinx.html.*

fun SpreadsheetTemplate(title: String, context: BaseTemplateContext, contentFn: DIV.() -> Unit) = BaseTemplate(context) {
    main(classes = "dashboard-layout") {
        div(classes = "dashboard-container") {
            h1(classes = "page-title") { +title }
            div(classes = "content-wrapper") {
                div(classes = "spreadsheet-controls") {
                    button(classes = "control-button") {
                        attributes["onclick"] = "addRow()"
                        +"Add Row"
                    }
                    button(classes = "control-button") {
                        attributes["onclick"] = "addColumn()"
                        +"Add Column"
                    }
                }
                div {
                    id = "spreadsheet"
                    classes = setOf("spreadsheet-container")
                }
                script {
                    unsafe {
                        +"""
                        let hot;
                        document.addEventListener('DOMContentLoaded', function() {
                            const container = document.getElementById('spreadsheet');
                            hot = new Handsontable(container, {
                                data: [
                                    ['', '', '', '', ''],
                                    ['', '', '', '', ''],
                                    ['', '', '', '', '']
                                ],
                                rowHeaders: true,
                                colHeaders: true,
                                height: 'auto',
                                licenseKey: 'non-commercial-and-evaluation',
                                contextMenu: true,
                                minSpareRows: 1,
                                minSpareCols: 1,
                                stretchH: 'all'
                            });

                            // Make the table responsive
                            window.addEventListener('resize', () => {
                                hot.render();
                            });
                        });

                        function addRow() {
                            const currentData = hot.getData();
                            const newRow = new Array(currentData[0].length).fill('');
                            hot.alter('insert_row', currentData.length);
                            hot.render();
                        }

                        function addColumn() {
                            const currentData = hot.getData();
                            hot.alter('insert_col', currentData[0].length);
                            hot.render();
                        }
                        """
                    }
                }
            }
        }
    }
}

fun createSpreadsheetPage(context: BaseTemplateContext) = SpreadsheetTemplate("Data Entry Spreadsheet", context) {
    // Additional content can be added here if needed
}