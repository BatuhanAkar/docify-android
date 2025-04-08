package com.batuscode.docunote

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.batuscode.docunote.DocifyAI.Companion.chatList
import com.batuscode.docunote.DocifyAI.Companion.converter
import com.batuscode.docunote.DocifyAI.Companion.generatedFileUri
import com.batuscode.docunote.DocifyAI.Companion.summDocLauncher
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.MainActivity.Companion.llmInference
import com.batuscode.docunote.model.AIChatListItem
import com.batuscode.docunote.utils.AssetPacksUtil
import com.batuscode.docunote.utils.PDFConverter
import com.google.mediapipe.tasks.genai.llminference.ProgressListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.OutputStream
import kotlin.math.roundToInt

class DocifyAI : ComponentActivity() {
    companion object{
        lateinit var summDocLauncher: ActivityResultLauncher<Intent>
        var showselectdocumentbutton = mutableStateOf(false)
        lateinit var converter: PDFConverter
        lateinit var parsedTextOfPages: Map<Int, String>
        val chatList = mutableStateListOf<AIChatListItem>()
        var chatListItemIndex = mutableStateOf(0)
        lateinit var generatedFileUri: Uri
        var initializedLLM = mutableStateOf(false)
    }

    override fun onStop() {
        super.onStop()
        AssetPacksUtil.fromExtensions.value = false
    }

