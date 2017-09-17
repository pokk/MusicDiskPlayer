package com.devrapid.Example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.devrapid.kotlinknifer.logw
import kotlinx.android.synthetic.main.activity_main.rotatedCircleImageView

class MainActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rotatedCircleImageView.onClickEvent = { view, isPaused ->
            logw("hello world")
        }
    }
}
