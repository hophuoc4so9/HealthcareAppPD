package com.example.healthcareapppd.presentation.ui.map

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.example.healthcareapppd.data.api.model.Facility
import com.example.healthcareapppd.data.api.getLatLng

object NavigationHelper {


    fun navigateToFacility(context: Context, facility: Facility) {
        val latLng = facility.getLatLng() ?: return
        val label = facility.name ?: "Cơ sở y tế"


        val gmmIntentUri = Uri.parse(
            "google.navigation:q=${latLng.latitude},${latLng.longitude}&mode=d"
        )

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Nếu không có app, mở trên trình duyệt web
            openGoogleMapsWeb(context, latLng.latitude, latLng.longitude, label)
        }
    }


    fun showOnMapOnly(context: Context, lat: Double, lng: Double, label: String) {
            val gmmIntentUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($label)")
        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            openGoogleMapsWeb(context, lat, lng, label)
        }
    }


    private fun openGoogleMapsWeb(context: Context, lat: Double, lng: Double, label: String?) {
        val uri = if (label != null) {
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&destination_place_id=$label")
        } else {
            Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        }

        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    }


    fun isGoogleMapsInstalled(context: Context): Boolean {
        return try {
            context.packageManager.getPackageInfo("com.google.android.apps.maps", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    fun startTurnByTurnNavigation(context: Context, facility: Facility) {
        val latLng = facility.getLatLng() ?: return

        // URI này sẽ mở turn-by-turn navigation trực tiếp
        val gmmIntentUri = Uri.parse(
            "google.navigation:q=${latLng.latitude},${latLng.longitude}"
        )

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        if (mapIntent.resolveActivity(context.packageManager) != null) {
            context.startActivity(mapIntent)
        } else {
            // Fallback: hiển thị dialog để tải Google Maps
            AlertDialog.Builder(context)
                .setTitle("Cần Google Maps")
                .setMessage("Bạn cần cài đặt Google Maps để sử dụng tính năng điều hướng.")
                .setPositiveButton("Tải Google Maps") { _, _ ->
                    openPlayStore(context, "com.google.android.apps.maps")
                }
                .setNegativeButton("Mở trên web") { _, _ ->
                    openGoogleMapsWeb(context, latLng.latitude, latLng.longitude, facility.name)
                }
                .show()
        }
    }


    private fun openPlayStore(context: Context, packageName: String) {
        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("market://details?id=$packageName")
                )
            )
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }
}