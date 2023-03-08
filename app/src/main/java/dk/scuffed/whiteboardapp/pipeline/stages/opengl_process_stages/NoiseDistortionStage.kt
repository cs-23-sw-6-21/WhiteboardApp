package dk.scuffed.whiteboardapp.pipeline.stages

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.*
import java.nio.ByteBuffer


/**
 * A silly shader that distorts the UVs of the input over time.
 */
internal class NoiseDistortionStage(private val context: Context, private val inputFrameBufferInfo: FramebufferInfo, private val pipeline: Pipeline): GLOutputStage(context, R.raw.vertex_shader, R.raw.shaderfun, pipeline) {

    // Texture data
    private lateinit var textureBuffer : ByteBuffer
    private lateinit var textureUnitPair: TextureUnitPair

    private val time = System.currentTimeMillis();

    private var textureHandle : Int = 0

    init {
        setup()
        loadTexture()
    }

    override fun setupFramebufferInfo() {
        val resolution = Size(inputFrameBufferInfo.textureSize.width, inputFrameBufferInfo.textureSize.height)
        allocateFramebuffer(GLES20.GL_RGBA, resolution)
    }

    fun loadTexture(){
        // Load noise texture as bitmap
        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.noisetexture)

        // Copy into a byte buffer so it can be used by GLES2
        val width = bitmap.width
        val height = bitmap.height
        val size: Int = bitmap.getRowBytes() * bitmap.getHeight()
        textureBuffer = ByteBuffer.allocate(size)
        bitmap.copyPixelsToBuffer(textureBuffer)
        textureBuffer.position(0)

        // Setup the GLES2 texture stuff
        textureUnitPair = pipeline.allocateTextureUnit(this)
        glActiveTexture(textureUnitPair.textureUnit)

        textureHandle = glGenTexture()

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(
            GLES20.GL_TEXTURE_2D,
            0,
            GLES20.GL_RGBA,
            width,
            height,
            GLES20.GL_UNSIGNED_BYTE,
            textureBuffer
        )
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // Input framebuffer resolution
        val framebufferResolutionHandle = glGetUniformLocation(program, "framebuffer_resolution")
        glUniform2f(framebufferResolutionHandle, inputFrameBufferInfo.textureSize.width.toFloat(), inputFrameBufferInfo.textureSize.height.toFloat())

        // Input framebuffer
        val framebufferTextureHandle = glGetUniformLocation(program, "framebuffer")
        glUniform1i(framebufferTextureHandle, inputFrameBufferInfo.textureUnitPair.textureUnitIndex)
        glActiveTexture(inputFrameBufferInfo.textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, inputFrameBufferInfo.textureHandle)

        // Pass in the noise texture
        val noiseTextureHandle = glGetUniformLocation(program, "noise")
        glUniform1i(noiseTextureHandle, textureUnitPair.textureUnitIndex)
        glActiveTexture(textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Pass in time (LOW PRECISION)
        val timeHandle = glGetUniformLocation(program, "time")
        glUniform1f(timeHandle, (System.currentTimeMillis() - time)/1000f)
    }
}