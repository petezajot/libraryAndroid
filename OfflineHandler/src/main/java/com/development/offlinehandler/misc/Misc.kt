package com.development.offlinehandler.misc

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import android.util.Log
import okhttp3.internal.Internal
import okhttp3.internal.Internal.instance
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class Misc(context: Context) {
    var ctx = context

    fun isNetDisp(): Boolean{//Verificar si los dispositivos de red están activos
        val connectivityManager: ConnectivityManager = ctx.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val actNetInfo: NetworkInfo = connectivityManager.activeNetworkInfo!!
        return (actNetInfo != null && actNetInfo.isConnected)
    }

    fun isOnlineNet(): Boolean{//Verificar si hay acceso a internet
        try{
            var p: Process = Runtime.getRuntime().exec("ping -c 1 www.google.com")
            val i = p.waitFor()
            val reachable: Boolean = (i == 0)
            return reachable
        }catch (e: Exception){
            Log.e("osOnlineNet: ", e.localizedMessage)
        }
        return false
    }

    fun dateTime(): String{//Fecha/Hora actuales
        val sdf = SimpleDateFormat("dd/M/yyyy hh:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate
    }
}