package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Devices
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Client
import com.example.model.RepairOrder

@Composable
fun ClientsScreen(
    clients: List<Client>,
    orders: List<RepairOrder>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onClientClick: (Client) -> Unit,
    onCallClient: (String) -> Unit,
    onWhatsAppClient: (String, String) -> Unit,
    onAddClientClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("clients_screen")
    ) {
        // Search & Add Client Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Buscar cliente por nombre o teléfono...", fontSize = 13.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("client_search_input")
            )
        }

        if (clients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No hay clientes registrados" else "No se encontraron clientes",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Registra clientes para gestionar sus datos y equipos.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = onAddClientClick,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("empty_add_client_button")
                    ) {
                        Icon(Icons.Default.PersonAdd, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Registrar Primer Cliente")
                    }
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(clients, key = { it.id }) { client ->
                    val clientOrdersCount = orders.count {
                        it.clientId == client.id || it.clientName.equals(client.name, ignoreCase = true)
                    }
                    val activeOrdersCount = orders.count {
                        (it.clientId == client.id || it.clientName.equals(client.name, ignoreCase = true)) &&
                                it.status.isActiveInWorkshop
                    }

                    ClientCard(
                        client = client,
                        totalOrders = clientOrdersCount,
                        activeOrders = activeOrdersCount,
                        onClick = { onClientClick(client) },
                        onCall = { onCallClient(client.phone) },
                        onWhatsApp = {
                            onWhatsAppClient(
                                client.phone,
                                "Hola ${client.name}, le saludamos desde Taller ElectroFix."
                            )
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun ClientCard(
    client: Client,
    totalOrders: Int,
    activeOrders: Int,
    onClick: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .testTag("client_card_${client.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = CircleShape,
                modifier = Modifier.size(46.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = client.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = client.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = client.phone,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "$totalOrders equipo(s)",
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (activeOrders > 0) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "$activeOrders en taller",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCall,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Llamar",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onWhatsApp,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "WhatsApp",
                        tint = Color(0xFF15803D),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
