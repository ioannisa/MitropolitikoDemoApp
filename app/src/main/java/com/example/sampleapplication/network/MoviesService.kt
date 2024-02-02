package com.example.sampleapplication.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MoviesService {
    val api: MoviesApi by lazy {
         Retrofit.Builder()
            .baseUrl("https://services.anifantakis.eu/mitropolitiko/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MoviesApi::class.java)
    }
}