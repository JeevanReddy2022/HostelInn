package com.example.hostelmanager.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

// ✅ FIX: Default to null instead of throwing an error. This prevents white-screen crashes.
val LocalAppContainer = staticCompositionLocalOf<AppContainer?> { null }

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val container = LocalAppContainer.current
        ?: error("AppContainer not provided. Ensure CompositionLocalProvider(LocalAppContainer provides ...) wraps the NavHost.")
    return viewModel(factory = AppViewModelProvider.factory(container))
}