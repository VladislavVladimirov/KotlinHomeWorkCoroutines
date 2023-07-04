import PostsApiService.client

import PostsApiService.getComments
import PostsApiService.getPosts
import dto.PostWithComments
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
                println(posts)
                println()
                posts.forEach { post ->
                    println("=========================Пост================================")
                    println("id: ${post.post.id}")
                    println("Дата публикации: ${post.post.published}")
                    println("Автор: ${post.post.author}")
                    println("Аватар: ${post.post.authorAvatar}")
                    println("${post.post.content}")
                    println("Лайки: ${post.post.likes}")
                    println("Лайкнул ли я: ${post.post.likedByMe}")
                    if (post.post.attachment != null) {
                        println("Вложения: ${post.post.attachment}")
                    }
                    if (post.comments.isNotEmpty()) {
                        println("Комментарии:\n")
                        post.comments.forEach { comment ->
                            println("!!!!!!!!!!!!!!!!!!!!!!Комментарий!!!!!!!!!!!!!!!!!!!!!!!!!")
                            println("id: ${comment.id}")
                            println("Дата публикации: ${comment.published}")
                            println("Автор: ${comment.author}")
                            println("Аватар: ${comment.authorAvatar}")
                            println("${comment.content}")
                            println("Лайки: ${comment.likes}")
                            println("Лайкнул ли я: ${comment.likedByMe}\n")

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
