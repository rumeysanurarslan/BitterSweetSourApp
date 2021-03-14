package com.example.yemeksiparisprojesi

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.squareup.picasso.Picasso
import org.json.JSONException
import org.json.JSONObject

class SepetAdapter(private var mContext: Context, private var sepettekilerListe:ArrayList<SepettekiYemekler>, private val sepetTutari: SepetTutari)
    : RecyclerView.Adapter<SepetAdapter.CardTasarimTutucu>()
{

    inner class CardTasarimTutucu(tasarim: View) : RecyclerView.ViewHolder(tasarim){
        var sepet_card: CardView
        var card_yemek_resim: ImageView
        var card_yemek_adi: TextView
        var card_yemek_fiyat: TextView
        var sepetYemekAdetEkle : Button
        var yemek_sil: ImageButton
        var yemek_adet: TextView


        init {
            sepet_card = tasarim.findViewById(R.id.sepet_yemek_card)
            card_yemek_resim = tasarim.findViewById(R.id.sepet_yemek_resim)
            card_yemek_adi = tasarim.findViewById(R.id.sepet_yemek_adi)
            card_yemek_fiyat = tasarim.findViewById(R.id.sepet_yemek_fiyat)
            sepetYemekAdetEkle = tasarim.findViewById(R.id.sepetAdetEkle)
            yemek_sil = tasarim.findViewById(R.id.yemek_sil)
            yemek_adet = tasarim.findViewById(R.id.sepetYemekAdet)

        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardTasarimTutucu {
        val tasarim = LayoutInflater.from(mContext)
                .inflate(R.layout.sepet_card,parent,false)
        return CardTasarimTutucu(tasarim)
    }

    override fun getItemCount(): Int {
        return sepettekilerListe.size
    }


    override fun onBindViewHolder(holder: CardTasarimTutucu, position: Int) {
        val sepet = sepettekilerListe.get(position)

        val url = "http://kasimadalan.pe.hu/yemekler/resimler/${sepet.yemek.yemek_resim_adi}"
        Picasso.get().load(url)
                .placeholder(R.drawable.yemek_placeholder)
                .error(R.drawable.yemek_resim_error)
                .into(holder.card_yemek_resim)

        holder.card_yemek_adi.text = "${sepet.yemek.yemek_adi}"
        holder.card_yemek_fiyat.text = "${sepet.yemek.yemek_fiyat } ₺ "
        holder.yemek_adet.text = "${sepet.yemek_siparis_adet}"

        holder.sepetYemekAdetEkle.setOnClickListener {
            sepeteEkle(sepet.yemek)
        }

        holder.yemek_sil.setOnClickListener {
            sepettenUrunSil(sepet.yemek)
        }


    }

    fun tumSepettekiYemekler(){
        var total = 0
        val url = "http://kasimadalan.pe.hu/yemekler/tum_sepet_yemekler.php"
        val istek = StringRequest(Request.Method.GET, url,Response.Listener { cevap ->
            try {

                val tempListe = ArrayList<SepettekiYemekler>()

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

                    total += yemek_fiyati

                    var y = Yemekler(yemek_id, yemek_adi, yemek_resim_adi, yemek_fiyati)
                    var yemek = SepettekiYemekler(y, yemek_siparis_adet )
                    tempListe.add(yemek)
                }

                sepettekilerListe = tempListe
                notifyDataSetChanged()

                sepetTutari.sepetTutariniGuncelle(total)

            }catch (e: JSONException){

            }
        },Response.ErrorListener {Log.e("Hata", "Veri okuma")})

        Volley.newRequestQueue(mContext).add(istek)
    }

    fun sepeteEkle(yemek: Yemekler){
        val url = "http://kasimadalan.pe.hu/yemekler/insert_sepet_yemek.php"
        val istek = object: StringRequest(Request.Method.POST,url,Response.Listener { cevap ->

            Toast.makeText(mContext, "1 ${mContext.getResources().getString(R.string.adet)} ${yemek.yemek_adi} ${mContext.getResources().getString(R.string.sepete_eklendi)}", Toast.LENGTH_LONG).show()
            tumSepettekiYemekler()

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

    fun sepettenUrunSil(yemek: Yemekler, isAll : Boolean = false){
        val url = "http://kasimadalan.pe.hu/yemekler/delete_sepet_yemek.php"
        val istek = object: StringRequest(Request.Method.POST,url,Response.Listener { cevap ->
            if(!isAll)
                Toast.makeText(mContext, "${yemek.yemek_adi} ${mContext.getResources().getString(R.string.sepetten_silindi)}", Toast.LENGTH_LONG).show()
            tumSepettekiYemekler()

        },Response.ErrorListener { Log.e("Ekle", "Hata") }){
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String,String>()
                params["yemek_id"] = yemek.yemek_id.toString()
                return params
            }
        }

        Volley.newRequestQueue(mContext).add(istek)
    }

}