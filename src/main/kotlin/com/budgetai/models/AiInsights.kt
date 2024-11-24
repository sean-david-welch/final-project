import com.budgetai.models.InsightType
import com.budgetai.models.Sentiment
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class AiInsightDTO(
    val id: Int = 0,
    val userId: Int,
    val budgetId: Int,
    val budgetItemId: Int? = null,
    val prompt: String,
    val response: String,
    val type: InsightType,
    val sentiment: Sentiment? = null,
    val metadata: JsonElement? = null,
    val createdAt: String? = null
)
