package com.myfirstcompose.notesandpasswords.ui.main

import android.content.Context
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Alignment.Companion.Center
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.myfirstcompose.notesandpasswords.NapListType
import com.myfirstcompose.notesandpasswords.NotesAndPasswordsViewModel
import com.myfirstcompose.notesandpasswords.R
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.data.Tag
import com.myfirstcompose.notesandpasswords.ui.theme.*
import com.myfirstcompose.notesandpasswords.utils.getSimpleNapList
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MainBody(
    onListElementClick: (Long) -> Unit = {},
    onListElementDismiss: (Long) -> Unit = {},
    onFabClick: () -> Unit = {},
    viewModel: NotesAndPasswordsViewModel,
) {

    val list by viewModel.allNaps.observeAsState(listOf())
    val napListTypeState = viewModel.napListType.collectAsState()
    val showFilter = viewModel.filterState.collectAsState()
    val availableTags by viewModel.tags.observeAsState()
    val selectedTags by viewModel.selectedTags.collectAsState()

    MainBodyStateless(
        list = list,
        onListElementClick = onListElementClick,
        onListElementDismiss = onListElementDismiss,
        onFabClick = onFabClick,
        updateSavableSearchText = viewModel::setSearchText,
        napListType = napListTypeState.value,
        showFilter = showFilter.value,
        availableTags = availableTags,
//        closeTagsPicker = viewModel::setShowSelectedTagsPickerState,
        addTagToSelection = viewModel::addTagToSelection,
        removeTagFromSelection = viewModel::removeTagFromSelection,
        selectedTags = selectedTags

    )

}

@Composable
fun rememberScreenConfiguration(
    configuration: Configuration = LocalConfiguration.current,
) = remember(configuration){
    ScreenConfiguration(configuration)
}

