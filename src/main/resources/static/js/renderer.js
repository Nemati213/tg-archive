
import { getAvatarColor, formatTime, formatIsoDate, extractFileName, formatFullDateTime } from './utils.js';
export class MessageRenderer {
    constructor(chatBoxId, templateId, lightboxId, lightboxImgId) {
        this.chatBox = document.getElementById(chatBoxId);
        this.template = document.getElementById(templateId).content;
        this.lightbox = document.getElementById(lightboxId);
        this.lightboxImg = document.getElementById(lightboxImgId);
        this.chatOwner = 'System';
        this.lastAuthor = null;
        this.lastDate = null;

        this._initLightbox();
    }

    setOwner(ownerName) {
        this.chatOwner = ownerName;
    }

    clear() {
        const prevBtn = this.chatBox.querySelector('#loadPrevBtn');
        const prevBtnWrapper = prevBtn ? prevBtn.parentElement : null;

        this.chatBox.innerHTML = '';

        if (prevBtnWrapper) {
            this.chatBox.appendChild(prevBtnWrapper);
        }

        this.lastAuthor = null;
        this.lastDate = null;
    }

    renderBatch(messages, prepend = false) {
        if (prepend) {
            const firstMessage = this.chatBox.querySelector('.message-row, .w-full.flex.justify-center.my-4');

            messages.forEach(msg => {
                const node = this._createMessageNode(msg);
                if (firstMessage) {
                    this.chatBox.insertBefore(node, firstMessage);
                } else {
                    this.chatBox.appendChild(node);
                }
            });
        } else {
            messages.forEach(msg => {
                const node = this._createMessageNode(msg);
                this.chatBox.appendChild(node);
            });
        }
    }

    scrollToDate(isoDate) {
        const targetMessage = document.querySelector(`.message-row[data-date="${isoDate}"]`);
        if (targetMessage) {
            targetMessage.scrollIntoView({ behavior: 'smooth', block: 'center' });
            targetMessage.classList.add('highlight-animation');
            setTimeout(() => targetMessage.classList.remove('highlight-animation'), 2000);
            return true;
        }
        return false;
    }

    scrollToMessage(msgId) {
        const targetMessage = document.getElementById(`msg-${msgId}`);
        if (targetMessage) {
            targetMessage.scrollIntoView({ behavior: 'smooth', block: 'center' });

            targetMessage.classList.add('highlight-animation');
            setTimeout(() => targetMessage.classList.remove('highlight-animation'), 2000);
            return true;
        }
        return false;
    }

    _initLightbox() {
        this.lightbox.addEventListener('click', () => {
            this.lightbox.classList.add('hidden');
            this.lightboxImg.src = '';
        });
    }

