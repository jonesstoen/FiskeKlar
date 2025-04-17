package no.uio.ifi.in2000.team46.data.local.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import no.uio.ifi.in2000.team46.data.local.database.dao.FishingLogDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FishingLog
import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.team46.data.local.database.dao.FishTypeDao
import no.uio.ifi.in2000.team46.data.local.database.dao.UserDao
import no.uio.ifi.in2000.team46.data.local.database.entities.FishType
import no.uio.ifi.in2000.team46.data.local.database.entities.User


@Database(
    entities = [FishingLog::class, User::class, FishType::class],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun fishingLogDao(): FishingLogDao
    abstract fun userDao(): UserDao
    abstract fun fishTypeDao(): FishTypeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val inst = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            // Kjør alltid ved åpning av DB: pre‑populer hvis tom
                            INSTANCE?.let { database ->
                                CoroutineScope(Dispatchers.IO).launch {
                                    val dao = database.fishTypeDao()
                                    if (dao.getCount() == 0) {
                                        dao.insertAll(
                                            listOf(
                                                FishType(name = "Torsk"),
                                                FishType(name = "Sei"),
                                                FishType(name = "Hyse"),
                                                FishType(name = "Lyr"),
                                                FishType(name = "Kolje"),
                                                FishType(name = "Makrell"),
                                                FishType(name = "Lange"),
                                                FishType(name = "Kveite"),
                                                FishType(name = "Steinbit"),
                                                FishType(name = "Rødspette"),
                                                FishType(name = "Skrubbe"),
                                                FishType(name = "Pigghvar"),
                                                FishType(name = "Hornfisk"),
                                                FishType(name = "Sjøørret")
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    })
                    .build()
                INSTANCE = inst
                inst
            }
        }
    }
}
