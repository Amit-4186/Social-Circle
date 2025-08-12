package com.example.socialcircle.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.ui.theme.Blue40
import com.example.socialcircle.viewModels.FriendsViewModel
import kotlinx.coroutines.launch

@Composable
fun FriendScreen(friendsViewModel: FriendsViewModel, mainNavController: NavController, onChatClick: (String) -> Unit) {

    val tab = listOf(
        "Friend List",
        "Friend Requests"
    )
    val pagerState = rememberPagerState(initialPage = 0) { tab.size }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = pagerState.currentPage,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Blue20,
            contentColor = Color.White,
            divider = {},
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(positions[pagerState.currentPage]),
                    height = 3.dp,
                    color = Blue40
                )
            }
        ) {
            tab.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    text = { Text(title) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.LightGray,
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            beyondViewportPageCount = 1
        ) { page ->
            when (page) {
                0 -> FriendList(friendsViewModel, onChatClick, mainNavController)
                1 -> FriendsRequest(friendsViewModel)
            }
        }
    }
}