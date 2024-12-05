// spreadsheet.js

const createSpreadsheetStore = () => {
    let hotInstance = null;
    let columnHeaders = ['A', 'B', 'C', 'D', 'E'];

    return {
        getHot: () => hotInstance,
        setHot: (instance) => { hotInstance = instance },
        getHeaders: () => columnHeaders,
        setHeaders: (headers) => {
            columnHeaders = headers;
            if (hotInstance) {
                hotInstance.updateSettings({
                    colHeaders: headers,
                    nestedHeaders: [headers]
                });
            }
        }
    };
};

const spreadsheetStore = createSpreadsheetStore();

const DEFAULT_CONFIG = {
    data: [
        ['', '', '', '', ''],
        ['', '', '', '', ''],
        ['', '', '', '', '']
    ],
    rowHeaders: true,
    colHeaders: spreadsheetStore.getHeaders(),
    height: 'auto',
    licenseKey: 'non-commercial-and-evaluation',
    contextMenu: true,
    minSpareRows: 1,
    minSpareCols: 1,
    stretchH: 'all',
    manualColumnResize: true,
    wordWrap: true,
    comments: true,
    allowInsertColumn: true,
    allowInsertRow: true,
    enterBeginsEditing: true,
    outsideClickDeselects: false,
    headerTooltips: {
        rows: true,
        columns: true
    },
    cells(row, col) {
        const cellProperties = {};
        if (row === -1) {
            cellProperties.readOnly = false;
            cellProperties.editor = 'text';
        }
        return cellProperties;
    },
    afterChange: (changes, source) => {
        if (source === 'edit' && changes) {
            const [row, col, oldVal, newVal] = changes[0];
            if (row === -1) {
                const headers = spreadsheetStore.getHeaders();
                headers[col] = newVal;
                spreadsheetStore.setHeaders(headers);
            }
        }
    }
};

const initializeSpreadsheet = (containerId = 'spreadsheet') => {
    const container = document.getElementById(containerId);
    if (!container) return;

    const hot = new Handsontable(container, {
        ...DEFAULT_CONFIG,
        afterChange: handleDataChange
    });

    spreadsheetStore.setHot(hot);
    setupEventListeners();
};

const setupEventListeners = () => {
    window.addEventListener('resize', () => {
        const hot = spreadsheetStore.getHot();
        if (hot) hot.render();
    });
};

const addRow = () => {
    const hot = spreadsheetStore.getHot();
    if (!hot) return;

    const currentData = hot.getData();
    const newRowData = Array(currentData[0].length).fill('');
    const newData = [...currentData, newRowData];
    hot.updateData(newData);
};

const addColumn = () => {
    const hot = spreadsheetStore.getHot();
    if (!hot) return;

    const currentData = hot.getData();
    const newData = currentData.map(row => [...row, '']);
    hot.updateData(newData);

    const headers = spreadsheetStore.getHeaders();
    spreadsheetStore.setHeaders([...headers, `Column ${headers.length + 1}`]);
};

const getData = () => {
    const hot = spreadsheetStore.getHot();
    return hot ? hot.getData() : null;
};

const handleDataChange = (changes) => {
    if (!changes) return;
    console.log('Data changed:', changes);
};

document.addEventListener('DOMContentLoaded', () => initializeSpreadsheet());

window.spreadsheetManager = {
    addRow,
    addColumn,
    getData,
    initialize: initializeSpreadsheet
};