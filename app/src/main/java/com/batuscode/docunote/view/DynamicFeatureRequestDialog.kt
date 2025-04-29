package com.batuscode.docunote.view

import android.graphics.Shader
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.batuscode.docunote.R
import com.batuscode.docunote.ui.theme.DocuNoteTheme

@Composable
fun DynamicFeatureRequestDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Blur efekt için Dialog arkasına blur efekt simülasyonu
    Dialog(onDismissRequest = { onDismiss() }) {
        Surface(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .wrapContentHeight() ,
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.background,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.dynamicfeaturedownloadrequest_title),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center ,
                    color = MaterialTheme.colorScheme.onSecondary,
                )

                Text(
                    text = stringResource(R.string.dynamicfeaturedownloadrequest_explain),
                    color = MaterialTheme.colorScheme.onSecondary,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(
                        onClick = onDismiss
                    ) {
                        Text(
                            text = stringResource(R.string.late_download) ,
                            color = MaterialTheme.colorScheme.onSecondary,
                        )
                    }

                    Button(
                        onClick = onConfirm
                    ) {
                        Text(
                            text = stringResource(R.string.download) ,
                            color = MaterialTheme.colorScheme.onSecondary,
                            textAlign = TextAlign.Center ,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DialogPreview(){
    DocuNoteTheme {
    }
}
