package com.batuscode.docunote.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.R
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel

@Composable
fun HandNotes(appViewModel: AppViewModel){
    val dummyList1 = List(20) { "Item #${it + 1}" }
    val handNotesFolders = appViewModel.handNotesFolders
    Column {
        Text(text = stringResource(R.string.handNotes))
        LazyRow (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(handNotesFolders) { item ->
                HandNotesListItem(folder = item)
            }
        }
    }

}

@Composable
fun HandNotesListItem(folder: Folder) {
    Box(
        modifier = Modifier
            .width(120.dp)
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(
                painter = painterResource(id = folder.icon),
                contentDescription = "icon",
                modifier = Modifier
                    .size(120.dp)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                text = folder.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 8.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewHandNotes(){
    DocuNoteTheme {
        HandNotes(AppViewModel())
    }
}