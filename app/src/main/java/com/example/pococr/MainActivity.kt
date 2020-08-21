package com.example.pococr

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.development.offlinehandler.OfflineHandler
import com.google.gson.annotations.SerializedName
import kotlinx.android.synthetic.main.activity_main.*
import java.util.HashMap

class MainActivity : AppCompatActivity(), View.OnClickListener{

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Inicializar Librería
        OfflineHandler(this)
            .init(
                "https://techhub.docsolutions.com/OnBoardingPre/WebApi/api/workflow/",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InRlY2hodWIiLCJuYW1laWQiOiIxIiwibmJmIjoxNTk3OTQzOTc4LCJleHAiOjE1OTgwMzAzNzgsImlhdCI6MTU5Nzk0Mzk3OCwiaXNzIjoiQXV0ZW50aWNhY2lvbk9uQm9hcmRpbmdTZXJ2aWNlIiwiYXVkIjoiRGVmYXVsdEF1ZGllbmNlIn0.l4IHCJlur1S6ma_ZHtfSBJhGXd8Wyou7gG59nIYjIfg")

        btnNext.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Obtener primer stage
        var json = OfflineHandler(this).getStages("0")
        titleVal.text = json.get("name")
        jsonVal.text = json.get("json")
    }

    override fun onClick(v: View?) {
        //Guardar stage actual
        var hash = HashMap<String, Any>()
        hash.put("stageId", 0)
        hash.put("name", titleVal.text.toString())
        hash.put("json", "{json contenido}")
        hash.put("endpoint", "/api/muestra/metodo")
        hash.put("seq", 0)
        hash.put("folio", "987654321")
        //Método "saveStages"
        OfflineHandler(this).saveStages(hash)


        //Recuperar datos del stage siguiente
        jsonVal.text = ""
        //Método "getStages"
        var json = OfflineHandler(this).getStages(titleVal.text.toString())
        jsonVal.text = json.get("json")

        titleVal.text = ""
        titleVal.text = json.get("name")
    }

}