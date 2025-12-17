package com.reindra.myapplication.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BatteryAlert
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

import androidx.compose.ui.res.stringResource

data class OnboardingPage(
    val titleRes: Int,
    val descriptionRes: Int,
    val icon: ImageVector,
    val color: Color
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onFinish: () -> Unit
) {
    val pages = listOf(
        OnboardingPage(
            com.reindra.myapplication.R.string.onboarding_welcome_title,
            com.reindra.myapplication.R.string.onboarding_welcome_desc,
            Icons.Rounded.TouchApp,
            MaterialTheme.colorScheme.primary
        ),
        OnboardingPage(
            com.reindra.myapplication.R.string.onboarding_wake_title,
            com.reindra.myapplication.R.string.onboarding_wake_desc,
            Icons.Rounded.Vibration,
            MaterialTheme.colorScheme.tertiary
        ),
        OnboardingPage(
            com.reindra.myapplication.R.string.onboarding_battery_title,
            com.reindra.myapplication.R.string.onboarding_battery_desc,
            Icons.Rounded.BatteryAlert,
            MaterialTheme.colorScheme.error
        ),
        OnboardingPage(
            com.reindra.myapplication.R.string.onboarding_lock_title,
            com.reindra.myapplication.R.string.onboarding_lock_desc,
            Icons.Rounded.CheckCircle,
            MaterialTheme.colorScheme.secondary
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(0.1f))
        
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(0.7f)
        ) { position ->
            OnboardingPageContent(page = pages[position])
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Indicators
        Row(
            Modifier
                .wrapContentHeight()
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { iteration ->
                val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                Box(
                    modifier = Modifier
                        .padding(4.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(10.dp)
                )
            }
        }

        Spacer(modifier = Modifier.weight(0.1f))

        Button(
            onClick = {
                scope.launch {
                    if (pagerState.currentPage < pages.size - 1) {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    } else {
                        onFinish()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = if (pagerState.currentPage == 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = if (pagerState.currentPage == pages.size - 1) stringResource(com.reindra.myapplication.R.string.onboarding_button_get_started) else stringResource(com.reindra.myapplication.R.string.onboarding_button_next)
            )
        }
        
         if (pagerState.currentPage < pages.size - 1) {
             TextButton(onClick = onFinish) {
                 Text(stringResource(com.reindra.myapplication.R.string.onboarding_button_skip), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
             }
         } else {
             Spacer(modifier = Modifier.height(48.dp)) // Placeholder for skip button
         }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = page.icon,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = page.color
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = stringResource(page.titleRes),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(page.descriptionRes),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )
    }
}
