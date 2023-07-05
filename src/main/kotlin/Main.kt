import PostsApiService.client
import PostsApiService.getAuthor
import PostsApiService.getComments
import PostsApiService.getPosts
import dto.CommentWithAuthor
import dto.PostWithComments
import dto.PostsWithCommentsAndAuthors
import kotlinx.coroutines.*
import kotlin.coroutines.EmptyCoroutineContext

fun main() {
    with(CoroutineScope(EmptyCoroutineContext)) {
        launch {
            try {
                val posts = getPosts(client)
                    .map { post ->
                        async {
                            PostWithComments(post, getComments(client, post.id))
                        }
                    }.awaitAll()
                val postsWithCommentsAndAuthors = posts.map {
                    async {
                        PostsWithCommentsAndAuthors(it.post,
                            getAuthor(client,it.post.authorId),
                            it.comments.map {
                                comment ->
                                CommentWithAuthor(comment, getAuthor(client,comment.authorId))
                            }
                        )
                    }
                }.awaitAll()
                postsWithCommentsAndAuthors.forEach { post ->
                    println("=========================Пост================================")
                    println("id: ${post.post.id}")
                    println("Дата публикации: ${post.post.published}")
                    println("Автор: ${post.author.name}")
                    println("Аватар: ${post.author.avatar}")
                    println(post.post.content)
                    println("Лайки: ${post.post.likes}")
                    println("Лайкнул ли я: ${post.post.likedByMe}")
                    if (post.post.attachment != null) {
                        println("Вложения: ${post.post.attachment}")
                    }
                    if (post.commentWithAuthor.isNotEmpty()) {
                        println("Комментарии:\n")
                        post.commentWithAuthor.forEach {
                            println("!!!!!!!!!!!!!!!!!!!!!!Комментарий!!!!!!!!!!!!!!!!!!!!!!!!!")
                            println("id: ${it.comment.id}")
                            println("Дата публикации: ${it.comment.published}")
                            println("Автор: ${it.commentAuthor.name}")
                            println("Аватар: ${it.commentAuthor.avatar}")
                            println(it.comment.content)
                            println("Лайки: ${it.comment.likes}")
                            println("Лайкнул ли я: ${it.comment.likedByMe}\n")

                        }
                        println()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    Thread.sleep(1000L)
}
