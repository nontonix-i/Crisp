package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.KerupukDao
import com.example.data.model.Barang
import com.example.data.model.KasTransaksi
import com.example.data.model.PenjualanHarian
import com.example.data.model.PinjamanSetoran
import com.example.data.model.Setoran
import com.example.data.model.StokMutasi
import com.example.data.model.TransferPedagang

@Database(
    entities = [
        Barang::class,
        StokMutasi::class,
        PenjualanHarian::class,
        TransferPedagang::class,
        Setoran::class,
        PinjamanSetoran::class,
        KasTransaksi::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun kerupukDao(): KerupukDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kerupukku_database.db"
                ).fallbackToDestructiveMigration()
                 .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
