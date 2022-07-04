package com.myfirstcompose.notesandpasswords

import android.app.Application
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.currentRecomposeScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
    NotesAndPasswordsTheme {
        val navController = rememberNavController()
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
        ) { innerPadding ->
            NotesAndPasswordsNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}

@Composable
fun NotesAndPasswordsNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    Log.v("NavHost upper", "$currentRecomposeScope")
    val owner = LocalViewModelStoreOwner.current
    val viewModel: NotesAndPasswordsViewModel = viewModel(
        viewModelStoreOwner = owner!!,
        factory = NotesAndPasswordsViewModelFactory(
            LocalContext.current.applicationContext
                    as Application)
    )

    NavHost(
        navController = navController,
        startDestination = NotesAndPasswordsListScreen.Main.name,
        modifier = modifier
    ) {
        composable(NotesAndPasswordsListScreen.Main.name) {
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
            arguments = listOf(
                navArgument("id") {
                    type = NavType.LongType
                }
            ),
        )
        { entry ->
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

