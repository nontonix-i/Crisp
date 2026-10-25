package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "barang")
data class Barang(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val nama: String = "Crisp Snack Pack",
    val hpp: Int = 11000,
    val harga_jual_default: Int = 15000,
    val nabung_wajib: Int = 500,
    val tarif_setor: Int = 11000,
    val created_at: Long = System.currentTimeMillis()
)
