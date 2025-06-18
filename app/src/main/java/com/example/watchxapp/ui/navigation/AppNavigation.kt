package com.example.watchxapp.ui.navigation

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.watchxapp.R
import com.example.watchxapp.data.repository.DataSource
import com.example.watchxapp.ui.screens.chat.ChatScreen
import com.example.watchxapp.ui.screens.chat.ChatsListScreen
import com.example.watchxapp.ui.screens.home.AccountDetailScreen
import com.example.watchxapp.ui.screens.home.WatchXHomeScreen
import com.example.watchxapp.ui.screens.profile.ProfileScreen
import com.example.watchxapp.ui.screens.search.SearchScreen
import com.example.watchxapp.viewmodel.ChatViewModel
import com.example.watchxapp.viewmodel.DashboardViewModel
import com.example.watchxapp.viewmodel.SearchViewModel
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

sealed class BottomNavItem(val route: String, val label: String, @DrawableRes val iconResId: Int) {
    object Home : BottomNavItem("home", "Home", R.drawable.home_icon)
    object Search : BottomNavItem("search", "Search", R.drawable.search_icon)
    object Workspaces : BottomNavItem("workspaces", "Workspaces", R.drawable.workspaces_icon)
    object Profile : BottomNavItem("profile", "Profile", R.drawable.manage_accounts_icon)
}

@Composable
fun MainScreenView(rootNavController: NavController, isDarkTheme: Boolean, onThemeChange: (Boolean) -> Unit) {
    val navController = rememberNavController()
    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = if (isDarkTheme) R.drawable.dark_tree_img else R.drawable.light_tree_img),
            contentDescription = "App Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = { BottomNavigationBar(navController = navController) }
        ) { paddingValues ->
            Box(modifier = Modifier.padding(paddingValues)) {
                NavigationGraph(
                    navController = navController,
                    rootNavController = rootNavController,
                    isDarkTheme = isDarkTheme,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    val items = listOf(BottomNavItem.Home, BottomNavItem.Search, BottomNavItem.Workspaces, BottomNavItem.Profile)
    NavigationBar {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                selected = currentRoute?.startsWith(item.route) == true,
                onClick = {
                    if (currentRoute?.startsWith(item.route) == true && currentRoute != item.route) {
                        navController.popBackStack()
                    } else {
                        navController.navigate(item.route) {
                            navController.graph.startDestinationRoute?.let { route -> popUpTo(route) { saveState = true } }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    Image(
                        painter = painterResource(id = item.iconResId),
                        contentDescription = item.label,
                        modifier = Modifier.size(28.dp),
                        colorFilter = ColorFilter.tint(LocalContentColor.current)
                    )
                },
                label = { Text(item.label, style = MaterialTheme.typography.labelSmall) }
            )
        }
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    rootNavController: NavController,
    isDarkTheme: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    val dashboardViewModel: DashboardViewModel = viewModel()
    val chatViewModel: ChatViewModel = viewModel()
    val searchViewModel: SearchViewModel = viewModel()

    NavHost(navController = navController, startDestination = BottomNavItem.Home.route) {
        composable(BottomNavItem.Home.route) {
            WatchXHomeScreen(
                navController = navController,
                dashboardViewModel = dashboardViewModel,
                chatViewModel = chatViewModel
            )
        }
        composable(BottomNavItem.Search.route) {
            // FIX: The ChatViewModel must also be passed to the SearchScreen
            SearchScreen(
                searchViewModel = searchViewModel,
                dashboardViewModel = dashboardViewModel,
                chatViewModel = chatViewModel
            )
        }
        composable(BottomNavItem.Workspaces.route) {
            ChatsListScreen(
                chatViewModel = chatViewModel,
                onChatClick = { chatId ->
                    val chat = chatViewModel.chats.value.find { it.id == chatId }
                    val chatName = URLEncoder.encode(chat?.name ?: "Chat", StandardCharsets.UTF_8.toString())
                    navController.navigate("chat/$chatId/$chatName")
                }
            )
        }
        composable(BottomNavItem.Profile.route) {
            ProfileScreen(
                rootNavController = rootNavController,
                isDarkTheme = isDarkTheme,
                onThemeChange = onThemeChange
            )
        }
        composable(
            route = "chat/{chatId}/{chatName}",
            arguments = listOf(
                navArgument("chatId") { type = NavType.StringType },
                navArgument("chatName") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val chatId = backStackEntry.arguments?.getString("chatId")
            val chatName = backStackEntry.arguments?.getString("chatName")?.let {
                URLDecoder.decode(it, StandardCharsets.UTF_8.toString())
            } ?: "Chat"

            if (chatId != null) {
                ChatScreen(chatId = chatId, chatName = chatName, chatViewModel = chatViewModel)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: Chat ID not found.")
                }
            }
        }
        composable("account_detail/{handle}", arguments = listOf(navArgument("handle") { type = NavType.StringType })) {
            val handle = it.arguments?.getString("handle")
            val account = handle?.let { DataSource.findAccountByHandle(it) }
            if (account != null) {
                AccountDetailScreen(account = account, navController = navController)
            }
        }
    }
}