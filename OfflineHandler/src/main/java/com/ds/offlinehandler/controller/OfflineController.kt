package com.ds.offlinehandler.controller

import com.ds.offlinehandler.OfflineSingleton
import com.ds.offlinehandler.model.*
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlin.collections.HashMap

internal class OfflineController {
    internal fun dataStorage(
        body: ApiRequestGetData
    ) {
        var jsonModel: HashMap<String, Any> = HashMap()
        var gson = Gson()
        var bodyArr = body.Body

        if (bodyArr != null){
            if (bodyArr.size > 0){
                //Respuesta del workflow correcta
                for (i in 0 until bodyArr.size) {
                    var stages = bodyArr[i].Stages
                    for (j in 0 until stages.size) {
                        var verExt = verifyExistence(stages[j].Id, OfflineSingleton.h!!.TAB_JSON, OfflineSingleton.h!!.COL_STAGE_ID)
                        jsonModel.put("bodyId", bodyArr[i].Id)
                        jsonModel.put("bodyName", bodyArr[i].Name)
                        jsonModel.put("stageId", stages[j].Id)
                        jsonModel.put("dateTime", OfflineSingleton.misc!!.dateTime())
                        jsonModel.put("name", stages[j].Name)
                        jsonModel.put("sequence", stages[j].Sequence)
                        var json = gson.toJson(stages[j])
                        jsonModel.put("jsonString", json)

                        if (verExt == 1){
                            //Sí existe, hacemos update
                            updateApiData(jsonModel)
                        }else{
                            //No existe, hacemos insert
                            insertApiData(jsonModel)
                        }
                        productStorage(json)
                    }
                }
            }else{
                return
            }
        }else{
            return
        }
    }

