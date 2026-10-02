package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BookEntity::class,
        NoteEntity::class,
        FriendEntity::class,
        EventEntity::class,
        UserProfileEntity::class,
        NeighborhoodCheckoutEntity::class,
        NeighborhoodMessageEntity::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun noteDao(): NoteDao
    abstract fun friendDao(): FriendDao
    abstract fun eventDao(): EventDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun neighborhoodCheckoutDao(): NeighborhoodCheckoutDao
    abstract fun neighborhoodMessageDao(): NeighborhoodMessageDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_library_database"
                )
                .fallbackToDestructiveMigration(dropAllTables = true)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
