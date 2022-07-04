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
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
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
import com.myfirstcompose.notesandpasswords.ui.theme.NotesAndPasswordsTheme
import com.myfirstcompose.notesandpasswords.ui.theme.PinkSuperLight
import com.myfirstcompose.notesandpasswords.utils.createCopyAndReturnRealPath
import com.myfirstcompose.notesandpasswords.utils.getPreviewNap
import kotlinx.coroutines.launch

@Composable
fun NotesAndPasswordsDetail(
    id: Long,
    viewModel: NotesAndPasswordsViewModel,
    onNavigateBack: () -> Unit,
) {

    val scope = rememberCoroutineScope()

    Log.v("NotesAndPasswordsDetail", "$currentRecomposeScope id - $id")
    Log.v("NotesAndPasswordsDetail", "viewModel - $viewModel")

    var nap by remember { mutableStateOf<Nap?>(null) }
    LaunchedEffect(key1 = id) {
        nap = viewModel.getNewOrExistingNapById(id)
    }

    if (nap != null) {
        Log.v("NotesAndPasswordsDetail", "nap:$nap")

        var currentList by remember {
            if (nap!!.notes.isEmpty() && !nap!!.credentials.isEmpty()) {
                mutableStateOf(NotesAndPasswordsCurrentList.Passwords)
            } else {
                mutableStateOf(NotesAndPasswordsCurrentList.Notes)
            }
        }
        val onTitleChange: (String) -> Unit = { newTitle ->
            nap!!.title.value = newTitle
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
                } else {
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
    currentList: NotesAndPasswordsCurrentList = NotesAndPasswordsCurrentList.Notes,
    onTitleChange: (String) -> Unit = {},
    onSwipeRight: () -> Unit = {},
    onSwipeLeft: () -> Unit = {},
    onFabSaveClick: () -> Unit = {},
    onFabAddClick: () -> Unit = {},
) {

    Log.v("NotesAndPasswordsDetail", "$currentRecomposeScope")

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
                Icon(Icons.Filled.Add, "")
            }
            FloatingActionButton(
                onClick = onFabSaveClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Filled.Check, "")
            }

        }

    }

}


@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesAndPasswordsDetailTop(
    currentList: NotesAndPasswordsCurrentList,
    nap: Nap,
    onTitleChange: (String) -> Unit,
) {

    val initialUri = if (nap.image == "") {
        null
    } else {
        Uri.parse(nap.image)
    }
    Log.v("NotesAndPasswordsDetail", "Initial Uri - $initialUri")
    var imageUri by remember {
        mutableStateOf(initialUri)
    }
    Log.v("NotesAndPasswordsDetail", "Image Uri - $imageUri")
    val context = LocalContext.current
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }
    Log.v("NotesAndPasswordsDetail", "bitmap - $bitmap")
    val launcher = rememberLauncherForActivityResult(contract =
    ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            imageUri = createCopyAndReturnRealPath(context = context, uri = it)
        }
    }

    imageUri?.let {
        if (Build.VERSION.SDK_INT < 28) {
            @Suppress("deprecation")
            bitmap.value = MediaStore.Images
                .Media.getBitmap(context.contentResolver, it)
        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver, it)
            try {
                bitmap.value = ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                Toast.makeText(LocalContext.current, e.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
        nap.image = it.toString()
        Log.v("NotesAndPasswordsDetail", "Image assigned")
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
                value = nap.title.value,
                onValueChange = {
                    onTitleChange(it)
                },
                label = {
                    Text(
                        text = stringResource(R.string.text_title),
                        textAlign = TextAlign.Center,
                    )
                },
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    unfocusedBorderColor = MaterialTheme.colors.secondary,
                    unfocusedLabelColor = MaterialTheme.colors.secondary),
                modifier = Modifier
                    .wrapContentHeight(CenterVertically)
                    .padding(start = 8.dp),
            )
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedContent(
                targetState = currentList,
                transitionSpec = {
                    fadeIn() + slideInHorizontally(
                        animationSpec = tween(400),
                        initialOffsetX = { fullWidth ->
                            when (targetState) {
                                NotesAndPasswordsCurrentList.Notes -> -fullWidth
                                NotesAndPasswordsCurrentList.Passwords -> fullWidth
                            }
                        }) with slideOutHorizontally(
                        animationSpec = tween(200),
                        targetOffsetX = { fullWidth ->
                            when (targetState) {
                                NotesAndPasswordsCurrentList.Notes -> fullWidth
                                NotesAndPasswordsCurrentList.Passwords -> -fullWidth
                            }
                        })
                }
            ) { targetState ->
                when (targetState) {
                    NotesAndPasswordsCurrentList.Notes -> CurrentListTopText(text = stringResource(R.string.text_notes))
                    NotesAndPasswordsCurrentList.Passwords -> CurrentListTopText(text = stringResource(
                        R.string.text_passwords))
                }
            }
        }
    }
}

