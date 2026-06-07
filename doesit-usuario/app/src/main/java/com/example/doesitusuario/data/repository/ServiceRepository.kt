package com.example.doesitusuario.data.repository

import com.example.doesitusuario.data.model.*
import com.example.doesitusuario.data.network.RetrofitClient
import com.example.doesitusuario.data.network.SessionManager

class ServiceRepository {
    private val api = RetrofitClient.apiService

    suspend fun getCategories() = try {
        val r = api.getCategories(SessionManager.bearerToken())
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar categorias"))
    } catch (e: Exception) { Result.failure<List<ServiceCategory>>(Exception("Sem conexão")) }

    suspend fun getServiceRequestForm() = try {
        val r = api.getServiceRequestForm()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar dados do formulário"))
    } catch (e: Exception) { Result.failure<ServiceRequestFormResponse>(Exception("Sem conexão")) }

    suspend fun getAvailableProviders(request: AvailableProvidersRequest) = try {
        val r = api.getAvailableProviders(SessionManager.bearerToken(), request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.providers)
        else Result.failure(Exception("Nenhum prestador encontrado"))
    } catch (e: Exception) { Result.failure<List<ProviderAvailableDTO>>(Exception("Sem conexão")) }

    suspend fun confirmBooking(request: ConfirmBookingRequest) = try {
        val r = api.confirmBooking(SessionManager.bearerToken(), request)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao confirmar agendamento"))
    } catch (e: Exception) { Result.failure<ConfirmBookingResponse>(Exception("Sem conexão")) }

    suspend fun getProvidersForCategory(
        categoryId: Long,
        onlineOnly: Boolean,
        onlyWomen: Boolean
    ) = try {
        val r = api.getProvidersForCategory(SessionManager.bearerToken(), categoryId, onlineOnly, onlyWomen)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Nenhum prestador encontrado"))
    } catch (e: Exception) { Result.failure<List<ProviderDTO>>(Exception("Sem conexão")) }

    suspend fun createRequest(
        categoryId: Long,
        description: String,
        type: String,
        scheduledAt: String? = null,
        preferredProviderId: Long? = null,
        addressId: Long? = null
    ) = try {
        val r = api.createRequest(
            SessionManager.bearerToken(),
            ServiceRequestCreate(categoryId, description, type, scheduledAt, preferredProviderId, addressId)
        )
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao criar solicitação"))
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    suspend fun getHistory(status: String? = null) = try {
        val r = api.getMyHistory(status)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.orders)
        else Result.failure(Exception("Erro ao carregar histórico"))
    } catch (e: Exception) { Result.failure<List<ServiceRequestDTO>>(Exception("Sem conexão")) }

    suspend fun getById(id: Long) = try {
        val r = api.getRequestById(id)
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!)
        else Result.failure(Exception("Erro ao carregar pedido"))
    } catch (e: Exception) { Result.failure<ServiceRequestDTO>(Exception("Sem conexão")) }

    // Rota Única de Atualização (@PUT("atualizar-pedido"))
    suspend fun updateOrderStatus(requestId: Long, newStatusId: Int) = try {
        val r = api.updateOrderStatus(
            SessionManager.bearerToken(),
            UpdateOrderStatusRequest(requestId, newStatusId)
        )
        if (r.isSuccessful) Result.success(r.body()!!)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(if (err.isNotBlank()) err else "Erro ao atualizar pedido"))
        }
    } catch (e: Exception) { Result.failure<UpdateOrderStatusResponse>(Exception("Sem conexão")) }

    // Métodos de conveniência baseados nos status fornecidos:
    // 3 - CONCLUIDO, 4 - CANCELADO
    suspend fun cancelRequest(id: Long) = updateOrderStatus(id, 4)
    suspend fun confirmFinish(id: Long) = updateOrderStatus(id, 3)

    suspend fun rate(serviceRequestId: Long, stars: Int, comment: String) = try {
        val r = api.rate(SessionManager.bearerToken(), RatingRequest(serviceRequestId, stars, comment))
        if (r.isSuccessful) Result.success(Unit)
        else {
            val err = r.errorBody()?.string() ?: ""
            Result.failure(Exception(when {
                err.contains("já avaliou") -> "Você já avaliou este serviço"
                else -> "Erro ao enviar avaliação"
            }))
        }
    } catch (e: Exception) { Result.failure<Unit>(Exception("Sem conexão")) }

    suspend fun getAddresses() = try {
        val r = api.getAddresses()
        if (r.isSuccessful && r.body() != null) Result.success(r.body()!!.addresses)
        else Result.failure(Exception("Erro ao carregar endereços"))
    } catch (e: Exception) { Result.failure<List<AddressDTO>>(Exception("Sem conexão")) }
}
