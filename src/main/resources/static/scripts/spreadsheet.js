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
                hotInstance.updateSettings({ colHeaders: headers });
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
    colHeaders: true,
    height: 'auto',
    licenseKey: 'non-commercial-and-evaluation',
    contextMenu: true,
    minSpareRows: 1,
    minSpareCols: 1,
    stretchH: 'all',
    nestedHeaders: [
        spreadsheetStore.getHeaders()
    ],
    afterGetColHeader: (col, TH) => {
        if (col >= 0) {
            TH.className += ' htEditable';
        }
    },
    beforeOnCellMouseDown: (event, coords) => {
        if (coords.row === -1 && coords.col >= 0) {
            event.stopImmediatePropagation();
            const headers = spreadsheetStore.getHeaders();

            const input = document.createElement('input');
            input.value = headers[coords.col] || '';
            input.style.width = '100%';
            input.style.height = '100%';
            input.style.boxSizing = 'border-box';

            const th = event.target.closest('th');
            th.innerHTML = '';
            th.appendChild(input);
            input.focus();

            input.onblur = () => {
                const newHeaders = [...headers];
                newHeaders[coords.col] = input.value;
                spreadsheetStore.setHeaders(newHeaders);
                th.innerHTML = input.value;
            };

            input.onkeydown = (e) => {
                if (e.key === 'Enter') {
                    input.blur();
                }
            };
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