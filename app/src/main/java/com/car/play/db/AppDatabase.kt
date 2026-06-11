package com.car.play.android.app.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        CarMaintenanceEntity::class,
        FuelEntity::class,
        ReminderEntity::class,
        TripEntity::class,
        ExpenseEntity::class,
        TirePressureEntity::class,
        CarProfileEntity::class,
        InsuranceEntity::class,
        MileageLogEntity::class,
        DrivingScoreEntity::class
    ],
    version = 3
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun carMaintenanceDao(): CarMaintenanceDao
    abstract fun fuelDao(): FuelDao
    abstract fun reminderDao(): ReminderDao
    abstract fun tripDao(): TripDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun tirePressureDao(): TirePressureDao
    abstract fun carProfileDao(): CarProfileDao
    abstract fun insuranceDao(): InsuranceDao
    abstract fun mileageLogDao(): MileageLogDao
    abstract fun drivingScoreDao(): DrivingScoreDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE car_maintenance ADD COLUMN date TEXT DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("""CREATE TABLE IF NOT EXISTS fuel_records (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    liters REAL NOT NULL,
                    costPerLiter REAL NOT NULL,
                    totalCost REAL NOT NULL,
                    odometer REAL NOT NULL,
                    fuelType TEXT NOT NULL,
                    station TEXT NOT NULL,
                    notes TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS reminders (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    description TEXT NOT NULL,
                    date TEXT NOT NULL,
                    time TEXT NOT NULL,
                    isRecurring INTEGER NOT NULL,
                    recurringInterval TEXT NOT NULL,
                    isCompleted INTEGER NOT NULL,
                    category TEXT NOT NULL,
                    priority TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS trips (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    startTime INTEGER NOT NULL,
                    endTime INTEGER NOT NULL,
                    distance REAL NOT NULL,
                    duration INTEGER NOT NULL,
                    averageSpeed REAL NOT NULL,
                    maxSpeed REAL NOT NULL,
                    startAddress TEXT NOT NULL,
                    endAddress TEXT NOT NULL,
                    routePoints TEXT NOT NULL,
                    date TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS expenses (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    title TEXT NOT NULL,
                    amount REAL NOT NULL,
                    category TEXT NOT NULL,
                    date TEXT NOT NULL,
                    notes TEXT NOT NULL,
                    receiptPath TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS tire_pressure (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    frontLeft REAL NOT NULL,
                    frontRight REAL NOT NULL,
                    rearLeft REAL NOT NULL,
                    rearRight REAL NOT NULL,
                    recommendedPressure REAL NOT NULL,
                    date TEXT NOT NULL,
                    notes TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS car_profiles (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    name TEXT NOT NULL,
                    make TEXT NOT NULL,
                    model TEXT NOT NULL,
                    year TEXT NOT NULL,
                    color TEXT NOT NULL,
                    licensePlate TEXT NOT NULL,
                    vin TEXT NOT NULL,
                    purchaseDate TEXT NOT NULL,
                    imagePath TEXT NOT NULL,
                    isActive INTEGER NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS insurance (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    policyNumber TEXT NOT NULL,
                    provider TEXT NOT NULL,
                    type TEXT NOT NULL,
                    premium REAL NOT NULL,
                    startDate TEXT NOT NULL,
                    endDate TEXT NOT NULL,
                    agentName TEXT NOT NULL,
                    agentPhone TEXT NOT NULL,
                    documentPath TEXT NOT NULL,
                    notes TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS mileage_logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    startOdometer REAL NOT NULL,
                    endOdometer REAL NOT NULL,
                    distance REAL NOT NULL,
                    purpose TEXT NOT NULL,
                    startLocation TEXT NOT NULL,
                    endLocation TEXT NOT NULL,
                    isBusinessTrip INTEGER NOT NULL,
                    notes TEXT NOT NULL
                )""")

                database.execSQL("""CREATE TABLE IF NOT EXISTS driving_scores (
                    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    date TEXT NOT NULL,
                    overallScore INTEGER NOT NULL,
                    accelerationScore INTEGER NOT NULL,
                    brakingScore INTEGER NOT NULL,
                    speedScore INTEGER NOT NULL,
                    corneringScore INTEGER NOT NULL,
                    distanceDriven REAL NOT NULL,
                    duration INTEGER NOT NULL,
                    hardBrakes INTEGER NOT NULL,
                    rapidAccelerations INTEGER NOT NULL
                )""")
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
