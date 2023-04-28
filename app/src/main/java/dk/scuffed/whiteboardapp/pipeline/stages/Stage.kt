package dk.scuffed.whiteboardapp.pipeline.stages

import android.os.Looper
import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.glFinish
import dk.scuffed.whiteboardapp.pipeline.CSVWriter
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.SwitchablePointPipeline

/**
 * Baseclass for stages.
 * Manages updates and connection to the pipeline.
 */
internal abstract class Stage(private val pipeline: IPipeline) {

    private val name: String

    private var resolution: Size

    init {
        pipeline.addStage(this)
        name = this.javaClass.name
        resolution = pipeline.getInitialResolution()
        if (CSVWriter.recordTimings) {
            CSVWriter.MainWriter.write("${name},")
        }
    }

    protected fun getResolution(): Size {
        return resolution
    }

    protected abstract fun update()

    fun performUpdate() {
        val startTime = System.nanoTime()
        update()
        if (CSVWriter.useGlFinish && this is GLOutputStage) {
            glFinish()
        }
        val endTime = System.nanoTime()

        //Calculate duration and convert to ms
        val duration = (endTime - startTime).toDouble() / 1000000.0

        if (CSVWriter.recordTimings) {
                CSVWriter.MainWriter.write("$name,")
        }
        if (BuildConfig.DEBUG) {
            if (this is GLOutputStage) {
                Log.d("Stages", "Stage: $name is ${frameBufferInfo.textureSize} ms")
            }
            if (this is BitmapOutputStage) {
                Log.d("Stages", "Stage: $name is ${Size(outputBitmap.width, outputBitmap.height)} ms")
            }
        }
    }

    fun performOnResolutionChanged(resolution: Size) {
        this.resolution = resolution
        whenResolutionChanged(resolution)
    }

    protected open fun whenResolutionChanged(resolution: Size) {
        // Intentionally left blank :^)
    }
}