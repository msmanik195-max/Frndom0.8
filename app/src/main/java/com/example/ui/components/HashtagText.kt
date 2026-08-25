package com.example.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Regex to capture English, digits, underscore, and Bengali Unicode range (\u0980-\u09FF)
private val HASHTAG_REGEX = Regex("(#[a-zA-Z0-9_\u0980-\u09FF]+)")

@Composable
fun HashtagText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF050505),
    hashtagColor: Color = Color(0xFF1877F2),
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = (fontSize.value * 1.35f).sp,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onHashtagClick: ((String) -> Unit)? = null,
    onTextClick: (() -> Unit)? = null
) {
    val annotatedString = buildAnnotatedString {
        var lastIndex = 0
        HASHTAG_REGEX.findAll(text).forEach { matchResult ->
            val start = matchResult.range.first
            val end = matchResult.range.last + 1

            // Append normal text before hashtag
            if (start > lastIndex) {
                append(text.substring(lastIndex, start))
            }

            val tag = matchResult.value
            val tagStartInAnnotated = length
            append(tag)
            val tagEndInAnnotated = length

            // Attach annotation and style for hashtag
            addStyle(
                style = SpanStyle(
                    color = hashtagColor,
                    fontWeight = FontWeight.SemiBold
                ),
                start = tagStartInAnnotated,
                end = tagEndInAnnotated
            )
            addStringAnnotation(
                tag = "HASHTAG",
                annotation = tag,
                start = tagStartInAnnotated,
                end = tagEndInAnnotated
            )

            lastIndex = end
        }

        // Append remaining text
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    val style = TextStyle(
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        lineHeight = lineHeight
    )

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        style = style,
        maxLines = maxLines,
        overflow = overflow,
        onTextLayout = onTextLayout,
        onClick = { offset ->
            var clickedHashtag: String? = null
            annotatedString.getStringAnnotations(tag = "HASHTAG", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    clickedHashtag = annotation.item
                    onHashtagClick?.invoke(annotation.item)
                }
            if (clickedHashtag == null) {
                onTextClick?.invoke()
            }
        }
    )
}

/**
 * Facebook-style expandable post text.
 * Shows 3 lines initially when text is long, with "... See more" in English.
 * Expands to full text when clicked.
 */
@Composable
fun ExpandableHashtagText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF050505),
    hashtagColor: Color = Color(0xFF1877F2),
    seeMoreColor: Color = Color(0xFF65676B),
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.Start,
    lineHeight: TextUnit = (fontSize.value * 1.35f).sp,
    collapsedMaxLines: Int = 3,
    onHashtagClick: ((String) -> Unit)? = null
) {
    var isExpanded by remember { mutableStateOf(false) }
    var isOverflowing by remember { mutableStateOf(false) }

    // Initial estimation to prevent flash
    val lineBreakCount = remember(text) { text.count { it == '\n' } }
    val isPotentiallyLong = remember(text, lineBreakCount) {
        lineBreakCount >= collapsedMaxLines || text.length > 130
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
    ) {
        if (!isExpanded) {
            HashtagText(
                text = text,
                color = color,
                hashtagColor = hashtagColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign,
                lineHeight = lineHeight,
                maxLines = collapsedMaxLines,
                overflow = TextOverflow.Ellipsis,
                onTextLayout = { layoutResult ->
                    if (layoutResult.hasVisualOverflow || layoutResult.lineCount > collapsedMaxLines || layoutResult.didOverflowHeight) {
                        isOverflowing = true
                    }
                },
                onHashtagClick = onHashtagClick,
                onTextClick = {
                    if (isOverflowing || isPotentiallyLong) {
                        isExpanded = true
                    }
                }
            )

            if (isOverflowing || isPotentiallyLong) {
                Row(
                    modifier = Modifier
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { isExpanded = true }
                        .padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "… See more",
                        fontSize = fontSize,
                        fontWeight = FontWeight.SemiBold,
                        color = seeMoreColor
                    )
                }
            }
        } else {
            HashtagText(
                text = text,
                color = color,
                hashtagColor = hashtagColor,
                fontSize = fontSize,
                fontWeight = fontWeight,
                textAlign = textAlign,
                lineHeight = lineHeight,
                maxLines = Int.MAX_VALUE,
                overflow = TextOverflow.Clip,
                onHashtagClick = onHashtagClick
            )
        }
    }
}

