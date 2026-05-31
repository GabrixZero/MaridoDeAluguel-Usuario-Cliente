package com.example.doesitusuario.ui.screens.addresses

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.doesitusuario.data.model.AddressDTO
import com.example.doesitusuario.data.network.RetrofitClient
import com.example.doesitusuario.data.network.SessionManager
import com.example.doesitusuario.ui.screens.login.DarkTextField
import com.example.doesitusuario.ui.screens.login.ErrorBanner
import com.example.doesitusuario.ui.theme.AppColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressFormScreen(
    addressId: Long? = null,
    onBack: () -> Unit,
    onSuccess: (String) -> Unit,
    onNavigateToHome: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToProfile: () -> Unit
) {
    val api = RetrofitClient.apiService
    val scope = rememberCoroutineScope()
    val isEditing = addressId != null

    var cep by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var number by remember { mutableStateOf("") }
    var neighborhood by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var isDefault by remember { mutableStateOf(false) }

    var originalAddress by remember { mutableStateOf<AddressDTO?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isFetching by remember { mutableStateOf(isEditing) }
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorFields by remember { mutableStateOf(setOf<String>()) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    // Load address if editing
    LaunchedEffect(addressId) {
        if (isEditing && addressId != null) {
            isFetching = true
            try {
                val response = api.getAddresses()
                if (response.isSuccessful) {
                    val addr = response.body()?.addresses?.find { it.id == addressId }
                    if (addr != null) {
                        originalAddress = addr
                        cep = addr.cep ?: ""
                        street = addr.street ?: ""
                        number = addr.number ?: ""
                        neighborhood = addr.neighborhood ?: ""
                        city = addr.city ?: ""
                        state = addr.state ?: ""
                        title = addr.tag ?: ""
                        isDefault = addr.isDefault
                    } else {
                        errorMessage = "Endereço não encontrado"
                        isError = true
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Erro ao carregar endereço"
                isError = true
            }
            isFetching = false
        }
    }

    // Reset error after 5s
    LaunchedEffect(isError) {
        if (isError) {
            delay(5000)
            isError = false
        }
    }

    val hasChanges = if (isEditing && originalAddress != null) {
        cep != originalAddress!!.cep ||
        street != originalAddress!!.street ||
        number != originalAddress!!.number ||
        neighborhood != originalAddress!!.neighborhood ||
        city != originalAddress!!.city ||
        state != originalAddress!!.state ||
        title != originalAddress!!.tag ||
        isDefault != originalAddress!!.isDefault
    } else {
        cep.isNotBlank() && street.isNotBlank() && number.isNotBlank() && 
        neighborhood.isNotBlank() && city.isNotBlank() && state.isNotBlank()
    }

    val buttonEnabled = hasChanges && !isLoading

    Scaffold(
        containerColor = AppColors.Background,
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        if (isEditing) "Editar endereço" else "Novo endereço", 
                        fontWeight = FontWeight.Bold, 
                        color = AppColors.TextPrimary 
                    ) 
                },
                navigationIcon = { 
                    IconButton(onClick = onBack) { 
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = AppColors.TextPrimary) 
                    } 
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = AppColors.Surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHome,
                    icon = { Icon(Icons.Default.Home, contentDescription = "Início") },
                    label = { Text("Início") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToHistory,
                    icon = { Icon(Icons.AutoMirrored.Filled.Assignment, contentDescription = "Pedidos") },
                    label = { Text("Pedidos") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
                NavigationBarItem(
                    selected = false,
                    onClick = onNavigateToProfile,
                    icon = { Icon(Icons.Default.PersonOutline, contentDescription = "Perfil") },
                    label = { Text("Perfil") },
                    colors = NavigationBarItemDefaults.colors(
                        unselectedIconColor = AppColors.TextDisabled,
                        unselectedTextColor = AppColors.TextDisabled
                    )
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isFetching) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = AppColors.Primary
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(20.dp)
                ) {
                    DarkTextField(
                        value = cep,
                        onValueChange = { 
                            cep = it.filter { c -> c.isDigit() }.take(8)
                            errorFields = errorFields - "cep"
                        },
                        label = "CEP",
                        placeholder = "Ex: 00000-000",
                        isError = errorFields.contains("cep"),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(Modifier.height(16.dp))

                    DarkTextField(
                        value = street,
                        onValueChange = { 
                            street = it
                            errorFields = errorFields - "street"
                        },
                        label = "Rua",
                        placeholder = "Ex: Rua das Flores",
                        isError = errorFields.contains("street"),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(0.4f)) {
                            DarkTextField(
                                value = number,
                                onValueChange = { 
                                    number = it
                                    errorFields = errorFields - "number"
                                },
                                label = "Número",
                                placeholder = "Ex: 123",
                                isError = errorFields.contains("number")
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Box(Modifier.weight(0.6f)) {
                            DarkTextField(
                                value = neighborhood,
                                onValueChange = { 
                                    neighborhood = it
                                    errorFields = errorFields - "neighborhood"
                                },
                                label = "Bairro",
                                placeholder = "Ex: Centro",
                                isError = errorFields.contains("neighborhood"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    Row(Modifier.fillMaxWidth()) {
                        Box(Modifier.weight(0.7f)) {
                            DarkTextField(
                                value = city,
                                onValueChange = { 
                                    city = it
                                    errorFields = errorFields - "city"
                                },
                                label = "Cidade",
                                placeholder = "Ex: São Paulo",
                                isError = errorFields.contains("city"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Box(Modifier.weight(0.3f)) {
                            DarkTextField(
                                value = state,
                                onValueChange = { 
                                    state = it.uppercase().take(2)
                                    errorFields = errorFields - "state"
                                },
                                label = "Estado",
                                placeholder = "SP",
                                isError = errorFields.contains("state"),
                                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))

                    DarkTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = "Título do endereço (Opcional)",
                        placeholder = "Ex: Casa, Trabalho",
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
                    )
                    Spacer(Modifier.height(24.dp))

                    // Principal Toggle
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = AppColors.SurfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Definir como endereço principal",
                                fontSize = 16.sp,
                                color = AppColors.TextPrimary
                            )
                            Switch(
                                checked = isDefault,
                                onCheckedChange = { isDefault = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = AppColors.Primary,
                                    uncheckedThumbColor = AppColors.TextDisabled,
                                    uncheckedTrackColor = AppColors.ButtonDisabled
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(32.dp))

                    Button(
                        onClick = {
                            val errors = mutableSetOf<String>()
                            if (cep.length < 8) errors.add("cep")
                            if (street.isBlank()) errors.add("street")
                            if (number.isBlank()) errors.add("number")
                            if (neighborhood.isBlank()) errors.add("neighborhood")
                            if (city.isBlank()) errors.add("city")
                            if (state.length < 2) errors.add("state")

                            if (errors.isNotEmpty()) {
                                errorFields = errors
                                errorMessage = "Por favor, preencha todos os campos obrigatórios corretamente."
                                isError = true
                                return@Button
                            }

                            scope.launch {
                                isLoading = true
                                
                                var finalTitle = title.trim()
                                if (finalTitle.isBlank()) {
                                    try {
                                        val currentAddresses = api.getAddresses().body()?.addresses ?: emptyList()
                                        // Count only addresses that follow the pattern "Endereço X"
                                        val pattern = Regex("^Endereço (\\d+)$")
                                        val lastNumber = currentAddresses
                                            .mapNotNull { addr -> 
                                                val tagStr = addr.tag ?: ""
                                                pattern.find(tagStr)?.groupValues?.get(1)?.toIntOrNull()
                                            }
                                            .maxOrNull() ?: 0
                                        finalTitle = "Endereço ${lastNumber + 1}"
                                    } catch (e: Exception) {
                                        finalTitle = "Endereço"
                                    }
                                }

                                val body = mapOf(
                                    "titulo" to finalTitle,
                                    "cep" to cep,
                                    "rua" to street,
                                    "numero" to number,
                                    "bairro" to neighborhood,
                                    "cidade" to city,
                                    "estado" to state,
                                    "is_favorite" to isDefault.toString()
                                )

                                try {
                                    val response = if (isEditing) {
                                        api.updateAddress(addressId ?: 0L, body)
                                    } else {
                                        api.createAddress(body)
                                    }

                                    if (response.isSuccessful) {
                                        onSuccess(if (isEditing) "Endereço atualizado com sucesso!" else "Endereço cadastrado com sucesso!")
                                    } else {
                                        val errorBody = response.errorBody()?.string()
                                        android.util.Log.e("AddressForm", "API Error: ${response.code()} - $errorBody")
                                        errorMessage = "Erro ao salvar: ${response.code()}"
                                        isError = true
                                    }
                                } catch (e: Exception) {
                                    android.util.Log.e("AddressForm", "Connection Exception", e)
                                    errorMessage = "Erro técnico: ${e.localizedMessage}"
                                    isError = true
                                }
                                isLoading = false
                            }
                        },
                        enabled = buttonEnabled,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (buttonEnabled) AppColors.Primary else AppColors.ButtonDisabled,
                            contentColor = if (buttonEnabled) Color.White else AppColors.TextButtonDisabled
                        )
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        } else {
                            Text(
                                if (isEditing) "Salvar Alterações" else "Adicionar Endereço",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (isEditing) {
                        Spacer(Modifier.height(16.dp))
                        TextButton(
                            onClick = { showDeleteConfirm = true },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.textButtonColors(contentColor = AppColors.Error)
                        ) {
                            Text("Excluir endereço", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                    
                    Spacer(Modifier.height(56.dp))
                }
            }

            // Delete Confirmation Dialog
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    containerColor = AppColors.Surface,
                    title = { Text("Excluir endereço", fontWeight = FontWeight.Bold, color = AppColors.TextPrimary) },
                    text = { Text("Tem certeza que deseja remover este endereço?", color = AppColors.TextSecondary) },
                    confirmButton = {
                        Button(
                            onClick = {
                                scope.launch {
                                    isDeleting = true
                                    try {
                                        val response = api.deleteAddress(addressId!!)
                                        if (response.isSuccessful) {
                                            onSuccess("Endereço removido com sucesso!")
                                        } else {
                                            errorMessage = "Erro ao excluir endereço."
                                            isError = true
                                        }
                                    } catch (e: Exception) {
                                        errorMessage = "Erro de conexão."
                                        isError = true
                                    }
                                    isDeleting = false
                                    showDeleteConfirm = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AppColors.Error),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            if (isDeleting) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                            else Text("Confirmar", color = Color.White)
                        }
                    },
                    dismissButton = {
                        OutlinedButton(
                            onClick = { showDeleteConfirm = false },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Recusar", color = AppColors.TextPrimary)
                        }
                    }
                )
            }

            // Error Banner
            AnimatedVisibility(
                visible = isError && errorMessage.isNotEmpty(),
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier.align(Alignment.TopCenter).padding(top = 10.dp)
            ) {
                ErrorBanner(message = errorMessage)
            }
        }
    }
}
