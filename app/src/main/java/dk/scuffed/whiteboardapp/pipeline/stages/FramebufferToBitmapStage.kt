package dk.scuffed.whiteboardapp.pipeline.stages

import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.*

internal class FramebufferToBitmapStage(private val inputFramebufferInfo: FramebufferInfo, config: Bitmap.Config, pipeline: Pipeline)
    : BitmapOutputStage(pipeline, Size(inputFramebufferInfo.textureSize.width, inputFramebufferInfo.textureSize.height), config) {

    override fun update() {
        readFrameBuffer(outputBitmap, inputFramebufferInfo)
    }

    private fun readFrameBuffer(bitmap: Bitmap, framebufferInfo: FramebufferInfo) {
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        val buffer = glReadPixels(0, 0, inputFramebufferInfo.textureSize.width, framebufferInfo.textureSize.height, GLES20.GL_RGBA)
        bitmap.copyPixelsFromBuffer(buffer)
    }
}