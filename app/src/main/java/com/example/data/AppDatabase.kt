package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.model.Client
import com.example.model.DeviceCategory
import com.example.model.InventoryItem
import com.example.model.InventoryType
import com.example.model.PaymentStatus
import com.example.model.RepairOrder
import com.example.model.RepairStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [RepairOrder::class, Client::class, InventoryItem::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun repairOrderDao(): RepairOrderDao
    abstract fun clientDao(): ClientDao
    abstract fun inventoryDao(): InventoryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "electrofix_database"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(AppDatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class AppDatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database.clientDao(), database.repairOrderDao(), database.inventoryDao())
                    }
                }
            }
        }

        suspend fun populateInitialData(clientDao: ClientDao, orderDao: RepairOrderDao, inventoryDao: InventoryDao) {
            val now = System.currentTimeMillis()
            val day = 86400000L

            val client1 = Client(
                id = 1,
                name = "Carlos Mendoza",
                phone = "+34 612 345 678",
                email = "carlos.mendoza@email.com",
                address = "Calle Gran Vía 45, 3B",
                notes = "Cliente frecuente. Prefiere contacto por WhatsApp."
            )
            val client2 = Client(
                id = 2,
                name = "Laura Martínez",
                phone = "+34 678 901 234",
                email = "laura.mtz@email.com",
                address = "Av. Diagonal 230",
                notes = "Diseñadora gráfica, requiere equipo con urgencia."
            )
            val client3 = Client(
                id = 3,
                name = "Alejandro Gómez",
                phone = "+34 654 321 098",
                email = "alex.gomez99@email.com",
                address = "Plaza Mayor 12",
                notes = "Gamer y creador de contenido."
            )
            val client4 = Client(
                id = 4,
                name = "Sofía Valenzuela",
                phone = "+34 633 112 233",
                email = "sofia.valenzuela@email.com",
                address = "Calle Alcalá 88",
                notes = "Empresa de diseño local."
            )

            clientDao.insertClients(listOf(client1, client2, client3, client4))

            val order1 = RepairOrder(
                id = 1,
                orderNumber = "ORD-1001",
                clientId = 1,
                clientName = client1.name,
                clientPhone = client1.phone,
                clientEmail = client1.email,
                deviceCategory = DeviceCategory.SMART_TV,
                deviceBrand = "Samsung",
                deviceModel = "Crystal UHD 55\" 4K (TU8000)",
                serialNumber = "SAM55TU8000X9821",
                accessoriesIncluded = "Control remoto original, Cable de poder",
                reportedIssue = "La pantalla se escucha pero no da imagen. Parpadea un destello al encender y se queda negra.",
                technicalDiagnosis = "Falla confirmada en regletas LED de retroiluminación (Backlight). Fuente de poder y tarjeta Mainboard en buen estado.",
                workPerformed = "Reemplazo de kit completo de tiras LED de aluminio y calibración de corriente del inverter.",
                status = RepairStatus.LISTO,
                paymentStatus = PaymentStatus.ABONADO,
                estimatedCost = 135.0,
                partsCost = 45.0,
                laborCost = 90.0,
                depositPaid = 50.0,
                totalAmount = 135.0,
                entryDate = now - (3 * day),
                estimatedDeliveryDate = now,
                completedDate = now - (2 * 3600000L),
                warrantyDays = 90,
                isUrgent = false,
                statusNote = "Reparación concluida con éxito. 6 horas de prueba continua aprobadas. Listo para entrega."
            )

            val order2 = RepairOrder(
                id = 2,
                orderNumber = "ORD-1002",
                clientId = 3,
                clientName = client3.name,
                clientPhone = client3.phone,
                clientEmail = client3.email,
                deviceCategory = DeviceCategory.CONSOLE,
                deviceBrand = "Sony PlayStation",
                deviceModel = "PlayStation 5 Digital Edition",
                serialNumber = "CFI-1115B-2938102",
                accessoriesIncluded = "1 Mando DualSense Blanco, Cable HDMI 2.1",
                reportedIssue = "El puerto HDMI se soltó tras un tirón de cable. No transmite señal de video al monitor (Luz blanca fija en consola).",
                technicalDiagnosis = "Pines internos del puerto HDMI quebrados y pistas levantadas. Filtros ESD intactos.",
                workPerformed = "Micro-soldadura de nuevo puerto HDMI 2.1 reforzado y reconstrucción de 2 pistas.",
                status = RepairStatus.EN_REPARACION,
                paymentStatus = PaymentStatus.PENDIENTE,
                estimatedCost = 85.0,
                partsCost = 15.0,
                laborCost = 70.0,
                depositPaid = 0.0,
                totalAmount = 85.0,
                entryDate = now - (1 * day),
                estimatedDeliveryDate = now + (1 * day),
                warrantyDays = 60,
                isUrgent = true,
                statusNote = "Puerto HDMI nuevo montado. En proceso de verificación en osciloscopio y pruebas térmicas."
            )

            val order3 = RepairOrder(
                id = 3,
                orderNumber = "ORD-1003",
                clientId = 2,
                clientName = client2.name,
                clientPhone = client2.phone,
                clientEmail = client2.email,
                deviceCategory = DeviceCategory.COMPUTER,
                deviceBrand = "Apple MacBook",
                deviceModel = "MacBook Pro 14\" M1 Pro (2021)",
                serialNumber = "C02G901AMD6R",
                accessoriesIncluded = "Cargador MagSafe 3 67W original con cable trenzado",
                reportedIssue = "Derramó café sobre el teclado. El equipo se apagó de inmediato y no enciende con el cargador.",
                technicalDiagnosis = "Sulfatación en línea de 3.3V Always y corto en condensadores de entrada USB-C.",
                workPerformed = "Lavado ultrasónico de placa madre, remoción de corrosión y reemplazo de 3 condensadores cerámicos.",
                status = RepairStatus.DIAGNOSTICO,
                paymentStatus = PaymentStatus.PENDIENTE,
                estimatedCost = 190.0,
                partsCost = 30.0,
                laborCost = 160.0,
                depositPaid = 0.0,
                totalAmount = 190.0,
                entryDate = now - (6 * 3600000L),
                estimatedDeliveryDate = now + (2 * day),
                warrantyDays = 90,
                isUrgent = true,
                statusNote = "Placa en baño ultrasónico. Evaluando integridad de la memoria NAND y procesador M1."
            )

            val order4 = RepairOrder(
                id = 4,
                orderNumber = "ORD-1004",
                clientId = 4,
                clientName = client4.name,
                clientPhone = client4.phone,
                clientEmail = client4.email,
                deviceCategory = DeviceCategory.MOBILE,
                deviceBrand = "Apple iPhone",
                deviceModel = "iPhone 14 Pro 256GB",
                serialNumber = "F2LX9201MD",
                accessoriesIncluded = "Funda de silicona transparente",
                reportedIssue = "Caída fuerte: Cristal frontal roto con líneas verdes verticales. Táctil responde parcialmente.",
                technicalDiagnosis = "Panel OLED interno fracturado. Marco y chasis en buen estado.",
                workPerformed = "",
                status = RepairStatus.ESPERANDO_REPUESTO,
                paymentStatus = PaymentStatus.ABONADO,
                estimatedCost = 210.0,
                partsCost = 140.0,
                laborCost = 70.0,
                depositPaid = 100.0,
                totalAmount = 210.0,
                entryDate = now - (2 * day),
                estimatedDeliveryDate = now + (3 * day),
                warrantyDays = 90,
                isUrgent = false,
                statusNote = "Módulo de pantalla OLED original pedido a proveedor oficial. Arribo previsto en 48hs."
            )

            val order5 = RepairOrder(
                id = 5,
                orderNumber = "ORD-1000",
                clientId = 1,
                clientName = client1.name,
                clientPhone = client1.phone,
                clientEmail = client1.email,
                deviceCategory = DeviceCategory.AUDIO_OTHER,
                deviceBrand = "JBL",
                deviceModel = "Boombox 2 Bluetooth",
                serialNumber = "JBL-BB2-89410",
                accessoriesIncluded = "Cargador 24V",
                reportedIssue = "No carga la batería. Solo enciende si está conectado a la corriente.",
                technicalDiagnosis = "Batería de polímero de litio degradada y pin de carga con juego.",
                workPerformed = "Cambio de pack de batería 10.000mAh y resoldado de conector de carga.",
                status = RepairStatus.ENTREGADO,
                paymentStatus = PaymentStatus.PAGADO,
                estimatedCost = 75.0,
                partsCost = 35.0,
                laborCost = 40.0,
                depositPaid = 75.0,
                totalAmount = 75.0,
                entryDate = now - (10 * day),
                estimatedDeliveryDate = now - (6 * day),
                completedDate = now - (6 * day),
                deliveredDate = now - (5 * day),
                warrantyDays = 60,
                isUrgent = false,
                statusNote = "Entregado a entera satisfacción del cliente. Pago completo recibido."
            )

            orderDao.insertOrders(listOf(order1, order2, order3, order4, order5))

            val inv1 = InventoryItem(
                id = 1,
                name = "Pantalla OLED iPhone 13 / 13 Pro (Calidad Original)",
                type = InventoryType.REPUESTO,
                category = "Pantallas",
                sku = "REP-PNT-013",
                stock = 4,
                minStock = 2,
                costPrice = 45.0,
                salePrice = 85.0,
                compatibleModels = "iPhone 13, iPhone 13 Pro",
                location = "Cajón A-1",
                notes = "Incluye adhesivo de estanqueidad impermeable."
            )
            val inv2 = InventoryItem(
                id = 2,
                name = "Batería Samsung Galaxy S21 4000mAh",
                type = InventoryType.REPUESTO,
                category = "Baterías",
                sku = "REP-BAT-S21",
                stock = 6,
                minStock = 2,
                costPrice = 18.0,
                salePrice = 40.0,
                compatibleModels = "Galaxy S21 5G (G991B)",
                location = "Cajón B-3",
                notes = "Celdas de alta densidad con chip de seguridad."
            )
            val inv3 = InventoryItem(
                id = 3,
                name = "Disco SSD Kingston NV2 1TB NVMe M.2",
                type = InventoryType.REPUESTO,
                category = "Almacenamiento",
                sku = "REP-SSD-1TB",
                stock = 5,
                minStock = 2,
                costPrice = 52.0,
                salePrice = 80.0,
                compatibleModels = "Laptops y PCs con zócalo M.2 PCIe 4.0",
                location = "Estante C-2",
                notes = "Lectura hasta 3500MB/s. 3 años de garantía."
            )
            val inv4 = InventoryItem(
                id = 4,
                name = "Módulo de Carga USB-C Xiaomi Redmi Note 11",
                type = InventoryType.REPUESTO,
                category = "Pines de Carga",
                sku = "REP-PIN-RN11",
                stock = 1,
                minStock = 3,
                costPrice = 6.0,
                salePrice = 22.0,
                compatibleModels = "Redmi Note 11, Note 11S",
                location = "Cajón A-4",
                notes = "Stock bajo - Requerir reposición urgente."
            )
            val inv5 = InventoryItem(
                id = 5,
                name = "Pasta Térmica Arctic MX-4 4g",
                type = InventoryType.REPUESTO,
                category = "Insumos",
                sku = "REP-INS-MX4",
                stock = 8,
                minStock = 2,
                costPrice = 7.0,
                salePrice = 15.0,
                compatibleModels = "CPUs, GPUs, Consolas PS4/PS5/Xbox",
                location = "Mesa Técnica 1",
                notes = "Conductividad de 8.5 W/mK."
            )
            val inv6 = InventoryItem(
                id = 6,
                name = "Laptop Lenovo ThinkPad T480s i7 16GB 512GB SSD",
                type = InventoryType.EQUIPO,
                category = "Laptops",
                sku = "EQP-LEN-T480",
                stock = 2,
                minStock = 1,
                costPrice = 280.0,
                salePrice = 420.0,
                compatibleModels = "Reacondicionado Grado A con Cargador Original",
                location = "Vitrina Principal",
                notes = "Batería al 94%, Windows 11 Pro instalado con licencia."
            )
            val inv7 = InventoryItem(
                id = 7,
                name = "iPhone 11 128GB Negro (Seminuevo)",
                type = InventoryType.EQUIPO,
                category = "Smartphones",
                sku = "EQP-IPH-11BK",
                stock = 3,
                minStock = 1,
                costPrice = 210.0,
                salePrice = 320.0,
                compatibleModels = "Libre para cualquier operador",
                location = "Vitrina Móviles",
                notes = "Condición de batería 92%. Incluye cable y cargador."
            )
            val inv8 = InventoryItem(
                id = 8,
                name = "Consola Nintendo Switch OLED Neon (Completa)",
                type = InventoryType.EQUIPO,
                category = "Consolas",
                sku = "EQP-NSW-OLED",
                stock = 1,
                minStock = 1,
                costPrice = 220.0,
                salePrice = 310.0,
                compatibleModels = "Completa en caja",
                location = "Vitrina Consolas",
                notes = "Con dock, cargador original y grip para mandos."
            )

            inventoryDao.insertItems(listOf(inv1, inv2, inv3, inv4, inv5, inv6, inv7, inv8))
        }
    }
}
