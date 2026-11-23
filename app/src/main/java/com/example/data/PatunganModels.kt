package com.example.data

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

enum class PatunganArea(
    val areaName: String,
    val subZones: List<String>,
    val lat: Double,
    val lng: Double
) {
    DAGO(
        areaName = "Dago",
        subZones = listOf("Dago Atas", "Dago Bawah", "Dago Pojok", "Cisitu Dago"),
        lat = -6.8904,
        lng = 107.6134
    ),
    DIPATIUKUR(
        areaName = "Dipatiukur",
        subZones = listOf("Dipatiukur Utara", "Dipatiukur Selatan", "Sekitar ITB"),
        lat = -6.8935,
        lng = 107.6176
    ),
    CIHAMPELAS(
        areaName = "Cihampelas",
        subZones = listOf("Cihampelas Atas", "Cihampelas Bawah", "Tamansari"),
        lat = -6.8958,
        lng = 107.6038
    ),
    BUAH_BATU(
        areaName = "Buah Batu",
        subZones = listOf("Buah Batu Tengah", "Buah Batu Selatan", "Margacinta"),
        lat = -6.9338,
        lng = 107.6245
    ),
    CIBIRU(
        areaName = "Cibiru",
        subZones = listOf("Cibiru Wetan", "Cibiru Hilir", "Bundaran Cibiru"),
        lat = -6.9316,
        lng = 107.7176
    )
}

enum class PatunganStatus(val label: String) {
    OPEN("Menunggu Teman"),
    BERANGKAT("Patungan Berangkat!"),
    GAGAL_PENUH("Patungan Belum Penuh"),
    DIBATALKAN("Dibatalkan"),
    SELESAI("Selesai • H+1")
}

enum class DriverStage(val stepIndex: Int, val label: String) {
    MENUJU_TOKO(0, "Driver Menuju Toko"),
    AMBIL_KANTONG(1, "Driver Ambil 5 Kantong"),
    DALAM_PERJALANAN(2, "Driver Dalam Perjalanan"),
    SAMPAI(3, "Sampai")
}

enum class PatunganPaymentOption(val label: String, val subtitle: String) {
    QRIS("QRIS", "Scan QR semua e-wallet & m-banking"),
    GOPAY("GoPay", "Bayar instan GoPay"),
    OVO("OVO", "Bayar instan OVO"),
    DANA("DANA", "Bayar instan DANA"),
    SALDO_LUDES("Saldo LUDES", "Dana ditahan aman di Saldo LUDES")
}

data class PatunganMember(
    val slotIndex: Int,
    val aliasName: String,
    val areaDetail: String,
    val addressLabel: String,
    val distanceKmFromStore: Double,
    val isCurrentUser: Boolean = false,
    val isDelivered: Boolean = false,
    val paidAmountRp: Int = 13000
)

data class PatunganSplitBreakdown(
    val userTotalPaidRp: Int,
    val bagPriceRp: Int,
    val patunganShippingRp: Int,
    val merchantReceivesRp: Int,
    val ludesBagFeeRp: Int,
    val driverReceivesRp: Int,
    val ludesPatunganFeeRp: Int,
    val ludesTotalFeeRp: Int
)

fun calculatePatunganSplit(
    bagPriceRp: Int,
    patunganShippingRp: Int = 3000
): PatunganSplitBreakdown {
    val ludesBagFee = (bagPriceRp * 10) / 100 // 10% fee kantong (Rp1.000 dari Rp10.000)
    val merchantReceives = bagPriceRp - ludesBagFee // Rp9.000 dari Rp10.000
    val ludesPatunganFee = 500 // Rp500 fee atur patungan
    val driverReceives = (patunganShippingRp - ludesPatunganFee).coerceAtLeast(0) // Rp2.500 dari Rp3.000
    return PatunganSplitBreakdown(
        userTotalPaidRp = bagPriceRp + patunganShippingRp,
        bagPriceRp = bagPriceRp,
        patunganShippingRp = patunganShippingRp,
        merchantReceivesRp = merchantReceives,
        ludesBagFeeRp = ludesBagFee,
        driverReceivesRp = driverReceives,
        ludesPatunganFeeRp = ludesPatunganFee,
        ludesTotalFeeRp = ludesBagFee + ludesPatunganFee
    )
}

