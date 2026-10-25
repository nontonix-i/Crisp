package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "stok_mutasi",
    indices = [
        Index("lokasi"),
        Index("tanggal")
    ]
)
data class StokMutasi(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val lokasi: String, // "pool" | "tangan" | "pedagang"
    val tipe: String, // "masuk_pabrik" | "keluar_tangan" | "laku" | "balik_pool" | "transfer_keluar" | "transfer_masuk"
    val jumlah: Int, // positif/negatif
    val ref_id: Long? = null,
    val keterangan: String = "",
    val created_at: Long = System.currentTimeMillis()
)
