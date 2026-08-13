export function getAvatarColor(name) {
    const colors = [
        'bg-red-500', 'bg-orange-500', 'bg-green-500',
        'bg-teal-500', 'bg-blue-500', 'bg-indigo-500',
        'bg-purple-500', 'bg-pink-500'
    ];
    let hash = 0;
    for (let i = 0; i < name.length; i++) {
        hash = name.charCodeAt(i) + ((hash << 5) - hash);
    }
    const index = Math.abs(hash % colors.length);
    return colors[index];
}

export function formatTime(dateString) {
    if (!dateString) return '';
    return new Date(dateString).toLocaleTimeString('ru-RU', {
        hour: '2-digit',
        minute: '2-digit'
    });
}

export function formatIsoDate(dateString) {
    if (!dateString) return '';
    const d = new Date(dateString);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
}



export function extractFileName(path) {
    if (!path) return 'file';
    return path.substring(path.lastIndexOf('/') + 1);
}

export function formatFullDateTime(dateString) {
    if (!dateString) return '';
    return new Intl.DateTimeFormat('ru-RU', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit'
    }).format(new Date(dateString));
}
