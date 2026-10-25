package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PinjamanSetoran
import com.example.data.model.ReceiptData
import com.example.data.model.ReceiptFactory
import com.example.ui.components.EmptyStateWidget
import com.example.ui.components.ReceiptDialog
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.FormatUtils
import com.example.viewmodel.KerupukViewModel

@Composable
fun PinjamanListScreen(
    viewModel: KerupukViewModel,
    onBack: () -> Unit,
    onOpenAddPinjaman: () -> Unit,
    onOpenRepayLoan: (Long) -> Unit
) {
    BackHandler { onBack() }

    val allLoans by viewModel.allPinjaman.collectAsStateWithLifecycle()
    val activeLoansWithBalance by viewModel.activeLoansWithBalance.collectAsStateWithLifecycle()
    var filterTab by remember { mutableStateOf("aktif") }
    var selectedReceipt by remember { mutableStateOf<ReceiptData?>(null) }

    val keluarLoans = remember(allLoans) {
        allLoans.filter { it.tipe == "keluar" }
    }

    val displayedLoans = remember(keluarLoans, filterTab, activeLoansWithBalance) {
        when (filterTab) {
            "aktif" -> keluarLoans.filter { it.status == "belum_lunas" }
            "lunas" -> keluarLoans.filter { it.status == "lunas" }
            else -> keluarLoans
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CleanBg)
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CleanSurface,
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("btn_back_pinjaman")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Kembali",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Daftar Pinjaman Setoran",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                IconButton(
                    onClick = onOpenAddPinjaman,
                    modifier = Modifier.testTag("btn_add_pinjaman_top")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Tambah Pinjaman",
                        tint = ElectricBlue
                    )
                }
            }
        }

        // Filter Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFE2E8F0))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            CleanPeriodTab(
                modifier = Modifier.weight(1f),
                title = "Aktif",
                isSelected = filterTab == "aktif",
                onClick = { filterTab = "aktif" }
            )
            CleanPeriodTab(
                modifier = Modifier.weight(1f),
                title = "Lunas",
                isSelected = filterTab == "lunas",
                onClick = { filterTab = "lunas" }
            )
            CleanPeriodTab(
                modifier = Modifier.weight(1f),
                title = "Semua",
                isSelected = filterTab == "semua",
                onClick = { filterTab = "semua" }
            )
        }

        // List
        if (displayedLoans.isEmpty()) {
            EmptyStateWidget(
                message = "Tidak Ada Pinjaman",
                subMessage = if (filterTab == "aktif") "Semua pinjaman uang setoran telah lunas" else "Belum ada catatan pinjaman"
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayedLoans, key = { it.id }) { loan ->
                    val activeInfo = activeLoansWithBalance.find { it.loan.id == loan.id }
                    val remaining = activeInfo?.remainingBalance ?: if (loan.status == "lunas") 0 else loan.jumlah

                    CleanPinjamanItemCard(
                        loan = loan,
                        remainingBalance = remaining,
                        onCardClick = { selectedReceipt = ReceiptFactory.fromPinjaman(loan) },
                        onRepayClick = { onOpenRepayLoan(loan.id) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
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
fun CleanPinjamanItemCard(
    loan: PinjamanSetoran,
    remainingBalance: Int,
    onCardClick: () -> Unit,
    onRepayClick: () -> Unit
) {
    val isSendiri = loan.pihak == "Sendiri"
    val isLunas = loan.status == "lunas"
    val dotColor = if (isSendiri) ElectricBlue else Color(0xFFF59E0B)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp))
            .clickable { onCardClick() }
            .testTag("loan_card_${loan.id}"),
        color = CleanSurface,
        shadowElevation = 1.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(dotColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isSendiri) "Prive (Pakai Sendiri)" else loan.pihak,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary
                    )
                }

                if (isLunas) {
                    Surface(
                        shape = CircleShape,
                        color = StatusSuccessBg
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "LUNAS",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = StatusSuccess
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = CircleShape,
                        color = StatusDangerBg
                    ) {
                        Text(
                            text = "BELUM LUNAS",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = StatusDanger
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Total Pinjaman: ${FormatUtils.formatRupiah(loan.jumlah)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    if (!isLunas) {
                        Text(
                            text = "Sisa: ${FormatUtils.formatRupiah(remainingBalance)}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = StatusDanger
                        )
                    } else if (loan.tanggal_kembali != null) {
                        Text(
                            text = "Kembali: ${FormatUtils.formatDate(loan.tanggal_kembali)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = StatusSuccess
                        )
                    }
                    Text(
                        text = "Tanggal: ${FormatUtils.formatDate(loan.tanggal)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }

                if (!isLunas) {
                    Button(
                        onClick = onRepayClick,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("btn_kembalikan_pinjaman_${loan.id}")
                    ) {
                        Text("Kembalikan", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            if (loan.keterangan.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Catatan: ${loan.keterangan}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
        }
    }
}
