package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ChatDao
import com.example.data.dao.PersonaDao
import com.example.data.model.ChatMessageEntity
import com.example.data.model.ChatSessionEntity
import com.example.data.model.PersonaEntity

@Database(
    entities = [
        PersonaEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun personaDao(): PersonaDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "nexus_ai_chat.db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
