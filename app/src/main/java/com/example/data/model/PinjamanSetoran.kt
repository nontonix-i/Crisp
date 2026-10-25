package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pinjaman_setoran",
    indices = [
        Index("tipe"),
        Index("parent_id")
    ]
)
data class PinjamanSetoran(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val tipe: String, // "keluar" | "kembali"
    val pihak: String, // nama pedagang atau "Sendiri"
    val jumlah: Int,
    val status: String = "belum_lunas", // "belum_lunas" | "lunas"
    val tanggal_kembali: Long? = null,
    val parent_id: Long? = null, // ID pinjaman asal jika ini pengembalian
    val keterangan: String = "",
    val created_at: Long = System.currentTimeMillis()
)
