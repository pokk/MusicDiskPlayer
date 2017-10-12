package com.devrapid.Example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.devrapid.kotlinknifer.logw
import kotlinx.android.synthetic.main.activity_main.btn_click
import kotlinx.android.synthetic.main.activity_main.rotatedCircleImageView

class MainActivity: AppCompatActivity() {
    var time = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rotatedCircleImageView.onClickEvent = { view, isPaused ->
            if (isPaused)
                view.stop()
            else
                view.start()

            logw("hello world")
        }
        rotatedCircleImageView.onChangeTime = { view, currTime ->
            logw(currTime)
        }
        btn_click.setOnClickListener {
            logw("!!!!!!!!!!!")
            time += 10
            rotatedCircleImageView.endTime = time
        }
    }
}