@Composable
fun CurrentListTopText(text: String) {
    Text(
        text = text,
        fontSize = 36.sp,
        color = MaterialTheme.colors.secondary,
        modifier = Modifier
            .padding(start = 8.dp)
            .fillMaxSize(),
        textAlign = TextAlign.Center
    )
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun TopImageWithPermission(
    bitmap: MutableState<Bitmap?>,
    onImageWithPermissionClick: () -> Unit,
) {

    var onImageClick = {}
    Log.v("TopImageWithPermission","TopImageWithPermission + $currentRecomposeScope")

    // Execute when not in Preview mode
    if (!LocalInspectionMode.current) {

        val storagePermissionState = rememberPermissionState(
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

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
                        stringResource(R.string.explanation_permission_denied)
                    } else {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        stringResource(R.string.explanation_permission_not_granted)
                    }
                Toast.makeText(LocalContext.current, textToShow, Toast.LENGTH_SHORT).show()
                onImageClick = { storagePermissionState.launchPermissionRequest() }

            }
        }
    }

    if (bitmap.value == null) {
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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun NotesAndPasswordsDetailBottom(
    currentList: NotesAndPasswordsCurrentList,
    nap: Nap,
    onSwipeRight: () -> Unit,
    onSwipeLeft: () -> Unit,
) {
    var offsetX by remember { mutableStateOf(0f) }
    var offsetY by remember { mutableStateOf(0f) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val (x, _) = dragAmount
                    when {
                        x > 50 -> {
                            onSwipeLeft()
                        }
                        x < -50 -> {
                            onSwipeRight()
                        }
                    }
                    offsetX += dragAmount.x
                    offsetY += dragAmount.y
                }
            }

    ) {
//        when (currentList) {
//            NotesAndPasswordsCurrentList.Notes -> NotesList(notes = nap.notes)
//            NotesAndPasswordsCurrentList.Passwords -> PasswordsList(credentials = nap.credentials)
//        }
        AnimatedContent(
            targetState = currentList,
            transitionSpec = {
                fadeIn() + slideInHorizontally(
                    animationSpec = tween(400),
                    initialOffsetX = { fullWidth ->
                        when (targetState) {
                            NotesAndPasswordsCurrentList.Notes -> -fullWidth
                            NotesAndPasswordsCurrentList.Passwords -> fullWidth
                        }
                    }) with slideOutHorizontally(
                    animationSpec = tween(200),
                    targetOffsetX = { fullWidth ->
                        when (targetState) {
                            NotesAndPasswordsCurrentList.Notes -> fullWidth
                            NotesAndPasswordsCurrentList.Passwords -> -fullWidth
                        }
                    })
            }
        ) { targetState ->
            when (targetState) {
                NotesAndPasswordsCurrentList.Notes -> NotesList(notes = nap.notes)
                NotesAndPasswordsCurrentList.Passwords -> PasswordsList(credentials = nap.credentials)
            }
        }
    }
}

