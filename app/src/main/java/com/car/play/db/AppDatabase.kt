package com.car.play.android.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [CarMaintenanceEntity::class, FuelEntryEntity::class, ReminderEntity::class],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carMaintenanceDao(): CarMaintenanceDao
    abstract fun fuelDao(): FuelDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add the new column `date` as a String
                database.execSQL("ALTER TABLE car_maintenance ADD COLUMN date TEXT DEFAULT ''")
            }
        }
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `fuel_entry` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`dateMillis` INTEGER NOT NULL, " +
                        "`odometer` REAL NOT NULL, " +
                        "`liters` REAL NOT NULL, " +
                        "`totalCost` REAL NOT NULL, " +
                        "`fullTank` INTEGER NOT NULL)"
                )
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS `car_reminder` (" +
                        "`id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                        "`title` TEXT NOT NULL, " +
                        "`type` TEXT NOT NULL, " +
                        "`dueDateMillis` INTEGER NOT NULL, " +
                        "`note` TEXT NOT NULL, " +
                        "`leadDays` INTEGER NOT NULL, " +
                        "`notified` INTEGER NOT NULL DEFAULT 0)"
                )
            }
        }
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "car_maintenance_db"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
