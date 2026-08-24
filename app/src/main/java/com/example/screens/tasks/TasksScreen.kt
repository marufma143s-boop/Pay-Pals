package com.example.screens.tasks

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.components.SubScreenTopBar
import com.example.components.TaskCard
import com.example.repository.AppRepository

@Composable
fun TasksScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val tasks by repository.tasks.collectAsState()
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (onBackClick != null) {
            SubScreenTopBar(title = "Tasks", onBackClick = onBackClick)
        } else {
            SubScreenTopBar(title = "Tasks", onBackClick = {})
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(tasks) { task ->
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(animationSpec = tween(300)),
                    exit = fadeOut(animationSpec = tween(300))
                ) {
                    TaskCard(
                        task = task,
                        onClick = {
                            // Can show details if needed
                        },
                        onCompleteClick = {
                            repository.completeTask(task.id)
                        }
                    )
                }
            }
        }
    }
}
