package com.goexpress;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrderCompleted extends AppCompatActivity {

    ImageView imageView;
    Intent it;
    TextView order_id;
    Button relative;
    String ORDER_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_completed);

        it=getIntent();

        imageView = findViewById(R.id.image);
        order_id = findViewById(R.id.order_id);
        relative = findViewById(R.id.click);
        ORDER_ID = it.getStringExtra("order_id");


        order_id.setText("Your order no is : "+ORDER_ID);

        Animation zoomInAnimation = AnimationUtils.loadAnimation(this, R.anim.zoom_in);
        imageView.setAnimation(zoomInAnimation);
        relative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OrderCompleted.this,ViewOrderActivity.class).putExtra("order_id",ORDER_ID));
            }
        });

    }
}
