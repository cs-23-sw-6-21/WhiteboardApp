package dk.scuffed.whiteboardapp.pipeline.stages

import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.opengl.glFinish
import dk.scuffed.whiteboardapp.pipeline.IPipeline

private val recordTimings = true

/**
 * Baseclass for stages.
 * Manages updates and connection to the pipeline.
 */
internal abstract class Stage(pipeline: IPipeline) {

    private val name: String

    private var resolution: Size

    init {
        pipeline.addStage(this)
        name = this.javaClass.name
        resolution = pipeline.getInitialResolution()
    }

    protected fun getResolution(): Size {
        return resolution
    }

    protected abstract fun update()

    fun performUpdate() {
        val startTime = System.nanoTime()
        update()
        if (recordTimings && this is GLOutputStage) {
            glFinish()
        }
        val endTime = System.nanoTime()

        //Calculate duration and convert to ms
        val duration = (endTime - startTime).toDouble() / 1000000.0

        if (recordTimings) {
            Log.d("Stages", "Stage: $name took $duration ms")
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