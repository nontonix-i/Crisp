package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.ActiveLoanWithBalance
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
import com.example.ui.theme.StatusWarning
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.FormatUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SheetHeader(
    title: String,
    subtitle: String,
    onClose: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.testTag("close_sheet_button")
        ) {
            Icon(imageVector = Icons.Default.Close, contentDescription = "Tutup", tint = TextSecondary)
        }
    }
}

// 1. SHEET CATAT LAKU
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatatLakuSheet(
    stokTangan: Int,
    currentLakuToday: Int,
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var jumlahText by remember { mutableStateOf(if (currentLakuToday > 0) currentLakuToday.toString() else "45") }
    var hargaJual by remember { mutableIntStateOf(15000) }

    val lakuInt = jumlahText.toIntOrNull() ?: 0
    val maxAvailable = (stokTangan + currentLakuToday).coerceAtLeast(0)
    val isValid = lakuInt in 1..maxAvailable

    val pendapatan = lakuInt * hargaJual
    val nabungWajib = lakuInt * 500
    val kewajibanSetor = lakuInt * 11000
    val labaKotor = pendapatan - (lakuInt * 11000)
    val labaBersih = labaKotor - nabungWajib

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Catat Laku Hari Ini",
                subtitle = "Stok di tangan: $stokTangan pack",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "JUMLAH PACK LAKU",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_jumlah_laku"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    ),
                    trailingIcon = {
                        Text(
                            text = "pack",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(40, 45, 50, 55, 60).forEach { qty ->
                        ShortcutChip(
                            text = "$qty pack",
                            onClick = { jumlahText = qty.toString() },
                            isSelected = jumlahText == qty.toString()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "HARGA JUAL KE WARUNG",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    listOf(15000, 16000).forEach { harga ->
                        val isSelected = hargaJual == harga
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    2.dp,
                                    if (isSelected) ElectricBlue else CleanCardBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { hargaJual = harga },
                            color = if (isSelected) ElectricBlueLight else CleanBg
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { hargaJual = harga },
                                    colors = RadioButtonDefaults.colors(selectedColor = ElectricBlue)
                                )
                                Text(
                                    text = FormatUtils.formatRupiah(harga),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ElectricBlue else TextPrimary
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Breakdown Calculation Preview Card
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = CleanBg,
                    border = androidx.compose.foundation.BorderStroke(1.dp, CleanCardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "RINCIAN PERHITUNGAN OTOMATIS",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Pendapatan ($lakuInt × ${FormatUtils.formatRupiah(hargaJual)})", color = TextPrimary)
                            Text(text = FormatUtils.formatRupiah(pendapatan), fontWeight = FontWeight.Bold, color = ElectricBlue)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Kewajiban Setor ($lakuInt × Rp 11.000)", color = TextPrimary)
                            Text(text = FormatUtils.formatRupiah(kewajibanSetor), fontWeight = FontWeight.Bold, color = StatusDanger)
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Nabung Wajib ($lakuInt × Rp 500)", color = TextPrimary)
                            Text(text = FormatUtils.formatRupiah(nabungWajib), fontWeight = FontWeight.Bold, color = StatusSuccess)
                        }

                        Divider(modifier = Modifier.padding(vertical = 10.dp), color = CleanCardBorder)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Laba Bersih Saya", fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(
                                text = FormatUtils.formatRupiah(labaBersih),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = StatusSuccess
                            )
                        }
                    }
                }

                if (!isValid && lakuInt > maxAvailable) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jumlah laku melebihi stok yang ada di tangan ($maxAvailable pack)",
                        color = StatusDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan Penjualan",
                    onClick = {
                        if (isValid) {
                            onSave(lakuInt, hargaJual)
                        }
                    },
                    enabled = isValid,
                    testTag = "btn_simpan_laku"
                )
            }
        }
    }
}

// 2. SHEET AMBIL DARI POOL (User just logs what they took from pool!)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmbilJatahSheet(
    stokPool: Int = 0,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var jumlahText by remember { mutableStateOf("60") }
    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Ambil Barang dari Pool",
                subtitle = "Catat jumlah pack yang diambil untuk dibawa jualan",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "JUMLAH YANG DIAMBIL",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_ambil_jatah"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    ),
                    trailingIcon = {
                        Text(
                            text = "pack",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = TextSecondary,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(50, 55, 60, 100, 250).forEach { qty ->
                        ShortcutChip(
                            text = "$qty pack",
                            onClick = { jumlahText = qty.toString() },
                            isSelected = jumlahText == qty.toString()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Tambah ke Stok Saya",
                    onClick = { if (isValid) onSave(jumlahInt) },
                    enabled = isValid,
                    testTag = "btn_konfirmasi_ambil"
                )
            }
        }
    }
}

