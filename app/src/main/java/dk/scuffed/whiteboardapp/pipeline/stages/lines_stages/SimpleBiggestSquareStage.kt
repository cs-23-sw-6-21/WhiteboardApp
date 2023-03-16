package dk.scuffed.whiteboardapp.pipeline.stages.lines_stages

import dk.scuffed.whiteboardapp.pipeline.Pipeline
import dk.scuffed.whiteboardapp.pipeline.stages.LinesOutputStage
import dk.scuffed.whiteboardapp.pipeline.stages.PointsOutputStage
import dk.scuffed.whiteboardapp.utils.LineFloat
import dk.scuffed.whiteboardapp.utils.Vec2Float
import dk.scuffed.whiteboardapp.utils.Vec2Int
import java.util.*
import kotlin.math.abs

internal class SimpleBiggestSquareStage(private val horizontalLinesStage: LinesOutputStage, private val verticalLinesStage: LinesOutputStage, pipeline: Pipeline) :
    PointsOutputStage(pipeline, Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0), Vec2Int(0, 0)) {
    override fun update() {
        val topLine = LineFloat(Vec2Float(-1000f, 1000f), Vec2Float(1000f, 1000f))
        val bottomLine = LineFloat(Vec2Float(-1000f, -1000f), Vec2Float(1000f, -1000f))
        val leftLine = LineFloat(Vec2Float(-1000f, -1000f), Vec2Float(-1000f, 1000f))
        val rightLine = LineFloat(Vec2Float(1000f, 1000f), Vec2Float(1000f, -1000f))

        var smallestVert = LineFloat(Vec2Float(-10000f, -1f), Vec2Float(-10000f, 1f))
        var smallestVertX = Float.MAX_VALUE

        var largestVert = LineFloat(Vec2Float(10000f, -1f), Vec2Float(10000f, 1f))
        var largestVertX = Float.MIN_VALUE

        var smallestHor = LineFloat(Vec2Float(-1f, -10000f), Vec2Float(1f, 10000f))
        var smallestHorY = Float.MAX_VALUE

        var largestHor = LineFloat(Vec2Float(-1f, -10000f), Vec2Float(1f, 10000f))
        var largestHorY = Float.MIN_VALUE


        for (hor in 0 until horizontalLinesStage.lines.size) {
            val p1 = leftLine.intersect(horizontalLinesStage.lines[hor])
            val p2 = rightLine.intersect(horizontalLinesStage.lines[hor])

            if (p1 != null && p2 != null) {
                val avgy = (p1.y + p2.y) / 2.0f
                if (avgy < smallestHorY){
                    smallestHorY = avgy
                    smallestHor = horizontalLinesStage.lines[hor]
                }
                if (avgy > largestHorY){
                    largestHorY = avgy
                    largestHor = horizontalLinesStage.lines[hor]
                }
            }
        }
        for (hor in 0 until verticalLinesStage.lines.size) {
            val p1 = bottomLine.intersect(verticalLinesStage.lines[hor])
            val p2 = topLine.intersect(verticalLinesStage.lines[hor])

            if (p1 != null && p2 != null) {
                val avgx = (p1.x + p2.x) / 2.0f
                if (avgx < smallestVertX){
                    smallestVertX = avgx
                    smallestVert = verticalLinesStage.lines[hor]
                }
                if (avgx > largestVertX){
                    largestVertX = avgx
                    largestVert = verticalLinesStage.lines[hor]
                }
            }
        }



        val p1 = smallestVert.intersect(smallestHor)
        val p2 = smallestVert.intersect(largestHor)
        val p3 = largestVert.intersect(largestHor)
        val p4 = largestVert.intersect(smallestHor)

        if (p1 != null && p2 != null && p3 != null && p4 != null) {
            points[0] = p1.toVec2Int()
            points[1] = p2.toVec2Int()
            points[2] = p3.toVec2Int()
            points[3] = p4.toVec2Int()
        }

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
    fun convexHull(points: Array<Vec2Float>): Array<Vec2Float>? {
        val n = points.size
        // There must be at least 3 points
        if (n < 3) return null

        // Initialize Result
        val hull = Vector<Vec2Float>()

        // Find the leftmost point
        var l = 0
        for (i in 1 until n) if (points[i].x < points[l].x) l = i

        // Start from leftmost point, keep moving
        // counterclockwise until reach the start point
        // again. This loop runs O(h) times where h is
        // number of points in result or output.
        var p = l
        var q: Int
        do {
            // Add current point to result
            hull.add(points[p])

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
                if (orientation(points[p], points[i], points[q])
                    == 2
                ) q = i
            }

            // Now q is the most counterclockwise with
            // respect to p. Set p as q for next iteration,
            // so that q is added to result 'hull'
            p = q
        } while (p != l) // While we don't come to first
        // point

        /*
        var thing = "New points!\n"
        // Print Result
        for (temp: Vec2Float in hull) {
            thing +=
            (("(" + temp.x) + ", " +
                    temp.y) + ")\n"
        }
        Log.d("POINTS", thing)
         */

        return hull.toTypedArray()
    }

    // https://www.mathopenref.com/coordtrianglearea.html
    private fun areaOfTriangle(a: Vec2Float, b: Vec2Float, c: Vec2Float): Float {
        return abs((a.x * (b.y - c.y) + b.x * (c.y - a.y) + c.x * (a.y - b.y)) / 2.0f)
    }
}