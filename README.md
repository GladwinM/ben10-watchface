# Ben 10 – Omnitrix Watch Face
A Samsung Galaxy Watch 4 Classic watch face inspired by the Omnitrix from Ben 10.

## Features
- **Omnitrix green** glowing analog clock hands
- **Hex-grid** background (like alien tech)
- **Omnitrix hourglass** symbol at the centre
- **Green outer ring** with glowing tick marks
- **Orange second hand** (interactive mode only)
- **Date display** in monospace green font
- **Ambient mode** – simplified grey palette to save battery

---

## Requirements
| Tool | Version |
|------|---------|
| Android Studio | Hedgehog (2023.1.1) or newer |
| Android SDK | API 34 |
| Wear OS SDK | API 26+ |
| JDK | 17 |

---

## How to Build & Install

### 1. Open the project
Open **Android Studio** → *Open* → select the `ben 10` folder.

### 2. Let Gradle sync
Android Studio will download all dependencies automatically. Wait for the sync to finish.

### 3. Enable Developer Mode on your watch
On the **Galaxy Watch 4 Classic**:
1. Go to **Settings → About watch → Software**
2. Tap **Software version** 5 times until *Developer mode* appears
3. Go to **Settings → Developer options → ADB debugging** → ON
4. Enable **Wireless debugging** (or connect via USB with a USB-C OTG adapter)

### 4. Pair the watch with ADB
```bash
# Connect to your Galaxy Watch 4 Classic
adb connect 192.168.1.11:45647
adb devices   # confirm watch appears
```

### 5. Run / Install
In Android Studio, select your watch from the device dropdown and click **▶ Run** (Shift+F10).

Or build a release APK:
```
Build → Generate Signed Bundle/APK → APK
```
Then push it manually:
```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

### 6. Apply the watch face
On the watch, go to **Watch faces** (long-press the dial) → swipe to *Ben 10 Watch Face* → tap to apply.

---

## Project Structure
```
app/src/main/
├── java/com/ben10/watchface/
│   └── Ben10WatchFaceService.kt   ← All watch face drawing logic
├── res/
│   ├── drawable/preview_circular.xml
│   ├── mipmap-hdpi/ic_launcher.xml
│   ├── values/colors.xml
│   ├── values/strings.xml
│   └── xml/watch_face.xml
└── AndroidManifest.xml
```

---

## Customisation Tips
| What to change | Where |
|----------------|-------|
| Colours | `clrGreen`, `clrOrange` etc. at the top of `Ben10WatchFaceService.kt` |
| Hand thickness | `strokeWidth` in `initPaints()` |
| Glow intensity | `BlurMaskFilter` radius values |
| Date position | `cy + radius * 0.57f` in `drawDate()` |
