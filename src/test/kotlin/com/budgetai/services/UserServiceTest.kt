package com.budgetai.services

import com.budgetai.models.UserDTO
import com.budgetai.repositories.UserRepository
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class UserServiceTest {
    private lateinit var database: Database
    private lateinit var repository: UserRepository
    private lateinit var service: UserService
    private val dbFile = File("test.db")

    private val validEmail = "test@example.com"
    private val validName = "Test User"
    private val validPassword = "Password123!" // Meets security requirements

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}",
            driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(Users)
        }

        repository = UserRepository(database)
        service = UserService(repository)
    }

    @After
    fun tearDown() {
        transaction(database) {
            SchemaUtils.drop(Users)
        }
        dbFile.delete()
    }

    @Test
    fun `createUser should create user with valid data`() = runBlocking {
        // Given
        val request = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )

        // When
        val userId = service.createUser(request)
        val createdUser = service.getUser(userId)

        // Then
        assertNotNull(createdUser)
        assertEquals(request.email, createdUser.email)
        assertEquals(request.name, createdUser.name)
    }

    @Test
    fun `createUser should throw exception for invalid email format`() = runBlocking {
        // Given
        val request = UserService.UserCreationRequest(
            email = "invalid-email",
            name = validName,
            password = validPassword
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createUser(request)
        }
    }

    @Test
    fun `createUser should throw exception for weak password`() = runBlocking {
        // Given
        val request = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = "weak"
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createUser(request)
        }
    }

    @Test
    fun `createUser should throw exception for duplicate email`() = runBlocking {
        // Given
        val request = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        service.createUser(request)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.createUser(request)
        }
    }

    @Test
    fun `authenticateUser should succeed with correct credentials`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        service.createUser(createRequest)

        val authRequest = UserService.UserAuthenticationRequest(
            email = validEmail,
            password = validPassword
        )

        // When
        val authenticatedUser = service.authenticateUser(authRequest)

        // Then
        assertNotNull(authenticatedUser)
        assertEquals(validEmail, authenticatedUser.email)
    }

    @Test
    fun `authenticateUser should return null for incorrect password`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        service.createUser(createRequest)

        val authRequest = UserService.UserAuthenticationRequest(
            email = validEmail,
            password = "wrongpassword123!"
        )

        // When
        val result = service.authenticateUser(authRequest)

        // Then
        assertNull(result)
    }

    @Test
    fun `updateUser should update user information`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)

        val updatedUser = UserDTO(
            id = userId,
            email = "new@example.com",
            name = "Updated Name"
        )

        // When
        service.updateUser(userId, updatedUser)
        val result = service.getUser(userId)

        // Then
        assertEquals(updatedUser.email, result?.email)
        assertEquals(updatedUser.name, result?.name)
    }

    @Test
    fun `updateUser should throw exception for invalid email format`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)

        val updatedUser = UserDTO(
            id = userId,
            email = "invalid-email",
            name = "Updated Name"
        )

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.updateUser(userId, updatedUser)
        }
    }

    @Test
    fun `updatePassword should succeed with correct current password`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)
        val newPassword = "NewPassword123!"

        // When
        service.updatePassword(userId, validPassword, newPassword)

        // Then
        val authRequest = UserService.UserAuthenticationRequest(
            email = validEmail,
            password = newPassword
        )
        val authenticatedUser = service.authenticateUser(authRequest)
        assertNotNull(authenticatedUser)
    }

    @Test
    fun `updatePassword should throw exception for incorrect current password`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.updatePassword(userId, "wrongpassword", "NewPassword123!")
        }
    }

    @Test
    fun `updatePassword should throw exception for weak new password`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)

        // When/Then
        assertFailsWith<IllegalArgumentException> {
            service.updatePassword(userId, validPassword, "weak")
        }
    }

    @Test
    fun `deleteUser should remove user`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        val userId = service.createUser(createRequest)

        // When
        service.deleteUser(userId)
        val deletedUser = service.getUser(userId)

        // Then
        assertNull(deletedUser)
    }

    @Test
    fun `getUserByEmail should return correct user`() = runBlocking {
        // Given
        val createRequest = UserService.UserCreationRequest(
            email = validEmail,
            name = validName,
            password = validPassword
        )
        service.createUser(createRequest)

        // When
        val user = service.getUserByEmail(validEmail)

        // Then
        assertNotNull(user)
        assertEquals(validEmail, user.email)
        assertEquals(validName, user.name)
    }
}