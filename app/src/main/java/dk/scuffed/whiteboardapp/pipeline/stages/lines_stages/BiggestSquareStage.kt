package dk.scuffed.whiteboardapp.pipeline.stages.lines_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.stages.LinesOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.QuadFloat
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import java.util.*
import kotlin.math.round

internal class BiggestSquareStage(
    private val horizontalLinesStage: LinesOutputStage,
    private val verticalLinesStage: LinesOutputStage,
    pipeline: IPipeline
) :
    PointsOutputStage(pipeline, Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0)) {

    private var OldPoints = Array<Array<Vec2Int>>(4) {Array<Vec2Int>(20) { Vec2Int(0, 0) } }
    private var pointIndex = 0


    override fun update() {
        var bestQuad = QuadFloat()
        var bestArea = 0f

        for (x1 in 0 until horizontalLinesStage.lines.size) {
            for (x2 in x1 + 1 until horizontalLinesStage.lines.size) {
                for (y1 in 0 until verticalLinesStage.lines.size) {
                    for (y2 in y1 + 1 until verticalLinesStage.lines.size) {
                        val lx1 = horizontalLinesStage.lines[x1]
                        val lx2 = horizontalLinesStage.lines[x2]
                        val ly1 = verticalLinesStage.lines[y1]
                        val ly2 = verticalLinesStage.lines[y2]

                        val p1 = lx1.intersect(ly1)
                        val p2 = lx1.intersect(ly2)
                        val p3 = lx2.intersect(ly1)
                        val p4 = lx2.intersect(ly2)

                        if (p1 != null && p2 != null && p3 != null && p4 != null) {
                            val quad = makeQuadConvex(QuadFloat(p1, p2, p3, p4))

                            val area = quad.area()
                            if (area > bestArea) {
                                bestQuad = quad
                                bestArea = area
                            }
                        }
                    }
                }
            }
        }

        // Use the last found quad if we don't detect any
        if (bestArea != 0f) {
            val fixedQuad = fixupQuad(bestQuad)

            OldPoints[0][pointIndex] = fixedQuad.a.toVec2Int()
            OldPoints[1][pointIndex] = fixedQuad.b.toVec2Int()
            OldPoints[2][pointIndex] = fixedQuad.c.toVec2Int()
            OldPoints[3][pointIndex] = fixedQuad.d.toVec2Int()

            pointIndex += 1

            if (pointIndex >= 20) {
                pointIndex = 0
            }

            points[0] = weightedAvgPoint(OldPoints[0])
            points[1] = weightedAvgPoint(OldPoints[1])
            points[2] = weightedAvgPoint(OldPoints[2])
            points[3] = weightedAvgPoint(OldPoints[3])

        }
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

        for (point in pointList){
            val distFromMean = point.toVec2Float().distance(Vec2Float(meanX.toFloat(), meanY.toFloat()))
            val weight = if (distFromMean <= 5.0) 1.0 else 1.0 / distFromMean
            sumX += weight * point.x.toDouble()
            sumY += weight * point.y.toDouble()
            weightSum += weight
        }

        return Vec2Int(round(sumX/weightSum).toInt(), round(sumY/weightSum).toInt())
    }

    // The point a should be the top left corner
    // The quad should be counter-clockwise
    private fun fixupQuad(quad: QuadFloat): QuadFloat {
        val points = ArrayList(listOf(*quad.toVec2FloatArray()))

        var bottomLeftPoint: Vec2Float? = null
        for (point in points) {
            if (bottomLeftPoint == null) {
                bottomLeftPoint = point
            } else if (point.x + point.y < bottomLeftPoint.x + bottomLeftPoint.y) {
                bottomLeftPoint = point
            }
        }

        points.remove(bottomLeftPoint)

        var bottomRightPoint: Vec2Float? = null
        for (point in points) {
            if (bottomRightPoint == null) {
                bottomRightPoint = point
            } else if ((-point.x) + point.y < (-bottomRightPoint.x) + bottomRightPoint.y) {
                bottomRightPoint = point
            }
        }

        points.remove(bottomRightPoint)

        var topRightPoint: Vec2Float? = null
        for (point in points) {
            if (topRightPoint == null) {
                topRightPoint = point
            } else if (point.x + point.y > topRightPoint.x + topRightPoint.y) {
                topRightPoint = point
            }
        }

        points.remove(topRightPoint)

        val topLeftPoint = points[0]

        return QuadFloat(topLeftPoint, bottomLeftPoint!!, bottomRightPoint!!, topRightPoint!!)
    }

    private fun makeQuadConvex(quad: QuadFloat): QuadFloat {
        val points = ArrayList(listOf(*quad.toVec2FloatArray()))

        val a = points[0]
        points.remove(a)

        val b = shortestDistance(a, points)
        points.remove(b)

        val c = shortestDistance(b, points)
        points.remove(c)

        val d = points[0]

        return QuadFloat(a, b, c, d)
    }

    private fun shortestDistance(point: Vec2Float, points: ArrayList<Vec2Float>): Vec2Float {
        var shortestDistance = Float.MAX_VALUE
        var shortestPoint = Vec2Float(0f, 0f)

        for (other in points) {
            val distance = point.distance(other)
            if (distance < shortestDistance) {
                shortestDistance = distance
                shortestPoint = other
            }
        }

        return shortestPoint
    }
}