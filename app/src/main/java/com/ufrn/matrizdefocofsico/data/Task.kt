package com.ufrn.matrizdefocofsico.data

import java.util.UUID

data class Task(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val quadrant: Quadrant
)