fun resolvePatunganAreaForProduct(product: ProductEntity): PatunganArea {
    val text = "${product.storeName} ${product.storeAddress}".lowercase()
    return when {
        "buah batu" in text -> PatunganArea.BUAH_BATU
        "cibiru" in text -> PatunganArea.CIBIRU
        "cihampelas" in text -> PatunganArea.CIHAMPELAS
        "dipatiukur" in text -> PatunganArea.DIPATIUKUR
        else -> PatunganArea.DAGO
    }
}

fun resolveUserPatunganArea(locationLabel: String, lat: Double, lng: Double): PatunganArea {
    val lower = locationLabel.lowercase()
    return when {
        "buah batu" in lower -> PatunganArea.BUAH_BATU
        "cibiru" in lower -> PatunganArea.CIBIRU
        "cihampelas" in lower -> PatunganArea.CIHAMPELAS
        "dipatiukur" in lower -> PatunganArea.DIPATIUKUR
        "dago" in lower || "cisitu" in lower -> PatunganArea.DAGO
        else -> PatunganArea.entries.minByOrNull { area ->
            haversineDistanceKm(lat, lng, area.lat, area.lng)
        } ?: PatunganArea.DAGO
    }
}

fun isPatunganAreaCompatible(
    userLocationLabel: String,
    userLat: Double,
    userLng: Double,
    groupArea: PatunganArea
): Boolean {
    val userArea = resolveUserPatunganArea(userLocationLabel, userLat, userLng)
    if (userArea == groupArea) return true
    // Strictly never mix distant zones (e.g., Dago vs Buah Batu / Cibiru)
    val distKm = haversineDistanceKm(userLat, userLng, groupArea.lat, groupArea.lng)
    return distKm <= 1.0
}

fun formatDistanceKmCompact(km: Double): String {
    return if (km < 1.0) {
        "${(km * 1000).toInt()}m"
    } else {
        String.format(Locale.US, "%.1f km", km)
    }
}

fun serializePatunganMembers(members: List<PatunganMember>): String {
    val arr = JSONArray()
    members.forEach { m ->
        arr.put(
            JSONObject().apply {
                put("slotIndex", m.slotIndex)
                put("aliasName", m.aliasName)
                put("areaDetail", m.areaDetail)
                put("addressLabel", m.addressLabel)
                put("distanceKmFromStore", m.distanceKmFromStore)
                put("isCurrentUser", m.isCurrentUser)
                put("isDelivered", m.isDelivered)
                put("paidAmountRp", m.paidAmountRp)
            }
        )
    }
    return arr.toString()
}

fun deserializePatunganMembers(json: String): List<PatunganMember> {
    if (json.isBlank()) return emptyList()
    return runCatching {
        val arr = JSONArray(json)
        buildList {
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                add(
                    PatunganMember(
                        slotIndex = obj.optInt("slotIndex", i + 1),
                        aliasName = obj.optString("aliasName", "Tetangga ${i + 1}"),
                        areaDetail = obj.optString("areaDetail", "Dago"),
                        addressLabel = obj.optString("addressLabel", "Komplek Dago"),
                        distanceKmFromStore = obj.optDouble("distanceKmFromStore", 0.5),
                        isCurrentUser = obj.optBoolean("isCurrentUser", false),
                        isDelivered = obj.optBoolean("isDelivered", false),
                        paidAmountRp = obj.optInt("paidAmountRp", 13000)
                    )
                )
            }
        }
    }.getOrDefault(emptyList())
}
