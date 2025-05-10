package com.batuscode.docunote

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.batuscode.docunote.AiActivity.Companion.aiActivityViewModel
import com.batuscode.docunote.AiActivity.Companion.packageName
import com.batuscode.docunote.integrity.IntegrityHelper
import com.batuscode.docunote.model.AIChatListItem
import com.batuscode.docunote.ui.theme.DocuNoteTheme
import com.batuscode.docunote.utils.AiUtil
import com.batuscode.docunote.utils.Auth
import com.batuscode.docunote.utils.DrawerSide
import com.batuscode.docunote.utils.PDFUtil
import com.batuscode.docunote.viewmodel.AiActivityViewModel
import com.google.android.play.core.review.ReviewException
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import com.google.android.play.core.review.testing.FakeReviewManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@AndroidEntryPoint
class AiActivity : ComponentActivity() {

    companion object {

        init {
            System.loadLibrary("jpdfium");
        }
        lateinit var context: Context
        lateinit var summDocLauncher: ActivityResultLauncher<Intent>
        lateinit var aiActivityViewModel: AiActivityViewModel
        var proChecked = mutableStateOf(false)
        val snackbarHostState = SnackbarHostState()
        lateinit var packageName : String
    }

    private val knowledgePdfFilePickerLauncher : ActivityResultLauncher<Intent> = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){
        result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            data?.let { it ->

                val uri = it.data

                val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                Log.d("newuri", uri.toString())

                aiActivityViewModel.update_knowledgePdfFileUri(uri!!)


            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted : Boolean ->
        CoroutineScope(Dispatchers.IO).launch {
            aiActivityViewModel.saveNotificationState(isGranted)
        }
    }
    @Composable
    private fun AskNotificationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if (ContextCompat.checkSelfPermission(this , Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
                //can post notification
            } else if (notificationPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                NotificationPermissionDialog(
                    onDismiss = {
                        aiActivityViewModel._isGrantedNotificationPermission.value = false
                    }
                )
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    @Composable
    fun NotificationPermissionDialog(onDismiss: () -> Unit){
        Dialog(
            onDismissRequest = { onDismiss } ,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true , usePlatformDefaultWidth = true)
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight() ,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth() ,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally ,
                ) {

                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground) ,
                        contentDescription = "" ,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = stringResource(R.string.notificationpermissionquestiontext , stringResource(R.string.app_name)),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center ,
                    )

                    Text(
                        text = stringResource(R.string.in_app_notifications),
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth() ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.End ,
                    ) {
                        TextButton(
                            onClick = { onDismiss }
                        ) {
                            Text(
                                text = stringResource(R.string.nothanks) ,
                            )
                        }

                        Button(
                            onClick = {
                                aiActivityViewModel._isGrantedNotificationPermission.value = false
                                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.ok) ,
                                textAlign = TextAlign.Center ,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }


    @Composable
    fun notificationPermissionRationale(permission : String) : Boolean{
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            val isGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            return if (isGranted == PackageManager.PERMISSION_GRANTED) false else true
        } else {
            return true
        }

    }

    @Composable
    fun InappmessageDialog(onDismiss: () -> Unit , title : String = "" , body : String = ""){
        Dialog(
            onDismissRequest = { onDismiss() } ,
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true , usePlatformDefaultWidth = true)
        ) {
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
                    .wrapContentHeight() ,
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(24.dp)
                        .fillMaxWidth() ,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally ,
                ) {

                    Image(
                        painter = painterResource(R.drawable.ic_launcher_foreground) ,
                        contentDescription = "" ,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )

                    Text(
                        text = title,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center ,
                    )

                    Text(
                        text = body,
                        fontSize = 16.sp,
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth() ,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center ,
                    ) {

                        Button(
                            onClick = {
                                onDismiss()
                            }
                        ) {
                            Text(
                                text = stringResource(R.string.ok) ,
                                textAlign = TextAlign.Center ,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()

        if (!Auth.user.value.pro && !proChecked.value){
            proChecked.value = proChecked.value.not()
            val intent = Intent(this , StoreActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Auth.detachDelivery()
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this
        Auth.delivery()
        PDFUtil.init(context)
        Companion.packageName = packageName

        aiActivityViewModel = ViewModelProvider(this).get(AiActivityViewModel::class.java)
        lifecycleScope.launch {
            aiActivityViewModel.openKnowledgePDFfilePickerActivity.collect{

                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "application/pdf"
                }
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                knowledgePdfFilePickerLauncher.launch(intent)
            }
        }
        summDocLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ){ result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                data?.let { it ->

                    val uri = it.data

                    val takeFlags: Int = Intent.FLAG_GRANT_READ_URI_PERMISSION or
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION
// Check for the freshest data.
                    contentResolver.takePersistableUriPermission(uri!!, takeFlags)

                    Log.d("newuri", uri.toString())

                    CoroutineScope(Dispatchers.IO).launch {
                        AiUtil.sumPDF(uri , context)
                    }


                }
            }
        }

        enableEdgeToEdge()
        setContent {
                val isNeedAskNotificationPermission by remember { derivedStateOf { aiActivityViewModel._isGrantedNotificationPermission.value } }

                var isModSelected by remember { mutableStateOf(false) }
                var selectedIndex by remember { mutableStateOf(-1) }
                var prompt = remember { mutableStateOf("") }
                val isFileSelected by aiActivityViewModel.isSelectedKnowledgePDFfile.collectAsState()
                var isDrawerOpen = remember { mutableStateOf(false) }
                val user by Auth.user
            val inappmessage by Auth.inappmessage
            val chatList by aiActivityViewModel.chatList.collectAsState()
            val knowledgeChatList by aiActivityViewModel.knowledgeList.collectAsState()
            val selectedFileUri by aiActivityViewModel.knowledgePdfFileUri.collectAsState()
            var expanded by remember { mutableStateOf(false) }

                val context = LocalContext.current
                DocuNoteTheme(darkTheme = true) {
                    Scaffold(
                        snackbarHost = {
                        SnackbarHost(hostState = snackbarHostState)
                    },
                        modifier = Modifier
                            .fillMaxSize() ,
                        topBar = {
                            TopAppBar(
                                title = {} ,
                                modifier = Modifier
                                    .nestedScroll(rememberNestedScrollInteropConnection()),
                                navigationIcon = {
                                    IconButton(
                                        onClick = {

                                            val intent = Intent(context , StoreActivity::class.java)
                                            context.startActivity(intent)
                                        }
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.workspace_premium),
                                            contentDescription = null,
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                } ,
                                actions = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth() ,
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (selectedIndex != -1){
                                            AiChoiceSegmentedButton(
                                                modifier = Modifier
                                                    .align(Alignment.Center)
                                                    .padding(end = 8.dp),
                                                selectedIndex,
                                                onOptionSelected = {
                                                        index -> selectedIndex = index
                                                }
                                            )
                                        }
                                        Row(
                                            modifier = Modifier
                                                .wrapContentWidth()
                                                .align(Alignment.CenterEnd) ,
                                            verticalAlignment = Alignment.CenterVertically ,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp) ,
                                        ) {

                                            Box(
                                                modifier = Modifier
                                                    .clickable {
                                                        val intent = Intent(context , MainActivity::class.java)
                                                        context.startActivity(intent)
                                                    }
                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.workspaces) ,
                                                    contentDescription = ""
                                                )
                                            }
                                            Box(
                                                modifier = Modifier
                                                    .clickable(
                                                        enabled = true ,
                                                        onClick = {
                                                           // isDrawerOpen.value = isDrawerOpen.value.not()
                                                            expanded = !expanded
                                                        }
                                                    )
                                            ) {
                                                AsyncImage(
                                                    model = Auth.auth.currentUser?.photoUrl ,
                                                    contentDescription = stringResource(R.string.profile_photo) ,
                                                    modifier = Modifier
                                                        .size(36.dp)
                                                        .clip(CircleShape),
                                                    contentScale = ContentScale.Crop
                                                )

                                                DropdownMenu(
                                                    expanded = expanded,
                                                    onDismissRequest = { expanded = false }
                                                ) {
                                                    DropdownMenuItem(
                                                        text = { Text(text = stringResource(R.string.rate_review)) },
                                                        onClick = {
                                                            val manager = FakeReviewManager(context)
                                                            val request = manager.requestReviewFlow()

                                                            request.addOnCompleteListener { task ->
                                                                if (task.isSuccessful) {
                                                                    val reviewInfo = task.result
                                                                    val activity = context as Activity
                                                                    manager.launchReviewFlow(activity, reviewInfo)
                                                                } else {
                                                                    val reviewErrorCode = (task.exception as ReviewException).errorCode
                                                                    Log.e("InAppReview", "Hata kodu: $reviewErrorCode")
                                                                }
                                                            }
                                                        }
                                                    )
                                                    DropdownMenuItem(
                                                        text = { Text(text = stringResource(R.string.sign_out)) },
                                                        onClick = {
                                                            CoroutineScope(Dispatchers.IO).launch {
                                                                Auth.signOut(context)
                                                            }
                                                        }
                                                    )
                                                }
                                            }


                                        }
                                    }
                                } ,
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = Color.Transparent.copy(0.0f)
                                )
                            )
                        } ,
                        bottomBar = {
                            BottomAppBar(
                                modifier = Modifier
                                    .alpha(if (selectedIndex == -1) 0f else 1f)
                                    .height(150.dp),
                                containerColor = Color.Transparent ,
                                contentColor = Color.Transparent
                            ) {
                                when(selectedIndex){
                                    0 ->
                                        // summarize section ...
                                    {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize() ,
                                            contentAlignment = Alignment.Center
                                        ){
                                            IconButton(
                                                onClick = {
                                                    Log.d("aiactivity" , "clicked select document button ...")
                                                    // handle summ ai ...

                                                    if (user.pro){
                                                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT ).apply {
                                                            addCategory(Intent.CATEGORY_OPENABLE)
                                                            type = "application/pdf"
                                                        }
                                                        intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
                                                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                        summDocLauncher.launch(intent)
                                                    } else {
                                                        CoroutineScope(Dispatchers.Main).launch {
                                                            snackbarHostState.showSnackbar(
                                                                message = "special for knowledge pro users" ,
                                                                duration = SnackbarDuration.Short
                                                            )

                                                        }
                                                    }

                                                } ,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(CircleShape) ,

                                            ) {
                                                Image(
                                                    painter = painterResource(R.drawable.file_open_) ,
                                                    contentDescription = "" ,
                                                    contentScale = ContentScale.Crop ,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                )
                                            }
                                        }
                                    }
                                    1 ->
                                        // knowledge section ...
                                    {

                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .padding(horizontal = 8.dp, vertical = 8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxSize() ,
                                                verticalAlignment = Alignment.CenterVertically ,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                IconButton(
                                                    onClick = {
                                                        if (user.pro){
                                                            // pdf dosyası seçilmemişse
                                                            if (isFileSelected == false){
                                                                aiActivityViewModel.handleSelectKnowledgePdfFile()
                                                            } else {

                                                            }
                                                        } else {
                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "special for knowledge pro users" ,
                                                                    duration = SnackbarDuration.Short
                                                                )

                                                            }
                                                        }
                                                    } ,
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = Color.Transparent
                                                    ) ,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(R.drawable.attach_file_) ,
                                                        contentDescription = "" ,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                    )
                                                    }
                                                    BasicTextField(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .requiredHeight(68.dp),
                                                        value = prompt.value ,
                                                        onValueChange = { newPrompt ->
                                                            prompt.value = newPrompt
                                                        } ,
                                                        textStyle = MaterialTheme.typography.bodyMedium,
                                                        decorationBox = { innerTextField ->
                                                            Box(
                                                                modifier = Modifier
                                                                    .shadow(
                                                                        elevation = 8.dp,
                                                                        shape = RoundedCornerShape(32.dp)
                                                                    )
                                                                    .background(
                                                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                                                        shape = RoundedCornerShape(8.dp)
                                                                    )
                                                                    .padding(
                                                                        horizontal = 32.dp,
                                                                        vertical = 16.dp
                                                                    )

                                                            ) {
                                                                if (prompt.value.isEmpty()){
                                                                    Text(
                                                                        text = stringResource(R.string.message_placeholder) ,
                                                                        style = MaterialTheme.typography.bodySmall
                                                                    )
                                                                }
                                                                innerTextField()
                                                            }

                                                        }
                                                    )

                                                IconButton(
                                                    onClick = {
                                                        if (user.pro){

                                                            val userPrompt = prompt.value
                                                            prompt.value = ""
                                                            AiUtil.knowledgeChat(selectedFileUri!!,context,userPrompt)
                                                        } else {
                                                            CoroutineScope(Dispatchers.Main).launch {
                                                                snackbarHostState.showSnackbar(
                                                                    message = "special for knowledge pro users" ,
                                                                    duration = SnackbarDuration.Short
                                                                )
                                                            }
                                                        }
                                                    } ,
                                                    colors = IconButtonDefaults.iconButtonColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                                                    ) ,
                                                    modifier = Modifier
                                                        .size(48.dp)
                                                        .clip(CircleShape)

                                                ) {
                                                    Image(
                                                        painter = painterResource(R.drawable.arrow_upward_) ,
                                                        contentDescription = "" ,
                                                        contentScale = ContentScale.Crop ,
                                                        modifier = Modifier
                                                            .size(32.dp)
                                                    )
                                                }
                                            }


                                        }
                                    }
                                    else -> {}
                                }

                            }
                        } ,

                        ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .padding(innerPadding)
                                .fillMaxSize() ,
                            contentAlignment = Alignment.TopCenter
                        ) {
                            if (selectedIndex == -1){
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "How can i help you?" ,
                                        style = MaterialTheme.typography.headlineLarge ,
                                        modifier = Modifier
                                    )
                                    Spacer(modifier = Modifier.height(50.dp))
                                    AiChoiceSegmentedButton(
                                        modifier = Modifier ,
                                        selectedIndex,
                                        onOptionSelected = {
                                                index -> selectedIndex = index
                                        }
                                    )
                                }
                            } else {
                                if (selectedIndex == 0){
                                    AiChatFlow(chatList , selectedIndex)
                                } else if(selectedIndex == 1) {
                                    AiChatFlow(knowledgeChatList , selectedIndex)
                                }
                            }


                        }
                    }

                    if (isDrawerOpen.value){
                        CustomSideDrawerOverlay(
                            isDrawerOpen = isDrawerOpen.value ,
                            onDismiss = { isDrawerOpen.value = isDrawerOpen.value.not() } ,
                            drawerContent = { CustomSideDrawerContent() } ,
                            // No need to pass content here since it's handled separately
                            drawerWidth = 300.dp,  // Customize the drawer width
                            showMask = true,  // Optional: if you want to show the mask when drawer is open
                            drawerSide = DrawerSide.RIGHT ,
                            animationDuration = 300 ,  // Animation duration for opening/closing the drawer
                        )
                    }

                    if (isNeedAskNotificationPermission){
                        AskNotificationPermission()
                    }

                    if (inappmessage.show){
                        InappmessageDialog(
                            onDismiss = {
                                Log.d("aiactivity" , "inappmessage")
                                Auth.update_inappmessage(inappmessage.copy(show = false))
                            } ,
                            title = inappmessage.title ,
                            body = inappmessage.body
                        )
                    }
                }
        }
    }
}

