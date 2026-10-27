package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.Barang
import com.example.data.model.KasTransaksi
import com.example.data.model.PenjualanHarian
import com.example.data.model.PinjamanSetoran
import com.example.data.model.Setoran
import com.example.data.model.StokMutasi
import com.example.data.model.TransferPedagang
import kotlinx.coroutines.flow.Flow

@Dao
interface KerupukDao {

    // === BARANG ===
    @Query("SELECT * FROM barang LIMIT 1")
    suspend fun getBarang(): Barang?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBarang(barang: Barang): Long

    // === STOK MUTASI ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMutasi(mutasi: StokMutasi): Long

    @Query("SELECT * FROM stok_mutasi ORDER BY tanggal DESC, id DESC")
    fun getAllMutasi(): Flow<List<StokMutasi>>

    @Query("SELECT * FROM stok_mutasi ORDER BY tanggal DESC, id DESC LIMIT :limit")
    fun getRecentMutasi(limit: Int = 10): Flow<List<StokMutasi>>

    @Query("SELECT * FROM stok_mutasi WHERE tanggal >= :startOfDay AND tanggal <= :endOfDay ORDER BY tanggal DESC, id DESC")
    fun getMutasiToday(startOfDay: Long, endOfDay: Long): Flow<List<StokMutasi>>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM stok_mutasi WHERE lokasi = :lokasi")
    fun getStokByLokasi(lokasi: String): Flow<Int>

    // === PENJUALAN HARIAN ===
    @Query("SELECT * FROM penjualan_harian WHERE tanggal = :tanggal LIMIT 1")
    suspend fun getPenjualanByTanggal(tanggal: String): PenjualanHarian?

    @Query("SELECT * FROM penjualan_harian WHERE tanggal = :tanggal LIMIT 1")
    fun observePenjualanByTanggal(tanggal: String): Flow<PenjualanHarian?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePenjualan(penjualan: PenjualanHarian): Long

    @Query("DELETE FROM penjualan_harian WHERE tanggal = :tanggal")
    suspend fun deletePenjualanByTanggal(tanggal: String)

    @Query("SELECT * FROM penjualan_harian ORDER BY tanggal DESC")
    fun getAllPenjualan(): Flow<List<PenjualanHarian>>

    @Query("SELECT COALESCE(SUM(kewajiban_setor), 0) FROM penjualan_harian")
    fun getTotalKewajibanSetor(): Flow<Int>

    @Query("SELECT COALESCE(SUM(nabung_wajib), 0) FROM penjualan_harian")
    fun getTotalNabungWajib(): Flow<Int>

    // === TRANSFER PEDAGANG ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransfer(transfer: TransferPedagang): Long

    @Query("SELECT * FROM transfer_pedagang ORDER BY tanggal DESC, id DESC")
    fun getAllTransfer(): Flow<List<TransferPedagang>>

    // === SETORAN ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSetoran(setoran: Setoran): Long

    @Query("SELECT * FROM setoran ORDER BY tanggal DESC, id DESC")
    fun getAllSetoran(): Flow<List<Setoran>>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM setoran")
    fun getTotalSudahSetor(): Flow<Int>

    // === PINJAMAN SETORAN ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPinjaman(pinjaman: PinjamanSetoran): Long

    @Update
    suspend fun updatePinjaman(pinjaman: PinjamanSetoran)

    @Query("SELECT * FROM pinjaman_setoran WHERE id = :id LIMIT 1")
    suspend fun getPinjamanById(id: Long): PinjamanSetoran?

    @Query("SELECT * FROM pinjaman_setoran ORDER BY tanggal DESC, id DESC")
    fun getAllPinjaman(): Flow<List<PinjamanSetoran>>

    @Query("SELECT * FROM pinjaman_setoran WHERE tipe = 'keluar' ORDER BY tanggal DESC, id DESC")
    fun getAllPinjamanKeluar(): Flow<List<PinjamanSetoran>>

    @Query("SELECT * FROM pinjaman_setoran WHERE tipe = 'kembali' AND parent_id = :parentId")
    suspend fun getPengembalianByParent(parentId: Long): List<PinjamanSetoran>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM pinjaman_setoran WHERE tipe = 'keluar'")
    fun getTotalPinjamanKeluar(): Flow<Int>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM pinjaman_setoran WHERE tipe = 'kembali'")
    fun getTotalPinjamanKembali(): Flow<Int>

