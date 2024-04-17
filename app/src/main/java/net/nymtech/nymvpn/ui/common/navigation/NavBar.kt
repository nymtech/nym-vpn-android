package net.nymtech.nymvpn.ui.common.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import net.nymtech.nymvpn.ui.AppViewModel
import net.nymtech.nymvpn.ui.NavItem
import net.nymtech.nymvpn.ui.theme.iconSize

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavBar(appViewModel: AppViewModel, navController: NavController) {
	val navBackStackEntry by navController.currentBackStackEntryAsState()
	val navItem = NavItem.from(navBackStackEntry?.destination?.route)
	val context = LocalContext.current
	val keyboardController = LocalSoftwareKeyboardController.current

	LaunchedEffect(navBackStackEntry) {
		keyboardController?.hide()
	}

	val emptyTitle = navItem.title.asString(context) == ""
	AnimatedVisibility(!emptyTitle) {
		CenterAlignedTopAppBar(
			title = {
				Text(
					navItem.title.asString(context),
					style = MaterialTheme.typography.titleLarge,
				)
			},
			actions = {
				navItem.trailing?.let {
					IconButton(
						onClick = {
							when (it) {
								NavItem.settingsIcon -> navController.navigate(NavItem.Settings.route)
								NavItem.clearLogsIcon -> appViewModel.clearLogs()
							}
						},
					) {
						Icon(
							imageVector = it,
							contentDescription = it.name,
							tint = MaterialTheme.colorScheme.onSurface,
							modifier =
							Modifier.size(
								iconSize,
							),
						)
					}
				}
			},
			navigationIcon = {
				navItem.leading?.let {
					IconButton(
						onClick = {
							when {
								it == NavItem.backIcon -> navController.popBackStack()
							}
						},
					) {
						Icon(imageVector = it, contentDescription = it.name)
					}
				}
			},
		)
	}
}
