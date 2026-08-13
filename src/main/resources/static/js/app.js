import { MessageRenderer } from './renderer.js';
import { fetchConfig, fetchMessages, fetchPageByDate, fetchPageByMessageId, createNote, deleteNote, fetchReadingProgress, saveReadingProgress } from './api.js';

const PAGE_SIZE = 50;

class App {
    constructor() {
        this.renderer = new MessageRenderer('chat-box', 'message-template', 'lightbox', 'lightbox-img');

        this.currentPage = 0;
        this.firstLoadedPage = 0;
        this.currentFilters = {};
        this.isTimelineMode = true;

        this.searchBtn = document.getElementById('searchBtn');
        this.loadMoreBtn = document.getElementById('loadMoreBtn');
        this.loadPrevBtn = document.getElementById('loadPrevBtn');
        this.calendarInput = document.getElementById('calendarJumpInput');

        this.searchInput = document.getElementById('searchInput');
        this.authorInput = document.getElementById('authorInput');
        this.mediaTypeInput = document.getElementById('mediaTypeInput');
        this.dateFromInput = document.getElementById('dateFrom');
        this.dateToInput = document.getElementById('dateTo');

        this.noteModal = document.getElementById('note-modal');
        this.noteTextarea = document.getElementById('note-textarea');
        this.noteSaveBtn = document.getElementById('note-save-btn');
        this.noteCancelBtn = document.getElementById('note-cancel-btn');
        this.currentNoteMessageId = null;

        this.visibleMessages = new Set();
        this.readingProgressTimer = null;
        this.lastSavedMsgId = null;

        this.observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    this.visibleMessages.add(entry.target);
                } else {
                    this.visibleMessages.delete(entry.target);
                }
            });
            this._scheduleProgressSave();
        }, {
            root: this.renderer.chatBox,
            threshold: 0.1
        });

    }

    async init() {
        try {
            const config = await fetchConfig();
            this.renderer.setOwner(config.chatOwner);

            this._setupEventListeners();

            const savedMessageId = await fetchReadingProgress();

            if (savedMessageId) {
                console.log(`Восстанавливаем сессию. Переход к сообщению ${savedMessageId}...`);
                await this._handleJumpToMessage(savedMessageId);
            } else {
                console.log('Новая сессия. Загружаем начало архива.');
                await this._loadInitialTimeline();
            }
        } catch (error) {
            console.error('Failed to initialize app:', error);
            await this._loadInitialTimeline();
        }
    }

    async _loadInitialTimeline() {
        this.currentPage = 0;
        this.firstLoadedPage = 0;
        this.isTimelineMode = true;
        this.currentFilters = {};

        this.renderer.clear();
        this._clearInputs();

        await this._loadPage(0, false);
    }

    async _loadPage(page, prepend = false) {
        try {
            const data = await fetchMessages(page, PAGE_SIZE, this.currentFilters);
            this.renderer.renderBatch(data.content, prepend);
            this._updatePaginationButtons(data.first, data.last);

            const messages = this.renderer.chatBox.querySelectorAll('.message-row:not(.observed)');
            messages.forEach(msg => {
                this.observer.observe(msg);
                msg.classList.add('observed');
            });
        } catch (error) {
            console.error(`Failed to load page ${page}:`, error);
        }
    }

    async _handleSearch() {
            this.currentFilters = {
                query: this.searchInput.value.trim(),
                author: this.authorInput.value.trim(),
                dateFrom: this.dateFromInput.value,
                dateTo: this.dateToInput.value,
                mediaType: this.mediaTypeInput.value
            };

            const hasActiveFilters = Object.values(this.currentFilters).some(val => val && val !== 'ALL');

            this.renderer.clear();
            this.calendarInput.value = '';

            if (hasActiveFilters) {
                this.isTimelineMode = false;
                this.currentPage = 0;
                this.firstLoadedPage = 0;
                await this._loadPage(0, false);
            } else {
                this.isTimelineMode = true;

                const pageToLoad = this.savedTimelinePage || this.currentPage;

                this.currentPage = pageToLoad;
                this.firstLoadedPage = pageToLoad;
                await this._loadPage(pageToLoad, false);
            }
    }
    async _handleCalendarJump() {
        const selectedDate = this.calendarInput.value;
        if (!selectedDate) return;

        try {
            const targetPage = await fetchPageByDate(selectedDate, PAGE_SIZE);

            this.currentFilters = {};
            this._clearInputs();
            this.isTimelineMode = true;

            this.currentPage = targetPage;
            this.firstLoadedPage = targetPage;

            this.renderer.clear();

            await this._loadPage(targetPage, false);

            setTimeout(() => {
                const scrolled = this.renderer.scrollToDate(selectedDate);
                if (!scrolled && targetPage > 0) {
                    this.renderer.scrollToDate(selectedDate);
                }
            }, 100);

        } catch (error) {
            console.error('Failed to jump to date:', error);
        }
    }

    async _loadMore() {
        this.currentPage++;
        await this._loadPage(this.currentPage, false);
    }

    async _loadPrevious() {
        if (this.firstLoadedPage > 0) {
            const scrollHeightBefore = this.renderer.chatBox.scrollHeight;
            const scrollTopBefore = this.renderer.chatBox.scrollTop;

            this.firstLoadedPage--;
            await this._loadPage(this.firstLoadedPage, true);

            const scrollHeightAfter = this.renderer.chatBox.scrollHeight;
            this.renderer.chatBox.scrollTop = scrollTopBefore + (scrollHeightAfter - scrollHeightBefore);
        }
    }

    _updatePaginationButtons(isFirst, isLast) {
        if (isLast) {
            this.loadMoreBtn.classList.add('hidden');
        } else {
            this.loadMoreBtn.classList.remove('hidden');
        }

        if (this.isTimelineMode && this.firstLoadedPage > 0) {
            this.loadPrevBtn.classList.remove('hidden');
        } else {
            this.loadPrevBtn.classList.add('hidden');
        }
    }

    _clearInputs() {
        this.searchInput.value = '';
        this.authorInput.value = '';
        this.dateFromInput.value = '';
        this.dateToInput.value = '';
        this.mediaTypeInput.value = 'ALL';
    }

    _setupEventListeners() {
        this.searchBtn.addEventListener('click', () => this._handleSearch());

        this.loadMoreBtn.addEventListener('click', () => this._loadMore());

        this.loadPrevBtn.addEventListener('click', () => this._loadPrevious());

        this.calendarInput.addEventListener('change', () => this._handleCalendarJump());

        this.searchInput.addEventListener('keydown', (e) => {
            if (e.key === 'Enter') this._handleSearch();
        });

        let scrollTimeout;
        this.renderer.chatBox.addEventListener('scroll', () => {
            this.renderer.chatBox.classList.add('scrolling');
            clearTimeout(scrollTimeout);
            scrollTimeout = setTimeout(() => {
                this.renderer.chatBox.classList.remove('scrolling');
            }, 1200);
        });

        this.renderer.chatBox.addEventListener('click', async (e) => {
            const replyBlock = e.target.closest('.msg-reply');
            if (replyBlock) {
                const targetMsgId = replyBlock.getAttribute('data-reply-to-id');
                if (targetMsgId) await this._handleJumpToMessage(targetMsgId);
                return;
            }

            const addBtn = e.target.closest('.add-note-btn');
            if (addBtn) {
                const msgRow = addBtn.closest('.message-row');
                if (msgRow) {
                    this.currentNoteMessageId = msgRow.id.replace('msg-', '');
                    this.noteModal.classList.remove('hidden');
                    this.noteTextarea.focus();
                }
                return;
            }

            const deleteBtn = e.target.closest('.delete-note-btn');
            if (deleteBtn) {
                const noteItem = deleteBtn.closest('.note-item');
                const noteId = noteItem.getAttribute('data-note-id');

                if (confirm('Точно удалить заметку?')) {
                    try {
                        await deleteNote(noteId);
                        this.renderer.removeNoteFromDom(noteId);
                    } catch (error) {
                        console.error('Ошибка удаления:', error);
                        alert('Не удалось удалить заметку');
                    }
                }
            }
        });

        let activeMedia = null;
        this.renderer.chatBox.addEventListener('play', (e) => {
            if (activeMedia && activeMedia !== e.target) {
                activeMedia.pause();
                if (activeMedia.tagName === 'VIDEO' && activeMedia.classList.contains('video-note')) {
                    activeMedia.muted = true;
                    activeMedia.classList.replace('border-green-500', 'border-blue-500');
                }
            }
            activeMedia = e.target;
        }, true);

        this.noteCancelBtn.addEventListener('click', () => {
            this.noteModal.classList.add('hidden');
            this.noteTextarea.value = '';
            this.currentNoteMessageId = null;
        });

        this.noteSaveBtn.addEventListener('click', async () => {
            const text = this.noteTextarea.value.trim();
            if (!text || !this.currentNoteMessageId) return;

            try {
                this.noteSaveBtn.disabled = true;
                this.noteSaveBtn.textContent = '...';

                const newNote = await createNote(this.currentNoteMessageId, text);
                this.renderer.appendNoteToMessage(this.currentNoteMessageId, newNote);
                this.noteCancelBtn.click();
            } catch (error) {
                console.error('Ошибка сохранения заметки:', error);
                alert('Не удалось сохранить заметку');
            } finally {
                this.noteSaveBtn.disabled = false;
                this.noteSaveBtn.textContent = 'Сохранить';
            }
        });
    }

    async _handleJumpToMessage(msgId) {
            const isFoundInDom = this.renderer.scrollToMessage(msgId);
            if (isFoundInDom) return;

            try {
                console.log(`Сообщение ${msgId} не в DOM. Ищем страницу...`);

                const targetPage = await fetchPageByMessageId(msgId, PAGE_SIZE);

                this.currentFilters = {};
                this._clearInputs();
                this.isTimelineMode = true;

                this.currentPage = targetPage;
                this.firstLoadedPage = targetPage;

                this.renderer.clear();
                await this._loadPage(targetPage, false);

                setTimeout(() => {
                    this.renderer.scrollToMessage(msgId);
                }, 100);

            } catch (error) {
                console.error('Ошибка перехода к сообщению:', error);
                alert('Оригинальное сообщение не найдено (возможно, оно было удалено или не экспортировано)');
            }
        }

    _scheduleProgressSave() {
        if (!this.isTimelineMode) return;

        clearTimeout(this.readingProgressTimer);

        this.readingProgressTimer = setTimeout(() => {
            if (this.visibleMessages.size === 0) return;

            const visibleArray = Array.from(this.visibleMessages);
            const chatBoxTop = this.renderer.chatBox.getBoundingClientRect().top;

            let topMsg = visibleArray[0];
            let minDiff = Math.abs(topMsg.getBoundingClientRect().top - chatBoxTop);

            for (let i = 1; i < visibleArray.length; i++) {
                const diff = Math.abs(visibleArray[i].getBoundingClientRect().top - chatBoxTop);
                if (diff < minDiff) {
                    minDiff = diff;
                    topMsg = visibleArray[i];
                }
            }

            const msgId = topMsg.id.replace('msg-', '');

            if (this.lastSavedMsgId !== msgId) {
                this.lastSavedMsgId = msgId;
                saveReadingProgress(msgId);
                console.log('Прогресс сохранен на сообщении:', msgId);
            }
        }, 1200);
    }

}

const app = new App();
document.addEventListener('DOMContentLoaded', () => app.init());