package com.example.tech.foodapp.data.repository

import com.example.tech.foodapp.data.network.AuthApi
import com.example.tech.foodapp.sample_data.SampleLoginSuccessResponse
import com.example.tech.foodapp.sample_data.SampleUserData
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class AuthRepositoryTest {
    private val api: AuthApi = mockk()
    private val sessionManager: SessionManager = mockk(relaxed = true)
    private val cartRepository: CartRepository = mockk(relaxed = true)
    // relaxed=true ignores calls to internal methods like saveUserInfo

    private lateinit var repository: AuthRepository

    @Before
    fun setup() {
        repository = AuthRepository(
            api = api,
            sessionManager = sessionManager,
            cartRepository = cartRepository
        )
    }

    @Test
    fun `Login success saves user info and returns Success state`() = runTest {
        // Arrange (Stubbing)
        val mockResponse = SampleLoginSuccessResponse
        coEvery {
            api.login(any())
        } returns mockResponse

        // Act
        val result = repository.loginUser(
            email = SampleUserData.email,
            pswd = "Password123!"
        )

        // Assert
        // If it fails, this will show the actual error message inside the failure
        assertTrue(
            "Repository returned failure: ${result.exceptionOrNull()?.message}",
            result.isSuccess
        )
        coVerify { sessionManager.saveUserInfo(SampleUserData) }
    }
}