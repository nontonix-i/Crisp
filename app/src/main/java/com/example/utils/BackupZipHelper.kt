package com.example.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.model.Barang
import com.example.data.model.KasTransaksi
import com.example.data.model.PenjualanHarian
import com.example.data.model.PinjamanSetoran
import com.example.data.model.Setoran
import com.example.data.model.StokMutasi
import com.example.data.model.TransferPedagang
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

data class BackupManifest(
    val version: Int = 1,
    val appName: String = "CrispFlow",
    val backupDate: String,
    val timestamp: Long,
    val totalRecords: Int,
    val counts: Map<String, Int>
)

data class BackupDataPackage(
    val manifest: BackupManifest,
    val barangList: List<Barang>,
    val mutasiList: List<StokMutasi>,
    val penjualanList: List<PenjualanHarian>,
    val transferList: List<TransferPedagang>,
    val setoranList: List<Setoran>,
    val pinjamanList: List<PinjamanSetoran>,
    val kasList: List<KasTransaksi>
)

object BackupZipHelper {

    private const val MANIFEST_FILE = "backup_manifest.json"
    private const val BARANG_FILE = "barang.json"
    private const val MUTASI_FILE = "stok_mutasi.json"
    private const val PENJUALAN_FILE = "penjualan_harian.json"
    private const val TRANSFER_FILE = "transfer_pedagang.json"
    private const val SETORAN_FILE = "setoran.json"
    private const val PINJAMAN_FILE = "pinjaman_setoran.json"
    private const val KAS_FILE = "kas_transaksi.json"
    private const val README_FILE = "README.txt"

