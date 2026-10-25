package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
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
}