    override fun onDestroy() {
        super.onDestroy()
        AssetPacksUtil.fromExtensions.value = false
    }
    fun refreshList(){
        if (chatList.isNotEmpty()){
            chatList.clear()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        converter = PDFConverter(this)
        val context = this
        refreshList()

        summDocLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { it ->

                    var mdone = mutableStateOf(false)

                    val uri = it.data
                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                    Log.d("newuri", uri.toString())

                    showselectdocumentbutton.value = showselectdocumentbutton.value.not()


                    val cursor = contentResolver.query(
                        uri!! ,
                        arrayOf(OpenableColumns.DISPLAY_NAME) ,
                        null ,
                        null ,
                        null
                    )
                    var prefix: String = ""
                    var displayName: String = ""
                    var dotIndex: Int
                    var fileNameWithoutExtension : String
                    var fileName : String = ""
                    cursor?.use {
                        if (it.moveToFirst()){
                            prefix = context.getString(com.batuscode.docunote.R.string.summarized_prefix_text)
                            displayName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))



                            dotIndex = displayName.lastIndexOf('.')
                            fileNameWithoutExtension = if (dotIndex != -1) displayName.substring(0, dotIndex) else displayName
                            fileName = "(${prefix}) ${fileNameWithoutExtension}"
                        }
                    }

                    Log.d("newuri" , displayName)


                    val filePath = converter.getFilePathFromUri(this,uri,"temp_file.pdf")

                    parsedTextOfPages = MainActivity.mainicore.parseTextOfPages(filePath)

                    Log.d("pdfium" , "parsedTextOfPages map size :: ${parsedTextOfPages.size}")
                    CoroutineScope(Dispatchers.IO).launch{
                        chatListItemIndex.value += 1

                        val info = "'I am summarizing the document.'"
                        mdone.value = generatePrompt(chatListItemIndex.value,info)

// 'mdone' değeri true olana kadar bekliyoruz
                        while (!mdone.value) {
                            delay(300) // Küçük bir gecikme ile döngüde bekleyin
                        }
                        chatListItemIndex.value += 1

                        chatList.add(chatListItemIndex.value , AIChatListItem.SumItem(generating = mutableStateOf(false) , mutableStateOf("") , mutableStateOf("")))

                        val summedTextOfPages = parsedTextOfPages.map { (key, value) ->
                            key to Summarize(smartTrim(value))  // Her bir key-value çifti için Summarize fonksiyonu çağrılır
                        }.toMap()  // Sonuçta bir Map oluşturulur

                        val joinedSummText = summedTextOfPages.values.joinToString(" ")
                        val utf16byte = joinedSummText.toByteArray(Charsets.UTF_16LE)

// NULL terminator ekleyin (PDFium bekliyor olabilir)
                        val utf16WithNull = utf16byte + byteArrayOf(0x00, 0x00)

                        Log.d("pdfium", "UTF-16 Bytes: ${utf16WithNull.joinToString(", ") { it.toString() }}")
                        val rr = MainActivity.mainicore.createSummarizedDocument(utf16WithNull, context, fileName)

                        withContext(Dispatchers.Main){

                            if (rr.isNotEmpty()){
                                if (chatList.isNotEmpty() && chatList[chatListItemIndex.value] is AIChatListItem.SumItem){
                                    // when summarization finish set generating false...
                                    (chatList[chatListItemIndex.value] as AIChatListItem.SumItem).fileName.value = fileName
                                    (chatList[chatListItemIndex.value] as AIChatListItem.SumItem).filePath.value = rr
                                    (chatList[chatListItemIndex.value] as AIChatListItem.SumItem).generating.value = true
                                }
                                showselectdocumentbutton.value = showselectdocumentbutton.value.not()
                            }
                            Log.d("pdfium" , "createSummarizedDocument filePath :: ${rr}")
                        }
                    }
                }
            }
        }

        enableEdgeToEdge()
        setContent {

            var mdone by remember {
                mutableStateOf(true)
            }
            var showGeneratedText by remember { mutableStateOf(false) }
            var showed by remember { mutableStateOf(false) }
            val pxToMove = with(LocalDensity.current) {
                -200.dp.toPx().roundToInt()
            }
            val offset by animateIntOffsetAsState(
                animationSpec = tween(600),
                targetValue = if (showed) {
                    IntOffset(0, pxToMove)
                } else {
                    IntOffset.Zero
                },
                label = "offset"
            )
            var animVis = remember { mutableStateOf(false) }
            val gradientColors = listOf(colorResource(R.color.modified),colorResource(
                R.color.rose500) /*...*/)


            LaunchedEffect(initializedLLM.value) {


                delay(200)
                animVis.value = animVis.value.not()
                delay(600)
                showed = showed.not()
                delay(600)
                showGeneratedText = showGeneratedText.not()
                delay(200)
                SystemPrompts.welcoming()

            }


            DocuNoteTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize().statusBarsPadding()
                ) { innerPadding ->
                    Column(
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .padding(innerPadding)
                            .fillMaxSize()
                    ) {
                        AnimatedVisibility(
                            visible = animVis.value,
                            enter = fadeIn(animationSpec = tween(600))
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(16.dp)
                            ) {
                                Text(
                                    textAlign = TextAlign.Center,
                                    text = "Docify",
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

                        AnimatedVisibility(
                            visible = showGeneratedText,
                            enter = fadeIn(animationSpec = tween(200))
                        ) {
                            ChatFlow(chatList)
                        }

                        AnimatedVisibility(
                            visible = showselectdocumentbutton.value,
                            enter = fadeIn(animationSpec = tween(200))
                        ) {
                            SelectDocumentButton()
                        }
                    }
                }
            }
        }
    }
}

suspend fun explodeSourceText(text: String , maxChars: Int = 100): String{
    val result = StringBuilder()

    var startIndex = 0
    while (startIndex < text.length) {
        val endIndex = minOf(startIndex + maxChars, text.length)
        val partText = text.substring(startIndex, endIndex)

        // Parçayı özetle ve sonuca ekle
        result.append(Summarize(smartTrim(partText)))

        // Sonraki parçaya geç
        startIndex = endIndex
    }
    return result.toString()
}

