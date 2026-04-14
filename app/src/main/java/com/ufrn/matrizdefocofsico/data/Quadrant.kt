package com.ufrn.matrizdefocofsico.data

enum class Quadrant(
    val label: String,
    val subtitle: String,
    val backgroundColorInt: Int,
    val bubbleColorInt: Int
) {
    FAZER_AGORA(
        label = "Fazer Agora",
        subtitle = "Urgente + Importante",
        backgroundColorInt = 0xFFFF8A80.toInt(),
        bubbleColorInt = 0xFFFF5252.toInt()
    ),
    AGENDAR(
        label = "Agendar",
        subtitle = "Importante + Não Urgente",
        backgroundColorInt = 0xFF81C784.toInt(),
        bubbleColorInt = 0xFF43A047.toInt()
    ),
    DELEGAR(
        label = "Delegar",
        subtitle = "Urgente + Não Importante",
        backgroundColorInt = 0xFFFFD54F.toInt(),
        bubbleColorInt = 0xFFFFB300.toInt()
    ),
    ELIMINAR(
        label = "Eliminar",
        subtitle = "Não Urgente + Não Importante",
        backgroundColorInt = 0xFF90CAF9.toInt(),
        bubbleColorInt = 0xFF1E88E5.toInt()
    )
}
