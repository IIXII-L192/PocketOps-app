# PocketOps v3.0.2 - Release Notes

PocketOps v3.0.2 focuses on improving **QuickClip** reliability and updating system configurations.

## 🚀 Improvements & Optimizations

### 1. Instant Clipboard Capture (QuickClip)
* **Real-time Capture**: Re-architected the clipboard change detection engine to extract copied text and image URIs synchronously on the main thread immediately when a system clipboard change is broadcast.
* **Enhanced Reliability**: Resolved an issue where delayed background checks could result in the application losing window focus before reading the clipboard, which previously caused the system to block the read operation. This ensures 100% reliable and instant clip recording.
* **Efficient Persistence**: Offloaded pause verification and disk writing to lifecycle-aware background coroutines *after* retrieving the clip data, keeping the main thread completely responsive.

---

## 📋 System & Build Information
* **Version Name:** `3.0.2`
* **Version Code / Build:** `22`
* **Minimum Android SDK:** API Level `24`
* **Target Android SDK:** API Level `36`
