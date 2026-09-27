package com.trigenys.cleanroute.communication

import android.content.Context
import android.content.Intent
import com.trigenys.cleanroute.domain.ContactActionRepository
import com.trigenys.cleanroute.domain.ContactChannel
import com.trigenys.cleanroute.domain.CustomerId
import java.time.Instant

sealed interface ContactLaunchResult {
    data object Launched : ContactLaunchResult
    data class Unavailable(val message: String) : ContactLaunchResult
    data class Failed(val message: String) : ContactLaunchResult
}

class CustomerContactService(
    private val repository: ContactActionRepository,
    private val intentFactory: ContactIntentFactory,
    private val templates: CustomerMessageTemplateCatalog,
    private val whatsAppPackages: List<String> = listOf(
        "com.whatsapp",
        "com.whatsapp.w4b"
    )
) {
    suspend fun call(
        context: Context,
        customerId: CustomerId,
        rawPhone: String,
        at: Instant
    ): ContactLaunchResult {
        val intent = intentFactory.dial(rawPhone)
            ?: return ContactLaunchResult.Unavailable("Numéro de téléphone invalide.")

        if (intent.resolveActivity(context.packageManager) == null) {
            return ContactLaunchResult.Unavailable("Aucune application d’appel disponible.")
        }

        return launch(
            context = context,
            intent = intent,
            customerId = customerId,
            channel = ContactChannel.CALL,
            at = at,
            fallbackMessage = "Impossible d’ouvrir l’application d’appel."
        )
    }

    suspend fun whatsApp(
        context: Context,
        customerId: CustomerId,
        rawPhone: String,
        kind: CustomerMessageKind?,
        data: CustomerMessageData?,
        at: Instant
    ): ContactLaunchResult {
        val message = when {
            kind == null -> null
            data == null -> return ContactLaunchResult.Failed(
                "Les informations nécessaires au message sont indisponibles."
            )
            else -> templates.render(kind, data)
                ?: return ContactLaunchResult.Failed(
                    "Les informations nécessaires au message sont indisponibles."
                )
        }

        val packageName = whatsAppPackages.firstOrNull {
            context.packageManager.getLaunchIntentForPackage(it) != null
        } ?: return ContactLaunchResult.Unavailable(
            "WhatsApp n’est pas installé sur ce téléphone."
        )

        val intent = intentFactory.whatsApp(
            rawPhone = rawPhone,
            message = message,
            packageName = packageName
        ) ?: return ContactLaunchResult.Unavailable("Numéro de téléphone invalide.")

        if (intent.resolveActivity(context.packageManager) == null) {
            return ContactLaunchResult.Unavailable(
                "WhatsApp ne peut pas ouvrir cette conversation."
            )
        }

        return launch(
            context = context,
            intent = intent,
            customerId = customerId,
            channel = ContactChannel.WHATSAPP,
            at = at,
            fallbackMessage = "Impossible d’ouvrir WhatsApp."
        )
    }

    private suspend fun launch(
        context: Context,
        intent: Intent,
        customerId: CustomerId,
        channel: ContactChannel,
        at: Instant,
        fallbackMessage: String
    ): ContactLaunchResult =
        try {
            context.startActivity(intent)
            runCatching {
                repository.record(
                    customerId = customerId,
                    channel = channel,
                    at = at
                )
            }
            ContactLaunchResult.Launched
        } catch (_: Exception) {
            ContactLaunchResult.Failed(fallbackMessage)
        }
}
