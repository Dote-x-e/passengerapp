package com.levaeu.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.adapter.files.ViewPagerAdapter;
import com.fragments.BookingFragment;
import com.fragments.HistoryFragment;
import com.fragments.MainHeaderFragment;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MTextView;
import com.view.MaterialTabs;

import java.util.ArrayList;
import java.util.HashMap;

public class HistoryActivity extends AppCompatActivity {

    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    String userProfileJson;
    CharSequence[] titles;
    String app_type = "Ride";
    boolean isrestart = false;
    ImageView filterImageview;
    int selTabPos = 0;
    ArrayList<HashMap<String, String>> filterlist;
    android.support.v7.app.AlertDialog list_type;
    boolean isShow = false;
    public String selFilterType = "";
    ArrayList<Fragment> fragmentList = new ArrayList<>();

    ViewPager appLogin_view_pager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());

        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        isrestart = getIntent().getBooleanExtra("isrestart", false);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        filterImageview = (ImageView) findViewById(R.id.filterImageview);
        filterImageview.setOnClickListener(new setOnClickList());
        backImgView.setOnClickListener(new setOnClickList());
        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        setLabels();

        appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
        MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);


        if (generalFunc.getJsonValue("RIDE_LATER_BOOKING_ENABLED", userProfileJson).equalsIgnoreCase("Yes")) {

            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
            material_tabs.setVisibility(View.VISIBLE);
            fragmentList.add(generateHistoryFrag(Utils.Past));
            fragmentList.add(generateBookingFrag(Utils.Upcoming));
        } else {
            titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"),};
            material_tabs.setVisibility(View.GONE);
            fragmentList.add(generateHistoryFrag(Utils.Past));
        }
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
        appLogin_view_pager.setAdapter(adapter);
        material_tabs.setViewPager(appLogin_view_pager);

        if (isrestart) {
            appLogin_view_pager.setCurrentItem(1);
        }


        appLogin_view_pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                Logger.d("onPageScrolled", "::" + position);

            }

            @Override
            public void onPageSelected(int position) {
                selTabPos = position;
                Logger.d("onPageScrolled", "::" + "onPageSelected");
                fragmentList.get(position).onResume();
                selFilterType = "";
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                Logger.d("onPageScrolled", "::" + "onPageScrollStateChanged");
            }
        });
    }


    public void filterManage(ArrayList<HashMap<String, String>> filterlist) {

        this.filterlist = filterlist;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                filterImageview.setVisibility(View.VISIBLE);
            }
        });

    }

    public HistoryFragment generateHistoryFrag(String bookingType) {
        HistoryFragment frag = new HistoryFragment();
        Bundle bn = new Bundle();
        bn.putString("HISTORY_TYPE", "getRideHistory");
        frag.setArguments(bn);

        return frag;
    }

    public BookingFragment getBookingFrag() {

        if (bookingFragment != null) {
            return bookingFragment;
        }
        return null;
    }

    BookingFragment bookingFragment = null;

    public BookingFragment generateBookingFrag(String bookingType) {
        BookingFragment frag = new BookingFragment();
        Bundle bn = new Bundle();
        bn.putString("BOOKING_TYPE", bookingType);

        bookingFragment = frag;

        frag.setArguments(bn);


        return frag;
    }

    public void setLabels() {
        String menuMsgYourTrips = "";
        if (app_type.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_TRIPS");
        } else if (app_type.equalsIgnoreCase("Delivery")) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_DELIVERY");
        } else if (app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
        } else {
            menuMsgYourTrips = generalFunc.retrieveLangLBl("", "LBL_YOUR_BOOKING");
        }
        titleTxt.setText(menuMsgYourTrips);
    }


    @Override
    protected void onResume() {

        app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
        super.onResume();


    }

    public Context getActContext() {
        return HistoryActivity.this;
    }

    @Override
    public void onBackPressed() {
        if (isrestart) {
            Bundle bn = new Bundle();

            if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equals(Utils.CabGeneralType_UberX)) {
                new StartActProcess(getActContext()).startActWithData(UberXActivity.class, bn);
            } else if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase("Delivery")) {
                bn.putString("iVehicleCategoryId", generalFunc.getJsonValue("DELIVERY_CATEGORY_ID", userProfileJson));
                bn.putString("vCategory", generalFunc.getJsonValue("DELIVERY_CATEGORY_NAME", userProfileJson));


                if (generalFunc.getJsonValue("PACKAGE_TYPE", userProfileJson).equalsIgnoreCase("STANDARD")) {
                    new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);
                } else {

                }

            } else if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery)) {
                bn.putString("iVehicleCategoryId", generalFunc.getJsonValue("DELIVERY_CATEGORY_ID", userProfileJson));
                bn.putString("vCategory", generalFunc.getJsonValue("DELIVERY_CATEGORY_NAME", userProfileJson));

            } else {
                if (getIntent().getStringExtra("selType") != null) {
                    bn.putString("selType", getIntent().getStringExtra("selType"));
                    new StartActProcess(getActContext()).startActWithData(UberXActivity.class, bn);
                } else {
                    new StartActProcess(getActContext()).startActWithData(MainActivity.class, bn);
                }

            }
            finishAffinity();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == Activity.RESULT_OK) {

            if (data.getBooleanExtra("isList", false)) {
                return;
            }

            ViewPager appLogin_view_pager = (ViewPager) findViewById(R.id.appLogin_view_pager);
            MaterialTabs material_tabs = (MaterialTabs) findViewById(R.id.material_tabs);

            userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            app_type = generalFunc.getJsonValue("APP_TYPE", userProfileJson);
            ArrayList<Fragment> fragmentList = new ArrayList<>();

            if (app_type.equalsIgnoreCase("Ride-Delivery")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_RIDE"), generalFunc.retrieveLangLBl("", "LBL_DELIVER")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Ride));
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Deliver));
            } else if (app_type.equalsIgnoreCase("Delivery")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_DELIVER")};
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Deliver));
                material_tabs.setVisibility(View.GONE);
            } else if (app_type.equalsIgnoreCase("UberX")) {
//                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_UBERX")};
//                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_UberX));
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.Past));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));
                material_tabs.setVisibility(View.VISIBLE);
            } else if (app_type.equalsIgnoreCase("Ride")) {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_RIDE")};
                fragmentList.add(generateHistoryFrag(Utils.CabGeneralType_Ride));
                material_tabs.setVisibility(View.GONE);
            } else {
                titles = new CharSequence[]{generalFunc.retrieveLangLBl("", "LBL_PAST"), generalFunc.retrieveLangLBl("", "LBL_UPCOMING")};
                material_tabs.setVisibility(View.VISIBLE);
                fragmentList.add(generateHistoryFrag(Utils.Past));
                fragmentList.add(generateBookingFrag(Utils.Upcoming));

            }

            ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager(), titles, fragmentList);
            appLogin_view_pager.setAdapter(adapter);
            material_tabs.setViewPager(appLogin_view_pager);


        }
    }


    public void BuildType() {

        ArrayList<String> typeNameList = new ArrayList<>();
        for (int i = 0; i < filterlist.size(); i++) {
            typeNameList.add((filterlist.get(i).get("vTitle")));
        }
        CharSequence[] cs_currency_txt = typeNameList.toArray(new CharSequence[typeNameList.size()]);
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("Select Type", "LBL_SELECT_TYPE"));
        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection
                if (list_type != null) {
                    list_type.dismiss();
                }
                selFilterType = filterlist.get(item).get("vFilterParam");
//                fragmentList.get(selTabPos).onResume();
                fragmentList.get(appLogin_view_pager.getCurrentItem()).onResume();
            }
        });

        list_type = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_type);
        }

        list_type.show();


    }


    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            switch (view.getId()) {
                case R.id.backImgView:
                    onBackPressed();
                    break;

                case R.id.filterImageview:

                    BuildType();
                    break;

            }
        }
    }


}
