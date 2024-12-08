package com.budgetai.repositories

import com.budgetai.models.UserDTO
import com.budgetai.models.UserRole
import com.budgetai.models.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.Exception

private val ADMIN_EMAIL = System.getenv("ADMIN_EMAIL")
private val ADMIN_NAME = System.getenv("ADMIN_NAME")

class UserRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    // Helper Methods
    // Maps database row to UserDTO
    private fun toUser(row: ResultRow) = UserDTO(
        id = row[Users.id].value,
        name = row[Users.name],
        role = row[Users.role],
        email = row[Users.email],
    )

    private fun validateRole(role: String) {
        try {
            UserRole.valueOf(role.uppercase())
        } catch (e: IllegalArgumentException) {
            throw Exception("Invalid role: $role. Must be one of: ${UserRole.entries.joinToString()}")
        }
    }

    // Executes a database query within a coroutine context
    private suspend fun <T> dbQuery(block: suspend () -> T): T = newSuspendedTransaction(Dispatchers.IO, database) { block() }

    // Read Methods
    // Retrieves a user by their ID
    suspend fun findAll(): List<UserDTO> = dbQuery { Users.selectAll().map { toUser(it) } }

    suspend fun findById(id: Int): UserDTO? = dbQuery {
        Users.selectAll().where { Users.id eq id }.map(::toUser).singleOrNull()
    }

    // Retrieves a user by their email
    suspend fun findByEmail(email: String): UserDTO? = dbQuery {
        Users.selectAll().where { Users.email eq email }.map(::toUser).singleOrNull()
    }

    // Retrieves password hash for a user by ID
    suspend fun findPasswordHash(id: Int): String? = dbQuery {
        Users.select(Users.passwordHash).where { Users.id eq id }.map { it[Users.passwordHash] }.singleOrNull()
    }

    // Write Methods
    // Creates a new user and returns their ID
    suspend fun create(user: UserDTO): Int = dbQuery {
        // Validate role
        validateRole(user.role)

        if (user.name == ADMIN_NAME && user.email == ADMIN_EMAIL) {
            user.role = UserRole.ADMIN.toString()
        }

        // First check if email exists
        val existingUser = Users.selectAll().where { Users.email eq user.email }.firstOrNull()
        if (existingUser != null) {
            throw Exception("A user with email ${user.email} already exists")
        }

        Users.insert {
            it[email] = user.email
            it[name] = user.name
            it[role] = user.role.uppercase()
            it[passwordHash] = ""
        }[Users.id].value
    }

    // Updates user's basic information
    suspend fun update(id: Int, user: UserDTO) = dbQuery {
        // Validate role
        validateRole(user.role)

        // Check if new email already exists for a DIFFERENT user
        val existingUser = Users.selectAll().where { (Users.email eq user.email) and (Users.id neq id) }.firstOrNull()

        if (existingUser != null) {
            throw Exception("Cannot update: email ${user.email} is already in use by another user")
        }

        Users.update({ Users.id eq id }) {
            it[email] = user.email
            it[name] = user.name
            it[role] = user.role.uppercase()
        }
    }

    // Updates user's password hash
    suspend fun updatePassword(id: Int, hashedPassword: String) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[passwordHash] = hashedPassword
        }
    }

    // Deletes a user by their ID
    suspend fun delete(id: Int) = dbQuery {
        Users.deleteWhere { Users.id eq id }
    }
}