@Composable
fun AiChoiceSegmentedButton(modifier: Modifier = Modifier ,selectedIndex: Int,
                            onOptionSelected: (Int) -> Unit) {
    val options = listOf("Summarazation" , "Knowledge")


    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .wrapContentWidth() ,
        space = 0.dp ,
        content = {
            options.forEachIndexed { index, label ->
                SegmentedButton(
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .weight(1f),
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = options.size ,
                        baseShape = SegmentedButtonDefaults.baseShape.copy(CornerSize(16))
                    ) ,
                    onClick = {
                        onOptionSelected(index)
                    } ,
                    selected = index == selectedIndex ,
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.primary ,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = SegmentedButtonDefaults.borderStroke(color = MaterialTheme.colorScheme.surfaceVariant, width = 0.dp),
                    icon = {},
                    label = {
                        Text(
                            text = label
                        )
                    }
                )
            }
        }
    )
}

@Composable
fun MinimalDropdownMenu() {

}
@Composable
fun AiChatFlow(chatList: List<AIChatListItem> , menuIndex : Int){
    var state = rememberLazyListState()
    val summarizeWelcome = aiActivityViewModel.summarizeWelcome.collectAsState()
    val knowledgeWelcome = aiActivityViewModel.knowledge.collectAsState()
    LaunchedEffect(Unit) {
        if (menuIndex == 0){
            if (!summarizeWelcome.value){
                AiUtil.welcoming()
                aiActivityViewModel.update_SummarizeWelcome(true)
            }
        } else if (menuIndex == 1){
            if (!knowledgeWelcome.value){
                AiUtil.welcomeKnowledge()
                aiActivityViewModel.update_KnowledgeWelcome(true)
            }
        }
    }
    LazyColumn(
        state = state,
        modifier = Modifier
            .fillMaxSize()
    ) {
        itemsIndexed(chatList){ index , item ->
            LaunchedEffect(chatList.size) {
                state.animateScrollToItem(chatList.size-1)
            }
            when(item){
                is AIChatListItem.TextItem -> {
                    mChatBubble(message = item.text.value, item.generating.value , item.role)
                }
                is AIChatListItem.SumItem ->  {
                    mSummedItem(item)
                }
            }
        }

    }
}


