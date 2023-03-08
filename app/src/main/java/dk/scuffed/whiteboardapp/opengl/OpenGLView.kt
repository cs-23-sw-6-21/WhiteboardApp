package dk.scuffed.whiteboardapp.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.view.MotionEvent
import dk.scuffed.whiteboardapp.pipeline.stages.DraggablePointsStage
import dk.scuffed.whiteboardapp.pipeline.stages.DrawPipelineStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

class OpenGLView(context: Context) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)

        val renderer = PipelinedOpenGLRenderer(context)

        setRenderer(renderer)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            DrawPipelineStage.next()
        }
        if (event?.action == MotionEvent.ACTION_MOVE) {
            DraggablePointsStage.dragPoint(Vec2Int(event.x.toInt(), height-event.y.toInt()))
        }
        return true
    }
}