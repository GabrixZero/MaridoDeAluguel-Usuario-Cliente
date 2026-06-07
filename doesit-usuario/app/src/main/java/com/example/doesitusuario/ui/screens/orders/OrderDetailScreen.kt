package com.example.doesitusuario.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.data.model.ServiceRequestDTO
import com.example.doesitusuario.data.repository.ServiceRepository
import com.example.doesitusuario.ui.screens.login.ErrorBanner
import com.example.doesitusuario.ui.screens.login.SuccessBanner
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.theme.formatDateTimeBR
import com.example.doesitusuario.ui.theme.getCategoryLogo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId: Long,
    onBack: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val repository = remember { ServiceRepository() }
    val scope = rememberCoroutineScope()

    var pedido          by remember { mutableStateOf<ServiceRequestDTO?>(null) }
    var isLoading       by remember { mutableStateOf(true) }
    var isActionLoading by remember { mutableStateOf(false) }

    var bannerError   by remember { mutableStateOf("") }
    var bannerSuccess by remember { mutableStateOf("") }

    var selectedStars  by remember { mutableIntStateOf(0) }
    var comment        by remember { mutableStateOf("") }
    var alreadyRated   by remember { mutableStateOf(false) }

    LaunchedEffect(bannerError)   { if (bannerError.isNotEmpty())   { delay(5000); bannerError   = "" } }
    LaunchedEffect(bannerSuccess) { if (bannerSuccess.isNotEmpty()) { delay(5000); bannerSuccess = "" } }

    fun loadOrder() {
        scope.launch {
            isLoading = true
            repository.getById(orderId).fold(
                onSuccess = { pedido = it },
                onFailure = { bannerError = "Erro ao carregar detalhes" }
            )
            isLoading = false
        }
    }

    LaunchedEffect(orderId) { loadOrder() }

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Detalhes do Pedido", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        }
    ) { padding ->
        Box(Modifier.fillMaxSize()) {
            when {
                isLoading -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    CircularProgressIndicator(color = AppColors.Primary)
                }
                pedido == null -> Box(Modifier.fillMaxSize().padding(padding), Alignment.Center) {
                    Text("Pedido não encontrado", color = AppColors.TextSecondary)
                }
                else -> {
                    val p = pedido!!
                    
                    Column(
                        Modifier.fillMaxSize().padding(padding)
                            .verticalScroll(rememberScrollState()).padding(24.dp)
                    ) {
                        // ── Cabeçalho ────────────────────────────────────────
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(64.dp).clip(CircleShape).background(AppColors.SurfaceVariant),
                                Alignment.Center
                            ) {
                                androidx.compose.foundation.Image(
                                    painter = painterResource(getCategoryLogo(p.serviceName)),
                                    contentDescription = null, modifier = Modifier.size(40.dp)
                                )
                            }
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                Text(p.serviceName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                                Text(p.providerName ?: "Aguardando prestador", fontSize = 14.sp, color = AppColors.TextSecondary)
                            }
                            StatusBadge(p.status, p.statusId ?: 0)
                        }

                        Spacer(Modifier.height(28.dp))
                        HorizontalDivider(color = AppColors.Border)
                        Spacer(Modifier.height(20.dp))

                        // ── Informações ───────────────────────────────────────
                        val dateToFormat: String = p.date ?: ""
                        InfoRow("Data e Hora", try { formatDateTimeBR(dateToFormat) } catch(e: Exception) { dateToFormat })
                        InfoRow("Valor", "R$ ${String.format(Locale.getDefault(), "%.2f", p.value)}")
                        if (!p.address.isNullOrBlank()) InfoRow("Endereço", p.address)

                        Spacer(Modifier.height(12.dp))
                        Text("Descrição do Serviço", fontSize = 14.sp, color = AppColors.TextSecondary)
                        Spacer(Modifier.height(6.dp))
                        Text(p.description ?: "Sem descrição",
                            fontSize = 15.sp, color = AppColors.TextPrimary, lineHeight = 22.sp)

                        Spacer(Modifier.height(36.dp))

                        // ── Ações Baseadas no statusId ───────────────────────
                        // 1: PENDENTE | 2: AGENDADO | 3: CONCLUIDO | 4: CANCELADO | 5: EM ANDAMENTO
                        when (p.statusId) {
                            1, 2 -> { // Pendente ou Agendado: Pode cancelar
                                ActionButton(
                                    text = "Cancelar Solicitação", isError = true,
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repository.cancelRequest(orderId).fold(
                                                onSuccess = { onNavigateToHistory() },
                                                onFailure = { bannerError = it.message ?: "Erro ao cancelar" }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    isLoading = isActionLoading
                                )
                            }

                            5 -> { // Em andamento: Pode concluir
                                ActionButton(
                                    text = "Confirmar Finalização",
                                    onClick = {
                                        scope.launch {
                                            isActionLoading = true
                                            repository.confirmFinish(orderId).fold(
                                                onSuccess = {
                                                    bannerSuccess = "Serviço concluído com sucesso!"
                                                    delay(1000)
                                                    loadOrder()
                                                },
                                                onFailure = { bannerError = it.message ?: "Erro ao concluir" }
                                            )
                                            isActionLoading = false
                                        }
                                    },
                                    isLoading = isActionLoading
                                )
                            }

                            3 -> { // Concluído: Avaliação
                                if (alreadyRated) {
                                    Card(
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = CardDefaults.cardColors(containerColor = AppColors.SuccessLight)
                                    ) {
                                        Text("Avaliação enviada!", modifier = Modifier.padding(16.dp), color = AppColors.Success)
                                    }
                                } else {
                                    EvaluationSection(
                                        selectedStars = selectedStars, onStarsChange = { selectedStars = it },
                                        comment = comment, onCommentChange = { comment = it },
                                        providerName = p.providerName,
                                        onSend = {
                                            scope.launch {
                                                isActionLoading = true
                                                repository.rate(orderId, selectedStars, comment).fold(
                                                    onSuccess = { alreadyRated = true; bannerSuccess = "Avaliação enviada!" },
                                                    onFailure = { bannerError = "Erro ao avaliar" }
                                                )
                                                isActionLoading = false
                                            }
                                        },
                                        isLoading = isActionLoading
                                    )
                                }
                            }
                            
                            4 -> { /* Cancelado: Sem botões */ }
                        }

                        Spacer(Modifier.height(40.dp))
                    }
                }
            }

            // Banners
            AnimatedVisibility(visible = bannerError.isNotEmpty(), modifier = Modifier.align(Alignment.TopCenter)) { ErrorBanner(bannerError) }
            AnimatedVisibility(visible = bannerSuccess.isNotEmpty(), modifier = Modifier.align(Alignment.TopCenter)) { SuccessBanner(bannerSuccess) }
        }
    }
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = AppColors.TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary, textAlign = TextAlign.End)
    }
}