    // === KAS TRANSAKSI ===
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKas(kas: KasTransaksi): Long

    @Query("SELECT * FROM kas_transaksi ORDER BY tanggal DESC, id DESC")
    fun getAllKas(): Flow<List<KasTransaksi>>

    @Query("SELECT * FROM kas_transaksi ORDER BY tanggal DESC, id DESC LIMIT :limit")
    fun getRecentKas(limit: Int = 15): Flow<List<KasTransaksi>>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM kas_transaksi WHERE tipe = 'masuk'")
    fun getTotalKasMasuk(): Flow<Int>

    @Query("SELECT COALESCE(SUM(jumlah), 0) FROM kas_transaksi WHERE tipe = 'keluar'")
    fun getTotalKasKeluar(): Flow<Int>

    // === BACKUP & RESTORE QUERIES ===
    @Query("SELECT * FROM barang")
    suspend fun getAllBarangList(): List<Barang>

    @Query("SELECT * FROM stok_mutasi ORDER BY id ASC")
    suspend fun getAllMutasiList(): List<StokMutasi>

    @Query("SELECT * FROM penjualan_harian ORDER BY tanggal ASC")
    suspend fun getAllPenjualanList(): List<PenjualanHarian>

    @Query("SELECT * FROM transfer_pedagang ORDER BY id ASC")
    suspend fun getAllTransferList(): List<TransferPedagang>

    @Query("SELECT * FROM setoran ORDER BY id ASC")
    suspend fun getAllSetoranList(): List<Setoran>

    @Query("SELECT * FROM pinjaman_setoran ORDER BY id ASC")
    suspend fun getAllPinjamanList(): List<PinjamanSetoran>

    @Query("SELECT * FROM kas_transaksi ORDER BY id ASC")
    suspend fun getAllKasList(): List<KasTransaksi>

    // Batch Inserts for Restore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllBarang(list: List<Barang>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllMutasi(list: List<StokMutasi>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPenjualan(list: List<PenjualanHarian>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllTransfer(list: List<TransferPedagang>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSetoran(list: List<Setoran>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllPinjaman(list: List<PinjamanSetoran>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllKas(list: List<KasTransaksi>)

    // Delete methods
    @Query("DELETE FROM barang")
    suspend fun deleteAllBarang()

    @Query("DELETE FROM stok_mutasi")
    suspend fun deleteAllMutasi()

    @Query("DELETE FROM penjualan_harian")
    suspend fun deleteAllPenjualan()

    @Query("DELETE FROM transfer_pedagang")
    suspend fun deleteAllTransfer()

    @Query("DELETE FROM setoran")
    suspend fun deleteAllSetoran()

    @Query("DELETE FROM pinjaman_setoran")
    suspend fun deleteAllPinjaman()

    @Query("DELETE FROM kas_transaksi")
    suspend fun deleteAllKas()

    @Transaction
    suspend fun restoreDatabaseAtomic(
        barangList: List<Barang>,
        mutasiList: List<StokMutasi>,
        penjualanList: List<PenjualanHarian>,
        transferList: List<TransferPedagang>,
        setoranList: List<Setoran>,
        pinjamanList: List<PinjamanSetoran>,
        kasList: List<KasTransaksi>
    ) {
        deleteAllBarang()
        deleteAllMutasi()
        deleteAllPenjualan()
        deleteAllTransfer()
        deleteAllSetoran()
        deleteAllPinjaman()
        deleteAllKas()

        if (barangList.isNotEmpty()) insertAllBarang(barangList)
        if (mutasiList.isNotEmpty()) insertAllMutasi(mutasiList)
        if (penjualanList.isNotEmpty()) insertAllPenjualan(penjualanList)
        if (transferList.isNotEmpty()) insertAllTransfer(transferList)
        if (setoranList.isNotEmpty()) insertAllSetoran(setoranList)
        if (pinjamanList.isNotEmpty()) insertAllPinjaman(pinjamanList)
        if (kasList.isNotEmpty()) insertAllKas(kasList)
    }
}
