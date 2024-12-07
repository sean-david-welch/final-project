class SpreadsheetTable {
    constructor(selector = '.spreadsheet-table') {
        this.table = document.querySelector(selector);
        this.init();
    }

    init() {
        document.querySelector('[data-action="add-row"]')
            ?.addEventListener('click', () => this.addRow());
    }

    addRow() {
        const columnCount = this.table.querySelector('tr').children.length;
        const row = document.createElement('tr');

        for (let i = 0; i < columnCount; i++) {
            const cell = document.createElement('td');
            cell.contentEditable = "true";
            cell.className = 'spreadsheet-cell';
            row.appendChild(cell);
        }

        this.table.querySelector('tbody').appendChild(row);
    }
}

// Initialize the spreadsheet
document.addEventListener('DOMContentLoaded', () => {
    new SpreadsheetTable();
    document.querySelector('.spreadsheet-add-row').addEventListener('click', function() {
        const lastRow = document.querySelector('.spreadsheet-table tbody tr:last-child');
        const newRow = lastRow.cloneNode(true);
        lastRow.parentNode.appendChild(newRow);
    });
});