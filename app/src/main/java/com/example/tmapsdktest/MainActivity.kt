package com.example.tmapsdktest

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.skt.tmap.TMapData
import com.skt.tmap.TMapData.TMapPathType
import com.skt.tmap.TMapPoint
import com.skt.tmap.TMapView
import com.skt.tmap.overlay.TMapMarkerItem
import com.skt.tmap.poi.TMapPOIItem
import kotlinx.coroutines.*


class MainActivity : AppCompatActivity() {
    private lateinit var tMapView: TMapView
    private var isAppKeyAuthenticated = false
    private var firstMarker: TMapPoint? = null
    private var secondMarker: TMapPoint? = null
    private var isSetFirst = true
    private var isSetAllMarker = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // TMapView 초기화 및 AppKey 인증 대기
        initializeAndAwaitAppKeyAuthentication()
    }

    private fun initializeAndAwaitAppKeyAuthentication() {
        // TMapView 초기화
        tMapView = TMapView(this)
        tMapView.setSKTMapApiKey("Enter Your APPKEY")

        val tMapContainer = findViewById<FrameLayout>(R.id.tmapViewContainer)
        tMapContainer.addView(tMapView)

        // AppKey 인증 대기
        CoroutineScope(Dispatchers.Main).launch {
            waitForAppKeyAuthentication()
            // 인증이 완료되면 경로를 그리는 작업 실행
            drawRoute()
        }
    }

    private suspend fun waitForAppKeyAuthentication() {
        // AppKey 인증이 완료될 때까지 반복하여 확인
        while (!isAppKeyAuthenticated) {
            delay(1000) // 1초마다 확인
            isAppKeyAuthenticated = checkAppKeyAuthentication()
        }
    }

    private fun checkAppKeyAuthentication(): Boolean {
        return true
    }

    private fun setMarker(point: TMapPoint) {
        if(isSetFirst) {
            firstMarker = point
            Log.d("marker", "draw first marker")
        } else {
            secondMarker = point
            Log.d("marker", "draw second marker")
        }

        if(firstMarker != null && secondMarker != null) {
            isSetAllMarker = true
        }

        if(isSetAllMarker) {
            drawRoute()
        }
    }

    private fun drawRoute() {
        // 경로 그리는 작업
        val startPoint = TMapPoint(37.472678, 126.920928)
        val endPoint = TMapPoint(37.405619, 127.091903)
        TMapData().findPathDataWithType(TMapPathType.PEDESTRIAN_PATH, startPoint, endPoint
        ) { tMapPolyLine ->
            tMapPolyLine.lineWidth = 3F
            tMapPolyLine.lineColor = Color.BLUE
            tMapPolyLine.lineAlpha = 255

            tMapPolyLine.outLineWidth = 5F
            tMapPolyLine.outLineColor = Color.RED
            tMapPolyLine.outLineAlpha = 255

            tMapView.addTMapPolyLine(tMapPolyLine)
            val info = tMapView.getDisplayTMapInfo(tMapPolyLine.linePointList)
            tMapView.zoomLevel = info.zoom
            tMapView.setCenterPoint(
                info.point.latitude,
                info.point.longitude
            )
        }
    }
}
