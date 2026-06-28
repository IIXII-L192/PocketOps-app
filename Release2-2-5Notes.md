# PocketOps v2.2.5 - Release Notes

PocketOps v2.2.5 brings enhanced navigation physics, custom branding accents, and optimized interface routing logic.

## 🚀 What's New

### 1. Sequential Backstack Navigation
* Rewrote state management to use a formal Compose list-based LIFO backstack (`navigationStack`).
* Clicking the top-left back arrow now sequentially steps back through your screen history (e.g. Dashboard ↔ Enter Amount ↔ Setup / Chat / Settings) instead of closing immediately.

### 2. Intelligent Payment Mode Toggles
* **Setup Screen**: Toggling the top-right payment switch now updates the manage page in-place, keeping you on the respective Setup screen (UPI Manage ↔ PayPal Manage).
* **Quick Collect**: Toggling on the amount filler or QR display pages switches cleanly between payment modes (UPI Quick Collect ↔ PayPal Quick Collect).
* **Reset Rules**: Any mode change dynamically clears historical screens to prevent back-looping, ensuring the next back click returns you directly to the Main Menu.

### 3. Vector Accents & Space Optimization
* Rendered a neat, bold vector-based brand badge (`I I X I I`) directly below the **Support Me** button.
* Adjusted bottom dialog padding from `24dp` to `10dp` to balance spacing and place the badge equidistantly from the button and the bottom edge of the pane.
* The badge automatically inherits your current theme's primary color (e.g. monochrome, light/dark, or Material You dynamic colors).

### 4. Snappy GPU Transitions
* Reverted custom physics to Compose's native transition scheme, enabling instantaneous, lag-free transitions without ghosting, sweeping delays, or flickering.

---

*Compiled APK Package:* [PocketOps-v2.2.5.apk](file:///c:/Users/Aakarsh/AndroidStudioProjects/PocketOps/release/PocketOps-v2.2.5.apk)
*Auto-update Config:* [update.json](file:///c:/Users/Aakarsh/AndroidStudioProjects/PocketOps/update.json)
