package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.repository.UserPreferencesRepository
import com.example.security.BiometricAuthManager
import com.example.security.PinManager
import com.example.ui.components.PortfolioBottomNav
import com.example.ui.components.PortfolioTab
import com.example.ui.components.ProvidePrivacyMode
import com.example.ui.screens.AddPurchaseScreen
import com.example.ui.screens.BiometricEnableScreen
import com.example.ui.screens.GoldDollarScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PinEntryScreen
import com.example.ui.screens.PinSetupScreen
import com.example.ui.screens.PortfolioHomeScreen
import com.example.ui.screens.StockMarketScreen
import com.example.ui.viewmodel.PortfolioViewModel
import kotlinx.coroutines.launch

/** Local (in-app) security flow steps, run once per cold start after onboarding. */
private enum class LockStep { PIN_SETUP, BIOMETRIC_OPT_IN, PIN_ENTRY, UNLOCKED }

/**
 * Root composable for the portfolio app: onboarding once, then a PIN/biometric security
 * gate every cold start (see LockStep), then a 4-tab bottom nav
 * (Home / Gold & Dollar / Stock market / Add purchase) — matches the approved wireframe.
 * Uses plain tab-switch state instead of Navigation Compose since these 4 destinations
 * are flat and don't need a back stack.
 */
@Composable
fun PortfolioApp(
    viewModel: PortfolioViewModel,
    userPreferencesRepository: UserPreferencesRepository,
    biometricAuthManager: BiometricAuthManager,
    pinManager: PinManager,
    onExportRequested: () -> Unit,
    onImportRequested: () -> Unit
) {
    val isOnboardingCompleted by userPreferencesRepository.isOnboardingCompleted.collectAsStateWithLifecycle(initialValue = false)
    var currentTab by remember { mutableStateOf(PortfolioTab.HOME) }
    val scope = rememberCoroutineScopeCompat()

    var lockStep by remember {
        mutableStateOf(if (pinManager.isPinSet()) LockStep.PIN_ENTRY else LockStep.PIN_SETUP)
    }

    LaunchedEffect(Unit) {
        viewModel.refreshAll(watchlistSymbols = emptyList())
    }

    if (!isOnboardingCompleted) {
        OnboardingScreen(
            onFinishOnboarding = {
                scope.launch { userPreferencesRepository.setOnboardingCompleted(true) }
            }
        )
        return
    }

    when (lockStep) {
        LockStep.PIN_SETUP -> {
            PinSetupScreen(
                onPinCreated = { pin ->
                    pinManager.setPin(pin)
                    lockStep = if (biometricAuthManager.isBiometricAvailable()) {
                        LockStep.BIOMETRIC_OPT_IN
                    } else {
                        LockStep.UNLOCKED
                    }
                }
            )
            return
        }
        LockStep.BIOMETRIC_OPT_IN -> {
            BiometricEnableScreen(
                onEnable = {
                    biometricAuthManager.authenticate(
                        onSuccess = {
                            pinManager.setBiometricEnabled(true)
                            lockStep = LockStep.UNLOCKED
                        },
                        onError = { /* stay on this screen; person can retry or skip */ }
                    )
                },
                onSkip = { lockStep = LockStep.UNLOCKED }
            )
            return
        }
        LockStep.PIN_ENTRY -> {
            PinEntryScreen(
                biometricEnabled = pinManager.isBiometricEnabled(),
                onVerifyPin = { pin -> pinManager.verifyPin(pin) },
                onUnlocked = { lockStep = LockStep.UNLOCKED },
                onBiometricRequested = {
                    biometricAuthManager.authenticate(
                        onSuccess = { lockStep = LockStep.UNLOCKED },
                        onError = { /* fall back to typing the PIN */ }
                    )
                }
            )
            return
        }
        LockStep.UNLOCKED -> Unit
    }

    val isPrivacyModeEnabled by userPreferencesRepository.isPrivacyModeEnabled.collectAsStateWithLifecycle(initialValue = false)

    ProvidePrivacyMode(enabled = isPrivacyModeEnabled) {
        Scaffold(
            bottomBar = { PortfolioBottomNav(currentTab = currentTab, onTabSelected = { currentTab = it }) }
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                when (currentTab) {
                    PortfolioTab.HOME -> PortfolioHomeScreen(
                        viewModel = viewModel,
                        onExportRequested = onExportRequested,
                        onImportRequested = onImportRequested,
                        isPrivacyModeEnabled = isPrivacyModeEnabled,
                        onTogglePrivacyMode = {
                            scope.launch { userPreferencesRepository.setPrivacyModeEnabled(!isPrivacyModeEnabled) }
                        }
                    )
                    PortfolioTab.GOLD_DOLLAR -> GoldDollarScreen(viewModel)
                    PortfolioTab.STOCK -> StockMarketScreen(viewModel)
                    PortfolioTab.ADD_PURCHASE -> AddPurchaseScreen(viewModel)
                }
            }
        }
    }
}

@Composable
private fun rememberCoroutineScopeCompat() = androidx.compose.runtime.rememberCoroutineScope()
