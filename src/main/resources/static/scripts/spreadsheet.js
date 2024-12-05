const createSpreadsheetStore = () => {
    let hotInstance = null;
    let headerNames = ['A', 'B', 'C', 'D', 'E']; // Store header names

    return {
        getHot: () => hotInstance,
        setHot: (instance) => { hotInstance = instance },
        getHeaders: () => headerNames,
        setHeaders: (headers) => { headerNames = headers }
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
    afterGetColHeader: (col, TH) => {
        // Make header cells clickable
        TH.className += ' htClickable';
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
    setupHeaderListeners(container);
};

const setupEventListeners = () => {
    window.addEventListener('resize', () => {
        const hot = spreadsheetStore.getHot();
        if (hot) hot.render();
    });
};

const setupHeaderListeners = (container) => {
    container.addEventListener('mousedown', (event) => {
        const headerCell = event.target.closest('.htClickable');
        if (!headerCell) return;

        const hot = spreadsheetStore.getHot();
        const headers = spreadsheetStore.getHeaders();
        const colIndex = headerCell.getAttribute('data-col');

        if (colIndex === null) return;

        const newHeader = prompt('Enter new header name:', headers[colIndex]);
        if (newHeader === null) return;

        headers[colIndex] = newHeader;
        spreadsheetStore.setHeaders(headers);
        hot.updateSettings({
            colHeaders: headers
        });
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
    const headers = spreadsheetStore.getHeaders();
    headers.push(String.fromCharCode(65 + headers.length)); // Add next letter as header
    spreadsheetStore.setHeaders(headers);

    hot.updateSettings({
        colHeaders: headers
    });
    hot.updateData(newData);
};

const getData = () => {
    const hot = spreadsheetStore.getHot();
    return hot ? hot.getData() : null;
};

const getHeaders = () => {
    return spreadsheetStore.getHeaders();
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
    getHeaders,
    initialize: initializeSpreadsheet
};