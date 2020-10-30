package com.ds.offlinehandler.requests

import com.ds.offlinehandler.OfflineSingleton
import com.ds.offlinehandler.model.ApiRequestGetData
import com.ds.offlinehandler.model.RequestContent
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
                .baseUrl(OfflineSingleton.url)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(RequestApi::class.java)
        }
    }
}