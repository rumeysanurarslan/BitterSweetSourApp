package com.example.yemeksiparisprojesi

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import kotlinx.android.synthetic.main.activity_sepeti_goruntule.*
import org.json.JSONException
import org.json.JSONObject

class SepetiGoruntule() : AppCompatActivity(), SepetTutari {
    private lateinit var sepettekiYemekler : ArrayList<SepettekiYemekler>
    private lateinit var adapter:SepetAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sepeti_goruntule)

        toolbarSepetiGoruntule.setTitle("")
        setSupportActionBar(toolbarSepetiGoruntule)

        sepettekilerRv.setHasFixedSize(true)
        sepettekilerRv.layoutManager = LinearLayoutManager(this@SepetiGoruntule)

        tumSepettekiYemekler()

        buttonSiparisVer.setOnClickListener {
            tumSepettekiYemekleriSil(true)
            val ad = AlertDialog.Builder(this@SepetiGoruntule)
            ad.setTitle(R.string.siparis_verildi_title)
            ad.setMessage(R.string.siparis_verildi_msj)
            ad.setIcon(R.drawable.siparis_alindi_icon)
            ad.setPositiveButton(R.string.anasayfaya_don){
                d,i -> startActivity(Intent(this@SepetiGoruntule,MainActivity::class.java))
            }

            ad.create().show()
            tumSepettekiYemekler()

        }
        sepetToolbarLogo.setOnClickListener{
            startActivity(Intent(this@SepetiGoruntule,MainActivity::class.java))
        }

        toolbarSepetiGoruntule.setOnMenuItemClickListener {
            when(it.itemId) {
                R.id.action_sil -> tumSepettekiYemekleriSil()
                R.id.action_harita -> startActivity(Intent(this@SepetiGoruntule,HaritaActivity::class.java))
            }
            true
        }

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar,menu)
        val item = menu.findItem(R.id.action_ara)
        item.isVisible = false

        val item2 = menu.findItem(R.id.action_harita)
        item2.isVisible = true

        val item3 = menu.findItem(R.id.action_sil)
        item3.isVisible = true


        return super.onCreateOptionsMenu(menu)
    }


    override fun onBackPressed() {
        startActivity(Intent(this@SepetiGoruntule,MainActivity::class.java))
    }


    fun tumSepettekiYemekler(){
        var total : Int = 0
        val url = "http://kasimadalan.pe.hu/yemekler/tum_sepet_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url, Response.Listener { cevap ->
            try {

                sepettekiYemekler = ArrayList()

                val jsonObj = JSONObject(cevap)
                val yemekler = jsonObj.getJSONArray("sepet_yemekler")

                for(i in 0 until yemekler.length())
                {
                    val k = yemekler.getJSONObject(i)
                    val yemek_id = k.getInt("yemek_id")
                    val yemek_adi = k.getString("yemek_adi")
                    val yemek_resim_adi = k.getString("yemek_resim_adi")
                    val yemek_fiyati = k.getInt("yemek_fiyat")
                    val yemek_siparis_adet = k.getInt("yemek_siparis_adet")

                    total += yemek_siparis_adet * yemek_fiyati

                    var y = Yemekler(yemek_id, yemek_adi, yemek_resim_adi, yemek_fiyati)
                    var yemek = SepettekiYemekler(y, yemek_siparis_adet)
                    sepettekiYemekler.add(yemek)
                }

                adapter = SepetAdapter(this,sepettekiYemekler,this)
                adapter.notifyDataSetChanged()
                sepettekilerRv.adapter = adapter

                sepetTutariniGuncelle(total)

            }catch (e: JSONException){

            }
        }, Response.ErrorListener { Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(this@SepetiGoruntule).add(istek)
    }

    fun tumSepettekiYemekleriSil(isordered : Boolean = false) {
        for(i in 0 until sepettekiYemekler.size )
        {
            adapter.sepettenUrunSil(sepettekiYemekler[i].yemek, true)
        }
        if(!isordered)
        {
            Toast.makeText(this, R.string.tum_yemekler_silindi_msj, Toast.LENGTH_LONG).show()
            startActivity(Intent(this,MainActivity::class.java))
        }

    }

    override fun sepetTutariniGuncelle(total: Int) {
        totalItemCount.text = "${total} \u20BA"
        textView5.text = "5.99 ₺"
        textViewTotal.text = "${total + 5.99} \u20BA"

    }

}