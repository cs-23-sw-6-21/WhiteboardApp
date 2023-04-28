package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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
import dk.scuffed.whiteboardapp.pipeline.stages.input_stages.TextureStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.BinarizationFastStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.Downscale2xStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.GrayscaleStage
import dk.scuffed.whiteboardapp.pipeline.stages.opengl_process_stages.ScaleToResolution
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawFramebufferStage
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.SwitchablePointPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage

internal class Pipeline(private val context: Context, private val initialResolution: Size) : IPipeline {

    var stages = mutableListOf<Stage>()
    private var nextTextureUnit: Int = 0
    private val switchablePointPipeline: SwitchablePointPipeline

    private val indexToTextureUnit = intArrayOf(
        GLES20.GL_TEXTURE0,
        GLES20.GL_TEXTURE1,
        GLES20.GL_TEXTURE2,
        GLES20.GL_TEXTURE3,
        GLES20.GL_TEXTURE4,
        GLES20.GL_TEXTURE5,
        GLES20.GL_TEXTURE6,
        GLES20.GL_TEXTURE7,
        GLES20.GL_TEXTURE8,
        GLES20.GL_TEXTURE9,
        GLES20.GL_TEXTURE10,
        GLES20.GL_TEXTURE11,
        GLES20.GL_TEXTURE12,
        GLES20.GL_TEXTURE13,
        GLES20.GL_TEXTURE14,
        GLES20.GL_TEXTURE15,
        GLES20.GL_TEXTURE16,
        GLES20.GL_TEXTURE17,
        GLES20.GL_TEXTURE18,
        GLES20.GL_TEXTURE19,
        GLES20.GL_TEXTURE20,
        GLES20.GL_TEXTURE21,
        GLES20.GL_TEXTURE22,
        GLES20.GL_TEXTURE23,
        GLES20.GL_TEXTURE24,
        GLES20.GL_TEXTURE25,
        GLES20.GL_TEXTURE26,
        GLES20.GL_TEXTURE27,
        GLES20.GL_TEXTURE28,
        GLES20.GL_TEXTURE29,
        GLES20.GL_TEXTURE30,
        GLES20.GL_TEXTURE31
        )

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
            BitmapFactory.decodeResource(context.resources, R.drawable.binarizetest, opt),
            this
        )
        */

        val cameraXStage = CameraXStage(context, this)

        val entirePipeline = fullPipeline(context, cameraXStage, this)
        switchablePointPipeline = entirePipeline.first

        DrawFramebufferStage(
            context,
            entirePipeline.second.frameBufferInfo,
            this
        )

        if (CSVWriter.recordTimings){
            CSVWriter.MainWriter.write("Overall\n")
        }
    }

    /**
     * Sets the switch variable to the inputted boolean.
     * @param bool is a Boolean to changes the switches value.
     */
    fun switchStages(bool: Boolean){
        switchablePointPipeline.setSwitch(bool)
    }
    override fun draw() {
        val startTime = System.nanoTime()
        stages.forEach { stage -> stage.performUpdate() }
        val endTime = System.nanoTime()
        val duration = (endTime - startTime).toDouble() / 1000000.0

        if (CSVWriter.recordTimings)
        {
            CSVWriter.MainWriter.write("$duration\n")
            CSVWriter.frameCounter += 1
            if (CSVWriter.frameCounter == 100)
            {
                (context as MainActivity).findViewById<Button>(R.id.round_button).visibility = View.INVISIBLE
                CSVWriter.recordTimings = false
                CSVWriter.MainWriter.flush()
                CSVWriter.MainWriter.close()
                CSVWriter.CornerDetectionWriter.flush()
                CSVWriter.CornerDetectionWriter.close()
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
        val textureUnit = indexToTextureUnit[textureUnitIndex]
        return TextureUnitPair(textureUnit, textureUnitIndex)
    }
}
