# PocketOps v3.0.2 - Release Notes

PocketOps v3.0.2 introduces a major branding refresh with renamed features, improves clipboard reliability, adds Telegram/SMS capabilities, integrates a direct YouTube search module, transitions to a multi-platform Social Profiler, adds manual bookmarking features, copies a custom UPI logo, updates the Bookmarks menu icon, and implements direct share-to-save file features.

## 🚀 Improvements & Features

### 1. Local Save (New Feature)
* **Share to Save**: PocketOps now handles arbitrary shared files and text from the Android system share sheet (`ACTION_SEND` and `ACTION_VIEW` for `*/*` MIME types).
* **Location Picker**: Opens the system Storage Access Framework directory picker for you to save files and text locally to any folder.
* **SD Card Icon**: Imported a custom SD Card icon (`ic_sd_card`) from vector assets to represent the Local Save tile on the dashboard.
* **Guide Screen**: Displays step-by-step instructions on how to use Local Save when launched directly from the dashboard.

### 2. Branding Refresh & Icon Updates
* **Bookmarks Icon**: Changed the Bookmarks logo to a double-bookmarks stack (`ic_bookmarks`) for a cleaner dashboard look.
* **UPI Logo Copy**: Replaced the previous UPI logo with the custom vector design (`ic_upi_pay`) copied directly from the `qr-to-upi-main` codebase.

### 3. Global Feature Renaming
We have renamed our dashboard tiles and feature utilities to drop the "Quick" prefix and offer cleaner, action-oriented names:
* **Quick Collect** ➔ **Pay Collect**
* **Quick Chat** ➔ **WhatsApp Direct**
* **Quick Telegram** ➔ **Telegram Direct**
* **Quick SMS** ➔ **Send SMS**
* **Quick Insta** ➔ **Social Profiler**
* **Quick Clip** ➔ **Clip Vault**
* **Quick Link** ➔ **Bookmarks**
* **Quick Web** ➔ **Web Search**

### 4. Social Profiler (Multi-Platform)
* **Unified Profile Search**: Transformed the Instagram-only profiler into a multi-platform **Social Profiler** supporting Instagram, Facebook, Threads, X, and LinkedIn.
* **Vector Selection Row**: Built a horizontal icon-only platform selection row using custom-imported SVGs.
* **Smart Placeholders & Buttons**: Dynamically swaps username placeholders (`anshu07.192` vs `192aakarsh`) and updates the icon inside the "Open Profile" action button.
* **App Intents**: Launches official Android client apps directly with fallback to browser profiles.

### 5. Bookmarks (Manual Additions)
* **Empty State Update**: Changed the empty state notice to: `"Share links to save or add manually"`.
* **Sticky Add Button**: Added a clean, permanent squircle `+` button in the corner to manually input custom titles and URLs.
* **Input Dialog**: Seamlessly validation-checks manual URLs and inserts them at the top of the bookmarks list.

### 6. YT Explorer
* **Direct Search**: Added a dedicated **YT Explorer** dashboard tile and screen to search directly on YouTube.
* **YouTube Integration**: Launches search results directly in the YouTube app or browser at `https://www.youtube.com/results?search_query=<query>`.
* **Play Icon**: Custom play button vector (`ic_play`) imported from assets to match the visual language.

### 7. Telegram Direct & Send SMS
* **Telegram Direct**: Input a phone number, username (e.g. `@username`), or a `t.me` link to initiate chats instantly in the Telegram app without saving contact logs. No message body field is shown.
* **Send SMS**: Start SMS threads using a pre-filled message body text field.
* **Unified History**: Both tools integrate with our unified quick-chat history engine, displaying flag emojis (`✈️` for Telegram usernames) and allowing one-tap relaunches.

### 8. Instant Clipboard Capture (Clip Vault)
* **Real-time Capture**: Re-architected the clipboard change detection engine to extract copied text and image URIs synchronously on the main thread immediately when a system clipboard change is broadcast.
* **Enhanced Reliability**: Resolved an issue where delayed background checks could result in the application losing window focus before reading the clipboard, which previously caused the system to block the read operation. This ensures 100% reliable and instant clip recording.
* **Efficient Persistence**: Offloaded pause verification and disk writing to lifecycle-aware background coroutines *after* retrieving the clip data, keeping the main thread completely responsive.

### 9. Integrated Google Code Scanner
* **Public Google Code Scanner API**: Replaced the custom CameraX camera scanner with Google's Play Services Code Scanner API (`GmsBarcodeScanning`).
* **Zero Permissions**: Eliminated the need to request system Camera permission, handing scanning control to Google Play Services.
* **Stable Experience**: Solved device rotation and camera-binding lifecycle crashes by leveraging out-of-process QR detection.

---

## 📋 System & Build Information
* **Version Name:** `3.0.2`
* **Version Code / Build:** `22`
* **Minimum Android SDK:** API Level `24`
* **Target Android SDK:** API Level `36`
