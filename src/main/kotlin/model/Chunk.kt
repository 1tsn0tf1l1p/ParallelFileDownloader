package model

data class Chunk(
    val id: Int,
    val startByte: Long,
    val endByte: Long
)