package com.levaeu.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import com.adapter.files.PlacesAdapter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.GetAddressFromLocation;
import com.general.files.GetLocationUpdates;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.OnScrollTouchDelegate;
import com.general.files.StartActProcess;
import com.general.files.UpdateFrequentTask;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.MyScrollView;
import com.view.SelectableRoundedImageView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class SearchLocationActivity extends AppCompatActivity implements PlacesAdapter.setRecentLocClickList, GetAddressFromLocation.AddressFound, GetLocationUpdates.LocationUpdates, OnScrollTouchDelegate {


    MTextView titleTxt;
    GeneralFunctions generalFunc;
    LinearLayout mapLocArea, sourceLocationView, destLocationView;
    ArrayList<HashMap<String, String>> recentLocList = new ArrayList<>();

    JSONArray SourceLocations_arr;
    JSONArray DestinationLocations_arr;
    MTextView placesTxt, recentLocHTxtView;
    String userProfileJson = "";
    String whichLocation = "";
    MTextView mapLocTxt, homePlaceTxt, homePlaceHTxt;
    MTextView workPlaceTxt, workPlaceHTxt;
    LinearLayout homeLocArea, workLocArea;

    ImageView homeActionImgView, workActionImgView;

    MTextView cancelTxt;

    RecyclerView placesRecyclerView;
    EditText searchTxt;

    // MainActivity mainAct;
    ArrayList<HashMap<String, String>> placelist;
    PlacesAdapter placesAdapter;
    ImageView imageCancel;

    MTextView noPlacedata;
    InternetConnection intCheck;

    LinearLayout placearea;
    LinearLayout myLocationarea;
    LinearLayout destinationLaterArea;
    MTextView destHTxtView;

    GetAddressFromLocation getAddressFromLocation;
    GetLocationUpdates getLastLocation;

    double currentLat = 0.0;
    double currentLong = 0.0;

    MTextView mylocHTxtView;

    LinearLayout placesarea;

    private boolean isFirstLocation = true;
    boolean isDriverAssigned;
    MyScrollView dataArea;
    ImageView googleimagearea;

    String session_token = "";
    int MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = 2;
    String currentSearchQuery = "";
    UpdateFrequentTask sessionTokenFreqTask = null;
    ImageView ivRightArrow2, ivRightArrow3;
    String type = "";

    /*Common For Book for Some Else & MultiStopOver point */

    LinearLayout headerArea;
    /* */

    // Book for Some Else View Declaration Start

    RecyclerView contactListRecyclerView;
    LinearLayout contactListArea;
    String meLbl;
    boolean bfseEnabled = false;
    MTextView  nameTxt;
    // Book for Some Else View Declaration End

    // MultiStopOver points declaration Start
    public RecyclerView stopOverPointsRecyclerView;
    MTextView stopOverPointsSubInfoTxt;
    MTextView stopOverPointsInfoTxt;
    public int selectedPos = -1;
    LinearLayout stopOverPointsArea;
    MButton btn_type2;
    int submitBtnId;
    public int subSelectedPos = -1;
    public RecyclerView stopOverPointsSubRecyclerView;
    public int addressLimit = 4;
    public MaterialEditText materialEditText;
    public String iscubejekRental = "";
    boolean isFromMultiStopOver = false;
    boolean multiStopOverPointsEnabled = false;
    int DRIVER_ARRIVED_MIN_TIME_PER_MINUTE;
    public String isRental = "";
    String LBL_PICK_UP_FROM, LBL_STOP_OVER_TXT, LBL_DROP_AT;
    // MultiStopOver points declaration End

    public static void enableDisableView(View view, boolean enabled) {
        view.setEnabled(enabled);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;

            for (int idx = 0; idx < group.getChildCount(); idx++) {
                enableDisableView(group.getChildAt(idx), enabled);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_location);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        isDriverAssigned = getIntent().getBooleanExtra("isDriverAssigned", false);

        intCheck = new InternetConnection(getActContext());

        getLastLocation = new GetLocationUpdates(getActContext(), Utils.LOCATION_UPDATE_MIN_DISTANCE_IN_MITERS, false, this);

        sourceLocationView = (LinearLayout) findViewById(R.id.sourceLocationView);
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        dataArea = (MyScrollView) findViewById(R.id.dataArea);
        dataArea.setTouchDelegate(this);

        mapLocArea = (LinearLayout) findViewById(R.id.mapLocArea);
        mapLocArea.setOnClickListener(new setOnClickList());
        mapLocTxt = (MTextView) findViewById(R.id.mapLocTxt);

        destLocationView = (LinearLayout) findViewById(R.id.destLocationView);
        homePlaceTxt = (MTextView) findViewById(R.id.homePlaceTxt);
        homePlaceHTxt = (MTextView) findViewById(R.id.homePlaceHTxt);
        workPlaceTxt = (MTextView) findViewById(R.id.workPlaceTxt);
        workPlaceHTxt = (MTextView) findViewById(R.id.workPlaceHTxt);
        placesTxt = (MTextView) findViewById(R.id.locPlacesTxt);
        recentLocHTxtView = (MTextView) findViewById(R.id.recentLocHTxtView);
        cancelTxt = (MTextView) findViewById(R.id.cancelTxt);


        cancelTxt.setText(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
        placesarea = (LinearLayout) findViewById(R.id.placesarea);
        placesRecyclerView = (RecyclerView) findViewById(R.id.placesRecyclerView);
        placesRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                Utils.hideKeyboard(getActContext());
            }
        });
        searchTxt = (EditText) findViewById(R.id.searchTxt);
        searchTxt.setHint(generalFunc.retrieveLangLBl("Search", "LBL_Search"));
        cancelTxt.setOnClickListener(new setOnClickList());
        imageCancel = (ImageView) findViewById(R.id.imageCancel);
        noPlacedata = (MTextView) findViewById(R.id.noPlacedata);
        imageCancel.setOnClickListener(new setOnClickList());
        myLocationarea = (LinearLayout) findViewById(R.id.myLocationarea);
        mylocHTxtView = (MTextView) findViewById(R.id.mylocHTxtView);

        homeActionImgView = (ImageView) findViewById(R.id.homeActionImgView);
        workActionImgView = (ImageView) findViewById(R.id.workActionImgView);
        homeLocArea = (LinearLayout) findViewById(R.id.homeLocArea);
        workLocArea = (LinearLayout) findViewById(R.id.workLocArea);
        placearea = (LinearLayout) findViewById(R.id.placearea);
        destinationLaterArea = (LinearLayout) findViewById(R.id.destinationLaterArea);
        destHTxtView = (MTextView) findViewById(R.id.destHTxtView);
        googleimagearea = (ImageView) findViewById(R.id.googleimagearea);

        // Book for Some Else View Initialization Start

        nameTxt = (MTextView) findViewById(R.id.nameTxt);
        contactListRecyclerView = (RecyclerView) (findViewById(R.id.contactListRecyclerView));
        contactListArea = (LinearLayout) (findViewById(R.id.contactListArea));

        headerArea = (LinearLayout) (findViewById(R.id.headerArea));

        // Book for Some Else View Initialization End

        // MultiStopOver Points View Initialization Start
        if (getIntent().hasExtra("iscubejekRental")) {
            iscubejekRental = getIntent().getStringExtra("iscubejekRental");
        }

        if (getIntent().hasExtra("isRental")) {
            isRental = getIntent().getStringExtra("isRental");
        }

        stopOverPointsSubInfoTxt = (MTextView) (findViewById(R.id.stopOverPointsSubInfoTxt));
        stopOverPointsInfoTxt = (MTextView) (findViewById(R.id.stopOverPointsInfoTxt));
        stopOverPointsRecyclerView = (RecyclerView) (findViewById(R.id.stopOverPointsRecyclerView));
        stopOverPointsSubRecyclerView = (RecyclerView) (findViewById(R.id.stopOverPointsSubRecyclerView));
        stopOverPointsArea = (LinearLayout) (findViewById(R.id.stopOverPointsArea));
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());

        // MultiStopOver Points View Initialization End

        ivRightArrow2 = (ImageView) findViewById(R.id.ivRightArrow2);
        ivRightArrow3 = (ImageView) findViewById(R.id.ivRightArrow3);

        if (generalFunc.isRTLmode()) {
            ivRightArrow3.setRotationY(180);
            ivRightArrow2.setRotationY(180);
        }

        homeLocArea.setOnClickListener(new setOnClickList());
        workLocArea.setOnClickListener(new setOnClickList());
        placesTxt.setOnClickListener(new setOnClickList());

        destinationLaterArea.setOnClickListener(new setOnClickList());
        workActionImgView.setOnClickListener(new setOnClickList());
        homeActionImgView.setOnClickListener(new setOnClickList());
        myLocationarea.setOnClickListener(new setOnClickList());

        placelist = new ArrayList<>();
        MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE = GeneralFunctions.parseIntegerValue(2, generalFunc.getJsonValue("MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE", userProfileJson));

        if (getIntent().hasExtra("type")) {
            type = getIntent().getStringExtra("type");
        }

        setLabel();

        setWhichLocationAreaSelected(getIntent().getStringExtra("locationArea"));


        if (getIntent().hasExtra("eSystem")) {
            findViewById(R.id.placearea).setVisibility(View.GONE);
            myLocationarea.setVisibility(View.VISIBLE);
            if (generalFunc.getMemberId().equalsIgnoreCase("")) {
                placearea.setVisibility(View.GONE);
            } else {
                placearea.setVisibility(View.VISIBLE);
            }

            if (!generalFunc.isLocationEnabled()) {
                myLocationarea.setVisibility(View.GONE);

            }
        } else {
            if (generalFunc.getJsonValue("APP_TYPE", userProfileJson).equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                placearea.setVisibility(View.GONE);
                myLocationarea.setVisibility(View.VISIBLE);

            }

        }

        showDestination();

        searchTxt.setOnFocusChangeListener((v, hasFocus) -> {

            if (!hasFocus) {
                Utils.hideSoftKeyboard((Activity) getActContext(), searchTxt);
            } else {
                Utils.showSoftKeyboard((Activity) getActContext(), searchTxt);
            }
        });

        searchTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (currentSearchQuery.equals(s.toString().trim())) {
                    return;
                }

                currentSearchQuery = searchTxt.getText().toString();

                if (s.length() >= MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE) {
                    if (session_token.trim().equalsIgnoreCase("")) {
                        session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                        initializeSessionRegeneration();
                    }

                    placesRecyclerView.setVisibility(View.VISIBLE);

                    if (getIntent().hasExtra("eSystem")) {
                        googleimagearea.setVisibility(View.VISIBLE);
                    }
                    placesarea.setVisibility(View.GONE);
                    getGooglePlaces(currentSearchQuery, -1);
                } else {
                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    googleimagearea.setVisibility(View.GONE);
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                    placesarea.setVisibility(View.VISIBLE);
                    findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);

                    showDestination();
                    showMapLocation();

                    (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

                }
            }
        });

        searchTxt.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                getSearchGooglePlace(v.getText().toString(), -1);
                return true;
            }
            return false;
        });

        showMapLocation();
        placesRecyclerView.setHasFixedSize(true);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        placesRecyclerView.setLayoutManager(mLayoutManager);
        placesRecyclerView.setItemAnimator(new DefaultItemAnimator());

        if (getCallingActivity() != null && getCallingActivity().getClassName().equals(AddAddressActivity.class.getName())) {
            (findViewById(R.id.recentLocationArea)).setVisibility(View.GONE);
        }


        // Book for Some Else Implementation Start
        if (!bfseEnabled) {

            headerArea.setVisibility(View.GONE);
            cancelTxt.setVisibility(View.VISIBLE);
            generalFunc.removeValue(Utils.BFSE_SELECTED_CONTACT_KEY);
        }

        if (bfseEnabled) {

            contactListArea.setOnClickListener(new setOnClickList());
            findViewById(R.id.nameArea).setVisibility(View.VISIBLE);
            headerArea.setVisibility(View.VISIBLE);
            refreshList();
            checkOldSelectedPref(false);

            cancelTxt.setVisibility(View.GONE);
        } else if (multiStopOverPointsEnabled) {
            headerArea.setVisibility(View.VISIBLE);

            findViewById(R.id.nameArea).setVisibility(bfseEnabled ? View.VISIBLE : View.GONE);
        }

        // Book for Some Else Implementation End


        // MultiStopOver Implementation Start

        if (multiStopOverPointsEnabled && getIntent().hasExtra("stopOverPointsList")) {

            Gson gson = new Gson();
            String data1 = getIntent().getStringExtra("stopOverPointsList");




        } else {
            searchTxt.requestFocus();
        }

        LBL_PICK_UP_FROM = generalFunc.retrieveLangLBl("", "LBL_PICK_UP_FROM");
        LBL_STOP_OVER_TXT = generalFunc.retrieveLangLBl("", "LBL_STOP_OVER_TXT");
        LBL_DROP_AT = generalFunc.retrieveLangLBl("", "LBL_DROP_AT");


        showAddAddressArea(false);

        // MultiStopOver Implementation End

    }

    /**
     * @param IsFromBackPress - Show 2 or multi add destination - true/false
     */
    private void showAddAddressArea(boolean IsFromBackPress) {
        if (multiStopOverPointsEnabled) {
            if ((!isFromMultiStopOver || getIntent().hasExtra("isFromStopOver"))) {
                headerArea.setVisibility(View.VISIBLE);
                findViewById(R.id.searchLocArea).setVisibility(View.GONE);

            } else if (isFromMultiStopOver) {
                findViewById(R.id.searchLocArea).setVisibility(View.VISIBLE);
                headerArea.setVisibility(bfseEnabled ? View.VISIBLE : View.GONE);
            }
            String type = IsFromBackPress ? "resetIfallFiled" : "setSelectedPos";
            if (!isFromMultiStopOver || getIntent().hasExtra("isFromStopOver")) {


                    if (getIntent().getStringExtra("locationArea").equalsIgnoreCase("dest")) {
                        subSelectedPos = 1;
                    } else {
                        subSelectedPos = 0;
                    }
                    // Show Main Source & destination


                    if (findViewById(R.id.stopOverSubPointsArea).getHeight() == 0) {
                        LinearLayout.LayoutParams lyParams = (LinearLayout.LayoutParams) findViewById(R.id.stopOverSubPointsArea).getLayoutParams();
                        lyParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;
                        findViewById(R.id.stopOverSubPointsArea).setLayoutParams(lyParams);
                    }

            }

            showDestination();

        }
    }

    /**
     * @param addBlankArray - handle add of Blank Address
     * @param type          - type from which it called
     */
    private void addBlankArray(boolean addBlankArray, String type) {

        boolean allAdded = setIndexPos(type);
        if ((allAdded || addBlankArray) && Utils.checkText(type)) {
            addNewArray(1, type);
        }


        if (!type.equalsIgnoreCase("setSelectedPos")) {

        }


    }

    private void showMapLocation() {
        if (getIntent().hasExtra("hideSetMapLoc")) {
            mapLocArea.setVisibility(View.GONE);
            placesarea.setVisibility(View.GONE);
        } else {
            mapLocArea.setVisibility(View.VISIBLE);
        }

        if (getIntent().hasExtra("eSystem")) {
            mapLocArea.setVisibility(View.GONE);
        }

    }

    /**
     * @param type -> from Which type it came
     * @return
     */
    private boolean setIndexPos(String type) {
        boolean allAdded = true;
        boolean atLease1DestAdded = false;

        boolean resetIfAllFiled = type.equalsIgnoreCase("resetIfallFiled") || type.equalsIgnoreCase("Yes") || type.equalsIgnoreCase("Remove");
        boolean check = type.equalsIgnoreCase("setSelectedPos") || resetIfAllFiled;

//        if (type.equalsIgnoreCase("setSelectedPos") || resetIfAllFiled) {
        boolean isSet = false;


        if (allAdded && check) {
        }


        showButton(atLease1DestAdded);
        return allAdded;
    }


    public void searchSourceOrDest(String text, int selectedPos) {
        if (currentSearchQuery.equals(text.trim())) {
            Logger.d("ShowData", "1");
            return;
        }

        currentSearchQuery = text;

        if (text.length() >= MIN_CHAR_REQ_GOOGLE_AUTO_COMPLETE) {
            if (session_token.trim().equalsIgnoreCase("")) {
                session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis();
                initializeSessionRegeneration();
            }

            placesRecyclerView.setVisibility(View.VISIBLE);
            Logger.d("ShowData", "2");
            if (getIntent().hasExtra("eSystem")) {
                googleimagearea.setVisibility(View.VISIBLE);
            }
            placesarea.setVisibility(View.GONE);
            getGooglePlaces(currentSearchQuery, selectedPos);
        } else {
            if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                placesarea.setVisibility(View.VISIBLE);
            }
            googleimagearea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.GONE);
            noPlacedata.setVisibility(View.GONE);

            findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);

            showDestination();
            showMapLocation();

            (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);
            Logger.d("ShowData", "3");
        }
    }

    public void checkOldSelectedPref(boolean hide) {

        if (generalFunc.containsKey(Utils.BFSE_SELECTED_CONTACT_KEY) && Utils.checkText(generalFunc.retrieveValue(Utils.BFSE_SELECTED_CONTACT_KEY))) {


        } else {
            Gson gson = new Gson();

        }

        Gson gson = new Gson();
        String data1 = generalFunc.retrieveValue(Utils.BFSE_SELECTED_CONTACT_KEY);




    }

    private void showDestination() {
        if (getIntent().getStringExtra("locationArea").equalsIgnoreCase("dest")) {
            if ((getIntent().getStringExtra("type") != null && getIntent().getStringExtra("type").equalsIgnoreCase(Utils.CabGeneralType_Ride) && generalFunc.getJsonValue("APP_DESTINATION_MODE", userProfileJson).equalsIgnoreCase("NONSTRICT") && !isDriverAssigned)) {
                destinationLaterArea.setVisibility(View.VISIBLE);
            }
        }

        if (getIntent().hasExtra("isFromMulti") || getIntent().hasExtra("eFly")) {
            destinationLaterArea.setVisibility(View.GONE);
        }

        if (isFromMultiStopOver) {
            // Show destination handling for multi stop over

        }

    }

    public void getSearchGooglePlace(String input, int position) {
        noPlacedata.setVisibility(View.GONE);

        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY);

        String url = null;

        try {
            String s = input.trim();
            String[] split = s.split("\\s+");
            url = "https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=" + /*input.replace(" ", "%20")*/URLEncoder.encode(input/*.replace(" ", "%20")*/, "utf8") + "&key=" + serverKey + "&inputtype=" + "textquery" + "&fields=" + "photos,formatted_address,name,rating,geometry" +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true";
            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (url == null) {
            return;
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(responseString -> {

            if (generalFunc.getJsonValue("status", responseString).equals("OK")) {
                JSONArray candidatesArr = generalFunc.getJsonArray("candidates", responseString);

                if (input.length() == 0) {
                    placesRecyclerView.setVisibility(View.GONE);
                    noPlacedata.setVisibility(View.GONE);
                    googleimagearea.setVisibility(View.GONE);
                    if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                        placesarea.setVisibility(View.VISIBLE);
                    }
                    Logger.d("ShowData", "4");
                    findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);
                    placesarea.setVisibility(View.VISIBLE);
                    showDestination();
                    showMapLocation();

                    (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

                    return;
                }

                placelist.clear();
                for (int i = 0; i < candidatesArr.length(); i++) {
                    JSONObject item = generalFunc.getJsonObject(candidatesArr, i);

                    if (!generalFunc.getJsonValue("formatted_address", item.toString()).equals("")) {

                        HashMap<String, String> map = new HashMap<String, String>();

                        map.put("main_text", generalFunc.getJsonValueStr("formatted_address", item));
                        map.put("secondary_text", "");
                        map.put("place_id", "");
                        map.put("description", generalFunc.getJsonValueStr("name", item));

                        JSONObject obj = generalFunc.getJsonObject("location", generalFunc.getJsonObject("geometry", item));

                        map.put("lat", generalFunc.getJsonValueStr("lat", obj));
                        map.put("lng", generalFunc.getJsonValueStr("lng", obj));


                        placelist.add(map);
                    }

                }

                if (placelist.size() > 0) {
                    placesarea.setVisibility(View.GONE);

                    findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);
                    placesarea.setVisibility(View.VISIBLE);
                    showDestination();
                    showMapLocation();
                    Logger.d("ShowData", "5");
                    (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

                    if (position != -1) {
                        if (getCallingActivity() != null && getCallingActivity().getClassName().equals(AddAddressActivity.class.getName())) {
                            (findViewById(R.id.recentLocationArea)).setVisibility(View.GONE);
                        } else {
                            (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);
                        }

                        showMapLocation();

                    }


                    placesRecyclerView.setVisibility(View.VISIBLE);
                    googleimagearea.setVisibility(View.VISIBLE);
                    noPlacedata.setVisibility(View.GONE);

                    if (placesAdapter == null) {
                        placesAdapter = new PlacesAdapter(getActContext(), placelist);
                        placesRecyclerView.setAdapter(placesAdapter);
                        placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                    } else {
                        placesAdapter.notifyDataSetChanged();
                    }
                }
            } else if (currentSearchQuery.length() == 0) {
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.GONE);
                if (getIntent().hasExtra("eSystem")) {
                    googleimagearea.setVisibility(View.GONE);
                }
                if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                    placesarea.setVisibility(View.VISIBLE);
                }
                placesarea.setVisibility(View.VISIBLE);
                Logger.d("ShowData", "main 1");
                findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);

                showDestination();
                showMapLocation();

                (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

                return;
            } else if (position != -1) {

                placesarea.setVisibility(View.VISIBLE);
                findViewById(R.id.homeWorkArea).setVisibility(View.GONE);

                if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                    placesarea.setVisibility(View.VISIBLE);
                }
                googleimagearea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.GONE);

                showDestination();
                showMapLocation();

                (findViewById(R.id.recentLocationArea)).setVisibility(View.GONE);
                Logger.d("ShowData", "6");
            } else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }

                String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
                noPlacedata.setText(msg);
                placesarea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.GONE);
                googleimagearea.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);
                Logger.d("ShowData", "6");

            } else {
                Logger.d("ShowData", "7");
                placelist.clear();
                if (placesAdapter != null) {
                    placesAdapter.notifyDataSetChanged();
                }
                String msg = "";
                if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                    msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

                } else {
                    msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

                }

                noPlacedata.setText(msg);
                placesarea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.VISIBLE);

            }

        });
        exeWebServer.execute();
    }

    public boolean isRide() {
        return Utils.checkText(type) && type.equalsIgnoreCase(Utils.CabGeneralType_Ride);
    }

    public void initializeSessionRegeneration() {

        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        sessionTokenFreqTask = new UpdateFrequentTask(170000);
        sessionTokenFreqTask.setTaskRunListener(() -> session_token = Utils.userType + "_" + generalFunc.getMemberId() + "_" + System.currentTimeMillis());

        sessionTokenFreqTask.startRepeatingTask();
    }

    private void setLabel() {
        homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
        workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
        homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
        workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
        mapLocTxt.setText(generalFunc.retrieveLangLBl("Set location on map", "LBL_SET_LOC_ON_MAP"));

        placesTxt.setText(generalFunc.retrieveLangLBl("", "LBL_FAV_LOCATIONS"));
        recentLocHTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_RECENT_LOCATIONS"));

        mylocHTxtView.setText(generalFunc.retrieveLangLBl("I want services at my current location", "LBL_SERVICE_MY_LOCATION_HINT_INFO"));
        destHTxtView.setText(generalFunc.retrieveLangLBl("Enter destination later", "LBL_DEST_ADD_LATER"));
        DRIVER_ARRIVED_MIN_TIME_PER_MINUTE = generalFunc.parseIntegerValue(3, generalFunc.getJsonValue("DRIVER_ARRIVED_MIN_TIME_PER_MINUTE", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON)));

        /*Multi stop Over*/
        multiStopOverPointsEnabled = generalFunc.retrieveValue(Utils.ENABLE_STOPOVER_POINT_KEY).equalsIgnoreCase("Yes") && isRide() && !getIntent().hasExtra("eFly");
        stopOverPointsSubInfoTxt.setText(generalFunc.retrieveLangLBl("As a courtesy to your driver,please limit each stop to 3 minutes or less, otherwise your fare may change.", "LBL_MULTI_STOP_OVER_NOTIFY_TXT"));
        stopOverPointsInfoTxt.setText(generalFunc.retrieveLangLBl("Please keep stops to 3 minutes or less", "LBL_MULTI_STOP_OVER_NOTIFY_TXT1"));
        addressLimit = generalFunc.parseIntegerValue(4, generalFunc.retrieveValue(Utils.MAX_NUMBER_STOP_OVER_POINTS_KEY)) + 1;
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_DONE"));

        /*Book for some one else*/
        meLbl = generalFunc.retrieveLangLBl("", "LBL_CHOOSE_CONTACT_ME_TXT");
        bfseEnabled = generalFunc.retrieveValue(Utils.BOOK_FOR_ELSE_ENABLE_KEY).equalsIgnoreCase("Yes") && isRide();


    }

    @Override
    public void itemRecentLocClick(int position) {
        getSelectAddresLatLong(placelist.get(position).get("place_id"), placelist.get(position).get("description"), placelist.get(position).get("session_token"), placelist.get(position).get("lat"), placelist.get(position).get("lng"));
    }


    public void setWhichLocationAreaSelected(String locationArea) {
        this.whichLocation = locationArea;

        if (locationArea.equals("dest")) {
            destLocationView.setVisibility(View.VISIBLE);
            sourceLocationView.setVisibility(View.GONE);
            getRecentLocations("dest");
            checkPlaces(locationArea);

        } else if (locationArea.equals("source")) {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.VISIBLE);
            getRecentLocations("source");
            checkPlaces(locationArea);
        }


    }

    public void checkPlaces(final String whichLocationArea) {

        String home_address_str = generalFunc.retrieveValue("userHomeLocationAddress");
//        if(home_address_str.equalsIgnoreCase("")){
//            home_address_str = "----";
//        }
        String work_address_str = generalFunc.retrieveValue("userWorkLocationAddress");
//        if(work_address_str.equalsIgnoreCase("")){
//            work_address_str = "----";
//        }

        if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

            homePlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceHTxt.setText("" + home_address_str);
            homePlaceHTxt.setVisibility(View.VISIBLE);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            homeActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOME_PLACE"));
            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && !work_address_str.equalsIgnoreCase("")) {

            workPlaceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setText("" + work_address_str);
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextColor(getResources().getColor(R.color.gray));
            workPlaceHTxt.setVisibility(View.VISIBLE);
//            workPlaceTxt.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, img_edit, null);
            workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));
            workActionImgView.setImageResource(R.mipmap.ic_edit);

        } else {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WORK_PLACE"));
            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceTxt.setText("" + generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (home_address_str != null && home_address_str.equalsIgnoreCase("")) {
            homePlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_HOME_PLACE_TXT"));
            homePlaceTxt.setText("----");

            homePlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            homePlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            homePlaceTxt.setTextColor(Color.parseColor("#909090"));
            homePlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            homePlaceHTxt.setVisibility(View.VISIBLE);
            homeActionImgView.setImageResource(R.mipmap.ic_pluse);
        }

        if (work_address_str != null && work_address_str.equalsIgnoreCase("")) {
            workPlaceHTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_WORK_PLACE_TXT"));
            workPlaceTxt.setText("----");

            workPlaceHTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            workPlaceHTxt.setTextColor(getResources().getColor(R.color.black));

            workPlaceTxt.setTextColor(Color.parseColor("#909090"));
            workPlaceTxt.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);

            workPlaceHTxt.setVisibility(View.VISIBLE);
            workActionImgView.setImageResource(R.mipmap.ic_pluse);
        }
    }

    private void getRecentLocations(final String whichView) {
        final LayoutInflater mInflater = (LayoutInflater)
                getActContext().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        DestinationLocations_arr = generalFunc.getJsonArray("DestinationLocations", userProfileJson);
        SourceLocations_arr = generalFunc.getJsonArray("SourceLocations", userProfileJson);

        if (DestinationLocations_arr != null || SourceLocations_arr != null) {

            if (whichView.equals("dest")) {

                if (destLocationView != null) {
                    destLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < DestinationLocations_arr.length(); i++) {
                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject destLoc_obj = generalFunc.getJsonObject(DestinationLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);

                    final String tEndLat = generalFunc.getJsonValueStr("tEndLat", destLoc_obj);
                    final String tEndLong = generalFunc.getJsonValueStr("tEndLong", destLoc_obj);
                    final String tDaddress = generalFunc.getJsonValueStr("tDaddress", destLoc_obj);

                    recentAddrTxtView.setText(tDaddress);

                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tEndLat);
                    map.put("tLong", tEndLong);
                    map.put("taddress", tDaddress);

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view1 -> {
                        if (whichView != null) {
                            if (whichView.equals("dest")) {

                                double lati = generalFunc.parseDoubleValue(0.0, tEndLat);
                                double longi = generalFunc.parseDoubleValue(0.0, tEndLong);
                                resetOrAddDest(selectedPos, tDaddress, lati, longi, "" + false);

                            }

                        } else {

                        }
                    });
                    destLocationView.addView(view);
                }
            } else {
                if (sourceLocationView != null) {
                    sourceLocationView.removeAllViews();
                    recentLocList.clear();
                }
                for (int i = 0; i < SourceLocations_arr.length(); i++) {

                    final View view = mInflater.inflate(R.layout.item_recent_loc_design, null);
                    JSONObject loc_obj = generalFunc.getJsonObject(SourceLocations_arr, i);

                    MTextView recentAddrTxtView = (MTextView) view.findViewById(R.id.recentAddrTxtView);
                    LinearLayout recentAdapterView = (LinearLayout) view.findViewById(R.id.recentAdapterView);

                    final String tStartLat = generalFunc.getJsonValueStr("tStartLat", loc_obj);
                    final String tStartLong = generalFunc.getJsonValueStr("tStartLong", loc_obj);
                    final String tSaddress = generalFunc.getJsonValueStr("tSaddress", loc_obj);

                    recentAddrTxtView.setText(tSaddress);
                    HashMap<String, String> map = new HashMap<>();
                    map.put("tLat", tStartLat);
                    map.put("tLong", tStartLong);
                    map.put("taddress", tSaddress);

                    recentLocList.add(map);
                    recentAdapterView.setOnClickListener(view12 -> {
                        if (whichView != null) {
                            if (whichView.equals("source")) {

                                double lati = generalFunc.parseDoubleValue(0.0, tStartLat);
                                double longi = generalFunc.parseDoubleValue(0.0, tStartLong);
                                resetOrAddDest(selectedPos, tSaddress, lati, longi, "");

                            }


                        } else {

                        }
                    });
                    sourceLocationView.addView(view);
                }
            }

        } else {
            destLocationView.setVisibility(View.GONE);
            sourceLocationView.setVisibility(View.GONE);
            recentLocHTxtView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onAddressFound(String address, double latitude, double longitude, String geocodeobject) {

        resetOrAddDest(selectedPos, address, latitude, longitude, "" + false);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.ADD_HOME_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");

            generalFunc.storeData("userHomeLocationLatitude", "" + Latitude);
            generalFunc.storeData("userHomeLocationLongitude", "" + Longitude);
            generalFunc.storeData("userHomeLocationAddress", "" + Address);

            homePlaceTxt.setText(Address);
            checkPlaces(whichLocation);


            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);
            resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);


        } else if (requestCode == Utils.ADD_MAP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {

            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");

            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);

            resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);

        } else if (requestCode == Utils.ADD_WORK_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            String Latitude = data.getStringExtra("Latitude");
            String Longitude = data.getStringExtra("Longitude");
            String Address = data.getStringExtra("Address");


            generalFunc.storeData("userWorkLocationLatitude", "" + Latitude);
            generalFunc.storeData("userWorkLocationLongitude", "" + Longitude);
            generalFunc.storeData("userWorkLocationAddress", "" + Address);

            workPlaceTxt.setText(Address);
            checkPlaces(whichLocation);

            double lati = generalFunc.parseDoubleValue(0.0, Latitude);
            double longi = generalFunc.parseDoubleValue(0.0, Longitude);
            resetOrAddDest(data.getIntExtra("pos", -1), Address, lati, longi, "" + false);

        } else if (requestCode == Utils.REQUEST_PICK_CONTACTS) {
            checkOldSelectedPref(true);
        }


    }

    public void getGooglePlaces(String input, int selectedPos) {

        noPlacedata.setVisibility(View.GONE);

        String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY);

        String url = null;
        //   URLEncoder.encode(input.replace(" ", "%20"), "UTF-8")
        String session_token = this.session_token;

        try {

            url = "https://maps.googleapis.com/maps/api/place/autocomplete/json?input=" + URLEncoder.encode(input, "UTF-8") + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true&sessiontoken=" + session_token;

            if (getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                url = url + "&location=" + getIntent().getDoubleExtra("lat", 0.0) + "," + getIntent().getDoubleExtra("long", 0.0) + "&radius=20";
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


        if (url == null) {
            return;
        }
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);

        exeWebServer.setDataResponseListener(responseString -> {

            if (!this.currentSearchQuery.equals(input)) {
                return;
            }
            searchResult(responseString, selectedPos, input);

        });
        exeWebServer.execute();
    }

    private void searchResult(String responseString, int selectedPos, String input) {
        if (generalFunc.getJsonValue("status", responseString).equals("OK")) {
            JSONArray predictionsArr = generalFunc.getJsonArray("predictions", responseString);

            if (currentSearchQuery.length() == 0) {
                placesRecyclerView.setVisibility(View.GONE);
                noPlacedata.setVisibility(View.GONE);
                if (getIntent().hasExtra("eSystem")) {
                    googleimagearea.setVisibility(View.GONE);
                }
                if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                    placesarea.setVisibility(View.VISIBLE);
                }

                Logger.d("ShowData", "41");
                placesarea.setVisibility(View.VISIBLE);
                findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);

                showDestination();
                showMapLocation();

                (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

                return;
            }

            placelist.clear();
            for (int i = 0; i < predictionsArr.length(); i++) {
                JSONObject item = generalFunc.getJsonObject(predictionsArr, i);

                if (!generalFunc.getJsonValue("place_id", item.toString()).equals("")) {

                    HashMap<String, String> map = new HashMap<String, String>();

                    JSONObject structured_formatting = generalFunc.getJsonObject("structured_formatting", item);
                    map.put("main_text", generalFunc.getJsonValueStr("main_text", structured_formatting));
                    map.put("secondary_text", generalFunc.getJsonValueStr("secondary_text", structured_formatting));
                    map.put("place_id", generalFunc.getJsonValueStr("place_id", item));
                    map.put("description", generalFunc.getJsonValueStr("description", item));
                    map.put("session_token", session_token);

                    placelist.add(map);

                }
            }

            if (placelist.size() > 0) {

                if (selectedPos != -1) {
                    Logger.d("ShowData", "51");

                    if (getCallingActivity() != null && getCallingActivity().getClassName().equals(AddAddressActivity.class.getName())) {
                        (findViewById(R.id.recentLocationArea)).setVisibility(View.GONE);
                    } else {
                        (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);
                    }

                    showMapLocation();

                }

                placesarea.setVisibility(View.GONE);
                placesRecyclerView.setVisibility(View.VISIBLE);
                if (getIntent().hasExtra("eSystem")) {
                    googleimagearea.setVisibility(View.VISIBLE);
                }

                noPlacedata.setVisibility(View.GONE);

                if (placesAdapter == null) {
                    placesAdapter = new PlacesAdapter(getActContext(), placelist);
                    placesRecyclerView.setAdapter(placesAdapter);
                    placesAdapter.itemRecentLocClick(SearchLocationActivity.this);

                } else {
                    placesAdapter.notifyDataSetChanged();
                }
            }
        } else if (currentSearchQuery.length() == 0) {
            placesRecyclerView.setVisibility(View.GONE);
            noPlacedata.setVisibility(View.GONE);
            if (getIntent().hasExtra("eSystem")) {
                googleimagearea.setVisibility(View.GONE);
            }

            placesarea.setVisibility(View.VISIBLE);

            Logger.d("ShowData", "main 2");
            findViewById(R.id.homeWorkArea).setVisibility(View.VISIBLE);

            showDestination();
            showMapLocation();

            (findViewById(R.id.recentLocationArea)).setVisibility(View.VISIBLE);

            return;
        } else if (selectedPos != -1) {

            placesarea.setVisibility(View.VISIBLE);
            findViewById(R.id.homeWorkArea).setVisibility(View.GONE);

            if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                placesarea.setVisibility(View.VISIBLE);
            }
            googleimagearea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(!Utils.checkText(input) ? View.VISIBLE : View.GONE);
            noPlacedata.setVisibility(View.GONE);

            showDestination();
            showMapLocation();

            (findViewById(R.id.recentLocationArea)).setVisibility(View.GONE);
            Logger.d("ShowData", "61");

        } else if (generalFunc.getJsonValue("status", responseString).equals("ZERO_RESULTS")) {
            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            Logger.d("ShowData", "71");

            String msg = generalFunc.retrieveLangLBl("We didn't find any places matched to your entered place. Please try again with another text.", "LBL_NO_PLACES_FOUND");
            noPlacedata.setText(msg);
            placesarea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.GONE);
            if (getIntent().hasExtra("eSystem")) {
                googleimagearea.setVisibility(View.GONE);
            }
            noPlacedata.setVisibility(View.VISIBLE);


        } else {
            Logger.d("ShowData", "72");

            placelist.clear();
            if (placesAdapter != null) {
                placesAdapter.notifyDataSetChanged();
            }
            String msg = "";
            if (!intCheck.isNetworkConnected() && !intCheck.check_int()) {
                msg = generalFunc.retrieveLangLBl("No Internet Connection", "LBL_NO_INTERNET_TXT");

            } else {
                msg = generalFunc.retrieveLangLBl("Error occurred while searching nearest places. Please try again later.", "LBL_PLACE_SEARCH_ERROR");

            }

            noPlacedata.setText(msg);
            placesarea.setVisibility(View.GONE);
            placesRecyclerView.setVisibility(View.GONE);
            if (getIntent().hasExtra("eSystem")) {
                googleimagearea.setVisibility(View.GONE);
            }
            noPlacedata.setVisibility(View.VISIBLE);

        }
    }


    public void getSelectAddresLatLong(String Place_id, final String address, String session_token, String lat, String lng) {

        if (lat == null || lat.equalsIgnoreCase("") || lng
                == null || lng.equalsIgnoreCase("")) {
            String serverKey = generalFunc.retrieveValue(Utils.GOOGLE_SERVER_ANDROID_PASSENGER_APP_KEY);


            String url = "https://maps.googleapis.com/maps/api/place/details/json?placeid=" + Place_id + "&key=" + serverKey +
                    "&language=" + generalFunc.retrieveValue(Utils.GOOGLE_MAP_LANGUAGE_CODE_KEY) + "&sensor=true&fields=formatted_address,name,geometry&sessiontoken=" + session_token;

            ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), url, true);
            exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
            exeWebServer.setDataResponseListener(responseString -> {

                if (generalFunc.getJsonValue("status", responseString).equals("OK")) {
                    JSONObject resultObj = generalFunc.getJsonObject("result", responseString);
                    JSONObject geometryObj = generalFunc.getJsonObject("geometry", resultObj);
                    JSONObject locationObj = generalFunc.getJsonObject("location", geometryObj);
                    String latitude = generalFunc.getJsonValueStr("lat", locationObj);
                    String longitude = generalFunc.getJsonValueStr("lng", locationObj);


                    double lati = generalFunc.parseDoubleValue(0.0, latitude);
                    double longi = generalFunc.parseDoubleValue(0.0, longitude);
                    resetOrAddDest(selectedPos, address, lati, longi, "" + false);

                }

            });
            exeWebServer.execute();
        } else if (!lat.equals("") && !lng.equalsIgnoreCase("")) {

            double lati = generalFunc.parseDoubleValue(0.0, lat);
            double longi = generalFunc.parseDoubleValue(0.0, lng);
            resetOrAddDest(selectedPos, address, lati, longi, "" + false);

        }

    }

    @Override
    public void onLocationUpdate(Location mLastLocation) {
        if (isFirstLocation && mLastLocation != null) {
            isFirstLocation = false;
            currentLat = mLastLocation.getLatitude();
            currentLong = mLastLocation.getLongitude();
        }

    }

    public Context getActContext() {
        return SearchLocationActivity.this;
    }

    /*Multi Stop Over Start*/

    private void setAdapterPos(int position) {


        showDestination();

    }

    private void showButton(boolean anyOneDestAdded) {
        if (anyOneDestAdded) {
            btn_type2.setEnabled(true);
            btn_type2.setOnClickListener(new setOnClickList());
            btn_type2.setTextColor(Color.parseColor("#FFFFFF"));
            btn_type2.setBackgroundColor(getActContext().getResources().getColor(R.color.appThemeColor_2));
            btn_type2.setClickable(true);
        } else {
            btn_type2.setEnabled(false);
            btn_type2.setClickable(false);
            btn_type2.setOnClickListener(null);
            btn_type2.setTextColor(Color.parseColor("#FFFFFF"));
            btn_type2.setBackgroundColor(Color.parseColor("#BABABA"));
        }
    }

    @Override
    public void onTouchDelegate() {
        Utils.hideKeyboard(getActContext());
    }
    /*Multi Stop Over End*/

    /**
     * @param noOfBalnkArrayAdd - no of empty address which needs to be add
     * @param type              - type from which it called
     */
    private void addNewArray(int noOfBalnkArrayAdd, String type) {




    }

    /**
     * @param selPos    -> Selected Position if multiple destinations else -1
     * @param address
     * @param latitude
     * @param longitude
     * @param isSkip    -> destination skipped -> true/false
     */
    private void resetOrAddDest(int selPos, String address, double latitude, double longitude, String isSkip) {
        boolean setResult = true;
        Bundle bn = new Bundle();
        bn.putString("Address", address);
        bn.putString("Latitude", "" + latitude);
        bn.putString("Longitude", "" + longitude);
        if (Utils.checkText(isSkip)) {
            bn.putBoolean("isSkip", isSkip.equalsIgnoreCase("true") ? true : false);
        }

        if (isFromMultiStopOver && selPos != -1) {
            selectedPos = selPos;
            setResult = false;

            addBlankArray(false, "Yes");
            showStopOverPointsListArea(true, false);
            return;
        } else if (subSelectedPos != -1 && !isFromMultiStopOver) {

            addBlankArray(false, "");
        }

        if (materialEditText != null) {
            Utils.hideKeyboard(getActContext());
        }

        if (setResult) {
            setResult(bn);
        }

    }

    /**
     * @param bn -> Parse result bundle
     */
    public void setResult(Bundle bn) {
        Utils.hideKeyboard(getActContext());
        if (getIntent().hasExtra("isFromMulti")) {
            bn.putBoolean("isFromMulti", true);
            bn.putInt("pos", getIntent().getIntExtra("pos", -1));
        }

        if (multiStopOverPointsEnabled) {





            if (getIntent().hasExtra("stopOverPointsList")) {
                Gson gson = new Gson();
            }
        }

        Utils.hideKeyboard(this);

        new StartActProcess(getActContext()).setOkResult(bn);


        finish();
    }
    // Find and sort all points distance


    /*Book for Some One Else  Start*/


    public void showContacts() {
        refreshList();
        contactSelection(true);
    }

    private void contactSelection(boolean show) {
        if (show) {
            nameTxt.setVisibility(View.GONE);

            RelativeLayout.LayoutParams lyParams = (RelativeLayout.LayoutParams) contactListArea.getLayoutParams();
            lyParams.height = (int) Utils.getScreenPixelHeight(getActContext());

            contactListArea.setLayoutParams(lyParams);

//            enableDisableView(findViewById(R.id.contentArea), false);

        } else {
            checkOldSelectedPref(false);

            RelativeLayout.LayoutParams lyParams = (RelativeLayout.LayoutParams) contactListArea.getLayoutParams();
            lyParams.height = 0;

            contactListArea.setLayoutParams(lyParams);


//            enableDisableView(findViewById(R.id.contentArea), true);

        }
    }

    /**
     * @param show      -> show multiple destination add area - true/false
     * @param reSetData -> reset RecyclerView
     */
    private void showStopOverPointsListArea(boolean show, boolean reSetData) {

        if (findViewById(R.id.stopOverSubPointsArea).getHeight() != 0) {
            LinearLayout.LayoutParams lyParams = (LinearLayout.LayoutParams) findViewById(R.id.stopOverSubPointsArea).getLayoutParams();
            lyParams.height = 0;
            findViewById(R.id.stopOverSubPointsArea).setLayoutParams(lyParams);
        }
        if (multiStopOverPointsEnabled) {
            if (show && stopOverPointsArea.getHeight() == 0) {
                enableDisableView(findViewById(R.id.contentArea), false);
                RelativeLayout.LayoutParams lyParams = (RelativeLayout.LayoutParams) stopOverPointsArea.getLayoutParams();
                lyParams.height = (int) Utils.getScreenPixelHeight(getActContext());
                stopOverPointsArea.setLayoutParams(lyParams);
                headerArea.setVisibility(View.VISIBLE);
                findViewById(R.id.searchLocArea).setVisibility(View.GONE);
                searchTxt.setText("");
                Utils.hideKeyboard(getActContext());
            } else if (stopOverPointsArea.getHeight() != 0) {
                enableDisableView(findViewById(R.id.contentArea), true);
                RelativeLayout.LayoutParams lyParams = (RelativeLayout.LayoutParams) stopOverPointsArea.getLayoutParams();
                lyParams.height = 0;
                stopOverPointsArea.setLayoutParams(lyParams);
                findViewById(R.id.searchLocArea).setVisibility(isFromMultiStopOver ? View.VISIBLE : View.GONE);
                headerArea.setVisibility(bfseEnabled ? View.VISIBLE : View.GONE);

            }

            showDestination();


            if (reSetData) {



                setIndexPos("resetIfallFiled");
            }


        }
    }


    public void refreshList() {

        Utils.hideKeyboard(getActContext());

        if (generalFunc.containsKey(Utils.BFSE_SELECTED_CONTACT_KEY) && Utils.checkText(generalFunc.retrieveValue(Utils.BFSE_SELECTED_CONTACT_KEY))) {
            Gson gson = new Gson();
            String data1 = generalFunc.retrieveValue(Utils.BFSE_SELECTED_CONTACT_KEY);

        }


    }

    @Override
    public void onBackPressed() {
        if (contactListArea.getHeight() != 0) {
            contactSelection(false);
            return;
        } else if (isFromMultiStopOver && stopOverPointsArea.getHeight() == 0) {
            isFromMultiStopOver = false;
            showAddAddressArea(true);
            return;
        }

        super.onBackPressed();


    }

    /*Book for Some One Else End*/
    @Override
    protected void onDestroy() {
        if (sessionTokenFreqTask != null) {
            sessionTokenFreqTask.stopRepeatingTask();
        }
        super.onDestroy();
    }


    protected void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(getActContext(), android.Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            // show UI part if you want here to show some rationale !!!
            showContacts();

        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.READ_CONTACTS},
                    Utils.REQUEST_READ_CONTACTS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case Utils.REQUEST_READ_CONTACTS: {

                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    showContacts();

                } else {

                    // permission denied,Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

        }
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();

            Bundle bndl = new Bundle();

            if (i == R.id.cancelTxt) {
                onBackPressed();

            } else if (i == R.id.mapLocArea) {
                bndl.putString("locationArea", getIntent().getStringExtra("locationArea"));
                String from = !whichLocation.equals("dest") ? "isPickUpLoc" : "isDestLoc";
                String lati = !whichLocation.equals("dest") ? "PickUpLatitude" : "DestLatitude";
                String longi = !whichLocation.equals("dest") ? "PickUpLongitude" : "DestLongitude";
                String address = !whichLocation.equals("dest") ? "PickUpAddress" : "DestAddress";


                bndl.putString(from, "true");
                if (getIntent().getDoubleExtra("lat", 0.0) != 0.0 && getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                    bndl.putString(lati, "" + getIntent().getDoubleExtra("lat", 0.0));
                    bndl.putString(longi, "" + getIntent().getDoubleExtra("long", 0.0));
                    if (getIntent().hasExtra("address") && Utils.checkText(getIntent().getStringExtra("address"))) {
                        bndl.putString(address, "" + getIntent().getStringExtra("address"));
                    } else {
                        bndl.putString(address, "");
                    }

                } else if (getIntent().getDoubleExtra("lat", 0.0) != 0.0 && getIntent().getDoubleExtra("long", 0.0) != 0.0) {
                    bndl.putString(lati, "" + getIntent().getDoubleExtra("lat", 0.0));
                    bndl.putString(longi, "" + getIntent().getDoubleExtra("long", 0.0));
                    if (getIntent().hasExtra("address") && Utils.checkText(getIntent().getStringExtra("address"))) {
                        bndl.putString(address, "" + getIntent().getStringExtra("address"));
                    } else {
                        bndl.putString(address, "");
                    }

                }


                bndl.putString("IS_FROM_SELECT_LOC", "Yes");

                if (getIntent().hasExtra("isFromMulti")) {
                    bndl.putBoolean("isFromMulti", true);
                    bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                }


                if (getIntent().hasExtra("stopOverPointsList")) {
                    Gson gson = new Gson();
                    bndl.putInt("pos", selectedPos);
                    bndl.putBoolean("isFromStopOver", true);

                }


                if (getIntent().hasExtra("eSystem")) {
                    bndl.putString("eSystem", Utils.eSystem_Type);
                }

                new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                        bndl, Utils.ADD_MAP_LOC_REQ_CODE);


            } else if (i == R.id.locPlacesTxt) {

                //  bottomSheet.performClick();
            } else if (i == R.id.homeLocArea) {

//                if (mpref_place != null) {

                HashMap<String, String> data = new HashMap<>();
                data.put("userHomeLocationAddress", "");
                data.put("userHomeLocationLatitude", "");
                data.put("userHomeLocationLongitude", "");
                data = GeneralFunctions.retrieveValue(data, getActContext());

                final String home_address_str = data.get("userHomeLocationAddress");
                final String home_addr_latitude = data.get("userHomeLocationLatitude");
                final String home_addr_longitude = data.get("userHomeLocationLongitude");

                if (home_address_str != null && !home_address_str.equalsIgnoreCase("")) {

                    if (whichLocation.equals("dest")) {

                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));

                        Bundle bn = new Bundle();
                        bn.putString("Address", home_address_str);
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);
                        bn.putBoolean("isSkip", false);

                        resetOrAddDest(selectedPos, home_address_str, placeLocation.latitude, placeLocation.longitude, "" + false);
                    } else {

                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, home_addr_latitude), generalFunc.parseDoubleValue(0.0, home_addr_longitude));

                        Bundle bn = new Bundle();
                        bn.putString("Address", home_address_str);
                        bn.putString("Latitude", "" + placeLocation.latitude);
                        bn.putString("Longitude", "" + placeLocation.longitude);
                        bn.putBoolean("isSkip", false);

                        resetOrAddDest(selectedPos, home_address_str, placeLocation.latitude, placeLocation.longitude, "" + false);
                    }
                } else {
                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isHome", "true");

                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        if (getIntent().hasExtra("stopOverPointsList")) {
                            Gson gson = new Gson();
                            bndl.putInt("pos", selectedPos);
                            bndl.putBoolean("isFromStopOver", true);
                        }


                        new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }
                /*} else {
                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isHome", "true");

                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }

                        if (getIntent().hasExtra("stopOverPointsList")) {
                            Gson gson = new Gson();
                            String json = gson.toJson(stopOverPointsList);
                            bndl.putString("stopOverPointsList", json);
                            bndl.putInt("pos", selectedPos);
                            bndl.putBoolean("isFromStopOver", true);
                        }


                        new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_HOME_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }*/

            } else if (i == R.id.workLocArea) {

//                if (mpref_place != null) {

                HashMap<String, String> data = new HashMap<>();
                data.put("userWorkLocationAddress", "");
                data.put("userWorkLocationLatitude", "");
                data.put("userWorkLocationLongitude", "");
                data = generalFunc.retrieveValue(data);


                String work_address_str = data.get("userWorkLocationAddress");
                String work_addr_latitude = data.get("userWorkLocationLatitude");
                String work_addr_longitude = data.get("userWorkLocationLongitude");

                if (work_address_str != null && !work_address_str.equalsIgnoreCase("")) {

                    if (whichLocation.equals("dest")) {

                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, work_addr_latitude), generalFunc.parseDoubleValue(0.0, work_addr_longitude));


                        resetOrAddDest(selectedPos, work_address_str, placeLocation.latitude, placeLocation.longitude, "" + false);

                    } else {


                        LatLng placeLocation = new LatLng(generalFunc.parseDoubleValue(0.0, work_addr_latitude), generalFunc.parseDoubleValue(0.0, work_addr_longitude));


                        resetOrAddDest(selectedPos, work_address_str, placeLocation.latitude, placeLocation.longitude, "" + false);

                    }
                } else {

                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isWork", "true");
                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }
                        if (getIntent().hasExtra("stopOverPointsList")) {
                            Gson gson = new Gson();
                            bndl.putInt("pos", selectedPos);
                            bndl.putBoolean("isFromStopOver", true);
                        }

                        new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_WORK_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }
                /*} else {

                    if (intCheck.isNetworkConnected()) {
                        bndl.putString("isWork", "true");
                        if (getIntent().hasExtra("isFromMulti")) {
                            bndl.putBoolean("isFromMulti", true);
                            bndl.putInt("pos", getIntent().getIntExtra("pos", -1));
                        }
                        if (getIntent().hasExtra("stopOverPointsList")) {
                            Gson gson = new Gson();
                            String json = gson.toJson(stopOverPointsList);
                            bndl.putString("stopOverPointsList", json);
                            bndl.putInt("pos", selectedPos);
                            bndl.putBoolean("isFromStopOver", true);
                        }

                        new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                                bndl, Utils.ADD_WORK_LOC_REQ_CODE);
                    } else {
                        generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                    }
                }*/
            } else if (i == R.id.homeActionImgView) {
                if (intCheck.isNetworkConnected()) {
                    Bundle bn = new Bundle();
                    bn.putString("isHome", "true");
                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }
                    if (getIntent().hasExtra("stopOverPointsList")) {
                        Gson gson = new Gson();
                        bn.putInt("pos", selectedPos);
                        bn.putBoolean("isFromStopOver", true);
                    }

                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bn, Utils.ADD_HOME_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            } else if (i == R.id.workActionImgView) {

                if (intCheck.isNetworkConnected()) {
                    Bundle bn = new Bundle();
                    bn.putString("isWork", "true");
                    if (getIntent().hasExtra("isFromMulti")) {
                        bn.putBoolean("isFromMulti", true);
                        bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                    }
                    if (getIntent().hasExtra("stopOverPointsList")) {
                        Gson gson = new Gson();
                        bn.putInt("pos", selectedPos);
                        bn.putBoolean("isFromStopOver", true);
                    }

                    new StartActProcess(getActContext()).startActForResult(SearchPickupLocationActivity.class,
                            bn, Utils.ADD_WORK_LOC_REQ_CODE);
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            } else if (i == R.id.imageCancel) {
                placesRecyclerView.setVisibility(View.GONE);
                if (getIntent().hasExtra("eSystem")) {
                    googleimagearea.setVisibility(View.GONE);
                }
                if (getIntent().getBooleanExtra("isPlaceAreaShow", true)) {
                    placesarea.setVisibility(View.VISIBLE);
                }
                searchTxt.setText("");
                noPlacedata.setVisibility(View.GONE);
            } else if (i == R.id.myLocationarea) {

                if (intCheck.isNetworkConnected()) {
                    if (generalFunc.isLocationEnabled()) {
                        if (currentLat == 0.0 || currentLong == 0.0) {
                            generalFunc.showMessage(myLocationarea, generalFunc.retrieveLangLBl("", "LBL_NO_LOCATION_FOUND_TXT"));

                            return;
                        }

                        getAddressFromLocation = new GetAddressFromLocation(getActContext(), generalFunc);
                        getAddressFromLocation.setLoaderEnable(true);
                        getAddressFromLocation.setLocation(currentLat, currentLong);
                        getAddressFromLocation.setAddressList(SearchLocationActivity.this);
                        getAddressFromLocation.execute();


                    } else {
                        //GPS Dialog
                    }
                } else {
                    generalFunc.showMessage(mapLocArea, generalFunc.retrieveLangLBl("", "LBL_NO_INTERNET_TXT"));
                }
            } else if (i == R.id.destinationLaterArea) {

                Bundle bn = new Bundle();
                bn.putBoolean("isSkip", true);
                if (getIntent().hasExtra("isFromMulti")) {
                    bn.putBoolean("isFromMulti", true);
                    bn.putInt("pos", getIntent().getIntExtra("pos", -1));
                }
                new StartActProcess(getActContext()).setOkResult(bn);
                finish();

            }
            /*Book For SomeOne Else Click Events*/

            else if (i == R.id.backImgView) {
                onBackPressed();
            } else if (i == R.id.contactListArea) {
                contactSelection(false);

            }
            /*Book For SomeOne Else Click Events*/

            /*MultiStopOver Points Click Events start*/
            else if (i == submitBtnId) {
                setResult(new Bundle());
            }
            /*MultiStopOver Points Click Events end*/

        }
    }
}
