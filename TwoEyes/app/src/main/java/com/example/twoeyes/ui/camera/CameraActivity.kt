package com.example.twoeyes.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.twoeyes.R
import com.google.android.material.button.MaterialButton

class CameraActivity : AppCompatActivity() {
    private val REQUEST_IMAGE_CAPTURE = 101
    private val REQUEST_ALBUM_CAPTURE = 102
    private val CAMERA_PERMISSION_CODE = 103
    private val ALBUM_PERMISSION_CODE = 104
    private fun isGrantedCameraPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    private fun isGrantedAlbumPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.READ_MEDIA_IMAGES
    ) == PackageManager.PERMISSION_GRANTED
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_camera)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_camera)) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.button_back).setOnClickListener {
            finish()
        }
        findViewById<Button>(R.id.show_camera_button).setOnClickListener {
            requestCameraPermission()
        }
        findViewById<Button>(R.id.show_album_button).setOnClickListener {
            requestAlbumPermission()
        }
    }
    override fun finish() {
        super.finish()
        overridePendingTransition(0, R.anim.slide_down)
    }
    private fun requestCameraPermission() {
        if (isGrantedCameraPermission())
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        else
            dispatchTakePictureIntent()
    }
    private fun requestAlbumPermission() {
        if (isGrantedAlbumPermission())
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                ALBUM_PERMISSION_CODE)
        else
            dispatchOpenAlbumIntent()
    }
    private fun dispatchTakePictureIntent() {
        startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), REQUEST_IMAGE_CAPTURE)
    }
    private fun dispatchOpenAlbumIntent() {
        startActivityForResult(Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
        }, REQUEST_ALBUM_CAPTURE)
    }
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String?>,
        grantResults: IntArray,
        deviceId: Int
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults, deviceId)
        val isPermissionGranted = grantResults.isNotEmpty()
                && grantResults[0] == PackageManager.PERMISSION_GRANTED
        if (isPermissionGranted.not()) {
            Toast
                .makeText(this, "Permission not granted", Toast.LENGTH_LONG)
                .show()
            return
        }
        when (requestCode) {
            CAMERA_PERMISSION_CODE -> dispatchTakePictureIntent()
            ALBUM_PERMISSION_CODE -> dispatchOpenAlbumIntent()
            else -> return
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if ((resultCode == RESULT_OK).not()) {
            Toast
                .makeText(this, "Activity Result Error", Toast.LENGTH_LONG)
                .show()
            return
        }

        if (requestCode == REQUEST_IMAGE_CAPTURE) {
            val imageBitmap = data?.extras?.get("data") as Bitmap
            findViewById<ImageView>(R.id.picture_image_view).setImageBitmap(imageBitmap)
        } else if (requestCode == REQUEST_ALBUM_CAPTURE) {
            val uri = data?.data
            if (uri != null) {
                val imageView = findViewById<ImageView>(R.id.picture_image_view)
                Glide.with(this).load(uri).into(imageView)
            } else {
                Toast.makeText(this, "Failed to get image URI", Toast.LENGTH_SHORT).show()
            }
        }
    }
}