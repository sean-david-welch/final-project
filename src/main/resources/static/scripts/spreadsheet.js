class EditableTable {
    constructor(containerId) {
        this.container = document.getElementById(containerId);
        this.init();
    }

    init() {
        const table = document.createElement('table');
        table.innerHTML = `
            <tr>
                <th>Name</th>
                <th>Amount</th>
                <th>Category</th>
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
}