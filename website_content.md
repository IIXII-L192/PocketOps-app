# PocketOps Promotional Website Content Specifications

This document defines the structured content, messaging, and feature breakdown for the PocketOps promotional website.

---

## 🌐 URL Configuration
* **Primary URL Target**: `https://iixii-l192.github.io/PocketOps-app` (GitHub Pages hosting matching the repository namespace)
* **GitHub Repository Link**: `https://github.com/IIXII-L192/PocketOps-app`

---

## ⚡ Hero Section
* **Main Headline**: `Instant Actions. Zero Bloat.`
* **Sub-headline**: `PocketOps is a lightweight, offline-first dashboard for Android that puts micro-utilities directly in your notification drawer. Launch direct WhatsApp chats, generate instant UPI payment QRs, and lookup Instagram profiles in one tap.`
* **Call to Actions (CTAs)**:
  * Primary Button: `Download APK (v2.0.0)`
  * Secondary Button: `View Source on GitHub`

---

## 📦 App Overview & Technical Details
* **App Name**: PocketOps
* **Package Identifier**: `l192.aakarsh.pocketops`
* **Version**: `2.0.0`
* **Size**: ~10 MB
* **Requirements**: Android 8.0+ (API Level 24+)
* **License**: Open Source

---

## 🚀 Core App Functionalities

### 1. Quick UPI (Local QR Code Generator)
* **What it does**: Generate dynamic payment QRs offline in seconds.
* **Key Features**:
  * **100% Offline**: QR generation runs locally (uses ZXing) for absolute privacy.
  * **Auto-Brightness Boost**: Boosts display brightness to 100% when showing the QR code for instant scan reliability; restores original level on dismissal.
  * **VPA Profile Manager**: Save and switch between multiple UPI IDs/VPAs on the fly.
  * **Recent Amounts Chips**: One-tap shortcut chips to quickly re-use common transaction amounts.
  * **Polished Sharing Card**: Share a clean, custom-drawn canvas image containing the QR code, amount, and payment details without the app label.

### 2. Quick WhatsApp (Direct Messaging)
* **What it does**: Text phone numbers directly without adding them to your device contacts.
* **Key Features**:
  * **Contact-Free Chatting**: Cleans up temporary chat clutter in your phonebook.
  * **Automatic Sanitization**: Auto-formats numbers (handles country codes, spaces, and formatting characters automatically).

### 3. Quick Instagram (Direct Profile Search)
* **What it does**: Open any profile instantly using only their username.
* **Key Features**:
  * **Deep Linking**: Attempts to open the profile inside the official Instagram app first.
  * **Smart Fallback**: Gracefully falls back to web browser views if the app is missing.
  * **Prefix Stripping**: Automatically strips leading `@` symbols.

### 4. Quick Settings OS Tile Integration
* **What it does**: Access the utility dashboard over any active app.
* **Key Features**:
  * **Drawer Toggle**: Add the quick tile in your notification drawer to trigger the utility overlay immediately from anywhere.

---

## 🛡️ Value Proposition (Why PocketOps?)
* **Private & Secure**: No trackers, no server uploads, no network requirement for QR generation.
* **Adaptive Styling**: Seamlessly transitions the color accent to match the active utility (UPI Blue, WhatsApp Green, Instagram Pink).
* **Super Lightweight**: Built on Jetpack Compose with a compact footprint, launching inside a dialog theme overlay.

---

## 🤝 Creator Footer
* **Creator Credits**: `"Developed with 💖 by Aakarsh (L192)"`
* **Support Links**:
  * **GitHub Sponsors**: `https://github.com/sponsors/IIXII-L192`
  * **Direct Support**: UPI Pay (`anshujaat@nyes`)
