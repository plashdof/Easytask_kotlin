package com.week2.easytask.login.view

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.AuthErrorCause
import com.kakao.sdk.user.UserApiClient
import com.week2.easytask.CustomToast.showpwChangeToast
import com.week2.easytask.webview.view.MainActivity
import com.week2.easytask.R
import com.week2.easytask.Retrofit
import com.week2.easytask.Singleton
import com.week2.easytask.databinding.ActivityLoginBinding
import com.week2.easytask.login.model.CheckKakaoResponse
import com.week2.easytask.login.model.SigninResponse
import com.week2.easytask.login.model.SigninData
import com.week2.easytask.login.network.CheckKakaoAPI
import com.week2.easytask.login.network.SigninAPI
import com.week2.easytask.signup.SignupSingleton
import com.week2.easytask.signup.view.SignupActivity
import com.week2.easytask.signup.view.SignupkakaoActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity:AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private val SigninRetro = Retrofit.getloginInstance().create(SigninAPI::class.java)
    private val CheckKakaoRetro = Retrofit.getInstance().create(CheckKakaoAPI::class.java)

    private lateinit var sharedPreferences : SharedPreferences

    private var kakaoid = ""

    private var storestate = false
    private var pwstate = false
    private var Id = ""
    private var Pw = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sharedPreferences = getSharedPreferences("test", MODE_PRIVATE)
        val editor: SharedPreferences.Editor = sharedPreferences.edit()


        // ????????? ????????????
        loadID()

        // Id ?????? ????????? login page ??? ????????????, id ?????????????????? login page ??????

        if(intent.hasExtra("email")){
            Id = intent.getStringExtra("email").toString()
            binding.etId.setText(Id)
        }

        // pw ?????? ????????? login page ??? ????????????, Toast message ??????

        if(intent.hasExtra("pwchange")){
            Toast(this)
                .showpwChangeToast ("??????????????? ???????????????.", this)
        }


        
        // Id/Pw ?????? edittext focus ????????? ??????
        // ???????????? invisible ??????
        // ????????? ?????????
        
        binding.etId.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_login_et_focus)
                binding.tvLoginFail.visibility = View.INVISIBLE
                binding.btnEraseId.visibility = View.VISIBLE
            } else {
                view.setBackgroundResource(R.drawable.shape_login_et)
                binding.btnEraseId.visibility = View.INVISIBLE
            }
        }

        binding.etPw.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_login_et_focus)
                binding.tvLoginFail.visibility = View.INVISIBLE
            } else {
                view.setBackgroundResource(R.drawable.shape_login_et)
            }
        }

        // id input icon & pw input icon ????????? ??????
        // ????????? ?????? ??? ?????????
        // ????????? ?????? ??????

        binding.btnEraseId.setOnClickListener {
            Id = ""
            binding.etId.setText("")
        }

        binding.btnShowPw.setOnClickListener {
            if(pwstate){
                binding.etPw.transformationMethod = PasswordTransformationMethod.getInstance()
                binding.etPw.setSelection(Pw.length)
                binding.btnShowPw.setImageResource(R.drawable.login_pw_icon)
                pwstate = false
            }else{
                binding.etPw.transformationMethod = HideReturnsTransformationMethod.getInstance()
                binding.etPw.setSelection(Pw.length)
                binding.btnShowPw.setImageResource(R.drawable.login_pw_icon_on)
                pwstate = true
            }

        }


        // Id/Pw ?????? edittext ?????? String ????????? ??????
        // ?????? ????????????????????? ??????????????? ????????? / ????????? ?????? / ????????? ??????

        binding.etId.addTextChangedListener(object:TextWatcher{

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Id = binding.etId.text.toString()

                if(Id.isNotBlank() && Pw.isNotBlank()){
                    binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn_on)
                    binding.btnLogin.setTextColor(Color.parseColor("#FFFFFFFF"))
                    binding.btnLogin.isEnabled = true
                }else{
                    binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn)
                    binding.btnLogin.setTextColor(Color.parseColor("#D3D7DC"))
                    binding.btnLogin.isEnabled = false
                }

                if(storestate){
                    editor.putString("storedID", Id)
                    editor.apply()
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}

        })

        binding.etPw.addTextChangedListener(object:TextWatcher{


            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                Pw = binding.etPw.text.toString()

                if(Id.isNotBlank() && Pw.isNotBlank()){
                    binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn_on)
                    binding.btnLogin.setTextColor(Color.parseColor("#FFFFFFFF"))
                    binding.btnLogin.isEnabled= true
                }else{
                    binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn)
                    binding.btnLogin.setTextColor(Color.parseColor("#D3D7DC"))
                    binding.btnLogin.isEnabled = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}

        })

        
        // ????????? ?????? checkbox ???????????????
        // ????????? ??????

        binding.btnIdstore.setOnClickListener {

            if(storestate){
                binding.btnIdstore.setImageResource(R.drawable.login_checkoff)
                storestate = false

                editor.remove("storeID")
                editor.apply()

            }else{
                binding.btnIdstore.setImageResource(R.drawable.login_checkon)
                storestate = true

                editor.putString("storedID", Id)
                editor.apply()
            }
        }



        // ??????????????? ?????? ???????????????
        // -> SigninAPI ??????
        // -> response code 200 ?????? ???????????????. ?????? ????????? ??????
        // -> response code 401 ?????? ?????? ?????????
        // -> login btn, ???????????? ????????? ??????
        
        binding.btnLogin.setOnClickListener {

            // API ?????????
            binding.btnLogin.setTextColor(Color.parseColor("#93C3F8"))
            binding.etPw.clearFocus()
            binding.etId.clearFocus()
            binding.btnLogin.isEnabled = false

            val datas = SigninData(username = Id, password = Pw, isKakao = false)

            SigninRetro
                .signin(datas)
                .enqueue(object : Callback<SigninResponse>{
                    override fun onResponse(
                        call: Call<SigninResponse>,
                        response: Response<SigninResponse>
                    ) {
                        Log.d("API??????","${response.code()}")

                        if(response.code() == 200){
                            Log.d("API??????","${response.body()}")
                            Singleton.accessToken = response.body()!!.accessToken
                            Singleton.refreshToken = response.body()!!.refreshToken
                            Singleton.id = response.body()!!.id

                            editor.putString("ID",Singleton.id)
                            editor.putString("accessToken",Singleton.accessToken)
                            editor.putString("refreshToken",Singleton.refreshToken)
                            editor.apply()


                            val intent = Intent(this@LoginActivity, MainActivity::class.java)
                            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                            finish()

                        }else if(response.code() == 401){
                            // API ?????? ???
                            // ?????????. ??????????????? ??????
                            // edittext focus ??????
                            // ???????????? visible ??????

                            binding.tvLoginFail.visibility = View.VISIBLE
                            binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn)
                            binding.btnLogin.setTextColor(Color.parseColor("#D3D7DC"))
                        }

                    }
                    override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                        Log.d("API??????","${t.message}")
                    }
                })
        }

        // ????????? ?????? 
        binding.tvFindId.setOnClickListener {
            val intent = Intent(this, FindidActivity::class.java)
            startActivity(intent)
        }

        // ?????? ??????
        binding.tvFindPw.setOnClickListener {
            val intent = Intent(this,FindpwActivity::class.java)
            startActivity(intent)
        }

        // ???????????? ??????
        binding.tvSignup.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }


        // ????????? ?????? ??????
