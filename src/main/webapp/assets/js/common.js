function ajaxError(xhr) {
    let msg = '操作失敗';
    if (xhr && xhr.responseJSON && xhr.responseJSON.message) {
        msg = xhr.responseJSON.message;
    } else if (xhr && xhr.responseText) {
        msg = xhr.responseText;
    }
    alert(msg);
}

function money(value) {
    return Number(value || 0).toFixed(2);
}

function escapeHtml(value) {
    if (value === null || value === undefined) return '';
    return String(value)
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#039;');
}
