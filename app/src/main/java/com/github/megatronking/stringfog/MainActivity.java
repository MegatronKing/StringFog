package com.github.megatronking.stringfog;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.e("ok", "good");
        // 范例请参考：
        // https://github.com/MegatronKing/StringFog-Sample1
        // https://github.com/MegatronKing/StringFog-Sample2
    }

}
