package com.example.pococr

import android.app.Dialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
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

class MainActivity : AppCompatActivity(), View.OnClickListener {
    var idLocalStage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Inicializar Librería
        OfflineHandler(this)
            .init(
                "https://techhub.docsolutions.com/OnBoardingPre/WebApi/api/workflow/",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InVzcmFnZW50ZTAyIiwibmFtZWlkIjoiNDciLCJuYmYiOjE1OTgzMDE0MzMsImV4cCI6MTU5ODM4NzgzMywiaWF0IjoxNTk4MzAxNDMzLCJpc3MiOiJBdXRlbnRpY2FjaW9uT25Cb2FyZGluZ1NlcnZpY2UiLCJhdWQiOiJEZWZhdWx0QXVkaWVuY2UifQ.JDuwXFlNSJHyDylH7A8UQuj1DvXTzTHdYptMwMsqh8g")
        btnNext.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Obtener primer stage

        val timer = object: CountDownTimer(2000, 1000) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                var json = OfflineHandler(this@MainActivity).getStages("0", idLocalStage)
                titleVal.text = json.get("name").toString()
                jsonVal.text = json.get("json").toString()
                idLocalStage = json.get("stageId") as Int
                Log.e("Folio Móvil::: ", json.get("folioMovil").toString())
            }
        }
        timer.start()
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
                0)
        )

        //Recuperar datos del stage siguiente
        jsonVal.text = ""
        //Método "getStages"
        var json = OfflineHandler(this).getStages(titleVal.text.toString(), idLocalStage)
        jsonVal.text = json.get("json").toString()

        titleVal.text = ""
        titleVal.text = json.get("name").toString()
        idLocalStage = Integer.parseInt(json.get("stageId").toString())
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
            var loginResponse = OfflineHandler(this).login("usragente02", "Nuevo1234")
            if (loginResponse){
                //Existe y está correcto
                Toast.makeText(this, "Login Correcto", Toast.LENGTH_LONG).show()
            }else{
                //No existe o es incorrecto
                Toast.makeText(this, "Login incorrecto o no disponible para sesión offline", Toast.LENGTH_LONG).show()
            }

            /*OfflineHandler(this).saveUser(OfflineUserData(
                47,
                "usragente02",
                "Daniel",
                "Díaz",
                "Perez",
                "usragente02@correo.com",
                "1234567890",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InVzcmFnZW50ZTAyIiwibmFtZWlkIjoiNDciLCJuYmYiOjE1OTgyODY2MzYsImV4cCI6MTU5ODM3MzAzNiwiaWF0IjoxNTk4Mjg2NjM2LCJpc3MiOiJBdXRlbnRpY2FjaW9uT25Cb2FyZGluZ1NlcnZpY2UiLCJhdWQiOiJEZWZhdWx0QXVkaWVuY2UifQ.94Pd-17Pic4Yl5VdXLVwo1h06ljpBcUdgzq-KY3t1o0",
                "Nuevo1234"))*/


        }
        dialog.show()
    }


}