package com.example.data.model

import com.example.utils.FormatUtils

data class ReceiptLine(
    val label: String,
    val value: String,
    val isBold: Boolean = false,
    val isHighlight: Boolean = false,
    val isNegative: Boolean = false
)

data class ReceiptData(
    val title: String,
    val subtitle: String = "CRISPFLOW • MANAJEMEN KERUPUK",
    val receiptId: String,
    val timestamp: Long,
    val badge: String,
    val isPositive: Boolean,
    val primaryLabel: String,
    val primaryValue: String,
    val lines: List<ReceiptLine>,
    val notes: String? = null,
    val footerMessage: String = "Simpan struk ini sebagai bukti catatan transaksi."
)

object ReceiptFactory {

    fun fromStokMutasi(mutasi: StokMutasi, penjualan: PenjualanHarian? = null): ReceiptData {
        val absCount = Math.abs(mutasi.jumlah)
        val idCode = String.format("%04d", mutasi.id % 10000)

        return when (mutasi.tipe) {
            "laku" -> {
                val hargaJual = penjualan?.harga_jual ?: 13000
                val totalOmset = absCount.toLong() * hargaJual
                val setorPabrik = absCount.toLong() * 11000
                val nabungWajib = absCount.toLong() * 500
                val labaBersih = totalOmset - setorPabrik - nabungWajib

                ReceiptData(
                    title = "STRUK PENJUALAN",
                    receiptId = "CF-LAKU-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "PENJUALAN",
                    isPositive = true,
                    primaryLabel = "TOTAL OMSET PENJUALAN",
                    primaryValue = FormatUtils.formatRupiah(totalOmset),
                    lines = listOf(
                        ReceiptLine("Jumlah Laku Terjual", "$absCount Pack", isBold = true),
                        ReceiptLine("Harga Satuan", "${FormatUtils.formatRupiah(hargaJual)} / pack"),
                        ReceiptLine("Subtotal Omset", FormatUtils.formatRupiah(totalOmset), isBold = true),
                        ReceiptLine("Alokasi Setor Pabrik", "- ${FormatUtils.formatRupiah(setorPabrik)}", isNegative = true),
                        ReceiptLine("Alokasi Tabungan Wajib", "- ${FormatUtils.formatRupiah(nabungWajib)}", isNegative = true),
                        ReceiptLine("Estimasi Laba Bersih", FormatUtils.formatRupiah(labaBersih), isBold = true, isHighlight = true)
                    ),
                    notes = mutasi.keterangan.ifBlank { "Penjualan langsung kerupuk ke pelanggan/warung" }
                )
            }
            "ambil_pool", "keluar_tangan", "masuk_pabrik" -> {
                ReceiptData(
                    title = "STRUK AMBIL DARI POOL",
                    receiptId = "CF-POOL-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "MUTASI STOK",
                    isPositive = true,
                    primaryLabel = "JUMLAH DIAMBIL",
                    primaryValue = "+$absCount Pack",
                    lines = listOf(
                        ReceiptLine("Barang", "Kerupuk Kulit Super", isBold = true),
                        ReceiptLine("Asal Pengambilan", "Pool Pusat / Gudang"),
                        ReceiptLine("Tujuan", "Stok Tangan Pedagang"),
                        ReceiptLine("Jumlah Masuk", "+$absCount Pack", isBold = true, isHighlight = true),
                        ReceiptLine("Harga Modal Pabrik", "Rp 11.000 / pack"),
                        ReceiptLine("Total Kewajiban Setor", FormatUtils.formatRupiah(absCount.toLong() * 11000))
                    ),
                    notes = mutasi.keterangan.ifBlank { "Ambil stok jatah harian dari pool" }
                )
            }
            "balik_pool" -> {
                ReceiptData(
                    title = "STRUK PENGEMBALIAN SISA",
                    receiptId = "CF-RET-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "RETUR POOL",
                    isPositive = false,
                    primaryLabel = "SISA DIKEMBALIKAN",
                    primaryValue = "-$absCount Pack",
                    lines = listOf(
                        ReceiptLine("Barang", "Kerupuk Kulit Super", isBold = true),
                        ReceiptLine("Asal", "Stok Tangan Pedagang"),
                        ReceiptLine("Dikembalikan Ke", "Pool Pusat / Gudang"),
                        ReceiptLine("Jumlah Balik", "$absCount Pack", isBold = true, isNegative = true),
                        ReceiptLine("Status Setoran", "Kewajiban setor berkurang otomatis")
                    ),
                    notes = mutasi.keterangan.ifBlank { "Pengembalian sisa stok sore ke pool" }
                )
            }
            "transfer_keluar" -> {
                ReceiptData(
                    title = "STRUK TRANSFER KELUAR",
                    receiptId = "CF-TRF-OUT-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "TRANSFER",
                    isPositive = false,
                    primaryLabel = "JUMLAH KELUAR",
                    primaryValue = "-$absCount Pack",
                    lines = listOf(
                        ReceiptLine("Arah Transfer", "Kirim ke Rekan Pedagang", isBold = true),
                        ReceiptLine("Jumlah Barang", "$absCount Pack", isNegative = true),
                        ReceiptLine("Status Stok", "Stok tangan berkurang")
                    ),
                    notes = mutasi.keterangan.ifBlank { "Transfer stok ke pedagang lain" }
                )
            }
            "transfer_masuk" -> {
                ReceiptData(
                    title = "STRUK TRANSFER MASUK",
                    receiptId = "CF-TRF-IN-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "TRANSFER",
                    isPositive = true,
                    primaryLabel = "JUMLAH MASUK",
                    primaryValue = "+$absCount Pack",
                    lines = listOf(
                        ReceiptLine("Arah Transfer", "Terima dari Rekan Pedagang", isBold = true),
                        ReceiptLine("Jumlah Barang", "+$absCount Pack", isHighlight = true),
                        ReceiptLine("Status Stok", "Stok tangan bertambah")
                    ),
                    notes = mutasi.keterangan.ifBlank { "Transfer stok masuk dari rekan" }
                )
            }
            else -> {
                ReceiptData(
                    title = "STRUK MUTASI STOK",
                    receiptId = "CF-MUT-$idCode",
                    timestamp = mutasi.tanggal,
                    badge = "MUTASI",
                    isPositive = mutasi.jumlah >= 0,
                    primaryLabel = "PERUBAHAN STOK",
                    primaryValue = if (mutasi.jumlah >= 0) "+$absCount Pack" else "-$absCount Pack",
                    lines = listOf(
                        ReceiptLine("Tipe Mutasi", mutasi.tipe.replace("_", " ").uppercase()),
                        ReceiptLine("Lokasi", mutasi.lokasi.uppercase()),
                        ReceiptLine("Perubahan", "${mutasi.jumlah} Pack", isBold = true)
                    ),
                    notes = mutasi.keterangan.ifBlank { "Mutasi stok sistem" }
                )
            }
        }
    }

