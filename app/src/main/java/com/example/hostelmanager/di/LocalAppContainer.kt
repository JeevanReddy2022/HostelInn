package com.example.hostelmanager.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// Safe default: null instead of throwing. A missing provider becomes a clear,
// non-crashing message rather than a random IllegalStateException.
val LocalAppContainer = staticCompositionLocalOf<AppContainer?> { null }

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val container = LocalAppContainer.current
        ?: error("AppContainer not provided. Ensure CompositionLocalProvider(LocalAppContainer provides ...) wraps the NavHost.")
    return viewModel(factory = AppViewModelProvider.factory(container))
}