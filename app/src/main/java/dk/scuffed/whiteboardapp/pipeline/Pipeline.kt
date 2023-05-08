package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.opengl.GLES20
import android.util.Log
import android.util.Size
import android.view.View
import android.widget.Button
import dk.scuffed.whiteboardapp.MainActivity
import dk.scuffed.whiteboardapp.R
import dk.scuffed.whiteboardapp.opengl.*
import dk.scuffed.whiteboardapp.pipeline.stage_combinations.*
import dk.scuffed.whiteboardapp.pipeline.stages.*
import dk.scuffed.whiteboardapp.pipeline.stages.input_stages.CameraXStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.LetterboxingStage
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawFramebufferStage

const val useDoubleBuffering = true

internal class Pipeline(private val context: Context, private val initialResolution: Size) : IPipeline {

    var stages = mutableListOf<Stage>()
    private var nextTextureUnit: Int = 0

    private fun indexToTextureUnit(i: Int): Int{
        return GLES20.GL_TEXTURE0 + i;
    }

    init {
        glDisable(GLES20.GL_BLEND)
        glDisable(GLES20.GL_CULL_FACE)
        glDisable(GLES20.GL_DEPTH_TEST)
        glClearColorError()

        /*
        val opt = BitmapFactory.Options()
        opt.inScaled = false
        val textureStage = TextureStage(
            context,
            BitmapFactory.decodeResource(context.resources, R.drawable.whiteboard, opt),
            this
        )*/

        val cameraXStage = CameraXStage(context, this)


        val entirePipeline = mainThreadPipeline(context, cameraXStage, this)

        //dumpToGalleryFull(context, entirePipeline.second.frameBufferInfo, this)

        val letterbox = LetterboxingStage(context, entirePipeline.frameBufferInfo, this)

        DrawFramebufferStage(
            context,
            entirePipeline.frameBufferInfo,
            this
        )

        if (CSVWriter.recordOverallTimings){
            CSVWriter.MainWriter.write("Overall\n")
        }
    }

    override fun draw() {
        val startTime = System.nanoTime()
        stages.forEach { stage -> stage.performUpdate() }
        val endTime = System.nanoTime()
        val duration = (endTime - startTime).toDouble() / 1000000.0

        if (CSVWriter.recordOverallTimings || CSVWriter.recordStageTimings)
        {
            if (CSVWriter.recordOverallTimings)
            {
                CSVWriter.MainWriter.write("$duration\n")
            }
            CSVWriter.frameCounter += 1
            if (CSVWriter.frameCounter == CSVWriter.numberOfFrames)
            {
                (context as MainActivity).findViewById<Button>(R.id.round_button).visibility = View.INVISIBLE
                CSVWriter.recordStageTimings = false
                CSVWriter.recordOverallTimings = false
                CSVWriter.MainWriter.flush()
                CSVWriter.MainWriter.close()
            }
            Log.i("Pipeline", "Frame took ${duration}ms")
        }
    }

    override fun onResolutionChanged(resolution: Size) {
        stages.forEach { stage -> stage.performOnResolutionChanged(resolution) }
    }

    override fun getInitialResolution(): Size {
        return initialResolution
    }

    override fun allocateFramebuffer(
        stage: Stage,
        textureFormat: Int,
        size: Size
    ): FramebufferInfo {
        val fboHandle = glGenFramebuffer()

        val textureHandle = glGenTexture()

        val textureUnitPair = allocateTextureUnit(stage)
        glActiveTexture(textureUnitPair.textureUnit)

        glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle)
        glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, size, GLES20.GL_UNSIGNED_BYTE, null)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR) // Note: downscaling stages expect this to be linear
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE)
        glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE)
        glBindTexture(GLES20.GL_TEXTURE_2D, 0)

        glBindFramebuffer(fboHandle)
        glFramebufferTexture2D(GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, textureHandle)

        return FramebufferInfo(fboHandle, textureHandle, textureUnitPair, GLES20.GL_RGBA, size)
    }

    override fun addStage(stage: Stage) {
        stages.add(stage)
    }

    override fun allocateTextureUnit(stage: Stage): TextureUnitPair {
        val textureUnitIndex = nextTextureUnit++
        val textureUnit = indexToTextureUnit(textureUnitIndex)
        return TextureUnitPair(textureUnit, textureUnitIndex)
    }
}
