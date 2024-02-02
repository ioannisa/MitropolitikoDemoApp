package com.example.sampleapplication.network

import com.example.sampleapplication.utils.MoviesResponse
import retrofit2.Response
import retrofit2.http.GET

interface MoviesApi {

    @GET("movies.json")
    suspend fun getMoviesOnline(): Response<MoviesResponse>

}