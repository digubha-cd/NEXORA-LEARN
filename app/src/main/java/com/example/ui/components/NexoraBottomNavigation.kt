package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Stars
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraSurface
import com.example.ui.theme.NexoraTextMuted

/**
 * Bottom navigation items for NEXORA LEARN main navigation:
 * - Home
 * - Planner
 * - Exam (Step 6 Exam System Dashboard)
 * - Subjects
 * - Profile
 */
enum class NexoraTab(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME(
        route = "tab_home",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home,
        testTag = "nav_tab_home"
    ),
    PLANNER(
        route = "tab_planner",
        label = "Planner",
        selectedIcon = Icons.Filled.CalendarMonth,
        unselectedIcon = Icons.Outlined.CalendarMonth,
        testTag = "nav_tab_planner"
    ),
    EXAM(
        route = "tab_exam",
        label = "Exam",
        selectedIcon = Icons.Filled.Stars,
        unselectedIcon = Icons.Outlined.Stars,
        testTag = "nav_tab_exam"
    ),
    SUBJECTS(
        route = "tab_subjects",
        label = "Subjects",
        selectedIcon = Icons.Filled.MenuBook,
        unselectedIcon = Icons.Outlined.MenuBook,
        testTag = "nav_tab_subjects"
    ),
    PROFILE(
        route = "tab_profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person,
        testTag = "nav_tab_profile"
    )
}

/**
 * Clean, modern bottom navigation bar following the NEXORA LEARN dark theme.
 * Uses rounded container, subtle border, and cyan accent indicators.
 */
@Composable
fun NexoraBottomNavigationBar(
    currentTab: NexoraTab,
    onTabSelected: (NexoraTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(NexoraBackground)
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("nexora_bottom_navigation")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(NexoraSurface, shape = RoundedCornerShape(22.dp))
                .border(1.dp, NexoraBorder, shape = RoundedCornerShape(22.dp))
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NexoraTab.entries.forEach { tab ->
                val isSelected = tab == currentTab

                val iconTint by animateColorAsState(
                    targetValue = if (isSelected) NexoraCyan else NexoraTextMuted,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabIconTint"
                )

                val labelColor by animateColorAsState(
                    targetValue = if (isSelected) NexoraCyan else NexoraTextMuted,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabLabelColor"
                )

                val indicatorBg by animateColorAsState(
                    targetValue = if (isSelected) NexoraCyan.copy(alpha = 0.14f) else Color.Transparent,
                    animationSpec = tween(durationMillis = 200),
                    label = "tabBgColor"
                )

                val interactionSource = remember { MutableInteractionSource() }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(indicatorBg)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(bounded = true, color = NexoraCyan)
                        ) {
                            onTabSelected(tab)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag(tab.testTag),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                            contentDescription = tab.label,
                            tint = iconTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = tab.label,
                            color = labelColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}
