package com.devrapid.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.btn_1
import kotlinx.android.synthetic.main.activity_main.btn_click
import kotlinx.android.synthetic.main.activity_main.btn_else

class MainActivity : AppCompatActivity() {
    var time = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        rotatedCircleImageView.onClickEvent = { view, isPaused ->
//        }
//        rotatedCircleImageView.onChangeTime = { _, _ ->
//        }
        btn_click.setOnClickListener {
        }
        btn_else.setOnClickListener {
        }
        btn_1.setOnClickListener {
        }
    }
}
