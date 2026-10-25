package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.AmbilJatahSheet
import com.example.ui.components.BalikPoolSheet
import com.example.ui.components.CatatLakuSheet
import com.example.ui.components.KasSheet
import com.example.ui.components.KembalikanPinjamanSheet
import com.example.ui.components.MasukPabrikSheet
import com.example.ui.components.PinjamanSheet
import com.example.ui.components.QuickActionMenuSheet
import com.example.ui.components.QuickActionType
import com.example.ui.components.SetoranSheet
import com.example.ui.components.TransferSheet
import com.example.ui.theme.CleanBg
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.KerupukViewModel
import kotlinx.coroutines.flow.collectLatest

enum class AppTab(val title: String, val icon: ImageVector) {
    HOME("Home", Icons.Default.Home),
    STOK("Stok", Icons.Default.Inventory2),
    KAS("Kas", Icons.Default.AccountBalanceWallet),
    LAPORAN("Laporan", Icons.Default.BarChart)
}

enum class CurrentScreen {
    MAIN_TABS,
    PINJAMAN_LIST
}

@Composable
fun MainContainerScreen(viewModel: KerupukViewModel) {
    var selectedTab by remember { mutableStateOf(AppTab.HOME) }
    var currentScreen by remember { mutableStateOf(CurrentScreen.MAIN_TABS) }

    var activeSheet by remember { mutableStateOf<QuickActionType?>(null) }
    var isMenuSheetOpen by remember { mutableStateOf(false) }
    var repayLoanIdTarget by remember { mutableStateOf<Long?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val activeLoans by viewModel.activeLoansWithBalance.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.snackbarEvent.collectLatest { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CleanBg,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                val isSuccess = data.visuals.message.contains("✓")
                Surface(
                    modifier = Modifier
                        .padding(16.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    color = if (isSuccess) StatusSuccess else StatusDanger,
                    shadowElevation = 4.dp
                ) {
                    Text(
                        text = data.visuals.message,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp),
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentScreen == CurrentScreen.MAIN_TABS) {
                FloatingActionButton(
                    onClick = { isMenuSheetOpen = true },
                    containerColor = ElectricBlue,
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .size(64.dp)
                        .testTag("main_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Aksi Cepat",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        },
        bottomBar = {
            if (currentScreen == CurrentScreen.MAIN_TABS) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 8.dp,
                    color = CleanSurface,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanCardBorder)
                ) {
                    NavigationBar(
                        containerColor = CleanSurface,
                        contentColor = TextPrimary,
                        tonalElevation = 0.dp,
                        modifier = Modifier.navigationBarsPadding()
                    ) {
                        AppTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon,
                                        contentDescription = tab.title,
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ElectricBlue,
                                    selectedTextColor = ElectricBlue,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary,
                                    indicatorColor = ElectricBlueLight
                                ),
                                modifier = Modifier.testTag("nav_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                CurrentScreen.MAIN_TABS -> {
                    when (selectedTab) {
                        AppTab.HOME -> HomeScreen(
                            viewModel = viewModel,
                            onOpenAction = { activeSheet = it },
                            onNavigateToPinjaman = { currentScreen = CurrentScreen.PINJAMAN_LIST }
                        )
                        AppTab.STOK -> StokScreen(
                            viewModel = viewModel,
                            onOpenAction = { activeSheet = it }
                        )
                        AppTab.KAS -> KasScreen(
                            viewModel = viewModel,
                            onOpenAction = { activeSheet = it },
                            onNavigateToPinjaman = { currentScreen = CurrentScreen.PINJAMAN_LIST }
                        )
                        AppTab.LAPORAN -> LaporanScreen(
                            viewModel = viewModel
                        )
                    }
                }
                CurrentScreen.PINJAMAN_LIST -> {
                    PinjamanListScreen(
                        viewModel = viewModel,
                        onBack = { currentScreen = CurrentScreen.MAIN_TABS },
                        onOpenAddPinjaman = { activeSheet = QuickActionType.PINJAM_SETORAN },
                        onOpenRepayLoan = { loanId ->
                            repayLoanIdTarget = loanId
                        }
                    )
                }
            }
        }

        if (isMenuSheetOpen) {
            QuickActionMenuSheet(
                onDismiss = { isMenuSheetOpen = false },
                onSelectAction = { action ->
                    activeSheet = action
                }
            )
        }

        when (activeSheet) {
            QuickActionType.CATAT_LAKU -> {
                CatatLakuSheet(
                    stokTangan = summary.stokTangan,
                    currentLakuToday = summary.lakuHariIni,
                    onDismiss = { activeSheet = null },
                    onSave = { laku, harga ->
                        viewModel.catatLaku(laku, harga) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.AMBIL_JATAH -> {
                AmbilJatahSheet(
                    stokPool = summary.stokPool,
                    onDismiss = { activeSheet = null },
                    onSave = { qty ->
                        viewModel.ambilJatahPool(qty) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.BALIK_POOL -> {
                BalikPoolSheet(
                    stokTangan = summary.stokTangan,
                    onDismiss = { activeSheet = null },
                    onSave = { qty ->
                        viewModel.balikSisaPool(qty) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.MASUK_PABRIK -> {
                MasukPabrikSheet(
                    onDismiss = { activeSheet = null },
                    onSave = { qty, ket ->
                        viewModel.masukPabrik(qty, ket) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.TRANSFER_PEDAGANG -> {
                TransferSheet(
                    stokTangan = summary.stokTangan,
                    onDismiss = { activeSheet = null },
                    onSave = { arah, nama, qty, ket ->
                        viewModel.transferPedagang(arah, nama, qty, ket) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.SETOR_PABRIK -> {
                SetoranSheet(
                    sisaKewajiban = summary.sisaKewajibanSetor,
                    onDismiss = { activeSheet = null },
                    onSave = { jumlah, ket ->
                        viewModel.setorWajib(jumlah, ket) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.PINJAM_SETORAN -> {
                PinjamanSheet(
                    saldoKasSetoran = summary.saldoKasSetoranTersedia,
                    onDismiss = { activeSheet = null },
                    onSave = { pihak, jumlah, ket ->
                        viewModel.pinjamUang(pihak, jumlah, ket) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            QuickActionType.CATAT_KAS -> {
                KasSheet(
                    onDismiss = { activeSheet = null },
                    onSave = { tipe, kat, jumlah, ket ->
                        viewModel.catatKas(tipe, kat, jumlah, ket) { success, _ ->
                            if (success) activeSheet = null
                        }
                    }
                )
            }
            null -> {}
        }

        if (repayLoanIdTarget != null) {
            KembalikanPinjamanSheet(
                activeLoans = activeLoans,
                preSelectedLoanId = repayLoanIdTarget,
                onDismiss = { repayLoanIdTarget = null },
                onSave = { loanId, jumlah, ket ->
                    viewModel.kembalikanPinjaman(loanId, jumlah, ket) { success, _ ->
                        if (success) repayLoanIdTarget = null
                    }
                }
            )
        }
    }
}
