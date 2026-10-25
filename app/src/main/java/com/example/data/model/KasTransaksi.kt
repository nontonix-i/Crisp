package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "kas_transaksi",
    indices = [
        Index("tipe"),
        Index("tanggal")
    ]
)
data class KasTransaksi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val tipe: String, // "masuk" | "keluar"
    val kategori: String, // "setoran_warung" | "setor_pabrik" | "nabung_wajib" | "operasional" | "pinjaman_keluar" | "pinjaman_kembali" | "lain"
    val jumlah: Int,
    val keterangan: String = "",
    val created_at: Long = System.currentTimeMillis()
)
