class EditableTable {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.init();
    }

    init() {
        const table = document.createElement('table');
        table.innerHTML = `
            <tr>
                <th contenteditable="true">Header 1</th>
                <th contenteditable="true">Header 2</th>
                <th contenteditable="true">Header 3</th>
            </tr>
            <tr>
                <td contenteditable="true"></td>
                <td contenteditable="true"></td>
                <td contenteditable="true"></td>
            </tr>`;

        table.className = 'editable-table';
        this.container.appendChild(table);

        // Add new row button
        const addRowBtn = document.createElement('button');
        addRowBtn.textContent = 'Add Row';
        addRowBtn.onclick = () => this.addRow();
        this.container.appendChild(addRowBtn);

        this.addStyles();
    }

    addRow() {
        const row = document.createElement('tr');
        const columnCount = this.container.querySelector('tr').children.length;

        for (let i = 0; i < columnCount; i++) {
            const cell = document.createElement('td');
            cell.contentEditable = true;
            row.appendChild(cell);
        }

        this.container.querySelector('table').appendChild(row);
    }

    addStyles() {
        const style = document.createElement('style');
        style.textContent = `
            .editable-table {
                border-collapse: collapse;
                margin-bottom: 1rem;
            }
            .editable-table th, .editable-table td {
                border: 1px solid #ccc;
                padding: 8px;
                min-width: 100px;
            }
            .editable-table th:focus, .editable-table td:focus {
                outline: 2px solid #007bff;
            }
            button {
                padding: 8px 16px;
                background: #007bff;
                color: white;
                border: none;
                border-radius: 4px;
                cursor: pointer;
            }
        `;
        document.head.appendChild(style);
    }
}