package com.batuscode.docunote.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuscode.docunote.MainActivity
import com.batuscode.docunote.R

@Composable
fun DFDownloadProgressView(onDismiss : () -> Unit ,  modifier: Modifier , progress: State<Float>){
    Surface(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth()
            .wrapContentHeight() ,
        shape = RoundedCornerShape(24.dp),
        color = Color.Gray.copy(alpha = 0.2f),
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween ,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) {
                Text(
                    text = if (MainActivity.expdwstatus.value.equals("Pending")) stringResource(R.string.explain_pending_text)
                    else if(MainActivity.expdwstatus.value.equals("Downloading")) stringResource(R.string.explain_download_ballon)
                    else if (MainActivity.expdwstatus.value.equals("Transferring")) stringResource(R.string.explain_transferring_text) else "Waiting..."
                )
                OutlinedIconButton(
                    onClick = onDismiss ,
                    border = null ,
                    modifier = Modifier
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_close_24) ,
                        contentDescription = null
                    )
                }
            }

            if (MainActivity.expdwstatus.value.equals("Pending") || MainActivity.expdwstatus.value.equals("Transferring")){
                LinearProgressIndicator(
                    color = colorResource(R.color.blue300) ,
                    trackColor = Color.White
                )
            } else if (MainActivity.expdwstatus.value.equals("Downloading")){
                LinearProgressIndicator(
                    progress = progress.value /100f ,
                    modifier = Modifier ,
                    color = colorResource(R.color.blue300) ,
                    trackColor = Color.White
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun dfPreview(){
    val p = remember { mutableStateOf(0f) }
    DFDownloadProgressView(onDismiss = {}, modifier = Modifier , progress = p)
}