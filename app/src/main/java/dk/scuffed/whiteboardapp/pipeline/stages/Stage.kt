package dk.scuffed.whiteboardapp.pipeline.stages

import android.util.Log
import android.util.Size
import dk.scuffed.whiteboardapp.pipeline.Pipeline

/**
 * Baseclass for stages.
 * Manages updates and connection to the pipeline.
 */
internal abstract class Stage(pipeline: Pipeline) {

    private val name: String

    private var resolution: Size

    init {
        pipeline.addStage(this)
        name = this.javaClass.name
        resolution = pipeline.initialResolution
    }

    protected fun getResolution() : Size {
        return resolution
    }

    protected abstract fun update()

    fun performUpdate()
    {
        val startTime = System.nanoTime()
        update()
        val endTime = System.nanoTime()

        //Calculate duration and convert to ms
        val duration = (endTime - startTime).toDouble() / 1000000.0

        Log.d("Stages", "Stage: $name took $duration ms")
    }

    fun performOnResolutionChanged(resolution: Size) {
        this.resolution = resolution
        onResolutionChanged(resolution)
    }

    protected open fun onResolutionChanged(resolution: Size) {
        // Intentionally left blank :^)
    }
}