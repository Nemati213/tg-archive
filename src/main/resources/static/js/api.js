const API_BASE = '/api/v1';

export async function fetchConfig() {
    const response = await fetch(`${API_BASE}/config`);
    if (!response.ok) {
        throw new Error('Не удалось загрузить конфиг');
    }
    return response.json();
}


export async function fetchMessages(page = 0, size = 50, filters = {}) {
    const url = new URL(`${API_BASE}/messages`, window.location.origin);

    url.searchParams.append('page', page);
    url.searchParams.append('size', size);
    url.searchParams.append('sort', 'dateTime,asc');

    if (filters.query) url.searchParams.append('query', filters.query);
    if (filters.author) url.searchParams.append('author', filters.author);
    if (filters.dateFrom) url.searchParams.append('dateFrom', filters.dateFrom);
    if (filters.dateTo) url.searchParams.append('dateTo', filters.dateTo);
    if (filters.mediaType && filters.mediaType !== 'ALL') {
        url.searchParams.append('mediaType', filters.mediaType);
    }

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error(`Ошибка загрузки сообщений: ${response.status}`);
    }
    return response.json();
}


export async function fetchPageByDate(date, size = 50) {
    const url = new URL(`${API_BASE}/messages/page-by-date`, window.location.origin);

    url.searchParams.append('date', date);
    url.searchParams.append('pageSize', size);

    const response = await fetch(url);
    if (!response.ok) {
        throw new Error('Ошибка при поиске страницы по дате');
    }

    return response.json();
}

export async function fetchPageByMessageId(msgId, size = 50) {
    const url = new URL(`${API_BASE}/messages/page-by-id`, window.location.origin);
    url.searchParams.append('messageId', msgId);
    url.searchParams.append('pageSize', size);

    const response = await fetch(url);
    if (!response.ok) {
        if (response.status === 404) throw new Error('Оригинальное сообщение не найдено');
        throw new Error('Ошибка при вычислении страницы сообщения');
    }
    return response.json();
}

export async function createNote(messageId, text) {
    const response = await fetch(`${API_BASE}/notes`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ messageId, text })
    });

    if (!response.ok) {
        throw new Error(`Ошибка сохранения заметки: ${response.status}`);
    }
    return response.json();
}

export async function deleteNote(noteId) {
    const response = await fetch(`${API_BASE}/notes/${noteId}`, {
        method: 'DELETE'
    });

    if (!response.ok) {
        throw new Error(`Ошибка удаления заметки: ${response.status}`);
    }
}

export async function fetchReadingProgress() {
    const response = await fetch(`${API_BASE}/progress`);
    if (!response.ok) {
        throw new Error('Не удалось получить прогресс чтения');
    }
    const text = await response.text();
    return text ? parseInt(text, 10) : null;
}

export async function saveReadingProgress(messageId) {
    fetch(`${API_BASE}/progress?messageId=${messageId}`, { method: 'POST' })
        .catch(e => console.error('Ошибка сохранения прогресса:', e));
}