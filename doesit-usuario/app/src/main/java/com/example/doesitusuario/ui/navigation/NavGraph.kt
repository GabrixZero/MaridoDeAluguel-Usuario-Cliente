package com.example.doesitusuario.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.ui.screens.addresses.AddressFormScreen
import com.example.doesitusuario.ui.screens.addresses.AddressScreen
import com.example.doesitusuario.ui.screens.history.HistoryScreen
import com.example.doesitusuario.ui.screens.home.HomeScreen
import com.example.doesitusuario.ui.screens.login.LoginScreen
import com.example.doesitusuario.ui.screens.notifications.NotificationScreen
import com.example.doesitusuario.ui.screens.orders.AvailableProvidersScreen
import com.example.doesitusuario.ui.screens.orders.OrderDetailScreen
import com.example.doesitusuario.ui.screens.orders.OrderScreen
import com.example.doesitusuario.ui.screens.payments.PaymentScreen
import com.example.doesitusuario.ui.screens.profiles.ProfileScreen
import com.example.doesitusuario.ui.screens.login.RegisterScreen
import com.example.doesitusuario.ui.screens.settings.SettingScreen

@Composable
fun SetupNavGraph() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = "login") {

        composable("login") { backStackEntry ->
            val msg = backStackEntry.savedStateHandle.get<String>("success_msg")
            LoginScreen(
                onLoginSuccess        = { nav.navigate("home") { popUpTo("login") { inclusive = true } } },
                onGoToCadastro        = { nav.navigate("register") },
                initialSuccessMessage = msg
            )
            backStackEntry.savedStateHandle.remove<String>("success_msg")
        }

        composable("register") {
            RegisterScreen(
                onBackToLogin     = { nav.popBackStack() },
                onRegisterSuccess = { message ->
                    nav.previousBackStackEntry?.savedStateHandle?.set("success_msg", message)
                    nav.popBackStack()
                }
            )
        }

        composable("home") {
            HomeScreen(
                onNavigateToSettings         = { nav.navigate("settings") },
                onNavigateToNotifications    = { nav.navigate("notifications") },
                onNavigateToSolicitarServico = { nav.navigate("order") },
                onNavigateToPagamentos       = { nav.navigate("payments") },
                onNavigateToHistory          = { nav.navigate("history") },
                onNavigateToEnderecos        = { nav.navigate("addresses") },
                onNavigateToProfile          = { nav.navigate("profile") }
            )
        }

        composable("order") {
            OrderScreen(
                onBack = { nav.popBackStack() },
                onNavigateToProviders = { catId, onlyWomen, mode, date, time, addrId, comment, basePrice ->
                    val d = date ?: "null"
                    val t = time ?: "null"
                    nav.navigate("providers/$catId/$onlyWomen/$mode?date=$d&time=$t&addrId=$addrId&comment=${comment.ifBlank { "" }}&basePrice=$basePrice")
                }
            )
        }

        composable(
            route = "providers/{catId}/{onlyWomen}/{mode}?date={date}&time={time}&addrId={addrId}&comment={comment}&basePrice={basePrice}",
            arguments = listOf(
                navArgument("catId")     { type = NavType.LongType },
                navArgument("onlyWomen") { type = NavType.BoolType },
                navArgument("mode")      { type = NavType.StringType },
                navArgument("date")      { nullable = true; defaultValue = null },
                navArgument("time")      { nullable = true; defaultValue = null },
                navArgument("addrId")    { type = NavType.LongType; defaultValue = 0L },
                navArgument("comment")   { defaultValue = "" },
                navArgument("basePrice") { type = NavType.FloatType; defaultValue = 0f }
            )
        ) { back ->
            val catId     = back.arguments?.getLong("catId") ?: 0L
            val onlyWomen = back.arguments?.getBoolean("onlyWomen") ?: false
            val mode      = back.arguments?.getString("mode") ?: "Agora"
            val date      = back.arguments?.getString("date")?.takeIf { it != "null" }
            val time      = back.arguments?.getString("time")?.takeIf { it != "null" }
            val addrId    = back.arguments?.getLong("addrId") ?: 0L
            val comment   = back.arguments?.getString("comment") ?: ""
            val basePrice = (back.arguments?.getFloat("basePrice") ?: 0f).toDouble()

            AvailableProvidersScreen(
                catId     = catId,
                onlyWomen = onlyWomen,
                mode      = mode,
                date      = date,
                time      = time,
                addrId    = addrId,
                comment   = comment,
                basePrice = basePrice,
                onBack    = { nav.popBackStack() },
                onConfirm = {
                    nav.navigate("home") { popUpTo("home") { inclusive = true } }
                }
            )
        }

        composable("history") {
            HistoryScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToProfile = { nav.navigate("profile") },
                onNavigateToDetail  = { id -> nav.navigate("order-detail/$id") },
                onBack              = { nav.popBackStack() }
            )
        }

        composable("order-detail/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) { back ->
            val id = back.arguments?.getLong("id") ?: 0L
            OrderDetailScreen(
                orderId             = id,
                onBack              = { nav.popBackStack() },
                onNavigateToHistory = { nav.navigate("history") { popUpTo("history") { inclusive = true } } }
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onBack              = { nav.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationScreen(onBack = { nav.popBackStack() })
        }

        composable("settings") {
            SettingScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onNavigateToProfile = { nav.navigate("profile") },
                onLogout = { message ->
                    SessionManager.clear()
                    nav.navigate("login") { popUpTo(0) { inclusive = true } }
                    if (message != null)
                        nav.currentBackStackEntry?.savedStateHandle?.set("success_msg", message)
                },
                onBack = { nav.popBackStack() },
                onNavigateToHelp    = {},
                onNavigateToPhone   = {},
                onNavigateToPrivacy = {},
                onNavigateToTerms   = {}
            )
        }

        composable("payments") {
            PaymentScreen(
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onNavigateToProfile = { nav.navigate("profile") },
                onNavigateToAddCard = {},
                onBack              = { nav.popBackStack() }
            )
        }

        composable("addresses") {
            AddressScreen(
                onNavigateToHome        = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory     = { nav.navigate("history") },
                onNavigateToProfile     = { nav.navigate("profile") },
                onNavigateToAddAddress  = { nav.navigate("address-new") },
                onNavigateToEditAddress = { id -> nav.navigate("address-edit/$id") },
                onBack                  = { nav.popBackStack() }
            )
        }

        composable("address-new") {
            AddressFormScreen(
                onBack              = { nav.popBackStack() },
                onSuccess           = { _ -> nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }

        composable(
            route = "address-edit/{addressId}",
            arguments = listOf(navArgument("addressId") { type = NavType.LongType })
        ) { back ->
            val addressId = back.arguments?.getLong("addressId")
            AddressFormScreen(
                addressId           = addressId,
                onBack              = { nav.popBackStack() },
                onSuccess           = { _ -> nav.popBackStack() },
                onNavigateToHome    = { nav.navigate("home") { popUpTo("home") { inclusive = true } } },
                onNavigateToHistory = { nav.navigate("history") },
                onNavigateToProfile = { nav.navigate("profile") }
            )
        }
    }
}
