package com.example.doesitusuario.ui.screens.rating

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.doesitusuario.data.model.RatingRequest
import com.example.doesitusuario.data.network.RetrofitClient
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.ui.theme.AppColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvaliacaoScreen(
    requestId: Long,
    navController: NavController
) {
    val scope = rememberCoroutineScope()

    var selectedStars by remember { mutableStateOf(0) }
    var comment       by remember { mutableStateOf("") }
    var isLoading     by remember { mutableStateOf(false) }
    var errorMessage  by remember { mutableStateOf<String?>(null) }
    var showSuccess   by remember { mutableStateOf(false) }

    val starLabels = listOf("", "Ruim", "Regular", "Bom", "Ótimo", "Excelente")

    // ── Diálogo de sucesso ─────────────────────────────────────────────────────
    if (showSuccess) {
        AlertDialog(
            onDismissRequest = {},
            containerColor = AppColors.Surface,
            title = {
                Text(
                    text = "Avaliação enviada! ⭐",
                    fontWeight = FontWeight.Bold,
                    color = AppColors.TextPrimary
                )
            },
            text = {
                Text(
                    text = "Obrigado pelo feedback! Ele ajuda a melhorar a qualidade dos nossos profissionais.",
                    color = AppColors.TextSecondary,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Registra localmente como avaliado e volta
                        SessionManager.ratedRequestIds.add(requestId)
                        navController.popBackStack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("OK", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Avaliar Serviço",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AppColors.Primary
                )
            )
        },
        containerColor = AppColors.Background
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))

            // ── Cabeçalho ──────────────────────────────────────────────────────
            Text(
                text = "Como foi o serviço?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AppColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(10.dp))
            Text(
                text = "Sua avaliação ajuda outros clientes a\nencontrar os melhores profissionais.",
                fontSize = 14.sp,
                color = AppColors.TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(Modifier.height(36.dp))

            // ── Estrelas ───────────────────────────────────────────────────────
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..5) {
                    val scale by animateFloatAsState(
                        targetValue = if (selectedStars >= i) 1.3f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        ),
                        label = "star_$i"
                    )
                    Icon(
                        imageVector = if (selectedStars >= i) Icons.Filled.Star
                                      else Icons.Outlined.StarOutline,
                        contentDescription = "Estrela $i",
                        tint = if (selectedStars >= i) Color(0xFFFFC107)
                               else AppColors.TextSecondary,
                        modifier = Modifier
                            .size(52.dp)
                            .scale(scale)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                selectedStars = i
                                errorMessage = null
                            }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Label da nota
            Box(Modifier.height(22.dp)) {
                if (selectedStars > 0) {
                    Text(
                        text = starLabels[selectedStars],
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFFFC107)
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Card de comentário ─────────────────────────────────────────────
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AppColors.Surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Comentário (opcional)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AppColors.TextPrimary
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { if (it.length <= 300) comment = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        placeholder = {
                            Text(
                                text = "Conte como foi sua experiência...",
                                color = AppColors.TextSecondary,
                                fontSize = 14.sp
                            )
                        },
                        maxLines = 5,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = AppColors.Primary,
                            unfocusedBorderColor = AppColors.Border,
                            focusedTextColor     = AppColors.TextPrimary,
                            unfocusedTextColor   = AppColors.TextPrimary,
                            cursorColor          = AppColors.Primary
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${comment.length}/300",
                        fontSize = 12.sp,
                        color = AppColors.TextSecondary,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Mensagem de erro ───────────────────────────────────────────────
            errorMessage?.let { msg ->
                Text(
                    text = msg,
                    color = AppColors.Error,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Botão enviar ───────────────────────────────────────────────────
            Button(
                onClick = {
                    if (selectedStars == 0) {
                        errorMessage = "Selecione pelo menos 1 estrela para continuar."
                        return@Button
                    }
                    errorMessage = null
                    isLoading = true
                    scope.launch {
                        try {
                            val response = RetrofitClient.apiService.rate(
                                token  = SessionManager.bearerToken(),
                                rating = RatingRequest(
                                    serviceRequestId = requestId,
                                    stars            = selectedStars,
                                    comment          = comment.trim()
                                )
                            )
                            if (response.isSuccessful) {
                                showSuccess = true
                            } else {
                                errorMessage = when (response.code()) {
                                    400  -> "Este pedido já foi avaliado anteriormente."
                                    403  -> "Só é possível avaliar pedidos concluídos."
                                    else -> "Erro ao enviar avaliação (${response.code()}). Tente novamente."
                                }
                            }
                        } catch (e: Exception) {
                            errorMessage = "Erro de conexão. Verifique sua rede."
                        } finally {
                            isLoading = false
                        }
                    }
                },
                enabled  = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor         = AppColors.Primary,
                    disabledContainerColor = AppColors.Primary.copy(alpha = 0.5f)
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = Color.White,
                        modifier    = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp
                    )
                } else {
                    Text(
                        text       = "Enviar Avaliação",
                        fontSize   = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color      = Color.White
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // ── "Avaliar mais tarde" ───────────────────────────────────────────
            TextButton(
                onClick  = {
                    // Marca como dispensado: não abre automaticamente de novo
                    SessionManager.dismissedRatingIds.add(requestId)
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text  = "Avaliar mais tarde",
                    color = AppColors.TextSecondary,
                    fontSize = 14.sp
                )
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}
