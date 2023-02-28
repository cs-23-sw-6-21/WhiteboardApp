package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.readRawResource
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

internal abstract class BitmapOutputStage(
    pipeline: Pipeline,
    resolution: Size,
    config: Bitmap.Config
    ) : Stage(pipeline)
{
    public var outputBitmap: Bitmap

    init {
        outputBitmap = Bitmap.createBitmap(resolution.width, resolution.height, config)
    }
}