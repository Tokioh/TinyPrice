package com.apps.tinyprice

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apps.tinyprice.presentation.deals.DealsApp
import com.apps.tinyprice.presentation.deals.DealsViewModel
import com.apps.tinyprice.ui.theme.TinyPriceTheme

class MainActivity : ComponentActivity() {
    private val viewModel: DealsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            TinyPriceTheme {
                DealsApp(state = state, onEvent = viewModel::onEvent)
            }
        }
    }
}