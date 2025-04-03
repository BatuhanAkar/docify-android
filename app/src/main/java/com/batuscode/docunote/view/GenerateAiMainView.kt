package com.batuscode.docunote.view

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Cyan
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.R
import com.batuscode.docunote.SummfyAI

@Composable
fun GenerateAiMainView(){
    val gradientColors = listOf(colorResource(R.color.modified),colorResource(R.color.rose500) /*...*/)
    val context = LocalContext.current
    Surface(
        shape = RoundedCornerShape(20) ,
        color = Color.White ,
        shadowElevation = 20.dp ,
        modifier = Modifier
            .padding(horizontal = 40.dp , vertical = 16.dp) ,
        onClick = {
            ripple(bounded = true)

            val intent = Intent(context, SummfyAI::class.java)
            context.startActivity(intent)
        }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .width(350.dp)
                .height(150.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top ,
                horizontalArrangement = Arrangement.Start ,
                modifier = Modifier
                    .wrapContentSize()
                    .padding(horizontal = 20.dp , vertical = 20.dp)
            ) {
                Text(
                    textAlign = TextAlign.Center,
                    text = "Summarize AI",
                    style = TextStyle(
                        brush = Brush.linearGradient(
                            colors = gradientColors
                        )
                    ),
                    fontSize = 40.sp, // Diğer metinler için genel font boyutu
                    fontWeight = FontWeight.Black,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GenerateAiMainViewPreview(){
    DocuNoteTheme {
        GenerateAiMainView()
    }
}