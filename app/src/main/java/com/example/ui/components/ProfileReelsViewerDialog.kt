package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.model.PostItem
import com.example.data.model.ReactionType
import com.example.data.model.UserProfile
import com.example.data.repository.PostRepository
import com.example.data.repository.UserRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ProfileReelsViewerDialog(
    reels: List<PostItem>,
    initialIndex: Int = 0,
    currentUserId: String,
    currentUserProfile: UserProfile?,
    postRepository: PostRepository,
    onDismiss: () -> Unit
) {
    if (reels.isEmpty()) {
        onDismiss()
        return
    }

    val startIndex = initialIndex.coerceIn(0, reels.size - 1)
    val pagerState = rememberPagerState(initialPage = startIndex, pageCount = { reels.size })
    var showCommentsSheet by remember { mutableStateOf<PostItem?>(null) }
    var selectedReelForOptions by remember { mutableStateOf<PostItem?>(null) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                val isLiked = reel.likedByMap[currentUserId] == true
                val isVerifiedAuthor = reel.isAuthorVerified || (currentUserProfile != null && reel.authorId == currentUserProfile.uid && currentUserProfile.isVerificationActive()) || UserRepository.isUserVerifiedStatic(reel.authorId)
                val liveAuthorAvatar = UserRepository.getUserAvatarStatic(reel.authorId)
                val effectiveAuthorAvatar = if (currentUserProfile != null && reel.authorId == currentUserProfile.uid && currentUserProfile.profilePictureUrl.isNotBlank()) {
                    currentUserProfile.profilePictureUrl
                } else if (liveAuthorAvatar.isNotBlank()) {
                    liveAuthorAvatar
                } else {
                    reel.authorAvatarUrl
                }
                val authorInitial = reel.authorName.firstOrNull()?.uppercase() ?: "U"

                val coroutineScope = rememberCoroutineScope()
                val heartScale = remember { Animatable(0f) }
                val heartAlpha = remember { Animatable(0f) }

                val triggerHeartAnimation: () -> Unit = {
                    postRepository.setReaction(reel.id, currentUserId, ReactionType.LOVE)
                    coroutineScope.launch {
                        heartScale.snapTo(0.2f)
                        heartAlpha.snapTo(1f)
                        launch {
                            heartScale.animateTo(
                                targetValue = 1.35f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            )
                            heartScale.animateTo(
                                targetValue = 1.0f,
                                animationSpec = tween(durationMillis = 150)
                            )
                        }
                        delay(600)
                        heartAlpha.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .pointerInput(reel.id) {
                            detectTapGestures(
                                onDoubleTap = { triggerHeartAnimation() }
                            )
                        }
                ) {
                    if (reel.mediaUrl.isNotBlank()) {
                        FrndomVideoPlayer(
                            videoUrl = reel.mediaUrl,
                            modifier = Modifier.fillMaxSize(),
                            autoPlay = true,
                            isLooping = true,
                            onDoubleTap = { triggerHeartAnimation() }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
                                    )
                                )
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.PlayCircleOutline,
                                    contentDescription = "Video",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(64.dp)
                                )
                                if (reel.content.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(14.dp))
                                    Text(
                                        text = reel.content,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }

                    // Double Tap Heart
                    if (heartAlpha.value > 0f) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .scale(heartScale.value)
                                    .alpha(heartAlpha.value),
                                contentAlignment = Alignment.Center
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFFA383E).copy(alpha = 0.2f),
                                    modifier = Modifier.size(110.dp)
                                ) {}
                                Text(text = "❤️", fontSize = 72.sp, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    // Bottom Gradient Scrim
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .height(280.dp)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.88f))
                                )
                            )
                    )

                    // Right Side Action Buttons
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        // Like
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { postRepository.toggleLike(reel.id, currentUserId) }
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color(0xFFFA383E) else Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                            Text(
                                text = if (reel.likesCount > 0) "${reel.likesCount}" else "Like",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Comment
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { showCommentsSheet = reel }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubble,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = "${reel.commentsCount}",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        // Share
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.clickable { postRepository.incrementShare(reel.id) }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                            Text(
                                text = if (reel.sharesCount > 0) "${reel.sharesCount}" else "Share",
                                fontSize = 12.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        IconButton(onClick = { selectedReelForOptions = reel }) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Options",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Bottom Left Creator Info
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .fillMaxWidth(0.75f)
                            .padding(start = 16.dp, bottom = 48.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                modifier = Modifier.size(38.dp),
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                if (effectiveAuthorAvatar.isNotBlank()) {
                                    AsyncImage(
                                        model = effectiveAuthorAvatar,
                                        contentDescription = reel.authorName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                } else {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(
                                            text = authorInitial,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.width(10.dp))

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = reel.authorName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                if (isVerifiedAuthor) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    VerificationBadge(size = 15.dp, show = true)
                                }
                            }
                        }

                        if (reel.content.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            HashtagText(
                                text = reel.content,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Top Back Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Reels",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            // Comments Bottom Sheet
            showCommentsSheet?.let { reel ->
                CommentsBottomSheet(
                    postRepository = postRepository,
                    postId = reel.id,
                    userProfile = currentUserProfile,
                    onDismiss = { showCommentsSheet = null },
                    onCommentAdded = { postRepository.addComment(reel.id) }
                )
            }

            // Reel Options Bottom Sheet
            selectedReelForOptions?.let { reel ->
                PostOptionsBottomSheet(
                    post = reel,
                    currentUserId = currentUserId,
                    postRepository = postRepository,
                    onEditClick = { selectedReelForOptions = null },
                    onDeletePost = { selectedReelForOptions = null },
                    onDismiss = { selectedReelForOptions = null }
                )
            }
        }
    }
}
