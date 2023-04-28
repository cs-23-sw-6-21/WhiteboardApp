package dk.scuffed.whiteboardapp.pipeline

import android.os.Environment
import java.io.File
import java.io.OutputStreamWriter

object CSVWriter {

    var recordTimings = true

    val useGlFinish = true

    var frameCounter = 0

    val MainWriter : OutputStreamWriter

    val CornerDetectionWriter : OutputStreamWriter

    init {
        val dir = Environment.getExternalStorageDirectory()

        val mainFile = File(dir, "Latency-Data.csv")
        val cornerDetectionFile = File(dir, "CornerDetection-Latency-Data.csv")

        mainFile.createNewFile()
        cornerDetectionFile.createNewFile()

        MainWriter = mainFile.writer()
        MainWriter.write("")

        CornerDetectionWriter = mainFile.writer()
        MainWriter.write("")
    }

}