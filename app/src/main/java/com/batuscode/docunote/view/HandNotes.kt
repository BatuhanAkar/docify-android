package com.batuscode.docunote.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.batuscode.docunote.R
import com.batuscode.docunote.ui.theme.DocuNoteTheme

@Composable
fun HandNotes(modifier: Modifier = Modifier){
    val dummyList1 = List(20) { "Item #${it + 1}" }
    Column {
        Text(text = stringResource(R.string.handNotes))
        LazyRow (
            modifier = Modifier
                .fillMaxWidth()
        ) {
            items(dummyList1) { item ->
                HandNotesListItem(item = item)
            }
        }
    }

}

@Composable
fun HandNotesListItem(item: String) {
    Text(text = item, style = MaterialTheme.typography.bodyLarge)
}

@Preview(showBackground = true)
@Composable
fun PreviewHandNotes(){
    DocuNoteTheme {
        HandNotes()
    }
}