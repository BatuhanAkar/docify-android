package com.batuscode.docunote.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.R
import com.batuscode.docunote.model.ExtensionButton
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.viewmodel.AppViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Extensions(appViewModel: AppViewModel){
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = {
            appViewModel.update_extensionsOpenState(false)
        } ,
        sheetState = sheetState ,
        modifier = Modifier
            .fillMaxHeight()
    ) {

        Flow()

    }

}

@Composable
fun Flow(){

    val buttons = listOf(
        ExtensionButton(id = 1 , name = stringResource(id = R.string.createpdf) , R.drawable.createpdf2) ,
        ExtensionButton(id = 0 , name = stringResource(id = R.string.openpdf) , 0) ,
        ExtensionButton(id = 2 , name = stringResource(id = R.string.mergepdf) , 0 ) ,
        ExtensionButton(id = 3 , name = stringResource(id = R.string.splitpdf) , 0) ,
        ExtensionButton(id= 4 , name = stringResource(id = R.string.editpdf) , 0) ,
        ExtensionButton(id = 5 , name = stringResource(id = R.string.encryptpdf) , 0)

    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(2), // Sabit 2 kolonlu grid
        modifier = Modifier.wrapContentSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(buttons) { item ->
            GridItem(item)
        }
    }
}

@Composable
fun GridItem(button: ExtensionButton) {
    Box(
        modifier = Modifier
            .aspectRatio(1f) // Kare şeklinde her bir öğe
            .wrapContentSize()
            .padding(8.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Text(text = button.name)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewExtensions(){
    DocuNoteTheme {
        Flow()
    }
}