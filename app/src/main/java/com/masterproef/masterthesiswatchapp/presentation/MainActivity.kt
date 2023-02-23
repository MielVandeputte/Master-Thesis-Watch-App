package com.masterproef.masterthesiswatchapp.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.MutableLiveData
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Switch
import androidx.wear.compose.material.Text
import com.masterproef.masterthesiswatchapp.model.BackgroundService
import com.masterproef.masterthesiswatchapp.presentation.theme.MasterThesisWatchAppTheme

class MainActivity : ComponentActivity() {

    val checkedState = MutableLiveData(false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WearApp(activity = this)
        }
    }

    fun setConnectable() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        startService(serviceIntent)
    }

    fun setUnconnectable() {
        val serviceIntent = Intent(this, BackgroundService::class.java)
        stopService(serviceIntent)
    }

}

@Composable
fun WearApp(activity: MainActivity) {

    val checked by activity.checkedState.observeAsState()

    MasterThesisWatchAppTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Master Thesis Watch App",
                softWrap = false,
                fontSize = 12.sp
            )

            checked?.let { it ->
                Switch(
                    checked = it,
                    onCheckedChange = { it ->
                        activity.checkedState.postValue(it)

                        if (it) {
                            activity.setConnectable()
                        } else {
                            activity.setUnconnectable()
                        }
                    },
                )
            }

        }
    }
}

@Preview(device = Devices.WEAR_OS_SQUARE, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp(MainActivity())
}