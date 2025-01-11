package com.batuscode.docunote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.batuscode.docunote.model.Folder
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.view.Folders
import com.batuscode.docunote.view.HandNotes
import com.batuscode.docunote.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appViewModel : AppViewModel by viewModels()
        val folder = Folder("Downloads" , R.drawable.outline_folder_24)
        val folder1 = Folder("Matematik" , R.drawable.outline_folder_24)
        val folder2 = Folder("Coğrafya" , R.drawable.outline_folder_24)
        val folder3 = Folder("Kimya" , R.drawable.outline_folder_24)
        val folder4 = Folder("Türk Dili ve Edebiyatı" , R.drawable.outline_folder_24)
        appViewModel.loadFolders(folder)
        appViewModel.loadFolders(folder1)
        appViewModel.loadFolders(folder2)
        appViewModel.loadFolders(folder3)
        appViewModel.loadFolders(folder4)

        setContent {
            Flow(appViewModel = appViewModel)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Flow(appViewModel: AppViewModel) {
    DocuNoteTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize() ,
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_name)) }
                )
            }
        ) { innerPadding ->

            Column (
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize() ,

                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {

                Folders(
                    appViewModel = appViewModel
                )
                HandNotes(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Preview(showBackground = true , showSystemUi = true)
@Composable
fun GreetingPreview() {
    DocuNoteTheme {
        Flow(AppViewModel())
    }
}