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


class OfflineHandler() {
    //Inicializar librería
    fun init(url: String, token: String, wfid: Int, wfv: Int, apkv: String, imei: String){
        OfflineSingleton.h//Instancia inicial de la base de datos
        OfflineSingleton.url = url//URL en singleton internal
        OfflineSingleton.token = token//Token en singleton internal
        //Verificar conexión
        if (OfflineSingleton.misc!!.isNetDisp()){//Está habilitado el internet?
            if (OfflineSingleton.misc!!.isOnlineNet()){//Hay servicio?
                verifyDataOnline(wfid, wfv, apkv, imei)
            }
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
                        OfflineController().dataStorage(response.body()!!)
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
        var exists = OfflineController().exists()
        if (exists == 0){
            Log.e("NO HAY CONEXIÓN", "SIN CONEXIÓN")
        }
    }
    //Es posible utilizar la librería??
    fun isOfflineViable(): Boolean{
        var vc = OfflineController().isOfflineViable()
        var cj = if(vc.get(0).countJson > 0) 1 else 0
        var cu = if(vc.get(0).countUser > 0) 1 else 0
        var cp = if(vc.get(0).countProducts > 0) 1 else 0
        var sum = cj + cu + cp
        return if(sum == 3) true else false
    }
    //Recuperar stages de productos
    fun getProductStages(productId: String, productName: String, productStageId: String): HashMap<String, Any> {
        if (productId.equals("0") && productName.equals("0")){//Sí iniciamos solicitud
            //Y no hay un folio, creamos uno ficticio
            OfflineSingleton.invoice = "FF-0"+OfflineSingleton.misc!!.generateRandom(6)
        }
        var response = HashMap<String, Any>()
        response = OfflineController().getProductsData(productId, productName, productStageId)
        response.put("folioFake", OfflineSingleton.invoice)
        return response
    }

    //Recuperar stages de workflow
    fun getStages(stage: String, idLocalStage: Int): java.util.HashMap<String, Any> {
        var json = OfflineController().getApiData(stage, idLocalStage)
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
        hash.put("date", OfflineSingleton.misc!!.dateTime())

        var lastId = OfflineController().saveStageData(hash)
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
        hash.put("date", OfflineSingleton.misc!!.dateTime())

        var lastId = OfflineController().saveStageData(hash)
        if (lastId > 0){
            result = getProductStages(opd.stageId.toString(), opd.name, opd.productId)
        }

        if (result.get("productName").toString().equals("")){
            //Último stage
            OfflineSingleton.invoice = ""
        }

        setPersistenceOffline(
            result.get("productStageId").toString(),
            result.get("productName").toString(),
            result.get("productId").toString(),
            OfflineSingleton.invoice)

        return result
    }
    //Verificar si existe usuario para insertar o actualizar
    fun saveUser(oud: OfflineUserData){
        var existe = OfflineController().userExists(oud.userId)
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
        hash.put("response", oud.response)
        hash.put("rolename", oud.rolename)
        hash.put("currentfile", oud.currentfile)
        OfflineController().saveUserData(hash)
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
        hash.put("response", oud.response)
        hash.put("rolename", oud.rolename)
        hash.put("currentfile", oud.currentfile)
        OfflineController().updateUserData(hash)
    }

    fun login(user: String, pass: String): HashMap<String, Any> {
        var hash = java.util.HashMap<String, String>()
        hash.put("user", user)
        hash.put("pass", pass)

        return OfflineController().login(hash)
    }

    fun offlineResponses(): ArrayList<HashMap<String, Any>> {
        val res= OfflineController().getOfflineResponses()
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
    internal fun setPersistenceOffline(
        ips: String,//idproductstage
        nps: String,//nameproductstage
        ip: String,//idproduct
        iv: String){//invoice (folio)
        OfflineController().setPersistenceOffline(ips, nps, ip, iv)
    }


    fun getListPersistence(): ArrayList<HashMap<String, Any>> {
        return OfflineController().getListPersistence()
    }

    fun getPersistenceStage(invoice: String): java.util.HashMap<String, Any> {
        OfflineSingleton.invoice = invoice
        var persistenceData = OfflineController().getPersistenceDetail(invoice)
        var ips = persistenceData.get("ips").toString()
        var ip = persistenceData.get("ip").toString()

        return OfflineController().getPendant(ips.toInt(), ip.toInt())

    }

}