package com.example.plantcontrol

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.particle.android.sdk.cloud.ParticleCloudSDK
import io.particle.android.sdk.cloud.exceptions.ParticleCloudException
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking


class MainActivity : AppCompatActivity(), SeekBar.OnSeekBarChangeListener {


    val min = 100
    val max = 3000
    val step = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ParticleCloudSDK.init(this)


        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                Log.e("Particle Cloud", "new fetch")
                login("variables", "null")
                handler.postDelayed(this, 20000)
            }
        })

        seekBar2.max = ((max - min) / step)

        seekBar2.setOnSeekBarChangeListener(this)

        switch1.setOnClickListener { login("switchsms", "null") }

    }

    private fun login(command: String, args: String) {

        val email = ""
        val password = ""


        var limit = ""
        var moistureval = ""
        var humidity = ""
        var temp = ""
        var smsOn = 0


        runBlocking {
            var loginSuccess = GlobalScope.async {
                try {
                    ParticleCloudSDK.getCloud().logIn(email, password)
                    Log.i("Particle Cloud", "Successful LogIn")
                    if (command == "variables") {
                        val device = ParticleCloudSDK.getCloud().getDevice("2a003b000a47373336323230")
                        limit = device.getIntVariable("limit").toString()
                        moistureval = device.getIntVariable("moistureval").toString()
                        temp = device.getDoubleVariable("temp").toString()
                        humidity = device.getDoubleVariable("humidityval").toString()
                        smsOn = device.getIntVariable("smsonoff")
                    } else if (command == "changelimit") {
                        changeLimit(args)
                    } else if (command == "switchsms") {
                        switchSmsAlert()
                    }
                    true
                } catch (e: ParticleCloudException) {
                    Log.e("Particle Cloud", e.bestMessage)
                    e.printStackTrace()
                    false
                }
            }
            if (loginSuccess.await()) {
                println("Logged in")
            } else {
                println("unsuccessful logIn");
            }
        }

        if(command == "variables") {
            runOnUiThread {
                limitVal.text = limit
                seekBar2.progress = limit.toInt()
                if(smsOn == 1) {
                    switch1.isChecked = true
                } else if(smsOn == 0) {
                    switch1.isChecked = false
                }
                tempVal.text = temp
                plantMoist.text = moistureval
                humidityVal.text = humidity
            }
        }
    }

    private fun switchSmsAlert() {
        var argument = "off"

        if(switch1.isChecked) {
            argument = "on"
        } else if(!switch1.isChecked) {
            argument = "off"
        } else {
            return
        }
        val device = ParticleCloudSDK.getCloud().getDevice("2a003b000a47373336323230")
        device.callFunction("turnoffsms", mutableListOf(argument))
    }

    private fun changeLimit(value: String) {
        val device = ParticleCloudSDK.getCloud().getDevice("2a003b000a47373336323230")
        device.callFunction("changelimit", mutableListOf(value))
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
        val value = min + (progress * step)

        limitVal.text = value.toString()
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        var value = seekBar?.progress
        value as Int
        value += 100
        login("changelimit", value.toString())
    }
}
