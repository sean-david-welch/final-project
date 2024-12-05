// spreadsheet.js

// State management using a closure
const createSpreadsheetStore = () => {
    let hotInstance = null;

    return {
        getHot: () => hotInstance,
        setHot: (instance) => { hotInstance = instance }
    };
};

const spreadsheetStore = createSpreadsheetStore();

// Configuration
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

// Core functions
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

// Action handlers
const addRow = () => {
    const hot = spreadsheetStore.getHot();
    if (!hot) return;

    const currentData = hot.getData();
    hot.alter('insert_row', currentData.length);
    hot.render();
};

const addColumn = () => {
    const hot = spreadsheetStore.getHot();
    if (!hot) return;

    const currentData = hot.getData();
    hot.alter('insert_col', currentData[0].length);
    hot.render();
};

const getData = () => {
    const hot = spreadsheetStore.getHot();
    return hot ? hot.getData() : null;
};

const handleDataChange = (changes) => {
    if (!changes) return;
    console.log('Data changed:', changes);
    // Add any additional data change handling logic here
};

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => initializeSpreadsheet());

// Export functions for external use
window.spreadsheetManager = {
    addRow,
    addColumn,
    getData,
    initialize: initializeSpreadsheet
};