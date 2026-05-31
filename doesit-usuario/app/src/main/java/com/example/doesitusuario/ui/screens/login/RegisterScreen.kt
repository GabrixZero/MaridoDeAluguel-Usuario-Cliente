package com.example.doesitusuario.ui.screens.login

import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.doesitusuario.R
import com.example.doesitusuario.data.model.UserRegisterProfileDTO
import com.example.doesitusuario.ui.theme.AppColors
import com.example.doesitusuario.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay
import java.util.Calendar

@Composable
fun RegisterScreen(
    onBackToLogin: () -> Unit,
    onRegisterSuccess: (String) -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var nomeCompleto by remember { mutableStateOf("") }
    var cpf by remember { mutableStateOf("") }
    var dataNascimento by remember { mutableStateOf("") }
    var genero by remember { mutableStateOf("") }
    var cep by remember { mutableStateOf("") }
    var rua by remember { mutableStateOf("") }
    var numero by remember { mutableStateOf("") }
    var bairro by remember { mutableStateOf("") }
    var cidade by remember { mutableStateOf("") }
    var estado by remember { mutableStateOf("") }
    var celular by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var senha by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorFromVM by viewModel.errorMessage.collectAsState()
    
    var errorMessage by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorFields by remember { mutableStateOf(setOf<String>()) }

    val scrollState = rememberScrollState()
    val genderOptions = listOf("Masculino", "Feminino", "Outros", "Não informar")
    var expandedGender by remember { mutableStateOf(false) }

    val isPasswordStrong = remember(senha) {
        senha.length >= 8 &&
                senha.any { it.isUpperCase() } &&
                senha.any { it.isLowerCase() } &&
                senha.any { it.isDigit() } &&
                senha.any { !it.isLetterOrDigit() }
    }

    val isFormFilled = nomeCompleto.isNotBlank() && cpf.isNotBlank() && dataNascimento.isNotBlank() &&
            genero.isNotBlank() && cep.isNotBlank() && rua.isNotBlank() && numero.isNotBlank() &&
            bairro.isNotBlank() && cidade.isNotBlank() && estado.isNotBlank() &&
            celular.isNotBlank() && email.isNotBlank() && senha.isNotBlank()

    fun validateBirthDate(date: String): String? {
        if (date.length != 10) return "Data inválida"
        val parts = date.split("/")
        if (parts.size != 3) return "Data inválida"
        val day = parts[0].toIntOrNull() ?: return "Data inválida"
        val month = parts[1].toIntOrNull() ?: return "Data inválida"
        val year = parts[2].toIntOrNull() ?: return "Data inválida"

        val today = Calendar.getInstance()
        val birthDate = Calendar.getInstance().apply { set(year, month - 1, day) }
        var age = today.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) age--

        return when {
            age < 18 -> "Deve ter pelo menos 18 anos"
            age > 110 -> "Data de nascimento inválida"
            else -> null
        }
    }

    LaunchedEffect(errorFromVM) {
        if (errorFromVM.isNotEmpty()) {
            errorMessage = errorFromVM
            isError = true
            delay(5000)
            isError = false
            viewModel.clearMessages()
        }
    }

    Box(Modifier.fillMaxSize().background(AppColors.Background)) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp), contentAlignment = Alignment.Center) {
                Column(modifier = Modifier.padding(horizontal = 24.dp), horizontalAlignment = Alignment.Start) {
                    Image(painter = painterResource(id = R.drawable.logo_doesit), contentDescription = "DoesIt Logo", modifier = Modifier.size(72.dp))
                    Spacer(Modifier.height(24.dp))
                    Text("DoesIt", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AppColors.TextPrimary)
                    Text("Seu marido de aluguel para qualquer situação.", fontSize = 14.sp, color = AppColors.TextSecondary)
                }
            }

            Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
                Column(Modifier.weight(1f).clickable { onBackToLogin() }, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Entrar", color = AppColors.TextSecondary, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(60.dp).height(2.dp).background(Color.Transparent))
                }
                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Cadastrar", color = AppColors.TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Box(Modifier.width(60.dp).height(2.dp).background(AppColors.Primary))
                }
            }

            Spacer(Modifier.height(32.dp))

            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text("Dados pessoais", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = nomeCompleto, onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) nomeCompleto = it }, label = "Nome completo", placeholder = "Como está no seu documento", isError = errorFields.contains("nomeCompleto"))
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = cpf, onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) cpf = it }, label = "CPF", placeholder = "000.000.000-00", isError = errorFields.contains("cpf"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(1f)) {
                        DarkTextField(
                            value = dataNascimento,
                            onValueChange = { input ->
                                val clean = input.filter { it.isDigit() }
                                if (clean.length <= 8) {
                                    dataNascimento = buildString {
                                        for (i in clean.indices) {
                                            append(clean[i])
                                            if ((i == 1 || i == 3) && i != clean.lastIndex) append("/")
                                        }
                                    }
                                }
                            },
                            label = "Data de nascimento",
                            placeholder = "dd/mm/aaaa",
                            isError = errorFields.contains("dataNascimento"),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Gênero", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = AppColors.TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                        Box {
                            OutlinedTextField(
                                value = genero, onValueChange = {}, readOnly = true, placeholder = { Text("Selecione", color = AppColors.TextDisabled) },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), trailingIcon = { Icon(Icons.Default.KeyboardArrowDown, null, tint = AppColors.TextSecondary) },
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = AppColors.InputBackground, unfocusedContainerColor = AppColors.InputBackground,
                                    focusedBorderColor = if (errorFields.contains("genero")) AppColors.ErrorBanner else AppColors.Primary,
                                    unfocusedBorderColor = if (errorFields.contains("genero")) AppColors.ErrorBanner else Color.Transparent,
                                    focusedTextColor = AppColors.TextPrimary, unfocusedTextColor = AppColors.TextPrimary,
                                    disabledContainerColor = AppColors.InputBackground, disabledBorderColor = if (errorFields.contains("genero")) AppColors.ErrorBanner else Color.Transparent,
                                    disabledTextColor = AppColors.TextPrimary, disabledPlaceholderColor = AppColors.TextDisabled
                                ),
                                enabled = false
                            )
                            Box(Modifier.matchParentSize().background(Color.Transparent, RoundedCornerShape(12.dp)).clickable { expandedGender = true })
                            DropdownMenu(expanded = expandedGender, onDismissRequest = { expandedGender = false }, modifier = Modifier.background(AppColors.Surface)) {
                                genderOptions.forEach { option ->
                                    DropdownMenuItem(text = { Text(option, color = AppColors.TextPrimary) }, onClick = { genero = option; expandedGender = false })
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text("Endereço", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = cep, onValueChange = { if (it.length <= 8 && it.all { c -> c.isDigit() }) cep = it }, label = "CEP", placeholder = "00000-000", isError = errorFields.contains("cep"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(2f)) { DarkTextField(value = rua, onValueChange = { rua = it }, label = "Rua", placeholder = "Nome da rua", isError = errorFields.contains("rua")) }
                    Spacer(Modifier.width(16.dp))
                    Box(Modifier.weight(1f)) { DarkTextField(value = numero, onValueChange = { if (it.all { c -> c.isDigit() }) numero = it }, label = "Número", placeholder = "000", isError = errorFields.contains("numero"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)) }
                }
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = bairro, onValueChange = { bairro = it }, label = "Bairro", placeholder = "Nome do bairro", isError = errorFields.contains("bairro"))
                Spacer(Modifier.height(16.dp))

                Row(Modifier.fillMaxWidth()) {
                    Box(Modifier.weight(2f)) {
                        DarkTextField(
                            value = cidade,
                            onValueChange = { cidade = it },
                            label = "Cidade",
                            placeholder = "Sua cidade",
                            isError = errorFields.contains("cidade")
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Box(Modifier.weight(1f)) {
                        DarkTextField(
                            value = estado,
                            onValueChange = { if (it.length <= 2 && it.all { c -> c.isLetter() }) estado = it.uppercase() },
                            label = "Estado",
                            placeholder = "UF",
                            isError = errorFields.contains("estado")
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))
                Text("Acessos", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = AppColors.TextPrimary)
                Spacer(Modifier.height(16.dp))
                DarkTextField(
                    value = celular,
                    onValueChange = { if (it.length <= 11 && it.all { c -> c.isDigit() }) celular = it },
                    label = "Celular",
                    placeholder = "11912341234",
                    isError = errorFields.contains("celular"),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                )
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = email, onValueChange = { email = it }, label = "E-mail", placeholder = "exemplo@email.com", isError = errorFields.contains("email"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email))
                Spacer(Modifier.height(16.dp))
                DarkTextField(value = senha, onValueChange = { senha = it }, label = "Senha", placeholder = "Mínimo 8 caracteres", isPassword = true, isError = errorFields.contains("senha"), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password))

                Spacer(Modifier.height(48.dp))

                Button(
                    onClick = {
                        val currentErrors = mutableSetOf<String>()
                        if (nomeCompleto.isBlank()) currentErrors.add("nomeCompleto")
                        if (cpf.length != 11) currentErrors.add("cpf")
                        val dateError = validateBirthDate(dataNascimento)
                        if (dateError != null) currentErrors.add("dataNascimento")
                        if (genero.isBlank()) currentErrors.add("genero")
                        if (cep.length != 8) currentErrors.add("cep")
                        if (rua.isBlank()) currentErrors.add("rua")
                        if (numero.isBlank()) currentErrors.add("numero")
                        if (bairro.isBlank()) currentErrors.add("bairro")
                        if (cidade.isBlank()) currentErrors.add("cidade")
                        if (estado.isBlank()) currentErrors.add("estado")
                        if (celular.isBlank()) currentErrors.add("celular")
                        if (!email.contains("@")) currentErrors.add("email")
                        if (!isPasswordStrong) currentErrors.add("senha")

                        if (currentErrors.isNotEmpty()) {
                            errorFields = currentErrors
                            errorMessage = dateError ?: "Preencha todos os campos obrigatórios corretamente"
                            isError = true
                            return@Button
                        }

                        val dto = UserRegisterProfileDTO(
                            name = nomeCompleto, email = email, password = senha, phone = celular, cpf = cpf, birthDate = dataNascimento,
                            gender = genero, addressCep = cep, addressStreet = rua, addressNumber = numero, addressNeighborhood = bairro,
                            addressCity = cidade, addressState = estado
                        )

                        viewModel.register(dto) { msg ->
                            onRegisterSuccess(msg)
                        }
                    },
                    enabled = isFormFilled && !isLoading,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isFormFilled) AppColors.Primary else AppColors.ButtonDisabled,
                        contentColor = if (isFormFilled) Color.White else AppColors.TextButtonDisabled
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Criar conta", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            Icon(Icons.Default.ArrowForward, null, Modifier.size(20.dp))
                        }
                    }
                }
                Spacer(Modifier.height(56.dp))
            }
        }

        // Logic to dismiss local error after 5s
        LaunchedEffect(isError) {
            if (isError) {
                delay(5000)
                isError = false
                errorMessage = ""
            }
        }

        AnimatedVisibility(
            visible = isError && errorMessage.isNotEmpty(),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.padding(top = 40.dp).align(Alignment.TopCenter)
        ) {
            ErrorBanner(message = errorMessage)
        }
    }
}
