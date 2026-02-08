package com.melikyldrm.hesap.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    data object Basic : Screen(
        route = "basic",
        title = "Temel",
        selectedIcon = Icons.Filled.Calculate,
        unselectedIcon = Icons.Outlined.Calculate
    )

    data object Scientific : Screen(
        route = "scientific",
        title = "Bilimsel",
        selectedIcon = Icons.Filled.Science,
        unselectedIcon = Icons.Outlined.Science
    )

    data object Finance : Screen(
        route = "finance",
        title = "Finans",
        selectedIcon = Icons.Filled.AccountBalance,
        unselectedIcon = Icons.Outlined.AccountBalance
    )

    data object Converter : Screen(
        route = "converter",
        title = "Birim",
        selectedIcon = Icons.Filled.SwapHoriz,
        unselectedIcon = Icons.Outlined.SwapHoriz
    )

    data object Exchange : Screen(
        route = "exchange",
        title = "Döviz",
        selectedIcon = Icons.Filled.CurrencyExchange,
        unselectedIcon = Icons.Outlined.CurrencyExchange
    )

    data object History : Screen(
        route = "history",
        title = "Geçmiş",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History
    )

    data object Settings : Screen(
        route = "settings",
        title = "Ayarlar",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )

    companion object {
        val bottomNavItems = listOf(Basic, Scientific, Finance, Converter, Exchange)
    }
}

