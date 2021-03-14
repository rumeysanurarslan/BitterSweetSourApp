package com.example.yemeksiparisprojesi

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import kotlinx.android.synthetic.main.activity_harita.*
import kotlinx.android.synthetic.main.activity_sepeti_goruntule.*

class HaritaActivity : AppCompatActivity(), OnMapReadyCallback {
    private var izinKontrol:Int = 0
    private lateinit var flpc: FusedLocationProviderClient
    private lateinit var locationTask: Task<Location>
    private lateinit var mMap: GoogleMap
    private var Enlem: Double = 0.0
    private var Boylam: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_harita)
        flpc = LocationServices.getFusedLocationProviderClient(this)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        izinKontrol = ContextCompat.checkSelfPermission(this@HaritaActivity, Manifest.permission.ACCESS_FINE_LOCATION)

        if(izinKontrol != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this@HaritaActivity
                , arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),100)
        }

        haritaToolbarLogo.setOnClickListener{
            startActivity(Intent(this@HaritaActivity,MainActivity::class.java))
        }

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


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode === 100){
            izinKontrol = ContextCompat.checkSelfPermission(this@HaritaActivity, Manifest.permission.ACCESS_FINE_LOCATION)

            if(grantResults.size > 0 && grantResults[0] === PackageManager.PERMISSION_GRANTED){
                Toast.makeText(applicationContext,R.string.izin_kabul_edildi, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(applicationContext,R.string.izin_rededildi, Toast.LENGTH_LONG).show()
//                startActivity(Intent(this@HaritaActivity,MainActivity::class.java))

            }
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Add a marker in Sydney and move the camera
        val location = LatLng(40.95, 29.11)
        mMap.addMarker(MarkerOptions().position(location)
            .title("Turkcell")
            .icon(BitmapDescriptorFactory.fromResource(R.drawable.logo)))
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 17f))
    }
}