package com.batuscode.docunote.view

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.compose.ui.zIndex
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.MainActivity.Companion.context
import com.batuscode.docunote.PDFViewerActivity
import com.batuscode.docunote.R
import com.batuscode.docunote.manager.ActivityResultLauncherManager
import com.batuscode.docunote.model.File
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel

@Composable
fun RecentlyRead(appViewModel: AppViewModel){

    val exfList1 = remember {
        mutableListOf<File>()
    }

    exfList1.clear()
    val file = File("", stringResource(R.string.recently_read_item1))
    val file2 = File("", stringResource(R.string.recently_read_item2))


    exfList1.add(0,file)
    exfList1.add(1,file2)

    Log.d("exfilelist" , "size :: " + exfList1.size)

    val _recentlyReadedDocs = MainActivity.mainActivityViewModel.recentlyReadedDocs.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            text = stringResource(R.string.recentlyread) ,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            modifier = Modifier
                .padding(16.dp)
        )

        if (MainActivity.recentlyStat.value){

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                items(_recentlyReadedDocs.value.reversed()){
                        item -> RecentlyReadItemView(item)
                }
            }
        } else {

            Box(

                contentAlignment = Alignment.Center,
                modifier = Modifier
                .fillMaxWidth()
            ){
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .padding(vertical = 10.dp)
                )
                {
                    exfList1.forEachIndexed{
                        index , item ->

                        Box (
                            modifier = Modifier
                                .background(Color.Transparent)
                                .padding(vertical = 8.dp)
                                .clickable(
                                    enabled = true ,
                                    role = Role.Button ,
                                    onClickLabel = "recentlyDocument" ,
                                    onClick = {
                                        ripple(bounded = true)
                                    }
                                )
                        ){

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp , vertical = 8.dp)
                            ) {

                                Surface (
                                    shape = CircleShape ,
                                    color = Color.LightGray.copy(0.5f) ,
                                    modifier = Modifier
                                        .size(60.dp)
                                ){
                                    Box (
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        Image(

                                            painter = painterResource(R.drawable.open_pdf_01) ,
                                            contentDescription = "icon" ,
                                            alignment = Alignment.Center ,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .zIndex(1f)
                                        )
                                    }
                                }
                                Text(
                                    overflow = TextOverflow.Ellipsis,
                                    maxLines = 1,
                                    text = item.name ,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    Box (
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ){
                        Text(text = "${stringResource(R.string.explain_recently_read_part_text)} \uD83E\uDEE3",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                        )

                        Spacer(modifier = Modifier.height(40.dp))

                        FilledTonalButton(
                            modifier = Modifier
                                .align(Alignment.BottomCenter),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = colorResource(R.color.modified)
                            ),
                            onClick = {
                                ActivityResultLauncherManager.pickPDF(allowMultiplePick = false) { uri, FileNameWithOutExtension, _ ->

                                    val intent = Intent(context, PDFViewerActivity::class.java).apply {
                                        putExtra("fileUri", uri.toString())
                                        putExtra("fileDisplayName", FileNameWithOutExtension)
                                    }
                                    context.startActivity(intent)
                                }
                            }) {

                            Image(
                                painter = painterResource(R.drawable.open_pdf_01) ,
                                contentDescription = "icon" ,
                                alignment = Alignment.Center ,
                                modifier = Modifier
                                    .size(32.dp)
                                    .zIndex(1f)
                            )
                            Text(text = stringResource(R.string.opendocument) , color = Color.White)
                        }
                    }


                }
            }

        }
    }
}

@Composable
fun RecentlyReadItemView(file: File){

    Box (
        modifier = Modifier
            .background(Color.Transparent)
            .padding(vertical = 8.dp)
            .clickable(
                enabled = true ,
                role = Role.Button ,
                onClickLabel = "recentlyDocument" ,
                onClick = {
                    ripple(bounded = true)

                    val intent = Intent(context , PDFViewerActivity::class.java).apply {
                        putExtra("fileUri" , file.uri)
                        putExtra("fileDisplayName" , file.name)
                    }
                    context.startActivity(intent)
                }
            )
    ){

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp , vertical = 8.dp)
        ) {

            Surface (
                shape = CircleShape ,
                color = Color.LightGray.copy(0.5f) , 
                modifier = Modifier
                    .size(60.dp)
            ){
                Box (
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(

                        painter = painterResource(R.drawable.open_pdf_01) ,
                        contentDescription = "icon" ,
                        alignment = Alignment.Center ,
                        modifier = Modifier
                            .size(32.dp)
                            .zIndex(1f)
                    )
                }
            }
            Text(
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                text = file.name ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }



}



@Preview(showBackground = true)
@Composable
fun PreviewRecentlyRead(){
    File("", stringResource(R.string.recently_read_item1))
    DocuNoteTheme {
        //RecentlyRead(AppViewModel())
       // RecentlyReadItemView(file)
    }
}