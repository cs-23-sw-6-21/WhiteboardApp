package dk.scuffed.whiteboardapp.utils

interface IVec<TVec, TUnderlying> {
    operator fun plus(other: TVec): TVec
    operator fun minus(other: TVec): TVec
    operator fun times(other: TUnderlying): TVec
    operator fun div(other: TUnderlying): TVec

    fun distance(other: TVec): Float
}