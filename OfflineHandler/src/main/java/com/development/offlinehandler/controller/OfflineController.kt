package com.development.offlinehandler.controller

import android.content.Context
import android.util.Log
import com.development.offlinehandler.model.DBHelper
import com.development.offlinehandler.model.ReqCollection
import com.development.offlinehandler.model.RequestResponse
import java.util.*

internal class OfflineController {
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
    }

    fun getApiData(ctx: Context, stage: String, idLocalStage: Int): HashMap<String, Any> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        //Get Stages
        var json = HashMap<String, Any>()
        var query = if (stage == "0"){
            "SELECT " +
                    "${h.COL_STAGE_ID}, " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON}, " +
                    "${h.COL_ID} " +
                    "FROM ${h.TAB_JSON} " +
                    "ORDER BY ${h.COL_SEQUENCE} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${h.COL_STAGE_ID}, " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON}, " +
                    "${h.COL_ID} " +
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
            }while (c.moveToNext())
        }else{
            json.put("stageId", 0)
            json.put("name", "")
            json.put("json", "")
            json.put("id", 0)
        }
        db.close()
        return json
    }

    fun saveStageData(ctx: Context, hash: HashMap<String, Any>) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
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
        db.close()
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
                "AND NOT ${h.COL_FOLIO} = ''"
        Log.e("Query::: ", query)
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
}