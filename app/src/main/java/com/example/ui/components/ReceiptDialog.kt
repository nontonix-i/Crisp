package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.ReceiptData
import com.example.ui.theme.CleanBg
import com.example.ui.theme.CleanCardBorder
import com.example.ui.theme.ElectricBlue
import com.example.ui.theme.ElectricBlueBorder
import com.example.ui.theme.ElectricBlueLight
import com.example.ui.theme.StatusDanger
import com.example.ui.theme.StatusSuccess
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.utils.FormatUtils
import com.example.utils.ReceiptShareHelper

@Composable
fun ReceiptDialog(
    receipt: ReceiptData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .background(Color.White)
                .border(1.dp, CleanCardBorder, RoundedCornerShape(24.dp))
                .testTag("receipt_dialog"),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Header Row with Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier.clip(RoundedCornerShape(8.dp)),
                        color = ElectricBlueLight
                    ) {
                        Text(
                            text = receipt.badge,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = ElectricBlue
                            )
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("btn_close_receipt")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Tutup",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Brand Header
                Text(
                    text = "CRISPFLOW",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = ElectricBlue
                    )
                )
                Text(
                    text = receipt.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Receipt Title & Metadata
                Text(
                    text = receipt.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "No. Ref: ${receipt.receiptId}",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontFamily = FontFamily.Monospace,
                        color = TextSecondary
                    )
                )
                Text(
                    text = FormatUtils.formatDateTime(receipt.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Highlighted Primary Value Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .border(1.dp, CleanCardBorder, RoundedCornerShape(16.dp)),
                    color = CleanBg
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = receipt.primaryLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextSecondary
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = receipt.primaryValue,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = if (receipt.isPositive) StatusSuccess else StatusDanger
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(12.dp))

                // Line Items
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    receipt.lines.forEach { line ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = line.label,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (line.isBold) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (line.isBold) TextPrimary else TextSecondary
                                )
                            )
                            Text(
                                text = line.value,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (line.isBold) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        line.isHighlight -> StatusSuccess
                                        line.isNegative -> StatusDanger
                                        line.isBold -> TextPrimary
                                        else -> TextPrimary
                                    }
                                ),
                                textAlign = TextAlign.End
                            )
                        }
                    }
                }

                // Notes section if any
                if (!receipt.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    DashedDivider()
                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
                        color = CleanBg
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Catatan:",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = receipt.notes,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                DashedDivider()
                Spacer(modifier = Modifier.height(14.dp))

                // Simulated Barcode
                SimulatedBarcodeCompose(receipt.receiptId)

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = receipt.footerMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons: Share Image & Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_dismiss_receipt"),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(text = "Tutup", fontWeight = FontWeight.SemiBold)
                    }

                    Button(
                        onClick = {
                            ReceiptShareHelper.shareReceiptImage(context, receipt)
                        },
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_share_receipt_img"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ElectricBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Bagikan Gambar",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DashedDivider(
    modifier: Modifier = Modifier,
    color: Color = CleanCardBorder
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
    ) {
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        drawLine(
            color = color,
            start = Offset(0f, 0f),
            end = Offset(size.width, 0f),
            pathEffect = pathEffect,
            strokeWidth = 2f
        )
    }
}

@Composable
fun SimulatedBarcodeCompose(receiptId: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Canvas(
            modifier = Modifier
                .width(220.dp)
                .height(32.dp)
        ) {
            val barPattern = intArrayOf(
                3, 1, 4, 2, 1, 3, 2, 4, 1, 2, 4, 3, 1, 2, 3, 1,
                4, 2, 1, 3, 2, 4, 1, 3, 2, 1, 4, 2, 3, 1, 2, 4
            )
            val barHeight = size.height
            var curX = 0f
            val totalUnits = barPattern.sum() + (barPattern.size * 2)
            val unitWidth = size.width / totalUnits

            for (w in barPattern) {
                val barW = w * unitWidth
                drawRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(curX, 0f),
                    size = androidx.compose.ui.geometry.Size(barW, barHeight)
                )
                curX += barW + (unitWidth * 1.5f)
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = receiptId,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                letterSpacing = 1.sp
            ),
            color = TextSecondary
        )
    }
}
