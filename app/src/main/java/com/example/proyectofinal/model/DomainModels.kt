package com.example.proyectofinal.model

enum class UserRole(val label: String) {
    CIUDADANO("Ciudadano"),
    ADMIN("Administrador")
}

enum class Department(val code: String, val displayName: String, val departmentArea: String) {
    BACHES("BACHES", "Baches", "Departamento de Obras Públicas"),
    ALUMBRADO("ALUMBRADO", "Alumbrado", "Departamento de Servicios Públicos"),
    BASURA("BASURA", "Basura", "Departamento de Limpia y Recolección");

    companion object {
        fun fromCode(code: String): Department =
            entries.find { it.code.equals(code, ignoreCase = true) || it.displayName.equals(code, ignoreCase = true) }
                ?: BACHES
    }
}

enum class ReportStatus(val code: String, val displayName: String) {
    PENDIENTE("PENDIENTE", "Pendiente"),
    EN_PROCESO("EN_PROCESO", "En proceso"),
    RESUELTO("RESUELTO", "Resuelto");

    companion object {
        fun fromCode(code: String): ReportStatus =
            entries.find { it.code.equals(code, ignoreCase = true) || it.displayName.equals(code, ignoreCase = true) }
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
