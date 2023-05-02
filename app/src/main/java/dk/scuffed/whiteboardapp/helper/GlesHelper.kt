package dk.scuffed.whiteboardapp.helper

object GlesHelper {
    external fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, type: Int)
}