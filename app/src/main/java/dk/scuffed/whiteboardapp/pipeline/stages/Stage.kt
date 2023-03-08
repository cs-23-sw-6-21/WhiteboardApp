package dk.scuffed.whiteboardapp.pipeline

import android.util.Log

/**
 * Baseclass for stages.
 * Manages updates and connection to the pipeline.
 */
internal abstract class Stage(pipeline: Pipeline) {

    private val name: String

    init {
        pipeline.addStage(this)
        name = this.javaClass.name
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
}