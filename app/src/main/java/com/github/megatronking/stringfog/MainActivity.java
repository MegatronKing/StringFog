package com.github.megatronking.stringfog;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView)findViewById(R.id.text)).setText("你好");
        // 范例请参考：
        // https://github.com/MegatronKing/StringFog-Sample1
        // https://github.com/MegatronKing/StringFog-Sample2
    }

}
