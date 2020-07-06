package com.example.apitest

import android.app.Activity
import android.content.Intent
import android.os.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ebest.api.*

class sLoginSet : AppCompatActivity() {
    internal val RECEIVE_TIMEOUTERROR = -7               // TIMEOUT 에러
    internal val RECEIVE_INITECHERROR = -6               // initech 핸드세이킹 에러
    internal val RECEIVE_PERMISSIONERROR = -5               // 퍼미션취소
    internal val RECEIVE_ERROR = -4               // 일반적인 에러
    internal val RECEIVE_DISCONNECT = -3               // SOCKET이 연결종료된 경우
    internal val RECEIVE_SYSTEMERROR = -2               // 서버에서 내려주는 시스템에러
    internal val RECEIVE_CONNECTERROR = -1               // SOCKET 연결에러
    internal val RECEIVE_CONNECT = 0                 // SOCKET 연결완료
    internal val RECEIVE_DATA = 1                // TR데이타 수신
    internal val RECEIVE_REALDATA = 2                // 실시간데이타 수신
    internal val RECEIVE_MSG = 3                // TR메세지 수신
    internal val RECEIVE_LOGINCOMPLETE = 4                // 로그인완료
    internal val RECEIVE_RECONNECT = 5                // SOCKET종료후 재연결 완료
    internal val RECEIVE_SIGN = 6                // 선택한 공인인증서 관련 정보

    internal var m_nHandle = -1
    lateinit internal var manager: SocketManager
    internal var handler: ProcMessageHandler? = null
    internal inner class ProcMessageHandler : Handler() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
            }

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.loginset_activity);
        handler = ProcMessageHandler()
        manager = (application as ApplicationManager).getSockInstance()

        this.findViewById<Button>(R.id.btn_login1).setOnClickListener {
            manager.loginPopupID(this,handler as Handler);

        }
        this.findViewById<Button>(R.id.btn_login2).setOnClickListener {
            manager.loginPopupSign(this,handler as Handler);
        }
        this.findViewById<Button>(R.id.btn_login3).setOnClickListener {
            //manager.loginID(0,"","");
            var intent = Intent(this,sLoginSample1::class.java)
            startActivityForResult(intent,1);
        }
        this.findViewById<Button>(R.id.btn_login4).setOnClickListener {
            // manager.loginSign(0,"","");
            var intent = Intent(this,sLoginSample2::class.java)
            startActivityForResult(intent,1);
        }

    }

    override fun onResume() {
        super.onResume()
        m_nHandle = manager.setHandler(this, handler as Handler)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.deleteHandler(m_nHandle)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            1 -> {
                /* LoginProcess 결과값 */
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        setResult(resultCode)
                        finish()
                    }
                    Activity.RESULT_CANCELED -> {
                        setResult(resultCode)
                        finish()
                    }
                }
            }
        }
    }
}