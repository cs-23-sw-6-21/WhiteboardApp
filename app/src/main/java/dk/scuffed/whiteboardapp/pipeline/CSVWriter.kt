package dk.scuffed.whiteboardapp.pipeline

import android.os.Environment
import java.io.File
import java.io.OutputStreamWriter

object CSVWriter {

    var recordStageTimings = true

    var recordOverallTimings = false

    val useGlFinish = true

    var frameCounter = 0

    val numberOfFrames = 1100

    val MainWriter : OutputStreamWriter

    init {
        val dir = Environment.getExternalStorageDirectory()

        val mainFile = File(dir, "Latency-Data.csv")

        mainFile.createNewFile()

        MainWriter = mainFile.writer()
    }

}