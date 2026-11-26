package com.example.proyectofinal

import com.example.proyectofinal.model.*
import org.junit.Assert.*
import org.junit.Test

class DomainAndApiTest {
    @Test
    fun testDomainValidations() {
        val validDescription = "Esta es una descripción válida porque tiene más de 20 caracteres."
        assertNull(ReportValidator.validateDescription(validDescription))

        val invalidDescription = "Corta"
        assertNotNull(ReportValidator.validateDescription(invalidDescription))

        val validLocation = "Calle Principal #123"
        assertNull(ReportValidator.validateLocation(validLocation))

        val invalidLocation = "Aquí"
        assertNotNull(ReportValidator.validateLocation(invalidLocation))

        val validObservation = "Caso atendido por cuadrilla"
        assertNull(ReportValidator.validateAdminObservation(validObservation))
    }

    @Test
    fun testStateTransitions() {
        assertTrue(ReportStatus.canTransition(ReportStatus.PENDIENTE, ReportStatus.EN_PROCESO))
        assertTrue(ReportStatus.canTransition(ReportStatus.EN_PROCESO, ReportStatus.RESUELTO))
        assertFalse(ReportStatus.canTransition(ReportStatus.PENDIENTE, ReportStatus.RESUELTO))
        assertFalse(ReportStatus.canTransition(ReportStatus.RESUELTO, ReportStatus.PENDIENTE))
    }
}
