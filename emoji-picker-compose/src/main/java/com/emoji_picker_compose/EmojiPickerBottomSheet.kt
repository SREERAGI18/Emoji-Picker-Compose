package com.emoji_picker_compose

import android.content.Context
import androidx.annotation.RawRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.res.use
import androidx.lifecycle.viewmodel.compose.viewModel
import com.emoji_picker_compose.utils.CSVReaderUtil
import com.emoji_picker_compose.utils.Extensions.noRippleClick
import com.emoji_picker_compose.utils.UnicodeRenderableManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.ceil

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
    val viewModel = viewModel<EmojiVM>()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val emojiCategories = remember {
        mutableStateListOf<EmojiCategory>()
    }

    var selectedCategoryInd by remember {
        mutableIntStateOf(0)
    }
    val emojis = remember {
        viewModel.emojis
    }

    val lazyListState = rememberLazyListState()

    LaunchedEffect(true) {
        emojiCategories.addAll(viewModel.getEmojis(context))
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .collect { firstVisibleIndex ->
                if (firstVisibleIndex != selectedCategoryInd) {
                    selectedCategoryInd = firstVisibleIndex
                }
            }
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
        containerColor = containerColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        contentWindowInsets = contentWindowInsets,
        properties = properties
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                emojiCategories.forEachIndexed { index, emojiCategory ->
                    ItemCategoryHeader(
                        modifier = Modifier.weight(1f),
                        emojiCategory = emojiCategory,
                        isSelected = selectedCategoryInd == index,
                        onCategoryClicked = {
                            selectedCategoryInd = index
                            coroutineScope.launch(Dispatchers.Main) {
                                lazyListState.scrollToItem(selectedCategoryInd)
                            }
                        },
                    )
                }
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentPadding = PaddingValues(
                    horizontal = 10.dp,
                ),
                state = lazyListState
            ) {
                itemsIndexed(
                    items = emojis,
                    key = { index, emojiData ->
                        emojiData.categoryName
                    }
                ) { index, emojiData ->
                    ItemEmoji(
                        onEmojiPicked = {
                            onEmojiPicked.invoke(it)
                            onDismiss.invoke()
                        },
                        emojiData = emojiData
                    )
                }
            }
        }
    }
}

@Composable
fun ItemEmoji(
    emojiData: EmojiData,
    onEmojiPicked: (String) -> Unit
) {

    val emojis = emojiData.emojis

    BoxWithConstraints {
        val availableWidthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val minItemSizePx = with(LocalDensity.current) { 28.dp.toPx() }

        val columns = (availableWidthPx / minItemSizePx).toInt().coerceAtLeast(1)

        val paddingValues = PaddingValues(
            top = 10.dp,
            bottom = 20.dp,
            start = 0.dp,
            end = 0.dp
        )

        val rows = ceil(emojis.size / columns.toFloat()).toInt()
        val itemHeight = 40.dp
        val itemVerticalSpacing = 10.dp
        val itemTotalHeight = itemHeight.value+(itemVerticalSpacing.value*2)
        val gridHeight = (rows * (itemTotalHeight)).dp +
                paddingValues.calculateTopPadding() +
                paddingValues.calculateBottomPadding()

        Column(
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = emojiData.categoryName,
            )
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = itemHeight),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(gridHeight),
                contentPadding = paddingValues,
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalArrangement = Arrangement.spacedBy(itemVerticalSpacing)
            ) {
                itemsIndexed(
                    items = emojis,
                    key = { index, emoji ->
                        emoji
                    }
                ) { index, emoji ->
                    Text(
                        text = emoji,
                        style = TextStyle.Default.copy(
                            fontSize = 24.sp
                        ),
                        modifier = Modifier
                            .padding(4.dp)
                            .noRippleClick {
                                onEmojiPicked.invoke(emoji)
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun ItemCategoryHeader(
    modifier: Modifier = Modifier,
    emojiCategory: EmojiCategory,
    isSelected: Boolean,
    onCategoryClicked:() -> Unit,
) {
    Column(
        modifier = modifier
            .noRippleClick {
                onCategoryClicked.invoke()
            },
        horizontalAlignment = Alignment.CenterHorizontally
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
