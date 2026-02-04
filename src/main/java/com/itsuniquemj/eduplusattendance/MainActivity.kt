package com.itsuniquemj.eduplusattendance

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.os.Bundle
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.ceil
import kotlin.math.floor

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var btnCheck: Button

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webView)
        btnCheck = findViewById(R.id.btnCheck)

        // 🔥 DESKTOP MODE SETTINGS
        val settings = webView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.useWideViewPort = true
        settings.loadWithOverviewMode = true
        settings.builtInZoomControls = true
        settings.displayZoomControls = false
        settings.setSupportZoom(true)
        settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NORMAL
        settings.userAgentString =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                forceDesktopMode()   // ✅ desktop enforced
            }
        }

        webView.addJavascriptInterface(JSBridge(), "Android")
        webView.loadUrl("https://mysanjivani.edupluscampus.com/")

        btnCheck.setOnClickListener {
            injectAttendanceJS()
        }
        btnCheck.setOnLongClickListener {

            val email = "heymanojjadhav@gmail.com"

            AlertDialog.Builder(this)
                .setTitle("About")
                .setMessage(
                    "Eduplus Attendance Helper\n\n" +
                            "Developed by Manoj Rambhau Jadhav\n" +
                            "Computer Engineering\n\n" +
                            "Email: $email\n\n" +
                            "Built using Native Android (Kotlin) & WebView\n\n" +
                            "Tap email to contact"
                )
                .setPositiveButton("CONTACT") { _, _ ->
                    val intent = android.content.Intent(android.content.Intent.ACTION_SENDTO).apply {
                        data = android.net.Uri.parse("mailto:$email")
                        putExtra(android.content.Intent.EXTRA_SUBJECT, "Regarding Eduplus Attendance App")
                    }
                    startActivity(intent)
                }
                .setNegativeButton("CLOSE", null)
                .show()

            true
        }

    }

    // ================= ATTENDANCE LOGIC =================
    private fun injectAttendanceJS() {
        val js = """
            javascript:(function(){
                try{
                    let rows = document.querySelectorAll("table tr");
                    let present = 0;
                    let total = 0;

                    rows.forEach(r => {
                        let t = r.innerText.toLowerCase();
                        if(t.includes("present") || t.includes("absent")){
                            total++;
                            if(t.includes("present")) present++;
                        }
                    });

                    if(total === 0){
                        Android.show("⚠️ Open attendance page first");
                        return;
                    }

                    let percent = ((present/total)*100).toFixed(2);
                    let needed = Math.ceil((0.75*total - present)/0.25);
                    let bunk = Math.floor((present - 0.75*total)/0.75);

                    let msg = "📊 Present: " + present + "/" + total +
                              "<br>📈 Attendance: " + percent + "%<br><br>";

                    if(percent < 75){
                        msg += "⚠️ Attend " + needed + 
                               " more lecture's";
                    } else {
                        if(bunk > 0){
                            msg += "✅ You can bunk " + bunk + 
                                   " lecture(s) safely";
                        } else {
                            msg += "✅ Attendance is safe";
                        }
                    }

                    Android.show(msg);
                }catch(e){
                    Android.show("❌ Error reading attendance");
                }
            })();
        """
        webView.evaluateJavascript(js, null)
    }

    // ================= JS → ANDROID BRIDGE =================
    inner class JSBridge {
        @JavascriptInterface
        fun show(msg: String) {
            runOnUiThread {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Attendance Summary")
                    .setMessage(msg.replace("<br>", "\n")) // ✅ FIX \n ISSUE
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    // ================= FORCE DESKTOP MODE =================
    private fun forceDesktopMode() {
        val js = """
            javascript:(function(){
                var meta = document.querySelector('meta[name="viewport"]');
                if(meta){
                    meta.setAttribute('content', 'width=1200');
                } else {
                    meta = document.createElement('meta');
                    meta.name = 'viewport';
                    meta.content = 'width=1200';
                    document.getElementsByTagName('head')[0].appendChild(meta);
                }
            })();
        """
        webView.evaluateJavascript(js, null)
    }
}
