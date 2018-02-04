package com.devrapid.example

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.btn_click
import kotlinx.android.synthetic.main.activity_main.btn_else
import kotlinx.android.synthetic.main.activity_main.rotatedCircleImageView

class MainActivity : AppCompatActivity() {
    var time = 100
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rotatedCircleImageView.startTime = 44

        rotatedCircleImageView.onClickEvent = { view, isPaused ->
            if (isPaused)
                view.stop()
            else
                view.start()

        }
        rotatedCircleImageView.onChangeTime = { _, _ ->
        }
        btn_click.setOnClickListener {
            time += 10
            rotatedCircleImageView.endTime = time
        }
        btn_else.setOnClickListener {
            rotatedCircleImageView.startTime += 5
        }
    }
}
