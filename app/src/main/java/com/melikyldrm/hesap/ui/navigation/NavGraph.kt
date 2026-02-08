package com.melikyldrm.hesap.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.melikyldrm.hesap.ui.screens.basic.BasicCalculatorScreen
import com.melikyldrm.hesap.ui.screens.converter.ConverterScreen
import com.melikyldrm.hesap.ui.screens.exchange.ExchangeScreen
import com.melikyldrm.hesap.ui.screens.finance.FinanceScreen
import com.melikyldrm.hesap.ui.screens.history.HistoryScreen
import com.melikyldrm.hesap.ui.screens.scientific.ScientificCalculatorScreen
import com.melikyldrm.hesap.ui.screens.settings.SettingsScreen

@Composable
fun CalculatorNavHost(
    navController: NavHostController,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Basic.route,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300))
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300))
        }
    ) {
        composable(Screen.Basic.route) {
            BasicCalculatorScreen(
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick
            )
        }

        composable(Screen.Scientific.route) {
            ScientificCalculatorScreen(
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick
            )
        }

        composable(Screen.Finance.route) {
            FinanceScreen(
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick
            )
        }

        composable(Screen.Converter.route) {
            ConverterScreen(
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick
            )
        }

        composable(Screen.Exchange.route) {
            ExchangeScreen(
                onHistoryClick = onHistoryClick,
                onSettingsClick = onSettingsClick
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBackClick = { navController.popBackStack() },
                onItemClick = { history ->
                    // Navigate back and load expression
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = NavigationBarDefaults.Elevation
    ) {
        Screen.bottomNavItems.forEach { screen ->
            val selected = currentRoute == screen.route

            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = if (selected) screen.selectedIcon else screen.unselectedIcon,
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title) },
                selected = selected,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

