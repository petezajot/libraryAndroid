package com.ds.offlinehandler.model

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

internal class DBHelper(ctx: Context): SQLiteOpenHelper(ctx, DB_NAME, null, DB_VERSION){
    companion object{
        private val DB_VERSION = 1
        private val DB_NAME = "Offline_persistance"
    }

    val TAB_JSON: String               = "tab_json"
    val TAB_RESP: String               = "tab_responses"
    val TAB_USER: String               = "tab_user"
    val TAB_PRODUCTS: String           = "tab_products"
    val TAB_PERSISTENCE: String        = "tab_persistence"
    val COL_ID: String                 = "Id"
    val COL_BODY_ID: String            = "bodyId"
    val COL_BODY_NAME: String          = "bodyName"
    val COL_STAGE_ID: String           = "stage_id"
    val COL_STAGE_NAME: String         = "stage_name"
    val COL_STAGE_DESC: String         = "stage_desc"
    val COL_ROLENAME: String           = "role_name"
    val COL_CURRENT_FILE: String       = "current_file"
    val COL_PRODUCT_ID: String         = "product_id"
    val COL_PRODUCT_NAME: String       = "product_name"
    val COL_PRODUCT_DESC: String       = "product_desc"
    val COL_PRODUCT_STAGE: String      = "product_stage"
    val COL_DATE: String               = "date_time"
    val COL_NAME: String               = "name"
    val COL_SEQUENCE: String           = "sequence"
    val COL_JSON: String               = "json"
    val COL_ENDPOINT: String           = "endpoint"
    val COL_SYNC: String               = "estatus_sync"
    val COL_FOLIO: String              = "folio"
    val COL_USER_ID: String            = "user_id"
    val COL_USER_NAME: String          = "user_name"
    val COL_APAT: String               = "apat"
    val COL_AMAT: String               = "amat"
    val COL_EMAIL: String              = "email"
    val COL_PASS: String               = "pass"
    val COL_PHONE: String              = "phone"
    val COL_TOKEN: String              = "token"
    val COL_RESPONSE: String           = "col_response"
    val COL_PRODUCT: String            = "col_product"
    val COL_GROUP_NAME: String         = "group_name"
    val COL_GROUP_DESC: String         = "group_desc"
    val COL_GROUP_ID: String           = "group_id"
    val COL_PRODUCT_SEQ: String        = "product_seq"
    val COL_PRODUCT_STAGE_ID: String   = "product_stage_id"
    val COL_PRODUCT_STAGE_NAME: String = "product_stage_name"
    val COL_PERS_IPS: String           = "pers_ips"
    val COL_PERS_NPS: String           = "pers_nps"
    val COL_PERS_IP: String            = "pers_ip"
    val COL_PERS_INVOICE: String       = "pers_invoice"
    val COL_PERS_LASTREGID: String     = "pers_lastreg"
    val COL_PERS_ACTUALREG: String     = "pers_actualreg"
    val COL_PERS_STATUS: String        = "pers_status"


    override fun onCreate(db: SQLiteDatabase?) {
        offlineDataJson(db)
        offlineSaveResponses(db)
        offlineSaveUser(db)
        offlineSaveProducts(db)
        offlinePersistence(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TAB_JSON; ")
        db.execSQL("DROP TABLE IF EXISTS $TAB_RESP; ")
        db.execSQL("DROP TABLE IF EXISTS $TAB_USER; ")
        db.execSQL("DROP TABLE IF EXISTS $TAB_PRODUCTS; ")
        db.execSQL("DROP TABLE IF EXISTS $TAB_PERSISTENCE; ")
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
                "$COL_PASS VARCHAR, " +
                "$COL_RESPONSE VARCHAR, " +
                "$COL_ROLENAME VARCHAR, " +
                "$COL_CURRENT_FILE VARCHAR" +
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
                "$COL_JSON VARCHAR, " +
                "$COL_PRODUCT INTEGER DEFAULT 0" +
                ");"
        db!!.execSQL(query)

        var index = "CREATE INDEX stageName ON $TAB_JSON ($COL_NAME)"
        db.execSQL(index)
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

    private fun offlineSaveProducts(db: SQLiteDatabase?){
        val query = "CREATE TABLE IF NOT EXISTS $TAB_PRODUCTS(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_STAGE_ID INTEGER, " +
                "$COL_STAGE_NAME VARCHAR, " +
                "$COL_STAGE_DESC VARCHAR, " +
                "$COL_GROUP_NAME VARCHAR, " +
                "$COL_GROUP_DESC VARCHAR, " +
                "$COL_GROUP_ID INTEGER, " +
                "$COL_PRODUCT_STAGE_ID INTEGER, " +
                "$COL_PRODUCT_STAGE_NAME VARCHAR, " +
                "$COL_PRODUCT_ID INTEGER, " +
                "$COL_PRODUCT_SEQ INTEGER, " +
                "$COL_PRODUCT_NAME VARCHAR, " +
                "$COL_PRODUCT_DESC VARCHAR, " +
                "$COL_PRODUCT_STAGE VARCHAR" +
                ");"
        db!!.execSQL(query)
    }

    private fun offlinePersistence(db: SQLiteDatabase?){
        val query = "CREATE TABLE IF NOT EXISTS ${TAB_PERSISTENCE}(" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_PERS_IPS INTEGER, " +
                "$COL_PERS_NPS VARCHAR, " +
                "$COL_PERS_IP INTEGER, " +
                "$COL_PERS_INVOICE VARCHAR, " +
                "$COL_PERS_ACTUALREG INTEGER, " +
                "$COL_PERS_LASTREGID INTEGER, " +
                "$COL_PERS_STATUS INTEGER" +
                ");"
        db!!.execSQL(query)
    }


}