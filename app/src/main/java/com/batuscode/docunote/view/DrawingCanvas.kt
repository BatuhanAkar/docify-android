package com.batuscode.docunote.view

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import com.batuscode.docunote.viewmodel.DrawingAction
import com.tom_roush.pdfbox.pdmodel.common.PDRectangle
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlin.collections.plus
import kotlin.math.abs



@Composable
fun DrawingScreen(state: MutableState<DrawingState> , modifier: Modifier = Modifier) {
    // State'i remember ile tut

    DrawingCanvas(
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

data class PathData(
    val id: String,
    var color: Color,
    val path: List<Offset>,
    val thickness: Float = 10f
)


@Composable
fun DrawingCanvas(
    paths: List<PathData>,
    currentPath: PathData?,
    onAction: (DrawingAction) -> Unit ,
    modifier: Modifier = Modifier
) {




    Canvas(
        modifier = modifier
            .clipToBounds()
            .aspectRatio(PDRectangle.A4.width / PDRectangle.A4.height)
            .pointerInput(true) {

                detectDragGestures(
                    onDragStart = {
                        onAction(DrawingAction.OnNewPathStart)
                    },
                    onDragEnd = {
                        onAction(DrawingAction.OnPathEnd)
                    },
                    onDrag = { change, _ ->
                        onAction(DrawingAction.OnDraw(change.position))
                    },
                    onDragCancel = {
                        onAction(DrawingAction.OnPathEnd)
                    },
                )
            }
    ) {
        paths.fastForEach { pathData ->
            drawPath(
                path = pathData.path,
                color = pathData.color,
                thickness = pathData.thickness
            )
        }
        currentPath?.let {
            drawPath(
                path = it.path,
                color = it.color,
                thickness = it.thickness
            )
        }
    }
}

private fun DrawScope.drawPath(
    path: List<Offset>,
    color: Color,
    thickness: Float = 10f
) {
    val smoothedPath = Path().apply {
        if(path.isNotEmpty()) {
            moveTo(path.first().x, path.first().y)

            val smoothness = 5
            for(i in 1..path.lastIndex) {
                val from = path[i - 1]
                val to = path[i]
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
                (abs(point.x - offset.x) < 50f / 2 &&
                        abs(point.y - offset.y) < 50f / 2)
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
            path = currentPathData.path + offset
        )
    )
}
private fun onClearCanvasClick(state: MutableState<DrawingState>) {
    state.value = state.value.copy(
        currentPath = null,
        paths = emptyList()
    )
}