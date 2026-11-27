package com.example.hostelmanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        Hostel::class, RoomEntity::class, Person::class,
        Stay::class, Payment::class, Expense::class, PaymentEdit::class
    ],
    version = 14,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hostelDao(): HostelDao
    abstract fun roomDao(): RoomDao
    abstract fun personDao(): PersonDao
    abstract fun stayDao(): StayDao
    abstract fun paymentDao(): PaymentDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun paymentEditDao(): PaymentEditDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stays ADD COLUMN monthlyRent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("UPDATE stays SET monthlyRent = COALESCE((SELECT defaultRentPerBed FROM rooms WHERE rooms.id = stays.roomId), 0)")
            }
        }
        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE persons ADD COLUMN govtIdBackPath TEXT NOT NULL DEFAULT ''")
            }
        }
        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stays ADD COLUMN billingAnchorDay INTEGER NOT NULL DEFAULT 1")
                db.execSQL("""UPDATE stays SET billingAnchorDay = CAST(SUBSTR(joinDate, 9, 2) AS INTEGER) WHERE joinDate IS NOT NULL AND joinDate != '' AND LENGTH(joinDate) >= 10""")
            }
        }
        private val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS payments (id TEXT NOT NULL PRIMARY KEY, stayId TEXT NOT NULL, personId TEXT NOT NULL, hostelId TEXT NOT NULL, amountPaise INTEGER NOT NULL, mode TEXT NOT NULL, date TEXT NOT NULL, note TEXT NOT NULL DEFAULT '', createdAt INTEGER NOT NULL, FOREIGN KEY(stayId) REFERENCES stays(id) ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_stayId ON payments(stayId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_date ON payments(date)")
            }
        }
        private val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN paymentDate TEXT NOT NULL DEFAULT ''")
                db.execSQL("UPDATE payments SET paymentDate = date")
            }
        }
        private val MIGRATION_9_10 = object : Migration(9, 10) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("""CREATE TABLE IF NOT EXISTS expenses (id TEXT NOT NULL PRIMARY KEY, hostelId TEXT NOT NULL, expenseDate TEXT NOT NULL, enteredDate TEXT NOT NULL, category TEXT NOT NULL, amountPaise INTEGER NOT NULL, note TEXT NOT NULL DEFAULT '', receiptImagePath TEXT, createdAt INTEGER NOT NULL, FOREIGN KEY(hostelId) REFERENCES hostels(id) ON DELETE CASCADE)""")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_hostelId ON expenses(hostelId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_expenses_expenseDate ON expenses(expenseDate)")
            }
        }
        private val MIGRATION_10_11 = object : Migration(10, 11) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_personId ON payments(personId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_hostelId ON payments(hostelId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_payments_paymentDate ON payments(paymentDate)")
            }
        }
        private val MIGRATION_11_12 = object : Migration(11, 12) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE stays ADD COLUMN dailyRent INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE stays ADD COLUMN noticeDate TEXT")
            }
        }
        private val MIGRATION_12_13 = object : Migration(12, 13) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN waivedPaise INTEGER NOT NULL DEFAULT 0")
            }
        }
        // ✅ NEW: edit markers + append-only audit table
        private val MIGRATION_13_14 = object : Migration(13, 14) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE payments ADD COLUMN editCount INTEGER NOT NULL DEFAULT 0")
                db.execSQL("ALTER TABLE payments ADD COLUMN lastEditedAt INTEGER")
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `payment_edits` (
                        `id` TEXT NOT NULL,
                        `paymentId` TEXT NOT NULL,
                        `editedAt` INTEGER NOT NULL,
                        `reason` TEXT NOT NULL,
                        `amountBefore` INTEGER NOT NULL,
                        `amountAfter` INTEGER NOT NULL,
                        `waivedBefore` INTEGER NOT NULL,
                        `waivedAfter` INTEGER NOT NULL,
                        `modeBefore` TEXT NOT NULL,
                        `modeAfter` TEXT NOT NULL,
                        `paymentDateBefore` TEXT NOT NULL,
                        `paymentDateAfter` TEXT NOT NULL,
                        `recordedDateBefore` TEXT NOT NULL,
                        `recordedDateAfter` TEXT NOT NULL,
                        `noteBefore` TEXT NOT NULL,
                        `noteAfter` TEXT NOT NULL,
                        PRIMARY KEY(`id`),
                        FOREIGN KEY(`paymentId`) REFERENCES `payments`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_payment_edits_paymentId` ON `payment_edits` (`paymentId`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hostel_manager_db"
                )
                    .addMigrations(
                        MIGRATION_4_5, MIGRATION_5_6, MIGRATION_6_7,
                        MIGRATION_7_8, MIGRATION_8_9, MIGRATION_9_10,
                        MIGRATION_10_11, MIGRATION_11_12, MIGRATION_12_13,
                        MIGRATION_13_14
                    )
                    .fallbackToDestructiveMigrationFrom(1, 2, 3)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}