package com.aramoney.app.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.Category
import androidx.compose.material.icons.rounded.Celebration
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.LocalCafe
import androidx.compose.material.icons.rounded.LocalHospital
import androidx.compose.material.icons.rounded.LocalMall
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.Pets
import androidx.compose.material.icons.rounded.Redeem
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.Spa
import androidx.compose.material.icons.rounded.TwoWheeler
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.ui.graphics.vector.ImageVector

data class CategoryIconOption(
    val id: String,
    val label: String,
    val icon: ImageVector
)

/**
 * Daftar ikon bawaan resmi Ara Money yang dapat dipilih pengguna saat menambah/mengubah kategori.
 */
val availableCategoryIcons: List<CategoryIconOption> = listOf(
    CategoryIconOption("sparkles", "Skincare & Kecantikan", Icons.Rounded.Spa),
    CategoryIconOption("beverage", "Kopi & Boba", Icons.Rounded.LocalCafe),
    CategoryIconOption("food", "Makan & Kuliner", Icons.Rounded.Restaurant),
    CategoryIconOption("motorcycle", "Ojek Online", Icons.Rounded.TwoWheeler),
    CategoryIconOption("book", "Kuliah & Fotokopi", Icons.Rounded.Book),
    CategoryIconOption("gift", "Self Reward", Icons.Rounded.Redeem),
    CategoryIconOption("shopping_bag", "Belanja & Olshop", Icons.Rounded.ShoppingBag),
    CategoryIconOption("local_mall", "Mall & Hiburan", Icons.Rounded.LocalMall),
    CategoryIconOption("brush", "Make-up & Art", Icons.Rounded.Brush),
    CategoryIconOption("directions_bus", "Transportasi Umum", Icons.Rounded.DirectionsBus),
    CategoryIconOption("savings", "Nabung & Simpanan", Icons.Rounded.Savings),
    CategoryIconOption("medical", "Obat & Kesehatan", Icons.Rounded.LocalHospital),
    CategoryIconOption("fitness", "Gym & Olahraga", Icons.Rounded.FitnessCenter),
    CategoryIconOption("wifi", "Pulsa & Kuota", Icons.Rounded.Wifi),
    CategoryIconOption("favorite", "Hobi & Kesukaan", Icons.Rounded.Favorite),
    CategoryIconOption("pets", "Kucing & Anabul", Icons.Rounded.Pets),
    CategoryIconOption("celebration", "Nongkrong & Pesta", Icons.Rounded.Celebration),
    CategoryIconOption("account_balance_wallet", "Kiriman Ortu", Icons.Rounded.AccountBalanceWallet),
    CategoryIconOption("school", "Beasiswa Kampus", Icons.Rounded.School),
    CategoryIconOption("work", "Kerja Part-Time", Icons.Rounded.Work),
    CategoryIconOption("payments", "Gaji & Uang Masuk", Icons.Rounded.Payments),
    CategoryIconOption("debt_expense", "Hutang (Bayar)", Icons.Rounded.Receipt),
    CategoryIconOption("debt_income", "Piutang (Terima)", Icons.Rounded.AccountBalance),
    CategoryIconOption("category", "Lainnya / Umum", Icons.Rounded.Category)
)

/**
 * Mendapatkan ImageVector berdasarkan nama identifier ikon.
 */
fun getCategoryIconVector(iconName: String): ImageVector {
    return availableCategoryIcons.find { it.id == iconName }?.icon ?: Icons.Rounded.Category
}
