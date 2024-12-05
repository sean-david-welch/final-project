const createSpreadsheetStore = () => {
    let hotInstance = null;
    let headerNames = ['A', 'B', 'C', 'D', 'E'];

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
    height: 'auto',
    licenseKey: 'non-commercial-and-evaluation',
    contextMenu: true,
    minSpareRows: 1,
    minSpareCols: 1,
    stretchH: 'all',
    nestedHeaders: [[
        { label: 'A', colspan: 1 },
        { label: 'B', colspan: 1 },
        { label: 'C', colspan: 1 },
        { label: 'D', colspan: 1 },
        { label: 'E', colspan: 1 }
    ]],
    afterGetColHeader: (col, TH) => {
        const headerSpan = TH.querySelector('.colHeader');
        if (headerSpan) {
            headerSpan.setAttribute('contenteditable', 'true');

            // Prevent default Handsontable header behavior
            headerSpan.addEventListener('mousedown', (e) => {
                e.stopPropagation();
            });

            // Handle header editing
            headerSpan.addEventListener('blur', () => {
                const headers = spreadsheetStore.getHeaders();
                headers[col] = headerSpan.textContent;
                spreadsheetStore.setHeaders(headers);
            });

            // Prevent newlines in header
            headerSpan.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') {
                    e.preventDefault();
                    headerSpan.blur();
                }
            });
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
    const headers = spreadsheetStore.getHeaders();
    const newHeader = String.fromCharCode(65 + headers.length);
    headers.push(newHeader);
    spreadsheetStore.setHeaders(headers);

    hot.updateSettings({
        nestedHeaders: [[
            ...headers.map(header => ({ label: header, colspan: 1 }))
        ]]
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