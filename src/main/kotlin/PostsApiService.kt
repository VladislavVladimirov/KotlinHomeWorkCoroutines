import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dto.Author
import dto.Comment
import dto.Post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object PostsApiService {
    private const val BASE_URL = "http://192.168.0.4:9999/api"
    private val gson = Gson()

    suspend fun getPosts(client: OkHttpClient): List<Post> =
        makeRequest("$BASE_URL/posts", client, object : TypeToken<List<Post>>() {})

    suspend fun getComments(client: OkHttpClient, id: Long): List<Comment> =
        makeRequest("$BASE_URL/posts/$id/comments", client, object : TypeToken<List<Comment>>() {})

    suspend fun getAuthor(client: OkHttpClient, id: Long): Author =
        makeRequest("$BASE_URL/authors/$id", client, object : TypeToken<Author>() {})

    private suspend fun <T> makeRequest(url: String, client: OkHttpClient, typeToken: TypeToken<T>): T =
        withContext(Dispatchers.IO) {
            client.apiCall(url)
                .let { response ->
                    if (!response.isSuccessful) {
                        response.close()
                        throw RuntimeException(response.message)
                    }
                    val body = response.body ?: throw RuntimeException("Response body is null")
                    gson.fromJson(body.string(), typeToken.type)
                }
        }

    private suspend fun OkHttpClient.apiCall(url: String): Response {
        return suspendCoroutine { continuation ->
            Request.Builder()
                .url(url)
                .build()
                .let(::newCall)
                .enqueue(object : Callback {
                    override fun onResponse(call: okhttp3.Call, response: Response) {
                        continuation.resume(response)
                    }

                    override fun onFailure(call: okhttp3.Call, e: IOException) {
                        continuation.resumeWithException(e)
                    }
                })
        }
    }

    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(logging)
        .build()


}