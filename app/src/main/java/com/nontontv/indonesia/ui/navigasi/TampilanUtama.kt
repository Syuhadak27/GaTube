package com.nontontv.indonesia.ui.navigasi

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nontontv.indonesia.ui.channel.ChannelScreen
import com.nontontv.indonesia.ui.components.DialogPembaruan
import com.nontontv.indonesia.ui.detail.DetailScreen
import com.nontontv.indonesia.ui.home.HomeScreen
import com.nontontv.indonesia.ui.library.LibraryScreen
import com.nontontv.indonesia.ui.login.TautkanPerangkatScreen
import com.nontontv.indonesia.ui.player.PlayerScreen
import com.nontontv.indonesia.ui.playlist.PlaylistScreen
import com.nontontv.indonesia.ui.search.SearchScreen
import com.nontontv.indonesia.ui.settings.SettingsScreen
import com.nontontv.indonesia.ui.update.UpdateViewModel

/** Kerangka utama: bottom navigation + NavHost + dialog pembaruan otomatis. */
@Composable
fun TampilanUtama() {
    val navController: NavHostController = rememberNavController()
    val updateViewModel: UpdateViewModel = viewModel()

    val updateInfo by updateViewModel.updateInfo.collectAsStateWithLifecycle()
    val sedangMengunduh by updateViewModel.sedangMengunduh.collectAsStateWithLifecycle()
    val progres by updateViewModel.progres.collectAsStateWithLifecycle()
    val perluIzin by updateViewModel.perluIzin.collectAsStateWithLifecycle()
    val pesanUpdate by updateViewModel.pesan.collectAsStateWithLifecycle()

    // Cek pembaruan otomatis setiap aplikasi dibuka.
    LaunchedEffect(Unit) { updateViewModel.periksa() }

    LaunchedEffect(perluIzin) {
        if (perluIzin) {
            updateViewModel.bukaPengaturanIzin()
            updateViewModel.konsumsiIzin()
        }
    }

    val backStackEntry by navController.currentBackStackEntryAsState()
    val ruteSekarang = backStackEntry?.destination?.route
    val ruteAtas = setOf(Rute.BERANDA, Rute.CARI, Rute.PUSTAKA, Rute.PENGATURAN)
    val tampilBarBawah = ruteSekarang in ruteAtas

    Scaffold(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.background,
        bottomBar = {
            if (tampilBarBawah) {
                NavigationBar {
                    ItemBawah(
                        ikon = Icons.Filled.Home,
                        label = "Beranda",
                        rute = Rute.BERANDA,
                        ruteSekarang = ruteSekarang,
                        onClick = { navigasiBawah(navController, Rute.BERANDA) }
                    )
                    ItemBawah(
                        ikon = Icons.Filled.Search,
                        label = "Cari",
                        rute = Rute.CARI,
                        ruteSekarang = ruteSekarang,
                        onClick = { navigasiBawah(navController, Rute.CARI) }
                    )
                    ItemBawah(
                        ikon = Icons.Filled.VideoLibrary,
                        label = "Pustaka",
                        rute = Rute.PUSTAKA,
                        ruteSekarang = ruteSekarang,
                        onClick = { navigasiBawah(navController, Rute.PUSTAKA) }
                    )
                    ItemBawah(
                        ikon = Icons.Filled.Settings,
                        label = "Pengaturan",
                        rute = Rute.PENGATURAN,
                        ruteSekarang = ruteSekarang,
                        onClick = { navigasiBawah(navController, Rute.PENGATURAN) }
                    )
                }
            }
        }
    ) { dalam ->
        NavHost(
            navController = navController,
            startDestination = Rute.BERANDA,
            modifier = Modifier.padding(dalam)
        ) {
            composable(Rute.BERANDA) {
                HomeScreen(onBukaVideo = { navController.navigate(Rute.detail(it)) })
            }
            composable(Rute.CARI) {
                SearchScreen(onBukaVideo = { navController.navigate(Rute.detail(it)) })
            }
            composable(Rute.PUSTAKA) {
                LibraryScreen(onBukaVideo = { navController.navigate(Rute.detail(it)) })
            }
            composable(Rute.PENGATURAN) {
                SettingsScreen(navController)
            }
            composable(
                route = Rute.DETAIL,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) { entry ->
                DetailScreen(
                    videoId = entry.arguments?.getString("videoId").orEmpty(),
                    onBukaPemain = { navController.navigate(Rute.pemain(it)) },
                    onBukaKanal = { navController.navigate(Rute.kanal(it)) }
                )
            }
            composable(
                route = Rute.PEMAIN,
                arguments = listOf(navArgument("videoId") { type = NavType.StringType })
            ) { entry ->
                PlayerScreen(
                    videoId = entry.arguments?.getString("videoId").orEmpty(),
                    padaTutup = { navController.popBackStack() }
                )
            }
            composable(
                route = Rute.KANAL,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { entry ->
                ChannelScreen(
                    url = entry.arguments?.getString("url").orEmpty(),
                    onBukaVideo = { navController.navigate(Rute.detail(it)) }
                )
            }
            composable(
                route = Rute.PLAYLIST,
                arguments = listOf(navArgument("url") { type = NavType.StringType })
            ) { entry ->
                PlaylistScreen(
                    url = entry.arguments?.getString("url").orEmpty(),
                    onBukaVideo = { navController.navigate(Rute.detail(it)) }
                )
            }
            composable(Rute.TAUTAN) {
                TautkanPerangkatScreen(padaKembali = { navController.popBackStack() })
            }
        }
    }

    updateInfo?.let { info ->
        DialogPembaruan(
            versiBaru = info.versionName,
            changelog = info.changelog,
            sedangMengunduh = sedangMengunduh,
            progres = progres,
            perluIzin = perluIzin,
            pesan = pesanUpdate,
            padaPerbarui = updateViewModel::mulaiUnduh,
            padaNanti = updateViewModel::tutupDialog
        )
    }
}

@Composable
private fun RowScope.ItemBawah(
    ikon: ImageVector,
    label: String,
    rute: String,
    ruteSekarang: String?,
    onClick: () -> Unit
) {
    NavigationBarItem(
        selected = ruteSekarang == rute,
        onClick = onClick,
        icon = { Icon(ikon, contentDescription = label) },
        label = { Text(label) }
    )
}

private fun navigasiBawah(navController: NavHostController, rute: String) {
    navController.navigate(rute) {
        popUpTo(Rute.BERANDA) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
