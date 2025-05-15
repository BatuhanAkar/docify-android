package com.batuscode.docunote

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.batuscode.docunote.AiActivity
import com.batuscode.docunote.SignupActivity.Companion.validating
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.viewmodel.SignupActivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignupActivity : ComponentActivity() {


    companion object {
        init {
            System.loadLibrary("jpdfium")
        }
        lateinit var signupActivityViewModel : SignupActivityViewModel
        var validating = mutableStateOf(false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        signupActivityViewModel = ViewModelProvider(this).get(SignupActivityViewModel::class.java)

        enableEdgeToEdge()
        setContent {
            DocuNoteTheme(darkTheme = true) {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignUpScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}


@Composable
fun SignUpScreen(modifier: Modifier = Modifier){
    val context = LocalContext.current
    Column(
        modifier
            .fillMaxSize() ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom,

        ) {

        Spacer(modifier = Modifier.weight(1f))
        Column(
            modifier = Modifier ,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(R.drawable.ic_launcher_foreground) ,
                contentDescription = "" ,
                modifier = Modifier
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = stringResource(R.string.app_name) ,
                style = MaterialTheme.typography.titleLarge ,
                fontSize = 32.sp
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        if (validating.value){
            CircularProgressIndicator(
                modifier = Modifier
                    .absolutePadding(bottom = 16.dp)
            )
        } else {
            OutlinedButton (
                enabled = Auth.auth.currentUser == null,
                onClick = {
                    CoroutineScope(Dispatchers.Default).launch {
                        validating.value = validating.value.not()
                        Auth.firebaseAuthWithGoogle(context = context)
                    }
                } ,
                modifier = Modifier
                    .absolutePadding(bottom = 16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Row (
                    verticalAlignment = Alignment.CenterVertically ,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    Image(
                        painter = painterResource(R.drawable.android_dark_rd_na) ,
                        contentDescription = stringResource(R.string.gicon)
                    )
                    Text(
                        text = stringResource(R.string.continuewithgoogle) ,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    DocuNoteTheme(darkTheme = true) {
        SignUpScreen()
    }
}