class ScreenConfiguration(
    configuration: Configuration
){
    val screenDensity = configuration.densityDpi / 160f
    val baseOffsetValue = configuration.screenWidthDp.toFloat() * screenDensity
    val offsetValueWithIcon = baseOffsetValue - 150F
    val targetOffsetValue = (baseOffsetValue / 2 - searchViewWidth.value * screenDensity / 2)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MainBodyStateless(
    list: List<SimpleNap> = listOf(),
    onListElementClick: (Long) -> Unit = {},
    onListElementDismiss: (Long) -> Unit = {},
    onFabClick: () -> Unit = {},
    updateSavableSearchText: (String) -> Unit = {},
    napListType: NapListType = NapListType.VerticalList,
    showFilter: Boolean = false,
    availableTags: List<Tag>? = emptyList(),
    closeTagsPicker: (Boolean) -> Unit = {},
    addTagToSelection: (String) -> Unit = {},
    removeTagFromSelection: (String) -> Unit = {},
    selectedTags: List<String>
){

    val focusManager = LocalFocusManager.current
    val screenConfiguration = rememberScreenConfiguration()

    var searchText by remember { mutableStateOf("") }
    var searchEnabled by remember { mutableStateOf(false) }
    var spacerHeightState by remember { mutableStateOf(0) }

    val updateSearchState: (Boolean) -> Unit = {
        searchEnabled = it
    }

    val updateHeightState: (Boolean) -> Unit = { expanded ->
        spacerHeightState = if (expanded) {
            56
        } else {
            0
        }
    }

    val onSearch: KeyboardActionScope.() -> Unit = {
        focusManager.clearFocus()
    }

    val onSearchValueChange: (String) -> Unit = {
        searchText = it
        updateSavableSearchText(searchText)
    }

    Box {
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            AnimatableSpacer(spacerHeightState.dp)
            if (showFilter && (availableTags?.isEmpty() == false)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(
                        items = availableTags,
                        itemContent = { item ->
                            TagChip(item = item, addTagToSelection = addTagToSelection, removeTagFromSelection = removeTagFromSelection)
                        }
                    )
                }
            }
            if (napListType == NapListType.VerticalList) {
                NotesAndPasswordsList(list = list,
                    onListElementClick = onListElementClick,
                    onListElementDismiss = onListElementDismiss,
                    onFabClick = onFabClick)
            } else {
                NotesAndPasswordsGrid(list = list,
                    onListElementClick = onListElementClick,
                    onListElementDismiss = onListElementDismiss,
                    onFabClick = onFabClick)
            }
        }
        SearchView(
            value = searchText,
            searchEnabled = searchEnabled,
            onValueChange = onSearchValueChange,
            onSearch = onSearch,
            targetOffsetValue = screenConfiguration.targetOffsetValue,
            offsetValueWithIcon = screenConfiguration.offsetValueWithIcon,
            updateSearchState = updateSearchState,
            updateHeightState = updateHeightState,
        )
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TagChip(
    item: Tag,
    addTagToSelection: (String) -> Unit = {},
    removeTagFromSelection: (String) -> Unit = {},
) {
    var chipState by remember {
        mutableStateOf(false)
    }

    val backgroundColor = if (chipState) {
        PinkMedium
    } else
    {
        PinkLight
    }

    val leadingIcon : (@Composable () -> Unit)? = if (chipState) {
        { Icon(Icons.Default.Check,"") }
    } else
    {
        null
    }

    Chip(
        onClick = {
            if (chipState) {
                removeTagFromSelection(item.name)
            } else {
                addTagToSelection(item.name)
            }
            chipState = !chipState
        },
        colors = ChipDefaults.chipColors(backgroundColor = backgroundColor),
        leadingIcon = leadingIcon,
        modifier = Modifier.offset(y = 4.dp)
    ) {
        Text(text = item.name)
    }
}

@Composable
fun AnimatableSpacer(spacerHeightState: Dp) {
    val delay = if (spacerHeightState == 0.dp) {
        1000
    } else {
        0
    }
    val spacerHeight: Dp by animateDpAsState(
        targetValue = spacerHeightState,
        tween(
            delayMillis = delay,
            durationMillis = 2000,
            easing = LinearOutSlowInEasing
        )
    )
    Spacer(modifier = Modifier
        .height(spacerHeight)
        .background(Color.Transparent))
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchView(
    value: String,
    searchEnabled: Boolean,
    onValueChange: (String) -> Unit = {},
    onSearch: KeyboardActionScope.() -> Unit = {},
    updateSearchState: (Boolean) -> Unit = {},
    updateHeightState: (Boolean) -> Unit = {},
    targetOffsetValue: Float,
    offsetValueWithIcon: Float,
) {

    val coroutineScope = rememberCoroutineScope()
    val searchOffset = remember { Animatable(offsetValueWithIcon) }
    val keyboardController = LocalSoftwareKeyboardController.current

    val onLeadingIconClick: () -> Unit = {
        if (!searchEnabled) {
            updateHeightState(true)
            coroutineScope.launch {
                searchOffset.animateTo(
                    targetValue = targetOffsetValue,
                    animationSpec = tween(
                        durationMillis = 2000,
                        delayMillis = 500
                    )
                )
                updateSearchState(true)
                keyboardController?.show()
             }
        }
    }

    val onTrailingIconClick: () -> Unit = {
        updateHeightState(false)
        updateSearchState(false)
        coroutineScope.launch {
            searchOffset.animateTo(
                targetValue = offsetValueWithIcon,
                animationSpec = tween(
                    durationMillis = 3000,
                    delayMillis = 0
                )
            )
        }
    }

    Surface(
        modifier = Modifier
            .padding(top = 8.dp)
            .height(48.dp)
            .width(searchViewWidth)
            .offset {
                IntOffset(x = searchOffset.value.roundToInt(), y = 0)
            },
        shape = RoundedCornerShape(24.dp)

    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = !searchEnabled,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = null,
                    tint = PinkHeavy,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onLeadingIconClick)
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PinkHeavy,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onTrailingIconClick
                        ))
            },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(onSearch = onSearch),
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = PinkSuperLight,
                disabledTextColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent
            )
        )
    }
}

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun NotesAndPasswordsList(
    list: List<SimpleNap>,
    onListElementClick: (Long) -> Unit = {},
    onListElementDismiss: (Long) -> Unit = {},
    onFabClick: () -> Unit = {},
) {
    Box {
        LazyColumn(
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxSize()
            ,
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

        ) {
            items(
                items = list,
                key = {
                    it.id
                }
            ) { nap ->
//                NotesAndPasswordsListItem(nap,onListElementClick)
                SwipeToDismissElement(nap, onListElementClick, onListElementDismiss, modifier = Modifier.animateItemPlacement())
            }
        }
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .padding(8.dp)
                .align(BottomEnd)
        ) {
            Icon(Icons.Filled.Add, "")
        }
    }
}