@Composable
fun NotesList(notes: List<Note>) {

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

    var expandedState by rememberSaveable { mutableStateOf(note.id == 0L) }

    Card(
        modifier = Modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
            )
            .padding(8.dp)
            .clickable { expandedState = !expandedState }

  ) {
        if (!expandedState) {
            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = note.title.value,

                    )
            }

        } else {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    NoteElementTextField(
                        stateValue = note.title.value,
                        labelText = stringResource(R.string.text_title),
                        onValueChange = {
                            note.title.value = it
                        }
                    )
                    NoteElementTextField(
                        stateValue = note.content.value,
                        labelText = stringResource(R.string.text_content),
                        onValueChange = {
                            note.content.value = it
                        }
                    )
                }
                Button(
                    onClick = { expandedState = !expandedState },
                    contentPadding = PaddingValues(top = 6.dp),
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    modifier = Modifier
                        .align(TopCenter)
                        .offset(y = (-8).dp)
                        .height(26.dp)
//                        .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp))
                ) {
                    Text(text = stringResource(R.string.text_collapse))
                }
            }
        }
    }
}

@Composable
fun NoteElementTextField(
    stateValue: String,
    labelText: String,
    onValueChange: (String) -> Unit,

    ) {
    TextField(
        value = stateValue,
        label = { Text(text = labelText) },
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background,
            unfocusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedLabelColor = MaterialTheme.colors.secondary),
        modifier = Modifier
            .fillMaxSize())
}

@Composable
fun PasswordsList(credentials: List<Credential>) {
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
fun CredentialElement(credential: Credential) {

    var expandedState by rememberSaveable { mutableStateOf(credential.id == 0L) }

    Card(
        modifier = Modifier
            .animateContentSize(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
            )
            .padding(8.dp)
            .clickable { expandedState = !expandedState }
    ) {
        if (!expandedState) {
            Box(
                contentAlignment = Center,
                modifier = Modifier
                    .height(32.dp)
                    .fillMaxSize()
            ) {
                Text(
                    text = credential.title.value,

                    )
            }
        } else {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                ) {
                    CredentialElementTextField(
                        stateValue = credential.title.value,
                        labelText = stringResource(id = R.string.text_title),
                        onValueChange = {
                            credential.title.value = it
                        },
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)
                    )
                    CredentialElementTextField(
                        stateValue = credential.login.value,
                        labelText = stringResource(R.string.text_login),
                        onValueChange = {
                            credential.login.value = it
                        }
                    )
                    CredentialElementTextField(
                        stateValue = credential.password.value,
                        labelText = stringResource(R.string.text_password),
                        onValueChange = {
                            credential.password.value = it
                        }
                    )
                }
                Button(
                    onClick = { expandedState = !expandedState },
                    contentPadding = PaddingValues(top = 6.dp),
                    shape = RoundedCornerShape(0.dp, 0.dp, 16.dp, 16.dp),
                    modifier = Modifier
                        .align(TopCenter)
                        .offset(y = (-8).dp)
                        .height(26.dp)
//                        .clip(RoundedCornerShape(15.dp, 15.dp, 0.dp, 0.dp))
                ) {
                    Text(text = stringResource(R.string.text_collapse))
                }
//                Image(
//                    painter = painterResource(id = R.drawable.close_fullscreen_48px),
//                    contentDescription = null,
//                    modifier = Modifier
//                        .align(TopEnd)
//                        .size(24.dp)
//                        .padding(4.dp)
//                        .clickable { expandedState = !expandedState })
            }
        }
    }
}

@Composable
fun CredentialElementTextField(
    stateValue: String,
    labelText: String,
    onValueChange: (String) -> Unit,
    keyboardOptions: KeyboardOptions = KeyboardOptions(),
) {

    TextField(
        value = stateValue,
        label = { Text(text = labelText) },
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background,
            unfocusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedLabelColor = MaterialTheme.colors.secondary),
        modifier = Modifier
            .fillMaxSize()
    )

}

@Preview(showBackground = true)
@Composable
fun NotesAndPasswordsDetailStatelessPreview() {
    NotesAndPasswordsTheme {
        NotesAndPasswordsDetailStateless(
            nap = getPreviewNap()
        )
    }
}

/* Not working as expected
@Preview(showBackground = true)
@Composable
fun NotesAndPasswordsDetailStatelessPreview() {
    NotesAndPasswordsTheme {
        CredentialElement(credential = Credential())
    }
}*/

enum class NotesAndPasswordsCurrentList {
    Notes, Passwords
}

