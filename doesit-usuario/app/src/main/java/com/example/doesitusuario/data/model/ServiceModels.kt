package com.example.doesitusuario.data.model

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────────
data class UserLoginProfileDTO(
    @SerializedName("email")    val email: String,
    @SerializedName("password") val password: String
)

data class UserRegisterProfileDTO(
    @SerializedName("name")                val name: String,
    @SerializedName("email")               val email: String,
    @SerializedName("password")            val password: String,
    @SerializedName("phone")               val phone: String,
    @SerializedName("cpf")                 val cpf: String,
    @SerializedName("birthDate")           val birthDate: String,
    @SerializedName("gender")              val gender: String,
    @SerializedName("role")                val role: String = "USER",
    @SerializedName("addressCep")          val addressCep: String = "",
    @SerializedName("addressStreet")       val addressStreet: String = "",
    @SerializedName("addressNumber")       val addressNumber: String = "",
    @SerializedName("addressNeighborhood") val addressNeighborhood: String = "",
    @SerializedName("addressCity")         val addressCity: String = "",
    @SerializedName("addressState")        val addressState: String = ""
)

data class AuthResponse(
    @SerializedName("token")           val token: String? = null,
    @SerializedName("id")              val id: Long = 0,
    @SerializedName("nome")            val name: String? = null,
    @SerializedName("email")           val email: String? = null,
    @SerializedName("tipo")            val role: String? = null,
    @SerializedName("telefone")        val phone: String? = null,
    @SerializedName("cpf")             val cpf: String? = null,
    @SerializedName("data_nascimento") val birthDate: String? = null,
    @SerializedName("genero")          val gender: String? = null,
    @SerializedName("cep")             val addressCep: String? = null,
    @SerializedName("rua")             val addressStreet: String? = null,
    @SerializedName("numero")          val addressNumber: String? = null,
    @SerializedName("bairro")          val addressNeighborhood: String? = null,
    @SerializedName("cidade")          val addressCity: String? = null,
    @SerializedName("estado")          val addressState: String? = null,
    @SerializedName("rating")          val rating: Double? = 0.0
)

// ── Categorias ────────────────────────────────────────────────────────────────
data class ServiceCategory(
    @SerializedName("id")   val id: Long,
    @SerializedName("nome") val name: String,
    val basePrice: Double = 0.0,
    val icon: String? = null
)

// ── Prestadores ───────────────────────────────────────────────────────────────
data class ProviderDTO(
    @SerializedName("id")             val id: Long,
    @SerializedName("name")           val name: String,
    @SerializedName("email")          val email: String?,
    @SerializedName("rating")         val rating: Double?,
    @SerializedName("ratingCount")    val ratingCount: Int?,
    @SerializedName("online")         val isOnline: Boolean = false,
    @SerializedName("specialtyPrice") val specialtyPrice: Double? = null
)

// ── Pedidos ───────────────────────────────────────────────────────────────────
data class ServiceRequestCreate(
    @SerializedName("categoryId")         val categoryId: Long,
    @SerializedName("description")        val description: String,
    @SerializedName("type")               val type: String,
    @SerializedName("scheduledAt")        val scheduledAt: String? = null,
    @SerializedName("preferredProviderId") val preferredProviderId: Long? = null,
    @SerializedName("addressId")          val addressId: Long? = null
)

data class ServiceRequestDTO(
    @SerializedName("id")          val id: Long = 0,
    @SerializedName("servico")     val serviceName: String,
    @SerializedName("cliente")     val clientName: String? = null,
    @SerializedName("prestador")   val providerName: String? = null,
    @SerializedName("data")        val date: String,
    @SerializedName("valor")       val value: Double,
    @SerializedName("endereco")    val address: String? = null,
    @SerializedName("descricao")   val description: String? = null,
    @SerializedName("status", alternate = ["status_nome"]) val status: String,
    @SerializedName("status_id")   val statusId: Int? = null,
    @SerializedName("minha_role")  val myRole: String? = null,
    
    // Campo para compatibilidade com o History/NotificationStore (nome da outra parte)
    @SerializedName("nome_parte")  val otherPartyName: String? = null
)

data class OrderListResponse(
    @SerializedName("pedidos") val orders: List<ServiceRequestDTO>
)

// ── Avaliação ─────────────────────────────────────────────────────────────────
data class RatingRequest(
    @SerializedName("serviceRequestId") val serviceRequestId: Long,
    @SerializedName("stars")            val stars: Int,
    @SerializedName("comment")          val comment: String
)

// ── Notificações ──────────────────────────────────────────────────────────────
data class NotificationDTO(
    @SerializedName("titulo")         val title: String,
    @SerializedName("mensagem")       val message: String,
    @SerializedName("dt_notificacao") val timestamp: String
)

data class NotificationListResponse(
    @SerializedName("notificacoes") val notifications: List<NotificationDTO>
)

// ── Endereços ─────────────────────────────────────────────────────────────────
data class AddressDTO(
    @SerializedName("id")           val id: Long = 0,
    @SerializedName("titulo")       val tag: String? = null,
    @SerializedName("cep")          val cep: String? = null,
    @SerializedName("rua")          val street: String? = null,
    @SerializedName("numero")       val number: String? = null,
    @SerializedName("complemento")  val complement: String? = null,
    @SerializedName("referencia")   val reference: String? = null,
    @SerializedName("bairro")       val neighborhood: String? = null,
    @SerializedName("cidade")       val city: String? = null,
    @SerializedName("estado")       val state: String? = null,
    @SerializedName("is_favorite")  val isDefault: Boolean = false,
    @SerializedName("formatado")    val formatted: String? = null
)

data class AddressListResponse(
    @SerializedName("enderecos") val addresses: List<AddressDTO>
)

// ── Solicitar Serviço Form ────────────────────────────────────────────────────
data class ServiceRequestFormResponse(
    @SerializedName("genero")                  val gender: String,
    @SerializedName("mostrar_filtro_mulheres") val showWomenFilter: Boolean,
    @SerializedName("enderecos")               val addresses: List<AddressDTO>,
    @SerializedName("servicos")                val services: List<ServiceCategory>
)
