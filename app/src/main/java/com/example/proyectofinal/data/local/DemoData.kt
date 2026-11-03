package com.example.proyectofinal.data.local
import com.example.proyectofinal.data.local.entity.ReportEntity
object DemoData {
    const val PHOTO = "file:///android_asset/demo_incidente.png"
    const val RESOLVED_PHOTO = "file:///android_asset/demo_resuelto.png"
    fun reports(): List<ReportEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            ReportEntity("CA-1001", "ciudadano@comunidad.mx", "Juan Ciudadano", "BACHES", "Bache profundo de más de un metro que obstruye el carril derecho y daña llantas.", "Av. Insurgentes esquina con Hidalgo #142", "PENDIENTE", PHOTO, null, "Reporte recibido en Obras Públicas.", null, now-259200000, now-259200000),
            ReportEntity("CA-1002", "vecina@comunidad.mx", "María López", "ALUMBRADO", "Tres luminarias apagadas frente al parque dejan la acera totalmente oscura.", "Calle Juárez frente al Parque Central #45", "EN_PROCESO", PHOTO, null, "Cuadrilla eléctrica asignada para revisión.", "admin@ciudadalerta.mx", now-172800000, now-86400000, 2),
            ReportEntity("CA-1003", "ciudadano@comunidad.mx", "Juan Ciudadano", "BASURA", "Acumulación de bolsas de basura y ramas por falta de recolección.", "Calle Morelos #310 esquina con Allende", "RESUELTO", PHOTO, RESOLVED_PHOTO, "Camión recolector retiró los desechos y limpió la banqueta.", "admin@ciudadalerta.mx", now-432000000, now-18000000, 3)
        )
    }
}
