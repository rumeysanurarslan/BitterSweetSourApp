package com.example.yemeksiparisprojesi

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class YemeklerAdapter(private var mContext: Context, private var yemeklerListe:ArrayList<Yemekler>)
    : RecyclerView.Adapter<YemeklerAdapter.CardTasarimTutucu>()
{
    inner class CardTasarimTutucu(tasarim: View) : RecyclerView.ViewHolder(tasarim){
        var yemek_card: CardView
        var card_yemek_resim: ImageView
        var card_yemek_adi: TextView
        var card_yemek_fiyat: TextView
        var sepete_ekle_buton: ImageButton
        var yemek_detay_buton: ImageButton

        init {
            yemek_card = tasarim.findViewById(R.id.sepet_yemek_card)
            card_yemek_resim = tasarim.findViewById(R.id.card_yemek_resim)
            card_yemek_adi = tasarim.findViewById(R.id.card_yemek_adi)
            card_yemek_fiyat = tasarim.findViewById(R.id.yemek_fiyat)
            sepete_ekle_buton = tasarim.findViewById(R.id.yemek_ekle_buton)
            yemek_detay_buton = tasarim.findViewById(R.id.yemek_sil)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val tasarim = LayoutInflater.from(mContext)
            .inflate(R.layout.card_tasarim,parent,false)
        return CardTasarimTutucu(tasarim)
    }

    override fun getItemCount(): Int {
        return yemeklerListe.size
    }

    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        val yemek = yemeklerListe.get(position)

        val url = "http://kasimadalan.pe.hu/yemekler/resimler/${yemek.yemek_resim_adi}"
        Picasso.get().load(url)
            .placeholder(R.drawable.yemek_placeholder)
            .error(R.drawable.yemek_resim_error)
            .into(holder.card_yemek_resim)

        holder.card_yemek_adi.text = "${yemek.yemek_adi}"
        holder.card_yemek_fiyat.text = "${yemek.yemek_fiyat } ₺ "

        holder.yemek_detay_buton.setOnClickListener {
            val intent = Intent(mContext,YemekDetay::class.java)
            intent.putExtra("nesne",yemek)
            mContext.startActivity(intent)
        }


        holder.sepete_ekle_buton.setOnClickListener {
            sepeteEkle(yemek)
        }

        holder.yemek_card.setOnClickListener {
            val intent = Intent(mContext,YemekDetay::class.java)
            intent.putExtra("nesne",yemek)
            mContext.startActivity(intent)
        }

    }

    fun tumYemekler(){
        val url = "http://kasimadalan.pe.hu/yemekler/tum_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url,Response.Listener { cevap ->
            try {

                val tempListe = ArrayList<Yemekler>()

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
                    tempListe.add(yemek)
                }

                yemeklerListe = tempListe
                notifyDataSetChanged()

            }catch (e: JSONException){

            }
        },Response.ErrorListener {Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(mContext).add(istek)
    }

    fun sepeteEkle(yemek: Yemekler){

        val url = "http://kasimadalan.pe.hu/yemekler/insert_sepet_yemek.php"
        val istek = object: StringRequest(Request.Method.POST,url,Response.Listener { cevap ->


            val ad = AlertDialog.Builder(mContext)
            ad.setTitle(R.string.success_title) //baslik tanımlama
            var item = R.string.adet
            var basari =
            ad.setMessage("1 ${mContext.getResources().getString(R.string.adet)} ${yemek.yemek_adi} ${mContext.getResources().getString(R.string.basariyla_ekleme)}")
            ad.setIcon(R.drawable.siparis_alindi_icon) //icon ekleme

            ad.setPositiveButton(R.string.sepete_git){
                d,i -> mContext.startActivity(Intent(mContext,SepetiGoruntule::class.java))
            }

            ad.setNegativeButton(R.string.alisverise_devam){
                d,i -> mContext.startActivity(Intent(mContext,MainActivity::class.java))
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
                params["yemek_siparis_adet"] = "1"
                return params
            }
        }

        Volley.newRequestQueue(mContext).add(istek)
    }

}