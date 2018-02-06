package com.devrapid.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.btn_1
import kotlinx.android.synthetic.main.activity_main.btn_click
import kotlinx.android.synthetic.main.activity_main.btn_else
import kotlinx.android.synthetic.main.activity_main.ttt

class MainActivity : AppCompatActivity() {
    var time = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ttt.apply {
            //            playAnimator(5)
            totalTime = 10
            onProgressChanged = { progress, remainedTime ->
                //                logw(progress, remainedTime)
            }
            progress = 10.0
        }

//        rotatedCircleImageView.start()
//
//        rotatedCircleImageView.onClickEvent = { view, isPaused ->
//        }
//        rotatedCircleImageView.onChangeTime = { _, _ ->
//        }
        btn_click.setOnClickListener {
            ttt.stopAnimator()
        }
        btn_else.setOnClickListener {
            ttt.playAnimator()
        }
        btn_1.setOnClickListener {
            //            ttt.progress = 30.0

            ttt.currentTime = 9
        }
    }
}
