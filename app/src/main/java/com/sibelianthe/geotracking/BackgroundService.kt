package com.sibelianthe.geotracking

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter

class BackgroundService : Service(), LocationListener {

	private lateinit var locationManager: LocationManager
	var isGPSEnable = false
	private var isNetworkEnable = false
	private var locationList = mutableListOf<LocationData>()

	override fun onBind(intent: Intent?): IBinder? {
		return null
	}

	override fun onCreate() {
		super.onCreate()
		fn_getlocation()
	}

	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		return super.onStartCommand(intent, flags, startId)
	}

	override fun onLocationChanged(location: Location) {
		locationList.add(
			LocationData(
				location.longitude.toString(),
				location.latitude.toString(),
				DateTimeFormatter.ISO_INSTANT.format(
					Instant.now()
				)
			)
		)
		Log.d("backtask",locationList.toString())
		Log.d("backtask", "back")
	}

	private fun fn_getlocation() {
		locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
		isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
		isNetworkEnable = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

		if (ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_FINE_LOCATION
			) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
				this,
				Manifest.permission.ACCESS_COARSE_LOCATION
			) != PackageManager.PERMISSION_GRANTED
		) {

			return
		}
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10_000, 20f, this)

	}

	fun export(mainActivity: MainActivity) {
		exportData(mainActivity)
	}

	fun exportData(mainActivity: MainActivity) {
		Log.d("backtask",locationList.size.toString())
		val xml = generateXml(locationList)
		val path = mainActivity.getExternalFilesDir(null)
		val fileName = DateTimeFormatter.ISO_INSTANT.format(
			Instant.now()
		) + "tracking.gpx"
		val folder = File(path, "track")
		folder.mkdirs()

		val file = File(folder, fileName)
		file.appendText(xml)

	}

	private fun generateXml(locationList: MutableList<LocationData>): String {

		val gpx = xml("gpx") {
			attribute("version", "1.1")
			xmlns = "http://www.topografix.com/GPX/1/1"
			standalone = true
			version = XmlVersion.V11
			"metadata" {
				"name" { -"Spring Geotracker" }
				"author" {
					"name" { -"Spring Geotracker" }
					"link" {
						attribute("href", "https://thespringprogram.com/")
					}
				}
			}
			"trk" {
				"name" { -"Spring Geotracker" }
				"src" { -"Spring Geotracker" }
				"link" {
					attribute("href", "https://thespringprogram.com/")
				}
				locationList.forEach {
					"trkseg" {
						"trkpt" {
							attribute("lat", it.latitude)
							attribute("lon", it.longitude)
							"time" { -it.time }
						}
					}
				}
			}

		}
		return gpx.toString()
	}
	//necessaire pour les android en dessous de API lvl 30
	override fun onProviderEnabled(provider: String) {}

	override fun onProviderDisabled(provider: String) {}

	override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
}