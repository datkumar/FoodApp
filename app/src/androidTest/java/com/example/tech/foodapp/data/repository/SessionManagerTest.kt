package com.example.tech.foodapp.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.tech.foodapp.data.sample_data.SampleUserData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private lateinit var testDataStore: DataStore<Preferences>
    private lateinit var sessionManager: SessionManager

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        testDataStore = PreferenceDataStoreFactory.create(
            scope = CoroutineScope(
                Dispatchers.IO + Job()
            ),
            produceFile = {
                tempFolder.newFile("test_prefs.preferences_pb")
            }
        )
        // You might need a way to inject this testDataStore into SessionManager
        // Consider passing it via constructor for better testability
        sessionManager = SessionManager(context)
    }

    @Test
    fun saveAndReadUserData() = runTest {
        val user = SampleUserData
        sessionManager.saveUserInfo(user)

        val savedUser = sessionManager.userData.first()
        assertEquals("abc", savedUser?.token)
    }
}