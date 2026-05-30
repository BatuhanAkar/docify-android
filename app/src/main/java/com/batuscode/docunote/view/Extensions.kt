package com.batuscode.docunote.view

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import com.batuscode.docunote.CreatePDFActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.manager.ActivityResultLauncherManager
import com.batuscode.docunote.model.ExtensionButton
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Extensions( onDissmis : () -> Unit , appViewModel: AppViewModel){
    val createButtons = listOf(
        ExtensionButton(id = 0 , name = stringResource(id = R.string.openpdf) , R.drawable.open_pdf_01) ,
        ExtensionButton(id = 1 , name = stringResource(id = R.string.createpdf) , R.drawable.create_pdf_01) ,
        ExtensionButton(id = 2 , name = stringResource(id = R.string.mergepdf) , R.drawable.merge_pdf_01 ) ,
        ExtensionButton(id = 3 , name = stringResource(id = R.string.splitpdf) , R.drawable.split_pdf_01) ,
    )
    rememberModalBottomSheetState()
    rememberLazyListState()
    Dialog( onDismissRequest = onDissmis ) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .wrapContentWidth()
                .wrapContentHeight() ,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp
        ) {


            LazyVerticalGrid (
                verticalArrangement = Arrangement.Center,
                horizontalArrangement = Arrangement.Center,
                contentPadding = PaddingValues(0.dp),
                columns = GridCells.Fixed(2) ,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(createButtons){
                        item -> ButtonView(button = item , appViewModel = appViewModel)
                }
            }

        }
    }

}

@Preview(showBackground = true)
@Composable
fun extsPreview(){
    val createButtons = listOf(
        ExtensionButton(id = 0 , name = stringResource(id = R.string.openpdf) , R.drawable.open_pdf_01) ,
        ExtensionButton(id = 1 , name = stringResource(id = R.string.createpdf) , R.drawable.create_pdf_01) ,
        ExtensionButton(id = 2 , name = stringResource(id = R.string.mergepdf) , R.drawable.merge_pdf_01 ) ,
        ExtensionButton(id = 3 , name = stringResource(id = R.string.splitpdf) , R.drawable.split_pdf_01) ,
    )
    DocuNoteTheme(darkTheme = true) {
        Dialog( onDismissRequest = {} ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .wrapContentWidth()
                    .wrapContentHeight() ,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 4.dp
            ) {


                LazyVerticalGrid (
                    verticalArrangement = Arrangement.Center,
                    horizontalArrangement = Arrangement.Center,
                    columns = GridCells.Fixed(2) ,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    items(createButtons){
                            item -> ButtonView(button = item , appViewModel = AppViewModel())
                    }
                }

            }
        }
    }
}

@Composable
fun ButtonView(button: ExtensionButton , appViewModel: AppViewModel){

    FontFamily(Font(R.font.bebas_neue))
    val infiniteTransition = rememberInfiniteTransition()
    val targetOffset = with (LocalDensity.current){ 1000.dp.toPx() }
    infiniteTransition.animateFloat(
        initialValue = 0f ,
        targetValue = targetOffset ,
        animationSpec = infiniteRepeatable(animation = tween(50000 , easing = LinearEasing) , repeatMode = RepeatMode.Reverse) ,
        label = "offset"
    )
    listOf(
        MaterialTheme.colorScheme.background, Color.LightGray
    )
    remember {
        mutableStateOf<Boolean>(false)
    }
    var scope = rememberCoroutineScope()
    val context = LocalContext.current
    val _extensionsOpen = appViewModel._extensionsOpen.collectAsState()

    Box (
        modifier = Modifier
            .background(Color.Transparent)
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .wrapContentWidth(Alignment.CenterHorizontally)
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
                                    ActivityResultLauncherManager.pickPDF(allowMultiplePick = false) { uri , FileNameWithOutExtension , _ ->

                                        val intent = Intent(MainActivity.context, PDFViewerActivity::class.java).apply {
                                            putExtra("fileUri", uri.toString())
                                            putExtra("fileDisplayName", FileNameWithOutExtension)
                                        }
                                        context.startActivity(intent)
                                    }
                                }

                                1 -> {
                                    // create pdf button
                                    Log.d("extensionfunc", "clicked to button 1 " + button.id)

                                    val intent = Intent(context, CreatePDFActivity::class.java)
                                    context.startActivity(intent)
                                }

                                2 -> {
                                    // merge pdf button

                                    ActivityResultLauncherManager.pickPDF(allowMultiplePick = true){ _,_,MutliplePDFFileUris ->
                                        if (MutliplePDFFileUris?.isNotEmpty() == true){
                                            MainActivity.mainActivityViewModel.update_onBackOperationDescription("Merge PDF'S")
                                            MainActivity.mainActivityViewModel.update_OnScreenMergePDF(true)
                                        }
                                    }
                                }

                                3 -> {
                                    //split pdf button

                                    ActivityResultLauncherManager.pickPDF(false){ uri , _ , _ ->
                                        if (uri != null){
                                            MainActivity.memUri.value = uri
                                            MainActivity.mainActivityViewModel.update_onBackOperationDescription("Split PDF")
                                            MainActivity.mainActivityViewModel.update_OnScreenSplitPDF(true)
                                        }
                                    }

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