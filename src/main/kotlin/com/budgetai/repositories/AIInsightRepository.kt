package com.budgetai.repositories

import com.budgetai.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.JsonElement
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

class AiInsightRepository(private val database: Database) {
    // Initialize database schema
    init {
        transaction(database) {
            SchemaUtils.create(AiInsights)
        }
    }

    // Helper Methods
    private suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { block() }

    // Maps database row to AiInsightDTO
    private fun toAiInsight(row: ResultRow) = AiInsightDTO(
        id = row[AiInsights.id].value,
        userId = row[AiInsights.userId].value,
        budgetId = row[AiInsights.budgetId].value,
        budgetItemId = row[AiInsights.budgetItemId]?.value,
        prompt = row[AiInsights.prompt],
        response = row[AiInsights.response],
        type = row[AiInsights.type],
        sentiment = row[AiInsights.sentiment],
        metadata = row[AiInsights.metadata],
        createdAt = row[AiInsights.createdAt].toString()
    )

    // Read Methods
    // Retrieves an insight by its ID
    suspend fun findById(id: Int): AiInsightDTO? = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.id eq id }
            .map(::toAiInsight)
            .singleOrNull()
    }

    // Retrieves all insights for a given user ID
    suspend fun findByUserId(userId: Int): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.userId eq userId }
            .map(::toAiInsight)
    }

    // Retrieves all insights for a specific budget
    suspend fun findByBudgetId(budgetId: Int): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.budgetId eq budgetId }
            .map(::toAiInsight)
    }

    // Retrieves all insights for a specific budget item
    suspend fun findByBudgetItemId(budgetItemId: Int): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.budgetItemId eq budgetItemId }
            .map(::toAiInsight)
    }

    // Retrieves insights by type
    suspend fun findByType(type: InsightType): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.type eq type }
            .map(::toAiInsight)
    }

    // Retrieves insights by sentiment
    suspend fun findBySentiment(sentiment: Sentiment): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where { AiInsights.sentiment eq sentiment }
            .map(::toAiInsight)
    }

    // Retrieves insights for a user within a date range
    suspend fun findByUserIdAndDateRange(
        userId: Int,
        startDate: kotlinx.datetime.LocalDateTime,
        endDate: kotlinx.datetime.LocalDateTime
    ): List<AiInsightDTO> = dbQuery {
        AiInsights.selectAll()
            .where {
                (AiInsights.userId eq userId) and
                (AiInsights.createdAt greaterEq startDate.toString()) and
                (AiInsights.createdAt lessEq endDate.toString())
            }
            .map(::toAiInsight)
    }

    // Write Methods
    // Creates a new insight and returns its ID
    suspend fun create(insight: AiInsightDTO): Int = dbQuery {
        AiInsights.insertAndGetId { row ->
            row[userId] = EntityID(insight.userId, Users)
            row[budgetId] = EntityID(insight.budgetId, Budgets)
            insight.budgetItemId?.let { row[budgetItemId] = EntityID(it, BudgetItems) }
            row[prompt] = insight.prompt
            row[response] = insight.response
            row[type] = insight.type
            row[sentiment] = insight.sentiment
            row[metadata] = insight.metadata
        }.value
    }

    // Updates an existing insight
    suspend fun update(id: Int, insight: AiInsightDTO) = dbQuery {
        AiInsights.update({ AiInsights.id eq id }) { stmt ->
            stmt[userId] = EntityID(insight.userId, Users)
            stmt[budgetId] = EntityID(insight.budgetId, Budgets)
            insight.budgetItemId?.let { stmt[budgetItemId] = EntityID(it, BudgetItems) }
            stmt[prompt] = insight.prompt
            stmt[response] = insight.response
            stmt[type] = insight.type
            stmt[sentiment] = insight.sentiment
            stmt[metadata] = insight.metadata
        }
    }

    // Updates just the sentiment of an insight
    suspend fun updateSentiment(id: Int, sentiment: Sentiment?) = dbQuery {
        AiInsights.update({ AiInsights.id eq id }) { stmt ->
            stmt[AiInsights.sentiment] = sentiment
        }
    }

    // Updates just the metadata of an insight
    suspend fun updateMetadata(id: Int, metadata: JsonElement?) = dbQuery {
        AiInsights.update({ AiInsights.id eq id }) { stmt ->
            stmt[AiInsights.metadata] = metadata
        }
    }

    // Delete Methods
    // Deletes an insight by its ID
    suspend fun delete(id: Int) = dbQuery {
        AiInsights.deleteWhere { AiInsights.id eq id }
    }

    // Deletes all insights for a given user ID
    suspend fun deleteByUserId(userId: Int) = dbQuery {
        AiInsights.deleteWhere { AiInsights.userId eq userId }
    }

    // Deletes all insights for a given budget ID
    suspend fun deleteByBudgetId(budgetId: Int) = dbQuery {
        AiInsights.deleteWhere { AiInsights.budgetId eq budgetId }
    }

    // Deletes all insights for a given budget item ID
    suspend fun deleteByBudgetItemId(budgetItemId: Int) = dbQuery {
        AiInsights.deleteWhere { AiInsights.budgetItemId eq budgetItemId }
    }

    // Analysis Methods
    // Gets the distribution of insight types for a user
    suspend fun getInsightTypeDistribution(userId: Int): Map<InsightType, Int> = dbQuery {
        AiInsights
            .slice(AiInsights.type, AiInsights.id.count())
            .select { AiInsights.userId eq userId }
            .groupBy(AiInsights.type)
            .associate {
                it[AiInsights.type] to (it[AiInsights.id.count()] ?: 0)
            }
    }

    // Gets the sentiment distribution for a user
    suspend fun getSentimentDistribution(userId: Int): Map<Sentiment, Int> = dbQuery {
        AiInsights
            .slice(AiInsights.sentiment, AiInsights.id.count())
            .select { AiInsights.userId eq userId and AiInsights.sentiment.isNotNull() }
            .groupBy(AiInsights.sentiment)
            .associate {
                it[AiInsights.sentiment]!! to (it[AiInsights.id.count()] ?: 0)
            }
    }

    // Gets recent insights with pagination
    suspend fun getRecentInsights(
        userId: Int,
        limit: Int = 10,
        offset: Int = 0
    ): List<AiInsightDTO> = dbQuery {
        AiInsights
            .selectAll()
            .where { AiInsights.userId eq userId }
            .orderBy(AiInsights.createdAt to SortOrder.DESC)
            .limit(limit, offset.toLong())
            .map(::toAiInsight)
    }
}