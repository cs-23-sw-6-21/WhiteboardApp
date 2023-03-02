package dk.scuffed.whiteboardapp.utils

/**
 * This class is used to store coordinates in a vector2 integer.
 * @property x the x coordinate for a point.
 * @property y the y coordinate for a point.
 */
class Vec2Int(val x: Int, val y: Int){
    fun subtact(vec: Vec2Int): Vec2Int{
        return Vec2Int(this.x-vec.x, this.y-vec.y)
    }
    fun additon(vec: Vec2Int): Vec2Int{
        return Vec2Int(this.x+vec.x, this.y+vec.y)
    }
    fun multiply(scale: Int): Vec2Int{
        return Vec2Int(this.x*scale, this.y*scale)
    }
    fun vec2IntToFloat():Vec2Float{
        return Vec2Float(this.x.toFloat(), this.y.toFloat())
    }
}