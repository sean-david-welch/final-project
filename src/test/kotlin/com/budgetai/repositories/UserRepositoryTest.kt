package com.budgetai.repositories

import com.budgetai.models.UserDTO
import com.budgetai.models.UserRole
import com.budgetai.models.Users
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

class UserRepositoryTest {
    private lateinit var database: Database
    private lateinit var repository: UserRepository
    private val dbFile = File("test.db")

    @Before
    fun setUp() {
        // Setup SQLite database for testing
        database = Database.connect(
            url = "jdbc:sqlite:${dbFile.absolutePath}", driver = "org.sqlite.JDBC"
        )

        // Create tables
        transaction(database) {
            SchemaUtils.create(Users)
        }

        repository = UserRepository(database)
    }

    @After
    fun tearDown() {
        // Clean up tables after each test
        transaction(database) {
            SchemaUtils.drop(Users)
        }
        // Delete the test database file
        dbFile.delete()
    }

    private fun createSampleUser(
        email: String = "test@example.com", name: String = "Test User", role: UserRole = UserRole.USER
    ): UserDTO {
        return UserDTO(
            id = 0, email = email, name = name, role = role.toString()
        )
    }

    @Test
    fun `test create and find user by id`() = runBlocking {
        // Given
        val user = createSampleUser()

        // When
        val createdId = repository.create(user)
        val retrievedUser = repository.findById(createdId)

        // Then
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.name, retrievedUser.name)
    }

    @Test
    fun `test find user by email`() = runBlocking {
        // Given
        val user = createSampleUser()

        // When
        repository.create(user)
        val retrievedUser = repository.findByEmail(user.email)

        // Then
        assertNotNull(retrievedUser)
        assertEquals(user.email, retrievedUser.email)
        assertEquals(user.name, retrievedUser.name)
    }

    @Test
    fun `test create user with duplicate email throws exception`(): Unit = runBlocking {
        // Given
        val user1 = createSampleUser()
        val user2 = createSampleUser(name = "Another User") // Same email

        // When
        repository.create(user1)

        // Then
        assertFailsWith<Exception>("Should throw exception for duplicate email") {
            repository.create(user2)
        }
    }

    @Test
    fun `test update user information`() = runBlocking {
        // Given
        val initialUser = createSampleUser()
        val createdId = repository.create(initialUser)

        // When
        val updatedUser = initialUser.copy(
            id = createdId, name = "Updated Name", email = "updated@example.com"
        )
        repository.update(createdId, updatedUser)
        val retrievedUser = repository.findById(createdId)

        // Then
        assertNotNull(retrievedUser)
        assertEquals("Updated Name", retrievedUser.name)
        assertEquals("updated@example.com", retrievedUser.email)
    }

    @Test
    fun `test update user with existing email throws exception`(): Unit = runBlocking {
        // Given
        val user1 = createSampleUser(email = "user1@example.com")
        val user2 = createSampleUser(email = "user2@example.com")

        // When
        repository.create(user1)
        val id2 = repository.create(user2)

        // Then
        assertFailsWith<Exception>("Should throw exception when updating to existing email") {
            repository.update(id2, user2.copy(email = "user1@example.com"))
        }
    }

    @Test
    fun `test password management`() = runBlocking {
        // Given
        val user = createSampleUser()
        val hashedPassword = "hashed_password_123"

        // When
        val createdId = repository.create(user)
        repository.updatePassword(createdId, hashedPassword)
        val retrievedHash = repository.findPasswordHash(createdId)

        // Then
        assertEquals(hashedPassword, retrievedHash)
    }

    @Test
    fun `test delete user`() = runBlocking {
        // Given
        val user = createSampleUser()

        // When
        val createdId = repository.create(user)
        repository.delete(createdId)
        val retrievedUser = repository.findById(createdId)

        // Then
        assertNull(retrievedUser)
    }

    @Test
    fun `test find non-existent user returns null`() = runBlocking {
        // When
        val nonExistentUser = repository.findById(999)

        // Then
        assertNull(nonExistentUser)
    }

    @Test
    fun `test update password for non-existent user`() = runBlocking {
        // Given
        val nonExistentId = 999
        val hashedPassword = "new_password_hash"

        // When
        repository.updatePassword(nonExistentId, hashedPassword)
        val retrievedHash = repository.findPasswordHash(nonExistentId)

        // Then
        assertNull(retrievedHash)
    }

    @Test
    fun `test update user preserves existing email`() = runBlocking {
        // Given
        val user = createSampleUser()
        val createdId = repository.create(user)

        // When
        val updatedUser = user.copy(id = createdId, name = "New Name") // Same email
        repository.update(createdId, updatedUser)
        val retrievedUser = repository.findById(createdId)

        // Then
        assertNotNull(retrievedUser)
        assertEquals("New Name", retrievedUser.name)
        assertEquals(user.email, retrievedUser.email)
    }
}