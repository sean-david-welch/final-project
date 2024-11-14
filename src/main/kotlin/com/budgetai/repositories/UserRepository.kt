package com.budgetai.repositories

import com.budgetai.models.User
import com.budgetai.models.Users
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class UserRepository(private val database: Database) {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    suspend fun create(user: User): Int = dbQuery {
        Users.insert {
            it[name] = user.name
            it[age] = user.age
        }[Users.id]
    }

    suspend fun findById(id: Int): User? = dbQuery {
        Users.selectAll()
            .where { Users.id eq id }
            .map { toUser(it) }
            .singleOrNull()
    }

    suspend fun update(id: Int, user: User) = dbQuery {
        Users.update({ Users.id eq id }) {
            it[name] = user.name
            it[age] = user.age
        }
    }

    suspend fun delete(id: Int) = dbQuery {
        Users.deleteWhere { Users.id.eq(id) }
    }

    private fun toUser(row: ResultRow) = User(
        id = row[Users.id],
        name = row[Users.name],
        age = row[Users.age]
    )

    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}