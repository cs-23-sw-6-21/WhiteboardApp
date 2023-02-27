package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.openGL.*
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.Stage
import dk.scuffed.whiteboardapp.segmentation.PPSegmentation
import java.nio.ByteBuffer

internal class BitmapStage(context: Context, private val inputFramebufferInfo: FramebufferInfo, private val pipeline: Pipeline) : Stage(pipeline) {

    private lateinit var bitmap: Bitmap

    init {
        setup()
        bitmap = Bitmap.createBitmap(inputFramebufferInfo.textureSize.width, inputFramebufferInfo.textureSize.height, Bitmap.Config.ARGB_8888)
    }

    override fun setupFramebufferInfo() {
        allocateFramebuffer(GLES20.GL_RGBA, inputFramebufferInfo.textureSize)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        readFrameBuffer(bitmap, inputFramebufferInfo)

        writeFrameBuffer(bitmap, frameBufferInfo)
    }
    fun readFrameBuffer(bitmap: Bitmap, framebufferInfo: FramebufferInfo) {
        // TODO: take into account RGB, A, and RGBA bitmaps
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        var buffer = glReadPixels(0, 0, inputFramebufferInfo.textureSize.width, framebufferInfo.textureSize.height, GLES20.GL_RGBA)
        bitmap.copyPixelsFromBuffer(buffer)
    }
    fun writeFrameBuffer(bitmap: Bitmap, framebuffer: FramebufferInfo) {

        val buffer: ByteBuffer = ByteBuffer.allocate(bitmap.byteCount)
        bitmap.copyPixelsToBuffer(buffer)
        glActiveTexture(frameBufferInfo.textureUnitPair.textureUnit)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, frameBufferInfo.textureSize.width, frameBufferInfo.textureSize.height, GLES20.GL_UNSIGNED_BYTE, buffer)
    }



}