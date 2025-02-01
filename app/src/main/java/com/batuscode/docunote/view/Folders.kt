package com.batuscode.docunote.view

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.paddingFrom
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.RadialGradientShader
import androidx.compose.ui.graphics.Shader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.AlignmentLine
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.batuscode.docunote.FolderScopeActivity
import com.batuscode.docunote.MainActivity

@SuppressLint("ResourceAsColor")
@Composable
fun Folders(appViewModel: AppViewModel){
    val dummyList = List(20) { "Item #${it + 1}" }
   /* val folderlist = appViewModel.folders

    val folder = Folder( 0 ,"Downloads" , R.drawable.folder_icon_4_01)
    val folder1 = Folder( 1 , "Matematik" , R.drawable.folder_icon_4_01)
    val folder2 = Folder( 2 , "Coğrafya" , R.drawable.folder_icon_4_01)
    val folder3 = Folder( 3 , "Kimya" , R.drawable.folder_icon_4_01)
    val folder4 = Folder( 4 , "Türk Dili ve Edebiyatı" , R.drawable.folder_icon_4_01)
    appViewModel.loadFolders(folder)
    appViewModel.loadFolders(folder1)
    appViewModel.loadFolders(folder2)
    appViewModel.loadFolders(folder3)
    appViewModel.loadFolders(folder4)*/


    var exList = remember {
        mutableListOf<Folder>()
    }


    val folder = Folder( 0 ,"Kimya" , R.drawable.folder_icon_4_01)
    val folder1 = Folder( 1 , "Sınav" , R.drawable.folder_icon_4_01)

    exList.add(folder)
    exList.add(folder1)

    val folders = appViewModel.folders.collectAsState()
    Column(
        modifier = Modifier

    ) {
        Text(
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            text = stringResource(R.string.documents),
            color = Color.White,
            modifier = Modifier
                .wrapContentSize()
                .padding(start = 40.dp , end = 40.dp , top = 10.dp)
        )


        if (folders.value.isNotEmpty()){
            LazyRow (
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .requiredHeight(200.dp)
            ) {
                items(folders.value) { item ->
                    ListItem(folder = item)
                }

            }
        } else {
            Box(
                contentAlignment = Alignment.Center ,
                modifier = Modifier

                    .requiredHeight(200.dp)
            ) {
                /*
                Row(
                    modifier = Modifier
                        .fillMaxWidth()


                ) {
                    exList.forEach {
                        item ->

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier

                        ) {

                            Icon(
                                tint = Color.LightGray,
                                painter = painterResource(id = folder.icon),
                                contentDescription = "icon",
                                modifier = Modifier
                                    .size(200.dp)
                                    .align(Alignment.Center)

                            )
                            Text(
                                softWrap = true,
                                textAlign = TextAlign.Center,
                                text = folder.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 20.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontWeight = FontWeight.Black,
                                color = Color.Black,
                                modifier = Modifier
                                    .width(120.dp)
                                    .wrapContentHeight()
                                    .align(Alignment.Center)
                                    .padding(top = 80.dp)
                                    .clipToBounds()
                            )
                        }
                    }
                }*/

               /* Surface(
                    color = colorResource(R.color.modified).copy(0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .align(Alignment.Center)
                        .padding(start = 20.dp , end = 20.dp , top = 10.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {

                        Text("Dökümanları klasörleyin" ,
                            fontSize = 30.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(start = 40.dp , end = 40.dp , top = 10.dp)

                        )
                    }
                }*/

                Row(
                    horizontalArrangement = Arrangement.Center ,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.folder_icon_3_01),
                        contentDescription = "icon",
                        modifier = Modifier
                            .size(200.dp)

                    )
                    Column {

                        Text(text = "${stringResource(R.string.explain_folder_part_text)}\uD83E\uDEE1" ,
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Left,
                            modifier = Modifier

                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        FilledTonalButton(onClick = {


                            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                                addCategory(Intent.CATEGORY_OPENABLE)
                                type = "*/*"
                                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)

                            }
                           // MainActivity.mainActivity.startActivityForResult(intent, 55)
                            MainActivity.multiplyselectTofolderDocumentLauncher.launch(intent)
                        }) {

                            Image(painter = painterResource(R.drawable.baseline_create_new_folder_24) , "")
                            Text(text = stringResource(R.string.documentfolder))
                        }
                    }
                }

            }
        }

    }




}
@Composable
fun ListItem(folder: Folder) {
    val context = LocalContext.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier

            .clickable(
                enabled = true ,
                role = Role.Button ,
                onClickLabel = "doc" ,
                onClick = {

                    ripple(bounded = true)

                    Log.d("docClick" , "clicked at folder " + folder.id)
                    val fileName = folder.name

                    val intent = Intent(context , FolderScopeActivity::class.java)
                    intent.putExtra("folderName" , fileName)

                    context.startActivity(intent)

                   /* val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
                        // Optionally, specify a URI for the directory that should be opened in
                        // the system file picker when it loads.
                        putExtra(DocumentsContract.EXTRA_INITIAL_URI, "${Environment.DIRECTORY_DOCUMENTS}/DocuNote")
                    }

                    MainActivity.mainActivity.startActivityForResult(intent, 22)*/
                }
            )
    ) {

        Image(
            painter = painterResource(id = folder.icon),
            contentDescription = "icon",
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.Center)

        )
        Text(
            softWrap = true,
            textAlign = TextAlign.Center,
            text = folder.name,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 20.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Black,
            color = Color.Black,
            modifier = Modifier
                .width(120.dp)
                .wrapContentHeight()
                .align(Alignment.Center)
                .padding(top = 80.dp)
                .clipToBounds()
        )
    }
}
@Preview(showBackground = true)
@Composable
fun PreviewFolders(){
    DocuNoteTheme {
        Folders(AppViewModel())
    }
}