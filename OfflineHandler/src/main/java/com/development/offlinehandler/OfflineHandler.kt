package com.development.offlinehandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.development.offlinehandler.controller.OfflineController
import com.development.offlinehandler.misc.Misc
import com.development.offlinehandler.model.ApiRequestGetData
import com.development.offlinehandler.model.OfflineStageData
import com.development.offlinehandler.model.OfflineUserData
import com.development.offlinehandler.model.RequestContent
import com.development.offlinehandler.requests.RequestApi
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OfflineHandler(ctx: Context) {
    //Asignamos la variable de contexto (Contenida en constructor)
    var ctx = ctx
    //Inicializar librería
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
    //Consumir api de workflow
    protected fun verifyDataOnline() {
        //API request
        var exists = OfflineController().exists(ctx)
        var requestContent = RequestContent("1", "", "", "")
        RequestApi().getOfflineData(Singleton.token, requestContent).enqueue(object :
            Callback<ApiRequestGetData> {
            override fun onResponse(
                call: Call<ApiRequestGetData>,
                response: Response<ApiRequestGetData>
            ) {
                if (!response.isSuccessful) {
                    Log.e("Offline handler: ", "ERROR")
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

            override fun onFailure(call: Call<ApiRequestGetData>, t: Throwable) {
                Log.e("Offline handler: ", t.localizedMessage)
            }
        })
    }
    //Si no hay conexión y no fue posible almacenar en SQLite los workflows
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
    //Almacenar workflows
    internal fun saveData(body: ApiRequestGetData) {
        var gson = Gson()
        var bodyArr = body.Body
        for (i in 0 until bodyArr.size){
            var stages = bodyArr[i].Stages
            for (j in 0 until stages.size){
                var jsonModel: HashMap<String, Any> = HashMap<String, Any>()
                jsonModel.put("bodyId", bodyArr[i].Id)
                jsonModel.put("bodyName", bodyArr[i].Name)
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
    //Actualizar workflows
    internal fun updateData(body: ApiRequestGetData) {
        var gson = Gson()
        var bodyArr = body.Body
        for (i in 0 until bodyArr.size){
            var stages = bodyArr[i].Stages
            for (j in 0 until stages.size){
                var jsonModel: HashMap<String, Any> = HashMap<String, Any>()
                jsonModel.put("bodyId", bodyArr[i].Id)
                jsonModel.put("bodyName", bodyArr[i].Name)
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
    //Recuperar stages de workflow
    fun getStages(stage: String): java.util.HashMap<String, Any> {
        var json = OfflineController().getApiData(ctx, stage)
        return json
    }
    //Guardar respuestas de stages
    fun saveStages(osd: OfflineStageData){
        var hash = java.util.HashMap<String, Any>()
        hash.put("stageId", osd.stageId)
        hash.put("name", osd.name)
        hash.put("json", osd.json)
        hash.put("endpoint", osd.endpoint)
        hash.put("seq", osd.seq)
        hash.put("folio", osd.folio)
        hash.put("date", Misc(ctx).dateTime())

        OfflineController().saveStageData(ctx, hash)
    }
    //Verificar si existe usuario para insertar o actualizar
    fun saveUser(oud: OfflineUserData){
        var existe = OfflineController().userExists(ctx, oud.userId)
        if (existe == 1){
            //Si existe el usuario, hacemos update
            updateUser(oud)
        }else{
            //No existe, hacemos insert
            saveUserData(oud)
        }
    }
    //Hacer insert a SQLite usuario (Login offline)
    internal fun saveUserData(oud: OfflineUserData){
        var hash = java.util.HashMap<String, Any>()
        hash.put("userId", oud.userId)
        hash.put("userName", oud.userName)
        hash.put("name", oud.name)
        hash.put("apat", oud.apat)
        hash.put("amat", oud.amat)
        hash.put("email", oud.email)
        hash.put("phone", oud.phone)
        hash.put("token", oud.token)
        hash.put("pass", oud.pass)

        OfflineController().saveUserData(ctx, hash)
    }
    //Hacer update a SQLite usuario (Login offline)
    internal fun updateUser(oud: OfflineUserData){
        var hash = java.util.HashMap<String, Any>()
        hash.put("userId", oud.userId)
        hash.put("userName", oud.userName)
        hash.put("name", oud.name)
        hash.put("apat", oud.apat)
        hash.put("amat", oud.amat)
        hash.put("email", oud.email)
        hash.put("phone", oud.phone)
        hash.put("token", oud.token)
        hash.put("pass", oud.pass)

        OfflineController().updateUserData(ctx, hash)
    }

}