package com.example.socialcircle.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.socialcircle.ui.theme.Blue20
import com.example.socialcircle.ui.theme.Blue40
import com.example.socialcircle.viewModels.FriendsViewModel

@Composable
fun FriendScreen(friendsViewModel: FriendsViewModel, mainNavController: NavController, onChatClick: (String) -> Unit) {

    val tab = listOf(
        "Friend List",
        "Friend Requests"
    )
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Blue20,
            contentColor = Color.White,
            divider = {},
            indicator = { positions ->
                TabRowDefaults.SecondaryIndicator(
                    Modifier.tabIndicatorOffset(positions[selectedTabIndex]),
                    height = 3.dp,
                    color = Blue40
                )
            }
        ) {
            tab.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    selectedContentColor = Color.White,
                    unselectedContentColor = Color.LightGray,
                )
            }
        }
        when (selectedTabIndex) {
            0 -> FriendList(friendsViewModel, onChatClick)
            1 -> FriendsRequest(friendsViewModel)
        }
    }
//    Scaffold(
//
//    ) { padding ->
//        Box(Modifier.padding(padding)) {
//
//        }
//    }
}
