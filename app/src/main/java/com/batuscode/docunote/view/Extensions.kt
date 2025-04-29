package com.batuscode.docunote.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.ExtensionButton
import com.batuscode.docunote.model.Menu
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.AssetPacksUtil
import com.batuscode.docunote.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Extensions( onDissmis : () -> Unit , appViewModel: AppViewModel){
    val createButtons = listOf(
        ExtensionButton(id = 4 , name = "Summarize Document with AI",R.drawable.ai_document_extension_file_format_icon),
        ExtensionButton(id = 0 , name = stringResource(id = R.string.openpdf) , R.drawable.open_pdf_01) ,
        ExtensionButton(id = 1 , name = stringResource(id = R.string.createpdf) , R.drawable.create_pdf_01) ,
        ExtensionButton(id = 2 , name = stringResource(id = R.string.mergepdf) , R.drawable.merge_pdf_01 ) ,
        ExtensionButton(id = 3 , name = stringResource(id = R.string.splitpdf) , R.drawable.split_pdf_01) ,
    )
    val sheetState = rememberModalBottomSheetState()
    val menuListState = rememberLazyListState()
    Dialog( onDismissRequest = onDissmis ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight() ,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp
        ) {


            LazyVerticalGrid (
                columns = GridCells.Fixed(3)
            ) {
                items(createButtons){
                        item -> ButtonView(button = item , appViewModel = appViewModel)
                }
            }

        }
    }

}

@Composable
fun ButtonView(button: ExtensionButton , appViewModel: AppViewModel){

    val bebasFontFamily = FontFamily(Font(R.font.bebas_neue))
    val infiniteTransition = rememberInfiniteTransition()
    val targetOffset = with (LocalDensity.current){ 1000.dp.toPx() }
    val offset = infiniteTransition.animateFloat(
        initialValue = 0f ,
        targetValue = targetOffset ,
        animationSpec = infiniteRepeatable(animation = tween(50000 , easing = LinearEasing) , repeatMode = RepeatMode.Reverse) ,
        label = "offset"
    )
    val brushColors = listOf(
        MaterialTheme.colorScheme.background, Color.LightGray
    )
    var touchEffect = remember {
        mutableStateOf<Boolean>(false)
    }
    var scope = rememberCoroutineScope()
    val context = LocalContext.current
    val _extensionsOpen = appViewModel._extensionsOpen.collectAsState()

    Box (
        modifier = Modifier
            .background(Color.Transparent)
            .padding(vertical = 8.dp)
            .clickable(
                enabled = if (button.id == 4) MainActivity.waitinit.value else true ,
                role = Role.Button ,
                onClickLabel = "recentlyDocument" ,
                onClick = {

                    ripple(bounded = true)
                    scope.launch {
                        if (_extensionsOpen.value) {
                            appViewModel.update_extensionsOpenState(false)

                            when (button.id) {
                                0 -> {
                                    // open pdf button
                                    Log.d("extensionfunc", "clicked to button 0 " + button.id)
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/pdf"
                                    }
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

                                    // MainActivity.mainActivity.startActivityForResult(intent, 2)
                                    MainActivity.openDocumentLauncher.launch(intent)
                                }

                                1 -> {
                                    // create pdf button
                                    Log.d("extensionfunc", "clicked to button 1 " + button.id)

                                    val intent = Intent(context, CreatePDFActivity::class.java)
                                    context.startActivity(intent)
                                }

                                2 -> {
                                    // merge pdf button
                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                                    }
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    MainActivity.mergeDocumentLauncher.launch(intent)
                                }

                                3 -> {
                                    //split pdf button

                                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                        addCategory(Intent.CATEGORY_OPENABLE)
                                        type = "application/pdf"
                                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

                                    }
                                    intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                    MainActivity.splitDocumentLauncher.launch(intent)

                                }

                                4 -> {

                                    AssetPacksUtil.fromExtensions.value = true
                                    AssetPacksUtil.isPacksInstalled()
                                    /*if (ModelUtil.isModuleInstalled(context,"docifyai")){
                                        Log.d("dynamicModules" , "docifyai module is not installed...")
                                        ModelUtil.downloadModule(context)
                                    } else {
                                        Log.d("dynamicModules" , "docifyai module is installed...")
                                        ModelUtil.startModuleLauncherActivity(context,"com.batuscode.docifyai.DocifyAI")
                                    }*/
                                    /*val intent = Intent(context, SummfyAI::class.java)
                                    context.startActivity(intent)*/
                                }


                            }
                        }
                    }.invokeOnCompletion {
                        appViewModel.update_extensionsOpenState(false)
                    }
                }
            )
    ){

        Box(
            modifier = Modifier
                .wrapContentSize()
                .padding(horizontal = 8.dp , vertical = 8.dp) ,
            contentAlignment = Alignment.Center
        ) {

            Surface (
                shape = CircleShape ,
                color = Color.LightGray.copy(0.5f) ,
                modifier = Modifier
                    .size(60.dp)
            ){
                Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.wrapContentSize()
                ) {

                    if (button.icon != 0){
                        Image(
                            painter = painterResource(button.icon) ,
                            contentDescription = "icon" ,
                            alignment = Alignment.Center ,
                            modifier = Modifier
                                .size(32.dp)
                                .zIndex(1f)
                        )
                    }

                }
            }
           /* Text(
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = button.name ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )*/
        }
    }


}

@SuppressLint("ViewModelConstructorInComposable")
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewExtensions(){
    DocuNoteTheme {
    }
}