package co.onmind.hex.domain.ports.out

import co.onmind.hex.domain.model.Role

interface NotificationPort {
    fun notifyRoleCreated(role: Role)
    fun notifyRoleUpdated(role: Role)
    fun notifyRoleDeleted(roleId: Long)
}