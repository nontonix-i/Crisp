package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.PenjualanHarian
import com.example.data.model.ReceiptData
import com.example.data.model.ReceiptFactory
import com.example.ui.components.AppCard
import com.example.ui.components.BackupImportConfirmDialog
import com.example.ui.components.PrimaryActionButton
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
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.BackupDataPackage
import com.example.utils.FormatUtils
import com.example.viewmodel.KerupukViewModel

@Composable
fun LaporanScreen(
    viewModel: KerupukViewModel
) {
    val context = LocalContext.current
    val summary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
    val allPenjualan by viewModel.allPenjualan.collectAsStateWithLifecycle()

    var periodFilter by remember { mutableStateOf("minggu") } // "hari", "minggu", "bulan"
    var selectedReceipt by remember { mutableStateOf<ReceiptData?>(null) }

    // Backup & Restore states
    var pendingImportPackage by remember { mutableStateOf<BackupDataPackage?>(null) }
    var isExporting by remember { mutableStateOf(false) }
    var isImportLoading by remember { mutableStateOf(false) }

    val zipPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            isImportLoading = true
            viewModel.parseBackupFile(context, uri) { result ->
                isImportLoading = false
                result.onSuccess { pkg ->
                    pendingImportPackage = pkg
                }.onFailure { err ->
                    // error is emitted to snackbar inside ViewModel or can be logged
                }
            }
        }
    }

    val filteredPenjualan = remember(allPenjualan, periodFilter) {
        when (periodFilter) {
            "hari" -> {
                val today = FormatUtils.getTodayDateKey()
                allPenjualan.filter { it.tanggal == today }
            }
            "minggu" -> {
                val startWeek = FormatUtils.getStartOfWeek()
                allPenjualan.filter { it.created_at >= startWeek }
            }
            "bulan" -> {
                val startMonth = FormatUtils.getStartOfMonth()
                allPenjualan.filter { it.created_at >= startMonth }
            }
            else -> allPenjualan
        }
    }

    val totalLakuPeriod = remember(filteredPenjualan) { filteredPenjualan.sumOf { it.jumlah_laku } }
    val totalPendapatanPeriod = remember(filteredPenjualan) { filteredPenjualan.sumOf { it.pendapatan } }
    val totalNabungPeriod = remember(filteredPenjualan) { filteredPenjualan.sumOf { it.nabung_wajib } }
    val labaKotorPeriod = remember(totalPendapatanPeriod, totalLakuPeriod) { totalPendapatanPeriod - (totalLakuPeriod * 11000) }
    val labaBersihPeriod = remember(labaKotorPeriod, totalNabungPeriod) { labaKotorPeriod - totalNabungPeriod }

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
                text = "Laporan & Grafik",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
        }

        // 1. SEGMENTED CONTROL: Hari Ini / Minggu / Bulan
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFE2E8F0))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CleanPeriodTab(
                    modifier = Modifier.weight(1f),
                    title = "Hari Ini",
                    isSelected = periodFilter == "hari",
                    onClick = { periodFilter = "hari" }
                )
                CleanPeriodTab(
                    modifier = Modifier.weight(1f),
                    title = "Minggu Ini",
                    isSelected = periodFilter == "minggu",
                    onClick = { periodFilter = "minggu" }
                )
                CleanPeriodTab(
                    modifier = Modifier.weight(1f),
                    title = "Bulan Ini",
                    isSelected = periodFilter == "bulan",
                    onClick = { periodFilter = "bulan" }
                )
            }
        }

        // 2. HERO CARD LABA BERSIH (Electric Blue Gradient)
        item {
            AppCard(
                brush = Brush.horizontalGradient(
                    listOf(ElectricBlue, Color(0xFF1E6FFF), ElectricBlueGlow)
                )
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "ESTIMASI LABA BERSIH",
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
                                text = "${totalLakuPeriod} pack terjual",
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = FormatUtils.formatRupiah(labaBersihPeriod),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 38.sp
                        ),
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Laba kotor: ${FormatUtils.formatRupiah(labaKotorPeriod)} • Tabungan wajib: ${FormatUtils.formatRupiah(totalNabungPeriod)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 3. BAR CHART PENJUALAN (Clean White Card & Electric Blue Canvas)
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
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                tint = ElectricBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GRAFIK PENJUALAN PACK",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    CleanDailySalesChart(sales = allPenjualan.take(7))
                }
            }
        }

        // 4. REKAP SETORAN PABRIK (Clean White Card)
        item {
            AppCard {
                Column {
                    Text(
                        text = "REKAP KEWAJIBAN SETOR PABRIK",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Total Akumulasi Kewajiban", color = TextPrimary)
                        Text(text = FormatUtils.formatRupiah(summary.totalKewajibanSetor), fontWeight = FontWeight.Bold, color = TextPrimary)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sudah Disetor ke Pabrik", color = TextPrimary)
                        Text(text = FormatUtils.formatRupiah(summary.totalSudahSetor), fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }

                    Divider(modifier = Modifier.padding(vertical = 10.dp), color = CleanCardBorder)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sisa Kewajiban", fontWeight = FontWeight.Bold, color = TextPrimary)
                        Text(
                            text = if (summary.sisaKewajibanSetor <= 0) "Lunas ✓" else FormatUtils.formatRupiah(summary.sisaKewajibanSetor),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (summary.sisaKewajibanSetor <= 0) StatusSuccess else StatusDanger
                        )
                    }
                }
            }
        }

        // 5. REKAP PINJAMAN & STOK
        item {
            AppCard {
                Column {
                    Text(
                        text = "RINGKASAN PINJAMAN & STOK SAYA",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Stok Barang di Tangan", color = TextPrimary)
                        Text(text = FormatUtils.formatBarang(summary.stokTangan), fontWeight = FontWeight.Bold, color = ElectricBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Piutang Kawan Pedagang", color = TextPrimary)
                        Text(text = FormatUtils.formatRupiah(summary.piutangPedagang), fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Prive Pakai Sendiri", color = TextPrimary)
                        Text(text = FormatUtils.formatRupiah(summary.priveSendiri), fontWeight = FontWeight.Bold, color = ElectricBlue)
                    }
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Tabungan Wajib Terkumpul", color = TextPrimary)
                        Text(text = FormatUtils.formatRupiah(summary.totalNabungWajib), fontWeight = FontWeight.Bold, color = StatusSuccess)
                    }
                }
            }
        }

        // 6. RIWAYAT PENJUALAN HARIAN
        if (filteredPenjualan.isNotEmpty()) {
            item {
                SectionHeader(title = "Riwayat Penjualan")
            }
            items(filteredPenjualan.take(6), key = { it.id }) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, CleanCardBorder, RoundedCornerShape(14.dp))
                        .clickable { selectedReceipt = ReceiptFactory.fromPenjualanHarian(item) }
                        .testTag("penjualan_row_${item.id}"),
                    color = CleanSurface,
                    shadowElevation = 0.5.dp
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
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
                                    imageVector = Icons.Default.PointOfSale,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = item.tanggal,
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${item.jumlah_laku} pack • Klik buka struk",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = FormatUtils.formatRupiah(item.pendapatan),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                                color = ElectricBlue
                            )
                            Text(
                                text = "Laba: ${FormatUtils.formatRupiah(item.pendapatan - item.kewajiban_setor - item.nabung_wajib)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = StatusSuccess
                            )
                        }
                    }
                }
            }
        }

        // 7. CADANGAN & PEMULIHAN DATA LENGKAP (ZIP)
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
                                    imageVector = Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = ElectricBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "CADANGAN & RESTORE (ZIP)",
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Arsip database terkompresi .zip",
                                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                    color = TextSecondary
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Amankan seluruh rekaman transaksi, penjualan harian, stok, pinjaman, dan keuangan ke berkas .zip atau pulihkan database dari arsip cadangan kapan saja.",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, lineHeight = 17.sp),
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Tombol Export ZIP
                        Button(
                            onClick = {
                                isExporting = true
                                viewModel.exportBackupZip(context) { _, _ ->
                                    isExporting = false
                                }
                            },
                            enabled = !isExporting && !isImportLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_export_zip"),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricBlue,
                                contentColor = Color.White
                            )
                        ) {
                            if (isExporting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Mengemas...", fontSize = 12.sp)
                            } else {
                                Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Export ZIP", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Tombol Import ZIP
                        OutlinedButton(
                            onClick = {
                                zipPickerLauncher.launch("*/*")
                            },
                            enabled = !isExporting && !isImportLoading,
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("btn_import_zip"),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, ElectricBlue)
                        ) {
                            if (isImportLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = ElectricBlue,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(text = "Membaca...", fontSize = 12.sp, color = ElectricBlue)
                            } else {
                                Icon(imageVector = Icons.Default.CloudDownload, contentDescription = null, tint = ElectricBlue, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Import ZIP", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = ElectricBlue)
                            }
                        }
                    }
                }
            }
        }

        // 8. TOMBOL EXPORT CSV
        item {
            PrimaryActionButton(
                text = "Export Laporan CSV",
                icon = Icons.Default.Share,
                onClick = {
                    exportCsv(context, allPenjualan, summary)
                },
                testTag = "btn_export_csv"
            )
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }

    if (pendingImportPackage != null) {
        BackupImportConfirmDialog(
            backupPackage = pendingImportPackage!!,
            onDismiss = { pendingImportPackage = null },
            onConfirmRestore = {
                viewModel.restoreBackupPackage(pendingImportPackage!!) { _, _ ->
                    pendingImportPackage = null
                }
            }
        )
    }

    if (selectedReceipt != null) {
        ReceiptDialog(
            receipt = selectedReceipt!!,
            onDismiss = { selectedReceipt = null }
        )
    }
}

