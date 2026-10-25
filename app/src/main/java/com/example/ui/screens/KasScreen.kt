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
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
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
import com.example.data.model.KasTransaksi
import com.example.data.model.ReceiptData
import com.example.data.model.ReceiptFactory
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
fun KasScreen(
    viewModel: KerupukViewModel,
    onOpenAction: (QuickActionType) -> Unit,
    onNavigateToPinjaman: () -> Unit
) {
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val recentKas by viewModel.recentKas.collectAsStateWithLifecycle()
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
                text = "Kas & Keuangan",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // 1. CARD SALDO KAS UMUM (Clean White Card with Electric Blue Header)
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
                        Text(
                            text = "SALDO KAS OPERASIONAL",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(ElectricBlueLight, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountBalanceWallet,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = FormatUtils.formatRupiah(summary.saldoKasUmum),
                        style = MaterialTheme.typography.displayMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    )
                }
            }
        }

        // 2. CARD NABUNG WAJIB (Clean White Card with Green Accent)
        item {
            AppCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Savings,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "TABUNGAN WAJIB",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = FormatUtils.formatRupiah(summary.totalNabungWajib),
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusSuccess
                        )
                    }
                }
            }
        }

        // 3. CARD KEWAJIBAN SETOR PABRIK
        item {
            val sisa = summary.sisaKewajibanSetor
            val isDue = sisa > 0

            AppCard(
                borderColor = if (isDue) StatusDanger.copy(alpha = 0.3f) else ElectricBlueBorder
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
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (sisa < 0) "Lebih setor ${FormatUtils.formatRupiah(-sisa)}"
                                       else if (sisa == 0) "Lunas (Rp 0)"
                                       else FormatUtils.formatRupiah(sisa),
                                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (isDue) StatusDanger else StatusSuccess
                            )
                            Text(
                                text = "Total disetor: ${FormatUtils.formatRupiah(summary.totalSudahSetor)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }

                        Button(
                            onClick = { onOpenAction(QuickActionType.SETOR_PABRIK) },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Setor", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. CARD PINJAMAN SETORAN
        item {
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
                                text = "PINJAMAN UANG SETORAN",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }

                        Text(
                            text = "Kelola →",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

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
                            Text(text = "Total Belum Lunas", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = FormatUtils.formatRupiah(summary.totalPinjamanAktif),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusDanger
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, ElectricBlue, RoundedCornerShape(8.dp))
                                .clickable { onOpenAction(QuickActionType.PINJAM_SETORAN) },
                            color = CleanSurface
                        ) {
                            Text(
                                text = "+ Catat Pinjaman",
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = ElectricBlue
                                )
                            )
                        }
                    }
                }
            }
        }

        // 5. TOMBOL ROW
        item {
            SectionHeader(title = "Aksi Transaksi Kas")

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CleanKasActionButton(
                    modifier = Modifier.weight(1f),
                    label = "+ Pemasukan",
                    color = ElectricBlue,
                    onClick = { onOpenAction(QuickActionType.CATAT_KAS) }
                )
                CleanKasActionButton(
                    modifier = Modifier.weight(1f),
                    label = "+ Setor",
                    color = StatusSuccess,
                    onClick = { onOpenAction(QuickActionType.SETOR_PABRIK) }
                )
                CleanKasActionButton(
                    modifier = Modifier.weight(1f),
                    label = "- Pengeluaran",
                    color = StatusDanger,
                    onClick = { onOpenAction(QuickActionType.CATAT_KAS) }
                )
                CleanKasActionButton(
                    modifier = Modifier.weight(1f),
                    label = "+ Pinjam",
                    color = Color(0xFFD97706),
                    onClick = { onOpenAction(QuickActionType.PINJAM_SETORAN) }
                )
            }
        }

        // 6. SECTION RIWAYAT KAS
        item {
            SectionHeader(title = "Riwayat Arus Kas")
        }

        if (recentKas.isEmpty()) {
            item {
                EmptyStateWidget(
                    message = "Belum Ada Transaksi",
                    subMessage = "Catatan pemasukan dan pengeluaran akan muncul di sini"
                )
            }
        } else {
            items(recentKas, key = { it.id }) { kas ->
                CleanKasTransaksiCard(
                    kas = kas,
                    onClick = { selectedReceipt = ReceiptFactory.fromKasTransaksi(kas) }
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
fun CleanKasActionButton(
    modifier: Modifier = Modifier,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, CleanCardBorder, RoundedCornerShape(12.dp))
            .clickable { onClick() },
        color = CleanSurface,
        shadowElevation = 0.5.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = color
            )
        }
    }
}

@Composable
fun CleanKasTransaksiCard(
    kas: KasTransaksi,
    onClick: (() -> Unit)? = null
) {
    val isMasuk = kas.tipe == "masuk"
    val tint = if (isMasuk) StatusSuccess else StatusDanger
    val icon = if (isMasuk) Icons.Default.ArrowDownward else Icons.Default.ArrowUpward

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
            .testTag("kas_item_${kas.id}"),
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
                    text = if (kas.keterangan.isNotBlank()) kas.keterangan else kas.kategori.replace("_", " ").uppercase(),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "${kas.kategori.replace("_", " ")} • ${FormatUtils.formatDate(kas.tanggal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Text(
                text = "${if (isMasuk) "+" else "-"}${FormatUtils.formatRupiah(kas.jumlah)}",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = tint
            )
        }
    }
}
