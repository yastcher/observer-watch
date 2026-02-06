# Observer Watch

An Android application that continuously monitors the front-facing camera for faces and sends captured images to a remote server. Acts as an automated watchman for security and surveillance purposes.

## Features

- Real-time face detection using Google ML Kit (Play Services Vision)
- Runs as a foreground service with a persistent notification
- Captures and uploads images only when faces are detected
- Asynchronous image upload via OkHttp
- Automatic YUV-to-JPEG image conversion

## Architecture

```
MainActivity (Permission gate)
    |
CameraService (Foreground service)
    ├── Camera2 API (Front camera capture)
    ├── FaceDetectionService (ML Kit face detection)
    └── NetworkService (OkHttp image upload)
         |
         Remote Server
```

| Component              | Responsibility                                     |
|------------------------|----------------------------------------------------|
| `MainActivity`         | Requests camera permission, starts the service     |
| `CameraService`       | Manages camera lifecycle, foreground notification   |
| `FaceDetectionService` | Detects faces in camera frames, converts images    |
| `NetworkService`       | Uploads JPEG images to the server via HTTP POST    |

## Requirements

- Android SDK 21+ (Android 5.0 Lollipop)
- Target SDK 30 (Android 11)
- Device with a front-facing camera
- Google Play Services (for ML Kit face detection)

## Permissions

| Permission             | Purpose                              |
|------------------------|--------------------------------------|
| `CAMERA`               | Access the front-facing camera       |
| `INTERNET`             | Upload images to the remote server   |
| `FOREGROUND_SERVICE`   | Run the monitoring service           |

## Setup

### 1. Clone the repository

```bash
git clone https://github.com/your-username/observer-watch.git
cd observer-watch
```

### 2. Configure the server URL

Edit `src/main/kotlin/com/example/app/NetworkService.kt` and replace the placeholder:

```kotlin
const val SERVER_URL = "https://your-server.com/api/upload"
```

The server should accept `multipart/form-data` POST requests with an `image` field containing a JPEG file.

### 3. Build

```bash
./gradlew assembleDebug
```

### 4. Install

```bash
./gradlew installDebug
```

## Testing

Run unit tests:

```bash
./gradlew test
```

Tests cover:
- `NetworkServiceTest` — HTTP request formation, multipart upload, error handling (uses MockWebServer)
- `FaceDetectionServiceTest` — Detector initialization and resource cleanup
- `MainActivityTest` — Component class verification

## How It Works

1. `MainActivity` checks for the `CAMERA` permission. If not granted, it requests it and exits if denied.
2. Once permission is granted, it starts `CameraService` as a foreground service.
3. `CameraService` opens the front-facing camera via Camera2 API and sets up a repeating capture session.
4. Each frame is passed to `FaceDetectionService`, which converts YUV frames to bitmaps and runs face detection.
5. When faces are detected, the frame is converted to a JPEG file and uploaded asynchronously via `NetworkService`.

## Project Structure

```
observer-watch/
├── build.gradle.kts
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   └── kotlin/com/example/app/
│   │       ├── MainActivity.kt
│   │       ├── CameraService.kt
│   │       ├── FaceDetectionService.kt
│   │       └── NetworkService.kt
│   └── test/
│       └── kotlin/com/example/app/
│           ├── MainActivityTest.kt
│           ├── FaceDetectionServiceTest.kt
│           └── NetworkServiceTest.kt
└── README.md
```

## License

MIT
