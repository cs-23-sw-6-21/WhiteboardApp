package dk.scuffed.whiteboardapp.pipeline

import android.os.Environment
import android.os.Looper
import java.io.File
import java.io.OutputStreamWriter

object CSVWriter {

    var recordTimings = true

    val useGlFinish = true

    var frameCounter = 0

    val numberOfFrames = 1000

    val MainWriter : OutputStreamWriter

    init {
        val dir = Environment.getExternalStorageDirectory()

        val mainFile = File(dir, "Latency-Data.csv")

        mainFile.createNewFile()

        MainWriter = mainFile.writer()
    }

}