    fun fromKasTransaksi(kas: KasTransaksi): ReceiptData {
        val isMasuk = kas.tipe == "masuk"
        val idCode = String.format("%04d", kas.id % 10000)
        val kategoriFormatted = kas.kategori.replace("_", " ").uppercase()

        val lines = mutableListOf<ReceiptLine>()
        lines.add(ReceiptLine("Kategori Kas", kategoriFormatted, isBold = true))
        lines.add(ReceiptLine("Jenis Aliran", if (isMasuk) "Pemasukan (+)" else "Pengeluaran (-)", isHighlight = isMasuk, isNegative = !isMasuk))
        lines.add(ReceiptLine("Nominal Transaksi", FormatUtils.formatRupiah(kas.jumlah), isBold = true))

        val noteClean = kas.keterangan.ifBlank {
            when (kas.kategori) {
                "setor_pabrik" -> "Setoran uang hasil jualan ke pabrik kerupuk"
                "nabung_wajib" -> "Tabungan wajib pedagang Rp 500 per pack terjual"
                "setoran_warung" -> "Penerimaan hasil penjualan tunai dari warung"
                "operasional" -> "Biaya operasional harian / bensin / konsumsi"
                "pinjaman_keluar" -> "Peminjaman dana setoran"
                "pinjaman_kembali" -> "Pengembalian pelunasan dana pinjaman"
                else -> "Transaksi kas operasional"
            }
        }

        return ReceiptData(
            title = when (kas.kategori) {
                "setor_pabrik" -> "STRUK SETORAN PABRIK"
                "nabung_wajib" -> "STRUK TABUNGAN WAJIB"
                else -> if (isMasuk) "STRUK KAS MASUK" else "STRUK KAS KELUAR"
            },
            receiptId = "CF-KAS-$idCode",
            timestamp = kas.tanggal,
            badge = if (isMasuk) "KAS MASUK" else "KAS KELUAR",
            isPositive = isMasuk,
            primaryLabel = if (isMasuk) "TOTAL PENERIMAAN" else "TOTAL PENGELUARAN",
            primaryValue = "${if (isMasuk) "+" else "-"}${FormatUtils.formatRupiah(kas.jumlah)}",
            lines = lines,
            notes = noteClean
        )
    }

