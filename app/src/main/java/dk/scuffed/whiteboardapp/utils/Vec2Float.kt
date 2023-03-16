package dk.scuffed.whiteboardapp.utils

import kotlin.math.pow
import kotlin.math.sqrt

/**
 * This class is used to store coordinates in a vector2 float.
 * @property x the x coordinate for a point.
 * @property y the y coordinate for a point.
 */
class Vec2Float(val x: Float, val y: Float){
    fun subtact(vec: Vec2Float): Vec2Float{
        return Vec2Float(this.x-vec.x, this.y-vec.y)
    }
    fun additon(vec: Vec2Float): Vec2Float{
        return Vec2Float(this.x+vec.x, this.y+vec.y)
    }
    fun multiply(scale: Float): Vec2Float{
        return Vec2Float(this.x*scale, this.y*scale)
    }
    fun returnArray():FloatArray{
        return floatArrayOf(
            this.x, this.y, 0.0f,
        )
    }

    fun distance(to: Vec2Float): Float{
        return sqrt((to.x - this.x).pow(2f) + (to.y - this.y).pow(2f))
    }

    fun toVec2Int():Vec2Int{
        return Vec2Int(this.x.toInt(), this.y.toInt())
    }

    override fun toString(): String {
        return "($x, $y)"
    }
}