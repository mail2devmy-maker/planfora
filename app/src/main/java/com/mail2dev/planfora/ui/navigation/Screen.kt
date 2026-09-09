package com.mail2dev.planfora.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Logs : Screen("logs", "Logs", Icons.Rounded.EventNote)
    object Assets : Screen("assets", "Assets", Icons.Rounded.Forest)
    object Supplies : Screen("supplies", "Supplies", Icons.Rounded.Inventory2)
    object Profile : Screen("profile", "Profile", Icons.Rounded.Person)
    object NewLog : Screen("new_log?parentLogId={parentLogId}&timestamp={timestamp}&assetId={assetId}&assetIds={assetIds}&editingLogId={editingLogId}", "New Log", Icons.Rounded.Add) {
        fun createRoute(parentLogId: Long? = null, timestamp: Long? = null, assetId: Long? = null, assetIds: String? = null, editingLogId: Long? = null): String {
            val builder = StringBuilder("new_log")
            val params = mutableListOf<String>()
            parentLogId?.let { params.add("parentLogId=$it") }
            timestamp?.let { params.add("timestamp=$it") }
            assetId?.let { params.add("assetId=$it") }
            assetIds?.let { params.add("assetIds=$it") }
            editingLogId?.let { params.add("editingLogId=$it") }
            if (params.isNotEmpty()) {
                builder.append("?").append(params.joinToString("&"))
            }
            return builder.toString()
        }
    }
    object PlantDetail : Screen("plant_detail/{plantId}", "Plant Detail", Icons.Rounded.Grass) {
        fun createRoute(plantId: Long) = "plant_detail/$plantId"
    }
    object SupplyDetail : Screen("supply_detail/{supplyId}", "Supply Detail", Icons.Rounded.Inventory2) {
        fun createRoute(supplyId: Long) = "supply_detail/$supplyId"
    }
}

val bottomNavItems = listOf(
    Screen.Logs,
    Screen.Assets,
    Screen.Supplies,
    Screen.Profile
)
