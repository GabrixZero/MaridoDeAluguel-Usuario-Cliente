package com.example.doesitusuario.data.repository

import com.example.doesitusuario.data.model.*
import com.example.doesitusuario.data.network.CognitoService
import com.example.doesitusuario.data.network.RetrofitClient
import com.example.doesitusuario.data.network.SessionManager
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*

class UserRepository {
    private val api = RetrofitClient.apiService
    private val cognitoService = CognitoService()

    private fun saveSession(body: AuthResponse) {
        // Blindagem contra campos nulos vindos da API/Lambda
        val safeToken = body.token?.takeIf { it.isNotBlank() } ?: SessionManager.token
        val safeName = body.name ?: "Usuário"
        val safeEmail = body.email ?: ""

        SessionManager.save(
            token = safeToken,
            id = body.id, 
            name = safeName, 
            email = safeEmail,
            phone = body.phone ?: "", 
            cpf = body.cpf ?: "",
            birthDate = body.birthDate ?: "",
            gender = body.gender?.toString() ?: "", // Convertendo ID de gênero para String
            addressCep = body.addressCep ?: "",
            addressStreet = body.addressStreet ?: "",
            addressNumber = body.addressNumber ?: "",
            addressNeighborhood = body.addressNeighborhood ?: "",
            addressCity = body.addressCity ?: "",
            addressState = body.addressState ?: "",
            rating = body.rating ?: 0.0
        )
    }

    suspend fun login(email: String, password: String): Result<String> {
        val cognitoResult = cognitoService.signIn(email, password)
        return cognitoResult.fold(
            onSuccess = { token ->
                SessionManager.token = token
                getCurrentUser()
            },
            onFailure = { e ->
                val message = when (e) {
                    is NotAuthorizedException -> "E-mail ou senha incorretos."
                    is UserNotFoundException -> "Usuário não encontrado."
                    is UserNotConfirmedException -> "Conta não confirmada. Verifique seu e-mail."
                    is PasswordResetRequiredException -> "Redefinição de senha necessária."
                    else -> e.message ?: "Erro ao entrar"
                }
                Result.failure(Exception(message))
            }
        )
    }

    suspend fun getCurrentUser(): Result<String> = try {
        val r = api.getCurrentUser()
        if (r.isSuccessful && r.body() != null) {
            val body = r.body()!!
            saveSession(body)
            Result.success(body.name ?: "Usuário")
        } else {
            val errorMsg = r.errorBody()?.string() ?: "Erro desconhecido"
            android.util.Log.e("UserRepository", "API Error: ${r.code()} - $errorMsg")
            Result.failure(Exception("Erro ao carregar dados do usuário"))
        }
    } catch (e: Exception) {
        android.util.Log.e("UserRepository", "Connection Failure", e)
        Result.failure(Exception("Sem conexão com o servidor (Verifique sua internet)"))
    }

    suspend fun register(dto: UserRegisterProfileDTO): Result<String> {
        // Formatar data: dd/mm/aaaa -> aaaa-mm-dd
        val birthDateParts = dto.birthDate.split("/")
        val formattedBirthDate = if (birthDateParts.size == 3) {
            "${birthDateParts[2]}-${birthDateParts[1]}-${birthDateParts[0]}"
        } else {
            dto.birthDate
        }

        // Mapear gênero para ID String
        val genderId = when (dto.gender) {
            "Masculino" -> "1"
            "Feminino" -> "2"
            "Outros" -> "3"
            else -> "4"
        }

        val cognitoResult = cognitoService.signUp(
            email = dto.email,
            password = dto.password,
            name = dto.name,
            birthdate = formattedBirthDate,
            cpf = dto.cpf,
            genderId = genderId,
            phone = dto.phone,
            cep = dto.addressCep,
            street = dto.addressStreet,
            number = dto.addressNumber,
            neighborhood = dto.addressNeighborhood,
            city = dto.addressCity,
            state = dto.addressState
        )

        return cognitoResult.fold(
            onSuccess = {
                Result.success("Cadastro realizado com sucesso! Verifique seu e-mail.")
            },
            onFailure = { e ->
                android.util.Log.e("UserRepository", "Register error", e)
                val message = when (e) {
                    is UsernameExistsException -> "Este e-mail já está cadastrado."
                    is InvalidPasswordException -> "A senha não atende aos requisitos de segurança."
                    is InvalidParameterException -> e.message ?: "Dados inválidos. Verifique os campos."
                    is CodeDeliveryFailureException -> "Erro ao enviar código de verificação."
                    else -> e.message ?: "Erro ao criar conta"
                }
                Result.failure(Exception(message))
            }
        )
    }

    suspend fun forgotPassword(email: String): Result<String> = try {
        Result.failure(Exception("Funcionalidade em migração"))
    } catch (e: Exception) { Result.failure(Exception("Erro")) }

    suspend fun verifyCode(email: String, code: String): Result<String> {
        return Result.failure(Exception("Funcionalidade em migração"))
    }

    suspend fun resetPassword(email: String, code: String, newPass: String): Result<String> {
        return Result.failure(Exception("Funcionalidade em migração"))
    }

    suspend fun updateProfile(body: Map<String, String>): Result<String> = try {
        val r = api.updateProfile(SessionManager.bearerToken(), body)
        if (r.isSuccessful && r.body() != null) {
            val resp = r.body()!!
            saveSession(resp)
            Result.success(resp.name ?: "Usuário")
        } else Result.failure(Exception("Erro ao salvar perfil"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão com o servidor")) }

    suspend fun changePassword(current: String, newPass: String): Result<String> = try {
        val r = api.changePassword(SessionManager.bearerToken(), mapOf("currentPassword" to current, "newPassword" to newPass))
        if (r.isSuccessful) Result.success("Senha alterada com sucesso")
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("atual incorreta") -> "Senha atual incorreta"
                err.contains("6 caracteres") -> "Nova senha deve ter pelo menos 6 caracteres"
                else -> "Erro ao alterar senha"
            }))
        }
    } catch (e: Exception) { Result.failure(Exception("Sem conexão com o servidor")) }

    suspend fun deleteAccount(): Result<Unit> = try {
        val r = api.deleteAccount(SessionManager.bearerToken())
        if (r.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Erro ao excluir conta"))
    } catch (e: Exception) { Result.failure(Exception("Sem conexão com o servidor")) }

    // ── Endereços ─────────────────────────────────────────────────────────────
    suspend fun getAddresses(): Result<List<AddressDTO>> = try {
        val r = api.getAddresses()
        if (r.isSuccessful && r.body() != null) {
            Result.success(r.body()!!.addresses)
        } else {
            Result.failure(Exception("Erro ao buscar endereços"))
        }
    } catch (e: Exception) {
        android.util.Log.e("UserRepository", "Address Fetch Error", e)
        Result.failure(Exception("Sem conexão com o servidor"))
    }

    suspend fun getNotifications(): Result<List<NotificationDTO>> = try {
        val r = api.getNotifications()
        if (r.isSuccessful && r.body() != null) {
            Result.success(r.body()!!.notifications)
        } else {
            Result.failure(Exception("Erro ao buscar notificações"))
        }
    } catch (e: Exception) {
        android.util.Log.e("UserRepository", "Notification Fetch Error", e)
        Result.failure(Exception("Sem conexão com o servidor"))
    }
}
