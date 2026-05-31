package com.example.doesitusuario.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.doesitusuario.data.model.ProviderDTO
import com.example.doesitusuario.data.repository.ServiceRepository
import com.example.doesitusuario.ui.screens.login.ErrorBanner
import com.example.doesitusuario.ui.screens.login.SuccessBanner
import com.example.doesitusuario.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvailableProvidersScreen(
    catId: Long,
    onlyWomen: Boolean,
    mode: String,          // "Agora" | "Agendar"
    date: String?,
    time: String?,
    addrId: Long,
    comment: String,
    basePrice: Double,     // preço base da categoria (usado quando provider.specialtyPrice == null)
    onBack: () -> Unit,
    onConfirm: () -> Unit  // navega para Home após criar pedido com sucesso
) {
    val repository = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var providers        by remember { mutableStateOf<List<ProviderDTO>>(emptyList()) }
    var isLoading        by remember { mutableStateOf(true) }
    var selectedProvider by remember { mutableStateOf<ProviderDTO?>(null) }
    var isCreating       by remember { mutableStateOf(false) }

    var errorMsg   by remember { mutableStateOf("") }
    var successMsg by remember { mutableStateOf("") }

    LaunchedEffect(errorMsg)   { if (errorMsg.isNotEmpty())   { delay(5000); errorMsg   = "" } }
    LaunchedEffect(successMsg) { if (successMsg.isNotEmpty()) { delay(3000); successMsg = "" } }

    val onlineOnly = mode == "Agora"

    LaunchedEffect(Unit) {
        repository.getProvidersForCategory(catId, onlineOnly, onlyWomen).fold(
            onSuccess = { providers = it },
            onFailure = { errorMsg = it.message ?: "Erro ao carregar prestadores" }
        )
        isLoading = false
    }

    // Preço efetivo para cada provider: specialty > base
    fun effectivePrice(p: ProviderDTO): Double = p.specialtyPrice ?: basePrice

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Escolher Prestador", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                        Text(
                            if (onlineOnly) "Apenas online (modo Agora)" else "Online e offline (modo Agendar)",
                            fontSize = 12.sp, color = AppColors.TextSecondary
                        )
                    }
                },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            Surface(Modifier.fillMaxWidth(), tonalElevation = 8.dp, color = AppColors.Surface) {
                val sel = selectedProvider
                Column(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                    if (sel != null) {
                        Row(Modifier.fillMaxWidth().padding(bottom = 8.dp), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                            Column {
                                Text(sel.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = AppColors.TextPrimary)
                                Text("Preço: R$ ${String.format(Locale.getDefault(), "%.2f", effectivePrice(sel))}",
                                    fontSize = 13.sp, color = AppColors.Primary, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { selectedProvider = null }) {
                                Icon(Icons.Default.Close, null, tint = AppColors.TextSecondary)
                            }
                        }
                    }
                    Button(
                        onClick = {
                            val p = selectedProvider ?: return@Button
                            scope.launch {
                                isCreating = true
                                // Monta scheduledAt no formato ISO-8601 se modo Agendar
                                val scheduledAt = if (mode == "Agendar" && date != null && time != null) {
                                    buildIsoDateTime(date, time)
                                } else null

                                repository.createRequest(
                                    categoryId         = catId,
                                    description        = comment,
                                    type               = if (mode == "Agora") "IMMEDIATE" else "SCHEDULED",
                                    scheduledAt        = scheduledAt,
                                    preferredProviderId = p.id,
                                    addressId          = addrId.takeIf { it > 0 }
                                ).fold(
                                    onSuccess = {
                                        successMsg = "Pedido enviado para ${p.name}!"
                                        delay(1500)
                                        onConfirm()
                                    },
                                    onFailure = { errorMsg = it.message ?: "Erro ao criar pedido" }
                                )
                                isCreating = false
                            }
                        },
                        enabled = selectedProvider != null && !isCreating,
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AppColors.Primary,
                            disabledContainerColor = AppColors.ButtonDisabled,
                            disabledContentColor   = AppColors.TextButtonDisabled
                        )
                    ) {
                        if (isCreating) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                        else {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(if (selectedProvider == null) "Selecione um prestador" else "Confirmar Pedido",
                                    fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                if (selectedProvider != null) {
                                    Spacer(Modifier.width(8.dp))
                                    Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
                providers.isEmpty() -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PersonOff, null, Modifier.size(56.dp), tint = AppColors.TextDisabled)
                        Spacer(Modifier.height(12.dp))
                        Text(
                            if (onlineOnly) "Nenhum prestador online no momento.\nTente o modo Agendar."
                            else "Nenhum prestador disponível para esta categoria.",
                            color = AppColors.TextSecondary, textAlign = TextAlign.Center, lineHeight = 22.sp
                        )
                    }
                }
                else -> LazyColumn(
                    Modifier.fillMaxSize().padding(padding),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(providers, key = { it.id }) { provider ->
                        ProviderCard(
                            provider       = provider,
                            effectivePrice = effectivePrice(provider),
                            basePrice      = basePrice,
                            isSelected     = selectedProvider?.id == provider.id,
                            onClick        = {
                                selectedProvider = if (selectedProvider?.id == provider.id) null else provider
                            }
                        )
                    }
                    item { Spacer(Modifier.height(100.dp)) }
                }
            }

            AnimatedVisibility(
                visible = errorMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).zIndex(10f)
            ) { ErrorBanner(errorMsg) }

            AnimatedVisibility(
                visible = successMsg.isNotEmpty(),
                enter = slideInVertically { -it } + fadeIn(),
                exit  = slideOutVertically { -it } + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp).zIndex(10f)
            ) { SuccessBanner(successMsg) }
        }
    }
}

