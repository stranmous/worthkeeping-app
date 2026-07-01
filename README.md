<div align="center">
  <!-- Place your logo or banner image here -->
  <img src="<!-- INSERT_BANNER_IMAGE_URL_HERE -->" alt="WorthKeeping Banner" width="100%">

  # WorthKeeping

  **An on-device Android application to organize, clean, and back up your photo gallery.**

  [![Android Platform](https://img.shields.io/badge/Platform-Android_12+-3DDC84?logo=android&logoColor=white)](#)
  [![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-7F52FF?logo=kotlin&logoColor=white)](#)
  [![License](https://img.shields.io/badge/License-Proprietary-blue.svg)](#)

</div>

---

WorthKeeping helps you sort and back up your photos locally. Instead of relying on cloud services for analysis, the app uses on-device Machine Learning (Google ML Kit) to categorize images into "Keepers" and "Clutter". Your photos remain on your device and are never uploaded to a remote server for processing.

---

## Features

### 1. Local Scanning
The app analyzes your gallery using standard Android MediaStore APIs. It provides an estimate of your gallery's size and the time required for a full scan.

<!-- Place your Scan Estimate / Progress screenshot here -->
<div align="center">
  <img src="<!-- INSERT_SCAN_SCREENSHOT_URL_HERE -->" alt="Scan Screen" width="300">
</div>

### 2. Automated Categorization
WorthKeeping uses ML Kit to help filter images:
- **Face Detection:** Camera photos containing people are automatically marked as Keepers.
- **Text Recognition (OCR):** Sensitive documents, receipts, and IDs are detected and flagged for manual review to prevent accidental deletion.

<!-- Place your Results Summary / Review screenshot here -->
<div align="center">
  <img src="<!-- INSERT_RESULTS_SCREENSHOT_URL_HERE -->" alt="Results Summary" width="300">
</div>

### 3. Folder Protection and Preferences
Folders with names like "Important" or "Do not delete" are automatically excluded from the clutter list. You can also specify exact folders to be skipped during scans.

<!-- Place your Scan Preferences screenshot here -->
<div align="center">
  <img src="<!-- INSERT_PREFERENCES_SCREENSHOT_URL_HERE -->" alt="Scan Preferences" width="300">
</div>

### 4. Export Options
Once your keepers are identified, you can export them to a secure location:
- **Local Storage:** Copies the organized files to a directory on your device. You can specify the destination folder name.
- **Google Drive Backup:** Connects to Google Drive using the restricted `drive.file` scope. The app only has access to the specific backup folder it creates.

<!-- Place your Export / Backup screenshot here -->
<div align="center">
  <img src="<!-- INSERT_BACKUP_SCREENSHOT_URL_HERE -->" alt="Backup Options" width="300">
</div>

### 5. Safe Deletion
The app does not delete files automatically. Any items marked as clutter must be manually reviewed and confirmed before they are moved to the Android system trash.

<!-- Place your Clean / Trash screenshot here -->
<div align="center">
  <img src="<!-- INSERT_CLEAN_SCREENSHOT_URL_HERE -->" alt="Safe Clean Up" width="300">
</div>

---

## Tech Stack

- **Platform:** Android 12+ (Min SDK 31, Target SDK 35)
- **Language:** Kotlin 
- **UI Framework:** Jetpack Compose (Material 3)
- **Architecture:** MVVM (Model-View-ViewModel) using Coroutines & Flows
- **Database:** Room (Local SQLite) & Jetpack DataStore
- **Image Loading:** Coil
- **On-Device ML:** Google ML Kit (Face Detection & Text Recognition)
- **Cloud Integration:** Google Drive REST API v3, Supabase (for anonymous feedback)

---

## Installation

You can install the app using the APK provided in the repository.

1. Download the latest `app-release.apk` from the [Releases](https://github.com/) page.
2. Open the APK on your Android device.
3. If prompted, allow your file manager to "Install unknown apps."
4. Complete the installation and open the app.

---

## Building from Source

To compile the application locally:

### Prerequisites
- Android Studio (Koala or newer)
- JDK 17
- A Google Cloud Console project (for Google Drive OAuth)
- A Supabase project (for Feedback)

### Setup Instructions

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/worthkeeping-app.git
   cd worthkeeping-app
   ```

2. **Configure Environment Variables:**
   API keys are excluded from version control. Create a `local.properties` file in the root directory and add your keys:
   
   ```properties
   SUPABASE_URL=your_supabase_url_here
   SUPABASE_ANON_KEY=your_supabase_anon_key_here
   ```
   *(Note: The `google-services.json` file is not required because the app uses standard OAuth for Google Drive).*

3. **Build the App:**
   Open the project in Android Studio, sync Gradle, and run the app, or build via the command line:
   ```bash
   gradle installDebug
   ```

---

## Privacy & Security

**Privacy Policy:** The full privacy policy is available here: [WorthKeeping Privacy Policy](http://waqasai.me/worthkeeping-app/)

---

## License

All Rights Reserved. This project and its source code are proprietary.
