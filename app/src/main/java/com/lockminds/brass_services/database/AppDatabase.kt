package com.lockminds.brass_services.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lockminds.brass_services.database.daos.*
import com.lockminds.brass_services.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@Database(entities = [
    Lot::class,CheckPoint::class,
    CheckPointActions::class,CheckPointHistory::class,
    Accident::class,AccidentGallery::class, Office::class], version = 3, exportSchema = true)

 abstract class AppDatabase: RoomDatabase() {

    abstract fun checkPointDao(): CheckPointDao
    abstract fun checkPointHistoryDao(): CheckPointHistoryDao
    abstract fun lotDao(): LotDao
    abstract fun officeDao(): OfficesDao
    abstract fun accidentDao(): AccidentDao
    abstract fun accidentGalleryDao(): AccidentGalleryDao
    abstract fun checkPointActionDao(): CheckPointActionDao




    companion object{

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `offices` (`id` LONG, `team_id` TEXT,`name` TEXT, `latitude` TEXT, `longitude` TEXT, `created_at` TEXT, `deleted_at` TEXT, `updated_at` TEXT, `synced` TEXT," +
                        "PRIMARY KEY(`id`))")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE offices ADD COLUMN attendance TEXT")
            }
        }


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
                    .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
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
                .addMigrations(MIGRATION_1_2,MIGRATION_2_3)
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