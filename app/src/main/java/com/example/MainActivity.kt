package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.navigation.AppNavigation
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable Edge-to-Edge full bleeding content layouts
        enableEdgeToEdge()
        
        setContent {
            MyApplicationTheme {
                // Determine responsive layout breakpoints (tablet vs portrait smartphone)
                val isExpanded = resources.configuration.smallestScreenWidthDp >= 600

                AppNavigation(isExpandedScreen = isExpanded)
            }
        }
    }
}
