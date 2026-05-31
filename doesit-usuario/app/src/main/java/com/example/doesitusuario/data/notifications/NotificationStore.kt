package com.example.doesitusuario.data.notifications

import androidx.compose.runtime.mutableStateListOf

data class NotificationItem(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: String,
    var read: Boolean = false
)

object NotificationStore {
    val items = mutableStateListOf<NotificationItem>()

    val unreadCount get() = items.count { !it.read }

    fun addOrUpdate(item: NotificationItem) {
        val idx = items.indexOfFirst { it.id == item.id }
        if (idx >= 0) {
            // Preserva o estado 'read' — nunca reseta para não-lida ao re-sincronizar
            items[idx] = item.copy(read = items[idx].read)
        } else {
            items.add(0, item)
        }
    }

    fun markAllRead() { items.replaceAll { it.copy(read = true) } }

    fun markRead(id: String) {
        val idx = items.indexOfFirst { it.id == id }
        if (idx >= 0) items[idx] = items[idx].copy(read = true)
    }

    fun clear() { items.clear() }

    fun syncFromHistory(requests: List<com.example.doesitusuario.data.model.ServiceRequestDTO>) {
        requests.forEach { r ->
            when (r.status.uppercase()) {
                "ACCEPTED", "AGENDADO" -> addOrUpdate(NotificationItem(
                    id = "${r.id}_ACCEPTED",
                    title = "Pedido aceito! ✅",
                    body = "${r.otherPartyName} aceitou seu pedido de ${r.serviceName}.",
                    timestamp = r.date, read = false
                ))
                "COMPLETED", "CONCLUIDO" -> addOrUpdate(NotificationItem(
                    id = "${r.id}_COMPLETED",
                    title = "Serviço concluído! 🎉",
                    body = "Seu serviço de ${r.serviceName} foi concluído. Avalie o prestador!",
                    timestamp = r.date, read = false
                ))
                "REFUSED" -> addOrUpdate(NotificationItem(
                    id = "${r.id}_REFUSED",
                    title = "Agendamento recusado ❌",
                    body = "O prestador recusou seu agendamento de ${r.serviceName}. Faça uma nova solicitação.",
                    timestamp = r.date, read = false
                ))
                "CANCELLED", "CANCELADO" -> addOrUpdate(NotificationItem(
                    id = "${r.id}_CANCELLED",
                    title = "Pedido cancelado",
                    body = "Seu pedido de ${r.serviceName} foi cancelado.",
                    timestamp = r.date, read = true
                ))
            }
        }
        // Ordena: mais recente no topo (ISO strings são comparáveis lexicograficamente)
        val sorted = items.sortedByDescending { it.timestamp }
        items.clear()
        items.addAll(sorted)
    }
}
