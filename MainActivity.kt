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
