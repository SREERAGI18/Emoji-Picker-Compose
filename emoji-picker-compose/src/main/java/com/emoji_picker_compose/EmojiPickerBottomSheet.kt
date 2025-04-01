package com.emoji_picker_compose

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalBottomSheetDefaults
import androidx.compose.material3.ModalBottomSheetProperties
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.use
import com.emoji_picker_compose.utils.CSVReaderUtil
import com.emoji_picker_compose.utils.Extensions.noRippleClick
import com.emoji_picker_compose.utils.UnicodeRenderableManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmojiPickerBottomSheet(
    modifier: Modifier = Modifier,
    onEmojiPicked:(String) -> Unit,
    onDismiss:() -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(),
    sheetMaxWidth: Dp = BottomSheetDefaults.SheetMaxWidth,
    shape: Shape = BottomSheetDefaults.ExpandedShape,
    containerColor: Color = BottomSheetDefaults.ContainerColor,
    contentColor: Color = contentColorFor(containerColor),
    tonalElevation: Dp = 0.dp,
    scrimColor: Color = BottomSheetDefaults.ScrimColor,
    dragHandle: @Composable (() -> Unit)? = { BottomSheetDefaults.DragHandle() },
    contentWindowInsets: @Composable () -> WindowInsets = { BottomSheetDefaults.windowInsets },
    properties: ModalBottomSheetProperties = ModalBottomSheetDefaults.properties,
) {
    val context = LocalContext.current
    val emojiCategories = getEmojiCategories()

    var selectedCategoryInd by remember {
        mutableIntStateOf(0)
    }
    var emojis = remember {
        mutableStateListOf<String>()
    }
    val pagerState = rememberPagerState(
        pageCount = { emojiCategories.size },
        initialPage = selectedCategoryInd
    )

    LaunchedEffect(pagerState.currentPage) {
        selectedCategoryInd = pagerState.currentPage
    }

    LaunchedEffect(selectedCategoryInd) {
        emojis.clear()
        emojis.addAll(getEmojisByCategory(context, emojiCategories[selectedCategoryInd].resId))
        pagerState.scrollToPage(selectedCategoryInd)
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = {
            onDismiss.invoke()
        },
        sheetState = sheetState,
        sheetMaxWidth = sheetMaxWidth,
        shape = shape,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                itemsIndexed(emojiCategories) { index, emojiCategory ->
                    ItemCategoryHeader(
                        emojiCategory = emojiCategory,
                        isSelected = selectedCategoryInd == index,
                        onCategoryClicked = {
                            selectedCategoryInd = index
                        }
                    )
                }
            }
            HorizontalPager(
                state = pagerState
            ) {
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 28.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp),
                    contentPadding = PaddingValues(
                        horizontal = 20.dp,
                        vertical = 10.dp
                    ),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    itemsIndexed(emojis) { index, emoji ->
                        Text(
                            text = emoji,
                            style = TextStyle.Default.copy(
                                fontSize = 20.sp
                            ),
                            modifier = Modifier
                                .padding(4.dp)
                                .noRippleClick {
                                    onEmojiPicked.invoke(emoji)
                                    onDismiss.invoke()
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ItemCategoryHeader(
    emojiCategory: EmojiCategory,
    isSelected: Boolean,
    onCategoryClicked:() -> Unit
) {
    Column(
        modifier = Modifier
            .noRippleClick {
                onCategoryClicked.invoke()
            }
    ) {
        Icon(
            painter = painterResource(emojiCategory.categoryIcon),
            contentDescription = ""
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .height(2.dp)
                .width(24.dp)
                .background(
                    if(isSelected) {
                        Color.LightGray
                    } else {
                        Color.Transparent
                    }
                )
        )
    }
}

@Composable
fun getEmojiCategories(): List<EmojiCategory> {

    val context = LocalContext.current
    val emojiCategoryList = mutableListOf<EmojiCategory>()

    val resources = if (UnicodeRenderableManager.isEmoji12Supported())
            R.array.emoji_by_category_raw_resources_gender_inclusive
        else R.array.emoji_by_category_raw_resources

    val categoryNames = stringArrayResource(R.array.category_names)
    val categoryHeaderIconIds =
        context.resources.obtainTypedArray(R.array.emoji_categories_icons).use { typedArray ->
            IntArray(typedArray.length()) {
                typedArray.getResourceId(it, 0)
            }
        }

    val csvResIds = context.resources.obtainTypedArray(resources).use { typedArray ->
        IntArray(typedArray.length()) {
            typedArray.getResourceId(it, 0)
        }
    }

    csvResIds.forEachIndexed { index, resId ->
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
