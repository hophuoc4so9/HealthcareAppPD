package com.example.healthcareapppd.utils
import android.content.Context
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder
import com.google.mediapipe.tasks.text.textembedder.TextEmbedder.TextEmbedderOptions
import kotlin.math.sqrt

class SemanticSearchEngine(context: Context) {
    private var textEmbedder: TextEmbedder? = null

    init {
        // Cấu hình để load model từ thư mục assets
        val baseOptions = BaseOptions.builder()
            .setModelAssetPath("models/universal_sentence_encoder.tflite")
            .build()

        val options = TextEmbedderOptions.builder()
            .setBaseOptions(baseOptions)
            .build()

        textEmbedder = TextEmbedder.createFromOptions(context, options)
    }

    // Hàm chuyển Text thành Vector (Embedding)
    fun encode(text: String): FloatArray? {
        return try {
            val result = textEmbedder?.embed(text)
            result?.embeddingResult()?.embeddings()?.firstOrNull()?.floatEmbedding()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Hàm tính độ tương đồng
    fun cosineSimilarity(vec1: FloatArray, vec2: FloatArray): Float {
        if (vec1.size != vec2.size) return 0f

        var dotProduct = 0.0f
        var normA = 0.0f
        var normB = 0.0f

        for (i in vec1.indices) {
            dotProduct += vec1[i] * vec2[i]
            normA += vec1[i] * vec1[i]
            normB += vec2[i] * vec2[i]
        }

        return if (normA > 0 && normB > 0) {
            dotProduct / (sqrt(normA) * sqrt(normB)).toFloat()
        } else {
            0f
        }
    }

    fun close() {
        textEmbedder?.close()
    }
}