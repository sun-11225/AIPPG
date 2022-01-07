package com.viatom.bloodoxygendemo.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.viatom.bloodoxygendemo.MainActivity
import com.viatom.bloodoxygendemo.R
import com.viatom.bloodoxygendemo.data.entity.UserEntity
import com.viatom.bloodoxygendemo.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    lateinit var binding : ActivityWelcomeBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_welcome)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_welcome)
        binding.btnSignin.setOnClickListener {
            if (binding.inputEmail.text.toString() == "viatom" && binding.inputPassword.text.toString() == ("888888")) {
                Intent(this, MainActivity::class.java).let {
                    Bundle().apply {
                        putParcelable(
                            "userEntity",
                            UserEntity(1001, "小明", "188", "78", "1992-9-23", "男")
                        )
                        it.putExtras(this)
                    }
                    startActivity(it)
                }
            }else{
                Toast.makeText(this,"账户或密码不能为空",Toast.LENGTH_SHORT).show()
            }
        }

    }
}