package com.trigenys.cleanroute.data.transfer

import java.text.Normalizer
import java.util.Locale

internal object HeaderMapping {
    private val aliases = mapOf(
        "external_id" to setOf(
            "external_id", "id", "code_client", "customer_id", "client_id", "code"
        ),
        "name" to setOf(
            "name", "nom", "client", "nom_client", "customer", "customer_name"
        ),
        "phone" to setOf(
            "phone", "telephone", "tel", "mobile", "numero", "numero_telephone"
        ),
        "zone" to setOf(
            "zone", "quartier", "secteur", "area", "neighborhood"
        ),
        "address_label" to setOf(
            "address_label", "adresse", "repere", "reference", "location"
        ),
        "collection_frequency" to setOf(
            "collection_frequency", "frequence", "periodicite", "cadence"
        ),
        "monthly_fee_xaf" to setOf(
            "monthly_fee_xaf", "montant", "abonnement", "tarif", "prix", "fee"
        ),
        "status" to setOf(
            "status", "statut", "etat", "state"
        )
    )

    val required = setOf("name", "zone", "monthly_fee_xaf")

    fun map(headers: List<String>): HeaderMap {
        val canonicalByIndex = mutableMapOf<Int, String>()
        val unknown = mutableListOf<String>()

        headers.forEachIndexed { index, raw ->
            val normalized = normalize(raw)
            val canonical = aliases.entries.firstOrNull { normalized in it.value }?.key
            if (canonical == null) {
                if (raw.isNotBlank()) unknown += raw.trim()
            } else if (canonical !in canonicalByIndex.values) {
                canonicalByIndex[index] = canonical
            }
        }

        val missing = required - canonicalByIndex.values.toSet()
        return HeaderMap(
            canonicalByIndex = canonicalByIndex,
            unknownHeaders = unknown,
            missingRequired = missing.sorted()
        )
    }

    fun normalize(value: String): String {
        val decomposed = Normalizer.normalize(
            value.replace("\uFEFF", "").trim(),
            Normalizer.Form.NFD
        )
        return decomposed
            .replace(Regex("\\p{Mn}+"), "")
            .lowercase(Locale.ROOT)
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')
    }
}

internal data class HeaderMap(
    val canonicalByIndex: Map<Int, String>,
    val unknownHeaders: List<String>,
    val missingRequired: List<String>
) {
    fun value(row: List<String>, canonical: String): String? {
        val index = canonicalByIndex.entries.firstOrNull { it.value == canonical }?.key
            ?: return null
        return row.getOrNull(index)?.trim()?.takeIf(String::isNotEmpty)
    }
}
