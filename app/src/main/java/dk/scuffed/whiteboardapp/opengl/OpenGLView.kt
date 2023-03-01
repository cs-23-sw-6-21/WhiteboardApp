package dk.scuffed.whiteboardapp.opengl

import android.content.Context
import android.opengl.GLSurfaceView

class OpenGLView(context: Context) : GLSurfaceView(context) {
    init {
        setEGLContextClientVersion(2)

        val renderer = PipelinedOpenGLRenderer(context)

        setRenderer(renderer)
    }
}