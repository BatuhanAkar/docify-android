package com.batuscode.docunote.view

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
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.core.net.toUri
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.ExtensionButton
import com.batuscode.docunote.model.Menu
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Extensions(appViewModel: AppViewModel){
    val sheetState = rememberModalBottomSheetState()
    val menuListState = rememberLazyListState()
    ModalBottomSheet(
        onDismissRequest = {
            appViewModel.update_extensionsOpenState(false)
        } ,
        sheetState = sheetState ,
        containerColor = MaterialTheme.colorScheme.background,
        modifier = Modifier
            .fillMaxSize()
    ) {

        Flow(modalbottomsheetstate = sheetState , appViewModel = appViewModel)

    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Flow(modalbottomsheetstate: SheetState , appViewModel: AppViewModel){


    val createButtons = listOf(
        ExtensionButton(id = 0 , name = stringResource(id = R.string.openpdf) , R.drawable.open_pdf_01) ,
        ExtensionButton(id = 1 , name = stringResource(id = R.string.createpdf) , R.drawable.create_pdf_01) ,
        ExtensionButton(id = 2 , name = stringResource(id = R.string.mergepdf) , R.drawable.merge_pdf_01 ) ,
        ExtensionButton(id = 3 , name = stringResource(id = R.string.splitpdf) , R.drawable.split_pdf_01) ,

    )

  /*  val convertButtons = listOf(
        ExtensionButton(id= 4 , name = stringResource(id = R.string.pdftoword) , R.drawable.pdf_to_word_01) ,
        ExtensionButton(id = 5 , name = stringResource(id = R.string.wordtopdf) , R.drawable.word_to_pdf_01) ,
        ExtensionButton(id = 6 , name = stringResource(id = R.string.pdftoexcel) , R.drawable.pdf_to_excel_01),
        ExtensionButton(id = 7 , name = stringResource(id = R.string.exceltopdf) , R.drawable.excel_to_pdf_01),
        ExtensionButton(id = 8 , name = stringResource(id = R.string.pdftojpeg) , R.drawable.pdf_to_jpeg_01),
        ExtensionButton(id = 9 , name = stringResource(id = R.string.pdftopng) , R.drawable.pdf_to_png_01),
    )*/

    val menu = listOf(
        Menu(id = 0 , title = stringResource(R.string.create) , buttons = createButtons) ,

       // Menu(id = 1 , title = stringResource(R.string.convert) , buttons = convertButtons) ,

    )


    LazyColumn (
        contentPadding = PaddingValues(horizontal = 16.dp , vertical = 16.dp),
        modifier = Modifier
            .fillMaxSize()
            .zIndex(5f)
            .background(color = MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(createButtons){
                item -> ButtonFlow(button = item , modalbottomsheetstate = modalbottomsheetstate , appViewModel = appViewModel)
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GridItem(menu: Menu , modalbottomsheetstate: SheetState , appViewModel: AppViewModel) {
    val bebasFontFamily = FontFamily(Font(R.font.bebasneue_regular))
    val baseHegiht = 360.dp
    Box(
            modifier = Modifier
                .fillMaxSize()
                .height(if (menu.id == 0) baseHegiht else baseHegiht*2)
                .zIndex(1f)
                .background(color = MaterialTheme.colorScheme.background)
                ,
        contentAlignment = Alignment.TopCenter ,
    ) {
        Column (
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally ,

            modifier = Modifier
                .fillMaxSize()
        ) {
          /*  Row (
                verticalAlignment = Alignment.CenterVertically ,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .zIndex(1f)
            ) {
                if (menu.id == 0){
                    Image(
                        painter = painterResource(R.drawable.new_doc2_01) ,
                        contentDescription = "icon" ,
                        alignment = Alignment.Center ,
                        modifier = Modifier
                    )
                } else if (menu.id == 1){
                    Image(
                        painter = painterResource(R.drawable.convert_doc_01) ,
                        contentDescription = "icon" ,
                        alignment = Alignment.Center ,
                        modifier = Modifier
                            .wrapContentSize()
                    )
                }
                Text(
                    text = menu.title ,
                    textAlign = TextAlign.Left ,
                    style = TextStyle(
                        fontFamily = bebasFontFamily ,
                        fontWeight = FontWeight.Normal,
                        fontSize = 30.sp ,
                    ),
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .fillMaxWidth()
                        .zIndex(2f)
                )
            }

            HorizontalDivider(
                color = Color.Black,
                thickness = 2.dp,
                modifier = Modifier
                .width(170.dp)
                    .align(Alignment.Start)
                    .padding(bottom = 8.dp , start = 8.dp)
            )*/

            LazyColumn (

                modifier = Modifier
                    .fillMaxSize(),
                contentPadding = PaddingValues(2.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(menu.buttons) { item ->
                    ButtonFlow(button = item , modalbottomsheetstate = modalbottomsheetstate , appViewModel = appViewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ButtonFlow(button: ExtensionButton , modalbottomsheetstate: SheetState , appViewModel: AppViewModel){

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
    ElevatedCard(
        onClick = {
            ripple(bounded = true)
            scope.launch {
                if (modalbottomsheetstate.isVisible) {
                    modalbottomsheetstate.hide()

                    when (button.id) {
                        0 -> {
                            // open pdf button
                            Log.d("extensionfunc", "clicked to button 0 " + button.id)
                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
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
                                type = "*/*"
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
                                type = "*/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false)

                            }
                            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            MainActivity.splitDocumentLauncher.launch(intent)

                        }
                    }
                }
            }.invokeOnCompletion {
                if (!modalbottomsheetstate.isVisible){
                    appViewModel.update_extensionsOpenState(false)
                }
            }


        },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 3.dp , pressedElevation = 6.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        modifier = Modifier
            .padding(vertical = 8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp , vertical = 16.dp)
        ) {
            if (button.icon != 0){
                Image(
                    painter = painterResource(button.icon) ,
                    contentDescription = "icon" ,
                    alignment = Alignment.Center ,
                    modifier = Modifier
                        .size(64.dp)
                        .zIndex(1f)
                )
            }
            Text(
                text = button.name ,
                style = TextStyle(
                    fontFamily = bebasFontFamily ,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center ,
                    fontSize = 25.sp
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)

            )
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun PreviewExtensions(){
    DocuNoteTheme {
        val sheetState = rememberModalBottomSheetState()
        Flow(
            modalbottomsheetstate = sheetState , AppViewModel()
        )
    }
}