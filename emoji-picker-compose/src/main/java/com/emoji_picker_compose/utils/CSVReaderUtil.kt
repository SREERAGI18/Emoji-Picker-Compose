package com.emoji_picker_compose.utils

import android.content.Context

object CSVReaderUtil {
    fun readCSV(context: Context, rawResourceId: Int): List<String> {
        val data = mutableListOf<String>()
        try {
            val emojis = context.resources
                .openRawResource(rawResourceId)
                .bufferedReader()
                .useLines { it.toList() }
                .map { filterRenderableEmojis(it.split(",")) }
                .filter { it.isNotEmpty() }
                .map { it.first() }
            data.addAll(emojis)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return data
    }

    /**
     * To eliminate 'Tofu' (the fallback glyph when an emoji is not renderable), check the
     * renderability of emojis and keep only when they are renderable on the current device.
     */
    private fun filterRenderableEmojis(emojiList: List<String>) =
        emojiList.filter { UnicodeRenderableManager.isEmojiRenderable(it) }.toList()
}