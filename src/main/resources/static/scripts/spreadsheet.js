class SpreadsheetTable {
    constructor(selector = '.spreadsheet-table', maxRows = 15) {
        this.table = document.querySelector(selector);
        this.maxRows = maxRows;
        this.init();
    }

    init() {
        this.bindAddButton();
        this.bindTableInputs();
        this.ensureMinimumRows();
    }

    bindAddButton() {
        const addButton = document.querySelector('[data-action="add-row"]');
        addButton?.addEventListener('click', () => this.addRow());
        this.updateAddButtonState();
    }

    bindTableInputs() {
        this.table.querySelector('tbody').addEventListener('input', (e) => {
            if (e.target.cellIndex === 1) {
                this.sanitizeAmountInput(e.target);
            }
            this.updateHiddenField();
        });
    }

    sanitizeAmountInput(cell) {
        const value = cell.textContent.trim();
        if (value && isNaN(value)) {
            cell.textContent = value.replace(/[^\d.]/g, '');
        }
    }

    createCell(type = 'text') {
        if (type === 'category') {
            const cell = document.createElement('td');
            cell.className = 'spreadsheet-cell'; // Add the class here
            const select = this.table.querySelector('tbody tr:first-child td:last-child select').cloneNode(true);
            cell.appendChild(select);
            return cell;
        }

        const cell = document.createElement('td');
        cell.contentEditable = "true";
        cell.className = `spreadsheet-cell${type === 'amount' ? ' amount-cell' : ''}`;
        return cell;
    }

    createRow() {
        const row = document.createElement('tr');
        row.appendChild(this.createCell('text')); // name cell
        row.appendChild(this.createCell('amount')); // amount cell
        row.appendChild(this.createCell('category')); // category cell
        return row;
    }

    clear() {
        const tbody = this.table.querySelector('tbody');
        tbody.innerHTML = '';
        this.ensureMinimumRows();
    }

    ensureMinimumRows() {
        if (this.getCurrentRowCount() === 0) {
            this.addRow();
        }
    }

    getCurrentRowCount() {
        return this.table.querySelector('tbody').children.length;
    }

    updateAddButtonState() {
        const addButton = document.querySelector('[data-action="add-row"]');
        if (!addButton) return;

        const currentRows = this.getCurrentRowCount();
        const isMaxed = currentRows >= this.maxRows;

        addButton.disabled = isMaxed;
        addButton.title = isMaxed ? `Maximum of ${this.maxRows} rows reached` : 'Add new row';
        addButton.classList.toggle('hidden', isMaxed);
    }

    addRow() {
        if (this.getCurrentRowCount() >= this.maxRows) {
            console.warn(`Maximum row limit of ${this.maxRows} reached`);
            return;
        }

        this.table.querySelector('tbody').appendChild(this.createRow());
        this.updateAddButtonState();
        this.updateHiddenField();
    }

    collectTableData() {
        return Array.from(this.table.querySelectorAll('tbody tr'))
            .map(row => {
                const name = row.cells[0].textContent.trim();
                const amount = row.cells[1].textContent.trim();
                const categorySelect = row.cells[2].querySelector('select');
                const categoryType = categorySelect.value;
                return `${name},${amount},${categoryType}`;
            })
            .filter(row => {
                const [name, amount, categoryType] = row.split(',');
                return name || (amount && !isNaN(amount) && categoryType);
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

// Initialize the spreadsheet and expose it globally
document.addEventListener('DOMContentLoaded', () => {
    window.spreadsheetTable = new SpreadsheetTable('.spreadsheet-table', 15);
});