package com.mikyou.plugins.image.slimming.ui.model

import java.io.Serializable

data class ImageSlimmingModel(val inputDir: String, val outputDir: String, val filePrefix: String) : Serializable