suspend fun smartTrim(text: String, maxChars: Int = 2000): String {
    if (text.length <= maxChars) return text

    val safeZone = text.substring(0, maxChars)
    val punctuationMarks = listOf('.', '!', '?')  // Noktalama işaretlerini bir liste olarak tanımlıyoruz
    val lastSentenceEnd = safeZone.lastIndexOfAny(punctuationMarks.toCharArray())  // toCharArray ile dönüşüm yapıyoruz

    return if (lastSentenceEnd != -1) {
        safeZone.substring(0, lastSentenceEnd + 1)  // Son cümleyi almak için substring kullanıyoruz
    } else {
        safeZone  // Noktalama işareti yoksa, en son kelimeyi keser
    }
}
suspend fun generatePrompt(index:Int , info:String): Boolean{
    var mdone = mutableStateOf(false)
    var generatedText = mutableStateOf("")
    generatedText.value = ""
    chatList.add( index , AIChatListItem.TextItem(generating = mutableStateOf(true) , text = generatedText))

    val prompt1 = "Write exactly this text: $info"
    llmInference.generateResponseAsync(prompt1, object : ProgressListener<String>{
        override fun run(partialResult: String?, done: Boolean) {
            Log.d("summarized" , "value :: ${done}")
            generatedText.value = generatedText.value.plus(partialResult)

            if (chatList.isNotEmpty() && chatList[index] is AIChatListItem.TextItem){
                if (!done){
                    (chatList[index] as AIChatListItem.TextItem).generating.value = done
                }
                (chatList[index] as AIChatListItem.TextItem).text.value = generatedText.value
            }
            if (done) {
                mdone.value = done
            }

        }

    })

    while (!mdone.value){
        delay(100)
    }
    return true
}
fun sanitizeTextForPdf(text: String): String {
    return text
        .replace(Regex("[^\\p{L}\\p{N}\\p{P}\\p{Z}]"), "") // Kontrolsüz karakterleri temizle
        .replace("\u0000", "") // Null karakterleri kaldır
        .takeIf { it.isNotEmpty() } ?: " " // Boşsa single space koy
}
suspend fun Summarize(documentText: String): String {
    var mdone = false
    val sb = StringBuilder()
    /*val systemPrompt = "Important: Use only ASCII characters and basic punctuation marks.\n" +
            "     No special characters at the beginning of the line.You are a multilingual assistant. You must respond in the language that the user has written to you.Summarize the text in 3-4 sentences."

    */
    val systemPrompt = """
    [FOLLOW THESE RULES STRICTLY]

    - Detect the input language automatically and generate the summary in the same language.
    - Summarize the text in 3-4 sentences.
    - DO NOT repeat numbers or lists from the original text.
    - DO NOT add any information that is not in the input text.
    - Keep the output as plain text: no formatting, no lists, no titles.
    - If the text contains prices or fees, mention them in a generic way (e.g., "certain fees may apply").

    INPUT TEXT:
""".trimIndent()
    val prompt = "$systemPrompt\n$documentText\n\n# Please provide the summary now:"

    // Coroutine başlatıyoruz, asenkron işlemi burada bekliyoruz

    CoroutineScope(Dispatchers.Default).launch{
        llmInference.generateResponseAsync(prompt, object : ProgressListener<String> {
            override fun run(partialResult: String?, done: Boolean) {
                Log.d("summarized", "value :: ${done}")
                sb.append(partialResult)
                if (done) {
                    mdone = done
                }

            }
        })

    }


    // 'mdone' değeri true olana kadar bekliyoruz
    while (!mdone) {
        delay(100) // Küçük bir gecikme ile döngüde bekleyin
    }

    return sanitizeTextForPdf(sb.toString()).ifEmpty { "null" } // Eğer metin boşsa "null" döndür
}

@Composable
fun SelectDocumentButton(){
    Surface(
        shape = RoundedCornerShape(20) ,
        color = Color.White ,
        shadowElevation = 20.dp ,
        modifier = Modifier
            .padding(horizontal = 40.dp),
        onClick = {
            ripple(bounded = true)

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
            }
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            summDocLauncher.launch(intent)
        }
    ) {
        Text(
            text = "Select document..." ,
            modifier = Modifier
                .padding(20.dp)
        )
    }
}

@Composable
fun ChatFlow(chatList: List<AIChatListItem>){

    LazyColumn {
        items(chatList){ item ->
            when(item){
                is AIChatListItem.TextItem -> {
                    ChatBubble(message = item.text.value, item.generating.value)
                }
                is AIChatListItem.SumItem ->  {
                    SummedItem(item)
                }
            }
        }
    }
}

