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
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
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
import androidx.compose.material.icons.filled.Clear
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomCenter
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionStatus
import com.google.accompanist.permissions.rememberPermissionState
import com.myfirstcompose.notesandpasswords.NotesAndPasswordsViewModel
import com.myfirstcompose.notesandpasswords.R
import com.myfirstcompose.notesandpasswords.data.Credential
import com.myfirstcompose.notesandpasswords.data.Nap
import com.myfirstcompose.notesandpasswords.data.Note
import com.myfirstcompose.notesandpasswords.data.Tag
import com.myfirstcompose.notesandpasswords.ui.theme.*
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

    val tags = viewModel.tags.observeAsState()

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

        val deleteNote: (Note) -> Unit = { note ->
            nap!!.notes.remove(note)
        }

        val deleteCredential: (Credential) -> Unit = { credential ->
            nap!!.credentials.remove(credential)
        }

        NotesAndPasswordsDetailStateless(
            nap = nap!!,
            currentList = currentList,
            onTitleChange = onTitleChange,
            onSwipeRight = onSwipeRight,
            onSwipeLeft = onSwipeLeft,
            onFabSaveClick = onFabSaveClick,
            onFabAddClick = onFabAddClick,
            deleteNote = deleteNote,
            deleteCredential = deleteCredential,
            tags = tags.value,
            onNewTag = viewModel::newTag
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
    deleteNote: (Note) -> Unit = {},
    deleteCredential: (Credential) -> Unit = {},
    tags: List<Tag>? = emptyList(),
    onNewTag: (String) -> Unit = {}
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
                    nap = nap,
                    onTitleChange = onTitleChange,
                    tags = tags,
                    onNewTag = onNewTag
                )
                // Notes or passwords
                NotesAndPasswordsDetailBottom(
                    currentList = currentList,
                    nap = nap,
                    onSwipeRight = onSwipeRight,
                    onSwipeLeft = onSwipeLeft,
                    deleteNote = deleteNote,
                    deleteCredential = deleteCredential
                )
            }
//            val sh = GenericShape{ size,_ ->
//                lineTo(size.width * 0.75F, 0F)
//                lineTo(size.width, size.height * 0.25F)
//                lineTo(size.width, size.height)
//
//            }
//            Box(
//                modifier = Modifier
//                    .size(96.dp)
//                    .clip(sh)
//                    .background(PinkMedium)
//                    .align(alignment = TopEnd)
//            )
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
    nap: Nap,
    onTitleChange: (String) -> Unit,
    tags: List<Tag>?,
    onNewTag: (String) -> Unit = {}
) {

    var imageUri by remember {
        mutableStateOf(getInitialUri(nap.image))
    }

    val context = LocalContext.current
    val bitmap = remember {
        mutableStateOf<Bitmap?>(null)
    }

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
            TagDropdownMenu(currentTag = nap.tag, tags = tags, onNewTag = onNewTag )
        }
    }
}

@Composable
fun TagDropdownMenu(
    tags: List<Tag>?,
    currentTag: MutableState<String>,
    onNewTag: (String) -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }

    val defaultTagText = stringResource(R.string.text_seelct_tag)
    var tagText by remember {
        mutableStateOf(currentTag.value.ifEmpty {
            defaultTagText
        })
    }

    val showDialog = remember { mutableStateOf(false) }

    Log.v("TagDropdownMenu", "$currentRecomposeScope")

    Box(modifier = Modifier
        .fillMaxSize()
        .wrapContentSize(Alignment.TopStart)) {
        Text(
            tagText,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth()
                .clickable(onClick = { expanded = true })
                .align(Center)
                .wrapContentSize()
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            offset = DpOffset(x = 8.dp, y = 0.dp),
            modifier = Modifier
                .background(PinkMedium)
        ) {
            tags?.forEachIndexed { index, s ->
                DropdownMenuItem(onClick = {
                    expanded = false
                    currentTag.value = tags[index].name
                    tagText = tags[index].name
                }){
                    Text(text=s.name)
                }
            }
            DropdownMenuItem(
                onClick = {
                    showDialog.value = true
                }
            ){
                Text(text= stringResource(R.string.text_create_new_tag))
            }
        }
        DialogNewTag(onNewTag = onNewTag,showDialog = showDialog)
    }
}

@Composable
fun DialogNewTag(
    onNewTag: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
    showDialog: MutableState<Boolean>,
) {

    if (showDialog.value) {

        var newTagText by remember {
            mutableStateOf("")
        }

        Dialog(onDismissRequest = onDismiss) {
            Surface(shape = MaterialTheme.shapes.medium) {
                Column {
                    Column(Modifier.padding(24.dp)) {
                        Text(text = stringResource(R.string.text_enter_tag_name))
                        Spacer(Modifier.size(16.dp))
                        OutlinedTextField(
                            value = newTagText,
                            onValueChange = {newTagText = it},
                            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                unfocusedBorderColor = MaterialTheme.colors.secondary,
                                unfocusedLabelColor = MaterialTheme.colors.secondary),
                           )
                    }
                    Row(
                        Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        Arrangement.spacedBy(8.dp, Alignment.End),
                    ) {
                        Button(
                            onClick = {
                                onDismiss()
                                showDialog.value = false
                            })
                        {
                            Text(stringResource(R.string.answer_no))
                        }
                        Button(
                            onClick = {
                                onNewTag(newTagText)
                                showDialog.value = false
                            }) {
                            Text(text = stringResource(R.string.answer_yes))
                        }
                    }
                }
            }
        }
    }
}

