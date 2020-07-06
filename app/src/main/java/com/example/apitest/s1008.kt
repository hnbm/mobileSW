package com.example.apitest

import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.ebest.api.*

class s1008 : Fragment() {

    internal val RECEIVE_INITECHERROR = -6               // initech 핸드세이킹 에러
    internal val RECEIVE_PERMISSIONERROR = -5               // 퍼미션취소
    internal val RECEIVE_ERROR = -4               // 일반적인 에러
    internal val RECEIVE_SYSTEMERROR = -2               // 서버에서 내려주는 시스템에러
    internal val RECEIVE_DATA = 1                // TR데이타 수신
    internal val RECEIVE_REALDATA = 2                // 실시간데이타 수신
    internal val RECEIVE_MSG = 3                // TR메세지 수신

    internal var m_nHandle = -1
    internal var handler: ProcMessageHandler? = null
    lateinit internal var manager: SocketManager
    internal inner class ProcMessageHandler : Handler() {

        @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)

            when (msg.what) {

                // 퍼미션 에러
                RECEIVE_PERMISSIONERROR -> {
                    activity?.finishAffinity();                       // 해당앱의 루트 액티비티를 종료시킨다.
                    System.runFinalization();               // 현재 작업중인 쓰레드가 종료되면 종료 시키라는 명령어
                    System.exit(0);                 // 현재 액티비티를 종료시킨다.
                }

                // INITECH 핸드세이킹 에러
                RECEIVE_INITECHERROR -> {
                    val strMsg = msg.obj as String
                    Toast.makeText(activity?.applicationContext, strMsg, Toast.LENGTH_SHORT).show()
                }


                // 일반적인 에러
                RECEIVE_ERROR -> {
                    val strMsg = msg.obj as String
                    Toast.makeText(activity?.applicationContext, strMsg, Toast.LENGTH_SHORT).show()
                }

                // 서버에서 보내는 시스템 ERROR
                RECEIVE_SYSTEMERROR -> {
                    val pMsg = msg.obj as MsgPacket
                    Toast.makeText(
                        activity?.applicationContext,
                        pMsg.strMessageData,
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // TR데이타
                RECEIVE_DATA -> {
                    val lpDp = msg.obj as DataPacket
                    if (lpDp.strTRCode == "t1209") {
                        processT1209(lpDp.strBlockName!!, lpDp.pData!!)
                    }
                }

                // REAL데이타
                RECEIVE_REALDATA -> {
                    val lpRp = msg.obj as RealPacket
                    if (lpRp.strBCCode == "") {

                    }
                }


                // TR메세지
                RECEIVE_MSG -> {
                    val lpMp = msg.obj as MsgPacket
                    Toast.makeText(
                        activity?.applicationContext,
                        lpMp.strTRCode + " " + lpMp.strMsgCode + lpMp.strMessageData,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                // 접속종료 또는 재연결
                mainView.RECEIVE_DISCONNECT ,  mainView.RECEIVE_RECONNECT->{
                    mainView.onMessage(msg);
                }
            }
        }
    }

    lateinit var mainView: MainView
    lateinit var root: View
    internal var m_adapter = TableGrid().DataAdapter(R.layout.s1008_item01)
    lateinit var m_gridView: GridView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.activity_s1008, container, false)
        mainView = (activity as MainView)
        // get SocketManager instance
        handler = ProcMessageHandler()
        manager = (activity?.application as ApplicationManager).getSockInstance()
        m_gridView = root.findViewById<View>(R.id.grid_view) as GridView
        m_gridView.adapter = m_adapter


        root.findViewById<Button>(R.id.btn_last).setOnClickListener{
            request()
        }


        return root
    }
    override fun onResume() {
        super.onResume()
        m_nHandle = manager.setHandler(mainView, handler as Handler)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        manager.deleteHandler(m_nHandle)
    }

    private fun request(){

        m_adapter.items.clear()
        m_adapter.notifyDataSetChanged()

        /* inblock 데이터 필드값 사이 빈칸(" ") 필요 */
        val inblock = "0063" + " " + "01000000000000000000" + " "+ "KR 06 001"
        /* TR 요청 */
        manager.requestData(m_nHandle, "t1209", inblock, false, "", 200)

    }


    private fun processT1209(strBlockName: String, pData: ByteArray) {
        if (strBlockName == "t1209OutBlock1" == true) {
            processT1209OutBlock1(pData)
        }
    }
    private fun processT1209OutBlock1(pData: ByteArray){


        // 데이터 처리
        for(idx in 0..10)
        {
            val data_record: List<Triple<TableGrid.TYPE, Any?, Int>> = listOf(
                Triple(TableGrid.TYPE.STRING,"",R.id.view1)
            )
            m_adapter.addItem(data_record)
        }
        m_adapter.notifyDataSetChanged()
    }
}