package com.ifs21004.lostandfound.data.local.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ifs21004.lostandfound.data.local.entity.DelcomLostandFoundEntity

@Database(entities = [DelcomLostandFoundEntity::class], version = 1, exportSchema = false)
abstract class DelcomLostandFoundDatabase : RoomDatabase() {
    abstract fun delcomLostandFoundDao(): IDelcomLostandFoundDao

    companion object {
        private const val Database_NAME = "DelcomLostandFound.db"
        @Volatile
        private var INSTANCE: DelcomLostandFoundDatabase? = null

        @JvmStatic
        fun getInstance(context: Context): DelcomLostandFoundDatabase {
            if (INSTANCE == null) {
                synchronized(DelcomLostandFoundDatabase::class.java) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        DelcomLostandFoundDatabase::class.java,
                        Database_NAME
                    ).build()
                }
            }
            return INSTANCE as DelcomLostandFoundDatabase
        }
    }
}
