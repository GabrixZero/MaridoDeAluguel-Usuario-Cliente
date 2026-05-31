package com.example.doesitusuario.ui.screens.orders

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doesitusuario.data.model.AddressDTO
import com.example.doesitusuario.data.model.ServiceCategory
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.theme.getCategoryLogo
import com.example.doesitusuario.ui.viewmodel.ServiceRequestViewModel
import kotlinx.coroutines.launch
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(
    onBack: () -> Unit,
    onNavigateToProviders: (Long, Boolean, String, String?, String?, Long, String, Double) -> Unit,
    viewModel: ServiceRequestViewModel = viewModel()
) {
    val formData by viewModel.formData.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var selectedCategory by remember { mutableStateOf<ServiceCategory?>(null) }
    var onlyWomen        by remember { mutableStateOf(false) }
    var timingMode       by remember { mutableStateOf("Agora") }
    var selectedDateMillis by remember { mutableStateOf<Long?>(null) }
    var selectedTime     by remember { mutableStateOf("") }
    var selectedAddress  by remember { mutableStateOf<AddressDTO?>(null) }
    var comment          by remember { mutableStateOf("") }
    
    var showDatePicker   by remember { mutableStateOf(false) }
    var isAddressExpanded by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState()

    LaunchedEffect(Unit) {
        viewModel.loadFormData()
    }

    // Quando os dados carregarem, seleciona o endereço principal
    LaunchedEffect(formData) {
        formData?.let { data ->
            if (selectedAddress == null) {
                selectedAddress = data.addresses.find { it.isDefault } ?: data.addresses.firstOrNull()
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { selectedDateMillis = datePickerState.selectedDateMillis; showDatePicker = false }) { Text("OK", color = AppColors.Primary) } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") } }
        ) {
            DatePicker(state = datePickerState, showModeToggle = false)
        }
    }

    val canProceed = selectedCategory != null &&
        (timingMode == "Agora" || (selectedDateMillis != null && selectedTime.length == 4))

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Solicitar Serviço", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = AppColors.TextPrimary) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) } },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            Surface(Modifier.fillMaxWidth(), tonalElevation = 8.dp, color = AppColors.Surface) {
                Button(
                    onClick = {
                        val cat = selectedCategory ?: return@Button
                        val formattedDate = selectedDateMillis?.let { formatDate(it) }
                        val formattedTime = selectedTime.takeIf { timingMode == "Agendar" && it.length == 4 }
                            ?.let { "${it.substring(0,2)}:${it.substring(2)}" }
                        onNavigateToProviders(
                            cat.id, onlyWomen, timingMode,
                            formattedDate, formattedTime,
                            selectedAddress?.id ?: 0L,
                            comment,
                            cat.basePrice
                        )
                    },
                    enabled = canProceed,
                    modifier = Modifier.padding(20.dp).fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColors.Primary,
                        disabledContainerColor = AppColors.ButtonDisabled,
                        disabledContentColor = AppColors.TextButtonDisabled
                    )
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Buscar Profissional", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Icon(Icons.Default.ArrowForward, null, Modifier.size(18.dp))
                    }
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // Qual o serviço?
                item {
                    Column {
                        SectionHeader("QUAL O SERVIÇO?")
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(formData?.services ?: emptyList()) { cat ->
                                CategoryCard(cat, selectedCategory?.id == cat.id) { selectedCategory = cat }
                            }
                        }
                    }
                }

                // Preferências
                if (formData?.showWomenFilter == true) {
                    item {
                        Column {
                            SectionHeader("PREFERÊNCIAS")
                            Spacer(Modifier.height(12.dp))
                            Surface(
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                                    .clickable { onlyWomen = !onlyWomen },
                                color = if (onlyWomen) AppColors.Primary else AppColors.SurfaceVariant.copy(alpha = 0.5f)
                            ) {
                                Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("Somente prestadores mulheres",
                                        color = if (onlyWomen) Color.White else AppColors.TextPrimary,
                                        fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }

                // Para quando?
                item {
                    Column {
                        SectionHeader("PARA QUANDO?")
                        Spacer(Modifier.height(12.dp))
                        Row(
                            Modifier.fillMaxWidth().height(50.dp)
                                .background(AppColors.SurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            TimingOption("Agora",   timingMode == "Agora",   { timingMode = "Agora" },   Modifier.weight(1f))
                            TimingOption("Agendar", timingMode == "Agendar", { timingMode = "Agendar" }, Modifier.weight(1f))
                        }
                        AnimatedVisibility(visible = timingMode == "Agendar",
                            enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Column {
                                Spacer(Modifier.height(16.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(
                                        Modifier.weight(1.5f).height(56.dp)
                                            .background(AppColors.SurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                            .clickable { showDatePicker = true }.padding(horizontal = 16.dp),
                                        Alignment.CenterStart
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(selectedDateMillis?.let { formatDate(it) } ?: "Data",
                                                color = if (selectedDateMillis != null) AppColors.TextPrimary else AppColors.TextSecondary,
                                                modifier = Modifier.weight(1f))
                                            Icon(Icons.Default.DateRange, null, tint = AppColors.TextSecondary)
                                        }
                                    }
                                    OutlinedTextField(
                                        value = selectedTime,
                                        onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) selectedTime = it },
                                        placeholder = { Text("00:00") },
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(12.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = AppColors.TextSecondary) },
                                        visualTransformation = TimeVisualTransformation(),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            unfocusedContainerColor = AppColors.SurfaceVariant.copy(alpha = 0.5f),
                                            focusedContainerColor   = AppColors.SurfaceVariant.copy(alpha = 0.5f),
                                            unfocusedBorderColor    = Color.Transparent,
                                            focusedBorderColor      = AppColors.Primary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                // Onde será realizado?
                item {
                    Column {
                        SectionHeader("ONDE SERÁ REALIZADO?")
                        Spacer(Modifier.height(12.dp))
                        AddressSelector(selectedAddress, formData?.addresses ?: emptyList(), isAddressExpanded,
                            onToggleExpand = { isAddressExpanded = !isAddressExpanded },
                            onSelectAddress = { selectedAddress = it; isAddressExpanded = false }
                        )
                    }
                }

                // Comentário
                item {
                    Column {
                        SectionHeader("COMENTÁRIO")
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = comment, onValueChange = { comment = it },
                            placeholder = { Text("Descreva mais detalhes sobre o serviço...", color = AppColors.TextDisabled) },
                            modifier = Modifier.fillMaxWidth().height(120.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = AppColors.SurfaceVariant.copy(alpha = 0.5f),
                                focusedContainerColor   = AppColors.SurfaceVariant.copy(alpha = 0.5f),
                                unfocusedBorderColor    = Color.Transparent, focusedBorderColor = AppColors.Primary
                            )
                        )
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable fun SectionHeader(text: String) {
    Text(text, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AppColors.TextSecondary.copy(alpha = 0.7f))
}

@Composable
fun CategoryCard(category: ServiceCategory, isSelected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.size(width = 100.dp, height = 110.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSelected) AppColors.Primary else AppColors.SurfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            androidx.compose.foundation.Image(
                painter = painterResource(getCategoryLogo(category.name)),
                contentDescription = null, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(category.name, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else AppColors.TextPrimary)
        }
    }
}

@Composable fun TimingOption(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier) {
    Box(modifier.fillMaxHeight().clip(RoundedCornerShape(10.dp))
        .background(if (isSelected) Color.White else Color.Transparent).clickable { onClick() },
        Alignment.Center) {
        Text(text, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) AppColors.TextPrimary else AppColors.TextSecondary)
    }
}

@Composable
fun AddressSelector(selectedAddress: AddressDTO?, allAddresses: List<AddressDTO>,
    isExpanded: Boolean, onToggleExpand: () -> Unit, onSelectAddress: (AddressDTO) -> Unit) {
    Column(
        Modifier.fillMaxWidth().background(AppColors.SurfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() }.padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(AppColors.PrimaryLight, RoundedCornerShape(12.dp)), Alignment.Center) {
                Icon(Icons.Default.LocationOn, null, tint = AppColors.Primary)
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(selectedAddress?.tag ?: "Nenhum endereço", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AppColors.TextPrimary)
                Text(selectedAddress?.formatted ?: "Cadastre um endereço",
                    fontSize = 13.sp, color = AppColors.TextSecondary, maxLines = 1)
            }
            Icon(if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                null, tint = AppColors.TextSecondary)
        }
        if (isExpanded) {
            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = AppColors.Border.copy(alpha = 0.5f))
            allAddresses.filter { it.id != selectedAddress?.id }.forEach { address ->
                Row(Modifier.fillMaxWidth().clickable { onSelectAddress(address) }.padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Home, null, tint = AppColors.TextSecondary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(address.tag ?: "Endereço", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(address.formatted ?: "", fontSize = 12.sp, color = AppColors.TextSecondary)
                    }
                }
            }
        }
    }
}

fun formatDate(millis: Long): String {
    val cal = Calendar.getInstance().apply { timeInMillis = millis }
    val months = arrayOf("Jan","Fev","Mar","Abr","Mai","Jun","Jul","Ago","Set","Out","Nov","Dez")
    return "${cal.get(Calendar.DAY_OF_MONTH)} ${months[cal.get(Calendar.MONTH)]}, ${cal.get(Calendar.YEAR)}"
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter { it.isDigit() }.take(4)
        var out = ""
        for (i in digits.indices) { out += digits[i]; if (i == 1 && digits.length > 2) out += ":" }
        val mapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int) = if (offset <= 2) offset else if (offset <= 4) offset + 1 else out.length
            override fun transformedToOriginal(offset: Int) = if (offset <= 2) offset else if (offset <= 5) offset - 1 else digits.length
        }
        return TransformedText(AnnotatedString(out), mapping)
    }
}
