package dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import java.nio.ByteBuffer

/**
 * Writes the framebuffer into the bitmap each update.
 */
internal class FramebufferToBitmapStage(private val inputFramebufferInfo: FramebufferInfo, config: Bitmap.Config, pipeline: IPipeline)
    : BitmapOutputStage(pipeline, Size(inputFramebufferInfo.textureSize.width, inputFramebufferInfo.textureSize.height), config) {

    private val byteBuffer: ByteBuffer

    init {
        val width = inputFramebufferInfo.textureSize.width
        val height = inputFramebufferInfo.textureSize.height
        val bytesPerPixel = bytesPerPixel(inputFramebufferInfo.textureFormat)

        byteBuffer = ByteBuffer.allocateDirect(width * height * bytesPerPixel)
    }

    override fun update() {
        readFrameBuffer(outputBitmap, inputFramebufferInfo)
    }

    private fun readFrameBuffer(bitmap: Bitmap, framebufferInfo: FramebufferInfo) {
        byteBuffer.position(0)

        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glReadPixels(0, 0, inputFramebufferInfo.textureSize.width, framebufferInfo.textureSize.height, GLES20.GL_RGBA, byteBuffer)

        bitmap.copyPixelsFromBuffer(byteBuffer)
    }
}