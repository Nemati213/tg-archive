package ru.itmo.nemat;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import ru.itmo.nemat.models.Attachment;
import ru.itmo.nemat.models.AttachmentType;
import ru.itmo.nemat.models.Message;

import java.io.File;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class TelegramParser {
    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss 'UTC'XXX");

    private static final String MSG_SELECTOR = ".message.default";
    private static final String TEXT_SELECTOR = ".text";
    private static final String NAME_SELECTOR = ".from_name";
    private static final String DATE_SELECTOR = ".date";
    private static final String REPLY_SELECTOR = ".reply_to a";
    private static final String MEDIA_WRAP_SELECTOR = ".media_wrap";

    public List<Message> parseFile(File file) throws IOException {
        Document doc = Jsoup.parse(file, "UTF-8");
        Elements elements = doc.select(MSG_SELECTOR);

        List<Message> messages = new ArrayList<>();
        String currentAuthor = "Unknown";

        for (Element el : elements) {
            String forwardedFrom = null;

            Elements nameEls = el.select(NAME_SELECTOR);

            for (Element nameEl : nameEls) {
                if (nameEl.parent().hasClass("forwarded")) {
                    Element clone = nameEl.clone();
                    Element dateSpan = clone.selectFirst(".date");
                    if (dateSpan != null) {
                        dateSpan.remove();
                    }
                    forwardedFrom = clone.text().trim();
                } else {
                    currentAuthor = nameEl.text().trim();
                }
            }

            Message msg = convertToMessage(el, currentAuthor, forwardedFrom);
            messages.add(msg);
        }
        return messages;
    }

    private Message convertToMessage(Element el, String author, String forwardedFrom) {
        Long id = Long.parseLong(el.id().replace("message", ""));

        Element textEl = el.selectFirst(TEXT_SELECTOR);
        String text = (textEl != null) ? textEl.text().trim() : "";

        Element dateEl = el.selectFirst(".body > .date");
        if (dateEl == null) {
            dateEl = el.selectFirst(DATE_SELECTOR);
        }

        OffsetDateTime dt = (dateEl != null && dateEl.hasAttr("title"))
                ? OffsetDateTime.parse(dateEl.attr("title"), DATE_FORMATTER)
                : null;

        Long replyId = null;
        Element replyEl = el.selectFirst(REPLY_SELECTOR);
        if (replyEl != null) {
            String href = replyEl.attr("href");
            String rawReplyId = href.substring(href.lastIndexOf("message") + 7);
            replyId = Long.parseLong(rawReplyId);
        }

        Message msg = Message.builder()
                .tgId(id)
                .text(text)
                .author(author)
                .forwardedFrom(forwardedFrom)
                .dateTime(dt)
                .tgReplyId(replyId)
                .mediaPaths(new ArrayList<>())
                .build();

        msg.setMediaPaths(extractAttachments(el, msg));

        return msg;
    }

    private List<Attachment> extractAttachments(Element messageNode, Message msg) {
        List<Attachment> attachments = new ArrayList<>();
        Elements mediaWraps = messageNode.select(MEDIA_WRAP_SELECTOR);

        for (Element wrap : mediaWraps) {
            Element linkEl = wrap.selectFirst("a");
            if (linkEl == null) continue;

            String href = linkEl.attr("href");
            if (href.isEmpty()) continue;

            Element contentContainer = wrap.children().first();
            if (contentContainer == null) continue;

            String classes = contentContainer.className();
            AttachmentType type = AttachmentType.FILE;

            String hrefLower = href.toLowerCase();

            if (classes.contains("photo_wrap")) {
                type = AttachmentType.PHOTO;
            } else if (classes.contains("video_file_wrap") || classes.contains("animated_wrap")) {
                type = AttachmentType.VIDEO;
            } else if (classes.contains("sticker_wrap")) {
                type = AttachmentType.STICKER;
            } else if (classes.contains("media_voice_message")) {
                type = AttachmentType.VOICE;
            } else if (classes.contains("media_location") || classes.contains("media_live_location")) {
                type = AttachmentType.LOCATION;
            } else if (classes.contains("media_video")) {
                type = href.contains("round_video_messages") ? AttachmentType.VIDEO_NOTE : AttachmentType.VIDEO;
            } else if (classes.contains("media_photo")) {
                if (hrefLower.endsWith(".tgs") || hrefLower.endsWith(".webm") || hrefLower.endsWith(".webp")) {
                    type = AttachmentType.STICKER;
                } else {
                    type = AttachmentType.PHOTO;
                }
            }

            attachments.add(new Attachment(null, href, type, msg));
        }
        return attachments;
    }
}