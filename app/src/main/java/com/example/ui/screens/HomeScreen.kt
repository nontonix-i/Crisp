package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.ReceiptData
import com.example.data.model.ReceiptFactory
import com.example.data.model.StokMutasi
import com.example.ui.components.AppCard
import com.example.ui.components.EmptyStateWidget
import com.example.ui.components.QuickActionType
import com.example.ui.components.ReceiptDialog
import com.example.ui.components.SectionHeader
import com.example.ui.theme.CleanBg
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueBorder
import com.example.ui.theme.ElectricBlueGlow
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusDangerBg
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.StatusSuccessBg
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.FormatUtils
import com.example.viewmodel.KerupukViewModel

@Composable
fun HomeScreen(
    viewModel: KerupukViewModel,
    onOpenAction: (QuickActionType) -> Unit,
    onNavigateToPinjaman: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val todayMutasi by viewModel.todayMutasi.collectAsStateWithLifecycle()
    var selectedReceipt by remember { mutableStateOf<ReceiptData?>(null) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBg)
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CrispFlow",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    )
                    Text(
                        text = FormatUtils.formatDate(System.currentTimeMillis()),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }

                if (summary.stokTangan == 0) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.seedInitialStockIfNeeded() },
                        color = ElectricBlueLight,
                        border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlueBorder)
                    ) {
                        Text(
                            text = "+ Ambil Stok Awal",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, color = ElectricBlue)
                        )
                    }
                }
            }
        }

        // 1. HERO CARD: STOK SAYA (Clean White & Electric Blue Gradient)
        item {
            AppCard(
                brush = Brush.horizontalGradient(
                    colors = listOf(ElectricBlue, Color(0xFF1E6FFF), ElectricBlueGlow)
                ),
                onClick = { onOpenAction(QuickActionType.CATAT_LAKU) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STOK DI TANGAN",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp
                            ),
                            color = Color.White.copy(alpha = 0.9f)
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = "Siap Jual",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column {
                            Text(
                                text = "${summary.stokTangan}",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 44.sp
                                ),
                                color = Color.White
                            )
                            Text(
                                text = "pack siap jual",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onOpenAction(QuickActionType.AMBIL_JATAH) }
                            ) {
                                Text(
                                    text = "+ Ambil Pool",
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue
                                    )
                                )
                            }

                            if (summary.stokTangan > 0) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.2f),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { onOpenAction(QuickActionType.BALIK_POOL) }
                                ) {
                                    Text(
                                        text = "Balik",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. ROW 2 CARD: LAKU HARI INI | NABUNG WAJIB (Clean White Cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppCard(
                    modifier = Modifier.weight(1f),
                    onClick = { onOpenAction(QuickActionType.CATAT_LAKU) }
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LAKU HARI INI",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.PointOfSale,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "${summary.lakuHariIni}",
                            style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBlue
                        )
                        Text(
                            text = "pack terjual",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                AppCard(
                    modifier = Modifier.weight(1f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NABUNG WAJIB",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = FormatUtils.formatRupiah(summary.nabungWajibHariIni),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess
                        )
                        Text(
                            text = "Total: ${FormatUtils.formatRupiah(summary.totalNabungWajib)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 3. CARD KEWAJIBAN SETOR (Clean White Card with Electric Blue / Red status)
        item {
            val sisa = summary.sisaKewajibanSetor
            val isDue = sisa > 0
            val isLunas = sisa == 0
            val isSurplus = sisa < 0

            AppCard(
                borderColor = if (isDue) StatusDanger.copy(alpha = 0.3f) else ElectricBlueBorder,
                onClick = { onOpenAction(QuickActionType.SETOR_PABRIK) }
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isDue) Icons.Default.Warning else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isDue) StatusDanger else StatusSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "KEWAJIBAN SETOR PABRIK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Surface(
                            shape = CircleShape,
                            color = if (isDue) StatusDangerBg else StatusSuccessBg
                        ) {
                            Text(
                                text = if (isDue) "Belum Setor" else if (isLunas) "Lunas ✓" else "Lebih Setor",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDue) StatusDanger else StatusSuccess
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isSurplus) FormatUtils.formatRupiah(-sisa) else FormatUtils.formatRupiah(sisa),
                                style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDue) StatusDanger else StatusSuccess
                            )
                            Text(
                                text = "Sudah setor: ${FormatUtils.formatRupiah(summary.totalSudahSetor)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { onOpenAction(QuickActionType.SETOR_PABRIK) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("btn_setor_sekarang")
                        ) {
                            Text(
                                text = "Setor",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // 4. CARD PINJAMAN AKTIF (Clean White Card)
        if (summary.totalPinjamanAktif > 0) {
            item {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn() + slideInVertically()
                ) {
                    AppCard(
                        borderColor = ElectricBlueBorder,
                        onClick = onNavigateToPinjaman
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.MonetizationOn,
                                        contentDescription = null,
                                        tint = ElectricBlue,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "PINJAMAN DARI UANG SETORAN",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = TextPrimary
                                    )
                                }
                                Text(
                                    text = "Rincian →",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(text = "Piutang Pedagang", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = FormatUtils.formatRupiah(summary.piutangPedagang),
                                        fontWeight = FontWeight.Bold,
                                        color = StatusSuccess
                                    )
                                }
                                Column {
                                    Text(text = "Prive Sendiri", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = FormatUtils.formatRupiah(summary.priveSendiri),
                                        fontWeight = FontWeight.Bold,
                                        color = ElectricBlue
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "Total Pinjaman", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    Text(
                                        text = FormatUtils.formatRupiah(summary.totalPinjamanAktif),
                                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                        color = StatusDanger
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 5. CARD PENDAPATAN HARI INI
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "PENDAPATAN KOTOR HARI INI",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = FormatUtils.formatRupiah(summary.pendapatanHariIni),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Laba Bersih Saya:",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Text(
                            text = FormatUtils.formatRupiah(summary.labaBersihHariIni),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess
                        )
                    }
                }
            }
        }

        // 6. SECTION AKSI CEPAT (Grid 2x2 Clean Cards)
        item {
            SectionHeader(title = "Aksi Cepat")

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CleanQuickButton(
                        modifier = Modifier.weight(1f),
                        text = "Catat Laku",
                        icon = Icons.Default.PointOfSale,
                        color = ElectricBlue,
                        onClick = { onOpenAction(QuickActionType.CATAT_LAKU) },
                        tag = "btn_quick_catat_laku"
                    )
                    CleanQuickButton(
                        modifier = Modifier.weight(1f),
                        text = "Ambil Pool",
                        icon = Icons.Default.PanTool,
                        color = ElectricBlue,
                        onClick = { onOpenAction(QuickActionType.AMBIL_JATAH) },
                        tag = "btn_quick_ambil_jatah"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    CleanQuickButton(
                        modifier = Modifier.weight(1f),
                        text = "Transfer",
                        icon = Icons.Default.SwapHoriz,
                        color = Color(0xFF0284C7),
                        onClick = { onOpenAction(QuickActionType.TRANSFER_PEDAGANG) },
                        tag = "btn_quick_transfer"
                    )
                    CleanQuickButton(
                        modifier = Modifier.weight(1f),
                        text = "Setor Pabrik",
                        icon = Icons.Default.Paid,
                        color = StatusSuccess,
                        onClick = { onOpenAction(QuickActionType.SETOR_PABRIK) },
                        tag = "btn_quick_setor"
                    )
                }
            }
        }

        // 7. SECTION RIWAYAT HARI INI
        item {
            SectionHeader(title = "Riwayat Hari Ini")
        }

        if (todayMutasi.isEmpty()) {
            item {
                EmptyStateWidget(
                    message = "Belum Ada Aktivitas",
                    subMessage = "Penjualan dan mutasi stok hari ini akan tercatat di sini"
                )
            }
        } else {
            items(todayMutasi, key = { it.id }) { mutasi ->
                TimelineMutasiCard(
                    mutasi = mutasi,
                    onClick = { selectedReceipt = ReceiptFactory.fromStokMutasi(mutasi) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (selectedReceipt != null) {
        ReceiptDialog(
            receipt = selectedReceipt!!,
            onDismiss = { selectedReceipt = null }
        )
    }
}

@Composable
fun CleanQuickButton(
    modifier: Modifier = Modifier,
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        color = CleanSurface,
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier.padding(vertical = 14.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = TextPrimary
            )
        }
    }
}

@Composable
fun TimelineMutasiCard(
    mutasi: StokMutasi,
    onClick: (() -> Unit)? = null
) {
    val (icon, tint, title) = when (mutasi.tipe) {
        "laku" -> Triple(Icons.Default.PointOfSale, ElectricBlue, "Penjualan Laku")
        "ambil_pool", "keluar_tangan" -> Triple(Icons.Default.PanTool, ElectricBlue, "Ambil dari Pool")
        "balik_pool" -> Triple(Icons.Default.ArrowBack, Color(0xFF64748B), "Sisa Balik ke Pool")
        "masuk_pabrik" -> Triple(Icons.Default.PanTool, ElectricBlue, "Ambil dari Pool")
        "transfer_keluar" -> Triple(Icons.Default.SwapHoriz, StatusDanger, "Transfer Keluar")
        "transfer_masuk" -> Triple(Icons.Default.SwapHoriz, StatusSuccess, "Transfer Masuk")
        else -> Triple(Icons.Default.Info, ElectricBlue, "Mutasi Stok")
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, CleanCardBorder, RoundedCornerShape(14.dp))
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            )
            .testTag("mutasi_item_${mutasi.id}"),
        color = CleanSurface,
        shadowElevation = 0.5.dp
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(tint.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = if (mutasi.keterangan.isNotBlank()) mutasi.keterangan else "Stok Saya",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (mutasi.jumlah > 0) "+${mutasi.jumlah}" else "${mutasi.jumlah}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = if (mutasi.jumlah > 0) StatusSuccess else StatusDanger
                )
                Text(
                    text = FormatUtils.formatTime(mutasi.tanggal),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
