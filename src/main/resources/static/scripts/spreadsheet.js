class Spreadsheet {
    constructor(containerId = 'spreadsheet', options = {}) {
        this.containerId = containerId;
        this.instance = null;
        this.headerNames = Array(options.columns || 5).fill().map((_, i) => String.fromCharCode(65 + i));
        this.rows = options.rows || 20;
        this.initialize();
    }

    getNestedHeaders() {
        return [[...this.headerNames.map(header => ({
            label: header, colspan: 1
        }))]];
    }

    getDefaultConfig() {
        return {
            data: Array(this.rows).fill().map(() => Array(this.headerNames.length).fill('')),
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
            afterChange: this.handleDataChange.bind(this),
            className: 'dark-theme'
        };
    }

    initialize() {
        const container = document.getElementById(this.containerId);
        if (!container) {
            console.error(`Container with id '${this.containerId}' not found`);
            return;
        }

        this.applyStyles();
        this.instance = new Handsontable(container, this.getDefaultConfig());
        window.addEventListener('resize', () => {
            if (this.instance) this.instance.render();
        });
    }

    applyStyles() {
        const styleId = 'spreadsheet-styles';
        if (!document.getElementById(styleId)) {
            const style = document.createElement('style');
            style.id = styleId;
            style.textContent = `
                .dark-theme.handsontable {
                    background: #1f2937;
                }
                
                .dark-theme.handsontable th {
                    background-color: #1f2937;
                    color: #ffffff;
                    border-color: #374151;
                }
                
                .dark-theme.handsontable td {
                    background-color: #111827;
                    color: #ffffff;
                    border-color: #374151;
                }
                
                .dark-theme.handsontable .colHeader,
                .dark-theme.handsontable .rowHeader {
                    background-color: #1f2937;
                    color: #ffffff;
                    font-weight: 600;
                }
                
                .dark-theme.handsontable .htCore tbody tr td.current,
                .dark-theme.handsontable .htCore tbody tr th.current {
                    background-color: #374151;
                }
                
                .dark-theme.handsontable .htCore tbody tr td.area,
                .dark-theme.handsontable .htCore tbody tr th.area {
                    background-color: #2563eb !important;
                    opacity: 0.1;
                }
                
                .dark-theme.handsontable tr:hover td,
                .dark-theme.handsontable th:hover {
                    background-color: #374151;
                }
            `;
            document.head.appendChild(style);
        }
    }

    attachHeaderListeners(headerSpan, col) {
        if (!headerSpan) return;

        const newHeaderSpan = headerSpan.cloneNode(true);
        newHeaderSpan.setAttribute('contenteditable', 'true');
        headerSpan.parentNode.replaceChild(newHeaderSpan, headerSpan);

        newHeaderSpan.addEventListener('mousedown', (e) => e.stopPropagation());
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

    addRow() {
        if (!this.instance) return;
        const currentData = this.instance.getData();
        const newData = [...currentData, Array(this.headerNames.length).fill('')];
        this.instance.updateData(newData);
    }

    addColumn() {
        if (!this.instance) return;
        const currentData = this.instance.getData();
        const newHeader = String.fromCharCode(65 + this.headerNames.length);
        this.headerNames.push(newHeader);

        const newData = currentData.map(row => [...row, '']);
        this.instance.loadData(newData);
        this.instance.updateSettings({
            nestedHeaders: this.getNestedHeaders()
        });
    }

    getData() {
        return this.instance?.getData() ?? null;
    }

    getHeaders() {
        return [...this.headerNames];
    }

    handleDataChange(changes) {
        if (!changes) return;
        const event = new CustomEvent('spreadsheet-change', {
            detail: {changes, headers: this.getHeaders()}
        });
        document.dispatchEvent(event);
    }

    static create(containerId, options = {}) {
        return new Spreadsheet(containerId, options);
    }
}

// Initialize spreadsheet when DOM is ready
document.addEventListener('DOMContentLoaded', () => {
    const spreadsheet = Spreadsheet.create('spreadsheet', {
        rows: 20, columns: 5
    });

    window.spreadsheetManager = {
        addRow: () => spreadsheet.addRow(),
        addColumn: () => spreadsheet.addColumn(),
        getData: () => spreadsheet.getData(),
        getHeaders: () => spreadsheet.getHeaders(),
        initialize: (containerId, options) => Spreadsheet.create(containerId, options)
    };
});