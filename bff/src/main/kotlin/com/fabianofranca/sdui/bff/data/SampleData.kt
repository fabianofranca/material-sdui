package com.fabianofranca.sdui.bff.data

import java.util.concurrent.CopyOnWriteArrayList

data class Article(
    val id: String,
    val title: String,
    val summary: String,
    val body: String,
    val author: String,
    val tags: List<String>,
    val imageUrl: String,
    val url: String,
)

data class Profile(
    val name: String = "Ana Souza",
    val email: String = "ana@exemplo.com",
    val notifications: Boolean = true,
    val newsletter: Boolean = false,
    val theme: String = "system",
)

/** Dados em memória para as telas de exemplo. Num BFF de verdade, viriam dos serviços. */
class SampleData {
    private val topics = listOf("Kotlin", "Compose", "SDUI", "Material 3", "Ktor", "KMP", "iOS")

    val articles: List<Article> = (1..35).map { n ->
        val topic = topics[n % topics.size]
        Article(
            id = "a$n",
            title = "$topic na prática #$n",
            summary = "Um resumo curto do artigo $n sobre $topic, para caber em duas linhas no card.",
            body = (1..4).joinToString("\n\n") {
                "Parágrafo $it do artigo $n. Server-Driven UI deixa o backend descrever a tela, " +
                    "e o app só desenha com os componentes do Material 3."
            },
            author = listOf("Ana Souza", "Bruno Lima", "Carla Dias", "Davi Rocha")[n % 4],
            tags = listOf(topic, if (n % 2 == 0) "Tutorial" else "Opinião"),
            imageUrl = "https://picsum.photos/seed/sdui$n/800/450",
            url = "https://kotlinlang.org/docs/multiplatform.html",
        )
    }

    val following: List<String> = listOf("Ana Souza", "Bruno Lima", "Carla Dias")

    val posts: MutableList<String> = CopyOnWriteArrayList()

    @Volatile
    var profile: Profile = Profile()

    fun page(number: Int, size: Int = PAGE_SIZE): List<Article> =
        articles.drop((number - 1).coerceAtLeast(0) * size).take(size)

    fun hasPage(number: Int, size: Int = PAGE_SIZE): Boolean = (number - 1) * size < articles.size

    fun article(id: String?): Article? = articles.firstOrNull { it.id == id }

    companion object {
        const val PAGE_SIZE = 10
    }
}
