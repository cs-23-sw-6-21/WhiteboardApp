package dk.scuffed.whiteboardapp.utils

class LineInt(val startPoint: Vec2Int, val endpoint: Vec2Int) {
    fun toLineFloat(): LineFloat {
        return LineFloat(startPoint.toVec2Float(), endpoint.toVec2Float())
    }
}