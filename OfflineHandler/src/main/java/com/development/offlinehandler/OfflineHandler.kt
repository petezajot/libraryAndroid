package com.development.offlinehandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.development.offlinehandler.controller.OfflineController
import com.development.offlinehandler.misc.Misc
import com.development.offlinehandler.model.OfflineGetData
import com.development.offlinehandler.model.RequestContent
import com.development.offlinehandler.requests.RequestApi
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OfflineHandler(ctx: Context) {
    var ctx = ctx
    fun init(url: String, token: String){
        //URL global (Singleton)
        Singleton.url = url
        Singleton.token = token
        //Verificar conexión
        if (Misc(ctx).isNetDisp()){//Está habilitado el internet?
            if (Misc(ctx).isOnlineNet()){//Hay servicio?
                verifyDataOnline()
            }else{
                verifyDataOffline()
            }
        }else{
            verifyDataOffline()
        }
    }

    protected fun verifyDataOnline() {
        //API request
        var exists = OfflineController().exists(ctx)
        var requestContent = RequestContent("1", "", "", "")
        RequestApi().getOfflineData(Singleton.token, requestContent).enqueue(object : Callback<OfflineGetData> {
            override fun onResponse(
                call: Call<OfflineGetData>,
                response: Response<OfflineGetData>
            ) {
                if (!response.isSuccessful) {
                    Log.e("Offline handler: ", "ERROR")
                    Log.e("Offline handler: ", response.errorBody().toString())
                } else {
                    if (response.body()!!.IsOK) {
                        if (exists > 0) {
                            updateData(response.body()!!)
                        } else {
                            saveData(response.body()!!)
                        }
                    } else {
                        Log.e("Offline handler: ", response.body()!!.Messages)
                    }
                }
            }

            override fun onFailure(call: Call<OfflineGetData>, t: Throwable) {
                Log.e("Offline handler: ", t.localizedMessage)
            }
        })
    }

    internal fun verifyDataOffline(){
        var exists = OfflineController().exists(ctx)
        if (exists == 0){
            Toast.makeText(
                ctx,
                "No es posible actualizar datos para trabajar offline",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    internal fun saveData(body: OfflineGetData) {
        var gson = Gson()
        var bodyArr = body.Body
        for (i in 0 until bodyArr.size){
            var stages = bodyArr[i].Stages
            for (j in 0 until stages.size){
                var jsonModel: HashMap<String, Any> = HashMap<String, Any>()
                jsonModel.put("stageId", stages[j].Id)
                jsonModel.put("dateTime", Misc(ctx).dateTime())
                jsonModel.put("name", stages[j].Name)
                jsonModel.put("sequence", stages[j].Sequence)
                var json = gson.toJson(stages[j])
                jsonModel.put("jsonString", json)
                OfflineController().insertApiData(jsonModel, ctx)
            }
        }
    }

    internal fun updateData(body: OfflineGetData) {
        var gson = Gson()
        var bodyArr = body.Body
        for (i in 0 until bodyArr.size){
            var stages = bodyArr[i].Stages
            for (j in 0 until stages.size){
                var jsonModel: HashMap<String, Any> = HashMap<String, Any>()
                jsonModel.put("stageId", stages[j].Id)
                jsonModel.put("dateTime", Misc(ctx).dateTime())
                jsonModel.put("name", stages[j].Name)
                jsonModel.put("sequence", stages[j].Sequence)
                var json = gson.toJson(stages[j])
                jsonModel.put("jsonString", json)
                OfflineController().updateApiData(jsonModel, ctx)
            }
        }
    }

    fun getStages(stage: String): java.util.HashMap<String, String> {
        var json = OfflineController().getApiData(ctx, stage)
        return json
    }

    fun saveStages(hash: java.util.HashMap<String, Any>){
        hash.put("date", Misc(ctx).dateTime())
        OfflineController().saveStageData(ctx, hash)
    }

}