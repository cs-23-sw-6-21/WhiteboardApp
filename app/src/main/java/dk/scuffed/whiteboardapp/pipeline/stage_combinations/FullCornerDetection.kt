package dk.scuffed.whiteboardapp.pipeline.stage_combinations

import android.content.Context
import android.graphics.Bitmap
import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.GLOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.FramebufferToBitmapStage
import dk.scuffed.whiteboardapp.pipeline.stages.bitmap_process_stages.OpenCVLineDetectionStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.BiggestSquareStage
import dk.scuffed.whiteboardapp.pipeline.stages.lines_stages.LinesAngleDiscriminatorStage
import dk.scuffed.whiteboardapp.pipeline.stages.pipeline_stages.ThreadedBitmapInputPointOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.points_stages.WeightedPointsStage
import dk.scuffed.whiteboardapp.utils.Vec2Int

/**
 * Does corner detection using hough line transforms and finding the biggest quadrilateral.
 * Preprocesses the image using canny edge detection.
 * @return points found
 */
internal fun fullCornerDetection(
    context: Context,
    inputStage: GLOutputStage,
    pipeline: IPipeline
): PointsOutputStage {

    val threadedBitmapInputPointOutputStage = ThreadedBitmapInputPointOutputStage(
        { pipeline ->
            val edges = fullCannyEdgeDetection(
                context,
                inputStage,
                pipeline
            )

            val edgesBitmapStage = FramebufferToBitmapStage(
                edges.frameBufferInfo,
                Bitmap.Config.ARGB_8888,
                pipeline
            )
        },
        { inputBitmapStage, pipeline ->
            val openCVLineDetectionStage = OpenCVLineDetectionStage(
                inputBitmapStage,
                325,
                10,
                pipeline,
                3.0,
                Math.PI / 135,
                100.0,
                Math.PI / 15
            )

            val verticalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
                openCVLineDetectionStage,
                -(Math.PI / 4.0f).toFloat(),
                (Math.PI / 4.0f).toFloat(),
                pipeline
            )

            val horizontalLinesAngleDiscriminatorStage = LinesAngleDiscriminatorStage(
                openCVLineDetectionStage,
                (Math.PI / 4.0f).toFloat(),
                (Math.PI / 2.0f + Math.PI / 4.0f).toFloat(),
                pipeline
            )

            val biggestSquareStage = BiggestSquareStage(
                horizontalLinesAngleDiscriminatorStage,
                verticalLinesAngleDiscriminatorStage,
                pipeline
            )

            val weightedCornerStage = WeightedPointsStage(
                biggestSquareStage,
                20,
                5.0f,
                pipeline
            )
        },
        pipeline,
        Vec2Int(0,0),
        Vec2Int(0,0),
        Vec2Int(0,0),
        Vec2Int(0,0)
    )

    return threadedBitmapInputPointOutputStage.myOutputPointsStage
}