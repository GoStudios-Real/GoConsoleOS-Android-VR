# GoConsole VR

**GoStudios Cardboard VR Headset** — Stereoscopic VR gaming for Nokia G22/G21 and all Android devices.

![GoConsole VR](https://img.shields.io/badge/Version-1.0.0-blue) ![License](https://img.shields.io/badge/License-MIT-green) ![MinSDK](https://img.shields.io/badge/MinSDK-24-orange)

## GoStudios Cardboard VR Headset

No Google Cardboard SDK. Works with **any** cardboard or plastic VR headset holder.

### Headset Profiles

| Profile | IPD | FOV | Screen-to-Lens |
|---------|-----|-----|----------------|
| **GoStudios Cardboard** | 63mm | 90° | 40mm |
| **GoStudios Pro VR** | 63mm | 100° | 35mm |
| **GoStudios Lite** | 65mm | 80° | 45mm |
| Universal Cardboard | 64mm | 85° | 42mm |
| Custom | Adjustable | Adjustable | Adjustable |

### Calibration

- **IPD**: 40-80mm adjustable (matches your eye spacing)
- **Barrel Distortion**: Automatic lens correction
- **Per-device**: Auto-detects screen size and density

## Features

- Stereoscopic split-screen rendering (OpenGL ES 2.0)
- Gyroscope + Accelerometer head tracking
- Rotation Vector sensor fusion with complementary filter
- 5 built-in VR games
- Full Bluetooth controller support
- Blue #0066FF GoStudios theme

## Supported Devices

### Phones
- Nokia G22, G21, G20, G10, G50
- Samsung Galaxy S/A series
- Google Pixel
- OnePlus
- Any Android 7.0+ device with gyroscope

### Controllers
- Xbox One / Series X|S
- PS4 DualShock 4
- PS5 DualSense
- Nintendo Switch Pro Controller
- Nokia MD-11
- Any generic Bluetooth gamepad

## Built-in Games

| Game | Description | Controls |
|------|-------------|----------|
| VR Pong | Classic paddle vs AI | Left stick = move paddle |
| VR Snake | 3D grid snake | Left stick = direction |
| VR Space Shooter | Shoot aliens in space | Stick = move, A = shoot |
| VR Jigsaw | 3D puzzle assembly | Stick = move piece, A = snap |
| VR Basketball | Throw at the hoop | Stick = aim, Trigger = throw |

## Controls

| Input | Action |
|-------|--------|
| Head | Look around (gyroscope) |
| Left Stick | Move / Aim |
| A Button | Select / Shoot |
| B Button | Back |
| X Button | Secondary action |
| Y Button | Tertiary action |
| D-Pad | Navigate menus |
| Start | Pause |
| Select | Reset |
| Right Trigger | Throw / Power |
| Left Trigger | Alt action |

## Installation

1. Download govr-v1.0.0.apk
2. Enable "Install from unknown sources" on your phone
3. Install the APK
4. Pair your Bluetooth controller in Android Settings
5. Launch GoConsole VR
6. Select your headset profile
7. Put your phone in any cardboard VR headset
8. Play!

## Build from Source

`ash
git clone https://github.com/GoStudios-Real/GoConsoleOS-Android-VR.git
cd GoConsoleOS-Android-VR
./gradlew assembleDebug
`

APK output: pp/build/outputs/apk/debug/app-debug.apk

## Project Structure

`
app/src/main/java/com/gostudios/console/vr/
├── MainActivity.kt          # Setup screen with headset config
├── VRActivity.kt            # VR game menu (3D)
├── engine/
│   ├── VREngine.kt          # OpenGL ES stereoscopic renderer
│   └── HeadTracker.kt       # Gyroscope + accelerometer tracking
├── controller/
│   └── BluetoothController.kt  # Bluetooth gamepad input
├── headset/
│   └── GoStudiosHeadset.kt  # VR headset calibration profiles
├── games/
│   ├── VRGame.kt            # Base game class
│   ├── VRPong.kt            # Pong
│   ├── VRSnake.kt           # Snake
│   ├── VRSpaceShooter.kt    # Space shooter
│   ├── VRJigsaw.kt          # Jigsaw puzzle
│   ├── VRBasketball.kt      # Basketball
│   └── GameActivity.kt      # Game host activity
└── ui/
    └── VRUIRenderer.kt      # 3D UI overlay
`

## Tech Stack

- Kotlin
- OpenGL ES 2.0
- Android Sensor API (Gyroscope, Accelerometer, Rotation Vector)
- Android InputDevice API (Bluetooth controllers)
- Material Components

## License

MIT License — GoStudios Corporation

## Links

- [GitHub](https://github.com/GoStudios-Real/GoConsoleOS-Android-VR)
- [GoConsoleOS](https://github.com/GoStudios-Real/GoConsoleOS)
- [GoConsoleOS Android](https://github.com/GoStudios-Real/GoConsoleOS-Android)
- [GoConsoleOS Web](https://github.com/GoStudios-Real/GoConsoleOS-Web)
