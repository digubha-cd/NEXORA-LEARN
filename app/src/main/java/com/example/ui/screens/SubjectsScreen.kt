package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.outlined.AccountBalance
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.BusinessCenter
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.model.Subject
import com.example.ui.components.NexoraCard
import com.example.ui.components.NexoraLogo
import com.example.ui.theme.NexoraBackground
import com.example.ui.theme.NexoraBorder
import com.example.ui.theme.NexoraCyan
import com.example.ui.theme.NexoraElectricBlue
import com.example.ui.theme.NexoraGold
import com.example.ui.theme.NexoraMagenta
import com.example.ui.theme.NexoraPink
import com.example.ui.theme.NexoraPurple
import com.example.ui.theme.NexoraSurfaceElevated
import com.example.ui.theme.NexoraTextMuted
import com.example.ui.theme.NexoraTextPrimary
import com.example.ui.theme.NexoraTextSecondary

/**
 * Visual metadata for each of the 7 Class 12 Commerce subjects.
 */
private data class SubjectStyle(
    val icon: ImageVector,
    val accentColor: Color,
    val subtitle: String
)

private fun getSubjectStyle(subjectId: String): SubjectStyle {
    return when (subjectId) {
        "gujarati" -> SubjectStyle(
            icon = Icons.Outlined.MenuBook,
            accentColor = NexoraCyan,
            subtitle = "ગુજરાતી • First Language"
        )
        "english" -> SubjectStyle(
            icon = Icons.Outlined.Language,
            accentColor = NexoraElectricBlue,
            subtitle = "English • Second Language"
        )
        "sp_cc" -> SubjectStyle(
            icon = Icons.Outlined.Description,
            accentColor = NexoraPurple,
            subtitle = "Secretarial Practice & Commercial Correspondence"
        )
        "ba" -> SubjectStyle(
            icon = Icons.Outlined.BusinessCenter,
            accentColor = NexoraMagenta,
            subtitle = "Business Administration & Management"
        )
        "stat" -> SubjectStyle(
            icon = Icons.Outlined.BarChart,
            accentColor = NexoraCyan,
            subtitle = "Statistics • આંકડાશાસ્ત્ર"
        )
        "accounts" -> SubjectStyle(
            icon = Icons.Outlined.AccountBalance,
            accentColor = NexoraGold,
            subtitle = "Elements of Accounts • નામાનાં મૂળતત્ત્વો"
        )
        "economics" -> SubjectStyle(
            icon = Icons.Outlined.TrendingUp,
            accentColor = NexoraPink,
            subtitle = "Economics • અર્થશાસ્ત્ર"
        )
        else -> SubjectStyle(
            icon = Icons.Outlined.MenuBook,
            accentColor = NexoraCyan,
            subtitle = "Class 12 Commerce"
        )
    }
}

/**
 * Screen displaying the official 7 Class 12 Commerce subjects:
 * 1. Gujarati
 * 2. English
 * 3. SP & CC
 * 4. B.A.
 * 5. Statistics
 * 6. Elements of Accounts
 * 7. Economics
 *
 * Each card is interactive and navigates to the empty subject detail screen.
 */
@Composable
fun SubjectsScreen(
    onSubjectClick: (Subject) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(NexoraBackground)
            .statusBarsPadding()
            .testTag("subjects_screen")
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Subjects",
                            color = NexoraTextPrimary,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Class 12 Commerce • Gujarati Medium (7 Subjects)",
                            color = NexoraTextSecondary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    NexoraLogo(
                        emblemSize = 36.dp,
                        showWordmark = false,
                        showSubtitle = false
                    )
                }
            }

            // Overview Info Card
            item {
                NexoraCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subjects_overview_card"),
                    contentPadding = 14.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(NexoraCyan.copy(alpha = 0.15f), shape = CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.MenuBook,
                                contentDescription = null,
                                tint = NexoraCyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Gujarat Secondary & Higher Secondary Board",
                                color = NexoraTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Tap any subject to view study chapters & planner.",
                                color = NexoraTextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Subject Cards List
            items(Subject.OFFICIAL_SUBJECTS, key = { it.id }) { subject ->
                val style = getSubjectStyle(subject.id)

                NexoraCard(
                    onClick = { onSubjectClick(subject) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("subject_card_${subject.id}"),
                    borderColor = NexoraBorder,
                    cornerRadius = 16.dp,
                    contentPadding = 16.dp
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            // Subject Icon Emblem
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(style.accentColor.copy(alpha = 0.14f), shape = RoundedCornerShape(12.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = style.icon,
                                    contentDescription = subject.name,
                                    tint = style.accentColor,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = subject.name,
                                        color = NexoraTextPrimary,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(NexoraSurfaceElevated, shape = RoundedCornerShape(6.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = subject.code,
                                            color = style.accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = style.subtitle,
                                    color = NexoraTextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Normal,
                                    maxLines = 1
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = "Open ${subject.name}",
                            tint = NexoraTextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
