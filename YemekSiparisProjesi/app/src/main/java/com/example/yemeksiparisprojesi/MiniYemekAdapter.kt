package com.example.yemeksiparisprojesi

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
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

class MiniYemekAdapter(private var mContext: Context, private var yemeklerListe:ArrayList<Yemekler>)
    : RecyclerView.Adapter<MiniYemekAdapter.CardTasarimTutucu>()
{
    inner class CardTasarimTutucu(tasarim: View) : RecyclerView.ViewHolder(tasarim){
        var mini_yemek_card: CardView
        var mini_card_yemek_resim: ImageView
        var mini_card_yemek_adi: TextView
        var mini_card_yemek_fiyat: TextView
        var mini_sepete_ekle_buton: Button

        init {
            mini_yemek_card = tasarim.findViewById(R.id.mini_yemek_card)
            mini_card_yemek_resim = tasarim.findViewById(R.id.mini_card_yemek_resim)
            mini_card_yemek_adi = tasarim.findViewById(R.id.mini_yemek_adi)
            mini_card_yemek_fiyat = tasarim.findViewById(R.id.mini_yemek_fiyat)
            mini_sepete_ekle_buton = tasarim.findViewById(R.id.mini_ekle_button)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val tasarim = LayoutInflater.from(mContext)
                .inflate(R.layout.mini_card,parent,false)
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
                .into(holder.mini_card_yemek_resim)

        holder.mini_card_yemek_adi.text = "${yemek.yemek_adi}"
        holder.mini_card_yemek_fiyat.text = "${yemek.yemek_fiyat } ₺ "


        holder.mini_sepete_ekle_buton.setOnClickListener {
            sepeteEkle(yemek)
        }
    }

    fun tumYemekler(){
        val url = "http://kasimadalan.pe.hu/yemekler/tum_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url,Response.Listener { cevap ->
            try {

                val tempListe = ArrayList<Yemekler>()

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
                    tempListe.add(yemek)
                }

                yemeklerListe = tempListe
                notifyDataSetChanged() //listeyi güncellediğini database söyleme

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
            ad.setMessage("1 ${mContext.getResources().getString(R.string.adet)} ${yemek.yemek_adi} ${mContext.getResources().getString(R.string.basariyla_ekleme)}")
            ad.setIcon(R.drawable.siparis_alindi_icon) //icon ekleme

            ad.setPositiveButton(R.string.sepete_git){ //onay butonu
                d,i -> mContext.startActivity(Intent(mContext,SepetiGoruntule::class.java))
            }

            ad.setNegativeButton(R.string.alisverise_devam){ //onay butonu
                d,i -> mContext.startActivity(Intent(mContext,MainActivity::class.java))
            }

            ad.create().show() //önce create ile dialog olusuyor sonra show

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