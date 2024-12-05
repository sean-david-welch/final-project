class Spreadsheet {
    constructor(containerId = 'spreadsheet') {
        this.containerId = containerId;
        this.instance = null;
        this.headerNames = ['A', 'B', 'C', 'D', 'E'];
        this.initialize();
    }

    getNestedHeaders() {
        return [[...this.headerNames.map(header => ({label: header, colspan: 1}))]];
    }

    getDefaultConfig() {
        return {
            data: [['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', '']],
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
            afterChange: this.handleDataChange,
            className: 'dark-theme'
        };
    }

    initialize() {
        const container = document.getElementById(this.containerId);
        if (!container) return;

        const style = document.createElement('style');
        style.textContent = `
            .handsontable {
                background: #1f2937;
            }
            
            .handsontable th {
                background-color: #1f2937;
                color: #ffffff;
                border-color: #374151;
            }
            
            .handsontable td {
                background-color: #111827;
                color: #ffffff;
                border-color: #374151;
            }
            
            .handsontable .colHeader,
            .handsontable .rowHeader {
                background-color: #1f2937;
                color: #ffffff;
                font-weight: 600;
            }
            
            .handsontable .htCore tbody tr td.current,
            .handsontable .htCore tbody tr th.current {
                background-color: #374151;
            }
            
            .handsontable .htCore tbody tr td.area,
            .handsontable .htCore tbody tr th.area {
                background-color: #2563eb !important;
                opacity: 0.1;
            }
            
            .handsontable .htCore tbody tr td.highlight {
                background-color: #374151;
            }
            
            .handsontable .wtBorder {
                background-color: #2563eb !important;
            }
            
            .handsontable .wtBorder.current {
                background-color: #2563eb !important;
            }
            
            .handsontable tr:hover td {
                background-color: #374151;
            }
            
            .handsontable th:hover {
                background-color: #374151;
            }
            
            .handsontable .htNoFrame+td, 
            .handsontable .htNoFrame+th, 
            .handsontable td:first-of-type, 
            .handsontable th:first-child, 
            .handsontable th:nth-child(2), 
            .handsontable.htRowHeaders thead tr th:nth-child(2) {
                border-left: 1px solid #374151;
            }
        `;

        document.head.appendChild(style);
        this.instance = new Handsontable(container, this.getDefaultConfig());
        this.setupEventListeners();
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

    setupEventListeners() {
        window.addEventListener('resize', () => {
            if (this.instance) this.instance.render();
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

document.addEventListener('DOMContentLoaded', () => {
    const spreadsheet = Spreadsheet.create('spreadsheet');
    window.spreadsheetManager = {
        addRow: () => spreadsheet.addRow(),
        addColumn: () => spreadsheet.addColumn(),
        getData: () => spreadsheet.getData(),
        getHeaders: () => spreadsheet.getHeaders(),
        initialize: (containerId) => Spreadsheet.create(containerId)
    };
});