package com.example.apitest

import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.ebest.api.SocketManager
import kotlinx.android.synthetic.main.loginset_activity.*
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.net.*

class MainView : AppCompatActivity() , ActivityCompat.OnRequestPermissionsResultCallback {
    internal val RECEIVE_TIMEOUTERROR = -7               // TIMEOUT 에러
    internal val RECEIVE_INITECHERROR = -6               // initech 핸드세이킹 에러
    internal val RECEIVE_PERMISSIONERROR = -5               // 퍼미션취소
    internal val RECEIVE_ERROR = -4               // 일반적인 에러 (로그인 실패도 여기로 옴)
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

    internal var m_bLoginStatus = false;
    internal var m_nHandle = -1
    internal var handler: ProcMessageHandler? = null
    internal inner class ProcMessageHandler : Handler() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
            }

        }
    }
    lateinit internal var manager: SocketManager

    val fragmanager = supportFragmentManager
    val bJava = false
    val subviewlst = listOf<Triple<Int,String,Int>>(
        Triple(R.id.navi_1,"시간대별",R.drawable.l_icon_01),
        Triple(R.id.navi_4,"매수매도",R.drawable.l_icon_01),
        Triple(R.id.navi_6,"주문",R.drawable.l_icon_01),
        Triple(R.id.navi_8,"잔고",R.drawable.l_icon_01),
        Triple(R.id.navi_news,"경제기사",R.drawable.l_icon_01)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_navview)

        // get SocketManager instance
        handler = ProcMessageHandler()
        manager = (application as ApplicationManager).getSockInstance()
        //manager.setAutoReconnect(true);

        /*
        assets디렉토리의 서브 디렉토리 경로. 지정하지 않으면 기본값으로 res를 설정한다.
        TR 정보가 담긴 .res, 이미지 파일등의 경로.
        메인 액티비티에서 최초 1회 호출하여 설정한다.
        */
        manager.setRes(this, "res")
        manager.checkPermission(this, handler as Handler)       // 공인인증서 때문에 퍼미션이 필요하다.
        manager.setAutoLogin(true)


        fragmanager.beginTransaction().add(R.id.frameLayout, s1001()).commit()
        this@MainView.setTitle(subviewlst.get(0).second)


        /* 하단뷰 버튼 초기화 */
        val lstid = listOf<Triple<Int,String,Int>>(
            Triple(R.id.navi_log,"로그인",R.color._WHITE),
            subviewlst.get(0),
            subviewlst.get(1),
            subviewlst.get(2),
            subviewlst.get(3),
            subviewlst.get(4),
            Triple(R.id.navi_webview,"WebPage",R.drawable.l_icon_01)
        )


        for(idx in 0..lstid.size-1) {
            val txt = findViewById<LinearLayout>(lstid.get(idx).first).findViewById<TextView>(R.id.btn_name)
            txt.text = lstid.get(idx).second.toString()

            val icon = findViewById<LinearLayout>(lstid.get(idx).first).findViewById<ImageView>(R.id.btn_icon)
            icon.setImageResource(lstid.get(idx).third)
        }
        val main = findViewById<LinearLayout>(R.id.navi_1)
        val name = main.findViewById<TextView>(R.id.btn_name)
        val img = main.findViewById<ImageView>(R.id.btn_icon)
        name.setTextColor(Color.rgb(210,34,34))

    }
    // 퍼미션관련 콜백
    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onRequestPermissionsResult(requestCode: Int, permission: Array<String>, grandResults: IntArray) {
        // 거부한 경우 앱을 종료한다
        for (i in 0 .. grandResults.size-1) {
            if (grandResults[i] == -1) {
                finishAffinity();                       // 해당앱의 루트 액티비티를 종료시킨다.
                System.runFinalization();               // 현재 작업중인 쓰레드가 종료되면 종료 시키라는 명령어
                System.exit(0);                 // 현재 액티비티를 종료시킨다.
            }
        }

    }

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    fun onNaviBtnClicked(v: View)
    {

        val viewId = v.id
        var title = ""
        var fragment : Fragment? = null

        when(viewId)
        {
            R.id.navi_log->{
                LoginProcess()
                return
            }
            subviewlst.get(0).first->{

                if(!bJava) {
                    fragment = s1001()
                }
                else {
                    fragment = s1001_j()
                }
                title = subviewlst.get(0).second;
            }
            subviewlst.get(1).first->{
                fragment = s1004()
                title = subviewlst.get(1).second;
            }
            subviewlst.get(2).first->{
                fragment = s1006()
                title = subviewlst.get(2).second;
            }
            subviewlst.get(3).first->{
                fragment = s1010()
                title = subviewlst.get(3).second;
            }
            subviewlst.get(4).first->{
                fragment = news()
                title = subviewlst.get(4).second;
            }
            R.id.navi_webview->{
                val intent = Intent(this, WebViewActivity::class.java)
                startActivity(intent)
                return
            }
        }

        subviewlst.forEach {

            val id = it.first

            val main = findViewById<LinearLayout>(id)
            val name = main.findViewById<TextView>(R.id.btn_name)
            val img = main.findViewById<ImageView>(R.id.btn_icon)
            if ( id.equals(viewId))
            {
                name.setTextColor(Color.rgb(210,34,34))
            }
            else{
                name.setTextColor(Color.rgb(0,0,0))
            }
        }

        /*화면전환*/
        if(fragment != null) {
            fragmanager.beginTransaction().replace(R.id.frameLayout, fragment).commit()
            this@MainView.setTitle(title)
        }
    }

    override fun onResume() {
        super.onResume()
        checkloginstatus(m_bLoginStatus)

    }

    override fun onDestroy() {
        super.onDestroy()
    }



    /* 로그인창 상단 View의 이미지 등록 */
    fun setLoginImage(strImageName:String): Drawable? {

        /* asset resource 경로 지정 */
        var assetManager: AssetManager = resources.assets
        var strPath = "image/" + strImageName
        var iis: InputStream? = assetManager.open(strPath)
        var draw: Drawable? = Drawable.createFromStream(iis, null)

        return draw
    }

    /* 로그인 */
    private fun LoginProcess()
    {
        if(m_bLoginStatus == false) {
            /*
            로그인 화면에서 상단부에 출력되는 이미지 파일 정보
            (resource 파일 경로는  manager.setRes(this, "res") 에서 지정한 경로가 최상위 폴더)
            미지정(null)시 이미지는 출력되지 않는다.
            */
            var draw: Drawable? = setLoginImage("banner_login.png")

            /* 로그인 화면 호출 */
            //manager.loginPopup(this@MainView, handler as Handler, "이베스트 투자증권", draw) // strTitle: 화면 상단에 띄워질 이름, draw: 화면 상단부에 출력될 이미지. null시 빈공간
            var intent = Intent(this,sLoginSet::class.java)
            startActivityForResult(intent,1);
        }
        else{
            /* 로그아웃 */
            manager.logout()
            manager.disconnect()

            Toast.makeText(
                this,
                "로그아웃 완료",
                Toast.LENGTH_SHORT
            ).show()

            checkloginstatus(false)
        }
    }

    private fun checkloginstatus(loginstatus : Boolean)
    {
        val main = findViewById<LinearLayout>(R.id.navi_log)
        val name = main.findViewById<TextView>(R.id.btn_name)
        val img = main.findViewById<ImageView>(R.id.btn_icon)

        if(loginstatus) {
            name.setText("로그아웃")
            name.setTextColor(Color.rgb(210, 34, 34))
            img.setImageResource(R.drawable.ic_launcher_background)
            Toast.makeText(applicationContext, "로그인 완료", Toast.LENGTH_SHORT).show()
        }
        else{
            name.setText("로그인")
            name.setTextColor(Color.rgb(0,0,0))
            img.setImageResource(R.color._WHITE)
        }
        m_bLoginStatus = loginstatus
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            1 -> {
                /* LoginProcess 결과값 */
                when(resultCode) {
                    Activity.RESULT_OK -> {
                        checkloginstatus(true)
                    }
                    Activity.RESULT_CANCELED -> {
                        checkloginstatus(false)
                    }
                }
            }
        }
    }

    public fun onMessage(msg : Message){
        when (msg.what) {
            // SOCEKT이 연결이 끊어졌다.
            RECEIVE_DISCONNECT -> {
                val strMsg = msg.obj as String
                Toast.makeText(applicationContext, strMsg, Toast.LENGTH_SHORT).show()
            }
            // 재연결이 완료됐다.(재로그인이 완료됐다)
            RECEIVE_RECONNECT -> {
                var str = "재연결 "+msg.obj.toString()
                Toast.makeText(applicationContext, msg.obj.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

}