@Composable
fun mChatBubble(
    message: String,
    isLoading: Boolean ,
    role : String
) {

    // Box içinde balon metni ve üç nokta animasyonu
    Box(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
    ) {

        // Eğer isLoading true ise, üç nokta animasyonu gösterilecek
        if (isLoading) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp), // Opsiyonel: Butonun etrafında boşluk bırakmak için
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    strokeWidth = 2.dp ,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
        else {

            // Chat mesajı balonu
            Row(
                horizontalArrangement = if (role.equals("model")) Arrangement.Start else Arrangement.End ,
                verticalAlignment = Alignment.Top ,
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                if (role.equals("model")){
                    AsyncImage(
                        model = R.drawable.ic_launcher_foreground ,
                        contentDescription = "" ,
                        contentScale = ContentScale.Crop ,
                        modifier = Modifier
                            .clip(CircleShape)
                            .size(64.dp)
                    )
                    Column(
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(text = message)
                    }
                } else {
                    Column(
                        modifier = Modifier
                            .background(Color.Gray.copy(alpha = 0.2f), shape = RoundedCornerShape(16.dp))
                            .padding(16.dp)
                    ) {
                        Text(text = message)
                    }
                    AsyncImage(
                        model = Auth.auth.currentUser?.photoUrl ,
                        contentDescription = "" ,
                        contentScale = ContentScale.Crop ,
                        modifier = Modifier
                            .clip(CircleShape)
                            .padding(8.dp)
                            .size(64.dp)
                    )
                }

            }

        }
    }
}



