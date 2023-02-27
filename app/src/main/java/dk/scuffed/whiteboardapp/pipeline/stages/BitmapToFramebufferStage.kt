package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Stage
import java.nio.ByteBuffer

internal class BitmapToFramebufferStage(context: Context, private val inputBitmap: Bitmap, pipeline: Pipeline) : GLOU(pipeline) {
    init {
        setup()
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, Size(inputBitmap.width, inputBitmap.height) )
    }

    fun writeFrameBuffer(bitmap: Bitmap, framebuffer: FramebufferInfo) {
        val buffer: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        glActiveTexture(frameBufferInfo.textureUnitPair.textureUnit)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, frameBufferInfo.textureSize.width, frameBufferInfo.textureSize.height, GLES20.GL_UNSIGNED_BYTE, buffer)
    }

}