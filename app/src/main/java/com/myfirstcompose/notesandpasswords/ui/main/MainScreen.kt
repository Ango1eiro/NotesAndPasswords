package com.myfirstcompose.notesandpasswords.ui.main

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActionScope
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.BottomEnd
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.myfirstcompose.notesandpasswords.NotesAndPasswordsViewModel
import com.myfirstcompose.notesandpasswords.R
import com.myfirstcompose.notesandpasswords.data.SimpleNap
import com.myfirstcompose.notesandpasswords.ui.theme.NotesAndPasswordsTheme
import com.myfirstcompose.notesandpasswords.ui.theme.PinkHeavy
import com.myfirstcompose.notesandpasswords.ui.theme.PinkSuperLight
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

    val configuration = LocalConfiguration.current
    val focusManager = LocalFocusManager.current
    val coroutineScope = rememberCoroutineScope()

    val list by viewModel.allNaps.observeAsState(listOf())
    if (viewModel.currentNap.value != null) {
        viewModel.resetCurrentNap()
    }

    val screenDensity = configuration.densityDpi / 160f
    val baseOffsetValue = configuration.screenWidthDp.toFloat() * screenDensity
    val offsetValueWithIcon = baseOffsetValue - 150F

    var searchText by remember { mutableStateOf("") }
    var searchEnabled by remember { mutableStateOf(false) }
//    val searchOffset  =  remember { Animatable(offsetValueWithIcon) }
//    Log.v("MainBody", "$currentRecomposeScope - searchOffset - ${searchOffset.value}")

    var spacerHeightState by remember { mutableStateOf(0.dp) }

//    val spacerHeight: Dp by animateDpAsState(
//        targetValue = spacerHeightState,
//        tween(
//            delayMillis = 0,
//            durationMillis = 2000,
//            easing = LinearOutSlowInEasing
//        )
//    )
    Log.v("MainBody", "$currentRecomposeScope - spacerHeightStatee - $spacerHeightState")
//    Log.v("MainBody", "$currentRecomposeScope - spacerHeight - $spacerHeight")

    val invertSearchState: () -> Unit = {
        searchEnabled = !searchEnabled
        spacerHeightState = if (searchEnabled) {
            56.dp
        } else {
            0.dp
        }
    }




    val onSearch: KeyboardActionScope.() -> Unit = {
        focusManager.clearFocus()
    }

    Box(){
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatableSpacer(spacerHeightState)
            NotesAndPasswordsList(list = list,
                onListElementClick = onListElementClick,
                onListElementDismiss = onListElementDismiss,
                onFabClick = onFabClick)
        }
        SearchView(
            value = searchText,
//            searchOffset = searchOffset,
            searchEnabled = searchEnabled,
            onValueChange = {
                searchText = it
                viewModel.setSearchText(searchText)
            },
            onSearch = onSearch,
            screenDensity = screenDensity,
            baseOffsetValue = baseOffsetValue,
            offsetValueWithIcon = offsetValueWithIcon,
            invertSearchState = invertSearchState
//            onLeadingIconClick = onLeadingIconClick,
//            onTrailingIconClick = onTrailingIconClick
        )
    }


}

@Composable
fun AnimatableSpacer(spacerHeightState : Dp){
    val spacerHeight: Dp by animateDpAsState(
        targetValue = spacerHeightState,
        tween(
            delayMillis = 0,
            durationMillis = 2000,
            easing = LinearOutSlowInEasing
        )
    )
    Spacer(modifier = Modifier.height(spacerHeight))
}

@Composable
fun SearchView(
    value: String,
//    searchOffset: Animatable<Float, AnimationVector1D>,
    searchEnabled: Boolean,
    onValueChange: (String) -> Unit = {},
    onSearch: KeyboardActionScope.() -> Unit = {},
    onLeadingIconClick: () -> Unit = {},
    onTrailingIconClick: () -> Unit = {},
    invertSearchState: () -> Unit = {},
    screenDensity: Float,
    baseOffsetValue: Float,
    offsetValueWithIcon: Float) {

    val coroutineScope = rememberCoroutineScope()
    val searchOffset  =  remember { Animatable(offsetValueWithIcon) }

    val onLeadingIconClick: () -> Unit = {
        invertSearchState()
        coroutineScope.launch {
            searchOffset.animateTo(
                targetValue = (baseOffsetValue/2 - searchViewWidth.value* screenDensity/2),
                animationSpec = tween(
                    durationMillis = 2000,
                    delayMillis = 0
                )
            )
        }
    }

    val onTrailingIconClick: () -> Unit = {
        invertSearchState()
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
                    modifier = Modifier.clickable { onLeadingIconClick() })
            },
            trailingIcon = {
                Icon(
                    imageVector = Icons.Filled.ArrowForward,
                    contentDescription = null,
                    tint = PinkHeavy,
                    modifier = Modifier.clickable { onTrailingIconClick() })
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

@OptIn(ExperimentalMaterialApi::class)
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
                .padding(8.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally

        ) {
            items(
                items = list,
                key = {
                    it.id
                }
            ) { nap ->
//                NotesAndPasswordsListItem(nap,onListElementClick)
                SwipeToDismissElement(nap, onListElementClick, onListElementDismiss)
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
    val context = LocalContext.current
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
            var painter: Painter = painterResource(id = R.drawable.placeholder_image)
            var colorFilter: ColorFilter? = ColorFilter.tint(color = MaterialTheme.colors.secondary)
            if (nap.image == "") {
//                painter = painterResource(id = R.drawable.placeholder_image)
            } else {
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
                        Toast.makeText(LocalContext.current, e.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }

                }
                bitmap?.let {
                    painter = BitmapPainter(it.asImageBitmap())
                    colorFilter = null
                }

            }
            Image(
                painter = painter,
                colorFilter = colorFilter,
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
        }
    }
}

@ExperimentalMaterialApi
@Composable
fun SwipeToDismissElement(
    nap: SimpleNap,
    onListElementClick: (Long) -> Unit,
    onListElementDismiss: (Long) -> Unit,
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
        NotesAndPasswordsListItem(SimpleNap(id = 55, title = "Test title", image = ""))
    }
}

@Preview(showBackground = true)
@Composable
fun AlertDialogBeforeDeletePreview() {
    NotesAndPasswordsTheme {
        AlertDialogBeforeDelete()
    }
}

val searchViewWidth = 256.dp





