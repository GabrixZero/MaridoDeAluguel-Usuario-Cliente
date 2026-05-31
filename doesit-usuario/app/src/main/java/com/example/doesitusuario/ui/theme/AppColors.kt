package com.example.doesitusuario.ui.theme

import androidx.compose.ui.graphics.Color
import com.example.doesitusuario.R

object AppColors {
    val Primary       = Color(0xFF2979FF)
    val PrimaryLight  = Color(0xFFE3EDFF)
    val Success       = Color(0xFF34C759)
    val SuccessLight  = Color(0xFFE8F5E9)
    val Error         = Color(0xFFC62828)
    val ErrorLight    = Color(0xFFFFEBEE)
    val ErrorBanner   = Color(0xFFFF3B30)
    val SuccessBanner = Color(0xFF34C759)
    val Background    = Color(0xFFF8F8F8)
    val Surface       = Color(0xFFFFFFFF)
    val SurfaceVariant = Color(0xFFF0F0F0)
    val Border        = Color(0xFFE5E5E5)
    val TextPrimary   = Color(0xFF1A1A1A)
    val TextSecondary = Color(0xFF6B6B6B)
    val TextDisabled  = Color(0xFFAAAAAA)
    val ButtonDisabled     = Color(0xFFE0E0E0)
    val TextButtonDisabled = Color(0xFF9E9E9E)
    val InputBackground    = Color(0xFFF5F5F5)
    val White = Color(0xFFFFFFFF)
    val Black = Color(0xFF1A1A1A)
}

// Usa os drawables reais do projeto — logo_pedido_*
// Marcenaria: substitua R.drawable.logo_pedido_reparo pelo drawable correto quando disponível
fun getCategoryLogo(categoryName: String?): Int = when (categoryName?.lowercase()?.trim()) {
    "elétrica"    -> R.drawable.logo_pedido_eletrica
    "encanamento" -> R.drawable.logo_pedido_reparo
    "pintura"     -> R.drawable.logo_pedido_pintura
    "marcenaria"  -> R.drawable.logo_pedido_marcenaria   // TODO: substituir por logo_pedido_marcenaria quando adicionado
    "limpeza"     -> R.drawable.logo_pedido_limpeza
    "chaveiro"    -> R.drawable.logo_pedido_chaveiro
    "montagem"    -> R.drawable.logo_pedido_montagem
    "instalação"  -> R.drawable.logo_pedido_instalacao
    else          -> R.drawable.logo_pedido_eletrica
}

fun formatDateBR(isoDateTime: String?): String {
    if (isoDateTime.isNullOrBlank()) return "—"
    return try {
        val parts = isoDateTime.substring(0, 10).split("-")
        "${parts[2]}/${parts[1]}/${parts[0]}"
    } catch (e: Exception) { isoDateTime }
}

fun formatDateTimeBR(isoDateTime: String?): String {
    if (isoDateTime.isNullOrBlank()) return "—"
    return try {
        val date = isoDateTime.substring(0, 10).split("-")
        val time = if (isoDateTime.length >= 16) isoDateTime.substring(11, 16) else ""
        "${date[2]}/${date[1]}/${date[0]}${if (time.isNotEmpty()) " às $time" else ""}"
    } catch (e: Exception) { isoDateTime }
}
