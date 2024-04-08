package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOut
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.myfirstcompose.notesandpasswords.ui.detail.NotesAndPasswordsDetail
import com.myfirstcompose.notesandpasswords.ui.main.MainBody
import com.myfirstcompose.notesandpasswords.ui.theme.NotesAndPasswordsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NotesAndPasswordsApp()
        }
    }
}

@Composable
fun NotesAndPasswordsApp() {

    val navController = rememberNavController()
    val viewModel: NotesAndPasswordsViewModel = viewModel(
        viewModelStoreOwner = LocalViewModelStoreOwner.current!!,
        factory = NotesAndPasswordsViewModelFactory(
            LocalContext.current.applicationContext
                    as Application
        )
    )

    // State of the current list type [Grid|List]
    val napListTypeState = viewModel.napListType.collectAsState()

    // Listener to destination change is used for dynamic AppBar buttons
    var canPop by remember { mutableStateOf(false) }
    navController.addOnDestinationChangedListener { controller, _, _ ->
        canPop = controller.previousBackStackEntry != null
    }

    // AppBar navigation button
    val navigationIcon: (@Composable () -> Unit)? =
        if (canPop) {
            {
                IconButton(onClick = navController::popBackStack) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = "",
                        tint = MaterialTheme.colors.onPrimary
                    )
                }
            }
        } else {
            null
        }

    // AppBar actions buttons
    val actions: (@Composable RowScope.() -> Unit) =
        if (canPop) {
            {}
        } else {
            {
                IconButton(onClick = { viewModel.invertFilterState() }) {
                    Icon(
                        painter = painterResource(R.drawable.baseline_filter_alt_black_48),
                        contentDescription = "",
                        tint = MaterialTheme.colors.onPrimary,
                        modifier = Modifier.size(36.dp)
                    )
                }
                if (napListTypeState.value == NapListType.Grid) {
                    IconButton(onClick = viewModel::switchNapListType) {
                        Icon(
                            painter = painterResource(R.drawable.list_48px),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                } else {
                    IconButton(onClick = viewModel::switchNapListType) {
                        Icon(
                            painter = painterResource(R.drawable.grid_view_48px),
                            contentDescription = "",
                            tint = MaterialTheme.colors.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }
        }

    NotesAndPasswordsTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = stringResource(id = R.string.app_name)) },
                    navigationIcon = navigationIcon,
                    actions = actions,
                    backgroundColor = MaterialTheme.colors.primaryVariant,
                    contentColor = MaterialTheme.colors.onPrimary
                )
            },
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            NotesAndPasswordsNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                viewModel = viewModel
            )
        }
    }
}

@Composable
fun NotesAndPasswordsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    viewModel: NotesAndPasswordsViewModel,
) {
    Log.v("NavHost upper", "$currentRecomposeScope")


    NavHost(
        navController = navController,
        startDestination = NotesAndPasswordsListScreen.Main.name,
        modifier = modifier
    ) {
        composable(
            route = NotesAndPasswordsListScreen.Main.name,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            viewModel.setSearchText("")
            MainBody(
                onListElementClick = { id ->
                    navController.navigate("${NotesAndPasswordsListScreen.Detail.name}/$id")
                    Log.v("NavHost", "Clicked")
                },
                onListElementDismiss = { id ->
                    viewModel.deleteNap(id)
                },
                viewModel = viewModel,
                onFabClick = { navController.navigate("${NotesAndPasswordsListScreen.Detail.name}/-1") })
        }
        composable(
            route = "${NotesAndPasswordsListScreen.Detail.name}/{id}",
            enterTransition = { EnterTransition.None },
//            exitTransition = {ExitTransition.None},
            exitTransition = {
                fadeOut()
            },
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                }
            ),
        ) { entry ->
            val id = entry.arguments?.getLong("id") ?: -1
            Log.v("NavHost inside", "Before detail $currentRecomposeScope")
            Log.v("NavHost inside", "navController $navController")
            Log.v("NavHost inside", "id $id")
            Log.v("NavHost inside", "viewModel $viewModel")
            NotesAndPasswordsDetail(
                id = id,
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

    }

}