    _createMessageNode(msg) {
        const fragment = document.createDocumentFragment();
        const currentIsoDate = formatIsoDate(msg.dateTime);

        if (this.lastDate !== currentIsoDate) {
            fragment.appendChild(this._createDateDivider(msg.dateTime));
            this.lastDate = currentIsoDate;
            this.lastAuthor = null;
        }

        const msgNode = document.importNode(this.template, true);
        const row = msgNode.querySelector('.message-row');
        const bubble = msgNode.querySelector('.message-bubble');
        const avatar = msgNode.querySelector('.msg-avatar');
        const header = msgNode.querySelector('.msg-header');
        const authorEl = msgNode.querySelector('.msg-author');
        const dateEl = msgNode.querySelector('.msg-date');
        const textEl = msgNode.querySelector('.msg-text');

        row.setAttribute('data-date', currentIsoDate);
        row.id = `msg-${msg.id}`;

        const isOwner = msg.author === this.chatOwner;

        if (isOwner) {
            row.classList.add('self-end', 'flex-row-reverse');
            bubble.classList.add('bg-blue-100', 'rounded-br-none');
        } else {
            row.classList.add('self-start');
            bubble.classList.add('bg-white', 'rounded-bl-none');
        }

        const hasText = msg.text && msg.text.trim() !== "";
        const hasMedia = msg.mediaPaths && msg.mediaPaths.length > 0;

        const timeStr = formatTime(msg.dateTime);
        dateEl.textContent = timeStr;

        if (hasMedia && !hasText) {
            bubble.classList.remove('bg-white', 'bg-blue-100', 'p-3', 'shadow-sm');
            bubble.classList.add('bg-transparent', 'p-0', 'relative');

            dateEl.className = 'absolute bottom-2 right-2 text-[9px] text-white bg-black/50 px-1.5 py-0.5 rounded-full select-none z-10';
            bubble.appendChild(dateEl);
            textEl.classList.add('hidden');
        } else {
            textEl.classList.remove('hidden');
            dateEl.className = 'text-[9px] text-gray-400 float-right mt-1.5 ml-2 select-none';
            textEl.textContent = msg.text;
            textEl.appendChild(dateEl);
        }

        if (isOwner || msg.author === this.lastAuthor) {
            header.classList.add('hidden');

            if (msg.author === this.lastAuthor) {
                avatar.classList.add('opacity-0', 'pointer-events-none');
                row.classList.remove('my-1');
            }
        } else {
            header.classList.remove('hidden');
            authorEl.textContent = msg.author;
            avatar.classList.remove('opacity-0', 'pointer-events-none');
            avatar.textContent = msg.author.charAt(0).toUpperCase();
            avatar.className = `msg-avatar w-8 h-8 rounded-full flex items-center justify-center text-white text-xs font-bold shrink-0 self-start mt-1 select-none ${getAvatarColor(msg.author)}`;
        }

        this.lastAuthor = msg.author;

        if (msg.forwardedFrom) {
            const forwardedBlock = msgNode.querySelector('.msg-forwarded');
            forwardedBlock.classList.remove('hidden');
            forwardedBlock.querySelector('.forwarded-name').textContent = msg.forwardedFrom;

            if (hasMedia && !hasText) {
                bubble.classList.add('p-3', 'shadow-sm');
                if (isOwner) bubble.classList.add('bg-blue-100');
                else bubble.classList.add('bg-white');
                bubble.classList.remove('bg-transparent', 'p-0');
            }
        }

        if (msg.replyTo) {
            const replyBlock = msgNode.querySelector('.msg-reply');
            replyBlock.classList.remove('hidden');
            replyBlock.classList.add('cursor-pointer', 'hover:bg-blue-100', 'transition-colors');
            replyBlock.setAttribute('data-reply-to-id', msg.replyTo.id);

            replyBlock.querySelector('.reply-author').textContent = msg.replyTo.author;
            replyBlock.querySelector('.reply-text').textContent = msg.replyTo.text;
        }

        if (hasMedia) {
            this._renderMedia(msg.mediaPaths, msgNode.querySelector('.msg-media'), bubble);
        }
        const notesContainer = msgNode.querySelector('.msg-notes-container');
            if (msg.notes && msg.notes.length > 0) {
                msg.notes.forEach(note => {
                    notesContainer.appendChild(this._buildNoteElement(note));
                });
            }
        fragment.appendChild(msgNode);
        return fragment;
    }
    _createDateDivider(dateTime) {
        const dateObj = new Date(dateTime);
        const options = { day: 'numeric', month: 'long', year: 'numeric' };
        const text = dateObj.toLocaleDateString('ru-RU', options);

        const div = document.createElement('div');
        div.className = 'w-full flex justify-center my-4 sticky top-0 z-20 pointer-events-none transition-opacity duration-500 date-divider-sticky';
        div.innerHTML = `<span class="bg-white text-gray-500 text-xs font-bold py-1 px-3 rounded-full shadow-sm border border-gray-100 select-none">${text}</span>`;
        return div;
    }