@Composable
fun StatusBadge(status: String, statusId: Int) {
    val (color, label) = when (statusId) {
        1 -> Color(0xFFFFA000) to "PENDENTE"
        2 -> AppColors.Primary  to "AGENDADO"
        5 -> Color(0xFF2E7D32) to "EM ANDAMENTO"
        3 -> Color(0xFF2E7D32) to "CONCLUÍDO"
        4 -> AppColors.Error   to "CANCELADO"
        else -> AppColors.TextSecondary to status.uppercase()
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(4.dp)) {
        Text(label, color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
    }
}

@Composable
fun ActionButton(text: String, onClick: () -> Unit, enabled: Boolean = true, isError: Boolean = false, isLoading: Boolean = false) {
    Button(
        onClick = onClick, enabled = enabled && !isLoading,
        modifier = Modifier.fillMaxWidth().height(56.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (isError) AppColors.ErrorBanner else AppColors.Primary)
    ) {
        if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
        else Text(text, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
fun EvaluationSection(selectedStars: Int, onStarsChange: (Int) -> Unit, comment: String, onCommentChange: (String) -> Unit, providerName: String?, onSend: () -> Unit, isLoading: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Avaliar Serviço", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(20.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            for (i in 1..5) {
                Icon(
                    imageVector = if (selectedStars >= i) Icons.Default.Star else Icons.Outlined.StarOutline,
                    contentDescription = null,
                    tint = if (selectedStars >= i) Color(0xFFFFC107) else AppColors.TextDisabled,
                    modifier = Modifier.size(40.dp).clickable { onStarsChange(i) }
                )
            }
        }
        Spacer(Modifier.height(20.dp))
        OutlinedTextField(value = comment, onValueChange = onCommentChange, modifier = Modifier.fillMaxWidth().height(120.dp), placeholder = { Text("Seu comentário...") })
        Spacer(Modifier.height(20.dp))
        Button(onClick = onSend, enabled = selectedStars > 0 && !isLoading, modifier = Modifier.fillMaxWidth().height(56.dp)) {
            if (isLoading) CircularProgressIndicator(Modifier.size(24.dp), color = Color.White)
            else Text("Enviar Avaliação")
        }
    }
}