@Composable
fun NotesAndPasswordsListItem(
    nap: SimpleNap,
    onListElementClick: (Long) -> Unit = {},
) {
//    val imageDataPair = getImageData(nap, LocalContext.current)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(64.dp)
            .clickable { onListElementClick(nap.id) },

        elevation = 10.dp
    ) {
        Row(
            modifier = Modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter: Painter = painterResource(id = R.drawable.placeholder_image)
            var painterState by remember {
                mutableStateOf(painter)
            }
            val colorFilter: ColorFilter? = ColorFilter.tint(color = MaterialTheme.colors.secondary)
            var colorFilterState by remember {
                mutableStateOf(colorFilter)
            }

            val context = LocalContext.current

            LaunchedEffect(nap) {
                val imageDataPair = getImageData(nap, context)
                if (imageDataPair.second != null) {
                    painterState = imageDataPair.second as Painter
                    colorFilterState = null
                }
            }

            Image(
                painter = painterState,
                colorFilter = colorFilterState,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxSize()
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = nap.title,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .weight(1F, true),
            )
            val sh = GenericShape{ size,_ ->
                moveTo(size.width, 0f)
                lineTo(size.width, size.height * 0.75F)
                lineTo(size.width * 0.75F, size.height)
                lineTo(0f, size.height)
            }
            Box(
                modifier = Modifier
                    .size(64.dp)
//                    .clip(sh)
                    .background(PinkMedium, shape = sh)
            ) {
                Text(
                    text = nap.tag,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    overflow = TextOverflow.Visible,
                    modifier = Modifier
                        .align(alignment = Center)
                        .offset(x = (12).dp, y = (12).dp)
                        .rotate(-45F)

//                        .padding(16.dp)
                )

            }

        }
    }
}

@Composable
fun NotesAndPasswordsGridItem(
    nap: SimpleNap,
    onListElementClick: (Long) -> Unit = {},
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
            .height(64.dp)
            .clickable { onListElementClick(nap.id) },
        elevation = 10.dp
    ) {

        val painter: Painter = painterResource(id = R.drawable.placeholder_image)
        var painterState by remember {
            mutableStateOf(painter)
        }
        val colorFilter: ColorFilter? = ColorFilter.tint(color = MaterialTheme.colors.secondary)
        var colorFilterState by remember {
            mutableStateOf(colorFilter)
        }

        val context = LocalContext.current

        LaunchedEffect(nap) {
            val imageDataPair = getImageData(nap, context)
            if (imageDataPair.second != null) {
                painterState = imageDataPair.second as Painter
                colorFilterState = null
            }
        }

        Image(
            painter = painterState,
            colorFilter = colorFilterState,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .width(96.dp)
                .fillMaxSize()
        )

    }
}

private fun getImageData(
    nap: SimpleNap,
    context: Context
): Pair<ColorFilter?, Painter?> {
    var painter: Painter? = null
    var colorFilter: ColorFilter? = null
    if (nap.image != "") {
        var bitmap: Bitmap? = null
        if (Build.VERSION.SDK_INT < 28) {
            @Suppress("deprecation")
            bitmap = MediaStore.Images
                .Media.getBitmap(context.contentResolver, Uri.parse(nap.image))

        } else {
            val source = ImageDecoder
                .createSource(context.contentResolver, Uri.parse(nap.image))
            try {
                bitmap = ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                Toast.makeText(context, e.localizedMessage, Toast.LENGTH_SHORT)
                    .show()
            }

        }
        bitmap?.let {
            painter = BitmapPainter(it.asImageBitmap())
            colorFilter = null
        }

    }
    return Pair(colorFilter, painter)
}

