package dk.scuffed.whiteboardapp.pipeline.stages.lines_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.LinesOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import kotlin.math.abs

internal class BiggestSquareStage(private val horizontalLinesStage: LinesOutputStage, private val verticalLinesStage: LinesOutputStage, pipeline: Pipeline) :
    PointsOutputStage(pipeline, Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0)) {

    private val intersectionArray = arrayOf(Vec2Float(0f, 0f), Vec2Float(0f, 0f), Vec2Float(0f, 0f), Vec2Float(0f, 0f))
    private val hullArray = arrayOf(Vec2Float(0f, 0f), Vec2Float(0f, 0f), Vec2Float(0f, 0f), Vec2Float(0f, 0f))
    override fun update() {
        var bestA = Vec2Float(0f,0f)
        var bestB = Vec2Float(0f,0f)
        var bestC = Vec2Float(0f,0f)
        var bestD = Vec2Float(0f,0f)
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
                            intersectionArray[0] = p1
                            intersectionArray[1] = p2
                            intersectionArray[2] = p3
                            intersectionArray[3] = p4
                            val foundSquare = convexHull()
                            if (foundSquare) {
                                val a = hullArray[0]
                                val b = hullArray[1]
                                val c = hullArray[2]
                                val d = hullArray[3]

                                val area = areaOfTriangle(a, b, c) + areaOfTriangle(a, c, d)

                                if (area > bestArea) {
                                    bestA = a
                                    bestB = b
                                    bestC = c
                                    bestD = d
                                    bestArea = area
                                }
                            }
                        }
                    }
                }
            }
        }

        points[0] = bestA.toVec2Int()
        points[1] = bestB.toVec2Int()
        points[2] = bestC.toVec2Int()
        points[3] = bestD.toVec2Int()
    }

    // To find orientation of ordered triplet (p, q, r).
    // The function returns following values
    // 0 --> p, q and r are collinear
    // 1 --> Clockwise
    // 2 --> Counterclockwise
    fun orientation(p: Vec2Float, q: Vec2Float, r: Vec2Float): Int {
        val v = (q.y - p.y) * (r.x - q.x) -
                (q.x - p.x) * (r.y - q.y)
        if (v == 0.0f) return 0 // collinear
        return if (v > 0) 1 else 2 // clock or counterclock wise
    }

    // Prints convex hull of a set of n points.
    fun convexHull(): Boolean {
        val n = intersectionArray.size

        // Initialize Result

        // Find the leftmost point
        var l = 0
        for (i in 1 until n) if (intersectionArray[i].x < intersectionArray[l].x) l = i

        // Start from leftmost point, keep moving
        // counterclockwise until reach the start point
        // again. This loop runs O(h) times where h is
        // number of points in result or output.
        var p = l
        var q: Int
        var x = 0
        do {
            // Add current point to result
            hullArray[x++] = intersectionArray[p]

            // Search for a point 'q' such that
            // orientation(p, q, x) is counterclockwise
            // for all points 'x'. The idea is to keep
            // track of last visited most counterclock-
            // wise point in q. If any point 'i' is more
            // counterclock-wise than q, then update q.
            q = (p + 1) % n
            for (i in 0 until n) {
                // If i is more counterclockwise than
                // current q, then update q
                if (orientation(intersectionArray[p], intersectionArray[i], intersectionArray[q])
                    == 2
                ) q = i
            }

            // Now q is the most counterclockwise with
            // respect to p. Set p as q for next iteration,
            // so that q is added to result 'hull'
            p = q
        } while (p != l) // While we don't come to first

        return x == 4
    }

    // https://www.mathopenref.com/coordtrianglearea.html
    private fun areaOfTriangle(a: Vec2Float, b: Vec2Float, c: Vec2Float): Float {
        return abs((a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2.0f)
    }
}