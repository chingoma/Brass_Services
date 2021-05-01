package com.lockminds.brass_services.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lockminds.brass_services.database.daos.*
import com.lockminds.brass_services.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [
    Lot::class,CheckPoint::class,
    CheckPointActions::class,CheckPointHistory::class,
    Accident::class,AccidentGallery::class], version = 1, exportSchema = false)
 abstract class AppDatabase: RoomDatabase() {

    abstract fun checkPointDao(): CheckPointDao
    abstract fun checkPointHistoryDao(): CheckPointHistoryDao
    abstract fun lotDao(): LotDao
    abstract fun accidentDao(): AccidentDao
    abstract fun accidentGalleryDao(): AccidentGalleryDao
    abstract fun checkPointActionDao(): CheckPointActionDao

    companion object{

        // Singleton prevents multiple instances of database opening at the same time.
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope) :AppDatabase{
            // if the INSTANCE is not null, then return it,
            // if it is, then create the database
            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "com.lockminds.brass_services_database"
                ).addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                // return instance
                instance
            }
        }

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE
                    ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "com.lockminds.brass_services_database")
                .fallbackToDestructiveMigration()
                .build()
    }

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch { }
            }
        }

    }


}