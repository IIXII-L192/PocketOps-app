# PocketOps v3.0.2 - Release Notes

PocketOps v3.0.2 introduces a major branding refresh with renamed features, improves clipboard reliability, adds Telegram/SMS capabilities, integrates a direct YouTube search module, transitions to a multi-platform Social Profiler, adds manual bookmarking features, copies a custom UPI logo, updates the Bookmarks menu icon, and implements direct share-to-save file features.

## 🚀 Improvements & Features

### 1. Custom `.pocketops` File Extension (New Feature)
* **Dedicated Backup Format**: To resolve conflicts between file-saving operations and data restores, PocketOps now uses a custom `.pocketops` file type for backup imports/exports.
* **Smart Filtering**:
  * **Local Save** ignores any file with `.pocketops` extension.
  * **Backup Importer** only accepts `.pocketops` files, ignoring all other extensions.
* **Share/Open Compatibility**: Exports automatically write to `PocketOps_Backup_<date>.pocketops` and configure the sharing MIME type to `*/*`.

### 2. Local Save
* **Share to Save**: PocketOps handles arbitrary shared files and text from the Android system share sheet (`ACTION_SEND` and `ACTION_VIEW` for `*/*` MIME types).
* **Location Picker**: Opens the system Storage Access Framework directory picker for you to save files and text locally to any folder.
* **SD Card Icon**: Imported a custom SD Card icon (`ic_sd_card`) from vector assets to represent the Local Save tile on the dashboard.
* **Guide Screen**: Displays step-by-step instructions on how to use Local Save when launched directly from the dashboard.

### 3. Icon Customizations
* **YT Explorer**: Replaced the play button icon with the official YouTube logo vector (`ic_youtube.xml`).
* **Social Profiler**: Replaced the camera icon with a custom chat bubble vector (`ic_social.xml`) representing general social platforms.
* **Bookmarks Icon**: Changed the Bookmarks logo to a double-bookmarks stack (`ic_bookmarks.xml`) for a cleaner dashboard look.
* **UPI Logo Copy**: Replaced the previous UPI logo with the custom vector design (`ic_upi_pay.xml`) copied directly from the `qr-to-upi-main` codebase.

### 4. Global Feature Renaming
We have renamed our dashboard tiles and feature utilities to drop the "Quick" prefix and offer cleaner, action-oriented names:
* **Quick Collect** ➔ **Pay Collect**
* **Quick Chat** ➔ **WhatsApp Direct**
* **Quick Telegram** ➔ **Telegram Direct**
* **Quick SMS** ➔ **Send SMS**
* **Quick Insta** ➔ **Social Profiler**
* **Quick Clip** ➔ **Clip Vault**
* **Quick Link** ➔ **Bookmarks**
* **Quick Web** ➔ **Web Search**

### 5. Social Profiler (Multi-Platform)
* **Unified Profile Search**: Transformed the Instagram-only profiler into a multi-platform **Social Profiler** supporting Instagram, Facebook, Threads, X, and LinkedIn.
* **Vector Selection Row**: Built a horizontal icon-only platform selection row using custom-imported SVGs.
* **Smart Placeholders & Buttons**: Dynamically swaps username placeholders (`anshu07.192` vs `192aakarsh`) and updates the icon inside the "Open Profile" action button.
* **App Intents**: Launches official Android client apps directly with fallback to browser profiles.

### 6. Bookmarks (Manual Additions)
* **Empty State Update**: Changed the empty state notice to: `"Share links to save or add manually"`.
* **Sticky Add Button**: Added a clean, permanent squircle `+` button in the corner to manually input custom titles and URLs.
* **Input Dialog**: Seamlessly validation-checks manual URLs and inserts them at the top of the bookmarks list.

### 7. YT Explorer
* **Direct Search**: Added a dedicated **YT Explorer** dashboard tile and screen to search directly on YouTube.
* **YouTube Integration**: Launches search results directly in the YouTube app or browser at `https://www.youtube.com/results?search_query=<query>`.

### 8. Telegram Direct & Send SMS
* **Telegram Direct**: Input a phone number, username (e.g. `@username`), or a `t.me` link to initiate chats instantly in the Telegram app without saving contact logs. No message body field is shown.
* **Send SMS**: Start SMS threads using a pre-filled message body text field.
* **Unified History**: Both tools integrate with our unified quick-chat history engine, displaying flag emojis (`✈️` for Telegram usernames) and allowing one-tap relaunches.

### 9. Instant Clipboard Capture (Clip Vault)
* **Real-time Capture**: Re-architected the clipboard change detection engine to extract copied text and image URIs synchronously on the main thread immediately when a system clipboard change is broadcast.
* **Enhanced Reliability**: Resolved an issue where delayed background checks could result in the application losing window focus before reading the clipboard, which previously caused the operation to block.
* **Efficient Persistence**: Offloaded pause verification and disk writing to lifecycle-aware background coroutines.

### 10. Integrated Google Code Scanner
* **Public Google Code Scanner API**: Replaced the custom CameraX camera scanner with Google's Play Services Code Scanner API (`GmsBarcodeScanning`).
* **Zero Permissions**: Eliminated the need to request system Camera permission, handing scanning control to Google Play Services.

---

## 📋 System & Build Information
* **Version Name:** `3.0.2`
* **Version Code / Build:** `22`
* **Minimum Android SDK:** API Level `24`
* **Target Android SDK:** API Level `36`
