package com.example.screens.tasks

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.repository.AppRepository
import com.example.screens.packages.PackagesScreen

@Composable
fun TasksScreen(
    repository: AppRepository,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    PackagesScreen(
        repository = repository,
        onBackClick = onBackClick,
        modifier = modifier
    )
}

