package com.development.offlinehandler.controller

import android.content.Context
import com.development.offlinehandler.model.DBHelper
import java.util.HashMap

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

    fun insertApiData(jsonModel: HashMap<String, Any>, ctx: Context) {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        val query = "INSERT INTO ${h.TAB_JSON} (" +
                "${h.COL_STAGE_ID}, " +
                "${h.COL_DATE}, " +
                "${h.COL_NAME}, " +
                "${h.COL_SEQUENCE}, " +
                "${h.COL_JSON}" +
                ")VALUES(" +
                "${jsonModel.get("stageId")}, " +
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
                "${h.COL_DATE} = '${jsonModel.get("dateTime")}', " +
                "${h.COL_NAME} = '${jsonModel.get("name")}', " +
                "${h.COL_SEQUENCE} = ${jsonModel.get("sequence")}, " +
                "${h.COL_JSON} = '${jsonModel.get("jsonString")}' " +
                "WHERE ${h.COL_STAGE_ID} = ${jsonModel.get("stageId")};"
        db.execSQL(query)
    }

    fun getApiData(ctx: Context, stage: String): HashMap<String, String> {
        val h = DBHelper(ctx)
        val db = h.writableDatabase
        //Get Stages
        var json = HashMap<String, String>()
        var query = if (stage == "0"){
            "SELECT " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON} " +
                    "FROM ${h.TAB_JSON} " +
                    "ORDER BY ${h.COL_SEQUENCE} " +
                    "ASC LIMIT 1;"
        }else{
            "SELECT " +
                    "${h.COL_NAME}, " +
                    "${h.COL_JSON} " +
                    "FROM ${h.TAB_JSON} " +
                    "WHERE ${h.COL_SEQUENCE} > (" +
                    "SELECT ${h.COL_SEQUENCE} FROM ${h.TAB_JSON} WHERE ${h.COL_NAME} = '${stage}') " +
                    "ORDER BY ${h.COL_STAGE_ID} " +
                    "DESC LIMIT 1;"
        }
        var c = db.rawQuery(query, null)
        if (c.count > 0){
            c.moveToFirst()
            do {
                json.put("name", c.getString(0))
                json.put("json", c.getString(1))
            }while (c.moveToNext())
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
    }
}