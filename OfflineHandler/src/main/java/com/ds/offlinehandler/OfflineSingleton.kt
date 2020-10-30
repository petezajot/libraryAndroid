package com.ds.offlinehandler

import android.content.Context

object OfflineSingleton {
    var url = ""
    var token = ""

    var invoice = ""
    var idProductStage = ""
    var nameProductStage = ""
    var idProduct = ""

    var offlineHandler: OfflineHandler? = null

    fun getInstance(context: Context){
        if (offlineHandler == null){
            offlineHandler = OfflineHandler(context)
        }
    }

    init {
        print("Se invocó al singleton")
    }
}