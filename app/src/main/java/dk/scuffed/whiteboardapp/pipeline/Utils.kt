package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import com.google.common.io.ByteStreams
import dk.scuffed.whiteboardapp.utils.Vec2Float
import kotlin.math.abs

fun readRawResource(context: Context, resourceId: Int): String {
    val stream =
        context.resources.openRawResource(resourceId)
    val string = String(ByteStreams.toByteArray(stream))
    stream.close()

    return string
}

// https://www.mathopenref.com/coordtrianglearea.html
fun areaOfTriangle(a: Vec2Float, b: Vec2Float, c: Vec2Float): Float {
    return abs((a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2.0f)
}
