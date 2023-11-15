package com.nhtthuan.trackingbus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.nhtthuan.trackingbus.Model.Bus;

public class DetailBusActivity extends AppCompatActivity {

    TextView txtMa, txtTenxe, txtXinghiep, txtGiave, txtTanxuat;
    TextView txtThoigian, txtChieudi, txtChieuve, txtSoxe;

    Bus bus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_bus);

        initView();
        bus = getIntent().getParcelableExtra("xebus");

        txtMa.setText(bus.getBuscode());
        txtTenxe.setText(bus.getBusname());
        txtXinghiep.setText(bus.getEnterprise());
        txtTanxuat.setText(bus.getFrequency());
        txtGiave.setText(bus.getTicketprice());
        txtSoxe.setText(bus.getQuantityofbus());
        txtThoigian.setText(bus.getOperatingtime());
        txtChieudi.setText(bus.getBusitinerary());
        txtChieuve.setText(bus.getReturnbusitinerary());

    }

    private void initView() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Thông tin tuyến bus");

        txtMa = (TextView) findViewById(R.id.txtMa);
        txtTenxe = (TextView) findViewById(R.id.txtTenxe);
        txtXinghiep = (TextView) findViewById(R.id.txtXinghiep);
        txtGiave = (TextView) findViewById(R.id.txtGiave);
        txtTanxuat = (TextView) findViewById(R.id.txtTanxuat);
        txtThoigian = (TextView) findViewById(R.id.txtThoigian);
        txtSoxe = (TextView) findViewById(R.id.txtSoxe);
        txtChieudi = (TextView) findViewById(R.id.txtChieudi);
        txtChieuve = (TextView) findViewById(R.id.txtChieuve);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bus, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();

        if (itemId == android.R.id.home) {
            onBackPressed();
        } else if (itemId == R.id.actionMaps) {
            Intent intent = new Intent(this, MapsBusActivity.class);
            intent.putExtra("bus", bus);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
