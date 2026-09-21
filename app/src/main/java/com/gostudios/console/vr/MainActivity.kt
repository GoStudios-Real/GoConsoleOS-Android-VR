package com.gostudios.console.vr

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import com.gostudios.console.vr.controller.BluetoothController
import com.gostudios.console.vr.headset.GoStudiosHeadset

class MainActivity : Activity() {
    private lateinit var headset: GoStudiosHeadset
    private lateinit var ctrlStatus: TextView
    private lateinit var devInfo: TextView
    private lateinit var headsetInfo: TextView

    @SuppressLint("MissingPermission")
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        enterImmersive()

        headset = GoStudiosHeadset.getInstance(this)
        headset.detectDevice()

        val sv = ScrollView(this)
        val ll = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(48,48,48,48); setBackgroundColor(0xFF0D0D14.toInt()) }

        // Title
        ll.addView(TextView(this).apply { text = "GoConsole VR"; textSize = 32f; setTextColor(0xFF0066FF.toInt()) })
        ll.addView(TextView(this).apply { text = "GoStudios Cardboard VR Headset"; textSize = 16f; setTextColor(0xFF00C9DB.toInt()); setPadding(0,4,0,24) })

        // Device Info
        devInfo = TextView(this).apply {
            text = "Device: ${Build.MODEL}\nManufacturer: ${Build.MANUFACTURER}\nAndroid: ${Build.VERSION.RELEASE}\nScreen: ${headset.screenWidth.toInt()}x${headset.screenHeight.toInt()} @ ${headset.screenDensity}x"
            textSize = 13f; setTextColor(0xFF4A4A6A.toInt()); setPadding(0,0,0,24)
        }
        ll.addView(devInfo)

        // Headset Profile
        ll.addView(TextView(this).apply { text = "HEADSET PROFILE"; textSize = 12f; setTextColor(0xFF4A4A6A.toInt()); setPadding(0,0,0,8) })
        val profiles = GoStudiosHeadset.HeadsetProfile.entries
        val profileSpinner = Spinner(this)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, profiles.map { it.displayName })
        profileSpinner.adapter = adapter
        profileSpinner.setSelection(profiles.indexOf(headset.headsetModel.let { m -> profiles.find { it.displayName == m } ?: GoStudiosHeadset.HeadsetProfile.GOSTUDIOS_CARDBOARD }))
        profileSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) { profiles[pos].applyTo(headset); updateHeadsetInfo() }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        ll.addView(profileSpinner)

        // Headset Info
        headsetInfo = TextView(this).apply { textSize = 13f; setTextColor(0xFF7A80A0.toInt()); setPadding(0,12,0,24) }
        ll.addView(headsetInfo)
        updateHeadsetInfo()

        // IPD Slider
        ll.addView(TextView(this).apply { text = "IPD (interpupillary distance): ${headset.ipd.toInt()}mm"; textSize = 13f; setTextColor(0xFF7A80A0.toInt()); id = 1001 })
        ll.addView(SeekBar(this).apply {
            max = 40; progress = (headset.ipd - 40).toInt()
            setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(sb: SeekBar?, prog: Int, fromUser: Boolean) {
                    headset.ipd = prog + 40f
                    (ll.findViewById<TextView>(1001)).text = "IPD: ${headset.ipd.toInt()}mm"
                }
                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })
        })

        // Controller Status
        ll.addView(TextView(this).apply { text = "\nCONTROLLER STATUS"; textSize = 12f; setTextColor(0xFF4A4A6A.toInt()); setPadding(0,16,0,8) })
        ctrlStatus = TextView(this).apply { text = "Not connected - pair in Android Settings"; textSize = 14f; setTextColor(0xFFFFAA00.toInt()); setPadding(0,0,0,16) }
        ll.addView(ctrlStatus)

        // Scan
        ll.addView(Button(this).apply {
            text = "Scan for Controllers"; setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFF1E2D42.toInt())
            setPadding(24,16,24,16); setOnClickListener { scanBt() }
        })

        // Supported Controllers
        ll.addView(TextView(this).apply {
            text = "\nSupported Controllers:\n- Xbox One/Series X|S\n- PS4 DualShock 4\n- PS5 DualSense\n- Nintendo Switch Pro\n- Nokia MD-11\n- Any generic Bluetooth gamepad"
            textSize = 12f; setTextColor(0xFF4A4A6A.toInt()); setPadding(0,16,0,16)
        })

        // Start VR
        ll.addView(Button(this).apply {
            text = "START VR"; textSize = 18f; setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFF0066FF.toInt())
            setPadding(32,20,32,20); setOnClickListener { startActivity(Intent(this@MainActivity, VRActivity::class.java)) }
        })

        ll.addView(TextView(this).apply {
            text = "\nGoStudios Cardboard VR Headset\nPut your phone in any cardboard VR holder\nGyroscope provides head tracking\n\n5 VR Games:\n- VR Pong\n- VR Snake\n- VR Space Shooter\n- VR Jigsaw\n- VR Basketball\n\nControls:\nHead = Look around\nLeft Stick = Move\nA = Select/Shoot\nB = Back\nD-pad = Navigate\nTriggers = Action"
            textSize = 12f; setTextColor(0xFF7A80A0.toInt()); setPadding(0,16,0,0)
        })

        sv.addView(ll); setContentView(sv)
    }

    private fun updateHeadsetInfo() {
        headsetInfo.text = "Profile: ${headset.headsetModel}\nIPD: ${headset.ipd.toInt()}mm\nFOV: ${headset.lensFov.toInt()}°\nScreen-to-lens: ${headset.screenToLens.toInt()}mm"
    }

    @SuppressLint("MissingPermission")
    private fun scanBt() {
        ctrlStatus.text = "Scanning..."
        val btMgr = getSystemService(BLUETOOTH_SERVICE) as? BluetoothManager
        val adapter = btMgr?.adapter
        var found = false
        adapter?.bondedDevices?.forEach { d ->
            val n = d.name?.lowercase() ?: return@forEach
            if (n.contains("xbox")||n.contains("ps4")||n.contains("ps5")||n.contains("dualshock")||n.contains("dualsense")||n.contains("switch")||n.contains("controller")||n.contains("gamepad")||n.contains("nokia")) {
                ctrlStatus.text = "Found: ${d.name}"; ctrlStatus.setTextColor(0xFF00CC66.toInt()); found = true
            }
        }
        if (!found) { ctrlStatus.text = "No controller found - pair in Android Settings first"; ctrlStatus.setTextColor(0xFFFFAA00.toInt()) }
    }

    private fun enterImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.let { it.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars()); it.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE }
        } else { @Suppress("DEPRECATION") window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) }
    }
}
