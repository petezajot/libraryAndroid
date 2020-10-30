package com.ds.offlinehandler.controller

import android.content.Context
import com.ds.offlinehandler.OfflineSingleton
import com.ds.offlinehandler.misc.Misc
import com.ds.offlinehandler.model.ApiRequestGetData
import com.ds.offlinehandler.model.DBHelper
import com.ds.offlinehandler.model.ReqCollection
import com.ds.offlinehandler.model.RequestResponse
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlin.collections.HashMap

internal class OfflineController {
    fun dataStorage(
        body: ApiRequestGetData,
        ctx: Context
    ) {
        var h = DBHelper(ctx)
        var jsonModel: HashMap<String, Any> = HashMap()
        var gson = Gson()
        var bodyArr = body.Body
        for (i in 0 until bodyArr.size) {
            var stages = bodyArr[i].Stages
            for (j in 0 until stages.size) {
                var verExt = verifyExistence(stages[j].Id, ctx, h.TAB_JSON, h.COL_STAGE_ID)
                jsonModel.put("bodyId", bodyArr[i].Id)
                jsonModel.put("bodyName", bodyArr[i].Name)
                jsonModel.put("stageId", stages[j].Id)
                jsonModel.put("dateTime", Misc(ctx).dateTime())
                jsonModel.put("name", stages[j].Name)
                jsonModel.put("sequence", stages[j].Sequence)
                var json = gson.toJson(stages[j])
                jsonModel.put("jsonString", json)

                if (verExt == 1){
                    //Sí existe, hacemos update
                    updateApiData(jsonModel, ctx)
                }else{
                    //No existe, hacemos insert
                    insertApiData(jsonModel, ctx)
                }
                productStorage(json, ctx)
            }
        }
    }

    private fun productStorage(json: String, ctx: Context) {
        var h = DBHelper(ctx)
        var jsonModel: HashMap<String, Any> = HashMap()
        var parser = JsonParser()
        var obj = parser.parse(json).asJsonObject
        if (obj.get("Products") != null){
            var arr = parser.parse(obj.get("Products").toString()).asJsonArray
            arr.forEach {
                var wf = parser.parse(it.toString()).asJsonObject
                var wfArr = parser.parse(wf.get("WorkFlows").toString()).asJsonArray
                wfArr.forEach{
                    var wfContent = parser.parse(it.toString()).asJsonObject
                    var stages = parser.parse(wfContent.get("Stages").toString()).asJsonArray
                    stages.forEach{
                        var stageDetail = parser.parse(it.toString()).asJsonObject
                        var AllowOffline = stageDetail.get("AllowOffline").toString()
                        var verExt = verifyExistence(
                            (stageDetail.get("Id").toString()).toInt(),
                            ctx,
                            h.TAB_PRODUCTS,
                            h.COL_PRODUCT_ID)
                        jsonModel.put("idStage", obj.get("Id"))//INTEGER
                        jsonModel.put("nameStage", obj.get("Name").toString())
                        jsonModel.put("descStage", obj.get("Description").toString())
                        jsonModel.put("groupName", wfContent.get("Name").toString())
                        jsonModel.put("groupDesc", wfContent.get("Description").toString())
                        jsonModel.put("groupId", wfContent.get("Id"))//INTEGER
                        jsonModel.put("producStageId", wf.get("Id"))//INTEGER
                        jsonModel.put("productStageName", wf.get("Name").toString())
                        jsonModel.put("productId", stageDetail.get("Id"))//INTEGER
                        jsonModel.put("productSequence", stageDetail.get("Sequence"))//INTEGER
                        jsonModel.put("productName", stageDetail.get("Name").toString())
                        jsonModel.put("productDesc", stageDetail.get("Description").toString())
                        jsonModel.put("productStageData", "$it")

                        if (AllowOffline == "true"){
                            if (verExt == 1){
                                //Sí existe, hacemos update
                                updateProductData(jsonModel, ctx)
                            }else{
                                //No existe, hacemos insert
                                insertProductData(jsonModel, ctx)
                            }
                        }
                    }
                }
            }
        }
    }

