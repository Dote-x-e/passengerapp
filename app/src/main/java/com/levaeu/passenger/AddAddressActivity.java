package com.levaeu.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.model.LatLng;
import com.utils.Utils;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import java.util.HashMap;

public class AddAddressActivity extends AppCompatActivity {

    private static final String TAG = AddAddressActivity.class.getSimpleName();

    GeneralFunctions generalFunc;
    ImageView backImgView;
    MTextView titleTxt;

    MaterialEditText buildingBox;
    MaterialEditText landmarkBox;
    MaterialEditText addrtypeBox;
    MaterialEditText apartmentLocNameBox;
    MaterialEditText companyBox;
    MaterialEditText postCodeBox;
    MaterialEditText addr2Box;
    MaterialEditText deliveryIntructionBox;
    MaterialEditText vContryBox;

    ImageView locationImage;
    String addresslatitude;
    String addresslongitude;
    String address;
    MTextView locAddrTxtView;
    MTextView serviceAddrHederTxtView;
    MTextView AddrareaTxtView;
    MButton btn_type2;
    LinearLayout loc_area;

    String required_str = "";
    String type = "";
    String iUserAddressId;
    String quantity = "0";
    String SelectedVehicleTypeId = "";
    boolean isclick = false;

    String iCompanyId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_address);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        quantity = getIntent().getStringExtra("Quantity");
        SelectedVehicleTypeId = getIntent().getStringExtra("SelectedVehicleTypeId");

        backImgView = (ImageView) findViewById(R.id.backImgView);
        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        loc_area = (LinearLayout) findViewById(R.id.loc_area);

        buildingBox = (MaterialEditText) findViewById(R.id.buildingBox);
        landmarkBox = (MaterialEditText) findViewById(R.id.landmarkBox);
        addrtypeBox = (MaterialEditText) findViewById(R.id.addrtypeBox);
        apartmentLocNameBox = (MaterialEditText) findViewById(R.id.apartmentLocNameBox);
        companyBox = (MaterialEditText) findViewById(R.id.companyBox);
        postCodeBox = (MaterialEditText) findViewById(R.id.postCodeBox);
        addr2Box = (MaterialEditText) findViewById(R.id.addr2Box);
        deliveryIntructionBox = (MaterialEditText) findViewById(R.id.deliveryIntructionBox);
        vContryBox = (MaterialEditText) findViewById(R.id.vContryBox);
        locationImage = (ImageView) findViewById(R.id.locationImage);
        locAddrTxtView = (MTextView) findViewById(R.id.locAddrTxtView);
        serviceAddrHederTxtView = (MTextView) findViewById(R.id.serviceAddrHederTxtView);
        AddrareaTxtView = (MTextView) findViewById(R.id.AddrareaTxtView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type2.setOnClickListener(new setOnClick());
        loc_area.setOnClickListener(new setOnClick());

        if (getIntent().hasExtra("iCompanyId")) {
            iCompanyId=getIntent().getStringExtra("iCompanyId");

            String CURRENT_ADDRESS = generalFunc.retrieveValue(Utils.CURRENT_ADDRESSS);

            if (CURRENT_ADDRESS != null && !CURRENT_ADDRESS.equalsIgnoreCase("")) {
                address = CURRENT_ADDRESS;
                addresslatitude = generalFunc.retrieveValue(Utils.CURRENT_LATITUDE);
                addresslongitude = generalFunc.retrieveValue(Utils.CURRENT_LONGITUDE);
                locAddrTxtView.setText(address);
            } else {
                locAddrTxtView.setText(generalFunc.retrieveLangLBl("", "LBL_SELECT_ADDRESS_TITLE_TXT"));
            }

            if (iCompanyId != null && iCompanyId.equalsIgnoreCase("-1")) {
                addresslatitude = getIntent().getStringExtra("latitude");
                addresslongitude = getIntent().getStringExtra("longitude");
                address = getIntent().getStringExtra("address");
                locAddrTxtView.setText(address);
            }
        } else {
            addresslatitude = getIntent().getStringExtra("latitude");
            addresslongitude = getIntent().getStringExtra("longitude");
            address = getIntent().getStringExtra("address");
            type = getIntent().getStringExtra("type");
        }

        locAddrTxtView.setText(address);

        backImgView.setOnClickListener(new setOnClick());
        locationImage.setOnClickListener(new setOnClick());
        setLabel();
    }

    private void setLabel() {
        titleTxt.setText(generalFunc.retrieveLangLBl("Add New Address", "LBL_ADD_NEW_ADDRESS_TXT"));
        buildingBox.setBothText(generalFunc.retrieveLangLBl("Building/House/Flat No.", "LBL_JOB_LOCATION_HINT_INFO"));
        landmarkBox.setBothText(generalFunc.retrieveLangLBl("Landmark(e.g hospital,park etc.)", "LBL_LANDMARK_HINT_INFO"));
        addrtypeBox.setBothText(generalFunc.retrieveLangLBl("Nickname(optional-home,office etc.)", " LBL_ADDRESSTYPE_HINT_INFO"));
        serviceAddrHederTxtView.setText(generalFunc.retrieveLangLBl("Service address", "LBL_SERVICE_ADDRESS_HINT_INFO"));
        AddrareaTxtView.setText(generalFunc.retrieveLangLBl("Area of service", "LBL_AREA_SERVICE_HINT_INFO"));
        btn_type2.setText(generalFunc.retrieveLangLBl("Save", "LBL_SAVE_ADDRESS_TXT"));
        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
    }

    public void checkValues() {
        boolean buildingDataenterd = Utils.checkText(buildingBox) ? true
                : Utils.setErrorFields(buildingBox, required_str);
        boolean landmarkDataenterd = Utils.checkText(landmarkBox) ? true
                : Utils.setErrorFields(landmarkBox, required_str);

        if (buildingDataenterd == false || landmarkDataenterd == false) {
            return;
        }

        if (!getIntent().hasExtra("iCompanyId")) {
            if (addresslatitude.equalsIgnoreCase("") || addresslatitude.equalsIgnoreCase("0.0") || addresslongitude.equalsIgnoreCase("") || addresslongitude.equalsIgnoreCase("0.0")) {
                generalFunc.showMessage(titleTxt, generalFunc.retrieveLangLBl("", "LBL_SET_LOCATION"));

                return;
            }
        }
        if (!isclick) {
            isclick = true;
            addDeliveryAddr();
        }
    }

    private void addDeliveryAddr() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateUserAddressDetails");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);
        parameters.put("vServiceAddress", address);
        parameters.put("vBuildingNo", Utils.getText(buildingBox));
        parameters.put("vLandmark", Utils.getText(landmarkBox));
        parameters.put("vAddressType", Utils.getText(addrtypeBox));
        parameters.put("vLatitude", addresslatitude);
        parameters.put("vLongitude", addresslongitude);

        if (getIntent().hasExtra("iCompanyId")) {
            parameters.put("iCompanyId", iCompanyId);
        } else {
            parameters.put("iUserAddressId", "");
            parameters.put("iSelectVehicalId", "");
            parameters.put("iSelectVehicalId", SelectedVehicleTypeId);
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);

        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {

                    if (getIntent().hasExtra("iCompanyId")) {
                        generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                        Intent returnIntent = new Intent();
                        returnIntent.putExtra("ToTalAddress", "1");
                        setResult(Activity.RESULT_OK, returnIntent);
                        finish();

                    } else {

                        String userprofileJson = "";


                        generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));

                        userprofileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);


                        String IsProceed = generalFunc.getJsonValue("IsProceed", responseString);

                        if (IsProceed.equalsIgnoreCase("No")) {

                            final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                            generateAlert.setCancelable(false);
                            generateAlert.setBtnClickList(btn_id -> {
                                generateAlert.closeAlertBox();
                                new StartActProcess(getActContext()).setOkResult();
                                backImgView.performClick();

                            });
                            generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("Job Location not allowed", "LBL_JOB_LOCATION_NOT_ALLOWED"));
                            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

                            generateAlert.showAlertBox();
                            return;
                        }

                        iUserAddressId = generalFunc.getJsonValue("AddressId", responseString);

                        if (type.equals(Utils.CabReqType_Later)) {


                            if (generalFunc.getJsonValue("ToTalAddress", userprofileJson).equals("1")) {
                                Bundle bundle = new Bundle();
                                bundle.putString("latitude", addresslatitude);
                                bundle.putString("longitude", addresslongitude);
                                bundle.putString("address", address);
                                bundle.putString("iUserAddressId", iUserAddressId);
                                bundle.putString("SelectvVehicleType", getIntent().getStringExtra("SelectvVehicleType"));
                                bundle.putString("SelectvVehiclePrice", getIntent().getStringExtra("SelectvVehiclePrice"));
                                bundle.putString("iUserAddressId", iUserAddressId);
                                bundle.putString("Quantityprice", getIntent().getStringExtra("Quantityprice"));
                                bundle.putString("Quantity", quantity);
                                bundle.putString("SelectedVehicleTypeId", SelectedVehicleTypeId);
                                bundle.putBoolean("isWalletShow", getIntent().getBooleanExtra("isWalletShow", false));

                                finish();
                            } else {
                                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(btn_id -> {
                                    generateAlert.closeAlertBox();
                                    new StartActProcess(getActContext()).setOkResult();
                                    backImgView.performClick();

                                });
                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str_one, responseString)));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

                                generateAlert.showAlertBox();
                            }

                        } else {


                            if (generalFunc.getJsonValue("ToTalAddress", userprofileJson).equals("1")) {
                                Bundle bundle = new Bundle();
                                bundle.putBoolean("isufx", true);
                                bundle.putString("latitude", addresslatitude);
                                bundle.putString("longitude", addresslongitude);
                                bundle.putString("address", address);
                                bundle.putString("SelectvVehicleType", getIntent().getStringExtra("SelectvVehicleType"));
                                bundle.putString("SelectvVehiclePrice", getIntent().getStringExtra("SelectvVehiclePrice"));
                                bundle.putString("type", Utils.CabReqType_Now);
                                bundle.putString("iUserAddressId", iUserAddressId);
                                bundle.putString("Quantity", quantity);
                                bundle.putString("Quantityprice", getIntent().getStringExtra("Quantityprice"));
                                bundle.putString("SelectedVehicleTypeId", SelectedVehicleTypeId);
                                bundle.putString("Sdate", "");
                                bundle.putString("Stime", "");
                                bundle.putBoolean("isWalletShow", getIntent().getBooleanExtra("isWalletShow", false));
                                new StartActProcess(getActContext()).startActWithData(MainActivity.class, bundle);
                                finish();
                            } else {
                                final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                                generateAlert.setCancelable(false);
                                generateAlert.setBtnClickList(btn_id -> {
                                    generateAlert.closeAlertBox();


                                    new StartActProcess(getActContext()).setOkResult();
                                    backImgView.performClick();

                                });
                                generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str_one, responseString)));
                                generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));

                                generateAlert.showAlertBox();
                            }
                        }
                    }

                } else {
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return AddAddressActivity.this;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Utils.SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            Place place = PlaceAutocomplete.getPlace(getActContext(), data);
            LatLng placeLocation = place.getLatLng();
            locAddrTxtView.setText(place.getAddress().toString());
            addresslatitude = placeLocation.latitude + "";
            addresslongitude = placeLocation.longitude + "";
            address = place.getAddress().toString();
        } else if (requestCode == Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE && resultCode == RESULT_OK && data != null) {
            address = data.getStringExtra("Address");
            locAddrTxtView.setText(address);
            addresslatitude = data.getStringExtra("Latitude") == null ? "0.0" : data.getStringExtra("Latitude");
            addresslongitude = data.getStringExtra("Longitude") == null ? "0.0" : data.getStringExtra("Longitude");
        }
    }

    public class setOnClick implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.backImgView) {
                AddAddressActivity.super.onBackPressed();
            } else if (i == R.id.loc_area) {

                Bundle bn = new Bundle();
                bn.putString("locationArea", "source");
                bn.putBoolean("isaddressview", true);
                if (getIntent().hasExtra("iCompanyId")) {
                    bn.putString("eSystem", Utils.eSystem_Type);
                }

                new StartActProcess(getActContext()).startActForResult(SearchLocationActivity.class, bn, Utils.UBER_X_SEARCH_PICKUP_LOC_REQ_CODE);

            } else if (i == locationImage.getId()) {
                loc_area.performClick();
            } else if (i == btn_type2.getId()) {
                checkValues();
            }
        }
    }
}
