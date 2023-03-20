package dk.scuffed.whiteboardapp.pipeline.stages.lines_stages

import dk.scuffed.whiteboardapp.pipeline.IPipeline
import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.LinesOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.QuadFloat
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import java.util.*

internal class BiggestSquareStage(private val horizontalLinesStage: LinesOutputStage, private val verticalLinesStage: LinesOutputStage, pipeline: IPipeline) :
    PointsOutputStage(pipeline, Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0,0), Vec2Int(0,0)) {

    override fun update() {
        var bestQuad = QuadFloat()
        var bestArea = 0f

        for (x1 in 0 until horizontalLinesStage.lines.size) {
            for (x2 in x1+1 until horizontalLinesStage.lines.size) {
                for (y1 in 0 until verticalLinesStage.lines.size) {
                    for (y2 in y1+1 until verticalLinesStage.lines.size) {
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

            points[0] = fixedQuad.a.toVec2Int()
            points[1] = fixedQuad.b.toVec2Int()
            points[2] = fixedQuad.c.toVec2Int()
            points[3] = fixedQuad.d.toVec2Int()
        }
    }

    // The point a should be the top left corner
    // The quad should be counter-clockwise
    private fun fixupQuad(quad: QuadFloat): QuadFloat {
        val points = ArrayList(listOf(*quad.toVec2FloatArray()))

        var bottomLeftPoint: Vec2Float? = null
        for (point in points) {
            if (bottomLeftPoint == null) {
                bottomLeftPoint = point
            }
            else if (point.x + point.y < bottomLeftPoint.x + bottomLeftPoint.y) {
                bottomLeftPoint = point
            }
        }

        points.remove(bottomLeftPoint)

        var bottomRightPoint: Vec2Float? = null
        for (point in points) {
            if (bottomRightPoint == null) {
                bottomRightPoint = point
            }
            else if ((-point.x) + point.y < (-bottomRightPoint.x) + bottomRightPoint.y ) {
                bottomRightPoint = point
            }
        }

        points.remove(bottomRightPoint)

        var topRightPoint: Vec2Float? = null
        for (point in points) {
            if (topRightPoint == null) {
                topRightPoint = point
            }
            else if (point.x + point.y > topRightPoint.x + topRightPoint.y) {
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