// 3. SHEET BALIK SISA KE POOL
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalikPoolSheet(
    stokTangan: Int,
    onDismiss: () -> Unit,
    onSave: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var jumlahText by remember { mutableStateOf(stokTangan.coerceAtLeast(0).toString()) }
    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt in 1..stokTangan

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Kembalikan Sisa ke Pool",
                subtitle = "Sisa pack di tangan dikembalikan ke pool",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CleanBg
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Sisa Pack di Tangan", color = TextSecondary)
                        Text(
                            text = FormatUtils.formatBarang(stokTangan),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUMLAH YANG DIKEMBALIKAN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_balik_pool"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                )

                if (stokTangan > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    ShortcutChip(
                        text = "Balik Semua Sisa ($stokTangan pack)",
                        onClick = { jumlahText = stokTangan.toString() },
                        isSelected = jumlahText == stokTangan.toString()
                    )
                }

                if (jumlahInt > stokTangan) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Jumlah melebihi sisa di tangan ($stokTangan pack)",
                        color = StatusDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Konfirmasi Balik ke Pool",
                    onClick = { if (isValid) onSave(jumlahInt) },
                    enabled = isValid,
                    testTag = "btn_simpan_balik"
                )
            }
        }
    }
}

// 4. SHEET MASUK PABRIK (kept as simple stock entry helper if needed)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MasukPabrikSheet(
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var jumlahText by remember { mutableStateOf("250") }
    var keterangan by remember { mutableStateOf("Ambil jatah mingguan") }
    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Ambil Jatah Pack",
                subtitle = "Tambah stok pack yang diambil",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "JUMLAH PACK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth().testTag("input_masuk_pabrik"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan ke Stok Saya",
                    onClick = { if (isValid) onSave(jumlahInt, keterangan) },
                    enabled = isValid,
                    testTag = "btn_simpan_pabrik"
                )
            }
        }
    }
}

// 5. SHEET TRANSFER PEDAGANG
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferSheet(
    stokTangan: Int,
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var arah by remember { mutableStateOf("keluar") }
    var namaPedagang by remember { mutableStateOf("") }
    var jumlahText by remember { mutableStateOf("10") }
    var keterangan by remember { mutableStateOf("") }

    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt > 0 && namaPedagang.isNotBlank() && (arah == "masuk" || jumlahInt <= stokTangan)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Transfer Pack Pedagang",
                subtitle = "Tukar / oper stok pack dengan sesama pedagang",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Text(
                    text = "ARAH TRANSFER",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isKeluar = arah == "keluar"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (isKeluar) StatusDanger else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable { arah = "keluar" },
                        color = if (isKeluar) StatusDangerBg else CleanBg
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowUpward, contentDescription = null, tint = StatusDanger)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Keluar (Beri)", fontWeight = FontWeight.Bold, color = if (isKeluar) StatusDanger else TextPrimary)
                        }
                    }

                    val isMasuk = arah == "masuk"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (isMasuk) ElectricBlue else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable { arah = "masuk" },
                        color = if (isMasuk) ElectricBlueLight else CleanBg
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = null, tint = ElectricBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Masuk (Terima)", fontWeight = FontWeight.Bold, color = if (isMasuk) ElectricBlue else TextPrimary)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "NAMA PEDAGANG",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = namaPedagang,
                    onValueChange = { namaPedagang = it },
                    placeholder = { Text("Contoh: Pak Budi, Mas Anto") },
                    modifier = Modifier.fillMaxWidth().testTag("input_nama_pedagang"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUMLAH PACK",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth().testTag("input_jumlah_transfer"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (arah == "keluar") StatusDanger else ElectricBlue
                    )
                )

                if (arah == "keluar" && jumlahInt > stokTangan) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Stok di tangan hanya $stokTangan pack",
                        color = StatusDanger,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan Transfer",
                    onClick = { if (isValid) onSave(arah, namaPedagang, jumlahInt, keterangan) },
                    enabled = isValid,
                    testTag = "btn_simpan_transfer"
                )
            }
        }
    }
}

