package com.example.data.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "penjualan_harian",
    indices = [Index(value = ["tanggal"], unique = true)]
)
data class PenjualanHarian(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: String, // format YYYY-MM-DD (unik per hari)
    val jumlah_laku: Int,
    val harga_jual: Int,
    val pendapatan: Int, // laku * harga_jual
    val nabung_wajib: Int, // laku * 500
    val kewajiban_setor: Int, // laku * 11000
    val created_at: Long = System.currentTimeMillis()
)
