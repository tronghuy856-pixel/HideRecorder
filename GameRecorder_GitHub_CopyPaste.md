# GameRecorder — thứ tự tạo file trên GitHub

Đây là bản ghi màn hình Android thông thường. Nó không có chức năng che ESP/hack hoặc làm thay đổi nội dung game.

Tạo từng file theo đúng PATH bên dưới, rồi sao chép phần code tương ứng.

## 1. `settings.gradle.kts`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "GameRecorder"
include(":app")
```

## 2. `build.gradle.kts`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```kotlin
plugins {
    id("com.android.application") version "8.7.3" apply false
    id("org.jetbrains.kotlin.android") version "2.0.21" apply false
}
```

## 3. `gradle.properties`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
kotlin.code.style=official
```

## 4. `app/build.gradle.kts`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.gamerecorder"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gamerecorder"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }
}
```

## 5. `app/src/main/AndroidManifest.xml`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
    <uses-permission android:name="android.permission.FOREGROUND_SERVICE_MEDIA_PROJECTION" />
    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />

    <application
        android:allowBackup="false"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@android:style/Theme.Material.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

        <service
            android:name=".RecorderService"
            android:exported="false"
            android:foregroundServiceType="mediaProjection" />

    </application>
</manifest>
```

## 6. `app/src/main/res/values/strings.xml`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Game Recorder</string>
</resources>
```

## 7. `app/src/main/java/com/example/gamerecorder/MainActivity.kt`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```kotlin
package com.example.gamerecorder

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    companion object {
        private const val REQUEST_CAPTURE = 1001
        private const val REQUEST_NOTIFICATIONS = 1002
    }

    private lateinit var projectionManager: MediaProjectionManager
    private lateinit var status: TextView
    private lateinit var button: Button
    private var recording = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        projectionManager =
            getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        status = TextView(this).apply {
            text = "Sẵn sàng"
            textSize = 18f
            setPadding(24, 24, 24, 24)
        }

        button = Button(this).apply {
            text = "Bắt đầu quay"
            setOnClickListener {
                if (recording) stopRecording() else startRecording()
            }
        }

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 50, 24, 24)
            addView(status)
            addView(button)
        }

        setContentView(layout)

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                REQUEST_NOTIFICATIONS
            )
        }
    }

    private fun startRecording() {
        val captureIntent = projectionManager.createScreenCaptureIntent()
        startActivityForResult(captureIntent, REQUEST_CAPTURE)
    }

    private fun stopRecording() {
        val intent = Intent(this, RecorderService::class.java).apply {
            action = RecorderService.ACTION_STOP
        }
        startService(intent)

        recording = false
        button.text = "Bắt đầu quay"
        status.text = "Đã dừng. Video được lưu trong thư viện."
    }

    @Deprecated("Activity Result API not used in this minimal project")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CAPTURE) return

        if (resultCode != RESULT_OK || data == null) {
            status.text = "Đã hủy quyền quay màn hình"
            return
        }

        val serviceIntent = Intent(this, RecorderService::class.java).apply {
            action = RecorderService.ACTION_START
            putExtra(RecorderService.EXTRA_RESULT_CODE, resultCode)
            putExtra(RecorderService.EXTRA_DATA, data)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }

        recording = true
        button.text = "Dừng quay"
        status.text = "Đang quay màn hình..."
        Toast.makeText(this, "Bắt đầu quay", Toast.LENGTH_SHORT).show()
    }
}
```

## 8. `app/src/main/java/com/example/gamerecorder/RecorderService.kt`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```kotlin
package com.example.gamerecorder

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.provider.MediaStore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecorderService : Service() {

    companion object {
        const val ACTION_START = "START_RECORDING"
        const val ACTION_STOP = "STOP_RECORDING"

        const val EXTRA_RESULT_CODE = "result_code"
        const val EXTRA_DATA = "data"

        private const val CHANNEL_ID = "recorder_channel"
        private const val NOTIFICATION_ID = 7
    }

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var mediaRecorder: MediaRecorder? = null
    private var outputUri: Uri? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        when (intent?.action) {
            ACTION_START -> {
                val resultCode =
                    intent.getIntExtra(EXTRA_RESULT_CODE, -1)

                val data: Intent? =
                    if (Build.VERSION.SDK_INT >= 33) {
                        intent.getParcelableExtra(
                            EXTRA_DATA,
                            Intent::class.java
                        )
                    } else {
                        @Suppress("DEPRECATION")
                        intent.getParcelableExtra(EXTRA_DATA)
                    }

                if (resultCode == -1 || data == null) {
                    stopSelf()
                    return START_NOT_STICKY
                }

                startForeground(
                    NOTIFICATION_ID,
                    createNotification("Đang quay màn hình")
                )

                startRecording(resultCode, data)
            }

            ACTION_STOP -> {
                stopRecording()
            }
        }

        return START_NOT_STICKY
    }

    private fun startRecording(
        resultCode: Int,
        data: Intent
    ) {
        if (mediaRecorder != null) return

        val metrics = resources.displayMetrics
        val width = metrics.widthPixels
        val height = metrics.heightPixels
        val density = metrics.densityDpi

        val videoWidth = width - (width % 2)
        val videoHeight = height - (height % 2)

        val name = "GameRecorder_" +
                SimpleDateFormat(
                    "yyyyMMdd_HHmmss",
                    Locale.US
                ).format(Date()) +
                ".mp4"

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT >= 29) {
                put(
                    MediaStore.Video.Media.RELATIVE_PATH,
                    "Movies/GameRecorder"
                )
            }
        }

        outputUri = contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val uri = outputUri ?: run {
            stopSelf()
            return
        }

        val outputStream = contentResolver.openFileDescriptor(
            uri,
            "w"
        ) ?: run {
            contentResolver.delete(uri, null, null)
            stopSelf()
            return
        }

        mediaRecorder = MediaRecorder().apply {
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoEncodingBitRate(12_000_000)
            setVideoFrameRate(60)
            setVideoSize(videoWidth, videoHeight)
            setOutputFile(outputStream.fileDescriptor)
            prepare()
        }

        outputStream.close()

        val projectionManager =
            getSystemService(
                Context.MEDIA_PROJECTION_SERVICE
            ) as MediaProjectionManager

        mediaProjection =
            projectionManager.getMediaProjection(
                resultCode,
                data
            )

        val surface = mediaRecorder!!.surface

        virtualDisplay =
            mediaProjection!!.createVirtualDisplay(
                "GameRecorder",
                videoWidth,
                videoHeight,
                density,
                DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                surface,
                null,
                null
            )

        mediaRecorder!!.start()
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.stop()
        } catch (_: Exception) {
            outputUri?.let {
                contentResolver.delete(it, null, null)
            }
        }

        try {
            mediaRecorder?.reset()
        } catch (_: Exception) {
        }

        mediaRecorder?.release()
        mediaRecorder = null

        virtualDisplay?.release()
        virtualDisplay = null

        mediaProjection?.stop()
        mediaProjection = null

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val manager =
                getSystemService(NotificationManager::class.java)

            manager.createNotificationChannel(
                NotificationChannel(
                    CHANNEL_ID,
                    "Game Recorder",
                    NotificationManager.IMPORTANCE_LOW
                )
            )
        }
    }

    private fun createNotification(text: String): Notification {
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(this, CHANNEL_ID)
                .setContentTitle("Game Recorder")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(this)
                .setContentTitle("Game Recorder")
                .setContentText(text)
                .setSmallIcon(android.R.drawable.ic_menu_camera)
                .setOngoing(true)
                .build()
        }
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
```

## 9. `.github/workflows/build.yml`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```yaml
name: Build APK

on:
  workflow_dispatch:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - name: Checkout
        uses: actions/checkout@v4

      - name: Setup Java 17
        uses: actions/setup-java@v4
        with:
          distribution: temurin
          java-version: "17"

      - name: Setup Gradle
        uses: gradle/actions/setup-gradle@v4

      - name: Build Debug APK
        run: gradle assembleDebug

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: GameRecorder-debug
          path: app/build/outputs/apk/debug/app-debug.apk
```

## 10. `README.md`

Trong GitHub chọn **Add file → Create new file**, nhập chính xác PATH ở trên, rồi dán toàn bộ nội dung:

```markdown
# GameRecorder

Android screen recorder using MediaProjection + MediaRecorder.

## Build with GitHub Actions

1. Upload this project to a GitHub repository.
2. Open the Actions tab.
3. Select "Build APK".
4. Press "Run workflow".
5. Open the completed workflow run.
6. Download the "GameRecorder-debug" artifact.
7. Extract the ZIP and install the APK.

The app records the Android screen with the normal Android screen-capture permission dialog.
```
