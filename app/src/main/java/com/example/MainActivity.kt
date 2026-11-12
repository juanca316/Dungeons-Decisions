package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.ChronicleRepository
import com.example.data.DungeonsDatabase
import com.example.ui.DungeonsMainScreen
import com.example.ui.DungeonsViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(darkTheme = true, dynamicColor = false) {
                val context = LocalContext.current
                val repository = remember {
                    ChronicleRepository(DungeonsDatabase.getDatabase(context).chronicleDao())
                }
                val viewModel: DungeonsViewModel = viewModel(
                    factory = DungeonsViewModel.Factory(repository)
                )
                DungeonsMainScreen(viewModel = viewModel)
            }
        }
    }
}
