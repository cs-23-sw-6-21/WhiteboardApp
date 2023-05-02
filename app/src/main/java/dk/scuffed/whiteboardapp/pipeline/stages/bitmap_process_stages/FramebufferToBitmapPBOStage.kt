package dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLES30
import dk.scuffed.whiteboardapp.helper.GlesHelper.glReadPixels
import dk.scuffed.whiteboardapp.opengl.bytesPerPixel
import dk.scuffed.whiteboardapp.opengl.glActiveTexture
import dk.scuffed.whiteboardapp.opengl.glBindFramebuffer
import dk.scuffed.whiteboardapp.opengl.glReadPixels
import dk.scuffed.whiteboardapp.pipeline.FramebufferInfo
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.BitmapOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int
import java.nio.ByteBuffer
import java.nio.IntBuffer


/**
 * Writes the framebuffer into the bitmap each update.
 */
internal class FramebufferToBitmapPBOStage(
    private val inputFramebufferInfo: FramebufferInfo,
    config: Bitmap.Config,
    pipeline: IPipeline
) : BitmapOutputStage(
    pipeline,
    inputFramebufferInfo.textureSize,
    config
) {

    private val byteBuffer: ByteBuffer
    private val pboIds = IntBuffer.allocate(2)
    private var currentPboIndex = 0
    private var nextPboIndex = 1

    init {
        val width = inputFramebufferInfo.textureSize.width
        val height = inputFramebufferInfo.textureSize.height
        val bytesPerPixel = bytesPerPixel(inputFramebufferInfo.textureFormat)

        byteBuffer = ByteBuffer.allocateDirect(width * height * bytesPerPixel)


        // Generate the buffers for the pbo's
        GLES30.glGenBuffers(pboIds.capacity(), pboIds)

        // Loop for how many pbo's we have

        // Loop for how many pbo's we have
        for (i in 0 until pboIds.capacity()) {
            // Bind the Pixel_Pack_Buffer to the current pbo id
            GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds.get(i))

            // Buffer empty data, capacity is the width * height * 4
            val capacity = inputFramebufferInfo.textureSize.width * inputFramebufferInfo.textureSize.height * 4
            GLES30.glBufferData(GLES30.GL_PIXEL_PACK_BUFFER, capacity, null, GLES30.GL_STATIC_READ)
        }

        // Reset the current buffer so we can draw properly
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, 0)
    }

    override fun update() {
        readPixelsFromPBO(outputBitmap)
    }

    private fun readFrameBuffer(bitmap: Bitmap, framebufferInfo: FramebufferInfo) {
        byteBuffer.position(0)

        glBindFramebuffer(inputFramebufferInfo.fboHandle)
        glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)
        glReadPixels(Vec2Int(0, 0), inputFramebufferInfo.textureSize, GLES20.GL_RGBA, byteBuffer)

        //glReadPixels()

        bitmap.copyPixelsFromBuffer(byteBuffer)
    }

    /**
     * Reads the pixels from the PBO and swaps the buffers
     */
    private fun readPixelsFromPBO(bitmap: Bitmap) {
        // Bind the current buffer
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds.get(currentPboIndex))

        //glBindFramebuffer(inputFramebufferInfo.fboHandle)
        //glActiveTexture(inputFramebufferInfo.textureUnitPair.textureUnit)

        // Read pixels into the bound buffer
        glReadPixels(0, 0, inputFramebufferInfo.textureSize.width, inputFramebufferInfo.textureSize.height, GLES20.GL_RGBA, GLES30.GL_UNSIGNED_BYTE)

        // Bind the next buffer
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, pboIds.get(nextPboIndex))

        // Map to buffer to a byte buffer, this is our pixel data
        val pixelsBuffer = GLES30.glMapBufferRange(
            GLES30.GL_PIXEL_PACK_BUFFER,
            0,
            inputFramebufferInfo.textureSize.width * inputFramebufferInfo.textureSize.height * 4,
            GLES30.GL_MAP_READ_BIT
        ) as ByteBuffer

        bitmap.copyPixelsFromBuffer(pixelsBuffer)


        // Swap the buffer index
        currentPboIndex = (currentPboIndex + 1) % pboIds.capacity()
        nextPboIndex = (nextPboIndex + 1) % pboIds.capacity()

        // Unmap the buffers
        GLES30.glUnmapBuffer(GLES30.GL_PIXEL_PACK_BUFFER)
        GLES30.glBindBuffer(GLES30.GL_PIXEL_PACK_BUFFER, GLES20.GL_NONE)
        GLES30.glBindFramebuffer(GLES30.GL_FRAMEBUFFER, GLES20.GL_NONE)
    }
}