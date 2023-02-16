package dk.scuffed.whiteboardapp

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    val requestCamera = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            Log.d("camera permission", "granted")
            Toast.makeText(applicationContext, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Log.d("camera permission", "denied")
            finishAffinity()
        }
    }

    private lateinit var previewView : PreviewView

    private lateinit var imageCapture: ImageCapture
    lateinit var cameraProviderFuture : ListenableFuture<ProcessCameraProvider>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)

        requestCamera.launch(android.Manifest.permission.CAMERA)

        cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(Runnable {
            val cameraProvider = cameraProviderFuture.get()
            bindPreview(cameraProvider)
        }, ContextCompat.getMainExecutor(this))

    }

    private fun bindPreview(cameraProvider : ProcessCameraProvider) {
        var preview : Preview = Preview.Builder().build()

        var cameraSelector : CameraSelector = CameraSelector.Builder()
            .requireLensFacing(CameraSelector.LENS_FACING_BACK)
            .build()

        preview.setSurfaceProvider(previewView.getSurfaceProvider())

        cameraProvider.bindToLifecycle(this as LifecycleOwner, cameraSelector, preview)
    }

}