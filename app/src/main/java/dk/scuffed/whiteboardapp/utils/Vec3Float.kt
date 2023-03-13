package dk.scuffed.whiteboardapp.utils

/**
 * This class is used to store coordinates in a vector2 float.
 * @property x the x coordinate for a point.
 * @property y the y coordinate for a point.
 */
class Vec3Float(val x: Float, val y: Float, val z: Float){
    fun subtact(vec: Vec3Float): Vec3Float{
        return Vec3Float(this.x-vec.x, this.y-vec.y, this.z-vec.z)
    }
    fun additon(vec: Vec3Float): Vec3Float{
        return Vec3Float(this.x+vec.x, this.y+vec.y, this.z+vec.z)
    }
    fun multiply(scale: Float): Vec3Float{
        return Vec3Float(this.x*scale, this.y*scale, this.z*scale)
    }
    fun returnArray():FloatArray{
        return floatArrayOf(
            this.x, this.y, this.z,
        )
    }
}