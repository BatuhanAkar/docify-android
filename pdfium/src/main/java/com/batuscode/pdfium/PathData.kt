package com.batuscode.pdfium

data class PathData(
    val id: String,
    val mcolor: Int,
    val path: List<OffsetWrapper>,
    val thickness: Float = 10f
)