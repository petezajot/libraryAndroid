package com.development.offlinehandler.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DBHelper(ctx: Context): SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION){
    companion object{
        private val DB_VERSION = 1
        private val DB_NAME = "Offline_persistance"
    }

    val TAB_JSON: String      = "tab_json"
    val TAB_RESP: String      = "tab_responses"
    val TAB_USER: String      = "tab_user"
    val COL_ID: String        = "Id"
    val COL_BODY_ID: String   = "bodyId"
    val COL_BODY_NAME: String = "bodyName"
    val COL_STAGE_ID: String  = "stage_id"
    val COL_DATE: String      = "date_time"
    val COL_NAME: String      = "name"
    val COL_SEQUENCE: String  = "sequence"
    val COL_JSON: String      = "json"
    val COL_ENDPOINT: String  = "endpoint"
    val COL_SYNC: String      = "estatus_sync"
    val COL_FOLIO: String     = "folio"
    val COL_USER_ID: String   = "user_id"
    val COL_USER_NAME: String = "user_name"
    val COL_APAT: String      = "apat"
    val COL_AMAT: String      = "amat"
    val COL_EMAIL: String     = "email"
    val COL_PASS: String      = "pass"
    val COL_PHONE: String     = "phone"
    val COL_TOKEN: String     = "token"


    override fun onCreate(db: SQLiteDatabase?) {
        offlineDataJson(db)
        offlineSaveResponses(db)
        offlineSaveUser(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TAB_JSON; ")
        db!!.execSQL("DROP TABLE IF EXISTS $TAB_RESP; ")
        db!!.execSQL("DROP TABLE IF EXISTS $TAB_USER; ")
    }

    private fun offlineSaveUser(db: SQLiteDatabase?){
        val query = "CREATE TABLE IF NOT EXISTS $TAB_USER(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_USER_ID INTEGER, " +
                "$COL_USER_NAME VARCHAR, " +
                "$COL_NAME VARCHAR, " +
                "$COL_APAT VARCHAR, " +
                "$COL_AMAT VARCHAR, " +
                "$COL_EMAIL VARCHAR, " +
                "$COL_PHONE VARCHAR, " +
                "$COL_TOKEN VARCHAR, " +
                "$COL_PASS VARCHAR" +
                ");"
        db!!.execSQL(query)
    }

    private fun offlineDataJson(db: SQLiteDatabase?){
        val query = "CREATE TABLE IF NOT EXISTS $TAB_JSON(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_BODY_ID INTEGER, " +
                "$COL_BODY_NAME VARCHAR, " +
                "$COL_STAGE_ID INTEGER, " +
                "$COL_DATE VARCHAR, " +
                "$COL_NAME VARCHAR, " +
                "$COL_SEQUENCE INTEGER, " +
                "$COL_JSON VARCHAR" +
                ");"
        db!!.execSQL(query)

        var index = "CREATE INDEX stageName ON $TAB_JSON ($COL_NAME)"
        db!!.execSQL(index)
    }

    private fun offlineSaveResponses(db: SQLiteDatabase?){
        val query = "CREATE TABLE IF NOT EXISTS $TAB_RESP(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_STAGE_ID INTEGER, " +
                "$COL_NAME VARCHAR, " +
                "$COL_DATE VARCHAR, " +
                "$COL_JSON VARCHAR, " +
                "$COL_ENDPOINT VARCHAR, " +
                "$COL_SEQUENCE INTEGER, " +
                "$COL_FOLIO VARCHAR, " +
                "$COL_SYNC INTEGER" +
                ");"
        db!!.execSQL(query)
    }


}