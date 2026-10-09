package com.aramoney.app.presentation.components

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

/**
 * Pengendali Snackbar global aplikasi.
 *
 * Dipasang sekali di AraNavGraph sehingga feedback (mis. "Transaksi tersimpan" / "Dihapus — Urungkan")
 * tetap tampil walaupun bottom sheet atau dialog pemicunya sudah tertutup.
 */
@Stable
class AraSnackbarController(
    val hostState: SnackbarHostState,
    private val scope: CoroutineScope
) {
    fun show(
        message: String,
        actionLabel: String? = null,
        onAction: (() -> Unit)? = null
    ) {
        scope.launch {
            // Ganti snackbar lama agar feedback terbaru langsung terlihat
            hostState.currentSnackbarData?.dismiss()
            val result = hostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                withDismissAction = actionLabel == null,
                duration = if (actionLabel != null) SnackbarDuration.Long else SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                onAction?.invoke()
            }
        }
    }
}

@Composable
fun rememberAraSnackbarController(): AraSnackbarController {
    val hostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) { AraSnackbarController(hostState, scope) }
}

val LocalAraSnackbar = staticCompositionLocalOf<AraSnackbarController?> { null }
