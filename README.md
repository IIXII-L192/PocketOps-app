<div align="center">
  <img src="PocketOps.png?v=2.1.0" width="160" style="border-radius: 24%; object-fit: fill; background: transparent;" alt="PocketOps Icon"/>
  
  # PocketOps ⚡

  [![Kotlin](https://img.shields.io/badge/kotlin-2.0.21-purple.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
  [![Compose](https://img.shields.io/badge/compose-M3-blue.svg?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
  [![Android](https://img.shields.io/badge/Android-SDK%2037-green.svg?style=flat-square&logo=android)](https://developer.android.com)
  [![License](https://img.shields.io/badge/license-MIT-blue.svg?style=flat-square)](LICENSE)

  **Stop fumbling. Start executing.**

  PocketOps is a sleek, unified, offline-first utility dashboard for Android. Built with Material 3 Expressive guidelines, it gives you instant micro-actions directly from your launcher screen or quick settings drawer: instantly generate UPI payment QR codes with automatic screen brightness boost, launch direct WhatsApp chats without saving numbers to your contacts list, and open Instagram profiles directly by username.
</div>

---

## 📱 Screenshots

| Menu | Quick UPI | Quick UPI QR |
|:----:|:---------:|:------------:|
| <img src="Menu.png?v=2.1.0" width="200"/> | <img src="QuickUPI.png?v=2.1.0" width="200"/> | <img src="QuickUPIQR.png?v=2.1.0" width="200"/> |
| **Quick Chat** | **Quick Insta** | |
| <img src="QuickChat.png?v=2.1.0" width="200"/> | <img src="QuickInsta.png?v=2.1.0" width="200"/> | |

---

## 🚀 Key Features

*   **⚡ Quick UPI (UPI Feature)**: 
    *   Generate dynamic or static payment QR codes locally.
    *   Smart recent-amount chips for fast inputs.
    *   Auto-brightness boost to 100% while showing the QR code for instant scanner detection (restores previous brightness on close).
    *   Supports saving, managing, and switching between multiple UPI IDs.
*   **💬 Quick Chat**: 
    *   Start chat windows directly with any phone number without saving it to your contacts list.
*   **📸 Quick Insta**: 
    *   Type an Instagram username (auto-cleans leading `@` symbols) and open their profile directly in the official app or browser, or use Direct Search.
*   **⚙️ System Integrations**:
    *   *Quick Settings Tile*: Pull down from the notification shade to access the PocketOps utility drawer instantly from anywhere.
    *   *Homescreen Launcher Shortcuts*: Long-press the launcher icon to jump straight into UPI, Chat, or Instagram.

---

## 🎨 Expressive Theme System

PocketOps is built to adapt dynamically to your workflow. The entire app adjusts its color palette contextually depending on which tool is active:
*   🟢 **Green Theme**: Activates when Quick Chat is selected.
*   🌸 **Pink Theme**: Activates when Quick Instagram is selected.
*   🔹 **Blue Theme**: Activates when Quick UPI is selected.
*   🎨 **Device Theme-Aware**: Fully follows your system's Light/Dark mode settings.

---

## 🛠️ Tech Stack

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose (Material 3 Expressive Design)
*   **Data Storage**: Jetpack DataStore (Preferences)
*   **Widgets**: Jetpack Glance (Material 3 Widget support)
*   **QR Generation**: Local offline generation via ZXing

---

## 🔧 Getting Started

1.  Clone or open the project in **Android Studio**.
2.  Select **`jbr-21`** (Bundled JDK 21) in settings.
3.  Sync Gradle and run the app on your device.

---

## 👨‍💻 Developer & License
*   Maintained by **Aakarsh (L192)**.
*   Licensed under the [MIT License](LICENSE).
