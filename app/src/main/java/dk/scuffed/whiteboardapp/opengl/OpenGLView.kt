package dk.scuffed.whiteboardapp.opengl

import android.content.Context
import android.opengl.GLSurfaceView
import android.util.Log
import android.view.MotionEvent
import dk.scuffed.whiteboardapp.pipeline.stages.DrawPipelineStage

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
        return true
    }
}