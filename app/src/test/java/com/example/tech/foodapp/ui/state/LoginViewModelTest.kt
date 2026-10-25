package com.example.tech.foodapp.ui.state

import app.cash.turbine.test
import com.example.tech.foodapp.MainDispatcherRule
import com.example.tech.foodapp.data.repository.AuthRepository
import com.example.tech.foodapp.sample_data.SampleUserData
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `When Login successful, state transitions to Loading then Success`() = runTest {
        val mockUser = SampleUserData

        // Arrange
        coEvery {
            authRepository.loginUser(email = any(), pswd = any())
        } returns Result.success(mockUser)

        // Act & Assert
        viewModel.uiState.test {
            // 1. Initial State
            assertEquals(LoginUiState.Idle, awaitItem())

            // 2. Trigger Login
            viewModel.handleLogin(email = mockUser.email, pswd = "Password123!")

            // 3. Advance to the next state (Loading)
            // We use advanceUntilIdle() to let the coroutine run up to the first suspend point
            advanceUntilIdle()

            assertEquals(LoginUiState.Loading, awaitItem())

            // 4. Verify Success
            val success = awaitItem()
            assertTrue(success is LoginUiState.Success)
        }
    }
}