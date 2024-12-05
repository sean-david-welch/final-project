class SpreadsheetManager {
    constructor() {
        this.hot = null;
        this.initialize();
    }

    initialize() {
        document.addEventListener('DOMContentLoaded', () => {
            const container = document.getElementById('spreadsheet');
            if (!container) return;
            let Handsontable
            this.hot = new Handsontable(container, {
                data: [['', '', '', '', ''], ['', '', '', '', ''], ['', '', '', '', '']],
                rowHeaders: true,
                colHeaders: true,
                height: 'auto',
                licenseKey: 'non-commercial-and-evaluation',
                contextMenu: true,
                minSpareRows: 1,
                minSpareCols: 1,
                stretchH: 'all',
                afterChange: (changes) => {
                    if (changes) {
                        this.handleDataChange(changes);
                    }
                }
            });

            window.addEventListener('resize', () => {
                this.hot.render();
            });
        });
    }

    addRow() {
        if (!this.hot) return;
        const currentData = this.hot.getData();
        const newRow = new Array(currentData[0].length).fill('');
        this.hot.alter('insert_row', currentData.length);
        this.hot.render();
    }

    addColumn() {
        if (!this.hot) return;
        const currentData = this.hot.getData();
        this.hot.alter('insert_col', currentData[0].length);
        this.hot.render();
    }

    getData() {
        return this.hot ? this.hot.getData() : null;
    }

    handleDataChange(changes) {
        // Implement data change handling logic here
        console.log('Data changed:', changes);
    }
}

// Initialize the spreadsheet manager globally
new SpreadsheetManager();