@Composable
fun mSummedItem(summedItem: AIChatListItem.SumItem){
    val context = LocalContext.current


    // Eğer isLoading true ise, üç nokta animasyonu gösterilecek
    if (!summedItem.generating.value) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .padding(4.dp), // Opsiyonel: Butonun etrafında boşluk bırakmak için
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.fillMaxSize(),
                strokeWidth = 2.dp ,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
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
                        Log.d("summarized" , "filePath in uri value :: ${summedItem.filePath}")
                        val intent = Intent(context, PDFViewerActivity::class.java).apply {
                            putExtra("fileUri" , summedItem.filePath.value)
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
                    color = MaterialTheme.colorScheme.onSecondary ,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    text = summedItem.fileName.value ,
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 16.dp)
                )
                IconButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            PDFUtil.saveSumPDFfile(
                                summedItem.filePath.value ,
                                summedItem.fileName.value ,
                                context
                            )
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
fun CustomSideDrawerOverlay(
    isDrawerOpen: Boolean,
    onDismiss: () -> Unit,
    drawerContent: @Composable ColumnScope.() -> Unit,
    modifier: Modifier = Modifier,
    drawerWidth: Dp = 300.dp,
    animationDuration: Int = 300,
    maskColor: Color = Color.Black.copy(alpha = 0.5f),
    showMask: Boolean = false,
    drawerSide: DrawerSide = DrawerSide.RIGHT,
    cornerRadius: Dp = 32.dp,
    dragThresholdFraction: Float = 0.5f,
    enableSwipe: Boolean = true
) {
    // Coroutine scope for managing animations
    val scope = rememberCoroutineScope()

    val density = LocalDensity.current

    // Width of the drawer in pixels
    val drawerWidthPx = with(density) { drawerWidth.toPx() }

    // Offset for the drawer animation
    val offsetX = remember { Animatable(if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)) }

    // Launch animation when the drawer state changes
    LaunchedEffect(isDrawerOpen) {
        val targetOffsetX = if (isDrawerOpen) 0f else drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
        offsetX.animateTo(
            targetValue = targetOffsetX,
            animationSpec = tween(durationMillis = animationDuration)
        )
    }

    if (isDrawerOpen) {
        BackHandler {
            onDismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .zIndex(1f)
    ) {


        // Mask overlay when the drawer is open
        if (isDrawerOpen && showMask) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(maskColor)
                    .pointerInput(Unit) {
                        detectTapGestures(onTap = { onDismiss() })
                    }
            )
        }

        // Drawer content
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .width(drawerWidth)
                .offset { IntOffset(x = 2 * offsetX.value.roundToInt(), y = 0) }
                .align(if (drawerSide == DrawerSide.LEFT) Alignment.CenterStart else Alignment.CenterEnd)
                .systemBarsPadding()
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = if (cornerRadius > 0.dp) {
                        if (drawerSide == DrawerSide.LEFT) {
                            RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)
                        } else {
                            RoundedCornerShape(topStart = cornerRadius, bottomStart = cornerRadius)
                        }
                    } else {
                        RectangleShape
                    }
                )
                .pointerInput(Unit) {
                    if (enableSwipe) {
                        detectDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    val shouldClose = when (drawerSide) {
                                        DrawerSide.LEFT -> offsetX.value < -drawerWidthPx * dragThresholdFraction
                                        DrawerSide.RIGHT -> offsetX.value > drawerWidthPx * dragThresholdFraction
                                    }

                                    val finalTarget = if (shouldClose) {
                                        drawerWidthPx * (if (drawerSide == DrawerSide.LEFT) -1 else 1)
                                    } else {
                                        0f
                                    }

                                    offsetX.animateTo(
                                        targetValue = finalTarget,
                                        animationSpec = tween(durationMillis = animationDuration)
                                    )

                                    if (shouldClose) {
                                        onDismiss()
                                    }
                                }
                            }
                        ) { change, dragAmount ->
                            change.consume()

                            scope.launch {
                                val newOffset = offsetX.value + dragAmount.x

                                val clampedOffset = when (drawerSide) {
                                    DrawerSide.LEFT -> newOffset.coerceIn(-drawerWidthPx, 0f)
                                    DrawerSide.RIGHT -> newOffset.coerceIn(0f, drawerWidthPx)
                                }

                                offsetX.snapTo(clampedOffset)
                            }
                        }
                    }
                }
        ) {
            // Content inside the drawer
            drawerContent()
        }
    }
}

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@Composable
fun CustomSideDrawerContent(
    drawerWidth: Dp = 300.dp,
    cornerRadius: Dp = 32.dp,
){
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(drawerWidth)
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(topEnd = cornerRadius, bottomEnd = cornerRadius)

            ) ,
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(64.dp))
        Box(

        ) {
            AsyncImage(
                model = Auth.auth.currentUser?.photoUrl ,
                contentDescription = "stringResource(R.string.profile_photo)" ,
                contentScale = ContentScale.Crop ,
                modifier = Modifier
                    .size(172.dp)
                    .clip(CircleShape)
            )


        }
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = Auth.auth.currentUser?.displayName!! ,
            style = MaterialTheme.typography.titleLarge
        )
        Spacer(modifier = Modifier.height(32.dp))

        Column {


            OutlinedButton(
                onClick = {
                    val intent = Intent()
                    intent.setAction(Intent.ACTION_SEND)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    intent.setType("*/*")
                    intent.putExtra(Intent.EXTRA_TEXT,"merhaba")
                    context.startActivity(Intent.createChooser(intent,"share"))
                } ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.share_with_others)
                )
            }




            OutlinedButton(
                onClick = {

                } ,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.rate_review)
                )
            }
        }


        Spacer(modifier = Modifier.weight(1f))
        OutlinedButton(
            onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    Auth.signOut(context)
                }
            } ,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.sign_out) ,
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun AiActivityPreview() {
    var isModSelected by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(-1) }
    var prompt by remember { mutableStateOf("") }
    var isFileSelected by remember { mutableStateOf(false) }
    var isDrawerOpen = remember { mutableStateOf(false) }

    val context = LocalContext.current
    DocuNoteTheme(darkTheme = true) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize() ,
            topBar = {
                TopAppBar(
                    title = {} ,
                    modifier = Modifier
                        .nestedScroll(rememberNestedScrollInteropConnection()),
                    navigationIcon = {
                        IconButton(
                            onClick = {

                                val intent = Intent(context , StoreActivity::class.java)
                                context.startActivity(intent)
                            }
                        ) {
                            Image(
                                painter = painterResource(R.drawable.workspace_premium),
                                contentDescription = null,
                                contentScale = ContentScale.Crop
                            )
                        }
                    } ,
                    actions = {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth() ,
                            verticalAlignment = Alignment.CenterVertically ,
                            horizontalArrangement = Arrangement.End
                        ) {


                            Box(
                                modifier = Modifier
                                    .clickable(
                                        enabled = true ,
                                        onClick = {
                                            isDrawerOpen.value = isDrawerOpen.value.not()
                                        }
                                    )
                            ) {
                                Image(
                                    painter = painterResource(R.drawable.ic_launcher_foreground) ,
                                    contentDescription = stringResource(R.string.profile_photo) ,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }


                        }
                    } ,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent.copy(0.0f)
                    )
                )
            } ,
            bottomBar = {
                BottomAppBar(
                    modifier = Modifier
                        .alpha(if (selectedIndex == -1) 0f else 1f)
                        .height(150.dp),
                    containerColor = Color.Transparent ,
                    contentColor = Color.Transparent
                ) {
                    when(selectedIndex){
                        0 ->
                        {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize() ,
                                contentAlignment = Alignment.Center
                            ){
                                IconButton(
                                    onClick = {
                                        // handle ai ...
                                    } ,
                                    modifier = Modifier
                                        .size(68.dp)
                                        .clip(CircleShape) ,
                                    enabled = if (prompt.isEmpty()) false else true

                                ) {
                                    Image(
                                        painter = painterResource(R.drawable.file_open_) ,
                                        contentDescription = "" ,
                                        contentScale = ContentScale.Crop ,
                                        modifier = Modifier
                                            .size(48.dp)
                                    )
                                }
                            }
                        }
                        1 ->
                        {

                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 8.dp, vertical = 8.dp)
                            ) {

                                // foto seçilmemişse
                                if (isFileSelected == false){
                                    IconButton(
                                        onClick = {
                                            isFileSelected = isFileSelected.not()
                                        } ,
                                        colors = IconButtonDefaults.iconButtonColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                                        ) ,
                                        modifier = Modifier
                                            .size(68.dp)
                                    ) {
                                        Image(
                                            painter = painterResource(R.drawable.file_open_) ,
                                            contentDescription = "" ,
                                            modifier = Modifier
                                                .size(48.dp)
                                        )
                                    }
                                } else {

                                    Row(
                                        modifier = Modifier
                                            .fillMaxSize() ,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BasicTextField(
                                            modifier = Modifier
                                                .weight(1f)
                                                .requiredHeight(68.dp),
                                            value = prompt ,
                                            onValueChange = { newPrompt ->
                                                prompt = newPrompt
                                            } ,
                                            textStyle = MaterialTheme.typography.bodyMedium,
                                            decorationBox = { innerTextField ->
                                                Box(
                                                    modifier = Modifier
                                                        .shadow(
                                                            elevation = 8.dp,
                                                            shape = RoundedCornerShape(32.dp)
                                                        )
                                                        .background(
                                                            color = MaterialTheme.colorScheme.surfaceVariant,
                                                            shape = RoundedCornerShape(8.dp)
                                                        )
                                                        .padding(
                                                            horizontal = 32.dp,
                                                            vertical = 16.dp
                                                        )

                                                ) {
                                                    if (prompt.isEmpty()){
                                                        Text(
                                                            text = stringResource(R.string.message_placeholder) ,
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    innerTextField()
                                                }

                                            }
                                        )

                                        IconButton(
                                            onClick = {
                                            } ,
                                            colors = IconButtonDefaults.iconButtonColors(
                                                containerColor = Color.Transparent
                                            ) ,
                                            modifier = Modifier
                                                .size(68.dp)
                                                .clip(CircleShape)

                                        ) {
                                            Image(
                                                painter = painterResource(R.drawable.prompt_suggestion) ,
                                                contentDescription = "" ,
                                                contentScale = ContentScale.Crop ,
                                                modifier = Modifier
                                                    .size(48.dp)
                                            )
                                        }
                                    }
                                }




                            }
                        }
                        else -> {}
                    }

                }
            } ,

            ) { innerPadding ->
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize() ,
                contentAlignment = Alignment.Center
            ) {
                if (selectedIndex == -1){
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "How can i help you?" ,
                            style = MaterialTheme.typography.headlineLarge ,
                            modifier = Modifier
                        )
                        Spacer(modifier = Modifier.height(50.dp))
                        AiChoiceSegmentedButton(
                            modifier = Modifier ,
                            selectedIndex,
                            onOptionSelected = {
                                    index -> selectedIndex = index
                            }
                        )
                    }

                } else {

                   // AiChatFlow(chatList)
                }



            }
        }

        if (isDrawerOpen.value){
            CustomSideDrawerOverlay(
                isDrawerOpen = isDrawerOpen.value ,
                onDismiss = { isDrawerOpen.value = isDrawerOpen.value.not() } ,
                drawerContent = { CustomSideDrawerContent() } ,
                // No need to pass content here since it's handled separately
                drawerWidth = 300.dp,  // Customize the drawer width
                showMask = true,  // Optional: if you want to show the mask when drawer is open
                drawerSide = DrawerSide.LEFT ,
                animationDuration = 300 ,  // Animation duration for opening/closing the drawer
            )
        }
    }
}


