package com.ds.offlinehandler

import android.content.Context
import android.util.Log
import com.ds.offlinehandler.misc.Misc
import com.ds.offlinehandler.model.DBHelper

object OfflineSingleton{
    internal var url = ""
    internal var token = ""
    var invoice = ""

    var offlineHandler: OfflineHandler? = null
    internal var h: DBHelper? = null
    internal var misc: Misc? = null


    fun getInstance(context: Context){
        //Instancia internal de la base de datos
        if (h == null){
            h = DBHelper(context)
        }
        //Instancia de los miscellaneous
        if (misc == null){
            misc = Misc(context)
        }
    }

    init {
        //Log.e("SINGLETON::::: ", "OfflineSingleton inicializado")
        //Instancia de offline handler (NO REQUIERE CONTEXT)
        if (offlineHandler == null){
            offlineHandler = OfflineHandler()
        }
    }
}