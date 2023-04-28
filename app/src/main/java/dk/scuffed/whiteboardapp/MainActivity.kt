package dk.scuffed.whiteboardapp

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import dk.scuffed.whiteboardapp.opengl.OpenGLView
import org.opencv.android.OpenCVLoader


class MainActivity : AppCompatActivity() {
    private var stageSwitch = false
    private val CAMERA_PERMISSION_REQUEST_CODE = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!OpenCVLoader.initDebug()) {
            Log.d("Whiteboard App", "Unable to load OpenCV")
        } else {
            Log.d("Whiteboard App", "OpenCV loaded")
        }

        // Check and request camera permissions
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), CAMERA_PERMISSION_REQUEST_CODE)
        }
        else {
            setContentView(R.layout.activity_main)
            val buttonView = findViewById<ConstraintLayout>(R.id.button_view)
            val openGLView = OpenGLView(this)
            buttonView.addView(openGLView)
            findViewById<Button>(R.id.round_button).setOnClickListener {
                openGLView.switchStage(stageSwitch)
                stageSwitch = !stageSwitch
            }
        }
        // Check if the permission has already been granted
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission has already been granted, you can save CSV files
        } else {
            // Permission has not been granted yet, request it
            requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
        }

        if (!Environment.isExternalStorageManager()) {
            val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            startActivity(intent)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setContentView(OpenGLView(this))
            } else {
                Toast.makeText(this, "This app requires camera permission!", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
        }
        if (requestCode == 1)
        {
            // If request is cancelled, the grantResults array is empty
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted, you can save CSV files
            } else {
                Toast.makeText(this, "This app requires storage permission!", Toast.LENGTH_LONG)
                    .show()
                finish()
            }
            return
        }
    }
}
