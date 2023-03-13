package dk.scuffed.whiteboardapp.pipeline.stages.input_stages

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLES20
import android.util.Size
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.*
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import java.nio.ByteBuffer


/**
 * Outputs a bitmap given in constructor as a OpenGL framebuffer.
 */
internal class TextureStage(private val context: Context, private val texture : Bitmap, private val pipeline: Pipeline): GLOutputStage(context, R.raw.vertex_shader, R.raw.texture, pipeline) {

    // Texture data
    private lateinit var textureBuffer : ByteBuffer
    private lateinit var textureUnitPair: TextureUnitPair
    private var textureHandle : Int = 0

    init {
        setup()
        loadTexture()
    }

    override fun setupFramebufferInfo() {
        val resolution = Size(texture.width, texture.height)
        allocateFramebuffer(GLES20.GL_RGBA, resolution)
    }

    override fun setupUniforms(program: Int) {
        super.setupUniforms(program)

        // Pass in the texture
        val noiseTextureHandle = glGetUniformLocation(program, "source_texture")
        glUniform1i(noiseTextureHandle, textureUnitPair.textureUnitIndex)
        glActiveTexture(textureUnitPair.textureUnit)
        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)

        // Input framebuffer resolution
        val framebufferResolutionHandle = glGetUniformLocation(program, "resolution")
        glUniform2f(framebufferResolutionHandle, texture.width.toFloat(), texture.height.toFloat())

    }


    fun loadTexture(){

        // Copy into a byte buffer so it can be used by GLES2
        val width = texture.width
        val height = texture.height
        val size: Int = texture.getRowBytes() * texture.getHeight()
        textureBuffer = ByteBuffer.allocate(size)
        texture.copyPixelsToBuffer(textureBuffer)
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

}