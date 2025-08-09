package co.onmind.hex.config

import co.onmind.hex.domain.ports.out.NotificationPort
import co.onmind.hex.domain.ports.out.RoleRepositoryPort
import co.onmind.hex.infrastructure.notification.adapter.NotificationAdapter
import co.onmind.hex.infrastructure.persistence.adapter.RoleRepositoryAdapter
import io.micronaut.context.annotation.Factory
import jakarta.inject.Singleton

@Factory
class BeanConfiguration {

    @Singleton
    fun roleRepositoryPort(adapter: RoleRepositoryAdapter): RoleRepositoryPort = adapter

    @Singleton
    fun notificationPort(adapter: NotificationAdapter): NotificationPort = adapter
}