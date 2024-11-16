package com.budgetai.repositories

import com.budgetai.models.UserDTO
import com.budgetai.models.Users
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(user: UserDTO): Int = dbQuery {
        Users.insert {
            it[email] = user.email
            it[name] = user.name
            it[passwordHash] = ""
        }[Users.id].value
    }

    suspend fun findById(id: Int): UserDTO? = dbQuery {
        Users.selectAll().where { Users.id eq id }
            .map(::toUser)
            .singleOrNull()
    }

    suspend fun findByEmail(email: String): UserDTO? = dbQuery {
        Users.selectAll().where { Users.email eq email }
            .map(::toUser)
            .singleOrNull()
    }

    suspend fun update(id: Int, user: UserDTO) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[email] = user.email
            it[name] = user.name
        }
    }

    suspend fun updatePassword(id: Int, hashedPassword: String) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[passwordHash] = hashedPassword
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Users.deleteWhere { Users.id eq id }
    }

    private fun toUser(row: ResultRow) = UserDTO(
        id = row[Users.id].value,
        email = row[Users.email],
        name = row[Users.name],
        createdAt = row[Users.createdAt].toLocalDateTime(TimeZone.UTC)

    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }
}