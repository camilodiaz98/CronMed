package com.example.cronmed.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MedicamentoEntity::class, HistorialEntity::class, WaterLogEntity::class], version = 4, exportSchema = false)
abstract class MedicamentoDatabase : RoomDatabase() {
    abstract fun medicamentoDao(): MedicamentoDao

    companion object {
        @Volatile
        private var INSTANCE: MedicamentoDatabase? = null

        fun getDatabase(context: Context): MedicamentoDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MedicamentoDatabase::class.java,
                    "medicamento_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
