package com.aramoney.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class untuk Ara Money.
 * Menginisialisasi Dagger-Hilt container untuk manajemen dependensi Room & DataStore.
 */
@HiltAndroidApp
class AraMoneyApp : Application()
