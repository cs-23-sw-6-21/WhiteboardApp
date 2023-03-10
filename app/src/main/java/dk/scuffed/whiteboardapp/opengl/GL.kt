package dk.scuffed.whiteboardapp.opengl

import android.opengl.GLES20
import android.util.Log
import java.nio.Buffer
import java.nio.ByteBuffer
import java.security.InvalidParameterException


fun loadShader(type: Int, shaderCode: String): Int {
    val shader = glCreateShader(type)
    glShaderSource(shader, shaderCode)
    glCompileShader(shader)
    return shader
}

fun glEnable(i: Int) {
    GLES20.glEnable(i)
    logErrorIfAny("glEnable")
}

fun glDisable(i: Int) {
    GLES20.glDisable(i)
    logErrorIfAny("glDisable")
}

fun glClear(flag: Int) {
    GLES20.glClear(flag)
    logErrorIfAny("glClear")
}

fun glClearColor(red: Float, green: Float, blue: Float, alpha: Float) {
    GLES20.glClearColor(red, green, blue, alpha)
    logErrorIfAny("glClearColor")
}
fun glClearColorClear() {
    GLES20.glClearColor(0f, 0f, 0f, 0f)
    logErrorIfAny("glClearColor")
}
fun glClearColorError() {
    GLES20.glClearColor(1f, 0f, 1f, 1f)
    logErrorIfAny("glClearColor")
}


fun glViewport(x: Int, y: Int, width: Int, height: Int) {
    GLES20.glViewport(x, y, width, height)
    logErrorIfAny("glViewport")
}

fun glCreateShader(i: Int): Int {
    val shader = GLES20.glCreateShader(i)
    logErrorIfAny("glCreateShader")
    return shader
}

fun glShaderSource(shader: Int, shaderCode: String) {
    GLES20.glShaderSource(shader, shaderCode)
    logErrorIfAny("glShaderSource")
}

fun glCompileShader(shader: Int) {
    GLES20.glCompileShader(shader)
    logErrorIfAny("glCompileShader")

    val compiled = IntArray(1)
    GLES20.glGetShaderiv(
        shader,
        GLES20.GL_COMPILE_STATUS,
        compiled,
        0
    )

    if (compiled[0] == GLES20.GL_FALSE) {
        throw Exception(GLES20.glGetShaderInfoLog(shader))
    }
}

fun glCreateProgram(): Int {
    val program = GLES20.glCreateProgram()
    logErrorIfAny("glCreateProgram")
    return program
}

fun glAttachShader(program: Int, shader: Int) {
    GLES20.glAttachShader(program, shader)
    logErrorIfAny("glAttachShader")
}

fun glLinkProgram(program: Int) {
    GLES20.glLinkProgram(program)
    logErrorIfAny("glLinkProgram")

    val linkStatus = IntArray(1)
    GLES20.glGetProgramiv(
        program,
        GLES20.GL_LINK_STATUS,
        linkStatus,
        0
    )

    if (linkStatus[0] == GLES20.GL_FALSE) {
        throw Exception(GLES20.glGetProgramInfoLog(program))
    }
}

fun glUseProgram(program: Int) {
    GLES20.glUseProgram(program)
    logErrorIfAny("glUseProgram")
}

fun glGetAttribLocation(program: Int, attribName: String): Int {
    val location = GLES20.glGetAttribLocation(program, attribName)
    logErrorIfAny("glGetAttribLocation")
    return location
}

// https://docs.gl/es2/glEnableVertexAttribArray
fun glEnableVertexAttribArray(index: Int) {
    GLES20.glEnableVertexAttribArray(index)
    logErrorIfAny("glEnableVertexAttribArray")
}

// https://docs.gl/es2/glEnableVertexAttribArray
fun glDisableVertexAttribArray(index: Int) {
    GLES20.glDisableVertexAttribArray(index)
    logErrorIfAny("glDisableVertexAttribArray")
}

fun glVertexAttribPointer(
    index: Int,
    size: Int,
    type: Int,
    normalized: Boolean,
    stride: Int,
    ptr: Buffer
) {
    GLES20.glVertexAttribPointer(index, size, type, normalized, stride, ptr)
    logErrorIfAny("glVertexAttribPointer")
}

fun glGetUniformLocation(program: Int, uniformName: String): Int {
    val location = GLES20.glGetUniformLocation(program, uniformName)
    logErrorIfAny("glGetUniformLocation")
    return location
}

fun glUniform1f(location: Int, value: Float) {
    GLES20.glUniform1f(location, value)
    logErrorIfAny("glUniform1f")
}

