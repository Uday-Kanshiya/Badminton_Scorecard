# 🏸 Badminton Scorecard Pro

[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.0-blue.svg?logo=kotlin)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-brightgreen.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-success.svg?logo=android)](https://www.android.com)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM-orange.svg)](https://developer.android.com/topic/architecture)
[![Database](https://img.shields.io/badge/Database-Room%20SQLite%20%2B%20Flow-red.svg)](https://developer.android.com/training/data-storage/room)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](LICENSE)

A modern, tournament-grade digital badminton scorecard and umpire assistant app built with **Jetpack Compose**, **Material 3**, and **Clean MVVM Architecture**. 

Designed for casual players, club tournaments, and professional umpires, featuring official **BWF (Badminton World Federation) rules**, an interactive **court visualizer**, **Text-to-Speech (TTS) voice announcements**, **career analytics graphs**, **high-res image export**, and a **refreshing pastel theme**.

---

## 📥 Download APK

You can install the ready-to-use APK directly onto your Android device:

👉 **[Download BadmintonScorecard-v1.2.apk](apk/BadmintonScorecard-v1.2.apk)** *(~25.7 MB)*

> **Installation Tip:** After downloading the .apk on your phone, open it and tap **Install**. If prompted, enable *"Install unknown apps"* for your browser or file manager.

---

## ✨ Features

### 🏸 1. Official BWF Rules Engine
- **Singles & Doubles**: Full support for both formats with strict adherence to BWF court positioning rules.
- **Odd / Even Service Parity**:
  - **Even Score (0, 2, 4...)**: Server serves from the **Right** court; receiver receives in the diagonally opposite **Right** court.
  - **Odd Score (1, 3, 5...)**: Server serves from the **Left** court; receiver receives in the diagonally opposite **Left** court.
- **Doubles Rotation**: Automatically tracks server/receiver partner switches and retains non-serving partner positions until service breaks.
- **Deuce & Sudden Death**: Standard 2-point lead required after 20-20, capped at 30 points max.
- **7-0 Skunk Rule**: Optional Mercy Rule allowing instant set victory on a dominant 7-0 sweep.
- **Full Undo / Redo**: Step backward or forward through any point without corrupting rotation or score integrity.

### 🏟️ 2. Interactive Live Court Visualizer
- **Tournament Green Court (#135A31)**: Authentic court layout with crisp boundary lines, center line, and net divider.
- **Dynamic Server & Receiver Highlight**: Glowing cyan border and badges highlighting the active server and receiver in real time.
- **Smart Singles View**: Unoccupied service courts remain clean with only the active player placed in their serving/receiving box.
- **Quick Court Controls**:
  - ⇄ **Position Swapper**: Swap player positions in doubles if an incorrect lineup was set.
  - ⇅ **Serve Switcher**: Quick toggle serving side before the rally begins.
  - 🔄 **Court Ends Flip**: Switch ends at interval (11 points) or between sets.

### 🗣️ 3. Virtual Umpire Voice & Speech
- **Live Text-to-Speech (TTS)**: Professional verbal announcements of the score before each serve (e.g., *"Love all, play"*, *"2 serving 1"*, *"Game point Team A"*, *"Service over"*).
- **On-Screen Umpire Pill**: Toggleable speech bubble displaying the exact umpire callout text.

### 📊 4. Deep Analytics & Statistics
- **Match Summary Screen**:
  - Score progression graph tracking point-by-point flow.
  - Momentum swing timeline illustrating scoring runs and dominance shifts.
  - Point attribution chart breaking down points won on serve vs return.
- **Player Profiles & Career Records**:
  - Win/Loss outcome donut chart.
  - Serve vs Return point attribution breakdown.
  - Win rate momentum and career progression curve.
  - Doubles partnership chemistry rankings (win rates with medals 🥇, 🥈, 🥉).
- **Activity & MVP Dashboard**:
  - Responsive **Matches Over Time** bar chart with daily match counts and date labels.
  - **Real MVP Calculation**: Computes the Most Valuable Player based on total wins weighted by win rate percentage.

### 📤 5. High-Resolution Graphic Export
- **One-Click Image Sharing**: Generates crisp, branded PNG graphic summary cards including all graphs, set scores, player names, and winner banners.
- **Instant Social Share**: Direct sharing to WhatsApp, Instagram, Telegram, Twitter, or save directly to device storage.

### 🎨 6. Modern Pastel Light & OLED Dark Themes
- **Pastel Light Theme (Zero Harsh Stark White)**:
  - 🌿 **Soft Court Mint-Sage Canvas (#EFF5F2)**: Soft, easy on the eyes.
  - 💧 **Athletic Sky Blue (#E1F5FE, border #81D4FA)**: Applied to Team A lineup cards, overview metrics, and court controls.
  - 🍃 **Soft Mint Green (#E0F2E9, border #A5D6A7)**: Applied to Team B lineup cards, momentum charts, and won/loss donut.
  - 🏆 **Warm Golden Yellow (#FFF9C4, border #FFD54F)**: Applied to Winner banners, MVP card, and match rules.
  - ⚡ **Sleek Silver & Platinum (#ECEFF1, border #B0BEC5)**: Applied to Bottom navigation, timer pills, and match cards.
- **OLED Dark Theme**: High-contrast, battery-friendly dark aesthetic with vibrant team badges and court illumination.

### ☁️ 7. Google Cloud Backup & Offline-First Ownership
- **Google Cloud Backup**: Link Google account to sync and restore match records across devices.
- **JSON Data Export**: Download an offline JSON file of all players, matches, and stats for 100% data portability.
- **Player Management**: Add, edit player names and nicknames, and safely manage roster with deletion confirmation.

---

## 📱 App Navigation & Architecture

`mermaid
graph TD
    A[Home Screen] -->|New Match| B[Match Setup Screen]
    A -->|Roster| C[Player List Screen]
    A -->|History| D[Match History Screen]
    A -->|Analytics| E[Statistics Dashboard]
    A -->|Settings| F[Settings & Cloud Sync]

    B -->|Start Scoring| G[Live Match Scoreboard]
    G -->|Match Finish| H[Match Summary Screen]
    H -->|Share Image| I[Android Share Sheet]

    C -->|Select Player| J[Player Profile & Career Stats]
    D -->|Select Match| H
    E -->|View Chemistry| K[Doubles Partnership Analysis]
`

### Architecture Highlights
- **Clean Architecture & MVVM**: Unidirectional data flow (UDF) using Kotlin StateFlow and Coroutines.
- **Jetpack Compose & Material 3**: 100% declarative UI with fluid animations, adaptive cards, and responsive layouts.
- **Room Database**: Offline-first persistence with reactive DAO queries observing database mutations in real time.
- **Dagger Hilt**: Robust, clean dependency injection across ViewModels, Repositories, and DAOs.

---

## 🏸 BWF Service Court Parity Reference

| Score Parity | Server Court (Perspective looking at net) | Receiver Court (Perspective looking at net) |
| :--- | :--- | :--- |
| **Even (0, 2, 4, 6...)** | **Right Court** | **Right Court** *(Diagonally opposite)* |
| **Odd (1, 3, 5, 7...)** | **Left Court** | **Left Court** *(Diagonally opposite)* |

*In Singles, the player appears only in their active service court while the unoccupied court remains clean and uncluttered.*

---

## 🛠️ Tech Stack & Libraries

- **Language:** [Kotlin 2.0](https://kotlinlang.org/)
- **UI Toolkit:** [Jetpack Compose](https://developer.android.com/jetpack/compose) with [Material 3](https://m3.material.io/)
- **DI:** [Dagger Hilt 2.51](https://dagger.dev/hilt/)
- **Database:** [Room 2.6](https://developer.android.com/training/data-storage/room) (SQLite + Flow)
- **Asynchronous:** [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html) & [StateFlow](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-core/kotlinx.coroutines.flow/-state-flow/)
- **Audio / Speech:** [Android TextToSpeech (TTS)](https://developer.android.com/reference/android/speech/tts/TextToSpeech)
- **Graphics & Rendering:** Compose Canvas + Android GraphicsLayer bitmap capture
- **Min SDK:** 26 (Android 8.0 Oreo) • **Target SDK:** 34 (Android 14)

---

## 🚀 Building From Source

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17 or JDK 21 (bundled with Android Studio JBR)
- Android SDK 34

### Steps
1. **Clone the repository:**
   `ash
   git clone https://github.com/Uday-Kanshiya/Badminton_Scorecard.git
   cd Badminton_Scorecard
   `

2. **Open in Android Studio** or build directly using the Gradle wrapper:
   `ash
   # On Windows PowerShell
   .\gradlew.bat assembleDebug

   # On macOS / Linux
   ./gradlew assembleDebug
   `

3. **Locate the APK:**
   The generated APK will be available at:
   `
   app/build/outputs/apk/debug/app-debug.apk
   `
   Or inside the repository's pk/ directory:
   `
   apk/BadmintonScorecard-v1.2.apk
   `

---

## 📄 License

This project is licensed under the [MIT License](LICENSE) — feel free to use, modify, and distribute it freely.

---

*Made with ❤️ for the global badminton community by [Uday Kanshiya](https://github.com/Uday-Kanshiya).*