    fun fromTransferPedagang(transfer: TransferPedagang): ReceiptData {
        val isKeluar = transfer.arah == "keluar"
        val idCode = String.format("%04d", transfer.id % 10000)

        return ReceiptData(
            title = if (isKeluar) "STRUK TRANSFER KELUAR" else "STRUK TRANSFER MASUK",
            receiptId = "CF-TRF-$idCode",
            timestamp = transfer.tanggal,
            badge = "TRANSFER",
            isPositive = !isKeluar,
            primaryLabel = if (isKeluar) "JUMLAH DITRANSFER" else "JUMLAH DITERIMA",
            primaryValue = "${if (isKeluar) "-" else "+"}${transfer.jumlah} Pack",
            lines = listOf(
                ReceiptLine("Nama Rekan Pedagang", transfer.nama_pedagang, isBold = true),
                ReceiptLine("Arah Aliran", if (isKeluar) "Keluar (Memberi ke kawan)" else "Masuk (Menerima dari kawan)"),
                ReceiptLine("Volume Kerupuk", "${transfer.jumlah} Pack", isBold = true, isHighlight = !isKeluar, isNegative = isKeluar),
                ReceiptLine("Status Catatan", "Tercatat di pembukuan", isBold = true)
            ),
            notes = transfer.keterangan.ifBlank { "Transfer pack kerupuk antar sesama pedagang" }
        )
    }

    fun fromPinjaman(pinjaman: PinjamanSetoran): ReceiptData {
        val isKeluar = pinjaman.tipe == "keluar"
        val idCode = String.format("%04d", pinjaman.id % 10000)
        val isLunas = pinjaman.status == "lunas"

        return ReceiptData(
            title = if (isKeluar) "STRUK PINJAMAN SETORAN" else "STRUK PENGEMBALIAN PINJAMAN",
            receiptId = "CF-PJM-$idCode",
            timestamp = pinjaman.tanggal,
            badge = if (isKeluar) "PINJAMAN DIBERIKAN" else "PENGEMBALIAN",
            isPositive = !isKeluar,
            primaryLabel = if (isKeluar) "TOTAL DANA DIPINJAM" else "TOTAL DANA DIKEMBALIKAN",
            primaryValue = FormatUtils.formatRupiah(pinjaman.jumlah),
            lines = listOf(
                ReceiptLine("Pihak / Rekan", pinjaman.pihak, isBold = true),
                ReceiptLine("Jenis Transaksi", if (isKeluar) "Pinjaman Baru" else "Pelunasan Pinjaman"),
                ReceiptLine("Nominal", FormatUtils.formatRupiah(pinjaman.jumlah), isBold = true, isHighlight = isLunas),
                ReceiptLine("Status Pembayaran", if (isLunas) "LUNAS ✓" else "BELUM LUNAS", isBold = true, isNegative = !isLunas)
            ),
            notes = pinjaman.keterangan.ifBlank { "Pinjaman dana setoran pedagang" }
        )
    }

    fun fromPenjualanHarian(penjualan: PenjualanHarian): ReceiptData {
        val idCode = String.format("%04d", penjualan.id % 10000)
        val labaBersih = penjualan.pendapatan - penjualan.kewajiban_setor - penjualan.nabung_wajib

        return ReceiptData(
            title = "STRUK REKAP PENJUALAN",
            receiptId = "CF-RKP-$idCode",
            timestamp = penjualan.created_at,
            badge = "REKAP HARIAN",
            isPositive = true,
            primaryLabel = "TOTAL OMSET HARIAN",
            primaryValue = FormatUtils.formatRupiah(penjualan.pendapatan),
            lines = listOf(
                ReceiptLine("Tanggal Penjualan", penjualan.tanggal, isBold = true),
                ReceiptLine("Jumlah Laku", "${penjualan.jumlah_laku} Pack", isBold = true),
                ReceiptLine("Harga Jual Rata-rata", "${FormatUtils.formatRupiah(penjualan.harga_jual)} / pack"),
                ReceiptLine("Total Omset Penerimaan", FormatUtils.formatRupiah(penjualan.pendapatan), isBold = true),
                ReceiptLine("Kewajiban Setor Pabrik", "- ${FormatUtils.formatRupiah(penjualan.kewajiban_setor)}", isNegative = true),
                ReceiptLine("Tabungan Wajib Disimpan", "- ${FormatUtils.formatRupiah(penjualan.nabung_wajib)}", isNegative = true),
                ReceiptLine("Laba Bersih Saya", FormatUtils.formatRupiah(labaBersih), isBold = true, isHighlight = true)
            ),
            notes = "Rekap penjualan harian terverifikasi"
        )
    }
}
