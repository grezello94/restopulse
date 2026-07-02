package com.redlantern.restopulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.redlantern.restopulse.ui.RestoPulseRoot
import com.redlantern.restopulse.ui.theme.RestoPulseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            RestoPulseTheme {
                RestoPulseRoot()
            }
        }
    }
}
