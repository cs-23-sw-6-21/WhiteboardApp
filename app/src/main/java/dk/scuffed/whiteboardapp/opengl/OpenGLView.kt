package dk.scuffed.whiteboardapp.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.output_stages.DrawPipelineStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

class OpenGLView(context: Context) : GLSurfaceView(context) {
    private val renderer: PipelinedOpenGLRenderer
    init {
        setEGLContextClientVersion(2)

        renderer = PipelinedOpenGLRenderer(context)

        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            DrawPipelineStage.next()
        }
        if (event?.action == MotionEvent.ACTION_MOVE) {
            DraggablePointsStage.dragPoint(Vec2Int(event.x.toInt(), height - event.y.toInt()))
        }
        return true
    }

    /**
     * Switches the switchablePipelineStage
     * @param bool is a Boolean to changes the switches value.
     */
    fun switchStage(bool: Boolean){
        renderer.switchStage(bool)
    }
}