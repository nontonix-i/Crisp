package com.example.data.repository

import com.example.data.dao.KerupukDao
import com.example.data.model.Barang
import com.example.data.model.KasTransaksi
import com.example.data.model.PenjualanHarian
import com.example.data.model.PinjamanSetoran
import com.example.data.model.Setoran
import com.example.data.model.StokMutasi
import com.example.data.model.TransferPedagang
import com.example.utils.BackupDataPackage
import com.example.utils.BackupZipHelper
import com.example.utils.FormatUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first

data class ActiveLoanWithBalance(
    val loan: PinjamanSetoran,
    val totalRepaid: Int,
    val remainingBalance: Int
)

data class DashboardSummary(
    val stokPool: Int = 0,
    val stokTangan: Int = 0,
    val lakuHariIni: Int = 0,
    val pendapatanHariIni: Int = 0,
    val nabungWajibHariIni: Int = 0,
    val totalKewajibanSetor: Int = 0,
    val totalSudahSetor: Int = 0,
    val sisaKewajibanSetor: Int = 0,
    val saldoKasUmum: Int = 0,
    val saldoKasSetoranTersedia: Int = 0,
    val totalNabungWajib: Int = 0,
    val piutangPedagang: Int = 0,
    val priveSendiri: Int = 0,
    val totalPinjamanAktif: Int = 0,
    val labaKotorHariIni: Int = 0,
    val labaBersihHariIni: Int = 0
)

class KerupukRepository(private val dao: KerupukDao) {

    suspend fun ensureDefaultBarang(): Barang {
        val existing = dao.getBarang()
        if (existing != null) return existing
        val defaultBarang = Barang(
            nama = "Crisp Snack Pack",
            hpp = 11000,
            harga_jual_default = 15000,
            nabung_wajib = 500,
            tarif_setor = 11000
        )
        dao.insertBarang(defaultBarang)
        return defaultBarang
    }

    // Flows
    val stokPool: Flow<Int> = dao.getStokByLokasi("pool")
    val stokTangan: Flow<Int> = dao.getStokByLokasi("tangan")
    val allMutasi: Flow<List<StokMutasi>> = dao.getAllMutasi()
    val recentMutasi: Flow<List<StokMutasi>> = dao.getRecentMutasi(15)
    val allPenjualan: Flow<List<PenjualanHarian>> = dao.getAllPenjualan()
    val allTransfer: Flow<List<TransferPedagang>> = dao.getAllTransfer()
    val allSetoran: Flow<List<Setoran>> = dao.getAllSetoran()
    val allKas: Flow<List<KasTransaksi>> = dao.getAllKas()
    val recentKas: Flow<List<KasTransaksi>> = dao.getRecentKas(15)
    val allPinjaman: Flow<List<PinjamanSetoran>> = dao.getAllPinjaman()
    val totalSudahSetor: Flow<Int> = dao.getTotalSudahSetor()
    val totalKewajibanSetor: Flow<Int> = dao.getTotalKewajibanSetor()
    val totalNabungWajib: Flow<Int> = dao.getTotalNabungWajib()
    val totalKasMasuk: Flow<Int> = dao.getTotalKasMasuk()
    val totalKasKeluar: Flow<Int> = dao.getTotalKasKeluar()

    fun observeTodayPenjualan(): Flow<PenjualanHarian?> {
        val todayKey = FormatUtils.getTodayDateKey()
        return dao.observePenjualanByTanggal(todayKey)
    }

    fun observeTodayMutasi(): Flow<List<StokMutasi>> {
        val startOfDay = FormatUtils.getStartOfDay()
        val endOfDay = FormatUtils.getEndOfDay()
        return dao.getMutasiToday(startOfDay, endOfDay)
    }

    // Active Loans with remaining balances
    suspend fun getActiveLoansWithBalance(): List<ActiveLoanWithBalance> {
        val keluarLoans = dao.getAllPinjamanKeluar().first().filter { it.status == "belum_lunas" }
        return keluarLoans.map { loan ->
            val repayments = dao.getPengembalianByParent(loan.id)
            val repaidSum = repayments.sumOf { it.jumlah }
            val remaining = (loan.jumlah - repaidSum).coerceAtLeast(0)
            ActiveLoanWithBalance(
                loan = loan,
                totalRepaid = repaidSum,
                remainingBalance = remaining
            )
        }
    }

    // === MUTATION ACTIONS ===

