package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "setoran")
data class Setoran(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tanggal: Long = System.currentTimeMillis(),
    val jumlah: Int,
    val keterangan: String = "",
    val created_at: Long = System.currentTimeMillis()
)
