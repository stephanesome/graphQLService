package seg3x02.booksgraphqlapi.resolvers.types

class CreateBookInput(
    val bookNumber: Int? = null,
    val description: String? = null,
    val year: String? = null,
    val cost: Float? = null,
    val title: String? = null,
    val category: String? = null
)
