# BluetoothChat

**BluetoothChat** is a native Android peer-to-peer (P2P) messaging application built with modern **Jetpack Compose** and **Kotlin Coroutines**. It enables real-time, offline text messaging between two Android devices using Classic Bluetooth RFCOMM sockets—no internet connection or central server required.

---

## Features

- **Bluetooth Device Discovery**: Automatically scans and detects available nearby Classic Bluetooth devices.
- **P2P Connection Management**: Connect to paired devices or request pairing (bonding) directly from within the app interface.
- **Dual Server/Client Mode**: Operates as both an RFCOMM server listener and a client connection initiator for true peer-to-peer communication.
- **Real-time Messaging**: Instant bidirectional message transmission powered by Kotlin Coroutines and asynchronous Kotlin `Flow` streams.
- **Local Chat History Caching**: Retains chat history locally per device using JSON serialization and `SharedPreferences`, preserving conversations across app restarts.
- **Modern Jetpack Compose UI**: Designed with Material 3, dynamic edge-to-edge layout support, and smooth keyboard/IME handling.
- **Android 12+ Permission Handling**: Fully compliant with runtime Bluetooth permissions for API level 31+ (`BLUETOOTH_SCAN`, `BLUETOOTH_CONNECT`, `BLUETOOTH_ADVERTISE`) and legacy location permissions for API 30 and below.

---

## Architecture & Tech Stack

This project follows modern Android development best practices with **MVVM (Model-View-ViewModel)** architecture and **Unidirectional Data Flow (UDF)**.

### Tech Stack
- **Language**: Kotlin (v2.2.10)
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture**: MVVM, StateFlow, ViewModel
- **Min SDK**: 24 (Android 7.0)
- **Target & Compile SDK**: 37 (Android 16)

### Project Structure

```
br.com.emanueldias.bluetoothchat/
├── data/
│   └── bluetooth/
│       ├── BluetoothScanner.kt    # Bluetooth scanning, bonding, sockets & communication
│       ├── ChatCache.kt           # Local JSON-based message caching
│       └── ScanResult.kt          # Bluetooth scan state data model
├── domain/
│   ├── ChatMessage.kt             # Chat message domain model
│   └── Device.kt                  # Bluetooth device domain model
└── ui/
    ├── presentation/
    │   ├── components/            # Reusable UI components (AppBars, ChatBubbles, Device Items)
    │   ├── ChatScreen.kt          # Active chat view
    │   ├── DeviceList.kt          # Device scan & pair list view
    │   ├── DeviceListUiState.kt   # UI state representation
    │   └── DeviceViewModel.kt     # ViewModel orchestrating UI state & Bluetooth scanner
    └── theme/                     # Material 3 Color, Type, Theme configuration
```

---

## How Bluetooth Communication Works

1. **Service UUID**: Standard Serial Port Profile (SPP) UUID:
   `00001101-0000-1000-8000-00805F9B34FB`
2. **Server Listening Thread**: The app initializes a `BluetoothServerSocket` via `listenUsingRfcommWithServiceRecord` to passively listen for incoming P2P connections.
3. **Client Socket Connection**: When connecting to a selected device, the app creates a `BluetoothSocket` using `createRfcommSocketToServiceRecord` and initiates the connection handshake on an I/O dispatcher.
4. **Data Stream Processing**: Connected sockets stream UTF-8 byte arrays back and forth via continuous input stream buffer reads wrapped inside a Coroutine `callbackFlow`.

---

## Permissions & Compatibility

The app dynamically requests required permissions based on the Android version:

- **Android 12+ (API 31+)**:
  - `android.permission.BLUETOOTH_SCAN` (`neverForLocation`)
  - `android.permission.BLUETOOTH_CONNECT`
  - `android.permission.BLUETOOTH_ADVERTISE`
- **Android 11 and lower (API 30-)**:
  - `android.permission.BLUETOOTH`
  - `android.permission.BLUETOOTH_ADMIN`
  - `android.permission.ACCESS_FINE_LOCATION`
  - `android.permission.ACCESS_COARSE_LOCATION`

---

## Getting Started

### Prerequisites
- Android Studio Ladybug / Meerkat (or newer)
- JDK 11 or higher
- Two physical Android devices running API 24 (Android 7.0) or higher (Bluetooth communication cannot be tested on standard emulators without bluetooth passthrough support).

### Build & Run
1. Clone the repository:
   ```bash
   git clone https://github.com/emanueldias/BluetoothChat.git
   cd BluetoothChat
   ```
2. Open the project in **Android Studio**.
3. Connect your Android devices via USB / Wireless ADB.
4. Select the target device and press **Run** (`Shift + F10` / `Control + R`).
5. Grant Bluetooth permissions on both devices and start chatting offline!

---
