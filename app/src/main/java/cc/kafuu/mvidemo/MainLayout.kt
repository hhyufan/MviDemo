package cc.kafuu.mvidemo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import cc.kafuu.mvidemo.core.ActivityPreview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator

@Composable
fun MainLayout(
    uiState: MainUiState,
    onEmitUiIntent: (MainUiIntent) -> Unit
) {
    when (uiState) {
        MainUiState.None -> Unit
        is MainUiState.Master -> MasterLayout(uiState, onEmitUiIntent)
    }
}

@Composable
private fun MasterLayout(
    uiState: MainUiState.Master,
    onEmitUiIntent: (MainUiIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .navigationBarsPadding()
            .fillMaxSize()
    ) {
        if (uiState.isLoading) {
            // 显示加载中状态
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(50.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "加载中...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // 列表视图区
            when (val listState = uiState.listState) {
                // 空状态，使用Spacer占位
                MainListState.None -> Spacer(modifier = Modifier.weight(1f))
                // 应用包名列表状态
                is MainListState.ApplicationPackages -> ApplicationPackagesLayout(
                    uiState = uiState,
                    modifier = Modifier.weight(1f),
                    listState = listState,
                    onEmitUiIntent = onEmitUiIntent
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            onClick = { onEmitUiIntent(MainUiIntent.LoadApplicationList) }
        ) {
            Text(text = stringResource(R.string.load_application_list))
        }
    }
}

/**
 * 应用列表
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApplicationPackagesLayout(
    uiState: MainUiState.Master,
    listState: MainListState.ApplicationPackages,
    onEmitUiIntent: (MainUiIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
        val state = rememberPullToRefreshState()
        // 显示应用列表
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEmitUiIntent(MainUiIntent.RefreshApplicationList) },
            modifier = modifier
                .padding(horizontal = 10.dp)
                .fillMaxWidth(),
            state = state,
            indicator = {
                Indicator(
                    modifier = Modifier.align(Alignment.TopCenter),
                    isRefreshing = uiState.isRefreshing,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    state = state
                )
            },
        ) {

                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(listState.packages) {
                        Spacer(modifier = Modifier.height(10.dp))
                        ApplicationPackageItem(it)
                    }
                }
            }
        }
/**
 * 应用列表表项
 */
@Composable
private fun ApplicationPackageItem(
    packageName: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            text = packageName
        )
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
fun MasterLayoutEmptyPreview() {
    ActivityPreview(darkTheme = false) {
        MainLayout(
            uiState = MainUiState.Master(
                listState = MainListState.None
            ),
            onEmitUiIntent = {}
        )
    }
}

@Preview(widthDp = 320, heightDp = 640)
@Composable
fun MasterLayoutPreview() {
    val list = (0..100).map { "Item$it" }
    ActivityPreview(darkTheme = false) {
        MainLayout(
            uiState = MainUiState.Master(
                listState = MainListState.ApplicationPackages(list)
            ),
            onEmitUiIntent = {}
        )
    }
}
