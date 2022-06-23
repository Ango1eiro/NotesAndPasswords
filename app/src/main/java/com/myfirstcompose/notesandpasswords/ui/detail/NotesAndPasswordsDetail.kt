package com.myfirstcompose.notesandpasswords.ui.detail

import android.Manifest
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.myfirstcompose.notesandpasswords.NotesAndPasswordsViewModel
import com.myfirstcompose.notesandpasswords.R
import com.myfirstcompose.notesandpasswords.data.Credential
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.Note
import com.myfirstcompose.notesandpasswords.ui.theme.PinkSuperLight
import com.myfirstcompose.notesandpasswords.utils.createCopyAndReturnRealPath
import kotlinx.coroutines.launch

@Composable
fun NotesAndPasswordsDetail(id: Long, viewModel: NotesAndPasswordsViewModel, onNavigateBack: () -> Unit) {

    val scope = rememberCoroutineScope()

    Log.v("NotesAndPasswordsDetail","$currentRecomposeScope id - $id")

    var initialised by remember {
        mutableStateOf(false)
    }

    Log.v("NotesAndPasswordsDetail","initialised - $initialised")
    if(!initialised) {
        if (id < 0) {
            viewModel.newCurrentNap()
            initialised = true
        } else {
            LaunchedEffect(key1 = id, block = {
                viewModel.setCurrentNap(id)
                initialised = true
            })

        }
    }
    Log.v("NotesAndPasswordsDetail","initialised - $initialised")

    val nap by viewModel.currentNap.observeAsState()

    if (nap != null && initialised) {
        Log.v("NotesAndPasswordsDetail","nap:$nap")

        var currentList by remember {
            if (nap!!.notes.isEmpty() && !nap!!.credentials.isEmpty()){
                mutableStateOf(NotesAndPasswordsCurrentList.Passwords)
            } else
            {
                mutableStateOf(NotesAndPasswordsCurrentList.Notes)
            }
        }
        val onTitleChange : (String) -> Unit = {
                newTitle ->  nap!!.title.value = newTitle
        }
        val onSwipeRight = {
            scope.launch {
                if (currentList == NotesAndPasswordsCurrentList.Notes) {
                    currentList = NotesAndPasswordsCurrentList.Passwords
                }
            }
            Unit
        }
        val onSwipeLeft = {
            scope.launch {
                if (currentList == NotesAndPasswordsCurrentList.Passwords) {
                    currentList = NotesAndPasswordsCurrentList.Notes
                }
            }
            Unit
        }
        val onFabSaveClick = {
            nap!!.apply {
                viewModel.saveNap(this)
                onNavigateBack()
            }
            Unit
        }
        val onFabAddClick = {
            nap!!.apply {
                if (currentList == NotesAndPasswordsCurrentList.Notes) {
                    notes.add(Note())
                } else
                {
                    credentials.add(Credential())
                }

            }
            Unit
        }

        NotesAndPasswordsDetailStateless(
            nap = nap!!,
            currentList = currentList,
            onTitleChange = onTitleChange,
            onSwipeRight = onSwipeRight,
            onSwipeLeft = onSwipeLeft,
            onFabSaveClick = onFabSaveClick,
            onFabAddClick = onFabAddClick
        )
    }
}

@Composable
fun NotesAndPasswordsDetailStateless(
    nap: Nap,
    currentList: NotesAndPasswordsCurrentList,
    onTitleChange: (String) -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
    onFabSaveClick: () -> Unit,
    onFabAddClick: () -> Unit,
) {

    Log.v("NotesAndPasswordsDetail","$currentRecomposeScope")

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        elevation = 10.dp
    ) {
        Box {
            Column {
                // Picture and title
                NotesAndPasswordsDetailTop(
                    currentList = currentList,
                    nap = nap,
                    napTitle = nap.title,
                    onTitleChange = onTitleChange
                )
                // Notes or passwords
                NotesAndPasswordsDetailBottom(
                    currentList = currentList,
                    nap = nap,
                    onSwipeRight = onSwipeRight,
                    onSwipeLeft = onSwipeLeft
                )
            }
            FloatingActionButton(
                onClick = onFabAddClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart)
            ) {
                Icon(Icons.Filled.Add,"")
            }
            FloatingActionButton(
                onClick = onFabSaveClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Filled.Check,"")
            }

        }

    }

}


