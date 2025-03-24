package cc.kafuu.mvidemo

import android.content.Context
import android.content.pm.PackageManager
import androidx.lifecycle.viewModelScope
import cc.kafuu.mvidemo.core.CoreViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

class MainViewModel : CoreViewModel<MainUiIntent, MainUiState>(initStatus = MainUiState.None),
    KoinComponent {
    override fun onReceivedUiIntent(uiIntent: MainUiIntent) {
        when (uiIntent) {
            MainUiIntent.PageCreate -> onPageCreate()
            MainUiIntent.LoadApplicationList -> onLoadApplicationList(false)
            MainUiIntent.RefreshApplicationList -> onLoadApplicationList(true)
        }
    }

    private fun onPageCreate() {
        // 将uiState变更为Master状态
        MainUiState.Master().setup()
//        //上面的代码等价于下面的代码
//        _uiStateFlow.value = MainUiState.Master()
    }

    private fun onLoadApplicationList(isRefresh: Boolean) {
        // 先设置loading状态，确保UI能够立即更新显示loading
        viewModelScope.launch {

            // 如果是刷新状态，则不需要设置 loading 状态
            awaitUiStateOfType<MainUiState.Master>().let {
                if (isRefresh) it.copy(isRefreshing = true) else it.copy(isLoading = true)
            }.setup()

            // 模拟网络延迟
            delay(2000)

            // 执行耗时操作
            val applications = withContext(Dispatchers.IO) {
                get<Context>().packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            }

            // 获取最新状态并更新为最终状态
            awaitUiStateOfType<MainUiState.Master>().copy(
                listState = MainListState.ApplicationPackages(applications.map { it.packageName }),
                isRefreshing = false,
                isLoading = false
            ).setup()
        }
    }
}

/**
 * 主页Ui状态密封类
 */
sealed class MainUiState {
    /**
     * 空视图状态
     */
    data object None : MainUiState()

    /**
     * 页面主要状态
     */
    data class Master(
        val listState: MainListState = MainListState.None,
        val isRefreshing: Boolean = false,
        val isLoading: Boolean = false
    ) : MainUiState()
}

/**
 * 主页列表状态
 */
sealed class MainListState {
    /**
     * 空列表状态
     */
    data object None : MainListState()

    /**
     * 应用包名列表
     */
    data class ApplicationPackages(val packages: List<String>) : MainListState()
}

/**
 * 主页Ui意图密封类
 */
sealed class MainUiIntent {
    /**
     * 页面创建
     */
    data object PageCreate : MainUiIntent()

    /**
     * 加载应用列表（按钮点击）
     */
    data object LoadApplicationList : MainUiIntent()

    /**
     * 刷新应用列表（下拉刷新）
     */
    data object RefreshApplicationList : MainUiIntent()
}