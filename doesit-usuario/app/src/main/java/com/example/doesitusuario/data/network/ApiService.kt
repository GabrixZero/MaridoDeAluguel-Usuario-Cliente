package com.example.doesitusuario.data.network

import com.example.doesitusuario.data.model.*
import retrofit2.Response
import retrofit2.http.*

interface ApiService {
    // ── Auth ──────────────────────────────────────────────────────────────────
    @POST("api/auth/login")
    suspend fun login(@Body body: UserLoginProfileDTO): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body body: UserRegisterProfileDTO): Response<AuthResponse>

    @POST("api/auth/forgot-password")
    suspend fun forgotPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/verify-code")
    suspend fun verifyCode(@Body body: Map<String, String>): Response<Map<String, String>>

    @POST("api/auth/reset-password")
    suspend fun resetPassword(@Body body: Map<String, String>): Response<Map<String, String>>

    // ── Usuário ───────────────────────────────────────────────────────────────
    @GET("usuario")
    suspend fun getCurrentUser(): Response<AuthResponse>

    @PUT("api/users/me")
    suspend fun updateProfile(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<AuthResponse>

    @PUT("api/users/me/password")
    suspend fun changePassword(@Header("Authorization") token: String, @Body body: Map<String, String>): Response<Map<String, String>>

    @DELETE("api/users/me")
    suspend fun deleteAccount(@Header("Authorization") token: String): Response<Unit>

    // ── Categorias ────────────────────────────────────────────────────────────
    @GET("api/categories")
    suspend fun getCategories(@Header("Authorization") token: String): Response<List<ServiceCategory>>

    @GET("solicitar-servico")
    suspend fun getServiceRequestForm(): Response<ServiceRequestFormResponse>

    // ── Prestadores ───────────────────────────────────────────────────────────
    @POST("retorna-profissionais-disponiveis")
    suspend fun getAvailableProviders(
        @Header("Authorization") token: String,
        @Body body: AvailableProvidersRequest
    ): Response<AvailableProvidersResponse>

    @GET("api/providers/for-category/{categoryId}")
    suspend fun getProvidersForCategory(
        @Header("Authorization") token: String,
        @Path("categoryId") categoryId: Long,
        @Query("onlineOnly") onlineOnly: Boolean,
        @Query("onlyWomen") onlyWomen: Boolean
    ): Response<List<ProviderDTO>>

    // ── Pedidos ───────────────────────────────────────────────────────────────
    @POST("confirmar-agendamento")
    suspend fun confirmBooking(
        @Header("Authorization") token: String,
        @Body body: ConfirmBookingRequest
    ): Response<ConfirmBookingResponse>

    @PUT("atualizar-pedido")
    suspend fun updateOrderStatus(
        @Header("Authorization") token: String,
        @Body body: UpdateOrderStatusRequest
    ): Response<UpdateOrderStatusResponse>

    @POST("api/requests")
    suspend fun createRequest(@Header("Authorization") token: String, @Body body: ServiceRequestCreate): Response<ServiceRequestDTO>

    @GET("meus-pedidos")
    suspend fun getMyHistory(@Query("status") status: String? = null): Response<OrderListResponse>

    @GET("detalhes-pedido")
    suspend fun getRequestById(@Query("id") id: Long): Response<ServiceRequestDTO>

    // ── Avaliação ──
    @POST("api/ratings")
    suspend fun rate(@Header("Authorization") token: String, @Body rating: RatingRequest): Response<Any>

    // ── Endereços ─────────────────────────────────────────────────────────────
    @GET("meus-enderecos")
    suspend fun getAddresses(): Response<AddressListResponse>

    @POST("cadastrar-endereco")
    suspend fun createAddress(@Body body: Map<String, String>): Response<AddressDTO>

    @PUT("atualizar-endereco")
    suspend fun updateAddress(@Query("id") id: Long, @Body body: Map<String, String>): Response<AddressDTO>

    @PUT("api/addresses/{id}/default")
    suspend fun setDefaultAddress(@Header("Authorization") token: String, @Path("id") id: Long): Response<AddressDTO>

    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Long): Response<Unit>

    // ── Notificações ──────────────────────────────────────────────────────────
    @GET("minhas-notificacoes")
    suspend fun getNotifications(): Response<NotificationListResponse>
}
