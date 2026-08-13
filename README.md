# Telegram Chat Archive

A self-hosted viewer for Telegram Desktop chat exports. It imports exported HTML into PostgreSQL and turns it into a searchable chat timeline with media, replies, notes, and saved reading progress.

I built this project to make large Telegram exports easier to browse after they leave the Telegram client. The importer keeps message relationships and attachments intact, while the web interface stays close to the familiar chat layout.

## What it can do

- parse multi-file HTML exports from Telegram Desktop;
- display photos, videos, voice messages, stickers, files, and locations;
- preserve replies and forwarded-message attribution;
- filter messages by author, text, and date range;
- jump directly to a message or date;
- attach personal notes to messages;
- save reading progress for each user;
- protect the archive with role-based authentication.

## Stack

- Java 17 and Spring Boot 3
- Spring Data JPA and PostgreSQL
- Spring Security
- Jsoup
- Vanilla JavaScript and Tailwind CSS

## Run locally

You will need Java 17, Maven, PostgreSQL, and an HTML export produced by Telegram Desktop.

1. Create a PostgreSQL database, for example `tg_archive`.
2. Copy `src/main/resources/application.properties.example` to `src/main/resources/application.properties`.
3. Set the database credentials, the absolute export path, your Telegram display name, and new login passwords.
4. Start the application:

   ```bash
   mvn spring-boot:run
   ```

5. Open `http://localhost:8080` and sign in with one of the accounts from your local configuration.

On startup, the application scans the export, imports messages in batches, and links replies. Repeated launches skip messages that are already stored.

## Exporting a chat from Telegram

In Telegram Desktop, open the chat menu and choose **Export chat history**. Select HTML as the format and keep the generated directory structure unchanged. Point `app.export.root-path` to that directory.

The real `application.properties` file is ignored by Git. Keep database passwords and archive paths there, not in the example config.
