# PocketOps v3.0.2 - Release Notes

PocketOps v3.0.2 focuses on improving **QuickClip** reliability and updating system configurations.

## 🚀 Improvements & Optimizations

### 1. Instant Clipboard Capture (QuickClip)
* **Real-time Capture**: Re-architected the clipboard change detection engine to extract copied text and image URIs synchronously on the main thread immediately when a system clipboard change is broadcast.
* **Enhanced Reliability**: Resolved an issue where delayed background checks could result in the application losing window focus before reading the clipboard, which previously caused the system to block the read operation. This ensures 100% reliable and instant clip recording.
* **Efficient Persistence**: Offloaded pause verification and disk writing to lifecycle-aware background coroutines *after* retrieving the clip data, keeping the main thread completely responsive.

### 2. Quick Telegram & Quick SMS
* **Quick Telegram**: Launched straight from the dashboard. Input a phone number, username (e.g., `@username`), or a `t.me` link to initiate chats instantly in the Telegram app without saving contact logs.
* **Quick SMS**: Start SMS threads directly using the new message text field to pre-fill message bodies before launching your messaging app.
* **Unified History**: Both tools fully integrate with the existing quick-chat history engine, allowing you to store, toggle, and quickly relaunch previous chat targets.

### 3. Integrated Google Code Scanner
* **Public Google Code Scanner API**: Replaced the custom CameraX camera scanner with Google's Play Services Code Scanner API (`GmsBarcodeScanning`).
* **Zero Permissions**: Eliminated the need to request system Camera permission, handing scanning control to Google Play Services.
* **Stable Experience**: Solved device rotation and camera-binding lifecycle crashes by leveraging out-of-process QR detection.

---

## 📋 System & Build Information
* **Version Name:** `3.0.2`
* **Version Code / Build:** `22`
* **Minimum Android SDK:** API Level `24`
* **Target Android SDK:** API Level `36`
