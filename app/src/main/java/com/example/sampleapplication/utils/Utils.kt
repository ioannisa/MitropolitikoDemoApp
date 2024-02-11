package com.example.sampleapplication.utils

import androidx.compose.runtime.Immutable
import com.google.gson.annotations.SerializedName

@Immutable
data class Movie(
    val adult: Boolean,
    val backdrop_path: String,
    val genre_ids: List<Int>,
    val id: Int,
    val original_language: String,
    val original_title: String,
    val overview: String,
    val popularity: Double,
    val poster_path: String,
    val release_date: String,
    val title: String,
    val video: Boolean,
    val vote_average: Double,
    val vote_count: Int
)

@Immutable
data class MoviesResponse(
    @SerializedName("results") val results: List<Movie>
)

// example movie image:
// https://image.tmdb.org/t/p/w500/d0OdD1I8qAfETvE9Rp9Voq7R8LR.jpg
class NetworkUtils {
    enum class ImageSize(val pathSegment: String) {
        LARGE("w500"),
        SMALL("w200")
    }

    companion object {
        fun getImageUrl(path: String, size: ImageSize): String =
            "https://image.tmdb.org/t/p/${size.pathSegment}$path"
    }
}