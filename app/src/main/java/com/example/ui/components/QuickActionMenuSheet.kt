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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CleanBg
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.CleanSurface
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class QuickActionType {
    CATAT_LAKU,
    AMBIL_JATAH,
    BALIK_POOL,
    MASUK_PABRIK,
    TRANSFER_PEDAGANG,
    SETOR_PABRIK,
    CATAT_KAS,
    PINJAM_SETORAN
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickActionMenuSheet(
    onDismiss: () -> Unit,
    onSelectAction: (QuickActionType) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

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
        ) {
            SheetHeader(
                title = "Aksi Cepat",
                subtitle = "Pilih transaksi yang ingin dicatat",
                onClose = onDismiss
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Catat Laku",
                        sub = "Hasil jualan warung",
                        icon = Icons.Default.PointOfSale,
                        iconBg = ElectricBlue,
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.CATAT_LAKU)
                        },
                        tag = "action_tile_catat_laku"
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Ambil Pool",
                        sub = "Tambah pack jualan",
                        icon = Icons.Default.PanTool,
                        iconBg = ElectricBlue,
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.AMBIL_JATAH)
                        },
                        tag = "action_tile_ambil_jatah"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Balik ke Pool",
                        sub = "Sisa jualan dikembalikan",
                        icon = Icons.Default.ArrowBack,
                        iconBg = Color(0xFF64748B),
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.BALIK_POOL)
                        },
                        tag = "action_tile_balik_pool"
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Transfer",
                        sub = "Tukar kawan pedagang",
                        icon = Icons.Default.SwapHoriz,
                        iconBg = Color(0xFF0284C7),
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.TRANSFER_PEDAGANG)
                        },
                        tag = "action_tile_transfer"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Setor Pabrik",
                        sub = "Bayar kewajiban setor",
                        icon = Icons.Default.Paid,
                        iconBg = StatusSuccess,
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.SETOR_PABRIK)
                        },
                        tag = "action_tile_setor"
                    )
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Pinjam Setoran",
                        sub = "Orang atau prive",
                        icon = Icons.Default.MonetizationOn,
                        iconBg = Color(0xFFF59E0B),
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.PINJAM_SETORAN)
                        },
                        tag = "action_tile_pinjam"
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ActionTile(
                        modifier = Modifier.weight(1f),
                        title = "Catat Kas",
                        sub = "Pemasukan / operasional",
                        icon = Icons.Default.AccountBalanceWallet,
                        iconBg = ElectricBlue,
                        onClick = {
                            onDismiss()
                            onSelectAction(QuickActionType.CATAT_KAS)
                        },
                        tag = "action_tile_kas"
                    )
                }
            }
        }
    }
}

@Composable
fun ActionTile(
    modifier: Modifier = Modifier,
    title: String,
    sub: String,
    icon: ImageVector,
    iconBg: Color,
    onClick: () -> Unit,
    tag: String
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag(tag),
        color = CleanBg
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(iconBg, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = sub,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