// 6. SHEET SETORAN PABRIK
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetoranSheet(
    sisaKewajiban: Int,
    onDismiss: () -> Unit,
    onSave: (Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val defaultVal = if (sisaKewajiban > 0) sisaKewajiban.toString() else "110000"
    var jumlahText by remember { mutableStateOf(defaultVal) }
    var keterangan by remember { mutableStateOf("Setor pabrik") }

    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt > 0
    val sisaSetelahSetor = sisaKewajiban - jumlahInt

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Setor Uang ke Pabrik",
                subtitle = "Setoran fleksibel ke pihak pabrik",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (sisaKewajiban > 0) StatusDangerBg else StatusSuccessBg
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Sisa Kewajiban Belum Disetor", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                            Text(
                                text = if (sisaKewajiban > 0) FormatUtils.formatRupiah(sisaKewajiban) else "Lunas ✓",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = if (sisaKewajiban > 0) StatusDanger else StatusSuccess
                            )
                        }
                        if (sisaKewajiban > 0) {
                            ShortcutChip(
                                text = "Setor Penuh",
                                onClick = { jumlahText = sisaKewajiban.toString() },
                                isSelected = jumlahText == sisaKewajiban.toString()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUMLAH YANG DISETOR (RP)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth().testTag("input_jumlah_setor"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CleanBg
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "Sisa kewajiban setelah disetor:", color = TextSecondary)
                        Text(
                            text = if (sisaSetelahSetor < 0) "Lebih setor ${FormatUtils.formatRupiah(-sisaSetelahSetor)}"
                                   else if (sisaSetelahSetor == 0) "Lunas (Rp 0)"
                                   else FormatUtils.formatRupiah(sisaSetelahSetor),
                            fontWeight = FontWeight.Bold,
                            color = if (sisaSetelahSetor <= 0) StatusSuccess else StatusDanger
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan Setoran",
                    onClick = { if (isValid) onSave(jumlahInt, keterangan) },
                    enabled = isValid,
                    testTag = "btn_simpan_setor"
                )
            }
        }
    }
}

// 7. SHEET PINJAMAN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PinjamanSheet(
    saldoKasSetoran: Int,
    onDismiss: () -> Unit,
    onSave: (String, Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isSendiri by remember { mutableStateOf(false) }
    var namaPedagang by remember { mutableStateOf("") }
    var jumlahText by remember { mutableStateOf("50000") }
    var keterangan by remember { mutableStateOf("") }

    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val pihakFinal = if (isSendiri) "Sendiri" else namaPedagang.trim()
    val isValid = jumlahInt > 0 && pihakFinal.isNotBlank()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Catat Pinjaman Uang Setoran",
                subtitle = "Dipinjam kawan pedagang atau dipakai sendiri (prive)",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = ElectricBlueLight
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = "Uang Setoran Tersedia:", color = TextSecondary)
                        Text(
                            text = FormatUtils.formatRupiah(saldoKasSetoran),
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "SIAPA YANG MEMINJAM?",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (!isSendiri) ElectricBlue else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable { isSendiri = false },
                        color = if (!isSendiri) ElectricBlueLight else CleanBg
                    ) {
                        Text(
                            text = "Pedagang Lain",
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (!isSendiri) ElectricBlue else TextPrimary
                        )
                    }

                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (isSendiri) ElectricBlue else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable { isSendiri = true },
                        color = if (isSendiri) ElectricBlueLight else CleanBg
                    ) {
                        Text(
                            text = "Pakai Sendiri (Prive)",
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (isSendiri) ElectricBlue else TextPrimary
                        )
                    }
                }

                if (!isSendiri) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "NAMA PEDAGANG PEMINJAM",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = namaPedagang,
                        onValueChange = { namaPedagang = it },
                        placeholder = { Text("Contoh: Mas Hasan, Pak Slamet") },
                        modifier = Modifier.fillMaxWidth().testTag("input_peminjam_nama"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = CleanCardBorder
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUMLAH PINJAMAN (RP)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth().testTag("input_pinjaman_jumlah"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = ElectricBlue
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan Catatan Pinjaman",
                    onClick = { if (isValid) onSave(pihakFinal, jumlahInt, keterangan) },
                    enabled = isValid,
                    testTag = "btn_simpan_pinjaman"
                )
            }
        }
    }
}

