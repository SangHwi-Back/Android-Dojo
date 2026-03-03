package com.example.twoeyes.ui.camera

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.twoeyes.R
import com.google.android.material.button.MaterialButton

class CameraActivity : AppCompatActivity() {
    // Android 13 이상: READ_MEDIA_IMAGES 사용
    // Android 12 이하: READ_EXTERNAL_STORAGE 사용
    private val currentAlbumPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        Manifest.permission.READ_MEDIA_IMAGES else Manifest.permission.READ_EXTERNAL_STORAGE
    private fun isGrantedCameraPermission() = ContextCompat.checkSelfPermission(
        this,
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED
    private fun isGrantedAlbumPermission(): Boolean = ContextCompat.checkSelfPermission(
        this,
        currentAlbumPermission
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
        findViewById<MaterialButton>(R.id.button_back)
            .setOnClickListener { finish() }
        findViewById<Button>(R.id.show_camera_button)
            .setOnClickListener { requestCameraPermission() }
        findViewById<Button>(R.id.show_album_button)
            .setOnClickListener { requestAlbumPermission() }
    }
    override fun finish() {
        super.finish()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
            overrideActivityTransition(
                OVERRIDE_TRANSITION_CLOSE, 0, R.anim.slide_down)
        else
            @Suppress("DEPRECATION")
            overridePendingTransition(0, R.anim.slide_down)
    }
    // 카메라로 사진 찍기
    private val takePictureLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val imageBitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            result.data?.extras?.getParcelable("data", Bitmap::class.java)
        else
            @Suppress("DEPRECATION")
            result.data?.extras?.getParcelable("data")

        if (result.resultCode == RESULT_OK && imageBitmap != null)
            findViewById<ImageView>(R.id.picture_image_view).setImageBitmap(imageBitmap)
        else
            showToast("Failed to get image")
    }
    // 앨범에서 이미지 선택
    private val pickImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uri = result.data?.data
        if (result.resultCode == RESULT_OK && uri != null)
            Glide.with(this).load(uri).into(findViewById(R.id.picture_image_view))
        else
            showToast("Failed to get image URI")
    }
    // 카메라 권한 요청
    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (isGranted) takePictureLauncher.launch(intent) else showToast("Camera permission not granted")
    }
    // 앨범 권한 요청
    private val albumPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        if (isGranted) pickImageLauncher.launch(intent) else showToast("Album permission not granted")
    }
    private fun showToast(message: String) = Toast
        .makeText(this, message, Toast.LENGTH_LONG).show()
    private fun requestCameraPermission() {
        if (isGrantedCameraPermission())
            // 권한이 이미 있으면 바로 카메라 실행
            takePictureLauncher.launch(Intent(MediaStore.ACTION_IMAGE_CAPTURE))
        else
            // 권한이 없으면 권한 요청
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    private fun requestAlbumPermission() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply { type = "image/*" }
        if (isGrantedAlbumPermission())
            // 권한이 이미 있으면 바로 앨범 실행
            pickImageLauncher.launch(intent)
        else
            // 권한이 없으면 권한 요청
            albumPermissionLauncher.launch(currentAlbumPermission)
    }
}