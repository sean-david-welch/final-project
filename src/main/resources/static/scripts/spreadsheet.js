class Spreadsheet {
    constructor(containerId = 'spreadsheet') {
        this.containerId = containerId;
        this.instance = null;
        this.headerNames = ['A', 'B', 'C', 'D', 'E'];

        this.initialize();
    }

    getNestedHeaders() {
        return [[
            ...this.headerNames.map(header => ({ label: header, colspan: 1 }))
        ]];
    }

    attachHeaderListeners(headerSpan, col) {
        if (!headerSpan) return;

        headerSpan.setAttribute('contenteditable', 'true');

        const newHeaderSpan = headerSpan.cloneNode(true);
        headerSpan.parentNode.replaceChild(newHeaderSpan, headerSpan);

        newHeaderSpan.addEventListener('mousedown', (e) => {
            e.stopPropagation();
        });

        newHeaderSpan.addEventListener('blur', () => {
            this.headerNames[col] = newHeaderSpan.textContent;
            this.instance.updateSettings({
                nestedHeaders: this.getNestedHeaders()
            });
        });

        newHeaderSpan.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') {
                e.preventDefault();
                newHeaderSpan.blur();
            }
        });
    }

    getDefaultConfig() {
        return {
            data: [
                ['', '', '', '', ''],
                ['', '', '', '', ''],
                ['', '', '', '', '']
            ],
            rowHeaders: true,
            height: 'auto',
            licenseKey: 'non-commercial-and-evaluation',
            contextMenu: true,
            minSpareRows: 1,
            minSpareCols: 1,
            stretchH: 'all',
            nestedHeaders: this.getNestedHeaders(),
            afterGetColHeader: (col, TH) => {
                const headerSpan = TH.querySelector('.colHeader');
                this.attachHeaderListeners(headerSpan, col);
            },
            afterChange: this.handleDataChange
        };
    }

    initialize() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        this.instance = new Handsontable(container, this.getDefaultConfig());
        this.setupEventListeners();
    }

    setupEventListeners() {
        window.addEventListener('resize', () => {
            if (this.instance) this.instance.render();
        });
    }

    reinitializeHeaders() {
        this.instance.updateSettings({
            nestedHeaders: this.getNestedHeaders()
        });

        requestAnimationFrame(() => {
            const headerCells = this.instance.rootElement.querySelectorAll('.colHeader');
            headerCells.forEach((headerSpan, idx) => {
                this.attachHeaderListeners(headerSpan, idx);
            });
            this.instance.render();
        });
    }

    addRow() {
        if (!this.instance) return;

        const currentData = this.instance.getData();
        const newRowData = Array(currentData[0].length).fill('');
        const newData = [...currentData, newRowData];
        this.instance.updateData(newData);
    }

    addColumn() {
        if (!this.instance) return;

        const currentData = this.instance.getData();
        const newHeader = String.fromCharCode(65 + this.headerNames.length);
        this.headerNames.push(newHeader);

        const newData = currentData.map(row => [...row, '']);
        this.instance.loadData(newData);
        this.reinitializeHeaders();
    }

    getData() {
        return this.instance ? this.instance.getData() : null;
    }

    getHeaders() {
        return this.headerNames;
    }

    handleDataChange(changes) {
        if (!changes) return;
        console.log('Data changed:', changes);
    }

    static create(containerId) {
        return new Spreadsheet(containerId);
    }
}

// Usage
document.addEventListener('DOMContentLoaded', () => {
    const spreadsheet = Spreadsheet.create('spreadsheet');

    // Expose methods globally if needed
    window.spreadsheetManager = {
        addRow: () => spreadsheet.addRow(),
        addColumn: () => spreadsheet.addColumn(),
        getData: () => spreadsheet.getData(),
        getHeaders: () => spreadsheet.getHeaders(),
        initialize: (containerId) => Spreadsheet.create(containerId)
    };
});