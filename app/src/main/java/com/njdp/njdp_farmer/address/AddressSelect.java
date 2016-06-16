package com.njdp.njdp_farmer.address;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.basgeekball.awesomevalidation.AwesomeValidation;
import com.basgeekball.awesomevalidation.ValidationStyle;
import com.njdp.njdp_farmer.PersonalSet;
import com.njdp.njdp_farmer.R;

import com.njdp.njdp_farmer.address.widget.OnWheelChangedListener;
import com.njdp.njdp_farmer.address.widget.OnWheelScrollListener;
import com.njdp.njdp_farmer.address.widget.WheelView;
import com.njdp.njdp_farmer.address.widget.adapters.AbstractWheelTextAdapter;
import com.njdp.njdp_farmer.address.widget.adapters.ArrayWheelAdapter;
import com.njdp.njdp_farmer.mainpages;

/****
 * @docRoot    利用Android Wheel Control 实现的packview风格的三级城市底部弹出菜单
 * @version     1.0  
 * @since       2013\4\15
 * ****/  


public class AddressSelect extends Activity {

	private static final String TAG = "AddressSelect";
    private AwesomeValidation mValidation=new AwesomeValidation(ValidationStyle.BASIC);
    private ImageButton getback=null;
    private EditText select_et;
    private EditText xiang , cun;
	RelativeLayout test_pop_layout;
	int width,height;
    String address[];
    String str_province = null,str_city = null,str_county = null;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
        //设置沉浸模式
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
            window.setNavigationBarColor(Color.TRANSPARENT);
        }
		setContentView(R.layout.activity_address);

        try {
            address = ((String)getIntent().getExtras().get("address")).split("-");
        }catch (Exception e){
            address = new String[0];
        }

        // 获取屏幕的高度和宽度
		Display display = this.getWindowManager().getDefaultDisplay();
		width = display.getWidth();
	    height = display.getHeight();
		
	    // 获取弹出的layout
	    test_pop_layout = (RelativeLayout)findViewById(R.id.top_layout);

        xiang = (EditText)this.findViewById(R.id.xiang);
        xiang.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        cun = (EditText)this.findViewById(R.id.cun);
        cun.setImeOptions(EditorInfo.IME_ACTION_DONE);
		select_et = (EditText) findViewById(R.id.tpop_tv);
		select_et.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				// 显示 popupWindow
				PopupWindow popupWindow = makePopupWindow(AddressSelect.this);
				int[] xy = new int[2];
				test_pop_layout.getLocationOnScreen(xy);
				popupWindow.showAtLocation(test_pop_layout, Gravity.CENTER| Gravity.BOTTOM, 0, -height);
			}
		});

        //将传递过来的地址显示出来
        if(address.length > 4){
            str_province = address[0];
            str_city = address[1];
            str_county = address[2];
            xiang.setText(address[3].substring(0, address[3].length() - 1));
            cun.setText(address[4].substring(0, address[4].length() - 1));
            select_et.setText(address[0] + "-" + address[1] + "-" + address[2]);
        }
        getback=(ImageButton) super.findViewById(R.id.getback);
        //返回上一界面
        getback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button button_edit_ok = (Button) findViewById(R.id.btn_editFinish);
        button_edit_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mValidation.addValidation(AddressSelect.this, R.id.xiang, "^[\\u4e00-\\u9fa5]+$", R.string.err_xiangcun);
                mValidation.addValidation(AddressSelect.this, R.id.cun, "^[\\u4e00-\\u9fa5]+$", R.string.err_xiangcun);
                if (mValidation.validate()) {
                    //返回选择的地市
                    Intent intent = new Intent(AddressSelect.this, PersonalSet.class);
                    String temp = select_et.getText() + "-" + xiang.getText() + "乡-" + cun.getText() + "村";
                    temp = temp.replace("村村", "村");
                    temp = temp.replace("乡乡", "乡");
                    temp = temp.replace("镇乡", "镇");
                    intent.putExtra("address", temp);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
	}

	 
	 // Scrolling flag
    private boolean scrolling = false; 
    private TextView tv;
	// 创建一个包含自定义view的PopupWindow
	private PopupWindow makePopupWindow(Context cx)
	{
		final PopupWindow window;
 		window = new PopupWindow(cx);
 		 
        View contentView = LayoutInflater.from(this).inflate(R.layout.cities_layout, null);
        window.setContentView(contentView);
        
        
        tv = (TextView)contentView.findViewById(R.id.tv_cityName);
        
        final WheelView country = (WheelView) contentView.findViewById(R.id.country);
        country.setVisibleItems(3);
        country.setViewAdapter(new CountryAdapter(this));

        final String cities[][] = AddressData.CITIES;
        final String ccities[][][] = AddressData.COUNTIES;
        final WheelView city = (WheelView) contentView.findViewById(R.id.city);
        city.setVisibleItems(0);
        final WheelView ccity = (WheelView) contentView.findViewById(R.id.ccity);
        ccity.setVisibleItems(0);

        country.addChangingListener(new OnWheelChangedListener() {
            public void onChanged(WheelView wheel, int oldValue, int newValue) {
                if (!scrolling) {
                    updateCities(city, cities, newValue, ccity, ccities);
                }
            }
        });

        tv.setText(AddressData.PROVINCES[country.getCurrentItem()]);
        country.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                updateCities(city, cities, country.getCurrentItem(), ccity, ccities);

            }
        });
        
        // 地区选择
        city.addChangingListener(new OnWheelChangedListener() {
			public void onChanged(WheelView wheel, int oldValue, int newValue) {
			    if (!scrolling) {
			        updatecCities(ccity, ccities, country.getCurrentItem(),newValue); 
			    }
			}
		});
        
        city.addScrollingListener( new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }
            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                updatecCities(ccity, ccities, country.getCurrentItem(), city.getCurrentItem());
            }
        });

        ccity.addScrollingListener(new OnWheelScrollListener() {
            public void onScrollingStarted(WheelView wheel) {
                scrolling = true;
            }

            public void onScrollingFinished(WheelView wheel) {
                scrolling = false;
                try {
                    tv.setText(
                            AddressData.PROVINCES[country.getCurrentItem()] + "-" +
                            AddressData.CITIES[country.getCurrentItem()][city.getCurrentItem()] + "-" +
                            AddressData.COUNTIES[country.getCurrentItem()][city.getCurrentItem()][ccity.getCurrentItem()]);
                }
                catch (Exception exceptin)
                { }
            }
        });

        if(str_province == null || str_city == null || str_county == null || str_province.equals("不限")) {
            country.setCurrentItem(3);
        }else {
            int x = indexArry(AddressData.PROVINCES, str_province);
            int y = indexArry(AddressData.CITIES[x], str_city);
            int z = indexArry(AddressData.COUNTIES[x][y], str_county);
            country.setCurrentItem(x);
            city.setCurrentItem(y);
            ccity.setCurrentItem(z);
        }

        // 点击事件处理
        Button button_ok = (Button) contentView.findViewById(R.id.button_ok);
        button_ok.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                select_et.setText(AddressData.PROVINCES[country.getCurrentItem()] + "-" +
                        AddressData.CITIES[country.getCurrentItem()][city.getCurrentItem()] + "-" +
                        AddressData.COUNTIES[country.getCurrentItem()][city.getCurrentItem()][ccity.getCurrentItem()]);
                str_province = AddressData.PROVINCES[country.getCurrentItem()];
                str_city = AddressData.CITIES[country.getCurrentItem()][city.getCurrentItem()];
                str_county = AddressData.COUNTIES[country.getCurrentItem()][city.getCurrentItem()][ccity.getCurrentItem()];
                window.dismiss(); // 隐藏
            }
        });


        window.setWidth(width);
 		window.setHeight(height/2);
        
		// 设置PopupWindow外部区域是否可触摸
		window.setFocusable(true); //设置PopupWindow可获得焦点
		window.setTouchable(true); //设置PopupWindow可触摸
		window.setOutsideTouchable(true); //设置非PopupWindow区域可触摸
		return window;
	}
	
	 /**
     * Updates the city wheel
     */
    private void updateCities(WheelView city, String cities[][], int index, WheelView ccity, String ccities[][][]) {
        try
        {
            ArrayWheelAdapter<String> adapter =
                    new ArrayWheelAdapter<>(this, cities[index]);
            adapter.setTextSize(18);
            city.setViewAdapter(adapter);
            city.setCurrentItem(cities[index].length / 2);
            updatecCities(ccity, ccities, index, cities[index].length / 2);
        }
        catch (Exception exceptin)
        {}
    }
    
    /**
     * Updates the ccity wheel
     */
    private void updatecCities(WheelView city, String ccities[][][], int index,int index2) {
        try
        {
            ArrayWheelAdapter<String> adapter =
                    new ArrayWheelAdapter<>(this, ccities[index][index2]);
            adapter.setTextSize(18);
            city.setViewAdapter(adapter);
            city.setCurrentItem(ccities[index][index2].length / 2);
            tv.setText(
                    AddressData.PROVINCES[index] + "-" +
                    AddressData.CITIES[index][index2] + "-" +
                    AddressData.COUNTIES[index][index2][ccities[index][index2].length / 2]);
        }
        catch (Exception exceptin)
        {}
    }
    
    /**
     * Adapter for countries
     */
    private class CountryAdapter extends AbstractWheelTextAdapter {
        // Countries names
        private String countries[] = AddressData.PROVINCES;
        /**
         * Constructor
         */
        protected CountryAdapter(Context context) {
            super(context, R.layout.country_layout, NO_RESOURCE);
            
            setItemTextResource(R.id.country_name);
        }

        @Override
        public View getItem(int index, View cachedView, ViewGroup parent) {
            return super.getItem(index, cachedView, parent);
        }
        
        @Override
        public int getItemsCount() {
            return countries.length;
        }
        
        @Override
        protected CharSequence getItemText(int index) {
            return countries[index];
        }
    }

    //查找数组中的位置
    private int indexArry(String[] source, String str){
        int i = -1;
        for(String s : source)
        {
            i++;
            if(str.equals(s)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        View view = findViewById(R.id.top_layout);
        view.setBackgroundResource(0); //释放背景图片
    }

    //不跟随系统变化字体大小
    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config=new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics() );
        return res;
    }

}
