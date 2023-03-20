package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Int
import kotlin.math.pow

/**
 * Creates and outputs 4 points that you can drag using dragPoint.
 */
internal class DraggablePointsStage(
    pipeline: IPipeline
    ) : PointsOutputStage(pipeline)
{
    companion object {
        private var instance: DraggablePointsStage? = null

        fun dragPoint(screenPosition: Vec2Int) {
            instance?.dragPoint(screenPosition)
        }
    }

    fun dragPoint(screenPosition: Vec2Int){
        val closestIndex = getClosest(screenPosition)

        points[closestIndex] = screenPosition
    }

    private fun getClosest(screenPosition: Vec2Int): Int {
        var closestSquareDist = Float.POSITIVE_INFINITY
        var closestIndex = -1
        for (p in points.indices) {
            val dist = (points[p].x - screenPosition.x).toFloat().pow(2) + (points[p].y - screenPosition.y).toFloat().pow(2)
            if (dist < closestSquareDist) {
                closestSquareDist = dist
                closestIndex = p
            }
        }
        return closestIndex
    }


    init {
        instance = this
        setInitialPoints()
    }


    private fun setInitialPoints() {
        points.addAll(arrayOf(
            Vec2Int(200, 800),
            Vec2Int(200, 200),
            Vec2Int(800, 200),
            Vec2Int(800, 800),
        ))
    }



    override fun update() {

    }
}