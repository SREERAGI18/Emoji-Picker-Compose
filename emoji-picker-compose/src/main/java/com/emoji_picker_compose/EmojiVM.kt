package com.emoji_picker_compose

import android.content.Context
import android.content.res.Resources
import androidx.annotation.ArrayRes
import androidx.annotation.RawRes
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.emoji_picker_compose.utils.CSVReaderUtil
import com.emoji_picker_compose.utils.UnicodeRenderableManager

class EmojiVM: ViewModel() {

    val emojis = mutableStateListOf<EmojiData>()

    fun getEmojis(context: Context): List<EmojiCategory> {
        val emojiCategoryList = mutableListOf<EmojiCategory>()

        val resources = context.resources

        val rawResourcesId = if (UnicodeRenderableManager.isEmoji12Supported())
            R.array.emoji_by_category_raw_resources_gender_inclusive
        else R.array.emoji_by_category_raw_resources

        val categoryNames = resources.getStringArray(R.array.category_names)

        val categoryHeaderIconIds = context.resources.getResourceIdArray(R.array.emoji_categories_icons)
        val csvResIds = context.resources.getResourceIdArray(rawResourcesId)

        csvResIds.forEachIndexed { index, resId ->
            val emojisByCategory = getEmojisByCategory(context, resId)
            emojis.add(
                EmojiData(
                    categoryName = categoryNames[index],
                    emojis = emojisByCategory
                )
            )
            emojiCategoryList.add(
                EmojiCategory(
                    resId = resId,
                    categoryIcon = categoryHeaderIconIds[index],
                    categoryName = categoryNames[index]
                )
            )
        }

        return emojiCategoryList
    }

    fun getEmojisByCategory(context: Context, @RawRes emojiCsvRes: Int): List<String> {
        return CSVReaderUtil.readCSV(context, emojiCsvRes)
    }

    fun Resources.getResourceIdArray(@ArrayRes arrayRes: Int): IntArray {
        val ta = obtainTypedArray(arrayRes)
        val ids = IntArray(ta.length()) { ta.getResourceId(it, 0) }
        ta.recycle()
        return ids
    }

}