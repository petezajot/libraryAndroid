package com.example.pococr

import android.app.Activity
import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import com.development.offlinehandler.OfflineHandler
import com.development.offlinehandler.model.OfflineStageData
import com.development.offlinehandler.model.OfflineUserData
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

class MainActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Inicializar Librería
        OfflineHandler(this)
            .init(
                "https://techhub.docsolutions.com/OnBoardingPre/WebApi/api/workflow/",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InVzcmFnZW50ZTAyIiwibmFtZWlkIjoiNDciLCJuYmYiOjE1OTgyMDQ4MTQsImV4cCI6MTU5ODI5MTIxNCwiaWF0IjoxNTk4MjA0ODE0LCJpc3MiOiJBdXRlbnRpY2FjaW9uT25Cb2FyZGluZ1NlcnZpY2UiLCJhdWQiOiJEZWZhdWx0QXVkaWVuY2UifQ.QgS0u4x-rZObdXIsIk6cAuJJkRa4q1qP1nvyqx90jeU")

        btnNext.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Obtener primer stage
        var json = OfflineHandler(this).getStages("0")
        titleVal.text = json.get("name").toString()
        jsonVal.text = json.get("json").toString()
    }

    override fun onClick(v: View?) {
        //Guardar stage actual
        //Método "saveStages"
        OfflineHandler(this).saveStages(
            OfflineStageData(
                0,
                titleVal.text.toString(),
                "{json contenido}",
                "/api/muestra/metodo",
                0,
                "987654321")
        )

        //Recuperar datos del stage siguiente
        jsonVal.text = ""
        //Método "getStages"
        var json = OfflineHandler(this).getStages(titleVal.text.toString())
        jsonVal.text = json.get("json").toString()

        titleVal.text = ""
        titleVal.text = json.get("name").toString()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.actionbarmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_login -> {loginDialog()}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun loginDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.alert_login)
        var btn = dialog.findViewById<Button>(R.id.loginBtn)
        btn.setOnClickListener {
            //guardar usuario para el login offline (librería)
            OfflineHandler(this).saveUser(OfflineUserData(
                0,
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""))
        }
        dialog.show()
    }


}