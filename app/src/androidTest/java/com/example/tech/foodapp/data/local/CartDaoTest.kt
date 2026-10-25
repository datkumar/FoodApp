package com.example.tech.foodapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tech.foodapp.data.local.entities.CartItemEntity
import com.example.tech.foodapp.data.local.entities.RestaurantEntity
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CartDaoTest {
    private lateinit var db: AppDb
    private lateinit var dao: CartDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        // Using inMemoryDatabaseBuilder for isolation
        db = Room.inMemoryDatabaseBuilder(context, AppDb::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.cartDao()
    }

    @After
    fun tearDown() = db.close()

    @Test
    fun insertAndRetrieveCartItem() = runTest {
        val restaurant = RestaurantEntity("1", "Spice Court", "img_url", "Pune")
        val item = CartItemEntity("101", "1", "Paneer Tikka", 250, 1, 1)

        dao.insertRestaurant(restaurant)
        dao.insertOrUpdateItem(item)

        val items = dao.getAllCartItems().first()
        assertEquals(1, items.size)
        assertEquals("Paneer Tikka", items[0].name)
    }

    @Test
    fun cascadeDelete_whenRestaurantRemoved_itemsRemoved() = runTest {
        val restaurant = RestaurantEntity("1", "Spice Court", "img_url", "Pune")
        val item = CartItemEntity("101", "1", "Paneer Tikka", 250, 1, 1)

        dao.insertRestaurant(restaurant)
        dao.insertOrUpdateItem(item)

        dao.removeRestaurant("1") // Should trigger CASCADE

        val items = dao.getAllCartItems().first()
        assertTrue(items.isEmpty())
    }
}