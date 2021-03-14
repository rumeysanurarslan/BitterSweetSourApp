
package com.example.yemeksiparisprojesi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_sepeti_goruntule.*
import kotlinx.android.synthetic.main.activity_yemek_detay.*
import org.json.JSONException
import org.json.JSONObject


class YemekDetay : AppCompatActivity() {
    private lateinit var yemek:Yemekler
    private lateinit var yemeklerListe : ArrayList<Yemekler>
    var yemekAdet : Int = 0
    private lateinit var mini_adapter :MiniYemekAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yemek_detay)
        setSupportActionBar(toolbarYemekDetay)

        supportActionBar?.apply {
            setTitle("")
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }

        topRv.setHasFixedSize(true)
        topRv.layoutManager = StaggeredGridLayoutManager(1, LinearLayoutManager.HORIZONTAL);

        tumYemekler()

        yemek = intent.getSerializableExtra("nesne") as Yemekler

        textViewYemekAd.setText(yemek.yemek_adi)
        textViewYemekFiyat.setText("${yemek.yemek_fiyat} ₺")
        val url = "http://kasimadalan.pe.hu/yemekler/resimler/${yemek.yemek_resim_adi}"
        Picasso.get().load(url)
                .placeholder(R.drawable.yemek_placeholder)
                .error(R.drawable.yemek_resim_error)
                .into(imageViewYemekResim)



        imageButtonAdetEkle.setOnClickListener{
            yemekAdet = yemekAdet + 1
            textViewYemekAdet.setText((yemekAdet).toString())

        }
        imageButtonAdetSil.setOnClickListener{
            if(yemekAdet > 0)
            {
                yemekAdet = yemekAdet - 1
                textViewYemekAdet.setText((yemekAdet).toString())

            }
        }
        buttonSepetEkle.setOnClickListener {
            if(yemekAdet > 0)
            {
                sepeteEkle()
            }
            else
            {
                Toast.makeText(applicationContext, getResources().getString(R.string.adet_secme_mesaj), Toast.LENGTH_LONG).show()

            }
        }

        imageButtonLogo.setOnClickListener{
            startActivity(Intent(this@YemekDetay,MainActivity::class.java))
        }

        toolbarYemekDetay.setNavigationOnClickListener {
            startActivity(Intent(this@YemekDetay,MainActivity::class.java))
        }


    }
    override fun onBackPressed() {
        val yeniIntent = Intent(Intent.ACTION_MAIN)
        yeniIntent.addCategory(Intent.CATEGORY_HOME)
        yeniIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(yeniIntent)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu)
        val item = menu.findItem(R.id.action_ara)
        item.isVisible = false

        val item2 = menu.findItem(R.id.action_harita)
        item2.isVisible = false

        val item3 = menu.findItem(R.id.action_sil)
        item3.isVisible = false

        return super.onCreateOptionsMenu(menu)
    }

    fun sepeteEkle(){

        val url = "http://kasimadalan.pe.hu/yemekler/insert_sepet_yemek.php"
        val istek = object: StringRequest(Request.Method.POST,url,Response.Listener { cevap ->

            val ad = AlertDialog.Builder(this@YemekDetay)
            ad.setTitle(R.string.success_title)
            ad.setMessage("${yemekAdet} ${getResources().getString(R.string.adet)} ${yemek.yemek_adi} ${getResources().getString(R.string.basariyla_ekleme)}")
            ad.setIcon(R.drawable.siparis_alindi_icon)

            ad.setPositiveButton(R.string.sepete_git){
                d,i -> startActivity(Intent(this@YemekDetay,SepetiGoruntule::class.java))
            }

            ad.setNegativeButton(R.string.alisverise_devam){
                d,i -> startActivity(Intent(this@YemekDetay,MainActivity::class.java))
            }

            ad.create().show()

        },Response.ErrorListener { Log.e("Ekle", "Hata") }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                //gönderdiğimiz her değer String olmalı
                params["yemek_id"] = yemek.yemek_id.toString()
                params["yemek_adi"] = yemek.yemek_adi
                params["yemek_resim_adi"]= yemek.yemek_resim_adi
                params["yemek_fiyat"] = yemek.yemek_fiyat.toString()
                params["yemek_siparis_adet"] = yemekAdet.toString()
                return params
            }
        }

        Volley.newRequestQueue(this@YemekDetay).add(istek)
    }

    fun tumYemekler(){
        val url = "http://kasimadalan.pe.hu/yemekler/tum_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url, Response.Listener { cevap ->
            try {
                yemeklerListe = ArrayList()
                val jsonObj = JSONObject(cevap)
                val yemekler = jsonObj.getJSONArray("yemekler")

                for(i in 0 until 5)
                {
                    val k = yemekler.getJSONObject(i)
                    val yemek_id = k.getInt("yemek_id")
                    val yemek_adi = k.getString("yemek_adi")
                    val yemek_resim_adi = k.getString("yemek_resim_adi")
                    val yemek_fiyati = k.getInt("yemek_fiyat")
                    var yemek = Yemekler(yemek_id, yemek_adi, yemek_resim_adi, yemek_fiyati)
                    yemeklerListe.add(yemek)
                }

                mini_adapter = MiniYemekAdapter(this,yemeklerListe)
                topRv.adapter = mini_adapter

            }catch (e: JSONException){

            }
        }, Response.ErrorListener { Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(this).add(istek)
    }

}