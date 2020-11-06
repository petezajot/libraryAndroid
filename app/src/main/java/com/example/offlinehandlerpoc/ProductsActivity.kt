package com.example.offlinehandlerpoc

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.ds.offlinehandler.OfflineSingleton
import com.ds.offlinehandler.model.OfflineProductData
import kotlinx.android.synthetic.main.activity_products.*

class ProductsActivity : AppCompatActivity(), View.OnClickListener {
    var selectedProd: String? = null
    var continueSol: String? = null
    var productStages: HashMap<String, Any>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_products)
        //id del producto seleccionado
        selectedProd = intent.getStringExtra("productSelected")
        continueSol  = intent.getStringExtra("continueSol")
        //Botón
        productBtnNext.setOnClickListener(this)
    }

    override fun onResume() {
        super.onResume()
        //Sí continue sol es "0", entonces debe traer primer elemento
        //En caso contrario, es una solicitud recuperada
        if (continueSol!!.equals("0")){
            productStages = OfflineSingleton.offlineHandler!!.getProductStages("0", "0", selectedProd.toString())
        }else{
            productStages = OfflineSingleton.offlineHandler!!.getPersistenceStage(
                continueSol!!
            )
        }

        productVal.setText(productStages!!.get("productId").toString())
        productTitleVal.setText(productStages!!.get("productName").toString())
        jsonProductVal.setText(productStages!!.get("productStage").toString())
    }

    override fun onClick(v: View?) {
        val productId = productVal.text.toString()
        val productName = productTitleVal.text.toString()

        productStages = OfflineSingleton.offlineHandler!!.saveProductStages(
            OfflineProductData(
                productId.toInt(),
                productName,
                "{Respuesta del stage del producto}",
                "api/donde/enviamos/respuesta/del/producto",
                (productStages!!.get("productSeq").toString()).toInt(),
                "",
                selectedProd!!)
        )

        if (productStages!!.get("productName").toString().equals("")){
            //Finalizó ciclo
            //OfflineSingleton.invoice = ""
            var i = Intent(this, MainActivity::class.java)
            startActivity(i)
            finish()
        }

        productVal.setText(productStages!!.get("productId").toString())
        productTitleVal.setText(productStages!!.get("productName").toString())
        jsonProductVal.setText(productStages!!.get("productStage").toString())
    }
}