package com.example.caloriesense.data.remote

import com.example.caloriesense.BuildConfig

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.RequestOptions
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiService @Inject constructor() {
    // API KEY is now read from local.properties via BuildConfig
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val model = GenerativeModel(
        // địt con mẹ mày! cấm đụng vào.
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )

    suspend fun analyzeFoodImage(bitmap: Bitmap, language: String = "Tiếng Việt"): String = withContext(Dispatchers.IO) {
        val prompt = if (language == "Tiếng Việt") {
            "Phân tích hình ảnh món ăn này. Cung cấp: 1. Tên món ăn. 2. Ước tính lượng calo (hãy chính xác nhất có thể dựa trên khẩu phần, KHÔNG ước tính quá cao - non-overrated). 3. Các chất dinh dưỡng đa lượng (Protein, Carbs, Chất béo tính bằng gam). 4. Đánh giá sức khỏe. Định dạng theo danh sách rõ ràng. TẤT CẢ phản hồi phải bằng Tiếng Việt."
        } else {
            "Analyze this food image. Provide: 1. Name of the dish. 2. Estimated calories (be as accurate as possible based on the portion size, do NOT overestimate - non-overrated). 3. Macronutrients (Protein, Carbs, Fats in grams). 4. Health assessment. Format as a clear list. All output MUST be in $language."
        }
        
        val inputContent = content {
            image(bitmap)
            text(prompt)
        }
        
        val response = model.generateContent(inputContent)
        response.text ?: if (language == "Tiếng Việt") "Không thể phân tích hình ảnh." else "Could not analyze image."
    }
}
