package com.pomodoro.focus.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object FocusStateManager {
    private val _isFocusActive = MutableStateFlow(false)
    val isFocusActive = _isFocusActive.asStateFlow()

    private val _whitelistedPackages = MutableStateFlow<Set<String>>(setOf("com.whatsapp"))
    val whitelistedPackages = _whitelistedPackages.asStateFlow()

    fun setFocusActive(active: Boolean) {
        _isFocusActive.value = active
    }

    fun updateWhitelist(packages: Set<String>) {
        _whitelistedPackages.value = packages
    }
}
