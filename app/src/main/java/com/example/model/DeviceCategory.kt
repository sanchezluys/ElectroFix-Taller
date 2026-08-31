package com.example.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Laptop
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tv
import androidx.compose.ui.graphics.vector.ImageVector

enum class DeviceCategory(
    val title: String,
    val subtitle: String,
    val iconName: String
) {
    SMART_TV(
        title = "Smart TV / Pantalla",
        subtitle = "LED, OLED, QLED, Monitores",
        iconName = "tv"
    ),
    CONSOLE(
        title = "Consola de Videojuegos",
        subtitle = "PlayStation, Xbox, Nintendo, Portátiles",
        iconName = "sports_esports"
    ),
    MOBILE(
        title = "Dispositivo Móvil",
        subtitle = "Smartphones, Tablets, Smartwatches",
        iconName = "phone_android"
    ),
    COMPUTER(
        title = "Computador / Laptop",
        subtitle = "PC Escritorio, Laptops, MacBooks, All-in-One",
        iconName = "laptop"
    ),
    AUDIO_OTHER(
        title = "Audio y Otros",
        subtitle = "Equipos de sonido, Fuentes, Tarjetas electrónicas",
        iconName = "headphones"
    );

    fun getIcon(): ImageVector = when (this) {
        SMART_TV -> Icons.Default.Tv
        CONSOLE -> Icons.Default.SportsEsports
        MOBILE -> Icons.Default.PhoneAndroid
        COMPUTER -> Icons.Default.Laptop
        AUDIO_OTHER -> Icons.Default.Headphones
    }
}
