package com.batuscode.docunote

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.opengl.EGL14
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.GLUtils
import android.util.Log
import com.batuscode.docunote.utils.PDFConverter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MyGLRenderer(
    val context: Context,
    val pdfConverter: PDFConverter,
    val uri: Uri,
    val bitmaps: List<Bitmap>?
) : GLSurfaceView.Renderer {

    private val textureHandle = IntArray(1)
    private var programHandle: Int = 0
    private var positionHandle: Int = 0
    private var texCoordHandle: Int = 0
    private var textureHandleUniform: Int = 0

    private val vertices = floatArrayOf(
        -1.0f, -1.0f, 0.0f, // Bottom Left
        1.0f, -1.0f, 0.0f,  // Bottom Right
        -1.0f, 1.0f, 0.0f,  // Top Left
        1.0f, 1.0f, 0.0f    // Top Right
    )

    private val texCoords = floatArrayOf(
        0.0f, 1.0f, // Bottom Left
        1.0f, 1.0f, // Bottom Right
        0.0f, 0.0f, // Top Left
        1.0f, 0.0f  // Top Right
    )

    private lateinit var vertexBuffer: FloatBuffer
    private lateinit var texCoordBuffer: FloatBuffer

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        Log.d("OpenGL", "Surface created, initializing OpenGL context")

        // Check if the OpenGL context is valid
        val context = EGL14.eglGetCurrentContext()
        if (context == EGL14.EGL_NO_CONTEXT) {
            Log.e("OpenGL", "No valid OpenGL context")
            return
        }

        // Proceed with shader and program initialization
        GLES20.glClearColor(1.0f, 1.0f, 1.0f, 1.0f)

        // Load shaders and create program
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, VERTEX_SHADER_CODE)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, FRAGMENT_SHADER_CODE)

        if (vertexShader == 0 || fragmentShader == 0) {
            Log.e("OpenGL", "Shader compilation failed")
            return
        }

        programHandle = GLES20.glCreateProgram()
        if (programHandle == 0) {
            Log.e("OpenGL", "Failed to create program")
            return
        }

        GLES20.glAttachShader(programHandle, vertexShader)
        GLES20.glAttachShader(programHandle, fragmentShader)
        GLES20.glLinkProgram(programHandle)

        // Check for linking errors
        val linkStatus = IntArray(1)
        GLES20.glGetProgramiv(programHandle, GLES20.GL_LINK_STATUS, linkStatus, 0)
        if (linkStatus[0] == 0) {
            val error = GLES20.glGetProgramInfoLog(programHandle)
            Log.e("OpenGL", "Program linking failed: $error")
            GLES20.glDeleteProgram(programHandle)
            return
        }

        // Get attribute and uniform locations
        positionHandle = GLES20.glGetAttribLocation(programHandle, "aPosition")
        texCoordHandle = GLES20.glGetAttribLocation(programHandle, "aTexCoord")
        textureHandleUniform = GLES20.glGetUniformLocation(programHandle, "uTexture")

        // Generate texture
        GLES20.glGenTextures(1, textureHandle, 0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        // Load bitmap into texture
        bitmaps?.firstOrNull()?.let { bitmap ->
            Log.d("OpenGL", "Loading bitmap into texture: ${bitmap.width}x${bitmap.height}")
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        } ?: run {
            Log.e("OpenGL", "Bitmap is null or empty")
        }

        // Initialize vertex and texture coordinate buffers
        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(vertices)
                position(0)
            }

        texCoordBuffer = ByteBuffer.allocateDirect(texCoords.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .apply {
                put(texCoords)
                position(0)
            }
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Use the program
        GLES20.glUseProgram(programHandle)
        checkGlError("glUseProgram")

        // Bind texture
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0])
        checkGlError("glBindTexture")

        // Enable vertex and texture coordinate arrays
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        checkGlError("glEnableVertexAttribArray")

        // Set vertex and texture coordinate data
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 0, texCoordBuffer)
        checkGlError("glVertexAttribPointer")

        // Draw the quad
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
        checkGlError("glDrawArrays")

        // Disable vertex and texture coordinate arrays
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
        checkGlError("glDisableVertexAttribArray")
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        val shader = GLES20.glCreateShader(type)
        if (shader == 0) {
            Log.e("ShaderError", "Failed to create shader of type $type")
            return 0
        }

        GLES20.glShaderSource(shader, shaderCode)
        GLES20.glCompileShader(shader)

        // Check for compilation errors
        val compileStatus = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0)
        if (compileStatus[0] == 0) {
            val error = GLES20.glGetShaderInfoLog(shader)
            Log.e("ShaderError", "Shader compilation failed: $error")
            GLES20.glDeleteShader(shader)
            return 0
        }

        return shader
    }

    private fun checkGlError(op: String) {
        val error = GLES20.glGetError()
        if (error != GLES20.GL_NO_ERROR) {
            Log.e("OpenGLError", "$op: glError $error")
        }
    }

    companion object {
        private const val VERTEX_SHADER_CODE = """
            attribute vec4 aPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = aPosition;
                vTexCoord = aTexCoord;
            }
        """

        private const val FRAGMENT_SHADER_CODE = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
            }
        """
    }
}