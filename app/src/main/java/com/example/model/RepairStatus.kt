package com.example.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.*

enum class RepairStatus(
    val label: String,
    val shortLabel: String,
    val description: String,
    val stepOrder: Int
) {
    RECIBIDO(
        label = "Ingreso / Recibido en Taller",
        shortLabel = "Ingresos",
        description = "Equipo ingresado. Pendiente de revisión técnica inicial.",
        stepOrder = 1
    ),
    DIAGNOSTICO(
        label = "En Diagnóstico",
        shortLabel = "En Diagnóstico",
        description = "Técnico examinando componentes, mediciones y causas de falla.",
        stepOrder = 2
    ),
    PRESUPUESTADO(
        label = "Diagnosticado / Presupuestado",
        shortLabel = "Diagnosticados",
        description = "Diagnóstico completado. Presupuesto listo o aprobado.",
        stepOrder = 3
    ),
    ESPERANDO_REPUESTO(
        label = "Esperando Repuesto",
        shortLabel = "Esp. Repuesto",
        description = "Pieza en pedido a proveedor o esperando refacción.",
        stepOrder = 4
    ),
    EN_REPARACION(
        label = "En Reparación",
        shortLabel = "En Reparación",
        description = "Trabajo técnico activo: microelectrónica, soldadura o ensamble.",
        stepOrder = 5
    ),
    LISTO(
        label = "Reparado / Listo para Entrega",
        shortLabel = "Reparados",
        description = "Reparación finalizada con éxito y pruebas de calidad aprobadas.",
        stepOrder = 6
    ),
    ENTREGADO(
        label = "Entregado al Cliente",
        shortLabel = "Entregados",
        description = "Equipo retirado por el cliente con garantía vigente.",
        stepOrder = 7
    ),
    EN_GARANTIA(
        label = "En Garantía",
        shortLabel = "En Garantía",
        description = "Equipo retornado por servicio de garantía o revisión posterior.",
        stepOrder = 8
    ),
    NO_REPARADO(
        label = "No Reparado",
        shortLabel = "No Reparados",
        description = "Equipo sin reparación por daño irreparable o inviabilidad.",
        stepOrder = 9
    ),
    CANCELADO(
        label = "Cancelado",
        shortLabel = "Cancelados",
        description = "Orden cancelada o presupuesto rechazado.",
        stepOrder = 10
    );

    val backgroundColor: Color
        get() = when (this) {
            RECIBIDO -> StatusReceivedBg
            DIAGNOSTICO -> StatusDiagBg
            PRESUPUESTADO -> StatusDiagnosedBg
            ESPERANDO_REPUESTO -> StatusWaitingPartsBg
            EN_REPARACION -> StatusInRepairBg
            LISTO -> StatusReadyBg
            ENTREGADO -> StatusDeliveredBg
            EN_GARANTIA -> StatusWarrantyBg
            NO_REPARADO -> StatusNotRepairedBg
            CANCELADO -> StatusCancelledBg
        }

    val contentColor: Color
        get() = when (this) {
            RECIBIDO -> StatusReceivedFg
            DIAGNOSTICO -> StatusDiagFg
            PRESUPUESTADO -> StatusDiagnosedFg
            ESPERANDO_REPUESTO -> StatusWaitingPartsFg
            EN_REPARACION -> StatusInRepairFg
            LISTO -> StatusReadyFg
            ENTREGADO -> StatusDeliveredFg
            EN_GARANTIA -> StatusWarrantyFg
            NO_REPARADO -> StatusNotRepairedFg
            CANCELADO -> StatusCancelledFg
        }

    val isActiveInWorkshop: Boolean
        get() = this in listOf(RECIBIDO, DIAGNOSTICO, PRESUPUESTADO, ESPERANDO_REPUESTO, EN_REPARACION, LISTO, EN_GARANTIA)
}

enum class PaymentStatus(val label: String) {
    PENDIENTE("Pendiente"),
    ABONADO("Con Abono"),
    PAGADO("Pagado Total")
}
