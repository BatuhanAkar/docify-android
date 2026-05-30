package com.batuscode.docunote.view

import android.annotation.SuppressLint
import android.content.Intent
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
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuscode.docunote.FolderScopeActivity
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel

@SuppressLint("ResourceAsColor")
@Composable
fun Folders(appViewModel: AppViewModel){
    var exList = remember {
        mutableListOf<Folder>()
    }
    val folder = Folder( 0 ,"Kimya" , R.drawable.folder_icon_4_01 , emptyList())
    val folder1 = Folder( 1 , "Sınav" , R.drawable.folder_icon_4_01 , emptyList())

    exList.add(folder)
    exList.add(folder1)


    val _folders = MainActivity.mainActivityViewModel.folders.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween ,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {

            Text(
                fontSize = 30.sp,
                fontWeight = FontWeight.Black,
                text = stringResource(R.string.folders),
                modifier = Modifier
                    .wrapContentSize()
                    .padding(16.dp)
            )

            IconButton(
                onClick = {
                    MainActivity.mainActivityViewModel.update_NewFolderOnScreen(true)
                }
            ) {
                Icon(
                    painter = painterResource(R.drawable.create_new_folder) ,
                    contentDescription = null ,
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        }


        if (MainActivity.folderStat.value){
            LazyRow (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clipToBounds()
                    .requiredHeight(100.dp)
            ) {
                items(_folders.value) { item ->
                    ListItem(folder = item)
                }

            }
        } else {
            Box(
                contentAlignment = Alignment.Center ,
                modifier = Modifier

                    .requiredHeight(200.dp)
            ) {
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
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Left,
                            modifier = Modifier

                        )
                        Spacer(modifier = Modifier.height(40.dp))

                        FilledTonalButton(
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colorResource(R.color.modified)
                            ),
                            onClick = {
                                MainActivity.mainActivityViewModel.update_NewFolderOnScreen(true)
                            }
                        ) {

                            Image(painter = painterResource(R.drawable.baseline_create_new_folder_24) , "")
                            Text(
                                text = stringResource(R.string.documentfolder) ,
                                color = Color.White,
                            )
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
            .padding(start = 30.dp)
            .width(300.dp)
            .height(100.dp)
            .clip(RoundedCornerShape(20))
            .background(Color.LightGray.copy(0.5f))
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
                    intent.putExtra("folder" , folder)
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
        Row(
            verticalAlignment = Alignment.Top ,
            horizontalArrangement = Arrangement.Start ,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp , vertical = 20.dp)
        ) {
            Image(
                painter = painterResource(id = folder.icon),
                contentDescription = "icon",
                modifier = Modifier
                    .size(50.dp)

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
                modifier = Modifier
                    .padding(vertical = 15.dp)
            )
        }

    }
}
@Preview(showBackground = true)
@Composable
fun PreviewFolders(){

    val folder = Folder( 0 ,"Downloads" , R.drawable.folder_icon_4_01 , emptyList())
    DocuNoteTheme {
        //Folders(AppViewModel())
        ListItem(folder)

    }
}