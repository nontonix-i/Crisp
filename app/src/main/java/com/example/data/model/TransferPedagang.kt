package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transfer_pedagang")
data class TransferPedagang(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val arah: String, // "keluar" | "masuk"
    val nama_pedagang: String,
    val jumlah: Int,
    val keterangan: String = "",
    val created_at: Long = System.currentTimeMillis()
)