    fun createBackupPackage(
        barangList: List<Barang>,
        mutasiList: List<StokMutasi>,
        penjualanList: List<PenjualanHarian>,
        transferList: List<TransferPedagang>,
        setoranList: List<Setoran>,
        pinjamanList: List<PinjamanSetoran>,
        kasList: List<KasTransaksi>
    ): BackupDataPackage {
        val now = System.currentTimeMillis()
        val dateStr = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale("id", "ID")).format(Date(now))
        val counts = mapOf(
            "barang" to barangList.size,
            "stok_mutasi" to mutasiList.size,
            "penjualan_harian" to penjualanList.size,
            "transfer_pedagang" to transferList.size,
            "setoran" to setoranList.size,
            "pinjaman_setoran" to pinjamanList.size,
            "kas_transaksi" to kasList.size
        )
        val total = counts.values.sum()
        val manifest = BackupManifest(
            version = 1,
            appName = "CrispFlow",
            backupDate = dateStr,
            timestamp = now,
            totalRecords = total,
            counts = counts
        )
        return BackupDataPackage(
            manifest = manifest,
            barangList = barangList,
            mutasiList = mutasiList,
            penjualanList = penjualanList,
            transferList = transferList,
            setoranList = setoranList,
            pinjamanList = pinjamanList,
            kasList = kasList
        )
    }

    fun exportToZipFile(context: Context, backupPackage: BackupDataPackage): File {
        val backupDir = File(context.cacheDir, "backups").apply {
            if (!exists()) mkdirs()
        }

        val fileTimestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date(backupPackage.manifest.timestamp))
        val zipFile = File(backupDir, "CrispFlow_Backup_$fileTimestamp.zip")
        if (zipFile.exists()) {
            zipFile.delete()
        }

        ZipOutputStream(FileOutputStream(zipFile).buffered()).use { zos ->
            // 1. Manifest
            writeZipEntry(zos, MANIFEST_FILE, serializeManifest(backupPackage.manifest).toByteArray(Charsets.UTF_8))

            // 2. Data tables
            writeZipEntry(zos, BARANG_FILE, serializeBarang(backupPackage.barangList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, MUTASI_FILE, serializeMutasi(backupPackage.mutasiList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, PENJUALAN_FILE, serializePenjualan(backupPackage.penjualanList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, TRANSFER_FILE, serializeTransfer(backupPackage.transferList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, SETORAN_FILE, serializeSetoran(backupPackage.setoranList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, PINJAMAN_FILE, serializePinjaman(backupPackage.pinjamanList).toByteArray(Charsets.UTF_8))
            writeZipEntry(zos, KAS_FILE, serializeKas(backupPackage.kasList).toByteArray(Charsets.UTF_8))

            // 3. Readme
            val readmeText = buildString {
                append("=== CRISPFLOW DATA BACKUP ARCHIVE ===\n")
                append("Aplikasi: CrispFlow (Kerupuk & Snack Management)\n")
                append("Waktu Cadangan: ${backupPackage.manifest.backupDate}\n")
                append("Total Baris Data: ${backupPackage.manifest.totalRecords}\n\n")
                append("Rincian Data:\n")
                backupPackage.manifest.counts.forEach { (table, count) ->
                    append("- $table: $count rekaman\n")
                }
                append("\nFile ini dapat diimpor kembali ke aplikasi CrispFlow kapan saja melalui menu Laporan > Import Data ZIP.\n")
            }
            writeZipEntry(zos, README_FILE, readmeText.toByteArray(Charsets.UTF_8))
        }

        return zipFile
    }

    private fun writeZipEntry(zos: ZipOutputStream, entryName: String, data: ByteArray) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(data)
        zos.closeEntry()
    }

    fun shareZipFile(context: Context, file: File, backupDate: String) {
        val authority = "${context.packageName}.fileprovider"
        val contentUri = FileProvider.getUriForFile(context, authority, file)

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "application/zip"
            putExtra(Intent.EXTRA_STREAM, contentUri)
            putExtra(Intent.EXTRA_SUBJECT, "Backup Data CrispFlow - $backupDate")
            putExtra(
                Intent.EXTRA_TEXT,
                "Cadangan data CrispFlow lengkap tanggal $backupDate. Simpan file .zip ini di Google Drive atau penyimpanan aman."
            )
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(shareIntent, "Bagikan / Simpan Backup ZIP CrispFlow")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    fun readBackupFromUri(context: Context, uri: Uri): Result<BackupDataPackage> {
        return try {
            val contentResolver = context.contentResolver
            val rawStream = contentResolver.openInputStream(uri)
                ?: return Result.failure(Exception("Tidak dapat membuka file yang dipilih"))

            val entriesMap = mutableMapOf<String, String>()

            ZipInputStream(rawStream.buffered()).use { zis ->
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val name = entry.name.substringAfterLast("/")
                    if (!entry.isDirectory && name.isNotEmpty()) {
                        val baos = ByteArrayOutputStream()
                        val buffer = ByteArray(2048)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            baos.write(buffer, 0, len)
                        }
                        entriesMap[name] = baos.toString(Charsets.UTF_8.name())
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
            }

            val manifestContent = entriesMap[MANIFEST_FILE]
            if (manifestContent == null && !entriesMap.containsKey(PENJUALAN_FILE)) {
                return Result.failure(Exception("File ZIP tidak memiliki berkas cadangan data CrispFlow yang valid."))
            }

            val manifest = if (manifestContent != null) {
                parseManifest(manifestContent)
            } else {
                BackupManifest(
                    version = 1,
                    appName = "CrispFlow",
                    backupDate = "Tanpa Riwayat Tanggal",
                    timestamp = System.currentTimeMillis(),
                    totalRecords = 0,
                    counts = emptyMap()
                )
            }

            val barangList = entriesMap[BARANG_FILE]?.let { parseBarang(it) } ?: emptyList()
            val mutasiList = entriesMap[MUTASI_FILE]?.let { parseMutasi(it) } ?: emptyList()
            val penjualanList = entriesMap[PENJUALAN_FILE]?.let { parsePenjualan(it) } ?: emptyList()
            val transferList = entriesMap[TRANSFER_FILE]?.let { parseTransfer(it) } ?: emptyList()
            val setoranList = entriesMap[SETORAN_FILE]?.let { parseSetoran(it) } ?: emptyList()
            val pinjamanList = entriesMap[PINJAMAN_FILE]?.let { parsePinjaman(it) } ?: emptyList()
            val kasList = entriesMap[KAS_FILE]?.let { parseKas(it) } ?: emptyList()

            val updatedCounts = mapOf(
                "barang" to barangList.size,
                "stok_mutasi" to mutasiList.size,
                "penjualan_harian" to penjualanList.size,
                "transfer_pedagang" to transferList.size,
                "setoran" to setoranList.size,
                "pinjaman_setoran" to pinjamanList.size,
                "kas_transaksi" to kasList.size
            )
            val updatedManifest = manifest.copy(
                totalRecords = updatedCounts.values.sum(),
                counts = updatedCounts
            )

            Result.success(
                BackupDataPackage(
                    manifest = updatedManifest,
                    barangList = barangList,
                    mutasiList = mutasiList,
                    penjualanList = penjualanList,
                    transferList = transferList,
                    setoranList = setoranList,
                    pinjamanList = pinjamanList,
                    kasList = kasList
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal membaca file ZIP: ${e.message}"))
        }
    }

    // === SERIALIZERS ===

    private fun serializeManifest(manifest: BackupManifest): String {
        val root = JSONObject()
        root.put("version", manifest.version)
        root.put("appName", manifest.appName)
        root.put("backupDate", manifest.backupDate)
        root.put("timestamp", manifest.timestamp)
        root.put("totalRecords", manifest.totalRecords)
        val countsJson = JSONObject()
        manifest.counts.forEach { (k, v) -> countsJson.put(k, v) }
        root.put("counts", countsJson)
        return root.toString(2)
    }

    private fun parseManifest(jsonStr: String): BackupManifest {
        val root = JSONObject(jsonStr)
        val countsMap = mutableMapOf<String, Int>()
        if (root.has("counts")) {
            val countsJson = root.getJSONObject("counts")
            val keys = countsJson.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                countsMap[key] = countsJson.getInt(key)
            }
        }
        return BackupManifest(
            version = root.optInt("version", 1),
            appName = root.optString("appName", "CrispFlow"),
            backupDate = root.optString("backupDate", "Tidak diketahui"),
            timestamp = root.optLong("timestamp", System.currentTimeMillis()),
            totalRecords = root.optInt("totalRecords", countsMap.values.sum()),
            counts = countsMap
        )
    }

    private fun serializeBarang(list: List<Barang>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("nama", item.nama)
            obj.put("hpp", item.hpp)
            obj.put("harga_jual_default", item.harga_jual_default)
            obj.put("nabung_wajib", item.nabung_wajib)
            obj.put("tarif_setor", item.tarif_setor)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parseBarang(jsonStr: String): List<Barang> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<Barang>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Barang(
                    id = obj.optLong("id", 0),
                    nama = obj.optString("nama", "Crisp Snack Pack"),
                    hpp = obj.optInt("hpp", 11000),
                    harga_jual_default = obj.optInt("harga_jual_default", 15000),
                    nabung_wajib = obj.optInt("nabung_wajib", 500),
                    tarif_setor = obj.optInt("tarif_setor", 11000),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializeMutasi(list: List<StokMutasi>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("lokasi", item.lokasi)
            obj.put("tipe", item.tipe)
            obj.put("jumlah", item.jumlah)
            if (item.ref_id != null) obj.put("ref_id", item.ref_id)
            obj.put("keterangan", item.keterangan)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parseMutasi(jsonStr: String): List<StokMutasi> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<StokMutasi>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                StokMutasi(
                    id = obj.optLong("id", 0),
                    tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                    lokasi = obj.optString("lokasi", "tangan"),
                    tipe = obj.optString("tipe", "laku"),
                    jumlah = obj.optInt("jumlah", 0),
                    ref_id = if (obj.has("ref_id") && !obj.isNull("ref_id")) obj.getLong("ref_id") else null,
                    keterangan = obj.optString("keterangan", ""),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializePenjualan(list: List<PenjualanHarian>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("jumlah_laku", item.jumlah_laku)
            obj.put("harga_jual", item.harga_jual)
            obj.put("pendapatan", item.pendapatan)
            obj.put("nabung_wajib", item.nabung_wajib)
            obj.put("kewajiban_setor", item.kewajiban_setor)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parsePenjualan(jsonStr: String): List<PenjualanHarian> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<PenjualanHarian>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PenjualanHarian(
                    id = obj.optLong("id", 0),
                    tanggal = obj.getString("tanggal"),
                    jumlah_laku = obj.optInt("jumlah_laku", 0),
                    harga_jual = obj.optInt("harga_jual", 15000),
                    pendapatan = obj.optInt("pendapatan", 0),
                    nabung_wajib = obj.optInt("nabung_wajib", 0),
                    kewajiban_setor = obj.optInt("kewajiban_setor", 0),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializeTransfer(list: List<TransferPedagang>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("arah", item.arah)
            obj.put("nama_pedagang", item.nama_pedagang)
            obj.put("jumlah", item.jumlah)
            obj.put("keterangan", item.keterangan)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parseTransfer(jsonStr: String): List<TransferPedagang> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<TransferPedagang>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                TransferPedagang(
                    id = obj.optLong("id", 0),
                    tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                    arah = obj.optString("arah", "keluar"),
                    nama_pedagang = obj.optString("nama_pedagang", ""),
                    jumlah = obj.optInt("jumlah", 0),
                    keterangan = obj.optString("keterangan", ""),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializeSetoran(list: List<Setoran>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("jumlah", item.jumlah)
            obj.put("keterangan", item.keterangan)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parseSetoran(jsonStr: String): List<Setoran> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<Setoran>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                Setoran(
                    id = obj.optLong("id", 0),
                    tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                    jumlah = obj.optInt("jumlah", 0),
                    keterangan = obj.optString("keterangan", ""),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializePinjaman(list: List<PinjamanSetoran>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("tipe", item.tipe)
            obj.put("pihak", item.pihak)
            obj.put("jumlah", item.jumlah)
            obj.put("status", item.status)
            if (item.tanggal_kembali != null) obj.put("tanggal_kembali", item.tanggal_kembali)
            if (item.parent_id != null) obj.put("parent_id", item.parent_id)
            obj.put("keterangan", item.keterangan)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parsePinjaman(jsonStr: String): List<PinjamanSetoran> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<PinjamanSetoran>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                PinjamanSetoran(
                    id = obj.optLong("id", 0),
                    tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                    tipe = obj.optString("tipe", "keluar"),
                    pihak = obj.optString("pihak", "Sendiri"),
                    jumlah = obj.optInt("jumlah", 0),
                    status = obj.optString("status", "belum_lunas"),
                    tanggal_kembali = if (obj.has("tanggal_kembali") && !obj.isNull("tanggal_kembali")) obj.getLong("tanggal_kembali") else null,
                    parent_id = if (obj.has("parent_id") && !obj.isNull("parent_id")) obj.getLong("parent_id") else null,
                    keterangan = obj.optString("keterangan", ""),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }

    private fun serializeKas(list: List<KasTransaksi>): String {
        val array = JSONArray()
        list.forEach { item ->
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("tanggal", item.tanggal)
            obj.put("tipe", item.tipe)
            obj.put("kategori", item.kategori)
            obj.put("jumlah", item.jumlah)
            obj.put("keterangan", item.keterangan)
            obj.put("created_at", item.created_at)
            array.put(obj)
        }
        return array.toString(2)
    }

    private fun parseKas(jsonStr: String): List<KasTransaksi> {
        val array = JSONArray(jsonStr)
        val list = mutableListOf<KasTransaksi>()
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            list.add(
                KasTransaksi(
                    id = obj.optLong("id", 0),
                    tanggal = obj.optLong("tanggal", System.currentTimeMillis()),
                    tipe = obj.optString("tipe", "masuk"),
                    kategori = obj.optString("kategori", "operasional"),
                    jumlah = obj.optInt("jumlah", 0),
                    keterangan = obj.optString("keterangan", ""),
                    created_at = obj.optLong("created_at", System.currentTimeMillis())
                )
            )
        }
        return list
    }
}
