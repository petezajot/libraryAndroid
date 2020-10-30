package com.example.offlinehandlerpoc

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ajithvgiri.searchdialog.OnSearchItemSelected
import com.ajithvgiri.searchdialog.SearchListItem
import com.ajithvgiri.searchdialog.SearchableDialog
import com.ds.offlinehandler.OfflineSingleton
import com.ds.offlinehandler.model.OfflineStageData
import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), View.OnClickListener, OnSearchItemSelected {
    var idLocalStage = 0
    lateinit var searchableDialog: SearchableDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Instancia de la librería desde singleton en librería
        OfflineSingleton.getInstance(this)
        //Inicializar Librería. Método "init"
        OfflineSingleton.offlineHandler!!.init(
                "https://techhub.docsolutions.com/OBProgresemosDEV/WebApi/api/workflow/",
                "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1bmlxdWVfbmFtZSI6InVzcmFnZW50ZTAyIiwibmFtZWlkIjoiNDciLCJuYmYiOjE2MDQwMDM1NDgsImV4cCI6MTYwNDA4OTk0OCwiaWF0IjoxNjA0MDAzNTQ4LCJpc3MiOiJBdXRlbnRpY2FjaW9uT25Cb2FyZGluZ1NlcnZpY2UiLCJhdWQiOiJEZWZhdWx0QXVkaWVuY2UifQ.CsjJJD1aG5e0qeczMC3x2JiMQAKhCWzlvvDUW0o2Xrg",
                0,
                0,
                "1.0.0",
                "TEST"
            )
        btnNext.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Obtener primer stage
        val timer = object: CountDownTimer(500, 100) {
            override fun onTick(millisUntilFinished: Long) {}
            override fun onFinish() {
                //obtener stage inicial alojado por la librería. Método "getStages"
                /*Para obtener el stage inicial, es necesario pasar al stage el parámetro "0" en string
                * y el id del stage actual en entero en 0 también*/
                var json = OfflineSingleton.offlineHandler!!.getStages("0", 0)
                /*devuelve un hashmap con el id del stage devuelto, el nombre del stage y un json con
                * el contenido de configuración de ese stage*/
                stageVal.text = json.get("stageId").toString()
                titleVal.text = json.get("name").toString()
                jsonVal.text = json.get("json").toString()
                idLocalStage = json.get("stageId") as Int
            }
        }
        timer.start()
    }

    override fun onClick(v: View?) {
        when(v!!.id){
            R.id.btnNext -> {
                //Guardar stage actual
                //Método "saveStages"
                var hash = OfflineSingleton.offlineHandler!!.saveStages(
                    OfflineStageData(
                        Integer.parseInt(stageVal.text.toString()),//ID stage actual
                        titleVal.text.toString(),//Título stage actual
                        "{json contenido}",//json con respuestas del usuario
                        "/api/muestra/metodo",//endpoint a donde se sincronizara esta info
                        0,//sequence
                        "")//Folio de solicitud, si se envía vacío, la librería generará un folio falso temporal
                )//Este método almacena las respuestas del stage actual y a su vez, devuelve los datos
                //Del stage siguiente para pasar a el
                //La respuesta contiene un hashmap, mismo que tiene el elemento "products" que debe ser evaluado
                //para los pasos siguientes

                // TODO:Retornar también folio recién generádo

                //Validar si el stage tiene productos
                var products = hash.get("products").toString()
                if (!products.equals("0")){
                    //si es diferente a 0, se muestra producto
                    //En este caso (POC) genera los botónes necesarios dinámicamente para elegir el producto
                    //y continuar por los stages del producto seleccionado
                    titleVal.text = ""
                    titleVal.text = hash.get("name").toString()
                    stageVal.text = ""
                    stageVal.text = hash.get("stageId").toString()
                    //Bótones de productos
                    var parser = JsonParser()
                    var arr = parser.parse(hash.get("products").toString()).asJsonArray
                    arr.forEach {
                        var prods = parser.parse(it.toString()).asJsonObject
                        var content = prods.get("productos").toString()
                        var splited = content.split("|")
                        var id = splited[0].replace("\"", "")
                        var title = splited[1].replace("\"", "").replace("\\", "")

                        var btn = Button(this)
                        btn.setTag(id)
                        btn.setText(title)
                        container.addView(btn)
                        btn.setOnClickListener {
                            var i = Intent(this, ProductsActivity::class.java)
                            i.putExtra("productSelected", it.tag.toString())
                            i.putExtra("continueSol", "0")
                            startActivity(i)
                            finish()
                        }
                    }
                    jsonVal.text = ""
                }else{
                    //Es igual a 0, se muestra stage
                    //Recuperar datos del stage siguiente
                    //Solo muestra los datos del stage
                    jsonVal.text = ""
                    //Método "getStages"
                    jsonVal.text = hash.get("json").toString()

                    titleVal.text = ""
                    titleVal.text = hash.get("name").toString()

                    stageVal.text = ""
                    stageVal.text = hash.get("stageId").toString()

                    idLocalStage = Integer.parseInt(hash.get("stageId").toString())
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        var inflater = menuInflater
        inflater.inflate(R.menu.actionbarmenu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.action_login -> {loginDialog()}
            R.id.action_pendant -> {invoiceDialog()}
            R.id.action_sync -> {syncData()}
        }
        return super.onOptionsItemSelected(item)
    }

    private fun invoiceDialog() {
        //Lista de solicitudes pendientes por folio falso
        //Método "getListPersistence"
        //Devuelve un List<String> con los folios pendientes
        var lista = OfflineSingleton.offlineHandler!!.getListPersistence()
        var searchListItems: ArrayList<SearchListItem> = ArrayList()
        lista.forEach {
            val searchListItem = SearchListItem((it.get("ips").toString()).toInt(), it.get("folio").toString())
            searchListItems.add(searchListItem)
        }

        searchableDialog = SearchableDialog(this, searchListItems, "Solicitudes en curso")
        searchableDialog.setOnItemSelected(this)
        searchableDialog.show()
    }
    //Searchable items
    override fun onClick(position: Int, searchListItem: SearchListItem) {
        searchableDialog.dismiss()
        Toast.makeText(this, "Continuar con folio: ${searchListItem.title}", Toast.LENGTH_LONG).show()

        var i = Intent(this, ProductsActivity::class.java)
        i.putExtra("continueSol", searchListItem.title)
        i.putExtra("productSelected", searchListItem.id.toString())
        startActivity(i)
        finish()
    }

    private fun loginDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(true)
        dialog.setContentView(R.layout.alert_login)
        var btn = dialog.findViewById<Button>(R.id.loginBtn)
        btn.setOnClickListener {
            //Login offline (funciona siempre y cuando el usuario ya haya hecho un login online)
            //Responde con un dato boolean
            var loginResponse = OfflineSingleton.offlineHandler!!.login("usragente02", "Nuevo1234")
            if (loginResponse){
                //(TRUE) Existe el usuario y está correcto
                Toast.makeText(this, "Login Correcto", Toast.LENGTH_LONG).show()
            }else{
                //(FALSE) No existe el usuario o es incorrecto
                Toast.makeText(this, "Login incorrecto o no disponible para sesión offline", Toast.LENGTH_LONG).show()
            }

            /*
            //Éste método es para almacenar los datos del usuario una ves que se logueó correctamente online
            //Para posteriormente hacer el login offline

            offlineHandler!!.saveUser(OfflineUserData(
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
    //Sincronización de datos
    private fun syncData() {
        //Recuperar las respuestas almacenadas por el usuario
        //Método "offlineResponses"
        var json = ""
        var response = OfflineSingleton.offlineHandler!!.offlineResponses()
        response.forEach {
            /*Log.e("ID: ", it.get("id").toString())
            Log.e("Nombre: ", it.get("name").toString())
            Log.e("Stage: ", it.get("stageId").toString())
            Log.e("Folio: ", it.get("folio").toString())
            Log.e("EndPoint: ", it.get("endpoint").toString())
            Log.e("JSON: ", it.get("json").toString())*/
            json = it.get("json").toString()
        }

        var parser = JsonParser()
        var obj = parser.parse(json).asJsonObject
        Log.e("JSON::::: ", obj.toString())
    }

    private fun httpRequest(){

    }
}