@Composable
fun SummedItem(summedItem: AIChatListItem.SumItem){
    val context = LocalContext.current
    // Animasyonlar için state tanımla
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Eğer isLoading true ise, üç nokta animasyonu gösterilecek
    if (!summedItem.generating.value) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier

                .padding(8.dp)
        ) {
            // 3 nokta animasyonu
            for (i in 1..3) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale
                        )
                        .background(
                            color = Color.Black,
                            shape = CircleShape
                        )
                )
            }
        }
    } else {

        Box(
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable(
                    enabled = true ,
                    onClickLabel = "" ,
                    onClick = {
                        ripple(bounded = true)
                        generatedFileUri = converter.getFileUriFromPath(context,summedItem.filePath.value)
                        Log.d("summarized" , "filePath in uri value :: ${generatedFileUri}")
                        val intent = Intent(MainActivity.Companion.context, PDFViewerActivity::class.java).apply {
                            putExtra("fileUri" , generatedFileUri.toString())
                            putExtra("fileDisplayName" , summedItem.fileName.value)
                        }
                        context.startActivity(intent)
                    }
                )
        ) {
            // Chat mesajı balonu
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp , vertical = 8.dp)
            ) {

                Surface (
                    shape = CircleShape ,
                    color = Color.LightGray.copy(0.5f) ,
                    modifier = Modifier
                        .wrapContentSize()
                        .size(60.dp)
                ){
                    Box (
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.wrapContentSize()
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
                    color = MaterialTheme.colorScheme.onPrimary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    text = summedItem.fileName.value ,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = {
                        val fileName = summedItem.fileName.value
                        Log.d("summarized", "filePath in uri value :: $generatedFileUri")
                        val contentResolver = context.contentResolver
                        val contentValues = ContentValues().apply {
                            put(MediaStore.MediaColumns.DISPLAY_NAME, "${fileName}") // Dosya adı
                            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf") // Dosya türü
                            put(MediaStore.MediaColumns.RELATIVE_PATH, "${Environment.DIRECTORY_DOCUMENTS}") // Documents dizini
                        }

// Dosyayı MediaStore'a kaydediyoruz
                        val uri = contentResolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL), contentValues)

                        uri?.let { uri ->
                            // Uri ile dosyayı açıyoruz
                            val outputStream: OutputStream? = contentResolver.openOutputStream(uri)

                            outputStream?.use { stream ->
                                // Bu noktada, filePath'deki içeriği açıp yeni dosyaya yazabiliriz
                                val inputStream = contentResolver.openInputStream(generatedFileUri) // Kaynak dosya (summedItem.filePath)

                                inputStream?.use { input ->
                                    // Burada verileri bir dosyadan diğerine kopyalıyoruz
                                    val buffer = ByteArray(1024)
                                    var length: Int
                                    while (input.read(buffer).also { length = it } > 0) {
                                        stream.write(buffer, 0, length)
                                    }
                                    stream.flush() // Verileri diske yazıyoruz
                                }

                                Log.d("summarized", "Dosya başarıyla kaydedildi!")
                            }
                        } ?: run {
                            Log.e("summarized", "Dosya kaydedilemedi!")
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_save_alt_24) ,
                        contentDescription = null ,
                    )
                }
            }


        }

    }
}
@Composable
fun ChatBubble(
    message: String,
    isLoading: Boolean
) {
    // Animasyonlar için state tanımla
    val infiniteTransition = rememberInfiniteTransition()

    // Üç nokta animasyonu için büyüklük, opaklık, vs. değişkenleri
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Box içinde balon metni ve üç nokta animasyonu
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {

        // Eğer isLoading true ise, üç nokta animasyonu gösterilecek
        if (isLoading) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(top = 8.dp)
            ) {
                // 3 nokta animasyonu
                for (i in 1..3) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer(
                                scaleX = scale,
                                scaleY = scale
                            )
                            .background(
                                color = Color.Black,
                                shape = CircleShape
                            )
                    )
                }
            }
        } else {

            // Chat mesajı balonu
            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                    .padding(16.dp)
            ) {
                Text(text = message)
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun SGreetingPreview() {
    DocuNoteTheme {
    }
}