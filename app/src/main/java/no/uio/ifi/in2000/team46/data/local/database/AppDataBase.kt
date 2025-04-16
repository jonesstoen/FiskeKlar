package no.uio.ifi.in2000.team46.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import no.uio.ifi.in2000.team46.data.local.database.dao.FishingLogDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import android.content.Context
import no.uio.ifi.in2000.team46.data.local.database.dao.UserDao
import no.uio.ifi.in2000.team46.data.local.database.entities.User

@Database(entities = [FishingLog::class, User::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fishingLogDao(): FishingLogDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
