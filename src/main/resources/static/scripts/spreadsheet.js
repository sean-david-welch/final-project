class SpreadsheetTable {
    constructor(selector = '.spreadsheet-table', maxRows = 15) {
        this.table = document.querySelector(selector);
        this.maxRows = maxRows;
        this.init();
    }

    init() {
        const addButton = document.querySelector('[data-action="add-row"]');
        addButton?.addEventListener('click', () => this.addRow());
        this.updateAddButtonState();

        // Add input handlers to update hidden field on cell changes
        this.table.querySelector('tbody').addEventListener('input', (e) => {
            // If it's the amount column, ensure only numbers
            if (e.target.cellIndex === 1) {
                const value = e.target.textContent.trim();
                if (value && isNaN(value)) {
                    e.target.textContent = value.replace(/[^\d.]/g, '');
                }
            }
            this.updateHiddenField();
        });
    }

    clear() {
        const tbody = this.table.querySelector('tbody');
        // Clear all existing rows
        tbody.innerHTML = '';

        // Add one empty row
        const row = document.createElement('tr');

        // Add name cell
        const nameCell = document.createElement('td');
        nameCell.contentEditable = "true";
        nameCell.className = 'spreadsheet-cell';
        nameCell.addEventListener('input', () => this.updateHiddenField());
        row.appendChild(nameCell);

        // Add amount cell
        const amountCell = document.createElement('td');
        amountCell.contentEditable = "true";
        amountCell.className = 'spreadsheet-cell amount-cell';
        amountCell.addEventListener('input', () => this.updateHiddenField());
        row.appendChild(amountCell);

        tbody.appendChild(row);

        // Update the hidden field
        this.updateHiddenField();

        // Reset the add button state
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
            addButton.classList.add('hidden');
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

        const row = document.createElement('tr');

        // Add name cell
        const nameCell = document.createElement('td');
        nameCell.contentEditable = "true";
        nameCell.className = 'spreadsheet-cell';
        nameCell.addEventListener('input', () => this.updateHiddenField());
        row.appendChild(nameCell);

        // Add amount cell
        const amountCell = document.createElement('td');
        amountCell.contentEditable = "true";
        amountCell.className = 'spreadsheet-cell amount-cell';
        amountCell.addEventListener('input', () => this.updateHiddenField());
        row.appendChild(amountCell);

        this.table.querySelector('tbody').appendChild(row);
        this.updateAddButtonState();
        this.updateHiddenField();
    }

    collectTableData() {
        const rows = Array.from(this.table.querySelectorAll('tbody tr'));
        return rows.map(row => {
            const name = row.cells[0].textContent.trim();
            const amount = row.cells[1].textContent.trim();
            return `${name},${amount}`;
        }).filter(row => {
            const [name, amount] = row.split(',');
            return name || (amount && !isNaN(amount));
        });
    }

    updateHiddenField() {
        const csvData = this.collectTableData().join(';');
        const hiddenField = document.getElementById('spreadsheetData');
        if (hiddenField) {
            hiddenField.value = csvData;
        }
    }
}

// Initialize the spreadsheet
document.addEventListener('DOMContentLoaded', () => {
    new SpreadsheetTable('.spreadsheet-table', 15);
});