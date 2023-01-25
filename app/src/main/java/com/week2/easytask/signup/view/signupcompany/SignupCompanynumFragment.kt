package com.week2.easytask.signup.view.signupcompany

import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.week2.easytask.R
import com.week2.easytask.databinding.FragmentSignupcompanyNumBinding
import com.week2.easytask.signup.SignupSingleton
import com.week2.easytask.signup.view.SignupemailFragment

class SignupCompanynumFragment : Fragment() {

    private var _binding : FragmentSignupcompanyNumBinding? = null
    private val binding get() = _binding!!

    private var firstnum = ""
    private var secondnum = ""


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignupcompanyNumBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 법인등록 번호 입력칸 2개 포커싱 이벤트 처리

        binding.etFirstnum.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_login_et_focus)
                binding.tvWarn.visibility = View.INVISIBLE
            } else {
                view.setBackgroundResource(R.drawable.shape_login_et)
            }
        }

        binding.etSecondnum.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                view.setBackgroundResource(R.drawable.shape_login_et_focus)
                binding.tvWarn.visibility = View.INVISIBLE
            } else {
                view.setBackgroundResource(R.drawable.shape_login_et)
            }
        }

        // 인증하기 버튼 활성화 여부

        binding.etFirstnum.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                firstnum = binding.etFirstnum.text.toString()

                if(firstnum.length == 6 && secondnum.length == 7){
                    binding.btnCheck.setBackgroundResource(R.drawable.shape_login_btn_on)
                    binding.btnCheck.setTextColor(Color.parseColor("#FFFFFFFF"))
                    binding.btnCheck.isEnabled = true
                }else{
                    binding.btnCheck.setBackgroundResource(R.drawable.shape_login_btn)
                    binding.btnCheck.setTextColor(Color.parseColor("#D3D7DC"))
                    binding.btnCheck.isEnabled = false
                }
            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })


        binding.etSecondnum.addTextChangedListener(object : TextWatcher{
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                secondnum = binding.etSecondnum.text.toString()

                if(firstnum.length == 6 && secondnum.length == 7){
                    binding.btnCheck.setBackgroundResource(R.drawable.shape_login_btn_on)
                    binding.btnCheck.setTextColor(Color.parseColor("#FFFFFFFF"))
                    binding.btnCheck.isEnabled = true
                }else{
                    binding.btnCheck.setBackgroundResource(R.drawable.shape_login_btn)
                    binding.btnCheck.setTextColor(Color.parseColor("#D3D7DC"))
                    binding.btnCheck.isEnabled = false
                }

            }
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {}
        })



        // 인증하기 버튼 클릭시 이벤트처리

        binding.btnCheck.setOnClickListener {

            // SignupSingleton 에 companyNum 저장

            SignupSingleton.companyNum = firstnum + secondnum

            parentFragmentManager.beginTransaction()
                .replace(R.id.frag_signup_company, SignupCompanyemailFragment())
                .commit()
        }
    }


}