private fun getInitialUri(napImage: String) =
    if (napImage == "") {
        null
    } else {
        Uri.parse(napImage)
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
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.secondary,
                    shape = RoundedCornerShape(10.dp)
                )
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
    deleteNote: (Note) -> Unit = {},
    deleteCredential: (Credential) -> Unit = {},
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
        if (currentList == NotesAndPasswordsCurrentList.Notes) {
            Bookmark(onClick = onSwipeRight,stringResource(R.string.text_passwords), offset = Pair(-8,-12),color = PinkLight)
            Bookmark(onClick = onSwipeLeft,stringResource(R.string.text_notes), offset = Pair(-40,-12),color = PinkSuperLight)
        } else {
            Bookmark(onClick = onSwipeLeft,stringResource(R.string.text_notes), offset = Pair(-40,-12),color = PinkLight)
            Bookmark(onClick = onSwipeRight,stringResource(R.string.text_passwords), offset = Pair(-8,-12),color = PinkSuperLight)
        }
        when (currentList) {
            NotesAndPasswordsCurrentList.Notes -> NotesList(notes = nap.notes,deleteNote = deleteNote)
            NotesAndPasswordsCurrentList.Passwords -> PasswordsList(credentials = nap.credentials,deleteCredential = deleteCredential)
        }
    }
}

@Composable
fun BoxScope.Bookmark(onClick: () -> Unit,text: String, offset: Pair<Int,Int>, color: Color) {
    Button(
        onClick = onClick,
        elevation = null,
        colors = ButtonDefaults.buttonColors(backgroundColor = color),
        contentPadding = PaddingValues(top = 2.dp),
        shape = RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp),
        modifier = Modifier
            .align(TopEnd)
            .offset(x = (offset.first).dp, y = (offset.second).dp)
            .height(20.dp)
    ) {
        Text(text = text)
    }
}

@Composable
fun NotesList(
    notes: List<Note>,
    deleteNote: (Note) -> Unit = {},
) {

    Box(
        Modifier
            .padding(8.dp)
            .background(
                color = PinkSuperLight,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 0.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = notes) { note ->
                NoteElement(note, deleteNote = deleteNote)
            }
        }
    }
}

@Composable
fun NoteElement(
    note: Note,
    deleteNote: (Note) -> Unit = {},
) {

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
                ) {
                    Text(text = stringResource(R.string.text_collapse))
                }
                Button(
                    onClick = {deleteNote(note)},
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = RedDelete,
                        contentColor = MaterialTheme.colors.onPrimary,
                        disabledBackgroundColor = RedDelete,
                    ),
                    contentPadding = PaddingValues(bottom = 6.dp),
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .align(BottomCenter)
                        .offset(y = (8).dp)
                        .height(26.dp)
                ) {
                    Text(text = stringResource(R.string.text_delete))
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
        label = { Text(text = labelText, color = MaterialTheme.colors.primary) },
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
fun PasswordsList(
    credentials: List<Credential>,
    deleteCredential: (Credential) -> Unit = {},
) {
    Box(
        Modifier
            .padding(8.dp)
            .background(
                color = PinkSuperLight,
                shape = RoundedCornerShape(
                    topStart = 5.dp,
                    topEnd = 0.dp,
                    bottomStart = 5.dp,
                    bottomEnd = 5.dp
                )
            )
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = credentials) { credential ->
                CredentialElement(credential,deleteCredential)
            }
        }
    }
}

@Composable
fun CredentialElement(
    credential: Credential,
    deleteCredential: (Credential) -> Unit = {},
) {

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
                        },
                        showCopyToClipboardButton = true
                    )
                    CredentialElementTextField(
                        stateValue = credential.password.value,
                        labelText = stringResource(R.string.text_password),
                        onValueChange = {
                            credential.password.value = it
                        },
                        showCopyToClipboardButton = true,
                        showRevealPasswordButton = true
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
                Button(
                    onClick = {deleteCredential(credential)},
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = RedDelete,
                        contentColor = MaterialTheme.colors.onPrimary,
                        disabledBackgroundColor = RedDelete,
                    ),
                    contentPadding = PaddingValues(bottom = 6.dp),
                    shape = RoundedCornerShape(16.dp, 16.dp, 0.dp, 0.dp),
                    modifier = Modifier
                        .align(BottomCenter)
                        .offset(y = (8).dp)
                        .height(26.dp)
                ) {
                    Text(text = stringResource(R.string.text_delete))
                }
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
    showCopyToClipboardButton: Boolean = false,
    showRevealPasswordButton: Boolean = false
) {

    var showPassword by remember {
        mutableStateOf(!showRevealPasswordButton)
    }

    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val trailingIcons = @Composable {

        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .padding(
                    horizontal = 4.dp
                )
        ) {
            Spacer(modifier = Modifier.size(24.dp))
            if (showRevealPasswordButton) {
                IconButton(
                    onClick = {
                        showPassword = !showPassword
                    },
                    modifier = Modifier
                        .size(24.dp)

                ) {
                    Icon(
                        painter = painterResource(R.drawable.visibility_24px),
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary,
                    )
                }
            }
            if (showCopyToClipboardButton) {
                IconButton(
                    onClick = {
                        clipboardManager.setText(AnnotatedString((stateValue)))
                    },
                    modifier = Modifier
                        .size(24.dp)

                ) {
                    Icon(
                        painter = painterResource(R.drawable.content_copy_24px),
                        contentDescription = "",
                        tint = MaterialTheme.colors.secondary
                    )
                }
            }
        }
    }

    TextField(
        value = stateValue,
        label = { Text(text = labelText, color = MaterialTheme.colors.primary) },
        onValueChange = onValueChange,
        keyboardOptions = keyboardOptions,
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = MaterialTheme.colors.background,
            unfocusedIndicatorColor = MaterialTheme.colors.secondary,
            unfocusedLabelColor = MaterialTheme.colors.secondary),
        trailingIcon = trailingIcons,
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
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