@ExperimentalMaterialApi
@Composable
fun SwipeToDismissElement(
    nap: SimpleNap,
    onListElementClick: (Long) -> Unit,
    onListElementDismiss: (Long) -> Unit,
    modifier: Modifier = Modifier
) {

    val scope = rememberCoroutineScope()

    val dismissState = rememberDismissState(
        initialValue = DismissValue.Default
    )
    if (dismissState.isDismissed(DismissDirection.EndToStart) || dismissState.isDismissed(
            DismissDirection.StartToEnd)
    ) {
        AlertDialogBeforeDelete(
            onYes = {
                onListElementDismiss(nap.id)
            },
            onNo = {
                scope.launch { dismissState.reset() }
            }
        )
    }

    SwipeToDismiss(
        state = dismissState,
        /***  create dismiss alert Background */
        background = {
        },
        /**** Dismiss Content */
        dismissContent = {
            NotesAndPasswordsListItem(nap, onListElementClick)
        },
        /*** Set Direction to dismiss */
        directions = setOf(DismissDirection.EndToStart, DismissDirection.StartToEnd),
        modifier = modifier
    )
}

@Composable
fun AlertDialogBeforeDelete(
    onYes: () -> Unit = {},
    onNo: () -> Unit = {},
) {

    val showDialog = remember { mutableStateOf(true) }

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = {
                // Dismiss the dialog when the user clicks outside the dialog or on the back
                // button. If you want to disable that functionality, simply use an empty
                // onCloseRequest.
                onNo()
                showDialog.value = false
            },
            title = {
                Text(text = stringResource(R.string.question_delete_item))
            },
            confirmButton = {
                Button(
                    onClick = {
                        onYes()
                        showDialog.value = false
                    }) {
                    Text(text = stringResource(R.string.answer_yes))
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        onNo()
                        showDialog.value = false
                    })
                {
                    Text(stringResource(R.string.answer_no))
                }
            }
        )
    }

}

@Composable
fun NotesAndPasswordsGrid(
    list: List<SimpleNap>,
    onListElementClick: (Long) -> Unit = {},
    onListElementDismiss: (Long) -> Unit = {},
    onFabClick: () -> Unit = {},
) {
    Box {
        LazyVerticalGrid(
            modifier = Modifier
//                .padding(8.dp)
                .fillMaxSize()
            ,
            columns = GridCells.Adaptive(minSize = 96.dp),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)

        ) {
            items(
                items = list,
                key = {
                    it.id
                }
            ) { nap ->
                NotesAndPasswordsGridItem(nap,onListElementClick)
//                NotesAndPasswordsListItem(nap,onListElementClick)

            }
        }
        FloatingActionButton(
            onClick = onFabClick,
            modifier = Modifier
                .padding(8.dp)
                .align(BottomEnd)
        ) {
            Icon(Icons.Filled.Add, "")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NotesAndPasswordsListPreview() {
    NotesAndPasswordsTheme {
        NotesAndPasswordsList(
            list = getSimpleNapList(LocalContext.current.applicationContext)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NotesAndPasswordsListItemPreview() {
    NotesAndPasswordsTheme {
        NotesAndPasswordsListItem(SimpleNap(id = 55, title = "Test title", image = "", tag = ""))
    }
}

@Preview(showBackground = true)
@Composable
fun AlertDialogBeforeDeletePreview() {
    NotesAndPasswordsTheme {
        AlertDialogBeforeDelete()
    }
}

//@Preview(showBackground = true)
//@Composable
//fun MainBodyStatelessPreview() {
//    NotesAndPasswordsTheme {
//        MainBodyStateless(
//            list = getSimpleNapList(LocalContext.current.applicationContext)
//        )
//    }
//}

val searchViewWidth = 256.dp





