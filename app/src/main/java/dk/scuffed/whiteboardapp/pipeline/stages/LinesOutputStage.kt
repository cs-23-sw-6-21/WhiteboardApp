package dk.scuffed.whiteboardapp.pipeline.stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.utils.LineFloat

internal abstract class LinesOutputStage(pipeline: Pipeline) : Stage(pipeline) {
    val lines = ArrayList<LineFloat>()
}