@Composable
private fun ProviderCard(
    provider: ProviderDTO,
    effectivePrice: Double,
    basePrice: Double,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val hasCustomPrice = provider.specialtyPrice != null

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) AppColors.PrimaryLight else AppColors.Surface
        ),
        elevation = CardDefaults.cardElevation(if (isSelected) 0.dp else 1.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, AppColors.Primary) else null
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Avatar
            Surface(Modifier.size(56.dp), CircleShape,
                if (isSelected) AppColors.Primary.copy(alpha = 0.15f) else AppColors.SurfaceVariant) {
                Icon(Icons.Default.Person, null, Modifier.padding(14.dp),
                    tint = if (isSelected) AppColors.Primary else AppColors.TextDisabled)
            }

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(provider.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                    if (provider.isOnline) {
                        Spacer(Modifier.width(6.dp))
                        Box(Modifier.size(8.dp).background(Color(0xFF34C759), CircleShape))
                    }
                }

                if ((provider.ratingCount ?: 0) > 0) {
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, null, Modifier.size(14.dp), tint = Color(0xFFFFC107))
                        Text(" ${String.format("%.1f", provider.rating)} (${provider.ratingCount})",
                            fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }

                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "R$ ${String.format(Locale.getDefault(), "%.2f", effectivePrice)}",
                        fontSize = 15.sp, fontWeight = FontWeight.Bold,
                        color = if (isSelected) AppColors.Primary else AppColors.TextPrimary
                    )
                    if (hasCustomPrice) {
                        Spacer(Modifier.width(6.dp))
                        Surface(
                            color = AppColors.Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("Preço próprio", fontSize = 10.sp, color = AppColors.Primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                }
            }

            if (isSelected) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(24.dp), tint = AppColors.Primary)
            }
        }
    }
}

// Monta ISO-8601 a partir de "15 Mar, 2025" e "HH:mm"
private fun buildIsoDateTime(date: String, time: String): String? = try {
    // date format: "d Mon, yyyy" ex: "15 Mar, 2025"
    val months = mapOf("Jan" to "01","Fev" to "02","Mar" to "03","Abr" to "04","Mai" to "05","Jun" to "06",
        "Jul" to "07","Ago" to "08","Set" to "09","Out" to "10","Nov" to "11","Dez" to "12")
    val parts = date.replace(",", "").split(" ")
    val day   = parts[0].padStart(2, '0')
    val month = months[parts[1]] ?: "01"
    val year  = parts[2]
    val t     = if (time.contains(":")) time else "${time.take(2)}:${time.drop(2)}"
    "${year}-${month}-${day}T${t}:00"
} catch (e: Exception) { null }