@Composable
fun NotesAndPasswordsDetailTop(
    currentList: NotesAndPasswordsCurrentList,
    nap: Nap,
    napTitle: MutableState<String>,
    onTitleChange: (String) -> Unit,
) {

    val initialUri  = if (nap.image == "") {
        null
    } else {
        Uri.parse(nap.image)
    }
    Log.v("NotesAndPasswordsDetail","Initial Uri - $initialUri")
    var imageUri by remember {
        mutableStateOf(initialUri)
    }
    Log.v("NotesAndPasswordsDetail","Image Uri - $imageUri")
    val context = LocalContext.current
    val bitmap =  remember {
        mutableStateOf<Bitmap?>(null)
    }
    Log.v("NotesAndPasswordsDetail","bitmap - $bitmap")
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = createCopyAndReturnRealPath(context = context,uri = it)
        }
    }

    imageUri?.let {
        if (Build.VERSION.SDK_INT < 28) {
            @Suppress("deprecation")
            bitmap.value = MediaStore.Images
                .Media.getBitmap(context.contentResolver,it)
        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver,it)
            try {
                bitmap.value = ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                Toast.makeText(LocalContext.current,e.localizedMessage,Toast.LENGTH_SHORT).show()
            }
        }
        nap.image = it.toString()
        Log.v("NotesAndPasswordsDetail","Image assigned")
    }

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier
            .height(208.dp)
            .padding(8.dp),

        ) {
        TopImageWithPermission(
            bitmap = bitmap,
            onImageWithPermissionClick = {
                launcher.launch("image/*")
            }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp))
            OutlinedTextField(
                value = napTitle.value,
                onValueChange = {
                    onTitleChange(it)
                },
                label = {Text(
                    text = "Title",
                    textAlign = TextAlign.Center,
                )},
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = MaterialTheme.colors.secondary,
                    unfocusedLabelColor = MaterialTheme.colors.secondary),
                modifier = Modifier
//                    .fillMaxSize()
                    .wrapContentHeight(CenterVertically)
                    .padding(start = 8.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (currentList == NotesAndPasswordsCurrentList.Notes)
            {
                CurrentListTopText(text = "Notes")
            } else
            {
                CurrentListTopText(text = "Passwords")
            }
        }
    }
}

@Composable
fun CurrentListTopText(text: String){
    Text(
        text = text,
        fontSize = 36.sp,
        color = MaterialTheme.colors.secondary,
        modifier = Modifier
            .padding(start = 8.dp)
        ,
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TopImageWithPermission(
    bitmap: MutableState<Bitmap?>,
    onImageWithPermissionClick: () -> Unit,
) {

    val storagePermissionState = rememberPermissionState(
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    var onImageClick = {}

    when (storagePermissionState.status) {
        // If the camera permission is granted, then show screen with the feature enabled
        PermissionStatus.Granted -> {
            onImageClick = onImageWithPermissionClick // Pick image
        }
        is PermissionStatus.Denied -> {

            val textToShow =
                if ((storagePermissionState.status as PermissionStatus.Denied).shouldShowRationale) {
                    // If the user has denied the permission but the rationale can be shown,
                    // then gently explain why the app requires this permission
                    "The external storage is important for this app. Please grant the permission."
                } else {
                    // If it's the first time the user lands on this feature, or the user
                    // doesn't want to be asked again for this permission, explain that the
                    // permission is required
                    "Storage permission required for this feature to be available. " +
                            "Please grant the permission"
                }
            Toast.makeText(LocalContext.current,textToShow,Toast.LENGTH_SHORT).show()
            onImageClick = { storagePermissionState.launchPermissionRequest() }

        }
    }


    if(bitmap.value==null) {
        Image(
            painter = painterResource(id = R.drawable.placeholder_image),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            colorFilter = ColorFilter.tint(color = MaterialTheme.colors.secondary),
            modifier = Modifier
                .size(192.dp)
                .fillMaxSize()
                .border(width = 1.dp,
                    color = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(10.dp))
                .clickable { onImageClick() }
        )
    } else {
        Image(
            painter = BitmapPainter(bitmap.value!!.asImageBitmap()),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(192.dp)
                .fillMaxSize()
                .clickable { onImageClick() }
                .clip(RoundedCornerShape(10.dp))

        )
    }

}

@Composable
fun NotesAndPasswordsDetailBottom(
    currentList: NotesAndPasswordsCurrentList,
    nap: Nap,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
){
    var offsetX by remember { mutableStateOf(0f)}
    var offsetY by remember { mutableStateOf(0f)}
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x, _) = dragAmount
                    when {
                        x > 50 -> { onSwipeLeft() }
                        x < -50 -> { onSwipeRight() }
                    }
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }

    ){
        when (currentList) {
            NotesAndPasswordsCurrentList.Notes -> NotesList(notes = nap.notes)
            NotesAndPasswordsCurrentList.Passwords -> PasswordsList(credentials = nap.credentials)
        }
    }
}