// 8. SHEET KEMBALIKAN PINJAMAN
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KembalikanPinjamanSheet(
    activeLoans: List<ActiveLoanWithBalance>,
    preSelectedLoanId: Long? = null,
    onDismiss: () -> Unit,
    onSave: (Long, Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedId by remember {
        mutableStateOf(preSelectedLoanId ?: activeLoans.firstOrNull()?.loan?.id ?: 0L)
    }

    val selectedLoan = activeLoans.find { it.loan.id == selectedId }
    var jumlahText by remember { mutableStateOf(selectedLoan?.remainingBalance?.toString() ?: "0") }
    var keterangan by remember { mutableStateOf("") }

    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val maxRepay = selectedLoan?.remainingBalance ?: 0
    val isValid = selectedLoan != null && jumlahInt in 1..maxRepay

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Kembalikan Pinjaman",
                subtitle = "Catat uang pinjaman yang dikembalikan",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                if (activeLoans.isEmpty()) {
                    EmptyStateWidget(
                        message = "Tidak Ada Pinjaman Aktif",
                        subMessage = "Semua pinjaman uang setoran telah lunas."
                    )
                } else {
                    Text(
                        text = "PILIH PINJAMAN",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        activeLoans.forEach { item ->
                            val isSelected = item.loan.id == selectedId
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .border(
                                        2.dp,
                                        if (isSelected) ElectricBlue else CleanCardBorder,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        selectedId = item.loan.id
                                        jumlahText = item.remainingBalance.toString()
                                    },
                                color = if (isSelected) ElectricBlueLight else CleanBg
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = item.loan.pihak, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(text = "Tgl: ${FormatUtils.formatDate(item.loan.tanggal)}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                                    }
                                    Text(
                                        text = FormatUtils.formatRupiah(item.remainingBalance),
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) ElectricBlue else StatusDanger
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "JUMLAH PENGEMBALIAN (RP)",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = jumlahText,
                        onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth().testTag("input_kembalikan_jumlah"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricBlue,
                            unfocusedBorderColor = CleanCardBorder
                        ),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = ElectricBlue
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    if (selectedLoan != null) {
                        ShortcutChip(
                            text = "Lunasi Penuh (${FormatUtils.formatRupiah(selectedLoan.remainingBalance)})",
                            onClick = { jumlahText = selectedLoan.remainingBalance.toString() },
                            isSelected = jumlahText == selectedLoan.remainingBalance.toString()
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    PrimaryActionButton(
                        text = "Simpan Pengembalian",
                        onClick = { if (isValid) onSave(selectedId, jumlahInt, keterangan) },
                        enabled = isValid,
                        testTag = "btn_simpan_kembalikan"
                    )
                }
            }
        }
    }
}

// 9. SHEET KAS UMUM
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KasSheet(
    onDismiss: () -> Unit,
    onSave: (String, String, Int, String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var tipe by remember { mutableStateOf("keluar") }
    var kategori by remember { mutableStateOf("operasional") }
    var jumlahText by remember { mutableStateOf("15000") }
    var keterangan by remember { mutableStateOf("") }

    val jumlahInt = jumlahText.toIntOrNull() ?: 0
    val isValid = jumlahInt > 0

    val kategoriList = if (tipe == "masuk") {
        listOf("setoran_warung" to "Setoran Warung", "pinjaman_kembali" to "Pinjaman Kembali", "lain" to "Pemasukan Lain")
    } else {
        listOf("operasional" to "Operasional (Bensin/Parkir)", "setor_pabrik" to "Setor Pabrik", "nabung_wajib" to "Nabung Wajib", "pinjaman_keluar" to "Pinjaman Keluar", "lain" to "Pengeluaran Lain")
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CleanSurface,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            SheetHeader(
                title = "Catat Kas Manual",
                subtitle = "Catat pemasukan atau pengeluaran operasional",
                onClose = onDismiss
            )

            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val isMasuk = tipe == "masuk"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (isMasuk) ElectricBlue else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                tipe = "masuk"
                                kategori = "lain"
                            },
                        color = if (isMasuk) ElectricBlueLight else CleanBg
                    ) {
                        Text(
                            text = "+ Pemasukan",
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (isMasuk) ElectricBlue else TextPrimary
                        )
                    }

                    val isKeluar = tipe == "keluar"
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .border(2.dp, if (isKeluar) StatusDanger else CleanCardBorder, RoundedCornerShape(12.dp))
                            .clickable {
                                tipe = "keluar"
                                kategori = "operasional"
                            },
                        color = if (isKeluar) StatusDangerBg else CleanBg
                    ) {
                        Text(
                            text = "- Pengeluaran",
                            modifier = Modifier.padding(14.dp),
                            fontWeight = FontWeight.Bold,
                            color = if (isKeluar) StatusDanger else TextPrimary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "KATEGORI TRANSAKSI",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(kategoriList) { (key, label) ->
                        ShortcutChip(
                            text = label,
                            onClick = { kategori = key },
                            isSelected = kategori == key
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "JUMLAH NOMINAL (RP)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = jumlahText,
                    onValueChange = { jumlahText = it.filter { char -> char.isDigit() } },
                    modifier = Modifier.fillMaxWidth().testTag("input_kas_jumlah"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    ),
                    textStyle = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (tipe == "masuk") ElectricBlue else StatusDanger
                    )
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "KETERANGAN",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = keterangan,
                    onValueChange = { keterangan = it },
                    placeholder = { Text("Contoh: Bensin motor, uang makan, plastik") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricBlue,
                        unfocusedBorderColor = CleanCardBorder
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                PrimaryActionButton(
                    text = "Simpan Transaksi Kas",
                    onClick = { if (isValid) onSave(tipe, kategori, jumlahInt, keterangan) },
                    enabled = isValid,
                    testTag = "btn_simpan_kas"
                )
            }
        }
    }
}
