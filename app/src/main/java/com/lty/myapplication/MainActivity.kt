package com.lty.myapplication

import android.os.Bundle
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.webkit.WebViewAssetLoader
import com.lty.myapplication.web.KiteJSInterface
import timber.log.Timber
import timber.log.Timber.DebugTree


class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WebView.setWebContentsDebuggingEnabled(true)
        Timber.plant(DebugTree())
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        webView = findViewById(R.id.webview)
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            // 使用 AssetLoader 后，就不需要下面这两个危险的 File 权限了
            // allowFileAccessFromFileURLs = true
            // allowUniversalAccessFromFileURLs = true
        }

        // 1. 初始化 WebViewAssetLoader
        val assetLoader = WebViewAssetLoader.Builder()
            // 将 https://appassets.androidplatform.net/assets/ 的请求映射到本地的 assets 目录
            .addPathHandler("/assets/", WebViewAssetLoader.AssetsPathHandler(this))
            .build()

        // 2. 设置 WebViewClient 并拦截请求
        webView.webViewClient = object : WebViewClient() {
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                // 如果请求的是虚拟域名，assetLoader 会自动去 assets 目录找文件并返回
                return assetLoader.shouldInterceptRequest(request.url)
            }
        }

        // 挂载你之前写的 JSBridge
         val jsInterface = KiteJSInterface(webView, lifecycleScope)
         webView.addJavascriptInterface(jsInterface, "NativeInterface")

        // 3. 放弃 file:///，改用虚拟 HTTPS 域名加载你的 index.html
        // 注意路径：由于我们上面配置了 /assets/ 映射，所以拼接到这里
        webView.loadUrl("https://appassets.androidplatform.net/assets/index.html")
    }
}