    suspend fun catatPenjualanHariIni(
        jumlahLaku: Int,
        hargaJual: Int,
        tanggalKey: String = FormatUtils.getTodayDateKey()
    ): Result<Unit> {
        if (jumlahLaku <= 0) return Result.failure(Exception("Jumlah laku harus lebih dari 0"))
        val currentTangan = dao.getStokByLokasi("tangan").first()

        // Check if there is existing record for this date
        val existing = dao.getPenjualanByTanggal(tanggalKey)
        val adjustedMax = if (existing != null) currentTangan + existing.jumlah_laku else currentTangan

        if (jumlahLaku > adjustedMax) {
            return Result.failure(Exception("Jumlah laku ($jumlahLaku) melebihi stok di tangan ($adjustedMax)"))
        }

        val pendapatan = jumlahLaku * hargaJual
        val nabungWajib = jumlahLaku * 500
        val kewajibanSetor = jumlahLaku * 11000

        // If existing record exists for this day, clean up old records first
        if (existing != null) {
            dao.deletePenjualanByTanggal(tanggalKey)
        }

        val penjualan = PenjualanHarian(
            tanggal = tanggalKey,
            jumlah_laku = jumlahLaku,
            harga_jual = hargaJual,
            pendapatan = pendapatan,
            nabung_wajib = nabungWajib,
            kewajiban_setor = kewajibanSetor
        )
        val id = dao.insertOrUpdatePenjualan(penjualan)

        // Record stock mutation
        dao.insertMutasi(
            StokMutasi(
                tanggal = System.currentTimeMillis(),
                lokasi = "tangan",
                tipe = "laku",
                jumlah = -jumlahLaku,
                ref_id = id,
                keterangan = "Laku $jumlahLaku pack @${FormatUtils.formatRupiah(hargaJual)}"
            )
        )

        // Record kas masuk (pendapatan jualan warung)
        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = "masuk",
                kategori = "setoran_warung",
                jumlah = pendapatan,
                keterangan = "Hasil jualan $tanggalKey ($jumlahLaku pack)"
            )
        )

        // Record kas keluar (nabung wajib otomatis)
        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = "keluar",
                kategori = "nabung_wajib",
                jumlah = nabungWajib,
                keterangan = "Nabung wajib ($jumlahLaku pack × Rp 500)"
            )
        )

        return Result.success(Unit)
    }

    suspend fun ambilJatahPool(jumlah: Int): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah ambil harus lebih dari 0"))

        // User directly takes stock from pool into hand inventory
        dao.insertMutasi(
            StokMutasi(
                tanggal = System.currentTimeMillis(),
                lokasi = "tangan",
                tipe = "ambil_pool",
                jumlah = jumlah,
                keterangan = "Ambil jatah dari pool ($jumlah pack)"
            )
        )

        return Result.success(Unit)
    }

    suspend fun balikSisaPool(jumlah: Int): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah balik harus lebih dari 0"))
        val currentTangan = dao.getStokByLokasi("tangan").first()
        if (jumlah > currentTangan) {
            return Result.failure(Exception("Stok di tangan ($currentTangan) tidak mencukupi untuk kembalikan $jumlah pack"))
        }

        // Mutation tangan: -jumlah returned to pool
        dao.insertMutasi(
            StokMutasi(
                tanggal = System.currentTimeMillis(),
                lokasi = "tangan",
                tipe = "balik_pool",
                jumlah = -jumlah,
                keterangan = "Balik sisa jualan ke pool ($jumlah pack)"
            )
        )

        return Result.success(Unit)
    }

    suspend fun masukDariPabrik(jumlah: Int, keterangan: String = "Kiriman mingguan pabrik"): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah kiriman harus lebih dari 0"))

        dao.insertMutasi(
            StokMutasi(
                tanggal = System.currentTimeMillis(),
                lokasi = "pool",
                tipe = "masuk_pabrik",
                jumlah = jumlah,
                keterangan = if (keterangan.isNotBlank()) keterangan else "Kiriman mingguan pabrik"
            )
        )
        return Result.success(Unit)
    }

    suspend fun transferPedagang(
        arah: String, // "keluar" | "masuk"
        namaPedagang: String,
        jumlah: Int,
        keterangan: String
    ): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah transfer harus lebih dari 0"))
        if (namaPedagang.isBlank()) return Result.failure(Exception("Nama pedagang wajib diisi"))

        val currentTangan = dao.getStokByLokasi("tangan").first()
        if (arah == "keluar" && jumlah > currentTangan) {
            return Result.failure(Exception("Stok di tangan ($currentTangan) tidak cukup untuk transfer keluar $jumlah"))
        }

        val mutasiJumlah = if (arah == "keluar") -jumlah else jumlah
        val tipeMutasi = if (arah == "keluar") "transfer_keluar" else "transfer_masuk"
        val desc = if (arah == "keluar") "Transfer keluar ke $namaPedagang" else "Transfer masuk dari $namaPedagang"

        val tId = dao.insertTransfer(
            TransferPedagang(
                tanggal = System.currentTimeMillis(),
                arah = arah,
                nama_pedagang = namaPedagang.trim(),
                jumlah = jumlah,
                keterangan = keterangan
            )
        )

        dao.insertMutasi(
            StokMutasi(
                tanggal = System.currentTimeMillis(),
                lokasi = "tangan",
                tipe = tipeMutasi,
                jumlah = mutasiJumlah,
                ref_id = tId,
                keterangan = if (keterangan.isNotBlank()) "$desc - $keterangan" else desc
            )
        )

        return Result.success(Unit)
    }

    suspend fun setorWajib(jumlah: Int, keterangan: String = "Setor ke pabrik"): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah setoran harus lebih dari 0"))

        dao.insertSetoran(
            Setoran(
                tanggal = System.currentTimeMillis(),
                jumlah = jumlah,
                keterangan = keterangan
            )
        )

        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = "keluar",
                kategori = "setor_pabrik",
                jumlah = jumlah,
                keterangan = if (keterangan.isNotBlank()) keterangan else "Setor wajib pabrik"
            )
        )

        return Result.success(Unit)
    }

    suspend fun pinjamUangSetoran(
        pihak: String, // "Sendiri" atau nama pedagang
        jumlah: Int,
        keterangan: String
    ): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah pinjaman harus lebih dari 0"))
        if (pihak.isBlank()) return Result.failure(Exception("Pihak peminjam wajib diisi"))

        // Insert pinjaman
        dao.insertPinjaman(
            PinjamanSetoran(
                tanggal = System.currentTimeMillis(),
                tipe = "keluar",
                pihak = pihak.trim(),
                jumlah = jumlah,
                status = "belum_lunas",
                keterangan = keterangan
            )
        )

        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = "keluar",
                kategori = "pinjaman_keluar",
                jumlah = jumlah,
                keterangan = "Pinjaman setoran ($pihak) ${if (keterangan.isNotBlank()) "- $keterangan" else ""}"
            )
        )

        return Result.success(Unit)
    }

    suspend fun kembalikanPinjaman(
        parentLoanId: Long,
        jumlahKembali: Int,
        keterangan: String = ""
    ): Result<Unit> {
        if (jumlahKembali <= 0) return Result.failure(Exception("Jumlah pengembalian harus lebih dari 0"))
        val loan = dao.getPinjamanById(parentLoanId)
            ?: return Result.failure(Exception("Pinjaman tidak ditemukan"))

        val repayments = dao.getPengembalianByParent(parentLoanId)
        val previouslyRepaid = repayments.sumOf { it.jumlah }
        val remaining = loan.jumlah - previouslyRepaid

        if (jumlahKembali > remaining) {
            return Result.failure(Exception("Jumlah kembali (${FormatUtils.formatRupiah(jumlahKembali)}) melebihi sisa pinjaman (${FormatUtils.formatRupiah(remaining)})"))
        }

        val newTotalRepaid = previouslyRepaid + jumlahKembali
        val isFullRepaid = newTotalRepaid >= loan.jumlah

        // Insert return record
        dao.insertPinjaman(
            PinjamanSetoran(
                tanggal = System.currentTimeMillis(),
                tipe = "kembali",
                pihak = loan.pihak,
                jumlah = jumlahKembali,
                status = if (isFullRepaid) "lunas" else "belum_lunas",
                parent_id = parentLoanId,
                keterangan = if (keterangan.isNotBlank()) keterangan else "Pengembalian pinjaman ${loan.pihak}"
            )
        )

        // Update original loan status if fully paid
        if (isFullRepaid) {
            dao.updatePinjaman(
                loan.copy(
                    status = "lunas",
                    tanggal_kembali = System.currentTimeMillis()
                )
            )
        }

        // Record kas masuk
        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = "masuk",
                kategori = "pinjaman_kembali",
                jumlah = jumlahKembali,
                keterangan = "Pengembalian pinjaman (${loan.pihak})"
            )
        )

        return Result.success(Unit)
    }

    suspend fun catatKasUmum(
        tipe: String, // "masuk" | "keluar"
        kategori: String,
        jumlah: Int,
        keterangan: String
    ): Result<Unit> {
        if (jumlah <= 0) return Result.failure(Exception("Jumlah kas harus lebih dari 0"))

        dao.insertKas(
            KasTransaksi(
                tanggal = System.currentTimeMillis(),
                tipe = tipe,
                kategori = kategori,
                jumlah = jumlah,
                keterangan = keterangan
            )
        )
        return Result.success(Unit)
    }

    // === BACKUP & RESTORE ===
    suspend fun getBackupDataPackage(): BackupDataPackage {
        val barangList = dao.getAllBarangList()
        val mutasiList = dao.getAllMutasiList()
        val penjualanList = dao.getAllPenjualanList()
        val transferList = dao.getAllTransferList()
        val setoranList = dao.getAllSetoranList()
        val pinjamanList = dao.getAllPinjamanList()
        val kasList = dao.getAllKasList()

        return BackupZipHelper.createBackupPackage(
            barangList = barangList,
            mutasiList = mutasiList,
            penjualanList = penjualanList,
            transferList = transferList,
            setoranList = setoranList,
            pinjamanList = pinjamanList,
            kasList = kasList
        )
    }

    suspend fun restoreBackupDataPackage(backupPackage: BackupDataPackage): Result<Unit> {
        return try {
            dao.restoreDatabaseAtomic(
                barangList = backupPackage.barangList,
                mutasiList = backupPackage.mutasiList,
                penjualanList = backupPackage.penjualanList,
                transferList = backupPackage.transferList,
                setoranList = backupPackage.setoranList,
                pinjamanList = backupPackage.pinjamanList,
                kasList = backupPackage.kasList
            )
            ensureDefaultBarang()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(Exception("Gagal melakukan pemulihan database: ${e.message}"))
        }
    }
}
