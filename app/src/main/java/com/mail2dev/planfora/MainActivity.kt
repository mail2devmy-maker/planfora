package com.mail2dev.planfora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mail2dev.planfora.data.local.AppDatabase
import com.mail2dev.planfora.data.repository.JournalRepository
import com.mail2dev.planfora.data.repository.SupplyRepository
import com.mail2dev.planfora.ui.assets.AddAssetViewModel
import com.mail2dev.planfora.ui.assets.AddAssetViewModelFactory
import com.mail2dev.planfora.ui.assets.AssetsViewModel
import com.mail2dev.planfora.ui.assets.AssetsViewModelFactory
import com.mail2dev.planfora.ui.components.PlanForaBottomBar
import com.mail2dev.planfora.ui.logs.LogsViewModel
import com.mail2dev.planfora.ui.logs.LogsViewModelFactory
import com.mail2dev.planfora.ui.navigation.Screen
import com.mail2dev.planfora.ui.profile.ProfileViewModel
import com.mail2dev.planfora.ui.profile.ProfileViewModelFactory
import com.mail2dev.planfora.ui.screens.AssetsScreen
import com.mail2dev.planfora.ui.screens.LogsScreen
import com.mail2dev.planfora.ui.screens.NewLogEntryScreen
import com.mail2dev.planfora.ui.screens.PlantDetailScreen
import com.mail2dev.planfora.ui.screens.ProfileScreen
import com.mail2dev.planfora.ui.screens.SuppliesScreen
import com.mail2dev.planfora.ui.screens.SupplyDetailScreen
import com.mail2dev.planfora.ui.supplies.AddSupplyViewModel
import com.mail2dev.planfora.ui.supplies.AddSupplyViewModelFactory
import com.mail2dev.planfora.ui.supplies.SuppliesViewModel
import com.mail2dev.planfora.ui.supplies.SuppliesViewModelFactory
import com.mail2dev.planfora.ui.theme.PlanForaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val database = AppDatabase.getDatabase(this)
        val supplyRepository = SupplyRepository(database.diySupplyDao())
        val journalRepository = JournalRepository(
            database.journalLogDao(),
            database.plantAssetDao(),
            database.masterDao(),
            database.customFieldDao()
        )

        enableEdgeToEdge()
        setContent {
            PlanForaTheme(darkTheme = true) {
                val navController = rememberNavController()
                
                val suppliesViewModel: SuppliesViewModel = viewModel(
                    factory = SuppliesViewModelFactory(supplyRepository)
                )

                val addSupplyViewModel: AddSupplyViewModel = viewModel(
                    factory = AddSupplyViewModelFactory(supplyRepository, journalRepository)
                )
                
                val logsViewModel: LogsViewModel = viewModel(
                    factory = LogsViewModelFactory(journalRepository, supplyRepository)
                )

                val assetsViewModel: AssetsViewModel = viewModel(
                    factory = AssetsViewModelFactory(journalRepository)
                )

                val addAssetViewModel: AddAssetViewModel = viewModel(
                    factory = AddAssetViewModelFactory(journalRepository)
                )

                val profileViewModel: ProfileViewModel = viewModel(
                    factory = ProfileViewModelFactory(journalRepository, supplyRepository, database)
                )
                
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { PlanForaBottomBar(navController) }
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = Screen.Logs.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Logs.route) { LogsScreen(navController, logsViewModel, profileViewModel) }
                        composable(Screen.Assets.route) { 
                            AssetsScreen(navController, assetsViewModel, addAssetViewModel) 
                        }
                        composable(Screen.Supplies.route) { 
                            SuppliesScreen(navController, suppliesViewModel, addSupplyViewModel)
                        }
                        composable(Screen.Profile.route) { ProfileScreen(profileViewModel) }
                        composable(
                            route = Screen.NewLog.route,
                            arguments = listOf(
                                navArgument("parentLogId") { 
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("timestamp") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("assetId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("assetIds") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                },
                                navArgument("editingLogId") {
                                    type = NavType.StringType
                                    nullable = true
                                    defaultValue = null
                                }
                            )
                        ) { backStackEntry ->
                            val parentLogId = backStackEntry.arguments?.getString("parentLogId")?.toLongOrNull()
                            val timestamp = backStackEntry.arguments?.getString("timestamp")?.toLongOrNull()
                            val assetId = backStackEntry.arguments?.getString("assetId")?.toLongOrNull()
                            val assetIds = backStackEntry.arguments?.getString("assetIds")
                            val editingLogId = backStackEntry.arguments?.getString("editingLogId")?.toLongOrNull()
                            NewLogEntryScreen(navController, logsViewModel, parentLogId, timestamp, assetId, assetIds, editingLogId)
                        }
                        composable(
                            route = Screen.PlantDetail.route,
                            arguments = listOf(navArgument("plantId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val plantId = backStackEntry.arguments?.getLong("plantId") ?: 0L
                            PlantDetailScreen(plantId, navController, logsViewModel, assetsViewModel, addAssetViewModel, profileViewModel)
                        }
                        composable(
                            route = Screen.SupplyDetail.route,
                            arguments = listOf(navArgument("supplyId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val supplyId = backStackEntry.arguments?.getLong("supplyId") ?: 0L
                            SupplyDetailScreen(supplyId, navController, suppliesViewModel, addSupplyViewModel, logsViewModel)
                        }
                    }
                }
            }
        }
    }
}
