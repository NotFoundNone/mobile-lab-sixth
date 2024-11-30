import io.ktor.client.HttpClient
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json

class CharacterRepository(private val client: HttpClient) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCharacters(page: Int): List<Character> {
        val url = "https://anapioficeandfire.com/api/characters?page=$page&pageSize=50"
        return try {
            val response: HttpResponse = client.get(url)
            json.decodeFromString(response.bodyAsText())
        } catch (e: ClientRequestException) {
            emptyList()
        }
    }
}
