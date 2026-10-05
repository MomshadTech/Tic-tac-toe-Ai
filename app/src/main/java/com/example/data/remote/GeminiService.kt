package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.logic.TicTacToeEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class GeminiService {
  private val client = OkHttpClient.Builder()
    .connectTimeout(60, TimeUnit.SECONDS)
    .readTimeout(60, TimeUnit.SECONDS)
    .writeTimeout(60, TimeUnit.SECONDS)
    .build()

  private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

  data class AiMoveResult(
    val move: Int,
    val commentary: String,
    val isFromGemini: Boolean
  )

  suspend fun getAiMove(board: List<String>, availableMoves: List<Int>): AiMoveResult = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY

    // Fallback if API key is blank or placeholder
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      Log.d("GeminiService", "Gemini API key is not set or is placeholder, using strategic fallback AI")
      val fallbackMove = TicTacToeEngine.getBestMove(board)
      return@withContext AiMoveResult(
        move = fallbackMove,
        commentary = getRandomFallbackCommentary(fallbackMove, board),
        isFromGemini = false
      )
    }

    val boardDescription = TicTacToeEngine.formatBoardForPrompt(board)
    val prompt = """
      You are playing Tic-Tac-Toe as 'O' against a human player 'X'.
      Board layout (indices 0 to 8):
      0 | 1 | 2
      ---------
      3 | 4 | 5
      ---------
      6 | 7 | 8

      Current board state:
      $boardDescription

      Available empty indices to choose from: [${availableMoves.joinToString(", ")}]

      Instructions:
      1. Choose the best strategic move from the available indices.
      2. Write a short, engaging, witty commentary (1-2 sentences, max 15 words) explaining your tactical thinking or playfully teasing the opponent.
      3. Return ONLY a valid JSON object with format:
      {"move": <integer index from available empty indices>, "commentary": "<short witty string>"}
    """.trimIndent()

    try {
      val responseText = callGeminiApi(apiKey, prompt)
      val (move, commentary) = parseMoveResponse(responseText, availableMoves)
      if (move in availableMoves) {
        return@withContext AiMoveResult(move = move, commentary = commentary, isFromGemini = true)
      } else {
        // If Gemini returned an invalid move index, fall back to best move
        val fallbackMove = TicTacToeEngine.getBestMove(board)
        return@withContext AiMoveResult(
          move = fallbackMove,
          commentary = commentary.ifBlank { "Calculated square $fallbackMove as optimal!" },
          isFromGemini = true
        )
      }
    } catch (e: Exception) {
      Log.e("GeminiService", "Error calling Gemini API: ${e.message}", e)
      val fallbackMove = TicTacToeEngine.getBestMove(board)
      return@withContext AiMoveResult(
        move = fallbackMove,
        commentary = getRandomFallbackCommentary(fallbackMove, board),
        isFromGemini = false
      )
    }
  }

  suspend fun getVerdictCommentary(
    winner: String, // "HUMAN", "GEMINI", "DRAW"
    board: List<String>,
    roundNumber: Int
  ): String = withContext(Dispatchers.IO) {
    val apiKey = BuildConfig.GEMINI_API_KEY
    if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
      return@withContext when (winner) {
        "HUMAN" -> "Impressive strategic play! You claimed round $roundNumber."
        "GEMINI" -> "Three in a row! Tactical victory secured in round $roundNumber."
        else -> "A fiercely contested stalemate! Balanced defense on both sides."
      }
    }

    val outcomeText = when (winner) {
      "HUMAN" -> "The human player ('X') just defeated you ('O'). Concede defeat gracefully with a witty compliment or humorous excuse."
      "GEMINI" -> "You ('O') won against the human player ('X'). Declare victory with playful, sci-fi confidence without being rude."
      else -> "The game ended in a draw/tie. React to this evenly matched battle with a clever observation."
    }

    val prompt = """
      You are Gemini AI reflecting on the conclusion of Round $roundNumber in Tic-Tac-Toe.
      Outcome: $outcomeText
      Final Board: ${TicTacToeEngine.formatBoardForPrompt(board)}

      Provide a 1-2 sentence witty, fun verdict commentary for the post-game summary card.
      Do not include JSON formatting or quotes; just return the text.
    """.trimIndent()

    try {
      val text = callGeminiApi(apiKey, prompt)
      text.trim().trim('"')
    } catch (e: Exception) {
      Log.e("GeminiService", "Error getting verdict commentary: ${e.message}", e)
      when (winner) {
        "HUMAN" -> "Impressive foresight! You got the better of my neural circuits this round."
        "GEMINI" -> "Checkmate on the 3x3 grid! The AI takes round $roundNumber."
        else -> "A balanced standoff. Neither player gave an inch!"
      }
    }
  }

  private fun callGeminiApi(apiKey: String, prompt: String): String {
    val models = listOf("gemini-3.5-flash", "gemini-2.5-flash")
    var lastException: Exception? = null

    for (model in models) {
      try {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
        val requestJson = JSONObject().apply {
          val contentsArray = JSONArray().apply {
            val contentObj = JSONObject().apply {
              val partsArray = JSONArray().apply {
                put(JSONObject().apply {
                  put("text", prompt)
                })
              }
              put("parts", partsArray)
            }
            put(contentObj)
          }
          put("contents", contentsArray)

          val genConfig = JSONObject().apply {
            put("temperature", 0.3)
          }
          put("generationConfig", genConfig)
        }

        val requestBody = requestJson.toString().toRequestBody(jsonMediaType)
        val request = Request.Builder()
          .url(url)
          .post(requestBody)
          .build()

        val response = client.newCall(request).execute()
        val responseBodyString = response.body?.string() ?: ""

        if (!response.isSuccessful) {
          Log.w("GeminiService", "Model $model returned code ${response.code}: $responseBodyString")
          continue
        }

        val responseObj = JSONObject(responseBodyString)
        val candidates = responseObj.optJSONArray("candidates")
        val candidate = candidates?.optJSONObject(0)
        val content = candidate?.optJSONObject("content")
        val parts = content?.optJSONArray("parts")
        val part = parts?.optJSONObject(0)
        val text = part?.optString("text", "") ?: ""
        if (text.isNotBlank()) {
          return text
        }
      } catch (e: Exception) {
        lastException = e
      }
    }
    throw lastException ?: RuntimeException("Failed to call Gemini API")
  }

  private fun parseMoveResponse(rawText: String, availableMoves: List<Int>): Pair<Int, String> {
    // Attempt JSON parse
    try {
      val cleanJson = extractJson(rawText)
      val jsonObj = JSONObject(cleanJson)
      val move = jsonObj.optInt("move", -1)
      val commentary = jsonObj.optString("commentary", "")
      if (move in availableMoves) {
        return Pair(move, commentary)
      }
    } catch (_: Exception) {
    }

    // Fallback regex if full JSON parse failed
    val moveRegex = Regex(""""move"\s*:\s*(\d+)""")
    val match = moveRegex.find(rawText)
    val parsedMove = match?.groupValues?.getOrNull(1)?.toIntOrNull()
    if (parsedMove != null && parsedMove in availableMoves) {
      return Pair(parsedMove, "Calculating strategic positions...")
    }

    // Fallback to first available
    return Pair(availableMoves.firstOrNull() ?: -1, rawText.take(50))
  }

  private fun extractJson(text: String): String {
    val startIndex = text.indexOf('{')
    val endIndex = text.lastIndexOf('}')
    return if (startIndex != -1 && endIndex != -1 && endIndex > startIndex) {
      text.substring(startIndex, endIndex + 1)
    } else {
      text
    }
  }

  private fun getRandomFallbackCommentary(move: Int, board: List<String>): String {
    val options = listOf(
      "Analyzing grid vectors... Square $move looks optimal.",
      "Countering your placement with a move to square $move.",
      "Controlling space: taking square $move!",
      "Calculated defense initiated at position $move.",
      "Setting up a tactical sequence at position $move."
    )
    return options.random()
  }
}
