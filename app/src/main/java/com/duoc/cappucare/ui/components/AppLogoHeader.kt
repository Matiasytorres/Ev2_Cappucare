package com.duoc.cappucare.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.duoc.cappucare.R

@Composable
fun AppLogoHeader(modifier: Modifier = Modifier) {
    Image(
        painter = painterResource(id = R.drawable.logo_cappucare),
        contentDescription = "Logo Cappucare",
        modifier = modifier
            .height(120.dp)
            .padding(16.dp),
        contentScale = ContentScale.Fit
    )
}