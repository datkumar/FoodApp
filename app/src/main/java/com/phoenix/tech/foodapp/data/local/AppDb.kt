package com.phoenix.tech.foodapp.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.phoenix.tech.foodapp.data.local.entities.CartItemEntity
import com.phoenix.tech.foodapp.data.local.entities.RestaurantEntity
import com.phoenix.tech.foodapp.utils.AppConstants

@Database(
    entities = [RestaurantEntity::class, CartItemEntity::class],
    version = 1,
    exportSchema = false
)

abstract class AppDb : RoomDatabase() {
    abstract fun cartDao(): CartDao

    companion object {
        @Volatile
        private var INSTANCE: AppDb? = null

        fun getDatabase(context: Context): AppDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDb::class.java,
                    AppConstants.DB_NAME
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}