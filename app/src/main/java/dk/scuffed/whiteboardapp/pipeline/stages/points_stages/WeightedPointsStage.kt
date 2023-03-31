package dk.scuffed.whiteboardapp.pipeline.stages.points_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import kotlin.math.round

internal class WeightedPointsStage(
    private val inputPoints: PointsOutputStage,
    private val historySize: Int,
    private val weightThreshold: Float,
    pipeline: IPipeline)
    : PointsOutputStage(pipeline, Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0))
{
    private var oldPoints = Array<Array<Vec2Int>>(4) {Array<Vec2Int>(historySize) { Vec2Int(0, 0) } }
    private var pointIndex = 0


    override fun update() {
        oldPoints[0][pointIndex] = inputPoints.points[0]
        oldPoints[1][pointIndex] = inputPoints.points[1]
        oldPoints[2][pointIndex] = inputPoints.points[2]
        oldPoints[3][pointIndex] = inputPoints.points[3]

        pointIndex += 1

        if (pointIndex >= historySize) {
            pointIndex = 0
        }

        points[0] = weightedAvgPoint(oldPoints[0])
        points[1] = weightedAvgPoint(oldPoints[1])
        points[2] = weightedAvgPoint(oldPoints[2])
        points[3] = weightedAvgPoint(oldPoints[3])
    }

    private fun weightedAvgPoint(pointList: Array<Vec2Int>) : Vec2Int
    {
        var sumX = 0.0
        var sumY = 0.0

        for (point in pointList)
        {
            sumX += point.x
            sumY += point.y
        }

        var meanX = sumX / pointList.size
        var meanY = sumY / pointList.size

        var weightSum = 0.0
        sumX = 0.0
        sumY = 0.0

        // Loop goes through a list of points and calculates their distance from the mean position of the list.
        // The distance is compared to a threshold and used to calculate a weight, it its not within the threshold.
        // weighted-XY and total-weight values are summed to be used for calculating the new weighted average of all the points.
        for (point in pointList){
            val distFromMean = point.toVec2Float().distance(Vec2Float(meanX.toFloat(), meanY.toFloat()))
            val weight = if (distFromMean <= weightThreshold) 1.0 else 1.0 / distFromMean
            sumX += weight * point.x.toDouble()
            sumY += weight * point.y.toDouble()
            weightSum += weight
        }

        return Vec2Int(round(sumX/weightSum).toInt(), round(sumY/weightSum).toInt())
    }

}