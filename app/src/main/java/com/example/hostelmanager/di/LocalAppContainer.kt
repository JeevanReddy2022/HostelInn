package com.example.hostelmanager.di

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

val LocalAppContainer = staticCompositionLocalOf<AppContainer> {
    error("No AppContainer provided")
}

@Composable
inline fun <reified VM : ViewModel> appViewModel(): VM {
    val container = LocalAppContainer.current
    return viewModel(factory = AppViewModelProvider.factory(container))
}
