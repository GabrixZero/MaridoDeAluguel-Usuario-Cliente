package com.example.doesitusuario

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.ui.navigation.SetupNavGraph
import com.example.doesitusuario.ui.theme.DoesItUsuarioTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            SessionManager.init(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setContent {
            DoesItUsuarioTheme {
                SetupNavGraph()
            }
        }
    }
}
