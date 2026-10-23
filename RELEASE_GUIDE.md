# JARVIS AI — Release & APK Build Guide

## 1. Quick Overview

- **Release Tag:** `v1.0.0`
- **Release Title:** `JARVIS AI v1.0.0`
- **Target Asset Filename:** `JARVIS-AI-v1.0.0.apk`
- **Gradle Module:** `:app`
- **Generated Local Path:** `app/build/outputs/apk/debug/app-debug.apk`

---

## 2. Building the APK Locally / In Cloud

### Option A: Direct Gradle Build (CLI)
From the repository root directory, run:

```bash
# On Linux / macOS:
./gradlew :app:assembleDebug

# In environments where gradle is pre-installed (e.g. AI Studio container):
gradle :app:assembleDebug
```

Once the build finishes successfully (`BUILD SUCCESSFUL`), the output APK is generated at:
```
app/build/outputs/apk/debug/app-debug.apk
```

Rename or copy it to the release asset name:
```bash
cp app/build/outputs/apk/debug/app-debug.apk JARVIS-AI-v1.0.0.apk
```

### Option B: Google AI Studio Export
1. In the AI Studio top navigation bar, open the project menu.
2. Select **Export Project** or **Download APK/AAB**.
3. You can also right-click `app/build/outputs/apk/debug/app-debug.apk` in the file explorer to download it directly.

---

## 3. Publishing to GitHub Releases

### Method 1: GitHub Web Interface
1. Push your repository code to GitHub.
2. Navigate to your repository page: `https://github.com/<YOUR_USERNAME>/<YOUR_REPO>`.
3. In the right sidebar, click **Releases** (or navigate to `https://github.com/<YOUR_USERNAME>/<YOUR_REPO>/releases`).
4. Click **Draft a new release**.
5. Fill in the release details:
   - **Choose a tag:** Type `v1.0.0` and click **Create new tag: v1.0.0 on publish**.
   - **Target:** `main` (or default branch).
   - **Release title:** `JARVIS AI v1.0.0`
   - **Description:** Describe changes (e.g. Silent listening, Gemini API integration, WhatsApp voice messaging, lock-screen wake).
6. Under **Attach binaries by dropping them here or selecting them**, upload:
   - `JARVIS-AI-v1.0.0.apk` (copied from `app/build/outputs/apk/debug/app-debug.apk`).
7. Click **Publish release**.

### Method 2: GitHub CLI (`gh`)
If you have the GitHub CLI installed and authenticated:

```bash
# 1. Build the APK
./gradlew :app:assembleDebug

# 2. Copy to release asset name
cp app/build/outputs/apk/debug/app-debug.apk JARVIS-AI-v1.0.0.apk

# 3. Create release and attach asset
gh release create v1.0.0 JARVIS-AI-v1.0.0.apk \
  --title "JARVIS AI v1.0.0" \
  --notes "Initial v1.0.0 release with silent listening, WhatsApp voice commands, Gemini AI brain, and device controls."
```

### Method 3: Automated via GitHub Actions
The repository includes `.github/workflows/android-build-release.yml`. When you push a git tag:

```bash
git tag v1.0.0
git push origin v1.0.0
```

GitHub Actions will automatically:
1. Set up Java 17 and Gradle.
2. Compile and package the Android APK (`:app:assembleDebug`).
3. Upload the APK as a workflow artifact named `JARVIS-AI-v1.0.0-debug-apk`.
4. Automatically create the GitHub Release `JARVIS AI v1.0.0` with asset `JARVIS-AI-v1.0.0.apk` attached.

---

## 4. Security & Signing Distinction

| Build Type | Keystore | Intended Use | Notes |
|---|---|---|---|
| **Debug APK** | `debug.keystore` | Testing, sideloading, emulator verification | Sideloadable on any Android device with Developer Mode / Unknown Sources enabled. |
| **Release APK / AAB** | Secure Upload Keystore (`my-upload-key.jks`) | Google Play Store distribution | Requires `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` secrets. |

> **IMPORTANT:** Never commit production keystore files or passwords to public GitHub repositories. Use GitHub Actions Secrets (`SECRETS.KEYSTORE_BASE64`, etc.) if building signed release bundles.
