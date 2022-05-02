package io.github.dennistsar.sirs_kobweb.misc

//Stolen from Philipp Lackner
sealed class Resource<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?): Resource<T>(data)
    class Error<T>(message: String, data: T? = null): Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true): Resource<T>(null)
}

//data class Resource(
//    val stockInfos: List<Int> = emptyList(),
//    val company: Int? = null,
//    val isLoading: Boolean = false,
//    val error: String? = null
//)