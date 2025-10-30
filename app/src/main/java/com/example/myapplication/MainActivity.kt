package com.example.myapplication

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // این خط، فایل گرافیکی (UI) را به این کد متصل می‌کند
        setContentView(R.layout.activity_main)

        // از اینجا به بعد می‌تونی کد بنویسی:
        // مثلا: وقتی اپ اجرا شد چه اتفاقی بیفته
    }
}