@Preview(showBackground = true)
@Composable
fun KnowledgePreview(){
    var prompt by remember { mutableStateOf("") }
    DocuNoteTheme(darkTheme = true) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            IconButton(
                modifier = Modifier
                    .align(Alignment.TopCenter),
                onClick = {

                }
            ) {
                Image(
                    painter = painterResource(R.drawable.file_open_) ,
                    contentDescription = ""
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .weight(1f)
                        .requiredHeight(68.dp),
                    value = prompt ,
                    onValueChange = { newPrompt ->
                        prompt = newPrompt
                    } ,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    decorationBox = { innerTextField ->
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 8.dp,
                                    shape = RoundedCornerShape(32.dp)
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(
                                    horizontal = 32.dp,
                                    vertical = 16.dp
                                )

                        ) {
                            if (prompt.isEmpty()){
                                Text(
                                    text = stringResource(R.string.message_placeholder) ,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            innerTextField()
                        }
                    }
                )

                IconButton(
                    onClick = {
                        // handle ai ...
                    } ,
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape) ,
                    enabled = if (prompt.isEmpty()) false else true

                ) {
                    Image(
                        painter = painterResource(R.drawable.prompt_suggestion) ,
                        contentDescription = "" ,
                        contentScale = ContentScale.Crop ,
                        modifier = Modifier
                            .size(48.dp)
                    )
                }
            }
        }
    }
}