//        UserApiClient.instance.accessTokenInfo { tokenInfo, error ->
//            if (error != null) {
//                Toast.makeText(this, "?????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show()
//            }
//            else if (tokenInfo != null) {
//                Toast.makeText(this, "?????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show()
//                val intent = Intent(this, MainActivity::class.java)
//                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
//                finish()
//            }
//        }



        // ?????????????????? ?????? ?????? ????????????

        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                when {
                    error.toString() == AuthErrorCause.AccessDenied.toString() -> {
                        Toast.makeText(this, "????????? ?????? ???(?????? ??????)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidClient.toString() -> {
                        Toast.makeText(this, "???????????? ?????? ???", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidGrant.toString() -> {
                        Toast.makeText(this, "?????? ????????? ???????????? ?????? ????????? ??? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidRequest.toString() -> {
                        Toast.makeText(this, "?????? ???????????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.InvalidScope.toString() -> {
                        Toast.makeText(this, "???????????? ?????? scope ID", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Misconfigured.toString() -> {
                        Toast.makeText(this, "????????? ???????????? ??????(android key hash)", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.ServerError.toString() -> {
                        Toast.makeText(this, "?????? ?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    error.toString() == AuthErrorCause.Unauthorized.toString() -> {
                        Toast.makeText(this, "?????? ?????? ????????? ??????", Toast.LENGTH_SHORT).show()
                    }
                    else -> { // Unknown
                        Toast.makeText(this, "?????? ??????", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            else if (token != null) {

                // kakaotalk ??? ?????? ????????????,
                
                UserApiClient.instance.me { user, error ->

                    Log.d("API??????","${user.toString()}")

                    // kakao userid ????????????
                    kakaoid = user?.id.toString()
                    // ?????????????????? ????????? ????????? email ?????? ???????????? ??????
                    SignupSingleton.email = user?.kakaoAccount?.email.toString()

                }

                // kakao userid ??? easytask ??????????????? ??????
                // -> ???????????? ????????? kakao id ??? LoginAPI ??????
                // -> ???????????? ?????? ????????? kakao id ??? SignupKakaoActivity???
                CheckKakaoRetro
                    .checkkakao(kakaoid)
                    .enqueue(object : Callback<CheckKakaoResponse>{
                        override fun onResponse(
                            call: Call<CheckKakaoResponse>,
                            response: Response<CheckKakaoResponse>
                        ) {
                            Log.d("API??????","${response.raw()}")
                            if(response.code() == 200){
                                val datas = SigninData(SignupSingleton.email,"",true)
                                
                                SigninRetro
                                    .signin(datas)
                                    .enqueue(object : Callback<SigninResponse>{
                                        override fun onResponse(
                                            call: Call<SigninResponse>,
                                            response: Response<SigninResponse>
                                        ) {
                                            Log.d("API??????","${response.code()}")

                                            if(response.code() == 200){
                                                Log.d("API??????","${response.body()}")
                                                Singleton.accessToken = response.body()!!.accessToken
                                                Singleton.refreshToken = response.body()!!.refreshToken
                                                Singleton.id = response.body()!!.id

                                                val intent = Intent(this@LoginActivity, MainActivity::class.java)
                                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                                finish()

                                            }else if(response.code() == 401){
                                                // API ?????? ???
                                                // ?????????. ??????????????? ??????
                                                // edittext focus ??????
                                                // ???????????? visible ??????

                                                binding.tvLoginFail.visibility = View.VISIBLE
                                                binding.btnLogin.setBackgroundResource(R.drawable.shape_login_btn)
                                                binding.btnLogin.setTextColor(Color.parseColor("#D3D7DC"))
                                            }

                                        }
                                        override fun onFailure(call: Call<SigninResponse>, t: Throwable) {
                                            Log.d("API??????","${t.message}")
                                        }
                                    })
                            }else{
                                val intent = Intent(this@LoginActivity, SignupkakaoActivity::class.java)
                                startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
                                finish()
                            }
                        }

                        override fun onFailure(call: Call<CheckKakaoResponse>, t: Throwable) {}
                    })


            }
        }
        
        
        // kakao ????????? ??????
        binding.btnKakaoLogin.setOnClickListener {
            if(UserApiClient.instance.isKakaoTalkLoginAvailable(this)){
                UserApiClient.instance.loginWithKakaoTalk(this, callback = callback)
            }else{
                UserApiClient.instance.loginWithKakaoAccount(this, callback = callback)
            }
        }

    }

    // ????????? ????????? ????????? ????????? & edit text focus ??????
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action === MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt())) {
                    v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }

    fun kakaoLogout() {
        // ????????????
        UserApiClient.instance.logout { error ->
            if (error != null) {
                Log.e("Hello", "???????????? ??????. SDK?????? ?????? ?????????", error)
            } else {
                Log.i("Hello", "???????????? ??????. SDK?????? ?????? ?????????")
            }
        }
    }

    fun kakaoUnlink() {
        // ?????? ??????
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                Log.e("Hello", "?????? ?????? ??????", error)
            } else {
                Log.i("Hello", "?????? ?????? ??????. SDK?????? ?????? ?????? ???")
            }
        }
        finish()
    }

    // ????????? ????????????????????? ????????? ????????????
    
    fun loadID(){
        val getSharedID = sharedPreferences.getString("storedID", "ERROR")

        if(getSharedID != "ERROR"){
            binding.etId.setText(getSharedID)
            Id = getSharedID.toString()

            binding.btnIdstore.setImageResource(R.drawable.login_checkon)
            storestate = true
        }
    }


}