    _renderMedia(attachments, container, bubble) {
        attachments.forEach(attachment => {
            const fullPath = '/media/' + attachment.path;

            if (attachment.type === 'PHOTO') {
                const img = document.createElement('img');
                img.src = fullPath;
                img.className = 'max-w-sm max-h-80 rounded-lg object-contain cursor-zoom-in hover:opacity-95 transition-opacity';
                img.addEventListener('click', () => {
                    this.lightboxImg.src = fullPath;
                    this.lightbox.classList.remove('hidden');
                });
                container.appendChild(img);

            } else if (attachment.type === 'STICKER') {
                const img = document.createElement('img');
                img.src = fullPath;
                img.className = 'w-32 h-32 object-contain cursor-pointer';
                bubble.classList.remove('bg-white', 'bg-blue-100', 'p-3', 'shadow-sm');
                bubble.classList.add('bg-transparent', 'p-0');
                container.appendChild(img);

            } else if (attachment.type === 'VIDEO_NOTE') {
                const video = document.createElement('video');
                video.src = fullPath;
                video.className = 'video-note w-48 h-48 rounded-full object-cover cursor-pointer shadow-md border-4 border-blue-500 transition-all duration-300 hover:scale-105';
                video.loop = true;
                video.muted = true;
                video.playsInline = true;

                video.addEventListener('click', (e) => {
                    e.preventDefault();
                    if (video.paused) {
                        video.muted = false;
                        video.currentTime = 0;
                        video.play();
                        video.classList.replace('border-blue-500', 'border-green-500');
                    } else if (!video.muted) {
                        video.pause();
                        video.muted = true;
                        video.classList.replace('border-green-500', 'border-blue-500');
                    } else {
                        video.muted = false;
                        video.play();
                        video.classList.replace('border-blue-500', 'border-green-500');
                    }
                });
                container.appendChild(video);

            } else if (attachment.type === 'VIDEO') {
                const video = document.createElement('video');
                video.src = fullPath;
                video.controls = true;
                video.className = 'max-w-md max-h-96 rounded-2xl shadow-lg border border-gray-200 bg-black outline-none';
                container.appendChild(video);
            } else if (attachment.type === 'VOICE') {
                const audio = document.createElement('audio');
                audio.src = fullPath;
                audio.controls = true;
                audio.className = 'max-w-xs h-10 rounded-lg';
                container.appendChild(audio);

            } else if (attachment.type === 'FILE' || attachment.type === 'LOCATION') {
                const link = document.createElement('a');
                link.href = fullPath;
                link.target = '_blank';
                link.className = 'flex items-center gap-2 text-blue-600 hover:text-blue-800 text-sm font-semibold underline py-1';
                link.innerHTML = attachment.type === 'LOCATION'
                    ? `📍 Локация`
                    : `📄 ${extractFileName(attachment.path)}`;
                container.appendChild(link);
            }
        });
    }

    _buildNoteElement(note) {
        const div = document.createElement('div');
        div.className = 'note-item bg-orange-50 p-2 rounded-lg text-xs relative group/note';
        div.id = `note-${note.id}`;
        div.setAttribute('data-note-id', note.id);

        const dateTimeStr = formatFullDateTime(note.createdAt);

        div.innerHTML = `
            <div class="flex justify-between items-start gap-2 mb-1">
                <span class="font-bold text-orange-600 text-[11px] uppercase tracking-wide flex items-center gap-1">
                    <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z"></path></svg>
                    ${note.author || 'Аноним'}
                </span>
                <span class="text-[9px] text-orange-400 whitespace-nowrap">${dateTimeStr}</span>
            </div>
            <div class="text-gray-700 mt-1 whitespace-pre-wrap">${note.text}</div>

            <button class="delete-note-btn opacity-0 group-hover/note:opacity-100 absolute -right-2 -top-2 bg-red-100 text-red-500 hover:bg-red-500 hover:text-white rounded-full p-1 transition-all" title="Удалить">
                <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
            </button>
         `;
        return div;
    }

        appendNoteToMessage(msgId, noteDto) {
            const msgRow = document.getElementById(`msg-${msgId}`);
            if (!msgRow) return;

            const container = msgRow.querySelector('.msg-notes-container');
            if (container) {
                container.appendChild(this._buildNoteElement(noteDto));
            }
        }

        removeNoteFromDom(noteId) {
            const noteEl = document.getElementById(`note-${noteId}`);
            if (noteEl) {
                noteEl.remove();
            }
        }
}