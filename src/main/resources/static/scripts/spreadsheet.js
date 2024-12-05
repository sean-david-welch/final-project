// spreadsheet.js

const createSpreadsheetStore = () => {
    let hotInstance = null;
    let columnHeaders = ['A', 'B', 'C', 'D', 'E'];  // Default headers

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
    colHeaders: spreadsheetStore.getHeaders(),
    height: 'auto',
    licenseKey: 'non-commercial-and-evaluation',
    contextMenu: true,
    minSpareRows: 1,
    minSpareCols: 1,
    stretchH: 'all'
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

    // Add a new header for the new column
    const headers = spreadsheetStore.getHeaders();
    const newHeader = prompt('Enter column header:', `Column ${headers.length + 1}`);
    if (newHeader) {
        spreadsheetStore.setHeaders([...headers, newHeader]);
    }
};

const updateColumnHeader = (columnIndex) => {
    const headers = spreadsheetStore.getHeaders();
    const currentHeader = headers[columnIndex];
    const newHeader = prompt('Enter new column header:', currentHeader);

    if (newHeader && newHeader !== currentHeader) {
        const updatedHeaders = [...headers];
        updatedHeaders[columnIndex] = newHeader;
        spreadsheetStore.setHeaders(updatedHeaders);
    }
};

const getData = () => {
    const hot = spreadsheetStore.getHot();
    return hot ? hot.getData() : null;
};

const handleDataChange = (changes) => {
    if (!changes) return;
    console.log('Data changed:', changes);
};

document.addEventListener('DOMContentLoaded', () => {
    initializeSpreadsheet();

    // Add double-click event listener for header editing
    const container = document.getElementById('spreadsheet');
    if (container) {
        container.addEventListener('dblclick', (event) => {
            const headerElement = event.target.closest('.handsontable .ht_clone_top th');
            if (headerElement) {
                const columnIndex = headerElement.cellIndex - 1; // Adjust for row headers
                if (columnIndex >= 0) {
                    updateColumnHeader(columnIndex);
                }
            }
        });
    }
});

window.spreadsheetManager = {
    addRow,
    addColumn,
    getData,
    initialize: initializeSpreadsheet,
    updateColumnHeader
};