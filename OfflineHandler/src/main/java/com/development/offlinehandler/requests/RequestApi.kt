package com.development.offlinehandler.requests

import com.development.offlinehandler.Singleton
import com.development.offlinehandler.model.ApiRequestGetData
import com.development.offlinehandler.model.RequestContent
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

interface RequestApi {
    @Headers("Content-Type: application/json")
    @POST("GetWorkFlowDefinitions")
        fun getOfflineData(@Header("Authorization") auth: String, @Body params: RequestContent): Call<ApiRequestGetData>

    companion object{
        operator fun invoke(): RequestApi {
            return Retrofit.Builder()
                .baseUrl(Singleton.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RequestApi::class.java)
        }
    }
}