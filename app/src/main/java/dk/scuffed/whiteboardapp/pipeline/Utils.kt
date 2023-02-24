package dk.scuffed.whiteboardapp.pipeline

import android.content.Context
import com.google.common.io.ByteStreams

fun readRawResource(context: Context, resourceId: Int): String {
    val stream =
        context.resources.openRawResource(resourceId)
    val string = String(ByteStreams.toByteArray(stream))
    stream.close()

    return string
}
