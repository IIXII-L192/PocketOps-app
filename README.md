<!-- Banner Header -->
<p align="center">
  <img src="PocketOps.png?v=2.1.0" width="140" style="border-radius: 22%; box-shadow: 0px 8px 24px rgba(0,0,0,0.15);" alt="PocketOps Icon"/>
</p>

<h1 align="center">PocketOps</h1>

<p align="center">
  <strong>Stop fumbling. Start executing.</strong>
</p>

<p align="center">
  <a href="https://kotlinlang.org"><img src="https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?style=for-the-badge&logo=kotlin" alt="Kotlin"/></a>
  <a href="https://developer.android.com/jetpack/compose"><img src="https://img.shields.io/badge/Compose-M3%20Expressive-blue.svg?style=for-the-badge&logo=jetpackcompose" alt="Compose M3"/></a>
  <a href="https://developer.android.com"><img src="https://img.shields.io/badge/Android-SDK%2037-green.svg?style=for-the-badge&logo=android" alt="Android SDK"/></a>
  <a href="LICENSE"><img src="https://img.shields.io/badge/License-Custom%20Open%20Source-orange.svg?style=for-the-badge" alt="License"/></a>
</p>

<p align="center">
  PocketOps is a sleek, unified, offline-first utility dashboard for Android. Built with Jetpack Compose following Material 3 Expressive guidelines, it gives you instant micro-actions directly from your launcher screen, homescreen widget, or quick settings drawer.
</p>

---

## 📱 Screenshots

| Menu | Quick UPI | Quick UPI QR |
|:----:|:---------:|:------------:|
| <img src="Menu.png?v=2.1.0" width="200"/> | <img src="QuickUPI.png?v=2.1.0" width="200"/> | <img src="QuickUPIQR.png?v=2.1.0" width="200"/> |
| **Quick Chat** | **Quick Insta** | |
| <img src="QuickChat.png?v=2.1.0" width="200"/> | <img src="QuickInsta.png?v=2.1.0" width="200"/> | |

---

## 🗺️ Application Navigation Flow

```mermaid
graph TD
    %% Styling definitions
    classDef main fill:#d4ebf2,stroke:#1a5f7a,stroke-width:2px,color:#000;
    classDef green fill:#d1e7dd,stroke:#0f5132,stroke-width:2px,color:#000;
    classDef pink fill:#f8d7da,stroke:#842029,stroke-width:2px,color:#000;
    classDef settings fill:#e2e3e5,stroke:#41464b,stroke-width:2px,color:#000;
    
    Start([📱 App Launch]) --> Entry{Shortcut / QS Tile?}
    
    Entry -->|None| Dash[🏠 Dashboard Screen]:::main
    Entry -->|Quick Collect| Collect[⚡ UPI / PayPal Quick Collect]:::main
    Entry -->|Quick Chat| Chat[💬 WhatsApp Quick Chat]:::green
    Entry -->|Quick Insta| Insta[📸 Instagram Quick Insta]:::pink
    
    Dash -->|Select Tool| ToolSelect{Select Tool}
    ToolSelect -->|UPI / PayPal| Collect
    ToolSelect -->|WhatsApp| Chat
    ToolSelect -->|Instagram| Insta
    
    Collect -->|Toggle Payment Mode| Collect
    Collect -->|Manage IDs| Setup[⚙️ Manage Setup Screen]:::settings
    
    %% Navigation Back Stack Logic
    subgraph Navigation Stack [LIFO Backstack Navigation]
        Collect -->|Back Press| Dash
        Chat -->|Back Press| Dash
        Insta -->|Back Press| Dash
        Setup -->|Back Press| Collect
    end
```

---

## 🚀 Key Features

*   **⚡ Quick Collect (UPI / PayPal)**
    *   Generate dynamic or static payment QR codes locally.
    *   Smart recent-amount chips for fast inputs.
    *   **Auto-brightness boost** to 100% while showing the QR code for instant scanner detection (restores previous brightness on close).
    *   Supports saving, managing, and switching between multiple payment IDs.
*   **💬 Quick Chat**
    *   Start chat windows directly with any phone number without saving it to your contacts list.
*   **📸 Quick Insta**
    *   Type an Instagram username (auto-cleans leading `@` symbols) and open their profile directly in the official app or browser, or use Direct Search.
*   **⚙️ System Integrations**
    *   *Quick Settings Tile*: Access the PocketOps utility drawer instantly from anywhere.
    *   *Launcher Shortcuts*: Long-press the launcher icon to jump straight into UPI, Chat, or Instagram.

---

## 🎨 Expressive Theme System

PocketOps adapts contextually to your workflow. The entire interface shifts its color palette depending on which tool is active:
*   🟢 **Emerald Green Theme**: Activates when Quick Chat is selected.
*   🌸 **Expressive Pink Theme**: Activates when Quick Instagram is selected.
*   🔹 **Dynamic Blue Theme**: Activates when Quick UPI is selected.
*   🎨 **Device Theme-Aware**: Fully follows your system's Light/Dark mode settings.

---

## 🛠️ Tech Stack & Architecture

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Material 3 Expressive Design)
- **Data Persistence:** Jetpack DataStore (Preferences)
- **Widgets:** Jetpack Glance (Material 3 Widget support)
- **QR Generation:** Local offline generation via ZXing library
- **Routing:** Custom LIFO `navigationStack` backstack manager with state-aware toggle routing

---

## 🔧 Getting Started

1. Clone or open the project in **Android Studio**.
2. Select **`jbr-21`** (Bundled JDK 21) in your project structure settings.
3. Sync Gradle and run the app on your device.

---

## 📜 License Quick Reference

This project is licensed under the [PocketOps Custom Open Source License](LICENSE). It is source-available for personal use and inspection, but enforces strict distribution rights.

| Permissions | Requirements | Restrictions |
| :--- | :--- | :--- |
| 🟢 **Local Modification**<br>Modify, test, and use the code locally on any personal device. | 🟡 **Authorship Preservation**<br>Any permitted derivatives must retain original credits to Aakarsh (L192). | 🔴 **No Minor Redistribution**<br>Publishing clones with only minor changes (logo, name, packages) is strictly prohibited. |
| 🟢 **Original Share**<br>Redistribute original code using the same name, logo, and package ID. | 🟡 **Forced Open Source**<br>Permitted forks (≥30% changes) **must** remain open-source under this same license. | 🔴 **No Proprietary Derivatives**<br>You cannot close the source code of any modified versions. |

> [!IMPORTANT]
> To request custom exceptions or commercial licensing terms, you must request and obtain permission via email exclusively at **192aakarsh@gmail.com**.

*Maintained with ❤️ by **Aakarsh (L192)***.