    private fun productStorage(json: String) {
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
                            OfflineSingleton.h!!.TAB_PRODUCTS,
                            OfflineSingleton.h!!.COL_PRODUCT_ID)
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
                                updateProductData(jsonModel)
                            }else{
                                //No existe, hacemos insert
                                insertProductData(jsonModel)
                            }
                        }
                    }
                }
            }
        }
    }

    fun verifyExistence(id: Int, table: String, evaluate: String): Int{
        val db = OfflineSingleton.h!!.writableDatabase
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

    fun exists(): Int{
        var db = OfflineSingleton.h!!.writableDatabase
        var existe = 0
        val query = "SELECT COUNT(1) FROM ${OfflineSingleton.h!!.TAB_JSON}"
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

    fun userExists(userId: Int): Int {
        var db = OfflineSingleton.h!!.writableDatabase
        var existe = 0
        val query = "SELECT COUNT(1) FROM ${OfflineSingleton.h!!.TAB_USER} WHERE ${OfflineSingleton.h!!.COL_USER_ID} = $userId;"
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

    fun insertApiData(jsonModel: HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "INSERT INTO ${OfflineSingleton.h!!.TAB_JSON} (" +
                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_BODY_ID}, " +
                "${OfflineSingleton.h!!.COL_BODY_NAME}, " +
                "${OfflineSingleton.h!!.COL_DATE}, " +
                "${OfflineSingleton.h!!.COL_NAME}, " +
                "${OfflineSingleton.h!!.COL_SEQUENCE}, " +
                "${OfflineSingleton.h!!.COL_JSON}" +
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

    fun updateApiData(jsonModel: HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "UPDATE ${OfflineSingleton.h!!.TAB_JSON} SET " +
                "${OfflineSingleton.h!!.COL_BODY_ID} = ${jsonModel.get("bodyId")}, " +
                "${OfflineSingleton.h!!.COL_BODY_NAME} = '${jsonModel.get("bodyName")}', " +
                "${OfflineSingleton.h!!.COL_DATE} = '${jsonModel.get("dateTime")}', " +
                "${OfflineSingleton.h!!.COL_NAME} = '${jsonModel.get("name")}', " +
                "${OfflineSingleton.h!!.COL_SEQUENCE} = ${jsonModel.get("sequence")}, " +
                "${OfflineSingleton.h!!.COL_JSON} = '${jsonModel.get("jsonString")}' " +
                "WHERE ${OfflineSingleton.h!!.COL_STAGE_ID} = ${jsonModel.get("stageId")};"
        db.execSQL(query)
        db.close()
    }

    private fun insertProductData(hash: java.util.HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "INSERT INTO ${OfflineSingleton.h!!.TAB_PRODUCTS} (" +
                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_STAGE_NAME}, " +
                "${OfflineSingleton.h!!.COL_STAGE_DESC}, " +
                "${OfflineSingleton.h!!.COL_GROUP_NAME}, " +
                "${OfflineSingleton.h!!.COL_GROUP_DESC}, " +
                "${OfflineSingleton.h!!.COL_GROUP_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_SEQ}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE}" +
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
        updateStatusJson(stagevalue.toInt())
        db.close()
    }

    private fun updateProductData(hash: java.util.HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "UPDATE ${OfflineSingleton.h!!.TAB_PRODUCTS} SET " +
                "${OfflineSingleton.h!!.COL_STAGE_ID} = ${hash.get("idStage")}, " +
                "${OfflineSingleton.h!!.COL_STAGE_NAME} = '${hash.get("nameStage")}', " +
                "${OfflineSingleton.h!!.COL_STAGE_DESC} = '${hash.get("descStage")}', " +
                "${OfflineSingleton.h!!.COL_GROUP_NAME} = '${hash.get("groupName")}', " +
                "${OfflineSingleton.h!!.COL_GROUP_DESC} = '${hash.get("groupDesc")}', " +
                "${OfflineSingleton.h!!.COL_GROUP_ID} = ${hash.get("groupId")}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID} = ${hash.get("producStageId")}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME} = ${hash.get("productStageName")}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_SEQ} = ${hash.get("productSequence")}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_NAME} = '${hash.get("productName")}', " +
                "${OfflineSingleton.h!!.COL_PRODUCT_DESC} = '${hash.get("productDesc")}', " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE} = '${hash.get("productStageData")}' " +
                "WHERE ${OfflineSingleton.h!!.COL_PRODUCT_ID} = ${hash.get("productId")};"
        db.execSQL(query)

        var stagevalue = hash.get("idStage").toString()
        updateStatusJson(stagevalue.toInt())
        db.close()
    }

    private fun updateStatusJson(idStage: Int) {
        val db = OfflineSingleton.h!!.writableDatabase
        val update = "UPDATE ${OfflineSingleton.h!!.TAB_JSON} " +
                "SET ${OfflineSingleton.h!!.COL_PRODUCT} = 1 " +
                "WHERE ${OfflineSingleton.h!!.COL_STAGE_ID} = $idStage;"
        db.execSQL(update)
        db.close()
    }

    fun getApiData(stage: String, idLocalStage: Int): HashMap<String, Any> {
        val db = OfflineSingleton.h!!.writableDatabase
        //Get Stages
        var json = HashMap<String, Any>()
        var query: String = if (stage == "0") {
            "SELECT " +
                    "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                    "${OfflineSingleton.h!!.COL_NAME}, " +
                    "${OfflineSingleton.h!!.COL_JSON}, " +
                    "${OfflineSingleton.h!!.COL_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT} " +
                    "FROM ${OfflineSingleton.h!!.TAB_JSON} " +
                    "ORDER BY ${OfflineSingleton.h!!.COL_SEQUENCE} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                    "${OfflineSingleton.h!!.COL_NAME}, " +
                    "${OfflineSingleton.h!!.COL_JSON}, " +
                    "${OfflineSingleton.h!!.COL_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT} " +
                    "FROM ${OfflineSingleton.h!!.TAB_JSON} " +
                    "WHERE ${OfflineSingleton.h!!.COL_ID} > " +
                    "(SELECT ${OfflineSingleton.h!!.COL_ID} FROM ${OfflineSingleton.h!!.TAB_JSON} WHERE ${OfflineSingleton.h!!.COL_NAME} = '$stage' AND ${OfflineSingleton.h!!.COL_STAGE_ID} = $idLocalStage) " +
                    "ORDER BY ${OfflineSingleton.h!!.COL_ID} " +
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
        productId: String,
        productName: String,
        productStageId: String
    ): HashMap<String, Any> {
        val db = OfflineSingleton.h!!.writableDatabase
        //Get Stages
        var json = HashMap<String, Any>()
        var query: String = if (productName == "0") {
            "SELECT " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
                    "FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} " +
                    "WHERE ${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID} = $productStageId " +
                    "ORDER BY ${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_STAGE}, " +
                    "${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
                    "FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} " +
                    "WHERE ${OfflineSingleton.h!!.COL_ID} > " +
                    "(SELECT ${OfflineSingleton.h!!.COL_ID} FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} WHERE ${OfflineSingleton.h!!.COL_PRODUCT_NAME} = '$productName' AND ${OfflineSingleton.h!!.COL_PRODUCT_ID} = $productId) " +
                    "AND ${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID} = $productStageId " +
                    "ORDER BY ${OfflineSingleton.h!!.COL_ID} " +
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

    fun saveStageData(hash: HashMap<String, Any>): Int {
        val db = OfflineSingleton.h!!.writableDatabase
        var lastId = 0
        //Insertar
        val queryInsert = "INSERT INTO ${OfflineSingleton.h!!.TAB_RESP} (" +
                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_NAME}, " +
                "${OfflineSingleton.h!!.COL_DATE}, " +
                "${OfflineSingleton.h!!.COL_JSON}, " +
                "${OfflineSingleton.h!!.COL_ENDPOINT}, " +
                "${OfflineSingleton.h!!.COL_SEQUENCE}, " +
                "${OfflineSingleton.h!!.COL_FOLIO}, " +
                "${OfflineSingleton.h!!.COL_SYNC}" +
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

    fun saveUserData(hash: HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "INSERT INTO ${OfflineSingleton.h!!.TAB_USER} (" +
                "${OfflineSingleton.h!!.COL_USER_ID}, " +
                "${OfflineSingleton.h!!.COL_USER_NAME}, " +
                "${OfflineSingleton.h!!.COL_NAME}, " +
                "${OfflineSingleton.h!!.COL_APAT}, " +
                "${OfflineSingleton.h!!.COL_AMAT}, " +
                "${OfflineSingleton.h!!.COL_EMAIL}, " +
                "${OfflineSingleton.h!!.COL_PHONE}, " +
                "${OfflineSingleton.h!!.COL_TOKEN}, " +
                "${OfflineSingleton.h!!.COL_PASS}, " +
                "${OfflineSingleton.h!!.COL_RESPONSE}, " +
                "${OfflineSingleton.h!!.COL_ROLENAME}, " +
                "${OfflineSingleton.h!!.COL_CURRENT_FILE}" +
                ")VALUES(" +
                "${hash.get("userId")}, " +
                "'${hash.get("userName")}', " +
                "'${hash.get("name")}', " +
                "'${hash.get("apat")}', " +
                "'${hash.get("amat")}', " +
                "'${hash.get("email")}', " +
                "'${hash.get("phone")}', " +
                "'${hash.get("token")}', " +
                "'${hash.get("pass")}', " +
                "'${hash.get("response")}', " +
                "'${hash.get("rolename")}', " +
                "'${hash.get("currentfile")}'" +
                ");"
        db.execSQL(query)
        db.close()
    }

    fun updateUserData(hash: HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "UPDATE ${OfflineSingleton.h!!.TAB_USER} SET " +
                "${OfflineSingleton.h!!.COL_USER_NAME} = '${hash.get("userName")}', " +
                "${OfflineSingleton.h!!.COL_NAME} = '${hash.get("name")}', " +
                "${OfflineSingleton.h!!.COL_APAT} = '${hash.get("apat")}', " +
                "${OfflineSingleton.h!!.COL_AMAT} = '${hash.get("amat")}', " +
                "${OfflineSingleton.h!!.COL_EMAIL} = '${hash.get("email")}', " +
                "${OfflineSingleton.h!!.COL_PHONE} = '${hash.get("phone")}', " +
                "${OfflineSingleton.h!!.COL_TOKEN} = '${hash.get("token")}', " +
                "${OfflineSingleton.h!!.COL_PASS} = '${hash.get("pass")}', " +
                "${OfflineSingleton.h!!.COL_RESPONSE} = '${hash.get("response")}', " +
                "${OfflineSingleton.h!!.COL_ROLENAME} = '${hash.get("rolename")}', " +
                "${OfflineSingleton.h!!.COL_CURRENT_FILE} = '${hash.get("currentfile")}' " +
                "WHERE ${OfflineSingleton.h!!.COL_USER_ID} = ${hash.get("userId")};"
        db.execSQL(query)
        db.close()
    }

    fun login(hash: HashMap<String, String>): HashMap<String, Any> {
        val db = OfflineSingleton.h!!.writableDatabase
        var respHash = HashMap<String, Any>()

        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_ID}, " +
                "${OfflineSingleton.h!!.COL_USER_ID}, " +
                "${OfflineSingleton.h!!.COL_USER_NAME}, " +
                "${OfflineSingleton.h!!.COL_NAME}, " +
                "${OfflineSingleton.h!!.COL_APAT}, " +
                "${OfflineSingleton.h!!.COL_AMAT}, " +
                "${OfflineSingleton.h!!.COL_EMAIL}, " +
                "${OfflineSingleton.h!!.COL_PHONE}, " +
                "${OfflineSingleton.h!!.COL_TOKEN}, " +
                "${OfflineSingleton.h!!.COL_PASS}, " +
                "${OfflineSingleton.h!!.COL_RESPONSE}," +
                "${OfflineSingleton.h!!.COL_ROLENAME}, " +
                "${OfflineSingleton.h!!.COL_CURRENT_FILE} " +
                "FROM ${OfflineSingleton.h!!.TAB_USER} " +
                "WHERE ${OfflineSingleton.h!!.COL_USER_NAME} = '${hash.get("user")}' " +
                "AND ${OfflineSingleton.h!!.COL_PASS} = '${hash.get("pass")}'"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                respHash.put("id", c.getInt(0))
                respHash.put("userId", c.getInt(1))
                respHash.put("userName", c.getString(2))
                respHash.put("name", c.getString(3))
                respHash.put("apat", c.getString(4))
                respHash.put("amat", c.getString(5))
                respHash.put("email", c.getString(6))
                respHash.put("phone", c.getString(7))
                respHash.put("token", c.getString(8))
                respHash.put("pass", c.getString(9))
                respHash.put("response", c.getString(10))
                respHash.put("rolename", c.getString(11))
                respHash.put("currentfile", c.getString(12))
                respHash.put("isLogged", true)
            }while (c.moveToNext())
        }else{
            respHash.put("id", 0)
            respHash.put("userId", 0)
            respHash.put("userName", "")
            respHash.put("name", "")
            respHash.put("apat", "")
            respHash.put("amat", "")
            respHash.put("email", "")
            respHash.put("phone", "")
            respHash.put("token", "")
            respHash.put("pass", "")
            respHash.put("response", "")
            respHash.put("rolename", "")
            respHash.put("currentfile", "")
            respHash.put("isLogged", false)
        }
        db.close()
        return respHash
    }

    fun getOfflineResponses(): ReqCollection {
        val db = OfflineSingleton.h!!.writableDatabase
        var rc = ReqCollection()
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_ID}, " +
                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_NAME}, " +
                "${OfflineSingleton.h!!.COL_JSON}, " +
                "${OfflineSingleton.h!!.COL_ENDPOINT}, " +
                "${OfflineSingleton.h!!.COL_FOLIO} " +
                "FROM ${OfflineSingleton.h!!.TAB_RESP} " +
                "WHERE ${OfflineSingleton.h!!.COL_SYNC} = 0 " +
                "AND NOT ${OfflineSingleton.h!!.COL_FOLIO} = '' " +
                "ORDER BY ${OfflineSingleton.h!!.COL_SEQUENCE}"
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

    fun saveStagesData(hash: HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase

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
                        val query = "INSERT INTO ${OfflineSingleton.h!!.TAB_PRODUCTS} (" +
                                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE}" +
                                ")VALUES(" +
                                "${hash.get("stageId")}, " +
                                "${wf.get("Id")}, " +
                                "${wf.get("Name")}, " +
                                "${wf.get("Description")}, " +
                                "'${it}'" +
                                ");"
                        db.execSQL(query)

                        val update = "UPDATE ${OfflineSingleton.h!!.TAB_JSON} " +
                                "SET ${OfflineSingleton.h!!.COL_PRODUCT} = 1 " +
                                "WHERE ${OfflineSingleton.h!!.COL_STAGE_ID} = ${hash.get("stageId")};"
                        db.execSQL(update)
                    }
                }
            }
        }
        db.close()
    }

    fun updateStagesData(hash: java.util.HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase

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
                        val query = "UPDATE ${OfflineSingleton.h!!.TAB_PRODUCTS} SET " +
                                "${OfflineSingleton.h!!.COL_STAGE_ID} = ${hash.get("stageId")}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_NAME} = ${wf.get("Name")}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_DESC} = ${wf.get("Description")}, " +
                                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE} = '${it}' " +
                                "WHERE ${OfflineSingleton.h!!.COL_ID} = ${wf.get("Id")};"
                        db.execSQL(query)
                    }
                }
            }
        }
        db.close()
    }
    //Persistence
    fun getListPersistence(): ArrayList<HashMap<String, Any>> {
        val db = OfflineSingleton.h!!.writableDatabase
        var list: ArrayList<HashMap<String, Any>> = ArrayList()
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_PERS_INVOICE}, " +
                "${OfflineSingleton.h!!.COL_PERS_IPS} " +
                "FROM ${OfflineSingleton.h!!.TAB_PERSISTENCE} " +
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
        iv: String //invoice(Folio)
    ) {
        var productStageData = getActualProductStage(ip)
        var lastStageData = getLastProductStage(productStageData.get("productStageId") as Int)
        var exists = 0
        if (!OfflineSingleton.invoice.equals("")){
            exists = getExistentProduct(OfflineSingleton.invoice)
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
            updatePersistenceStatus(OfflineSingleton.invoice)
        }else{
            if (exists.equals(1)){
                //Hacemos update
                updatePersistence(hash)
            }else{
                //Hacemos insert
                insertPersistence(hash)
            }
        }
    }

    private fun updatePersistenceStatus(iv: String){
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "UPDATE ${OfflineSingleton.h!!.TAB_PERSISTENCE} SET " +
                "${OfflineSingleton.h!!.COL_PERS_STATUS} = 0 " +
                "WHERE ${OfflineSingleton.h!!.COL_PERS_INVOICE} = '$iv';"
        db.execSQL(query)
    }

    private fun updatePersistence(hash: java.util.HashMap<String, Any>) {
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "UPDATE ${OfflineSingleton.h!!.TAB_PERSISTENCE} SET " +
                "${OfflineSingleton.h!!.COL_PERS_IPS} = ${hash.get("ips")}, " +
                "${OfflineSingleton.h!!.COL_PERS_NPS} = '${hash.get("nps")}', " +
                "${OfflineSingleton.h!!.COL_PERS_IP} = ${hash.get("ip")}, " +
                "${OfflineSingleton.h!!.COL_PERS_ACTUALREG} = ${hash.get("actualReg")}, " +
                "${OfflineSingleton.h!!.COL_PERS_LASTREGID} = ${hash.get("lastReg")} " +
                "WHERE ${OfflineSingleton.h!!.COL_PERS_INVOICE} = '${hash.get("iv")}';"
        db.execSQL(query)
    }

    private fun insertPersistence(hash: HashMap<String, Any>){
        val db = OfflineSingleton.h!!.writableDatabase
        val query = "INSERT INTO ${OfflineSingleton.h!!.TAB_PERSISTENCE}(" +
                "${OfflineSingleton.h!!.COL_PERS_IPS}, " +
                "${OfflineSingleton.h!!.COL_PERS_NPS}, " +
                "${OfflineSingleton.h!!.COL_PERS_IP}, " +
                "${OfflineSingleton.h!!.COL_PERS_INVOICE}, " +
                "${OfflineSingleton.h!!.COL_PERS_ACTUALREG}, " +
                "${OfflineSingleton.h!!.COL_PERS_LASTREGID}, " +
                "${OfflineSingleton.h!!.COL_PERS_STATUS} " +
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

    private fun getActualProductStage(ip: String): HashMap<String, Any>{
        val db = OfflineSingleton.h!!.writableDatabase
        var hash = HashMap<String, Any>()
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_ID}, " +
                "${OfflineSingleton.h!!.COL_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_STAGE_DESC}, " +
                "${OfflineSingleton.h!!.COL_GROUP_NAME}, " +
                "${OfflineSingleton.h!!.COL_GROUP_DESC}, " +
                "${OfflineSingleton.h!!.COL_GROUP_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_SEQ}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE} " +
                "FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} " +
                "WHERE ${OfflineSingleton.h!!.COL_PRODUCT_ID} = $ip"
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

    private fun getLastProductStage(actualStage: Int): Int{
        val db = OfflineSingleton.h!!.writableDatabase
        var lastId = 0
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_ID} " +
                "FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} " +
                "WHERE ${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID} = $actualStage " +
                "ORDER BY ${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
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

    private fun getExistentProduct(invoice: String): Int{
        val db = OfflineSingleton.h!!.writableDatabase
        var exists = 0
        val query = "SELECT " +
                "COUNT(1) " +
                "FROM ${OfflineSingleton.h!!.TAB_PERSISTENCE} " +
                "WHERE ${OfflineSingleton.h!!.COL_PERS_INVOICE} = '$invoice';"
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

    fun getPersistenceDetail(invoice: String): HashMap<String, Any> {
        val db = OfflineSingleton.h!!.writableDatabase
        var hash = HashMap<String, Any>()
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_ID}, " +
                "${OfflineSingleton.h!!.COL_PERS_IPS}, " +
                "${OfflineSingleton.h!!.COL_PERS_NPS}, " +
                "${OfflineSingleton.h!!.COL_PERS_IP}, " +
                "${OfflineSingleton.h!!.COL_PERS_ACTUALREG}, " +
                "${OfflineSingleton.h!!.COL_PERS_LASTREGID} " +
                "FROM " +
                "${OfflineSingleton.h!!.TAB_PERSISTENCE} " +
                "WHERE ${OfflineSingleton.h!!.COL_PERS_INVOICE} = '$invoice';"
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

    fun getPendant(ips: Int, ip: Int): java.util.HashMap<String, Any> {
        val db = OfflineSingleton.h!!.writableDatabase
        var json = HashMap<String, Any>()
        val query = "SELECT " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_ID}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_DESC}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_NAME}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_STAGE}, " +
                "${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
                "FROM ${OfflineSingleton.h!!.TAB_PRODUCTS} " +
                "WHERE ${OfflineSingleton.h!!.COL_PRODUCT_STAGE_ID} = $ips " +
                "AND ${OfflineSingleton.h!!.COL_PRODUCT_ID} = $ip " +
                "ORDER BY ${OfflineSingleton.h!!.COL_PRODUCT_SEQ} " +
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

    fun isOfflineViable(): ViableCollection {
        val db = OfflineSingleton.h!!.writableDatabase
        var vc = ViableCollection()
        val query = "SELECT " +
                "(SELECT COUNT(1) FROM tab_json)AS countJson, " +
                "(SELECT COUNT(1) FROM tab_user)AS countUser, " +
                "(SELECT COUNT(1) FROM tab_products) AS countProducts;"
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToNext()
            do {
                var ov = OfflineViable()
                ov.countJson = c.getInt(0)
                ov.countUser = c.getInt(1)
                ov.countProducts = c.getInt(2)
                vc.add(ov)
            }while (c.moveToNext())
        }
        db.close()
        return vc
    }
}