document.getElementById('exportBtn').addEventListener('click', function() {
    // 1. Seleccionamos la tabla (la versión Desktop que tiene la estructura completa)
    const table = document.querySelector('table');
    const rows = Array.from(table.querySelectorAll('tr'));

    // 2. Procesamos los datos
    const csvContent = rows.map(row => {
        const cells = Array.from(row.querySelectorAll('th, td'));
        return cells.map((cell, index) => {
            // Ignoramos la columna de "Acciones" (la última)
            if (index === cells.length - 1) return null;

            // Limpiamos el texto: quitamos saltos de línea y espacios extra
            let data = cell.innerText.replace(/\n/g, ' ').trim();

            // Si el dato tiene comas, lo envolvemos en comillas para no romper el CSV
            return `"${data}"`;
        }).filter(item => item !== null).join(",");
    }).join("\n");

    // 3. Creamos el archivo y lo descargamos
    const blob = new Blob([new Uint8Array([0xEF, 0xBB, 0xBF]), csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement("a");

    const fecha = new Date().toLocaleDateString().replace(/\//g, '-');
    link.setAttribute("href", url);
    link.setAttribute("download", `reporte_compradores_${fecha}.csv`);
    link.style.visibility = 'hidden';

    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
});