@Composable
fun NotesList(notes : List<Note>) {

    Box(
        Modifier
            .padding(8.dp)
            .background(
                color = PinkSuperLight,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = notes) { note ->
                NoteElement(note)
            }
        }
    }
}

@Composable
fun NoteElement(note: Note) {

    var expandedState by rememberSaveable { mutableStateOf(false) }
    var titleState by rememberSaveable { mutableStateOf(note.title) }
    var contentState by rememberSaveable { mutableStateOf(note.content) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { expandedState = !expandedState }
    ) {
        if (!expandedState) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize()
            ) {
                Text(text = note.title)
            }
        } else {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    NoteElementTextField(
                        stateValue = titleState,
                        labelText = "Title",
                        onValueChange = {
                            note.title = it
                            titleState = it
                        }
                    )
                    NoteElementTextField(
                        stateValue = contentState,
                        labelText = "Content",
                        onValueChange = {
                            note.content = it
                            contentState = it
                        }
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.close_fullscreen_48px),
                    contentDescription = null,
                    modifier = Modifier
                        .align(TopEnd)
                        .size(24.dp)
                        .padding(4.dp)
                        .clickable { expandedState = !expandedState })
            }
        }
    }
}

@Composable
fun NoteElementTextField(
    stateValue: String,
    labelText: String,
    onValueChange: (String) -> Unit

){
    TextField(
        value = stateValue,
        label = {Text(text = labelText)},
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            unfocusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedLabelColor = MaterialTheme.colors.secondary),
        modifier = Modifier
            .fillMaxSize())
}

@Composable
fun PasswordsList(credentials : List<Credential>) {
    Box(
        Modifier
            .padding(8.dp)
            .background(
                color = PinkSuperLight,
                shape = RoundedCornerShape(5.dp)
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = credentials) { credential ->
                CredentialElement(credential)
            }
        }
    }
}

@Composable
fun CredentialElement(credential : Credential) {

    var expandedState by rememberSaveable { mutableStateOf(false) }
    var titleState by rememberSaveable { mutableStateOf(credential.title) }
    var loginState by rememberSaveable { mutableStateOf(credential.login) }
    var passwordState by rememberSaveable { mutableStateOf(credential.password) }

    Card(
        modifier = Modifier
            .padding(8.dp)
            .clickable { expandedState = !expandedState }
    ) {
        if (!expandedState) {
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxSize(),
            ) {
                Text(text = titleState)
            }
        } else
        {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    CredentialElementTextField(
                        stateValue = titleState,
                        labelText = "Title",
                        onValueChange = {
                            credential.title = it
                            titleState = it
                                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    CredentialElementTextField(
                        stateValue = loginState,
                        labelText = "Login",
                        onValueChange = {
                            credential.login = it
                            loginState = it
                                        }
                    )
                    CredentialElementTextField(
                        stateValue = passwordState,
                        labelText = "Password",
                        onValueChange = {
                            credential.password = it
                            passwordState = it
                                        }
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.close_fullscreen_48px),
                    contentDescription = null,
                    modifier = Modifier
                        .align(TopEnd)
                        .size(24.dp)
                        .padding(4.dp)
                        .clickable { expandedState = !expandedState })
            }
        }
    }
}

@Composable
fun CredentialElementTextField(
    stateValue: String,
    labelText: String,
    onValueChange: (String) -> Unit,
    keyboardOptions : KeyboardOptions = KeyboardOptions()
) {

    TextField(
        value = stateValue,
        label = {Text(text = labelText)},
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            unfocusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedLabelColor = MaterialTheme.colors.secondary),
        modifier = Modifier
            .fillMaxSize()
    )

}

enum class NotesAndPasswordsCurrentList {
    Notes, Passwords
}

