import com.budgetai.models.CategoryType
import kotlinx.serialization.Serializable
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.CurrentTimestamp
import org.jetbrains.exposed.sql.kotlin.datetime.timestamp


object Categories : IntIdTable("categories") {
    val name = varchar("name", 50).uniqueIndex()
    val type = enumerationByName("type", 20, CategoryType::class)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}

@Serializable
data class CategoryDTO(
    val id: Int = 0,
    val name: String,
    val type: CategoryType,
    val description: String? = null,
    val createdAt: String? = null
)

@Serializable
data class UpdateCategoryRequest(
    val name: String, val type: CategoryType, val description: String?
)