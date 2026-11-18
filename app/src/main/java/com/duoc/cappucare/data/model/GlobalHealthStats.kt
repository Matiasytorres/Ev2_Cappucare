package com.duoc.cappucare.data.model

data class GlobalHealthStats(
    val cases: Long = 0,
    val deaths: Long = 0,
    val recovered: Long = 0,
    val active: Long = 0,
    val updated: Long = 0
)