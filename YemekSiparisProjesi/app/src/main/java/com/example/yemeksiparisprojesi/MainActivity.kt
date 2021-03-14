package com.example.yemeksiparisprojesi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject


class MainActivity : AppCompatActivity(), SearchView.OnQueryTextListener {
    private lateinit var yemeklerListe: ArrayList<Yemekler>
    private lateinit var adapter:YemeklerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        toolbarMain.setTitle("")
        setSupportActionBar(toolbarMain)

        yemeklerRv.setHasFixedSize(true)

        yemeklerRv.layoutManager = LinearLayoutManager(this@MainActivity)

        tumYemekler()
        tumSepet()
        fab.setOnClickListener {
            startActivity(
                Intent(
                this@MainActivity
                ,SepetiGoruntule::class.java)
            )
        }

        toolbarMain.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_harita -> startActivity(Intent(this@MainActivity,HaritaActivity::class.java))

            }
            true
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
        val searchView = item.actionView as SearchView
        searchView.setOnQueryTextListener(this)

        val item2 = menu.findItem(R.id.action_sil)
        item2.isVisible = false

        return super.onCreateOptionsMenu(menu)
    }

    override fun onQueryTextSubmit(query: String): Boolean {
        aramaYap(query)
        return true
    }

    override fun onQueryTextChange(newText: String): Boolean {
        aramaYap(newText)
        return true
    }

    fun tumYemekler(){
        val url = "http://kasimadalan.pe.hu/yemekler/tum_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url, Response.Listener { cevap ->
            try {
                yemeklerListe = ArrayList()
                val jsonObj = JSONObject(cevap)
                val yemekler = jsonObj.getJSONArray("yemekler")

                for(i in 0 until yemekler.length())
                {
                    val k = yemekler.getJSONObject(i)
                    val yemek_id = k.getInt("yemek_id")
                    val yemek_adi = k.getString("yemek_adi")
                    val yemek_resim_adi = k.getString("yemek_resim_adi")
                    val yemek_fiyati = k.getInt("yemek_fiyat")
                    var yemek = Yemekler(yemek_id, yemek_adi, yemek_resim_adi, yemek_fiyati)
                    yemeklerListe.add(yemek)
                }

                adapter = YemeklerAdapter(this,yemeklerListe)
                yemeklerRv.adapter = adapter

            }catch (e: JSONException){

            }
        }, Response.ErrorListener { Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(this).add(istek)
    }

    fun aramaYap(aramaKelime : String){
        val url = "http://kasimadalan.pe.hu/yemekler/tum_yemekler_arama.php"
        val istek = object: StringRequest(Request.Method.POST, url, Response.Listener { cevap ->
            try {

                yemeklerListe = ArrayList()
                val jsonObj = JSONObject(cevap)
                val yemekler = jsonObj.getJSONArray("yemekler")

                for(i in 0 until yemekler.length())
                {
                    val k = yemekler.getJSONObject(i)
                    val yemek_id = k.getInt("yemek_id")
                    val yemek_adi = k.getString("yemek_adi")
                    val yemek_resim_adi = k.getString("yemek_resim_adi")
                    val yemek_fiyati = k.getInt("yemek_fiyat")
                    var yemek = Yemekler(yemek_id, yemek_adi, yemek_resim_adi, yemek_fiyati)
                    yemeklerListe.add(yemek)
                }
                adapter = YemeklerAdapter(this@MainActivity,yemeklerListe)
                yemeklerRv.adapter = adapter

            }catch (e: JSONException){

            }
        }, Response.ErrorListener {Log.e("Hata", "Veri okuma")}){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["yemek_adi"] = aramaKelime
                return params
            }
        }

       Volley.newRequestQueue(this@MainActivity).add(istek)
    }

    fun tumSepet(){ // bunu sepet adapter kullanarak yapıcaksın sonradan
        val url = "http://kasimadalan.pe.hu/yemekler/tum_sepet_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url,Response.Listener { cevap ->
            try {

                val tempListe = ArrayList<SepettekiYemekler>()

                val jsonObj = JSONObject(cevap)
                val yemekler = jsonObj.getJSONArray("sepet_yemekler")
                fab.setText(yemekler.length().toString())

            }catch (e: JSONException){

            }
        },Response.ErrorListener {Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(this).add(istek)
    }
}