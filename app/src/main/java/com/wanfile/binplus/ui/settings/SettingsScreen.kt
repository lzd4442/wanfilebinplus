package com.wanfile.binplus.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var dynamicColor by remember { mutableStateOf(true) }
    var saveHistory by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "设置",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column {
                ListItem(
                    headlineContent = { Text("动态取色 (Material You)") },
                    supportingContent = { Text("跟随系统壁纸自动变换主题色") },
                    trailingContent = {
                        Switch(
                            checked = dynamicColor,
                            onCheckedChange = { dynamicColor = it }
                        )
                    }
                )
                ListItem(
                    headlineContent = { Text("保存转换记录") },
                    supportingContent = { Text("在历史页中保留转换日志") },
                    trailingContent = {
                        Switch(
                            checked = saveHistory,
                            onCheckedChange = { saveHistory = it }
                        )
                    }
                )
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            ListItem(
                headlineContent = { Text("关于") },
                supportingContent = { Text("wanfilebin plus v1.0\n万物皆可 .bin") },
                leadingContent = {
                    Icon(Icons.Outlined.Info, null, tint = MaterialTheme.colorScheme.primary)
                }
            )
        }
    }
}
