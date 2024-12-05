// spreadsheet.js

const createSpreadsheetStore = () => {
    let hotInstance = null;

    return {
        getHot: () => hotInstance,
        setHot: (instance) => { hotInstance = instance }
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