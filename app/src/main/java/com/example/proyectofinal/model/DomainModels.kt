package com.example.proyectofinal.model

import androidx.annotation.StringRes
import com.example.proyectofinal.R

enum class UserRole(@StringRes val label: Int) {
    CIUDADANO(R.string.role_citizen),
    ADMIN(R.string.role_admin)
}

enum class Department(val code: String, val legacyName: String, @StringRes val displayNameRes: Int, @StringRes val departmentAreaRes: Int) {
    BACHES("BACHES", "Baches", R.string.dept_baches, R.string.area_obras_publicas),
    ALUMBRADO("ALUMBRADO", "Alumbrado", R.string.dept_alumbrado, R.string.area_servicios_publicos),
    BASURA("BASURA", "Basura", R.string.dept_basura, R.string.area_limpia);

    companion object {
        fun fromCode(code: String): Department =
            entries.find { it.code.equals(code, ignoreCase = true) || it.legacyName.equals(code, ignoreCase = true) }
                ?: BACHES
    }
}

enum class ReportStatus(val code: String, val legacyName: String, @StringRes val displayNameRes: Int) {
    PENDIENTE("PENDIENTE", "Pendiente", R.string.status_pending),
    EN_PROCESO("EN_PROCESO", "En proceso", R.string.status_in_process),
    RESUELTO("RESUELTO", "Resuelto", R.string.status_resolved);

    companion object {
        fun fromCode(code: String): ReportStatus =
            entries.find { it.code.equals(code, ignoreCase = true) || it.legacyName.equals(code, ignoreCase = true) }
                ?: PENDIENTE

        fun canTransition(from: ReportStatus, to: ReportStatus): Boolean {
            return (from == PENDIENTE && to == EN_PROCESO) ||
                   (from == EN_PROCESO && to == RESUELTO)
        }
    }
}

enum class MultimediaType(val code: String) {
    EVIDENCIA_INICIAL("EVIDENCIA_INICIAL"),
    EVIDENCIA_RESUELTO("EVIDENCIA_RESUELTO")
}

data class UserSession(
    val uid: String,
    val email: String,
    val nombre: String,
    val role: UserRole
)

data class DepartmentKpi(
    val department: Department,
    val total: Int,
    val pendientes: Int,
    val enProceso: Int,
    val resueltos: Int
)

object ReportValidator {
    fun validateDescription(desc: String): String? {
        val trimmed = desc.trim()
        return when {
            trimmed.isEmpty() -> "La descripción es obligatoria."
            trimmed.length < 20 -> "Mínimo 20 caracteres (actual: ${trimmed.length})."
            trimmed.length > 1000 -> "Máximo 1000 caracteres."
            else -> null
        }
    }

    fun validateLocation(loc: String): String? {
        val trimmed = loc.trim()
        return when {
            trimmed.isEmpty() -> "La ubicación escrita es obligatoria."
            trimmed.length < 10 -> "Mínimo 10 caracteres (actual: ${trimmed.length})."
            trimmed.length > 200 -> "Máximo 200 caracteres."
            else -> null
        }
    }

    fun validateAdminObservation(obs: String): String? {
        val trimmed = obs.trim()
        return when {
            trimmed.isEmpty() -> "La observación es obligatoria para cambiar el estado."
            trimmed.length < 5 -> "La observación debe tener al menos 5 caracteres."
            trimmed.length > 500 -> "Máximo 500 caracteres."
            else -> null
        }
    }
}
