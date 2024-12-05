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

const getNestedHeaders = (headers) => [[
    ...headers.map(header => ({ label: header, colspan: 1 }))
]];

const attachHeaderListeners = (headerSpan, col) => {
    if (!headerSpan) return;

    headerSpan.setAttribute('contenteditable', 'true');

    // Remove existing listeners to prevent duplicates
    const newHeaderSpan = headerSpan.cloneNode(true);
    headerSpan.parentNode.replaceChild(newHeaderSpan, headerSpan);

    // Prevent default Handsontable header behavior
    newHeaderSpan.addEventListener('mousedown', (e) => {
        e.stopPropagation();
    });

    // Handle header editing
    newHeaderSpan.addEventListener('blur', () => {
        const headers = spreadsheetStore.getHeaders();
        headers[col] = newHeaderSpan.textContent;
        spreadsheetStore.setHeaders(headers);

        const hot = spreadsheetStore.getHot();
        hot.updateSettings({
            nestedHeaders: getNestedHeaders(headers)
        });
    });

    // Prevent newlines in header
    newHeaderSpan.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            e.preventDefault();
            newHeaderSpan.blur();
        }
    });
};

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
    nestedHeaders: getNestedHeaders(spreadsheetStore.getHeaders()),
    afterGetColHeader: (col, TH) => {
        const headerSpan = TH.querySelector('.colHeader');
        attachHeaderListeners(headerSpan, col);
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

    // Force a complete refresh of the headers and data
    hot.updateSettings({
        nestedHeaders: getNestedHeaders(headers)
    });
    hot.updateData(newData);

    // Ensure all headers are properly initialized
    setTimeout(() => {
        const headerCells = hot.rootElement.querySelectorAll('.colHeader');
        headerCells.forEach((headerSpan, index) => {
            attachHeaderListeners(headerSpan, index);
        });
        hot.render();
    }, 0);
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