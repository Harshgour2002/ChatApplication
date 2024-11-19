package com.example.visionconnect.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [myChatsEntity::class], version = 1, exportSchema = true)
abstract class appDatabase : RoomDatabase() {

    abstract fun chatDao(): DAO

    companion object {
        @Volatile
        private var INSTANCE: appDatabase? = null

       /* private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new column with explicit default value if needed
                database.execSQL("ALTER TABLE my_ChatList_Table ADD COLUMN newColumn TEXT DEFAULT '' NOT NULL")
            }
        }

        */


        fun getDatabase(context: Context): appDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    appDatabase::class.java,
                    "VisionConnect_database" // More descriptive name
                )

                    .build()
                INSTANCE = instance
                instance
            }
        }}
}