    fun verifyExistence(id: Int, ctx: Context, table: String, evaluate: String): Int{
        var h = DBHelper(ctx)
        val db = h.writableDatabase
        var existence = 0

        val query = "SELECT " +
                "COUNT(1) " +
                "FROM $table " +
                "WHERE $evaluate = $id;"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                existence = c.getInt(0)
            }while (c.moveToNext())
        }

        db.close()
        return existence
    }

    fun exists(ctx: Context): Int{
        var h = DBHelper(ctx)
        var db = h.writableDatabase
        var existe = 0
        val query = "SELECT COUNT(1) FROM ${h.TAB_JSON}"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                existe = c.getInt(0)
            }while (c.moveToNext())
        }
        db.close()
        return existe
    }

    fun userExists(ctx: Context, userId: Int): Int {
        var h = DBHelper(ctx)
        var db = h.writableDatabase
        var existe = 0
        val query = "SELECT COUNT(1) FROM ${h.TAB_USER} WHERE ${h.COL_USER_ID} = $userId;"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                existe = c.getInt(0)
            }while (c.moveToNext())
        }
        db.close()
        return existe
    }

    fun insertApiData(jsonModel: HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "INSERT INTO ${h.TAB_JSON} (" +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_BODY_ID}, " +
                "${h.COL_BODY_NAME}, " +
                "${h.COL_DATE}, " +
                "${h.COL_NAME}, " +
                "${h.COL_SEQUENCE}, " +
                "${h.COL_JSON}" +
                ")VALUES(" +
                "${jsonModel.get("stageId")}, " +
                "${jsonModel.get("bodyId")}, " +
                "'${jsonModel.get("bodyName")}', " +
                "'${jsonModel.get("dateTime")}', " +
                "'${jsonModel.get("name")}'," +
                "${jsonModel.get("sequence")}, " +
                "'${jsonModel.get("jsonString")}'" +
                ");"
        db.execSQL(query)
        db.close()
    }

    fun updateApiData(jsonModel: HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "UPDATE ${h.TAB_JSON} SET " +
                "${h.COL_BODY_ID} = ${jsonModel.get("bodyId")}, " +
                "${h.COL_BODY_NAME} = '${jsonModel.get("bodyName")}', " +
                "${h.COL_DATE} = '${jsonModel.get("dateTime")}', " +
                "${h.COL_NAME} = '${jsonModel.get("name")}', " +
                "${h.COL_SEQUENCE} = ${jsonModel.get("sequence")}, " +
                "${h.COL_JSON} = '${jsonModel.get("jsonString")}' " +
                "WHERE ${h.COL_STAGE_ID} = ${jsonModel.get("stageId")};"
        db.execSQL(query)
        db.close()
    }

    private fun insertProductData(hash: java.util.HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "INSERT INTO ${h.TAB_PRODUCTS} (" +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_STAGE_NAME}, " +
                "${h.COL_STAGE_DESC}, " +
                "${h.COL_GROUP_NAME}, " +
                "${h.COL_GROUP_DESC}, " +
                "${h.COL_GROUP_ID}, " +
                "${h.COL_PRODUCT_STAGE_ID}, " +
                "${h.COL_PRODUCT_STAGE_NAME}, " +
                "${h.COL_PRODUCT_ID}, " +
                "${h.COL_PRODUCT_SEQ}, " +
                "${h.COL_PRODUCT_NAME}, " +
                "${h.COL_PRODUCT_DESC}, " +
                "${h.COL_PRODUCT_STAGE}" +
                ")VALUES(" +
                "${hash.get("idStage")}, " +
                "'${hash.get("nameStage")}', " +
                "'${hash.get("descStage")}', " +
                "'${hash.get("groupName")}', " +
                "'${hash.get("groupDesc")}', " +
                "${hash.get("groupId")}, " +
                "${hash.get("producStageId")}, " +
                "${hash.get("productStageName")}, " +
                "${hash.get("productId")}, " +
                "${hash.get("productSequence")}, " +
                "'${hash.get("productName")}', " +
                "'${hash.get("productDesc")}', " +
                "'${hash.get("productStageData")}' " +
                ");"
        db.execSQL(query)

        var stagevalue = hash.get("idStage").toString()
        updateStatusJson(stagevalue.toInt(), ctx)
        db.close()
    }

    private fun updateProductData(hash: java.util.HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "UPDATE ${h.TAB_PRODUCTS} SET " +
                "${h.COL_STAGE_ID} = ${hash.get("idStage")}, " +
                "${h.COL_STAGE_NAME} = '${hash.get("nameStage")}', " +
                "${h.COL_STAGE_DESC} = '${hash.get("descStage")}', " +
                "${h.COL_GROUP_NAME} = '${hash.get("groupName")}', " +
                "${h.COL_GROUP_DESC} = '${hash.get("groupDesc")}', " +
                "${h.COL_GROUP_ID} = ${hash.get("groupId")}, " +
                "${h.COL_PRODUCT_STAGE_ID} = ${hash.get("producStageId")}, " +
                "${h.COL_PRODUCT_STAGE_NAME} = ${hash.get("productStageName")}, " +
                "${h.COL_PRODUCT_SEQ} = ${hash.get("productSequence")}, " +
                "${h.COL_PRODUCT_NAME} = '${hash.get("productName")}', " +
                "${h.COL_PRODUCT_DESC} = '${hash.get("productDesc")}', " +
                "${h.COL_PRODUCT_STAGE} = '${hash.get("productStageData")}' " +
                "WHERE ${h.COL_PRODUCT_ID} = ${hash.get("productId")};"
        db.execSQL(query)

        var stagevalue = hash.get("idStage").toString()
        updateStatusJson(stagevalue.toInt(), ctx)
        db.close()
    }

    private fun updateStatusJson(idStage: Int, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val update = "UPDATE ${h.TAB_JSON} " +
                "SET ${h.COL_PRODUCT} = 1 " +
                "WHERE ${h.COL_STAGE_ID} = $idStage;"
        db.execSQL(update)
        db.close()
    }

    fun getApiData(ctx: Context, stage: String, idLocalStage: Int): HashMap<String, Any> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        //Get Stages
        var json = HashMap<String, Any>()
        var query: String = if (stage == "0") {
            "SELECT " +
                    "${h.COL_STAGE_ID}, " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON}, " +
                    "${h.COL_ID}, " +
                    "${h.COL_PRODUCT} " +
                    "FROM ${h.TAB_JSON} " +
                    "ORDER BY ${h.COL_SEQUENCE} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${h.COL_STAGE_ID}, " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON}, " +
                    "${h.COL_ID}, " +
                    "${h.COL_PRODUCT} " +
                    "FROM ${h.TAB_JSON} " +
                    "WHERE ${h.COL_ID} > " +
                    "(SELECT ${h.COL_ID} FROM ${h.TAB_JSON} WHERE ${h.COL_NAME} = '$stage' AND ${h.COL_STAGE_ID} = $idLocalStage) " +
                    "ORDER BY ${h.COL_ID} " +
                    "ASC LIMIT 1;"
        }

        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                json.put("stageId", c.getInt(0))
                json.put("name", c.getString(1))
                json.put("json", c.getString(2))
                json.put("id", c.getInt(3))
                json.put("product", c.getInt(4))
            }while (c.moveToNext())
        }else{
            json.put("stageId", 0)
            json.put("name", "")
            json.put("json", "")
            json.put("id", 0)
            json.put("product", 0)
        }
        db.close()
        return json
    }

    fun getProductsData(
        ctx: Context,
        productId: String,
        productName: String,
        productStageId: String
    ): HashMap<String, Any> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        //Get Stages
        var json = HashMap<String, Any>()
        var query: String = if (productName == "0") {
            "SELECT " +
                    "${h.COL_PRODUCT_STAGE_ID}, " +
                    "${h.COL_PRODUCT_STAGE_NAME}, " +
                    "${h.COL_PRODUCT_ID}, " +
                    "${h.COL_PRODUCT_DESC}, " +
                    "${h.COL_PRODUCT_NAME}, " +
                    "${h.COL_PRODUCT_STAGE}, " +
                    "${h.COL_PRODUCT_SEQ} " +
                    "FROM ${h.TAB_PRODUCTS} " +
                    "WHERE ${h.COL_PRODUCT_STAGE_ID} = $productStageId " +
                    "ORDER BY ${h.COL_PRODUCT_SEQ} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${h.COL_PRODUCT_STAGE_ID}, " +
                    "${h.COL_PRODUCT_STAGE_NAME}, " +
                    "${h.COL_PRODUCT_ID}, " +
                    "${h.COL_PRODUCT_DESC}, " +
                    "${h.COL_PRODUCT_NAME}, " +
                    "${h.COL_PRODUCT_STAGE}, " +
                    "${h.COL_PRODUCT_SEQ} " +
                    "FROM ${h.TAB_PRODUCTS} " +
                    "WHERE ${h.COL_ID} > " +
                    "(SELECT ${h.COL_ID} FROM ${h.TAB_PRODUCTS} WHERE ${h.COL_PRODUCT_NAME} = '$productName' AND ${h.COL_PRODUCT_ID} = $productId) " +
                    "AND ${h.COL_PRODUCT_STAGE_ID} = $productStageId " +
                    "ORDER BY ${h.COL_ID} " +
                    "ASC LIMIT 1;"
        }

        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                json.put("productStageId", c.getInt(0))
                json.put("productStageName", c.getString(1))
                json.put("productId", c.getInt(2))
                json.put("productDesc", c.getString(3))
                json.put("productName", c.getString(4))
                json.put("productStage", c.getString(5))
                json.put("productSeq", c.getInt(6))
            }while (c.moveToNext())
        }else{
            json.put("productStageId", 0)
            json.put("productStageName", "")
            json.put("productId", 0)
            json.put("productDesc", "")
            json.put("productName", "")
            json.put("productStage", "")
            json.put("productSeq", 0)
        }
        db.close()
        return json
    }

    fun saveStageData(ctx: Context, hash: HashMap<String, Any>): Int {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var lastId = 0
        //Insertar
        val queryInsert = "INSERT INTO ${h.TAB_RESP} (" +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_NAME}, " +
                "${h.COL_DATE}, " +
                "${h.COL_JSON}, " +
                "${h.COL_ENDPOINT}, " +
                "${h.COL_SEQUENCE}, " +
                "${h.COL_FOLIO}, " +
                "${h.COL_SYNC}" +
                ")VALUES(" +
                "${hash.get("stageId")}, " +
                "'${hash.get("name")}', " +
                "'${hash.get("date")}', " +
                "'${hash.get("json")}', " +
                "'${hash.get("endpoint")}', " +
                "${hash.get("seq")}, " +
                "'${hash.get("folio")}', " +
                "0" +
                ");"
        db.execSQL(queryInsert)

        val lastIdQry = "SELECT last_insert_rowid();"
        var c = db.rawQuery(lastIdQry, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                lastId = c.getInt(0)
            }while (c.moveToNext())
        }
        db.close()
        return lastId
    }

    fun saveUserData(ctx: Context, hash: HashMap<String, Any>) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "INSERT INTO ${h.TAB_USER} (" +
                "${h.COL_USER_ID}, " +
                "${h.COL_USER_NAME}, " +
                "${h.COL_NAME}, " +
                "${h.COL_APAT}, " +
                "${h.COL_AMAT}, " +
                "${h.COL_EMAIL}, " +
                "${h.COL_PHONE}, " +
                "${h.COL_TOKEN}, " +
                "${h.COL_PASS}" +
                ")VALUES(" +
                "${hash.get("userId")}, " +
                "'${hash.get("userName")}', " +
                "'${hash.get("name")}', " +
                "'${hash.get("apat")}', " +
                "'${hash.get("amat")}', " +
                "'${hash.get("email")}', " +
                "'${hash.get("phone")}', " +
                "'${hash.get("token")}', " +
                "'${hash.get("pass")}'" +
                ");"
        db.execSQL(query)
        db.close()
    }

    fun updateUserData(ctx: Context, hash: HashMap<String, Any>) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "UPDATE ${h.TAB_USER} SET " +
                "${h.COL_USER_NAME} = '${hash.get("userName")}', " +
                "${h.COL_NAME} = '${hash.get("name")}', " +
                "${h.COL_APAT} = '${hash.get("apat")}', " +
                "${h.COL_AMAT} = '${hash.get("amat")}', " +
                "${h.COL_EMAIL} = '${hash.get("email")}', " +
                "${h.COL_PHONE} = '${hash.get("phone")}', " +
                "${h.COL_TOKEN} = '${hash.get("token")}', " +
                "${h.COL_PASS} = '${hash.get("pass")}' " +
                "WHERE ${h.COL_USER_ID} = ${hash.get("userId")};"
        db.execSQL(query)
        db.close()
    }

    fun login(ctx: Context, hash: HashMap<String, String>): Boolean {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var login: Boolean = false
        val query = "SELECT COUNT (1) " +
                "FROM ${h.TAB_USER} " +
                "WHERE ${h.COL_USER_NAME} = '${hash.get("user")}' " +
                "AND ${h.COL_PASS} = '${hash.get("pass")}'"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                login = if (c.getInt(0) == 1) true else false
            }while (c.moveToNext())
        }
        db.close()
        return login
    }

    fun getOfflineResponses(ctx: Context): ReqCollection {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var rc = ReqCollection()
        val query = "SELECT " +
                "${h.COL_ID}, " +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_NAME}, " +
                "${h.COL_JSON}, " +
                "${h.COL_ENDPOINT}, " +
                "${h.COL_FOLIO} " +
                "FROM ${h.TAB_RESP} " +
                "WHERE ${h.COL_SYNC} = 0 " +
                "AND NOT ${h.COL_FOLIO} = '' " +
                "ORDER BY ${h.COL_SEQUENCE}"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                var rr = RequestResponse()
                rr.id       = c.getInt(0)
                rr.stageId  = c.getInt(1)
                rr.name     = c.getString(2)
                rr.json     = c.getString(3)
                rr.endpoint = c.getString(4)
                rr.folio    = c.getString(5)

                rc.add(rr)
            }while (c.moveToNext())
        }
        db.close()
        return rc
    }

    fun saveStagesData(hash: HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase

        var parser = JsonParser()//Parser
        var obj = parser.parse(hash.get("jsonString").toString()).asJsonObject//Convertir string de sqlite en objeto JSON
        if (obj.get("Products") != null){
            var arr = parser.parse(obj.get("Products").toString()).asJsonArray//Convertir string de sqlite en array JSON (PRODUCTS)
            arr.forEach {//Ciclar array JSON
                var wf = parser.parse(it.toString()).asJsonObject//Obtener workflow

                var wfArr = parser.parse(wf.get("WorkFlows").toString()).asJsonArray//obtener contenido de workflow
                wfArr.forEach {//Ciclar contenido de workflow
                    var wfContent = parser.parse(it.toString()).asJsonObject//Acceder a objetos dentro de workflow
                    var stages = parser.parse(wfContent.get("Stages").toString()).asJsonArray// acceder a Stages
                    stages.forEach {
                        val query = "INSERT INTO ${h.TAB_PRODUCTS} (" +
                                "${h.COL_STAGE_ID}, " +
                                "${h.COL_PRODUCT_ID}, " +
                                "${h.COL_PRODUCT_NAME}, " +
                                "${h.COL_PRODUCT_DESC}, " +
                                "${h.COL_PRODUCT_STAGE}" +
                                ")VALUES(" +
                                "${hash.get("stageId")}, " +
                                "${wf.get("Id")}, " +
                                "${wf.get("Name")}, " +
                                "${wf.get("Description")}, " +
                                "'${it}'" +
                                ");"
                        db.execSQL(query)

                        val update = "UPDATE ${h.TAB_JSON} " +
                                "SET ${h.COL_PRODUCT} = 1 " +
                                "WHERE ${h.COL_STAGE_ID} = ${hash.get("stageId")};"
                        db.execSQL(update)
                    }
                }
            }
        }
        db.close()
    }

    fun updateStagesData(hash: java.util.HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase

        var parser = JsonParser()//Parser
        var obj = parser.parse(hash.get("jsonString").toString()).asJsonObject//Convertir string de sqlite en objeto JSON
        if (obj.get("Products") != null){
            var arr = parser.parse(obj.get("Products").toString()).asJsonArray//Convertir string de sqlite en array JSON (PRODUCTS)
            arr.forEach {//Ciclar array JSON
                var wf = parser.parse(it.toString()).asJsonObject//Obtener workflow

                var wfArr = parser.parse(wf.get("WorkFlows").toString()).asJsonArray//obtener contenido de workflow
                wfArr.forEach {//Ciclar contenido de workflow
                    var wfContent = parser.parse(it.toString()).asJsonObject//Acceder a objetos dentro de workflow
                    var stages = parser.parse(wfContent.get("Stages").toString()).asJsonArray// acceder a Stages
                    stages.forEach {
                        val query = "UPDATE ${h.TAB_PRODUCTS} SET " +
                                "${h.COL_STAGE_ID} = ${hash.get("stageId")}, " +
                                "${h.COL_PRODUCT_NAME} = ${wf.get("Name")}, " +
                                "${h.COL_PRODUCT_DESC} = ${wf.get("Description")}, " +
                                "${h.COL_PRODUCT_STAGE} = '${it}' " +
                                "WHERE ${h.COL_ID} = ${wf.get("Id")};"
                        db.execSQL(query)
                    }
                }
            }
        }
        db.close()
    }
    //Persistence
    fun getListPersistence(ctx: Context): ArrayList<HashMap<String, Any>> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var list: ArrayList<HashMap<String, Any>> = ArrayList()
        val query = "SELECT " +
                "${h.COL_PERS_INVOICE}, " +
                "${h.COL_PERS_IPS} " +
                "FROM ${h.TAB_PERSISTENCE} " +
                "WHERE pers_status = 1;"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                var hash = HashMap<String, Any>()
                hash.put("folio", c.getString(0))
                hash.put("ips", c.getInt(1))
                list.add(hash)
            }while (c.moveToNext())
        }
        db.close()
        return list
    }

    fun setPersistenceOffline(
        ips: String,//idproductstage
        nps: String, //nameproductstage
        ip: String, //idproduct
        iv: String, //invoice(Folio)
        ctx: Context
    ) {
        var productStageData = getActualProductStage(ip, ctx)
        var lastStageData = getLastProductStage(productStageData.get("productStageId") as Int, ctx)
        var exists = 0
        if (!OfflineSingleton.invoice.equals("")){
            exists = getExistentProduct(OfflineSingleton.invoice, ctx)
        }

        var actualId = (productStageData.get("id").toString()).toInt()
        var lastId = lastStageData

        var hash = HashMap<String, Any>()
        hash.put("ips", ips)
        hash.put("nps", nps)
        hash.put("ip", ip)
        hash.put("iv", OfflineSingleton.invoice)
        hash.put("actualReg", actualId)
        hash.put("lastReg", lastId)

        if (actualId.equals(lastId)){
            //update estatus de la tabla a 0
            updatePersistenceStatus(OfflineSingleton.invoice, ctx)
        }else{
            if (exists.equals(1)){
                //Hacemos update
                updatePersistence(hash, ctx)
            }else{
                //Hacemos insert
                insertPersistence(hash, ctx)
            }
        }
    }

    private fun updatePersistenceStatus(iv: String, ctx: Context){
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "UPDATE ${h.TAB_PERSISTENCE} SET " +
                "${h.COL_PERS_STATUS} = 0 " +
                "WHERE ${h.COL_PERS_INVOICE} = '$iv';"
        db.execSQL(query)
    }

    private fun updatePersistence(hash: java.util.HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "UPDATE ${h.TAB_PERSISTENCE} SET " +
                "${h.COL_PERS_IPS} = ${hash.get("ips")}, " +
                "${h.COL_PERS_NPS} = '${hash.get("nps")}', " +
                "${h.COL_PERS_IP} = ${hash.get("ip")}, " +
                "${h.COL_PERS_ACTUALREG} = ${hash.get("actualReg")}, " +
                "${h.COL_PERS_LASTREGID} = ${hash.get("lastReg")} " +
                "WHERE ${h.COL_PERS_INVOICE} = '${hash.get("iv")}';"
        db.execSQL(query)
    }

    private fun insertPersistence(hash: HashMap<String, Any>, ctx: Context){
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "INSERT INTO ${h.TAB_PERSISTENCE}(" +
                "${h.COL_PERS_IPS}, " +
                "${h.COL_PERS_NPS}, " +
                "${h.COL_PERS_IP}, " +
                "${h.COL_PERS_INVOICE}, " +
                "${h.COL_PERS_ACTUALREG}, " +
                "${h.COL_PERS_LASTREGID}, " +
                "${h.COL_PERS_STATUS} " +
                ")VALUES(" +
                "${hash.get("ips")}, " +
                "'${hash.get("nps")}', " +
                "${hash.get("ip")}, " +
                "'${hash.get("iv")}', " +
                "${hash.get("actualReg")}, " +
                "${hash.get("lastReg")}, " +
                "1" +
                ");"
        db.execSQL(query)
    }

    private fun getActualProductStage(ip: String, ctx: Context): HashMap<String, Any>{
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var hash = HashMap<String, Any>()
        val query = "SELECT " +
                "${h.COL_ID}, " +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_STAGE_DESC}, " +
                "${h.COL_GROUP_NAME}, " +
                "${h.COL_GROUP_DESC}, " +
                "${h.COL_GROUP_ID}, " +
                "${h.COL_PRODUCT_STAGE_ID}, " +
                "${h.COL_PRODUCT_STAGE_NAME}, " +
                "${h.COL_PRODUCT_ID}, " +
                "${h.COL_PRODUCT_SEQ}, " +
                "${h.COL_PRODUCT_NAME}, " +
                "${h.COL_PRODUCT_DESC}, " +
                "${h.COL_PRODUCT_STAGE} " +
                "FROM ${h.TAB_PRODUCTS} " +
                "WHERE ${h.COL_PRODUCT_ID} = $ip"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                hash.put("id", c.getInt(0))
                hash.put("stageId", c.getInt(1))
                hash.put("stageDesc", c.getString(2))
                hash.put("groupName", c.getString(3))
                hash.put("groupDesc", c.getString(4))
                hash.put("groupId", c.getInt(5))
                hash.put("productStageId", c.getInt(6))
                hash.put("productStageName", c.getString(7))
                hash.put("productId", c.getInt(8))
                hash.put("productSeq", c.getInt(9))
                hash.put("productName", c.getString(10))
                hash.put("productDesc", c.getString(11))
                hash.put("productStage", c.getString(12))
            }while (c.moveToNext())
        }else{
            hash.put("id", 0)
            hash.put("stageId", 0)
            hash.put("stageDesc", "")
            hash.put("groupName", "")
            hash.put("groupDesc", "")
            hash.put("groupId", 0)
            hash.put("productStageId", 0)
            hash.put("productStageName", "")
            hash.put("productId", 0)
            hash.put("productSeq", 0)
            hash.put("productName", "")
            hash.put("productDesc", "")
            hash.put("productStage", "")
        }
        db.close()
        return hash
    }

    private fun getLastProductStage(actualStage: Int, ctx: Context): Int{
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var lastId = 0
        val query = "SELECT " +
                "${h.COL_ID} " +
                "FROM ${h.TAB_PRODUCTS} " +
                "WHERE ${h.COL_PRODUCT_STAGE_ID} = $actualStage " +
                "ORDER BY ${h.COL_PRODUCT_SEQ} " +
                "DESC LIMIT 1"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                lastId = c.getInt(0)
            }while (c.moveToNext())
        }
        db.close()
        return lastId
    }

    private fun getExistentProduct(invoice: String, ctx: Context): Int{
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var exists = 0
        val query = "SELECT " +
                "COUNT(1) " +
                "FROM ${h.TAB_PERSISTENCE} " +
                "WHERE ${h.COL_PERS_INVOICE} = '$invoice';"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                exists = c.getInt(0)
            }while (c.moveToNext())
        }
        db.close()
        return exists
    }

    fun getPersistenceDetail(invoice: String, ctx: Context): HashMap<String, Any> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var hash = HashMap<String, Any>()
        val query = "SELECT " +
                "${h.COL_ID}, " +
                "${h.COL_PERS_IPS}, " +
                "${h.COL_PERS_NPS}, " +
                "${h.COL_PERS_IP}, " +
                "${h.COL_PERS_ACTUALREG}, " +
                "${h.COL_PERS_LASTREGID} " +
                "FROM " +
                "${h.TAB_PERSISTENCE} " +
                "WHERE ${h.COL_PERS_INVOICE} = '$invoice';"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                hash.put("id", c.getInt(0))
                hash.put("ips", c.getInt(1))
                hash.put("nps", c.getString(2))
                hash.put("ip", c.getInt(3))
                hash.put("actualReg", c.getInt(4))
                hash.put("lastReg", c.getInt(5))
            }while (c.moveToNext())
        }
        db.close()
        return hash
    }

    fun getPendant(ips: Int, ip: Int, ctx: Context): java.util.HashMap<String, Any> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        var json = HashMap<String, Any>()
        val query = "SELECT " +
                "${h.COL_PRODUCT_STAGE_ID}, " +
                "${h.COL_PRODUCT_STAGE_NAME}, " +
                "${h.COL_PRODUCT_ID}, " +
                "${h.COL_PRODUCT_DESC}, " +
                "${h.COL_PRODUCT_NAME}, " +
                "${h.COL_PRODUCT_STAGE}, " +
                "${h.COL_PRODUCT_SEQ} " +
                "FROM ${h.TAB_PRODUCTS} " +
                "WHERE ${h.COL_PRODUCT_STAGE_ID} = $ips " +
                "AND ${h.COL_PRODUCT_ID} = $ip " +
                "ORDER BY ${h.COL_PRODUCT_SEQ} " +
                "ASC LIMIT 1;"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                json.put("productStageId", c.getInt(0))
                json.put("productStageName", c.getString(1))
                json.put("productId", c.getInt(2))
                json.put("productDesc", c.getString(3))
                json.put("productName", c.getString(4))
                json.put("productStage", c.getString(5))
                json.put("productSeq", c.getInt(6))
            }while (c.moveToNext())
        }else{
            json.put("productStageId", 0)
            json.put("productStageName", "")
            json.put("productId", 0)
            json.put("productDesc", "")
            json.put("productName", "")
            json.put("productStage", "")
            json.put("productSeq", 0)
        }
        db.close()
        return json
    }
}