fun glUniform1fv(location: Int, count: Int, value: FloatArray, offset: Int) {
    GLES20.glUniform1fv(location, count, value, offset)
    logErrorIfAny("glUniform1fv")
}

fun glUniform2f(location: Int, x: Float, y: Float) {
    GLES20.glUniform2f(location, x, y)
    logErrorIfAny("glUniform2f")
}

fun glUniform4f(location: Int, r: Float, g: Float, b: Float, a: Float) {
    GLES20.glUniform4f(location, r, g, b, a)
    logErrorIfAny("glUniform4f")
}

fun glUniform4fv(location: Int, count: Int, buffer: FloatArray, offset: Int) {
    GLES20.glUniform4fv(location, count, buffer, offset)
    logErrorIfAny("glUniform4fv")
}

fun glDrawArrays(mode: Int, first: Int, count: Int) {
    GLES20.glDrawArrays(mode, first, count)
    logErrorIfAny("glDrawArrays")
}

fun glDrawElements(mode: Int, count: Int, type: Int, buffer: Buffer) {
    GLES20.glDrawElements(mode, count, type, buffer)
    logErrorIfAny("glDrawElements")
}

fun glGenTextures(n: Int, textures: IntArray, offset: Int) {
    GLES20.glGenTextures(n, textures, offset)
    logErrorIfAny("glGenTextures")
}

fun glGenTexture(): Int {
    val textures = intArrayOf(999)
    glGenTextures(textures.size, textures, 0)
    return textures[0]
}

fun glBindTexture(target: Int, texture: Int)  {
    GLES20.glBindTexture(target, texture)
    logErrorIfAny("glBindTexture")
}

fun glTexParameteri(target: Int, pname: Int, param: Int) {
    GLES20.glTexParameteri(target, pname, param)
    logErrorIfAny("glTexParameteri")
}

fun glUniform1i(location: Int, target: Int) {
    GLES20.glUniform1i(location, target)
    logErrorIfAny("glUniform1i")
}

fun glActiveTexture(texture: Int) {
    GLES20.glActiveTexture(texture)
    logErrorIfAny("glActiveTexture")
}

fun glTexImage2D(target: Int, level: Int, format: Int, width: Int, height: Int, type: Int, data: ByteBuffer?) {
    GLES20.glTexImage2D(target, level, format, width, height, 0, format, type, data)
    logErrorIfAny("glTexImage2D")
}

fun glReadPixels(x: Int, y: Int, width: Int, height: Int, format: Int, byteBuffer: ByteBuffer){
    assert(width * height * bytesPerPixel(format) <= byteBuffer.capacity())
    GLES20.glReadPixels(x, y, width, height, format, GLES20.GL_UNSIGNED_BYTE, byteBuffer)
    logErrorIfAny("glReadPixels")
}

fun bytesPerPixel(textureFormat: Int): Int {
    return when (textureFormat) {
        GLES20.GL_RGBA -> 4
        GLES20.GL_RGB -> 3
        GLES20.GL_ALPHA -> 1
        else -> throw InvalidParameterException("format")
    }
}

fun glGenFramebuffers(count: Int, array: IntArray, offset: Int) {
    GLES20.glGenFramebuffers(count, array, offset)
    logErrorIfAny("glGenFramebuffers")
}

fun glGenFramebuffer(): Int {
    val framebuffers = intArrayOf(999)
    glGenFramebuffers(framebuffers.size, framebuffers, 0)
    return framebuffers[0]
}

fun glBindFramebuffer(fboHandle: Int) {
    GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, fboHandle)
    logErrorIfAny("glBindFramebuffer")
}

fun glFramebufferTexture2D(attachment: Int, textureTarget: Int, textureHandle: Int) {
    GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, attachment, textureTarget, textureHandle, 0)
    logErrorIfAny("glFramebufferTexture")
}

private fun logErrorIfAny(funcname: String) {
    var error = GLES20.glGetError()
    while (error != 0) {
        Log.e("OpenGL", funcname + ": " + error + ": " + errorToString(error))
        error = GLES20.glGetError()
    }
}

private fun errorToString(error: Int): String {
    return when (error) {
        GLES20.GL_INVALID_ENUM -> "GL_INVALID_ENUM"
        GLES20.GL_INVALID_VALUE -> "GL_INVALID_VALUE"
        GLES20.GL_INVALID_OPERATION -> "GL_INVALID_OPERATION"
        GLES20.GL_OUT_OF_MEMORY -> "GL_OUT_OF_MEMORY"
        GLES20.GL_INVALID_FRAMEBUFFER_OPERATION -> "GL_INVALID_FRAMEBUFFER_OPERATION"
        else -> "UNKNOWN ERROR"
    }
}