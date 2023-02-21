package dk.scuffed.whiteboardapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation

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

    private lateinit var segmentationModel : PPSegmentation

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

        segmentationModel = PPSegmentation(this, PPSegmentation.Model.PORTRAIT)
    }

    // TEMPORARY HACK TO TEST
    override fun onUserInteraction() {
        super.onUserInteraction()
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