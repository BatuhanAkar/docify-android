package com.batuscode.docunote.view

import android.graphics.Bitmap
import android.util.Log
import android.view.Surface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastForEach
import com.batuscode.docunote.viewmodel.DrawingAction
import com.batuscode.pdfium.OffsetWrapper
import com.batuscode.pdfium.PathData
import kotlin.collections.plus
import kotlin.math.abs



@Composable
fun DrawingScreen(draw: MutableState<Boolean>?, scale: MutableState<Float>?, offset: MutableState<Offset>?, page: Bitmap?, state: MutableState<DrawingState>, modifier: Modifier = Modifier) {
    // State'i remember ile tut



    DrawingCanvas(
        draw!!,
        scale!!,
        offset!!,
        page!!,
        paths = state.value.paths,
        currentPath = state.value.currentPath,
        onAction = { action ->
            when (action) {
                DrawingAction.OnClearCanvasClick -> onClearCanvasClick(state)
                is DrawingAction.OnDraw -> onDrawWithErase(state, action.offset)
                DrawingAction.OnNewPathStart -> onNewPathStart(state)
                DrawingAction.OnPathEnd -> onPathEnd(state)
                is DrawingAction.OnSelectColor -> onSelectColor(state, action.color)
            }
        } ,
        modifier = modifier
    )

}




data class DrawingState(
    var selectedColor: Color = Color.Black,
    val currentPath: PathData? = null,
    var paths: List<PathData> = emptyList(),
    var thickness: Float = 10f ,
    var isErasing: Boolean = false

)


@Composable
fun DrawingCanvas(
    draw: MutableState<Boolean>,
    scale: MutableState<Float> , offset: MutableState<Offset> ,
    page: Bitmap,
    paths: List<PathData>,
    currentPath: PathData?,
    onAction: (DrawingAction) -> Unit ,
    modifier: Modifier = Modifier
) {
    //val scale = scale.value
    val translationX = offset.value.x
    val translationY = offset.value.y
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current.density
    val screenWidthPx = configuration.screenWidthDp * density // Ekran genişliği (px cinsinden)
    val screenHeightPx = configuration.screenHeightDp * density // Ekran yüksekliği (px cinsinden)

    val scope = rememberCoroutineScope()
    Canvas(
        modifier = modifier
            //.background(Color.Red.copy(0.5f))
            .background(Color.Transparent)
            .clipToBounds()
            .aspectRatio(page.width.toFloat()/ page.height.toFloat())
            .then(
                if (draw.value){
                    Modifier
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    if (draw.value) {
                                        onAction(DrawingAction.OnNewPathStart)
                                    }
                                },
                                onDrag = {pointerInputChange, PointerInputChange ->
                                    if (draw.value) {
                                        onAction(DrawingAction.OnDraw(pointerInputChange.position))
                                    }
                                },
                                onDragEnd = {
                                    if (draw.value) {
                                        onAction(DrawingAction.OnPathEnd)
                                    }
                                }
                            )
                        }
                } else {
                    Modifier
                }
            )




    ) {


        drawImage(
            page.asImageBitmap(),
            dstSize = IntSize(size.width.toInt(), size.height.toInt()))

        paths.fastForEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
                thickness = pathData.thickness,
                scale.value
            )
        }
        currentPath?.let {
            drawPath(
                path = it.path,
                color = it.color,
                thickness = it.thickness,
                scale.value
            )
        }

        /*val canvasHeight = size.height
        val transformedPaths = paths.map { pathData ->
            pathData.copy(
                path = pathData.path.map { point ->
                    transformToBottomLeftOrigin(canvasHeight, point)
                }
            )
        }*/
    }
}

private fun DrawScope.drawPath(
    path: List<OffsetWrapper>,
    color: Color,
    thickness: Float = 10f ,
    scale: Float
) {
    val smoothedPath = Path().apply {
        if(path.isNotEmpty()) {
            moveTo(path.first().offset.x, path.first().offset.y)

            val smoothness = (5 / scale).toInt().coerceAtLeast(1)
            for(i in 1..path.lastIndex) {
                val from = path[i - 1].offset
                val to = path[i].offset
                val dx = abs(from.x - to.x)
                val dy = abs(from.y - to.y)
                if(dx >= smoothness || dy >= smoothness) {
                    quadraticTo(
                        x1 = (from.x + to.x) / 2f,
                        y1 = (from.y + to.y) / 2f,
                        x2 = to.x,
                        y2 = to.y
                    )
                }
            }
        }
    }
    drawPath(
        path = smoothedPath,
        color = color,
        style = Stroke(

            width = thickness,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun onSelectColor(state: MutableState<DrawingState>, color: Color) {
    state.value = state.value.copy(selectedColor = color)
}

private fun onPathEnd(state: MutableState<DrawingState>) {
    val currentPathData = state.value.currentPath ?: return
    state.value = state.value.copy(
        currentPath = null,
        paths = state.value.paths + currentPathData
    )
}

private fun onDrawWithErase(state: MutableState<DrawingState>, offset: Offset) {
    if (state.value.isErasing) {
        Log.d("earse" , "siliyor")
        // Silgi işlevi: Kullanıcının parmağının olduğu konumdaki path'i sil
        val pathsToKeep = state.value.paths.filterNot { pathData ->
            // Her pathData'nın içinde olan noktaları silgiyle karşılaştır
            pathData.path.any { point ->
                (abs(point.offset.x - offset.x) < 50f / 2 &&
                        abs(point.offset.y - offset.y) < 50f / 2)
            }
        }
        // Path listesine yalnızca silinmeyen path'leri ekle
        state.value = state.value.copy(paths = pathsToKeep)
    } else {
        // Silgi kapalıysa normal çizim işlemi

        onDraw(state, offset)
    }
}
private fun onNewPathStart(state: MutableState<DrawingState>) {
    state.value = state.value.copy(
        currentPath = PathData(
            id = System.currentTimeMillis().toString(),
            color = state.value.selectedColor,
            path = emptyList() ,
            thickness = state.value.thickness
        )
    )
}

private fun onDraw(state: MutableState<DrawingState>, offset: Offset) {
    val currentPathData = state.value.currentPath ?: return
    state.value = state.value.copy(
        currentPath = currentPathData.copy(
            path = currentPathData.path + OffsetWrapper(offset)
        )
    )
}
private fun onClearCanvasClick(state: MutableState<DrawingState>) {
    state.value = state.value.copy(
        currentPath = null,
        paths = emptyList()
    )
}

/**
 * Converts a top-left origin coordinate to a bottom-left origin coordinate.
 *
 * @param canvasHeight The height of the canvas.
 * @param point The point in top-left origin coordinates.
 * @return The transformed point in bottom-left origin coordinates.
 */
fun transformToBottomLeftOrigin(canvasHeight: Float, point: Offset): Offset {
    return Offset(point.x, canvasHeight - point.y)
}