@Composable
fun CleanPeriodTab(
    modifier: Modifier = Modifier,
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        color = if (isSelected) CleanSurface else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        shadowElevation = if (isSelected) 1.dp else 0.dp
    ) {
        Box(
            modifier = Modifier.padding(vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 13.sp
                ),
                color = if (isSelected) ElectricBlue else TextSecondary
            )
        }
    }
}

@Composable
fun CleanDailySalesChart(sales: List<PenjualanHarian>) {
    if (sales.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Belum ada data penjualan tercatat",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        return
    }

    val displayList = remember(sales) {
        sales.reversed().takeLast(7)
    }

    val maxVal = remember(displayList) {
        (displayList.maxOfOrNull { it.jumlah_laku } ?: 50).coerceAtLeast(10)
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val barWidth = 24.dp.toPx()
                val spacing = (size.width - (barWidth * displayList.size)) / (displayList.size + 1)
                val bottomY = size.height - 20.dp.toPx()
                val maxBarHeight = bottomY - 10.dp.toPx()

                // Baseline
                drawLine(
                    color = Color(0xFFCBD5E1),
                    start = Offset(0f, bottomY),
                    end = Offset(size.width, bottomY),
                    strokeWidth = 2f
                )

                displayList.forEachIndexed { index, item ->
                    val x = spacing + index * (barWidth + spacing)
                    val barHeight = (item.jumlah_laku.toFloat() / maxVal) * maxBarHeight
                    val topY = bottomY - barHeight

                    // Bar with Electric Blue & Glow gradient
                    drawRoundRect(
                        brush = Brush.verticalGradient(
                            listOf(ElectricBlue, ElectricBlueGlow)
                        ),
                        topLeft = Offset(x, topY),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            displayList.forEach { item ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${item.jumlah_laku}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = ElectricBlue
                    )
                    Text(
                        text = item.tanggal.takeLast(5),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

fun exportCsv(
    context: Context,
    sales: List<PenjualanHarian>,
    summary: com.example.data.repository.DashboardSummary
) {
    val csv = StringBuilder()
    csv.append("Tanggal,Jumlah Laku,Harga Jual,Pendapatan,Nabung Wajib,Kewajiban Setor\n")
    sales.forEach { s ->
        csv.append("${s.tanggal},${s.jumlah_laku},${s.harga_jual},${s.pendapatan},${s.nabung_wajib},${s.kewajiban_setor}\n")
    }

    csv.append("\n=== REKAP KEUANGAN CRISPFLOW ===\n")
    csv.append("Total Kewajiban Setor,${summary.totalKewajibanSetor}\n")
    csv.append("Total Sudah Setor,${summary.totalSudahSetor}\n")
    csv.append("Sisa Kewajiban,${summary.sisaKewajibanSetor}\n")
    csv.append("Total Tabungan Wajib,${summary.totalNabungWajib}\n")
    csv.append("Piutang Pedagang,${summary.piutangPedagang}\n")
    csv.append("Prive Sendiri,${summary.priveSendiri}\n")
    csv.append("Stok Pack di Tangan,${summary.stokTangan}\n")

    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csv.toString())
        putExtra(Intent.EXTRA_TITLE, "Laporan_CrispFlow.csv")
        type = "text/plain"
    }
    val shareIntent = Intent.createChooser(sendIntent, "Bagikan / Export Laporan CrispFlow")
    context.startActivity(shareIntent)
}
