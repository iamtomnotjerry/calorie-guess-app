package com.example.caloriesense.data.remote

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
    // API KEY provided by user in screenshot
    private val apiKey = "AIzaSyCB1MoOpbFnzU5-u_Tj_VZJTk0mSUob00o"
    
    private val model = GenerativeModel(
        modelName = "gemini-3-flash-preview",
        apiKey = apiKey
    )

    suspend fun analyzeFoodImage(bitmap: Bitmap): String = withContext(Dispatchers.IO) {
        val prompt = "Analyze this food image. Provide: 1. Name of the dish. 2. Estimated calories. 3. Macronutrients (Protein, Carbs, Fats in grams). 4. Health assessment. Format as a clear list."
        
        val inputContent = content {
            image(bitmap)
            text(prompt)
        }
        
        val response = model.generateContent(inputContent)
        response.text ?: "Could not analyze image."
    }
}
