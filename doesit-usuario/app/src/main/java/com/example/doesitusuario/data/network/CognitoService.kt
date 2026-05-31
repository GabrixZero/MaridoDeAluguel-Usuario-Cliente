package com.example.doesitusuario.data.network

import aws.sdk.kotlin.services.cognitoidentityprovider.CognitoIdentityProviderClient
import aws.sdk.kotlin.services.cognitoidentityprovider.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object CognitoConfig {
    const val REGION = "sa-east-1"
    const val USER_POOL_ID = "sa-east-1_MHcE8ykG1"
    const val CLIENT_ID = "7ojgimoe1rktluu5ait0d4gdtt"
    const val CLIENT_SECRET = "atgrc64f5i7kefee7mrd3bfehh71nsjr43orf2spg259otq85m5"
}

class CognitoService {

    private val client = CognitoIdentityProviderClient { region = CognitoConfig.REGION }

    private fun calculateSecretHash(userName: String): String {
        val hmacSha256 = "HmacSHA256"
        val secretKeySpec = SecretKeySpec(CognitoConfig.CLIENT_SECRET.toByteArray(), hmacSha256)
        val mac = Mac.getInstance(hmacSha256)
        mac.init(secretKeySpec)
        mac.update(userName.toByteArray())
        val rawHmac = mac.doFinal(CognitoConfig.CLIENT_ID.toByteArray())
        return android.util.Base64.encodeToString(rawHmac, android.util.Base64.NO_WRAP)
    }

    suspend fun signUp(
        email: String,
        password: String,
        name: String,
        birthdate: String, // YYYY-MM-DD
        cpf: String,
        genderId: String,
        phone: String,
        cep: String,
        street: String,
        number: String,
        neighborhood: String,
        city: String,
        state: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val attributes = listOf(
                AttributeType { this.name = "name"; this.value = name },
                AttributeType { this.name = "birthdate"; this.value = birthdate },
                AttributeType { this.name = "address"; this.value = cep },
                AttributeType { this.name = "custom:cpf"; this.value = cpf },
                AttributeType { this.name = "custom:tipo_usuario"; this.value = "CLIENTE" },
                AttributeType { this.name = "custom:id_genero"; this.value = genderId },
                AttributeType { this.name = "custom:telefone"; this.value = phone },
                AttributeType { this.name = "custom:rua"; this.value = street },
                AttributeType { this.name = "custom:numero"; this.value = number },
                AttributeType { this.name = "custom:bairro"; this.value = neighborhood },
                AttributeType { this.name = "custom:cidade"; this.value = city },
                AttributeType { this.name = "custom:estado"; this.value = state }
            )

            val request = SignUpRequest {
                clientId = CognitoConfig.CLIENT_ID
                secretHash = calculateSecretHash(email)
                username = email
                this.password = password
                this.userAttributes = attributes
            }

            client.signUp(request)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signIn(email: String, password: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val authParameters = mapOf(
                "USERNAME" to email,
                "PASSWORD" to password,
                "SECRET_HASH" to calculateSecretHash(email)
            )

            val request = InitiateAuthRequest {
                clientId = CognitoConfig.CLIENT_ID
                authFlow = AuthFlowType.UserPasswordAuth
                this.authParameters = authParameters
            }

            val response = client.initiateAuth(request)
            val idToken = response.authenticationResult?.idToken
            if (idToken != null) {
                Result.success(idToken)
            } else {
                Result.failure(Exception("Falha na autenticação: ID Token não recebido"))
            }
        } catch (e: NotAuthorizedException) {
            Result.failure(Exception("E-mail ou senha incorretos"))
        } catch (e: UserNotFoundException) {
            Result.failure(Exception("Usuário não encontrado"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
