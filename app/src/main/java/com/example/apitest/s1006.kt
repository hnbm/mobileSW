package com.example.apitest

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.ebest.api.DataPacket
import com.ebest.api.MsgPacket
import com.ebest.api.RealPacket
import com.ebest.api.SocketManager
import com.ebest.api.rm.ResourceManager
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Text
import org.xml.sax.Parser
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.lang.RuntimeException

import java.util.ArrayList
import java.util.HashMap

class s1006 : Fragment() {
    private var m_editTextAccount: EditText? = null
    private var m_editTextJongmok: EditText? = null
    private var jongmokName: TextView? = null
    private var m_editTextQty: EditText? = null
    private var m_editTextDanga: EditText? = null
    private var m_textViewJumunBunho: TextView? = null
    private var manager: SocketManager? = null
    private var root: View? = null

    lateinit var m_combobox: Spinner

    internal var m_dn = ""
    private var maesumaedo: Boolean = true  // true: 매수, false: 매도

    @SuppressLint("HandlerLeak")
    private inner class ProcMessageHandler : Handler() {
        override fun handleMessage(msg: Message) {

            val msg_type = msg.what
            when (msg_type) {
                RECEIVE_DATA -> {
                    val lpDp = msg.obj as DataPacket
                    val trcode = lpDp.strTRCode

                    if (trcode == "t1102") {
                        processT1102(lpDp.strBlockName!!, lpDp.pData)
                    } else if (trcode == "t1101") {
                        processT1101(lpDp.pData!!)
//                        Toast.makeText(
//                            activity?.applicationContext,
//                            lpDp.strTRCode + " " + lpDp.pData.toString() + lpDp.strUserData,
//                            Toast.LENGTH_SHORT
//                        ).show()
                    } else if (trcode!!.contains("CSPAT") || trcode.contains("CFOAT")) {
                        processCSPAT_CFOAT(lpDp.pData, lpDp.strTRCode)
                    }
                }
                RECEIVE_REALDATA -> {
                    val lpRp = msg.obj as RealPacket
                    if (lpRp.strBCCode == "S3_" || lpRp.strBCCode == "K3_") {
                        //processS3_(lpRp.strKeyCode, lpRp.pData);
                    } else if (lpRp.strBCCode == "SC0" || lpRp.strBCCode == "SC1" || lpRp.strBCCode == "SC2" || lpRp.strBCCode == "SC3" || lpRp.strBCCode == "SC4") {
                        var pData = lpRp.pData
                        var nLen = pData!!.size

                        // 주식주문접수
                        if (lpRp.strBCCode == "SC0") {
                            processSC0(pData)

                            // 주식주문체결
                        } else if (lpRp.strBCCode == "SC1") {
                            processSC1(pData)

                            // 주식주문정정
                        } else if (lpRp.strBCCode == "SC2") {
                            processSC2(pData)

                            // 주식주문취소
                        } else if (lpRp.strBCCode == "SC3") {
                            processSC3(pData)

                            // 주식주문거부
                        } else if (lpRp.strBCCode == "SC4") {
                            processSC4(pData)
                        }
                    }
                }
                RECEIVE_MSG -> {
                    val lpMp = msg.obj as MsgPacket
//                    Toast.makeText(
//                        activity!!.applicationContext,
//                        lpMp.strTRCode + " " + lpMp.strMsgCode + lpMp.strMessageData,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
                // 일반적인 에러
                RECEIVE_ERROR -> {
                    val strMsg = msg.obj as String
                    Toast.makeText(activity!!.applicationContext, strMsg, Toast.LENGTH_SHORT).show()
                }

                // 접속종료 또는 재연결
                RECEIVE_DISCONNECT, RECEIVE_RECONNECT -> run { mainView!!.onMessage(msg) }

                else -> {
                }
            }
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        root = inflater.inflate(R.layout.activity_s1006, null)
        // get SocketManager instance
        mainView = activity as MainView?
        m_handler = ProcMessageHandler()
        manager = (activity?.application as ApplicationManager).getSockInstance()

        m_editTextAccount = root!!.findViewById(R.id.editTextAccount)
        m_editTextJongmok = root!!.findViewById(R.id.editTextJongmok)
        jongmokName = root!!.findViewById<TextView>(R.id.textViewName)
        m_editTextQty = root!!.findViewById(R.id.editTextQty)
        m_editTextDanga = root!!.findViewById(R.id.editTextDanga)
        m_textViewJumunBunho = root!!.findViewById(R.id.textViewJumunBunho)


        //계좌번호
        m_combobox = root!!.findViewById(R.id.combo_acc) as Spinner
        var items = getAccountList();
        //val myAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, items)
        val myAdapter = ArrayAdapter(root!!.context, R.layout.spinneritem, items)
        m_combobox.adapter = myAdapter


        m_combobox.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {

                var t = (parent.getChildAt(0) as TextView)
                //t.setTextColor(Color.BLUE)
                //(parent.getChildAt(0) as TextView).textSize = 10f
                t.setTextSize(TypedValue.COMPLEX_UNIT_PX, ResourceManager.calcFontSize(t.textSize.toInt()))

                m_dn = m_combobox.getItemAtPosition(position) as String
                m_editTextAccount!!.setText(m_dn);


                //아이템이 클릭 되면 맨 위부터 position 0번부터 순서대로 동작하게 됩니다.
                when (position) {
                    0 -> {

                    }
                    1 -> {

                    }
                    //...
                    else -> {

                    }


                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {

            }
        }

        //        m_editTextJongmok.addTextChangedListener(new TextWatcher() {
        //            @Override
        //            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //
        //            }
        //            // 입력되는 텍스트에 변화가 있을 때
        //            @Override
        //            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        //                int nLen = m_editTextJongmok.getText().length();
        //                if (nLen == 6) {
        //                    requestData();
        //                }
        //            }
        //
        //            @Override
        //            public void afterTextChanged(Editable editable) {
        //
        //            }
        //        });

//        root!!.findViewById<Button>(R.id.buttoncancel).setOnClickListener(object :
//            View.OnClickListener {
//            override fun onClick(view: View) {
//                //취소
//                OnButtonCancelClicked()
//            }
//        })
//
        root!!.findViewById<Button>(R.id.buttonJumunList)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(view: View) {
                    //주문내역
                    OnButtonJumunListClicked()
                }
            })
//
//        root!!.findViewById<Button>(R.id.buttonMaedo).setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View) {
//                //매도
//                OnButtonMaedoClicked()
//            }
//        })
//
//        root!!.findViewById<Button>(R.id.buttonMaesu2).setOnClickListener(object : View.OnClickListener {
//            override fun onClick(view: View) {
//                //매수
//                OnButtonMaesuClicked()
//            }
//        })
//
//        root!!.findViewById<Button>(R.id.buttonjungjung)
//            .setOnClickListener(object : View.OnClickListener {
//                override fun onClick(view: View) {
//                    //정정
//                    OnButtonJungjungClicked()
//                }
//            })

        // 버튼 누르면 internal.txt에 주문 내역 저장 후 출력
        root!!.findViewById<Button>(R.id.buttonMoMaesu)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    maesumaedo = true   // true: 매수

                    // 주문 내역 사항 정의
                    var inputJongmok: String = m_editTextJongmok!!.text.toString()  // 종목코드
                    var inputQty: String = m_editTextQty!!.text.toString()          // 보유수량
                    var inputName: String = jongmokName!!.text.toString()           // 종목명
                    var inputDanga: String = m_editTextDanga!!.text.toString()      // 매입가

                    manager!!.requestData(m_nHandle, "t1101", inputJongmok, false, "", 30)
                }
            })

        root!!.findViewById<Button>(R.id.buttonMoMaedo)
            .setOnClickListener(object : View.OnClickListener {
                override fun onClick(v: View?) {
                    maesumaedo = false  // false: 매도

                    // 주문 내역 사항 정의
                    var inputJongmok: String = m_editTextJongmok!!.text.toString()  // 종목코드
                    var inputQty: String = m_editTextQty!!.text.toString()          // 보유수량
                    var inputName: String = jongmokName!!.text.toString()           // 종목명
                    var inputDanga: String = m_editTextDanga!!.text.toString()      // 매입가

                    manager!!.requestData(m_nHandle, "t1101", inputJongmok, false, "", 30)
                }
            })

        return root
        //return super.onCreateView(inflater, container, savedInstanceState);
    }

    private fun processT1101(pData: ByteArray) {
        /* 방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
         var map = manager!!.getOutBlockDataFromByte("t1101", "t1101OutBlock", pData!!)
        var pArray = manager.getAttributeFromByte("t1101", "t1101OutBlock", pData) // attribute
         */

        /* 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우  */
        val bAttributeInData = true
        var map = manager!!.getDataFromByte(pData, TRCODE.n1101col, bAttributeInData)
        Log.d("10map값", map!![0]!![0].toString())
        var pArray = manager!!.getAttributeFromByte(pData, TRCODE.n1101col) // attribute

        var inputJongmok: String = m_editTextJongmok!!.text.toString()  // 종목코드
        var inputQty: String = m_editTextQty!!.text.toString()          // 보유수량
        var inputName: String = jongmokName!!.text.toString()           // 종목명
        var inputDanga: String = m_editTextDanga!!.text.toString()      // 매입가

        // s1004에서 갖고 온 코드에서 현재가만 추출해 저장하는 전역변수 : 현재가
        var hyunjae: String = ""
        val templist: ArrayList<Pair<String, String>> = ArrayList()

        if (map != null) {
            for (i in 0..map.size - 1) {
                /*
                실데이터는 getOutBlockDataFromByte 에서 불러온 정보를 이용
                대비구분을 위한 데이터 getAttributeFromByte 에서 불러온 정보를 이용
                map의 index번호는 (TR 구조체의 인덱스넘버를 직접입력 또는 TRCODE.kt에서 선언된 enum class의 ordinal 값 사용 )
                */
                val arrayData: ArrayList<String> = ArrayList()
                TRCODE.T1101.values().forEach {
                    val temp = map[i]?.get(it.ordinal).toString()
                    arrayData.add(temp)
                }

                val price = manager!!.getCommaValue(arrayData.get(TRCODE.T1101.PRICE.ordinal))

                var _priceidx = 0

                for (idx in 0..9) {
                    //매수호가
                    val sub_bidho = (7 + 1) + ((9 - idx) * 6)
                    val sub_bidho_data = arrayData.get(sub_bidho)

                    //매수호가 수량
                    val sub_bidrem = (7 + 3) + ((9 - idx) * 6)
                    val sub_bidrem_data = arrayData.get(sub_bidrem)

                    val _pair: Pair<String, String> = Pair(sub_bidho_data, sub_bidrem_data)
                    templist.add(_pair)
                    if (idx == 9) {
                        Log.d("매수호가", sub_bidho_data)
                        Log.d("매수호가 수량", sub_bidrem_data)
                    }
                    if (idx == 9) {
                        hyunjae = arrayData.get(sub_bidho)
                    }
                }

                for (idx in 0..9) {
                    //매도호가
                    val sub_offerho = (7 + 0) + (idx * 6)
                    val sub_offerho_data = arrayData.get(sub_offerho)

                    //매도호가 수량
                    val sub_offerrem = (7 + 2) + (idx * 6)
                    val sub_offerrem_data = arrayData.get(sub_offerrem)

                    val _pair: Pair<String, String> = Pair(sub_offerho_data, sub_offerrem_data)
                    templist.add(_pair)
                    if (idx == 0) {
                        Log.d("매도호가", sub_offerho_data)
                        Log.d("매도호가 수량", sub_offerrem_data)
                    }
                }

                val shcode = arrayData.get(TRCODE.T1101.SHCODE.ordinal)
                var bOK = manager!!.addRealData(m_nHandle, "H1_", shcode, 6)
                val name = arrayData.get(TRCODE.T1101.HNAME.ordinal)
            }

            // 사려고 하는 가격이 최저가보다 작거나, 최고가보다 높으면 적정 매수가 입력 요망 토스트 띄움
            if (inputDanga.toInt() < templist.get(0).first.toInt() || inputDanga.toInt() > templist.get(19).first.toInt()) {
                Toast.makeText(activity!!.applicationContext, "적정 매수/매도가 입력 요망", Toast.LENGTH_SHORT).show()
                Log.d("메시지!", "적정 매수/매도가 입력 요망")
            // 매수 또는 매도 적정가
            } else {
                for (idx in 0..19) {
                    if (inputDanga.toInt() == templist.get(idx).first.toInt()) {
                        // 매수 희망
                        if (maesumaedo == true) {
                            // 입력한 매수 희망 주 수가 매도 잔량보다 작거나 같을 경우 (매수 가능한 경우)
                            if (inputQty.toInt() <= templist.get(idx).second.toInt()) {
                                buyIt()
                            // 입력한 매수 희망 주 수가 매도 잔량보다 클 경우 (매수 불가능한 경우)
                            } else {
                                Toast.makeText(activity!!.applicationContext, "매도 주문량 부족으로 매수 불가", Toast.LENGTH_SHORT).show()
                                Log.d("메시지!", "매도 주문량 부족으로 매수 불가")
                            }
                        // 매도 희망
                        } else {
                            // 입력한 매도 희망 주 수가 매수 잔량보다 작거나 같을 경우 (매도 가능한 경우)
                            if (inputQty.toInt() <= templist.get(idx).second.toInt()) {
                                sellIt()
                                // 입력한 매도 희망 주 수가 매수 잔량보다 클 경우 (매도 불가능한 경우)
                            } else {
                                Toast.makeText(activity!!.applicationContext, "매수 주문량 부족으로 매도 불가", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
        }
    }

    fun buyIt() {
        var jongmokExistInFile: Boolean = false
        var inputJongmok: String = m_editTextJongmok!!.text.toString()  // 종목코드
        var inputQty: String = m_editTextQty!!.text.toString()          // 보유수량
        var inputName: String = jongmokName!!.text.toString()           // 종목명
        var inputDanga: String = m_editTextDanga!!.text.toString()      // 매입가

        var jsonObject: JSONObject = JSONObject()
        var array: JSONArray = JSONArray()

        // 파일 path, 파일 이름 설정
        val path = context!!.filesDir
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "internal9.txt")

        // internal.txt 파일이 없으면 빈 JSONArray 입력해놓음
        if(!file.exists()) {
            FileOutputStream(file).use {
                it.write(array.toString().toByteArray())
            }
        }

        // internal.txt 읽어옴 (잔고 내역)
        var internalTxt = FileInputStream(file).bufferedReader().use {
            it.readText()
        }
        // 잔고 내역을 internalTxtArray에 저장 (기존 내역임)
        var internalTxtArray: JSONArray = JSONArray(internalTxt)
        Log.d("기존 내역", internalTxtArray.toString())

        for (idx in 0..internalTxtArray.length() - 1) {
            // 매수하려는 주식이 기존에 샀던 종목이면 qty 수 증가시킴 (기존 종목 삭제, qty 증가 후 종목 추가)
            if (inputJongmok == internalTxtArray.getJSONObject(idx).getString("jongmok")) {
                inputQty = (inputQty.toInt() + internalTxtArray.getJSONObject(idx).getString("qty").toInt()).toString()
                jongmokExistInFile = true

                // 기존 종목 삭제, qty 증가 후 종목 추가
                var afterArray = removeJongmok(internalTxtArray, idx)

                jsonObject!!.put("jongmok", inputJongmok)
                jsonObject!!.put("name", inputName)
                jsonObject!!.put("danga", inputDanga)
                jsonObject!!.put("qty", inputQty)
                afterArray.put(jsonObject)
                Log.d("이후 내역", afterArray.toString())

                // internal storage에 저장
                FileOutputStream(file).use {
                    it.write(afterArray.toString().toByteArray())
                }
            }
        }

        // 매수하려는 종목이 기존에 없던 종목이면 바로 종목 추가
        if (jongmokExistInFile == false) {
            jsonObject!!.put("jongmok", inputJongmok)
            jsonObject!!.put("name", inputName)
            jsonObject!!.put("danga", inputDanga)
            jsonObject!!.put("qty", inputQty)

            internalTxtArray.put(jsonObject)
            Log.d("이후 내역", internalTxtArray.toString())

            // internal storage에 저장
            FileOutputStream(file).use {
                it.write(internalTxtArray.toString().toByteArray())
            }
        }

        // data 추가할 때
//        file.appendText("record goes here")

         // interanl storage에 저장된 데이터를 Toast로 출력
        var inputAsString = FileInputStream(file).bufferedReader().use {
            it.readText()
        }
        var outputArray: JSONArray = JSONArray(inputAsString)
        var outputAsString: String? = ""
        for(i in 0 until outputArray.length()) {
            outputAsString += outputArray.getJSONObject(i).getString("jongmok") + " " + outputArray.getJSONObject(i).getString("name") + " " + outputArray.getJSONObject(i).getString("danga") + " " + outputArray.getJSONObject(i).getString("qty") + "개\n"
        }
        outputAsString = "매수 완료\n" + outputAsString

        Toast.makeText(activity!!.applicationContext, outputAsString, Toast.LENGTH_SHORT).show()
    }

    fun sellIt() {
        var jongmokExistInFile: Boolean = false
        var inputJongmok: String = m_editTextJongmok!!.text.toString()  // 종목코드
        var inputQty: String = m_editTextQty!!.text.toString()          // 보유수량
        var inputName: String = jongmokName!!.text.toString()           // 종목명
        var inputDanga: String = m_editTextDanga!!.text.toString()      // 매입가

        var jsonObject: JSONObject = JSONObject()
        var array: JSONArray = JSONArray()

        // 파일 path, 파일 이름 설정
        val path = context!!.filesDir
        val letDirectory = File(path, "LET")
        letDirectory.mkdirs()
        val file = File(letDirectory, "internal9.txt")

        // internal.txt 파일이 없으면 빈 JSONArray 입력해놓음
        if(!file.exists()) {
            FileOutputStream(file).use {
                it.write(array.toString().toByteArray())
            }
        }

        // internal.txt 읽어옴 (잔고 내역)
        var internalTxt = FileInputStream(file).bufferedReader().use {
            it.readText()
        }
        // 잔고 내역을 internalTxtArray에 저장 (기존 내역임)
        var internalTxtArray: JSONArray = JSONArray(internalTxt)
        Log.d("기존 내역", internalTxtArray.toString())

        for (idx in 0..internalTxtArray.length() - 1) {
            var afterArray: JSONArray = JSONArray()
            // 매도하려는 주식이 기존에 샀던 종목이면 qty 수 감소시킴 (기존 종목 삭제, qty 감소 후 종목 추가)
            if (inputJongmok == internalTxtArray.getJSONObject(idx).getString("jongmok")) {
                // 입력한 매도 수량이 갖고 있는 수량보다 작거나 같을 경우 (매도 가능한 경우)
                if (inputQty.toInt() <= internalTxtArray.getJSONObject(idx).getString("qty").toInt()) {
                    inputQty = (internalTxtArray.getJSONObject(idx).getString("qty").toInt() - inputQty.toInt()).toString()
                    jongmokExistInFile = true

                    // 보유 중인 해당 종목을 다 팔았을 경우 -> internal.txt에서 해당 종목 삭제
                    if (inputQty.toInt() == 0) {
                        afterArray = removeJongmok(internalTxtArray, idx)
                    // 매도 이후에도 해당 종목이 남았을 경우 -> 기존 종목 삭제, 매도 이후 잔량으로 추가
                    } else {
                        afterArray = removeJongmok(internalTxtArray, idx)

                        jsonObject!!.put("jongmok", inputJongmok)
                        jsonObject!!.put("name", inputName)
                        jsonObject!!.put("danga", inputDanga)
                        jsonObject!!.put("qty", inputQty)
                        afterArray.put(jsonObject)
                        Log.d("이후 내역", afterArray.toString())
                    }

                    // internal storage에 저장
                    FileOutputStream(file).use {
                        it.write(afterArray.toString().toByteArray())
                    }

                    // interanl storage에 저장된 데이터를 Toast로 출력
                    var inputAsString = FileInputStream(file).bufferedReader().use {
                        it.readText()
                    }
                    var outputArray: JSONArray = JSONArray(inputAsString)
                    var outputAsString: String? = ""
                    for(i in 0 until outputArray.length()) {
                        outputAsString += outputArray.getJSONObject(i).getString("jongmok") + " " + outputArray.getJSONObject(i).getString("name") + " " + outputArray.getJSONObject(i).getString("danga") + " " + outputArray.getJSONObject(i).getString("qty") + "개\n"
                    }
                    outputAsString = "매도 완료\n" + outputAsString

                    Toast.makeText(activity!!.applicationContext, outputAsString, Toast.LENGTH_SHORT).show()
                    Log.d("매도 이후", outputAsString)

                // 입력한 매도 수량이 갖고 있는 수량보다 클 경우 (매도 불가능한 경우)
                } else {
                    Toast.makeText(activity!!.applicationContext, "보유 주식 부족", Toast.LENGTH_SHORT).show()
                    Log.d("메세지!", "매도하려는 주 수가 보유 중인 주 수보다 큽니다")
                    jongmokExistInFile = true
                }
            }
        }

        // 매도하려는 종목이 기존에 없던 종목이면 오류 토스트 띄우기
        if (jongmokExistInFile == false) {
            Toast.makeText(activity!!.applicationContext, "보유 주식 없음", Toast.LENGTH_SHORT).show()
            Log.d("메세지!", "보유 중인 해당 종목이 없습니다")
        }

        // data 추가할 때
//        file.appendText("record goes here")
    }

    fun removeJongmok(gijonArray: JSONArray, index: Int): JSONArray {
        var output: JSONArray = JSONArray()
        for (idx in 0..gijonArray.length() - 1) {
            if (idx != index) {
                output.put(gijonArray.get(idx))
            }
        }
        return output
    }

    //계좌확인
    fun getAccountList() : ArrayList<String>{
        var temp : ArrayList<String> = arrayListOf();

        if(manager!!.isConnect() == false) return arrayListOf();
        var tempList = manager!!.getAccountList();
        var tempSize = manager!!.getAccountCount() as Int;

        for( i in 0.. tempSize-1){
            temp.add(tempList?.get(i)?.get(0) as String);
        }

        return temp;
    }

    override fun onResume() {
        super.onResume()
        /* 화면 갱신시 핸들 재연결 */
        m_nHandle = manager!!.setHandler(mainView!!, (m_handler as Handler?)!!)

        // 실시간 관련 추가 작업
        var bOK = manager!!.addRealData(m_nHandle, "SC0", "",0)
        bOK = manager!!.addRealData(m_nHandle, "SC1", "",0)
        bOK = manager!!.addRealData(m_nHandle, "SC2", "",0)
        bOK = manager!!.addRealData(m_nHandle, "SC3", "",0)
        bOK = manager!!.addRealData(m_nHandle, "SC4", "",0)
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()

        // 실시간 취소 작업
        var bOK = manager!!.deleteRealData(m_nHandle, "SC0", "",0)
        bOK = manager!!.deleteRealData(m_nHandle, "SC1", "",0)
        bOK = manager!!.deleteRealData(m_nHandle, "SC2", "",0)
        bOK = manager!!.deleteRealData(m_nHandle, "SC3", "",0)
        bOK = manager!!.deleteRealData(m_nHandle, "SC4", "",0)



        /* 해당 화면을 사용하지 않을떄 핸들값 삭제 */
        manager!!.deleteHandler(m_nHandle)
    }


    private fun OnButtonMaesuClicked() {
        requestMaemae("2")
    }

    private fun OnButtonMaedoClicked() {
        requestMaemae("1")
    }

    private fun OnButtonJungjungClicked() {
        requestJungjung()
    }

    private fun OnButtonCancelClicked() {
        requestCancel()
    }

    private fun OnButtonJumunListClicked() {
        val nLen = m_editTextJongmok!!.text.length
        if (nLen == 6) {
            requestData()
        }
    }

    // 주식주문
    // 55501035473 , 55551023962
    private fun requestMaemae(strMaemaeGubun: String) {
        // 계좌번호(20) , 입력비밀번호(8) , 종목번호(12) , 주문수량(16) , 주문가(13.2) , 매매구분(1) , 호가유형코드(2) , 신용거래코드(3) , 대출일(8) , 주문조건구분(1)

        var strAccount = m_editTextAccount!!.text.toString()
        //var strJongmok = manager.getStockLongCode(m_editTextJongmok.text.toString())
        var strJongmok = m_strJongmokCode
        var strQty = m_editTextQty!!.text.toString()
        var strDanga = m_editTextDanga!!.text.toString()
        var strHogaCode = "00"          // 보통가

        strAccount = manager!!.makeSpace(strAccount, 20)
        strJongmok = manager!!.makeSpace(strJongmok, 12)

        strQty = manager!!.makeZero(strQty, 16)

        if (strDanga.length == 0)
            strDanga = "0"

        strDanga = String.format("%.2f", java.lang.Double.parseDouble(strDanga))
        strDanga = manager!!.makeZero(strDanga, 13)
        var strPass = manager!!.makeSpace("1785",8);
        //manager.setHeaderInfo(1, "1");
        val strInBlock =
            strAccount + strPass + strJongmok + strQty + strDanga + strMaemaeGubun + strHogaCode + "000" + "        " + "0"
        //int nRqID = manager.requestDataAccount(m_nHandle, "CSPAT00600", strInBlock, 0, 'B', "", false, false, false, false, "", 30);
        val nRqID = manager!!.requestData(m_nHandle, "CSPAT00600", strInBlock, false, "", 30)
    }

    // 주식정정
    // 55501035473 , 55551023962
    private fun requestJungjung() {
        // 원주문번호(10) , 계좌번호(20) , 입력비밀번호(8) , 종목번호(12) , 주문수량(16) , 호가유형코드(2) , 주문조건구분(1) , 주문가(13.2)

        var strJumunBunho = m_textViewJumunBunho!!.text.toString()
        var strAccount = m_editTextAccount!!.text.toString()
        var strJongmok = m_strJongmokCode
        var strQty = m_editTextQty!!.text.toString()
        var strDanga = m_editTextDanga!!.text.toString()
        var strHogaCode = "00"          // 보통가

        strJumunBunho = manager!!.makeZero(strJumunBunho, 10)
        strAccount = manager!!.makeSpace(strAccount, 20)
        strJongmok = manager!!.makeSpace(strJongmok, 12)

        strQty = manager!!.makeZero(strQty, 16)

        if (strDanga.length == 0)
            strDanga = "0"

        strDanga = String.format("%.2f", java.lang.Double.parseDouble(strDanga))
        strDanga = manager!!.makeZero(strDanga, 13)
        var strPass = manager!!.makeSpace("1785",8);
        val strInBlock =
            strJumunBunho + strAccount + strPass + strJongmok + strQty + strHogaCode + "0" + strDanga
        val nRqID = manager!!.requestData(m_nHandle, "CSPAT00700", strInBlock, false, "", 30)
    }

    // 주식취소
    // 55501035473 , 55551023962
    private fun requestCancel() {
        // 원주문번호(10) , 계좌번호(20) , 입력비밀번호(8) , 종목번호(12) , 주문수량(16)

        var strJumunBunho = m_textViewJumunBunho!!.text.toString()
        var strAccount = m_editTextAccount!!.text.toString()
        var strJongmok = m_strJongmokCode
        var strQty = m_editTextQty!!.text.toString()

        strJumunBunho = manager!!.makeZero(strJumunBunho, 10)
        strAccount = manager!!.makeSpace(strAccount, 20)
        strJongmok = manager!!.makeSpace(strJongmok, 12)
        strQty = manager!!.makeZero(strQty, 16)
        var strPass = manager!!.makeSpace("1785",8);
        val strInBlock = strJumunBunho + strAccount + strPass + strJongmok + strQty
        val nRqID = manager!!.requestData(m_nHandle, "CSPAT00800", strInBlock, false, "", 30)
    }

    private fun requestData() {

        // 현재 수신받고 있는 종목의 실시간정보를 삭제한다.
        var bOK = manager!!.deleteRealData(m_nHandle, "S3_", m_strJongmokCode, 6)
        bOK = manager!!.deleteRealData(m_nHandle, "K3_", m_strJongmokCode, 6)
        m_strJongmokCode = m_editTextJongmok!!.text.toString()
        val nRqID = manager!!.requestData(m_nHandle, "t1102", m_strJongmokCode, false, "", 0)
    }


    private fun processT1102(strBlockName: String, pData: ByteArray?) {
        if (strBlockName == "t1102OutBlock") {
            processT1102OutBlock(pData)
        }
    }

    private fun processT1102OutBlock(pData: ByteArray?) {

        val pArray: Array<ByteArray>

        /*
        방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
        map = manager.getOutBlockDataFromByte("t1102", "t1102OutBlock", pData);
        pArray = manager.getAttributeFromByte("t1102", "t1102OutBlock", pData); // attribute
        */

        /* 방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt )를 사용하는 경우  */
        val nColLen = intArrayOf( 20, 8, 1, 8, 6, 12, 8, 8, 8, 8, 12, 12, 8, 6, 8, 6, 8, 6, 8, 8, 8, 8, 6, 6, 6, 12, 8, 5, 3, 3, 6, 6, 8, 8, 8, 8, 6, 6, 3, 3, 6, 6, 8, 8, 8, 8, 6, 6, 3, 3, 6, 6, 8, 8, 8, 8, 6, 6, 3, 3, 6, 6, 8, 8, 8, 8, 6, 6, 3, 3, 6, 6, 8, 8, 8, 8, 6, 6, 12, 12, 6, 12, 12, 6, 6, 6, 12, 12, 8, 8, 8, 8, 8, 12, 12, 8, 2, 8, 12, 8, 10, 12, 12, 12, 12, 13, 10, 12, 12, 12, 12, 13, 7, 7, 7, 7, 7, 10, 10, 10, 12, 10, 6, 3, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 8, 8, 8, 8, 8, 8, 8, 8, 8, 8, 18, 18, 8, 8, 8, 1, 8, 1, 8, 10, 8, 8, 1, 1, 8   )
        val bAttributeInData = true
        val map = manager!!.getDataFromByte(pData!!, nColLen, bAttributeInData)
        assert(map != null)


        jongmokName!!.setText(map!![0]?.get(0))

        val danga = root!!.findViewById<EditText>(R.id.editTextDanga)
        danga.setText(map[0]?.get(1))

    }

    private fun processCSPAT_CFOAT(pData: ByteArray?, TRName: String?) {

        var blockname1  = TRName!! + "OutBlock1"
        val blockname2 = TRName!! + "OutBlock2"
        val map: Array<Array<String>>?
        val pArray: Array<ByteArray>

        /*
        //방법 1. res 폴더에 TR정보가 담긴 *.res 파일이 있는 경우
        map = manager.getOutBlockDataFromByte(TRName, blockname, pData);
        pArray = manager.getAttributeFromByte(TRName, blockname, pData); // attribute
        */

        //방법2. 프로젝트의 TR정보가 담긴 소스(.kt, .java ... *현재 프로젝트의 TRCODE.kt등 )를 사용하는 경우.
        var OutBlockName: Array<String>? = null
        OutBlockName = arrayOf(blockname1, blockname2);

        var OutBlockOccursInfo: BooleanArray? = null
        var OutBlockLenInfo: Array<IntArray>? = null
        var hashmap: HashMap<*, *>? = null

        when (TRName!!) {
            "CSPAT00600" -> {
                // ex CSPAT00600.
                //OutBlockName = arrayOf("CSPAT00600OutBlock1", "CSPAT00600OutBlock2")
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5, 20, 8, 12, 16, 13, 1,2,2,1,1,2,3,8,3,1,6,20,10,10,10,10,10,12,1,1),
                    intArrayOf(5, 10, 9, 2, 2, 9, 9, 16, 10, 10, 10, 16, 16, 16, 16, 16, 40, 40)
                )
            }
            "CSPAT00700" -> {
                // ex CSPAT00700.
                //OutBlockName = arrayOf("CSPAT00700OutBlock1", "CSPAT00700OutBlock2")
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5, 20, 8, 12, 16, 2, 1, 13, 2, 6, 20, 10, 10, 10, 10, 10),
                    intArrayOf(5,10,10,9,2,2,9,2,1,1,3,8,1,1,9,16,1,10,10,10,16,16,16,40,40)
                )
            }
            "CSPAT00800" -> {
                // ex CSPAT00800.
                //OutBlockName = arrayOf("CSPAT00800OutBlock1", "CSPAT00800OutBlock2")
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5, 10, 20, 8, 12, 16, 2, 20, 6, 10, 10, 10, 10, 10),
                    intArrayOf(5, 10, 10, 9, 2, 2, 9, 2, 1, 1, 3, 8, 1, 1, 9, 1, 10, 10, 10, 40, 40)
                )
            }
            //선물옵션 정상주문
            "CFOAT00100"->{
                //OutBlockName = arrayOf("CFOAT00100OutBlock1", "CFOAT00100OutBlock2")
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5,2,20,8,12,1,2,2,2,15,16,2,9,20,10,10,10,10,16,12,9,12,10),
                    intArrayOf(5,10,40,40,50,16,16,16,16,16)
                )

            }
            //선물옵션 정정주문
            "CFOAT00200"->{
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5,2,20,8,12,2,10,2,15,16,2,9,20,10,10,10,10,9,12,10,10),
                    intArrayOf(5,10,40,40,50,16,16,16,16,16)
                )
            }
            //선물옵션 취소주문
            "CFOAT00300"->{
                OutBlockOccursInfo = booleanArrayOf(false, false)
                OutBlockLenInfo = arrayOf(
                    intArrayOf(5,2,20,8,12,2,10,16,2,9,20,10,10,10,10,10,9,12,10,10),
                    intArrayOf(5,10,40,40,50,16,16,16,16,16)
                )
            }
        }
        hashmap = manager!!.getDataFromByte(
            pData!!,
            OutBlockName!!,
            OutBlockOccursInfo!!,
            OutBlockLenInfo!!,
            false,
            "",
            "B"
        )
        val o1 = hashmap!![OutBlockName[0]]
        val o2 = hashmap[OutBlockName[1]]
        // OutBlock별 데이터
        val s1: Array<Array<String>>?
        val s2: Array<Array<String>>?
        s1 = (o1 as Array<Array<String>>?)
        s2 = (o2 as Array<Array<String>>?)
        map = s2;


        if (map != null) {
            val strJumunBunho = map[0][1]
            m_textViewJumunBunho!!.text = strJumunBunho

        }

    }

    // 주문접수
    private fun processSC0(pData: ByteArray) {

        val nColLen = intArrayOf(10,11,8,6,1,1,1,3,8,3,16,2,3,9,16,12,12,3,3,8,1,9,4,1,1,4,4,6,1,18,2,2,2,1,4,4,41,2,2,2,10,11,9,8,12,9,40,16,13,1,2,2,1,1,3,8,1,6,20,10,10,10,10,10,
            1,3,1,3,20,1,2,1,20,10,9,10,9,16,16,16,10,16,1,10,10,10,10,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,16,13,16,16,16,16,16,16,16,16,16)
        val bAttributeInData = false
        val strArray = manager!!.getDataFromByte(pData, nColLen, bAttributeInData)
        val nRowCount = strArray?.size
        val nColCount = nColLen.size
    }

    // 주문체결
    private fun processSC1(pData: ByteArray) {

        val nColLen = intArrayOf(10,11,8,6,1,1,1,3,8,3,16,2,3,9,16,12,12,3,3,8,1,9,4,1,1,4,4,6,1,18,2,2,2,1,4,4,41,2,2,2,3,11,9,40,12,40,10,10,10,16,13,16,13,16,16,16,16,4,10,1,2,
            16,9,12,1,16,16,16,16,13,16,12,12,1,2,3,2,2,8,3,20,3,9,3,20,1,2,7,9,16,16,16,16,16,16,16,16,16,6,20,10,10,10,10,10,16,1,6,1,1,9,9,16,16,16,16,16,16,16,16,13,16,16,16,16,16,16,16,16,16)
        val bAttributeInData = false
        val strArray = manager!!.getDataFromByte(pData, nColLen, bAttributeInData)
        val nRowCount = strArray?.size
        val nColCount = nColLen.size
    }

    // 주문정정
    private fun processSC2(pData: ByteArray) {

        val nColLen = intArrayOf(10,11,8,6,1,1,1,3,8,3,16,2,3,9,16,12,12,3,3,8,1,9,4,1,1,4,4,6,1,18,2,2,2,1,4,4,41,2,2,2,3,11,9,40,12,40,10,10,10,16,13,16,13,16,16,16,16,4,10,1,2,
            16,9,12,1,16,16,16,16,13,16,12,12,1,2,3,2,2,8,3,20,3,9,3,20,1,2,7,9,16,16,16,16,16,16,16,16,16,6,20,10,10,10,10,10,16,1,6,1,1,9,9,16,16,16,16,16,16,16,16,13,16,16,16,16,16,16,16,16,16)
        val bAttributeInData = false
        val strArray = manager!!.getDataFromByte(pData, nColLen, bAttributeInData)
        val nRowCount = strArray?.size
        val nColCount = nColLen.size
    }

    // 주문취소
    private fun processSC3(pData: ByteArray) {

        val nColLen = intArrayOf(10,11,8,6,1,1,1,3,8,3,16,2,3,9,16,12,12,3,3,8,1,9,4,1,1,4,4,6,1,18,2,2,2,1,4,4,41,2,2,2,3,11,9,40,12,40,10,10,10,16,13,16,13,16,16,16,16,4,10,1,2,
            16,9,12,1,16,16,16,16,13,16,12,12,1,2,3,2,2,8,3,20,3,9,3,20,1,2,7,9,16,16,16,16,16,16,16,16,16,6,20,10,10,10,10,10,16,1,6,1,1,9,9,16,16,16,16,16,16,16,16,13,16,16,16,16,16,16,16,16,16)
        val bAttributeInData = false
        val strArray = manager!!.getDataFromByte(pData, nColLen, bAttributeInData)
        val nRowCount = strArray?.size
        val nColCount = nColLen.size
    }

    // 주문거부
    private fun processSC4(pData: ByteArray) {

        val nColLen = intArrayOf(10,11,8,6,1,1,1,3,8,3,16,2,3,9,16,12,12,3,3,8,1,9,4,1,1,4,4,6,1,18,2,2,2,1,4,4,41,2,2,2,3,11,9,40,12,40,10,10,10,16,13,16,13,16,16,16,16,4,10,1,2,
            16,9,12,1,16,16,16,16,13,16,12,12,1,2,3,2,2,8,3,20,3,9,3,20,1,2,7,9,16,16,16,16,16,16,16,16,16,6,20,10,10,10,10,10,16,1,6,1,1,9,9,16,16,16,16,16,16,16,16,13,16,16,16,16,16,16,16,16,16)
        val bAttributeInData = false
        val strArray = manager!!.getDataFromByte(pData, nColLen, bAttributeInData)
        val nRowCount = strArray?.size
        val nColCount = nColLen.size
    }


    companion object {

        private val RECEIVE_INITECHERROR = -6              // initech 핸드세이킹 에러
        private val RECEIVE_PERMISSIONERROR = -5           // 퍼미션취소
        private val RECEIVE_ERROR = -4                     // 일반적인 에러
        private val RECEIVE_SYSTEMERROR = -2               // 서버에서 내려주는 시스템에러
        private val RECEIVE_DATA = 1                       // TR데이타 수신
        private val RECEIVE_REALDATA = 2                   // 실시간데이타 수신
        private val RECEIVE_MSG = 3                        // TR메세지 수신
        private val RECEIVE_RECONNECT = 5              // SOCKET종료후 재연결 완료
        private val RECEIVE_DISCONNECT = -3               // SOCKET이 연결종료된 경우
        private var m_nHandle = -1

        private var m_handler: ProcMessageHandler? = null

        private var m_strJongmokCode = ""

        private var mainView: MainView? = null
    }

}