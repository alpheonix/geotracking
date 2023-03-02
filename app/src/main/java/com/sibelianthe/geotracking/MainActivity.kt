package com.sibelianthe.geotracking

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.redundent.kotlin.xml.XmlVersion
import org.redundent.kotlin.xml.xml
import java.io.File
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*


class MainActivity : AppCompatActivity() {

	private lateinit var locationManager: LocationManager
	private val locationPermissionCode = 2
	private lateinit var button: Button
	private lateinit var webView: WebView
	private var temp = 0
	private val service = BackgroundService()

	companion object {
		var locationList = mutableListOf<LocationData>()

	}


	@SuppressLint("SetJavaScriptEnabled")
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)
		val intent = Intent(applicationContext, BackgroundService::class.java)
		startService(intent)
		button = findViewById(R.id.export)
		button.setOnClickListener {
			service.export(this)
			exportData(locationList)
		}

		setupPermissions()

		webView = findViewById(R.id.spring)

		//setup de la webview

		webView.settings.javaScriptEnabled = true
		webView.settings.domStorageEnabled = true
		webView.settings.javaScriptCanOpenWindowsAutomatically = true
		webView.settings.databaseEnabled = true
		webView.loadUrl("https://thespringprogram.com/")


	}

	override fun onDestroy() {
		super.onDestroy()
		//exportData(BackgroundService.locationList)
	}

	private fun setupPermissions() {
		ContextCompat.checkSelfPermission(
			this,
			Manifest.permission.ACCESS_COARSE_LOCATION
		)

		ContextCompat.checkSelfPermission(
			this,
			Manifest.permission.ACCESS_FINE_LOCATION
		)
	}


	//verification des permission et demande d'ajout de permission
	override fun onRequestPermissionsResult(
		requestCode: Int, permissions: Array<out String>, grantResults: IntArray
	) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults)
		if (requestCode == locationPermissionCode) {
			if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
				Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
			} else {
				Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
			}
		}
	}

	// creation du fichier dans lequel sera placé le chemin effectué
	fun exportData(locationList: MutableList<LocationData>) {
		Log.d("fronttask", locationList.size.toString())

		val xml = generateXml(locationList)
		val path = this.getExternalFilesDir(null)
		val fileName = DateTimeFormatter.ISO_INSTANT.format(
			Instant.now()
		) + "tracking.gpx"
		val folder = File(path, "track")
		folder.mkdirs()

		val file = File(folder, fileName)
		file.appendText(xml)

		val text: String = String.format(getString(R.string.fichier_cree), path.toString())

		Toast.makeText(this, text, Toast.LENGTH_LONG).show()
		//locationList.clear()

	}

	//generation du contenue du futur fichier GPX en utilisant https://github.com/redundent/kotlin-xml-builder
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


}



