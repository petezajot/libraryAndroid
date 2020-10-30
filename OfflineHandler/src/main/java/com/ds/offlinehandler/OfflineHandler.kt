package com.ds.offlinehandler

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.ds.offlinehandler.controller.OfflineController
import com.ds.offlinehandler.misc.Misc
import com.ds.offlinehandler.model.*
import com.ds.offlinehandler.requests.RequestApi
import com.google.gson.JsonParser
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class OfflineHandler(ctx: Context) {
    //Asignamos la variable de contexto (Contenida en constructor)
    var ctx = ctx
    //Inicializar librería
    fun init(url: String, token: String, wfid: Int, wfv: Int, apkv: String, imei: String){
        DBHelper(ctx)
        //URL global (Singleton)
        OfflineSingleton.url = url
        OfflineSingleton.token = token
        //Verificar conexión
        if (Misc(ctx).isNetDisp()){//Está habilitado el internet?
            if (Misc(ctx).isOnlineNet()){//Hay servicio?
                verifyDataOnline(wfid, wfv, apkv, imei)
            }else{
                verifyDataOffline()
            }
        }else{
            verifyDataOffline()
        }
    }
    //Consumir api de workflow
    protected fun verifyDataOnline(
        wfid: Int,
        wfv: Int,
        apkv: String,
        imei: String
    ) {
        //API request
        var apiBody = ApiBody(wfid, wfv,apkv, imei)
        var requestContent = RequestContent(apiBody, "", "", "")
        RequestApi().getOfflineData(OfflineSingleton.token, requestContent).enqueue(object :
            Callback<ApiRequestGetData> {
            override fun onResponse(
                call: Call<ApiRequestGetData>,
                response: Response<ApiRequestGetData>
            ) {
                if (!response.isSuccessful) {
                    Log.e("Offline handler: ", "ERROR")
                } else {
                    if (response.body()!!.IsOK) {
                        OfflineController().dataStorage(response.body()!!, ctx)
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

    //Recuperar stages de productos
    fun getProductStages(productId: String, productName: String, productStageId: String): HashMap<String, Any> {
        if (productId.equals("0") && productName.equals("0")){//Sí iniciamos solicitud
            //Y no hay un folio, creamos uno ficticio
            OfflineSingleton.invoice = "FF-0"+Misc(ctx).generateRandom(6)
        }
        var response = HashMap<String, Any>()
        response = OfflineController().getProductsData(ctx, productId, productName, productStageId)
        response.put("folioFake", OfflineSingleton.invoice)
        return response
    }

    //Recuperar stages de workflow
    fun getStages(stage: String, idLocalStage: Int): java.util.HashMap<String, Any> {
        var json = OfflineController().getApiData(ctx, stage, idLocalStage)
        var setData = HashMap<String, Any>()
        if (json.get("product") == 1){
            var parser = JsonParser()
            var obj = parser.parse(json.get("json").toString()).asJsonObject
            var arr = parser.parse(obj.get("Products").toString()).asJsonArray
            var productos: MutableList<HashMap<String, Any>> = mutableListOf<HashMap<String, Any>>()

            arr.forEach {
                var wf = parser.parse(it.toString()).asJsonObject

                var detalleProd = HashMap<String, Any>()
                detalleProd.put("productos", "${wf.get("Id")}|${wf.get("Name")}")
                productos.add(detalleProd)
            }
            json.put("products", productos)
            setData = json
        }else{
            json.put("products", "0")
            setData = json
        }
        return setData
    }
    //Guardar respuestas de stages
    fun saveStages(osd: OfflineStageData): java.util.HashMap<String, Any> {
        var hash = java.util.HashMap<String, Any>()
        var result = java.util.HashMap<String, Any>()

        hash.put("stageId", osd.stageId)
        hash.put("name", osd.name)
        hash.put("json", osd.json)
        hash.put("endpoint", osd.endpoint)
        hash.put("seq", osd.seq)
        hash.put("folio", OfflineSingleton.invoice)
        hash.put("date", Misc(ctx).dateTime())

        var lastId = OfflineController().saveStageData(ctx, hash)
        if (lastId > 0){
            result = getStages(osd.name, osd.stageId)
        }
        return result
    }

    fun saveProductStages(opd: OfflineProductData): java.util.HashMap<String, Any> {
        var hash = java.util.HashMap<String, Any>()
        var result = java.util.HashMap<String, Any>()
        hash.put("stageId", opd.stageId)
        hash.put("name", opd.name)
        hash.put("json", opd.json)
        hash.put("endpoint", opd.endpoint)
        hash.put("seq", opd.seq)
        hash.put("folio", OfflineSingleton.invoice)
        hash.put("date", Misc(ctx).dateTime())

        var lastId = OfflineController().saveStageData(ctx, hash)
        if (lastId > 0){
            result = getProductStages(opd.stageId.toString(), opd.name, opd.productId)
        }
        return result
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

    fun login(user: String, pass: String): Boolean {
        var hash = java.util.HashMap<String, String>()
        hash.put("user", user)
        hash.put("pass", pass)

        return OfflineController().login(ctx, hash)
    }

    fun offlineResponses(): ArrayList<HashMap<String, Any>> {
        val res= OfflineController().getOfflineResponses(ctx)
        var array = ArrayList<HashMap<String,Any>>()
        res.forEach {
            var hash = HashMap<String,Any>()
            hash.put("id", it.id)
            hash.put("endpoint", it.endpoint)
            hash.put("folio", it.folio)
            hash.put("json", it.json)
            hash.put("name", it.name)
            hash.put("stageId", it.stageId)

            array.add(hash)
        }
        return array
    }

    //Persistence
    fun setPersistenceOffline(
        ips: String,//idproductstage
        nps: String,//nameproductstage
        ip: String,//idproduct
        iv: String){//invoice (folio)
        OfflineController().setPersistenceOffline(ips, nps, ip, iv, ctx)
    }


    fun getListPersistence(): ArrayList<HashMap<String, Any>> {
        return OfflineController().getListPersistence(ctx)
    }

    fun getPersistenceStage(invoice: String): java.util.HashMap<String, Any> {
        OfflineSingleton.invoice = invoice
        var persistenceData = OfflineController().getPersistenceDetail(invoice, ctx)
        var ips = persistenceData.get("ips").toString()
        var ip = persistenceData.get("ip").toString()

        return OfflineController().getPendant(ips.toInt(), ip.toInt(), ctx)

    }

}