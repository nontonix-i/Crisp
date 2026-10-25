package com.example.ui.screens

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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.SwapHoriz
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
import com.example.data.model.TransferPedagang
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
fun StokScreen(
    viewModel: KerupukViewModel,
    onOpenAction: (QuickActionType) -> Unit
) {
    val tangan by viewModel.stokTangan.collectAsStateWithLifecycle()
    val recentMutasi by viewModel.recentMutasi.collectAsStateWithLifecycle()
    val transfers by viewModel.allTransfer.collectAsStateWithLifecycle()
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
            Text(
                text = "Stok Saya",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // 1. HERO CARD: STOK SAYA DI TANGAN
        item {
            AppCard(
                borderColor = ElectricBlueBorder
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(ElectricBlueLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PanTool,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "STOK SAYA",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$tangan",
                                style = MaterialTheme.typography.displayLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                ),
                                color = ElectricBlue
                            )
                            Text(
                                text = "pack di tangan",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = { onOpenAction(QuickActionType.AMBIL_JATAH) },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("+ Ambil Pool", fontWeight = FontWeight.Bold)
                            }

                            if (tangan > 0) {
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, ElectricBlueBorder, RoundedCornerShape(12.dp))
                                        .clickable { onOpenAction(QuickActionType.BALIK_POOL) },
                                    color = CleanSurface
                                ) {
                                    Text(
                                        text = "Balik Pool",
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        style = MaterialTheme.typography.labelLarge.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. CARD AKSI CEPAT STOK
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onOpenAction(QuickActionType.CATAT_LAKU) },
                    color = CleanSurface,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.PointOfSale,
                            contentDescription = null,
                            tint = ElectricBlue,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Catat Laku", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp))
                        .clickable { onOpenAction(QuickActionType.TRANSFER_PEDAGANG) },
                    color = CleanSurface,
                    shadowElevation = 1.dp
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Icon(
                            imageVector = Icons.Default.SwapHoriz,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(text = "Transfer Pedagang", fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                }
            }
        }

        // 3. CARD RIWAYAT TRANSFER PEDAGANG
        item {
            AppCard {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TRANSFER KE / DARI PEDAGANG",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenAction(QuickActionType.TRANSFER_PEDAGANG) },
                            color = ElectricBlueLight,
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlueBorder)
                        ) {
                            Text(
                                text = "+ Transfer Baru",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (transfers.isEmpty()) {
                        Text(
                            text = "Belum ada catatan transfer pack dengan kawan pedagang",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transfers.take(4).forEach { item ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (item.arah == "keluar") StatusDangerBg else StatusSuccessBg)
                                        .clickable {
                                            selectedReceipt = ReceiptFactory.fromTransferPedagang(item)
                                        }
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = if (item.arah == "keluar") Icons.Default.ArrowUpward else Icons.Default.ArrowDownward,
                                            contentDescription = null,
                                            tint = if (item.arah == "keluar") StatusDanger else StatusSuccess,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = "${item.nama_pedagang} (${if (item.arah == "keluar") "Ke" else "Dari"})",
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    }

                                    Text(
                                        text = "${if (item.arah == "keluar") "-" else "+"}${item.jumlah} pack",
                                        fontWeight = FontWeight.Bold,
                                        color = if (item.arah == "keluar") StatusDanger else StatusSuccess
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 4. SECTION MUTASI TERAKHIR
        item {
            SectionHeader(title = "Mutasi Stok Terakhir")
        }

        if (recentMutasi.isEmpty()) {
            item {
                EmptyStateWidget(
                    message = "Belum Ada Mutasi",
                    subMessage = "Pergerakan pack Anda akan tercatat di sini"
                )
            }
        } else {
            items(recentMutasi, key = { it.id }) { mutasi ->
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
