class SpreadsheetTable {
    constructor(selector = '.spreadsheet-table', maxRows = 15) {
        this.table = document.querySelector(selector);
        this.maxRows = maxRows;
        this.init();
    }

    init() {
        const addButton = document.querySelector('[data-action="add-row"]');
        addButton?.addEventListener('click', () => this.addRow());

        // Update button state based on initial row count
        this.updateAddButtonState();
    }

    getCurrentRowCount() {
        return this.table.querySelector('tbody').children.length;
    }

    updateAddButtonState() {
        const addButton = document.querySelector('[data-action="add-row"]');
        if (!addButton) return;

        const currentRows = this.getCurrentRowCount();
        if (currentRows >= this.maxRows) {
            addButton.disabled = true;
            addButton.title = `Maximum of ${this.maxRows} rows reached`;
            addButton.classList.add('disabled');
        } else {
            addButton.disabled = false;
            addButton.title = 'Add new row';
            addButton.classList.remove('disabled');
        }
    }

    addRow() {
        if (this.getCurrentRowCount() >= this.maxRows) {
            console.warn(`Maximum row limit of ${this.maxRows} reached`);
            return;
        }

        const columnCount = this.table.querySelector('tr').children.length;
        const row = document.createElement('tr');

        for (let i = 0; i < columnCount; i++) {
            const cell = document.createElement('td');
            cell.contentEditable = "true";
            cell.className = 'spreadsheet-cell';
            row.appendChild(cell);
        }

        this.table.querySelector('tbody').appendChild(row);
        this.updateAddButtonState();
    }
}

// Initialize the spreadsheet
document.addEventListener('DOMContentLoaded', () => {
    new SpreadsheetTable('.spreadsheet-table', 15);
});