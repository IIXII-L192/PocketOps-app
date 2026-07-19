# PocketOps v3.0.2 - Release Notes

PocketOps v3.0.2 introduces a major branding refresh with renamed features, improves clipboard reliability, adds Telegram/SMS capabilities, and integrates a direct YouTube search module.

## 🚀 Improvements & Features

### 1. Global Feature Renaming
We have renamed our dashboard tiles and feature utilities to drop the "Quick" prefix and offer cleaner, action-oriented names:
* **Quick Collect** ➔ **Pay Collect**
* **Quick Chat** ➔ **WhatsApp Direct**
* **Quick Telegram** ➔ **Telegram Direct**
* **Quick SMS** ➔ **Send SMS**
* **Quick Insta** ➔ **Insta Profiler**
* **Quick Clip** ➔ **Clip Vault**
* **Quick Link** ➔ **Bookmarks**
* **Quick Web** ➔ **Web Search**

### 2. YT Explorer (New Feature)
* **Direct Search**: Added a new dedicated **YT Explorer** dashboard tile and screen to search directly on YouTube.
* **YouTube Integration**: Launches search results directly in the YouTube app or browser at `https://www.youtube.com/results?search_query=<query>`.
* **Play Icon**: Custom play button vector (`ic_play`) imported from assets to match the visual language.

### 3. Telegram Direct & Send SMS
* **Telegram Direct**: Input a phone number, username (e.g. `@username`), or a `t.me` link to initiate chats instantly in the Telegram app without saving contact logs. No message body field is shown.
* **Send SMS**: Start SMS threads using a pre-filled message body text field.
* **Unified History**: Both tools integrate with our unified quick-chat history engine, displaying flag emojis (`✈️` for Telegram usernames) and allowing one-tap relaunches.

### 4. Instant Clipboard Capture (Clip Vault)
* **Real-time Capture**: Re-architected the clipboard change detection engine to extract copied text and image URIs synchronously on the main thread immediately when a system clipboard change is broadcast.
* **Enhanced Reliability**: Resolved an issue where delayed background checks could result in the application losing window focus before reading the clipboard, which previously caused the system to block the read operation. This ensures 100% reliable and instant clip recording.
* **Efficient Persistence**: Offloaded pause verification and disk writing to lifecycle-aware background coroutines *after* retrieving the clip data, keeping the main thread completely responsive.

### 5. Integrated Google Code Scanner
* **Public Google Code Scanner API**: Replaced the custom CameraX camera scanner with Google's Play Services Code Scanner API (`GmsBarcodeScanning`).
* **Zero Permissions**: Eliminated the need to request system Camera permission, handing scanning control to Google Play Services.
* **Stable Experience**: Solved device rotation and camera-binding lifecycle crashes by leveraging out-of-process QR detection.

---

## 📋 System & Build Information
* **Version Name:** `3.0.2`
* **Version Code / Build:** `22`
* **Minimum Android SDK:** API Level `24`
* **Target Android SDK:** API Level `36`
