package com.levaeu.passenger;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.general.files.CustomHorizontalScrollView;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.MyApp;
import com.general.files.OnSwipeTouchListener;
import com.general.files.SetOnTouchList;
import com.general.files.SlideAnimationUtil;
import com.kyleduo.switchbutton.SwitchButton;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.DividerView;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;
import com.view.simpleratingbar.SimpleRatingBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;

public class RatingActivity extends AppCompatActivity {

    String vehicleIconPath = CommonUtilities.SERVER_URL + "webimages/icons/VehicleType/";

    MTextView titleTxt;
    ImageView backImgView;
    DividerView desdashImage;

    GeneralFunctions generalFunc;

    ProgressBar loading;
    ErrorView errorView;
    MButton btn_type2;
    MTextView generalCommentTxt;
    CardView generalCommentArea;
    MaterialEditText commentBox;

    int submitBtnId;

    LinearLayout container;

    SimpleRatingBar ratingBar;
    String iTripId_str;
    LinearLayout uberXRatingLayoutArea, mainRatingArea;
    android.support.v7.app.AlertDialog giveTipAlertDialog;

    MTextView totalFareTxt;
    MTextView dateVTxt;
    MTextView promoAppliedVTxt;
    MTextView ratingHeaderTxt;
    float rating = 0;
    boolean isAnimated = false;

    String tipamount = "";
    boolean isCollectTip = false;

    boolean isUfx = false;
    boolean isFirst = false;

    MTextView fareHeadrtxt;
    ImageView fareindicatorImg;
    LinearLayout farecontainer;

    MTextView walletNoteTxt;
    String userProfileJson;

    AVLoadingIndicatorView loaderView;
    WebView paymentWebview;

    /* Fav Driver variable declaration start */
    LinearLayout lineArea, favArea;
    CustomHorizontalScrollView hScrollView;
    SwitchButton favSwitch;
    MTextView favDriverTitleTxt;

    int width;
    int width_D;
    int width_;
    /* Fav Driver variable declaration end */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rating);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        isUfx = getIntent().getBooleanExtra("isUfx", false);
        isFirst = getIntent().getBooleanExtra("isFirst", false);
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        desdashImage = (DividerView) findViewById(R.id.desdashImage);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        loading = (ProgressBar) findViewById(R.id.loading);
        errorView = (ErrorView) findViewById(R.id.errorView);

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        commentBox = (MaterialEditText) findViewById(R.id.commentBox);
        commentBox.setOverScrollMode(View.OVER_SCROLL_ALWAYS);
        commentBox.setScrollBarStyle(View.SCROLLBARS_INSIDE_INSET);
        commentBox.setOnTouchListener(new View.OnTouchListener() {

            public boolean onTouch(View v, MotionEvent event) {
                if (commentBox.hasFocus()) {
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    switch (event.getAction() & MotionEvent.ACTION_MASK) {
                        case MotionEvent.ACTION_SCROLL:
                            v.getParent().requestDisallowInterceptTouchEvent(false);
                            return true;
                    }
                }
                return false;
            }
        });

        generalCommentTxt = (MTextView) findViewById(R.id.generalCommentTxt);
        generalCommentArea = (CardView) findViewById(R.id.generalCommentArea);
        container = (LinearLayout) findViewById(R.id.container);
        ratingBar = (SimpleRatingBar) findViewById(R.id.ratingBar);
        fareHeadrtxt = (MTextView) findViewById(R.id.fareHeadrtxt);
        farecontainer = (LinearLayout) findViewById(R.id.farecontainer);
        fareindicatorImg = (ImageView) findViewById(R.id.fareindicatorImg);
        walletNoteTxt = (MTextView) findViewById(R.id.walletNoteTxt);
        fareindicatorImg.setOnClickListener(new setOnClickList());
        fareHeadrtxt.setOnClickListener(new setOnClickList());

        uberXRatingLayoutArea = (LinearLayout) findViewById(R.id.uberXRatingLayoutArea);
        mainRatingArea = (LinearLayout) findViewById(R.id.mainRatingArea);

        totalFareTxt = (MTextView) findViewById(R.id.totalFareTxt);
        dateVTxt = (MTextView) findViewById(R.id.dateVTxt);
        promoAppliedVTxt = (MTextView) findViewById(R.id.promoAppliedVTxt);
        ratingHeaderTxt = (MTextView) findViewById(R.id.ratingHeaderTxt);

        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);

        btn_type2.setOnClickListener(new setOnClickList());
        if (!isUfx) {
            backImgView.setVisibility(View.GONE);
            desdashImage.setVisibility(View.VISIBLE);
        } else {
            //getDetails();
            backImgView.setVisibility(View.VISIBLE);
            desdashImage.setVisibility(View.GONE);
        }

        paymentWebview = (WebView) findViewById(R.id.paymentWebview);
        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);



        setLabels();

        getFare();

        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) titleTxt.getLayoutParams();
        params.setMargins(Utils.dipToPixels(getActContext(), 15), 0, 0, 0);
        titleTxt.setLayoutParams(params);


        commentBox.setSingleLine(false);
        commentBox.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        commentBox.setGravity(Gravity.TOP);
        commentBox.setHideUnderline(true);
        commentBox.setFloatingLabel(MaterialEditText.FLOATING_LABEL_NONE);
    }

    private void scrollWithanimation() {
        if (!ratingBar.isFocusable()) {
            lineArea.performClick();
        }else
        {
            showSetAsFavArea(true);
        }
    }

    private void showSetAsFavArea(boolean show) {
        if (show) {

            if (generalFunc.isRTLmode()) {
                SlideAnimationUtil.slideInFromLeft(getActContext(), favArea);
            } else {
                SlideAnimationUtil.slideOutToRight(getActContext(), favArea);
            }

            if (!isAnimated) {
                hScrollView.setScrollingEnabled(true);

                ratingBar.animate().translationXBy(generalFunc.isRTLmode()?width_:-width_);

                ratingBar.setIndicator(true);
                ratingBar.setFocusable(false);

                favArea.setVisibility(View.VISIBLE);
                favArea.animate().translationXBy(generalFunc.isRTLmode()? width_D : -width_D) ;

                isAnimated = true;

                hScrollView.setScrollingEnabled(false);
            }

        } else {
//

            if (generalFunc.isRTLmode()) {
                SlideAnimationUtil.slideInFromRight(getActContext(), ratingBar);
            } else {
                SlideAnimationUtil.slideOutToLeft(getActContext(), ratingBar);
            }

            if (isAnimated) {
                hScrollView.setScrollingEnabled(true);

                ratingBar.animate().translationXBy(generalFunc.isRTLmode()?-width_:width_);

                ratingBar.setIndicator(false);
                ratingBar.setFocusable(true);

                favArea.animate().translationXBy(generalFunc.isRTLmode()?-width_D:width_D);
                favArea.setVisibility(View.GONE);

                isAnimated = false;

                hScrollView.setScrollingEnabled(false);
            }


        }
//        favArea.setVisibility(show?View.VISIBLE:View.GONE);
//        ratingBar.setVisibility(show?View.GONE:View.VISIBLE);
        lineArea.setOnClickListener(show ? new setOnClickList() : null);
    }

    /*fav driver feature End*/
    public class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            String data = url;
            data = data.substring(data.indexOf("data") + 5, data.length());
            try {

                String datajson = URLDecoder.decode(data, "UTF-8");
                loaderView.setVisibility(View.VISIBLE);

                view.setOnTouchListener(null);

                if (url.contains("success=1")) {

                    paymentWebview.setVisibility(View.GONE);
                    loaderView.setVisibility(View.GONE);

                    submitRating();


                }

                if (url.contains("success=0")) {

                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }


        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {

            generalFunc.showError();
            loaderView.setVisibility(View.GONE);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            loaderView.setVisibility(View.GONE);

            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_UP:
                        if (!v.hasFocus()) {
                            v.requestFocus();
                        }
                        break;
                }
                return false;
            });

        }
    }

    public Context getActContext() {
        return RatingActivity.this;
    }

    public void setLabels() {
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_RATING"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_BTN_SUBMIT_TXT"));
        commentBox.setHint(generalFunc.retrieveLangLBl("", "LBL_WRITE_COMMENT_HINT_TXT"));
        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_MYTRIP_Trip_Date"));
        promoAppliedVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DIS_APPLIED"));
        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_RIDE"));
        fareHeadrtxt.setText(generalFunc.retrieveLangLBl("Fare Details", "LBL_FARE_DETAILS"));
        totalFareTxt.setText(generalFunc.retrieveLangLBl("Total Fare", "LBL_Total_Fare"));
    }

    public void getFare() {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (container.getVisibility() == View.VISIBLE) {
            container.setVisibility(View.GONE);
        }
        if (loading.getVisibility() != View.VISIBLE) {
            loading.setVisibility(View.VISIBLE);
        }

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "displayFare");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        if (isUfx) {
            parameters.put("iTripId", getIntent().getStringExtra("iTripId"));
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                closeLoader();
                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseString)) {

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    String FormattedTripDate = generalFunc.getJsonValue("tTripRequestDateOrig", message);
                    String TotalFare = generalFunc.getJsonValue("TotalFare", message);
                    String fDiscount = generalFunc.getJsonValue("fDiscount", message);
                    String vDriverImage = generalFunc.getJsonValue("vDriverImage", message);
                    String CurrencySymbol = generalFunc.getJsonValue("CurrencySymbol", message);
                    String iVehicleTypeId = generalFunc.getJsonValue("iVehicleTypeId", message);
                    String iDriverId = generalFunc.getJsonValue("iDriverId", message);
                    String tEndLat = generalFunc.getJsonValue("tEndLat", message);
                    String tEndLong = generalFunc.getJsonValue("tEndLong", message);
                    String eCancelled = generalFunc.getJsonValue("eCancelled", message);
                    String vCancelReason = generalFunc.getJsonValue("vCancelReason", message);
                    String vCancelComment = generalFunc.getJsonValue("vCancelComment", message);
                    String vCouponCode = generalFunc.getJsonValue("vCouponCode", message);
                    String carImageLogo = generalFunc.getJsonValue("carImageLogo", message);
                    String iTripId = generalFunc.getJsonValue("iTripId", message);
                    String eType = generalFunc.getJsonValue("eType", message);
                    String eFavDriver = generalFunc.getJsonValue("eFavDriver", message);
                    String eFly = generalFunc.getJsonValue("eFly", responseString);


                    if (isFavDriverEnabled(eType) && !eFly.equalsIgnoreCase("Yes")) {

                        /*fav driver feature Start*/
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        width = displayMetrics.widthPixels;
                        width_D = (int) (width * 0.369);
                        width_ = (int) (width * 0.397);
                        favSwitch = (SwitchButton) findViewById(R.id.favSwitch);
                        lineArea = (LinearLayout) findViewById(R.id.lineArea);
                        favArea = (LinearLayout) findViewById(R.id.favArea);
                        hScrollView = (CustomHorizontalScrollView) findViewById(R.id.hScrollView);
                        favDriverTitleTxt = (MTextView) findViewById(R.id.favDriverTitleTxt);
                        favDriverTitleTxt.setText(generalFunc.retrieveLangLBl("save as Favorite Driver", "LBL_SAVE_AS_FAV_DRIVER"));

                        lineArea.setOnTouchListener(new SetOnTouchList());


                        favSwitch.setOnCheckedChangeListener((compoundButton, b) -> {

                            if (b == true) {
                                favSwitch.setThumbColorRes(R.color.Green);
                            } else {
                                favSwitch.setThumbColorRes(android.R.color.holo_red_dark);
                            }


                        });

                        favSwitch.setChecked(eFavDriver.equalsIgnoreCase("Yes"));


                        hScrollView.setOnTouchListener(new SetOnTouchList());
                        hScrollView.setOnClickListener(new setOnClickList());

                        ratingBar.setOnRatingBarChangeListener(new SimpleRatingBar.OnRatingBarChangeListener() {
                            @Override
                            public void onRatingChanged(SimpleRatingBar simpleRatingBar, float rating, boolean fromUser) {

                                if (favArea.getVisibility() == View.GONE) {
                                    showSetAsFavArea(true);

                                }
                            }
                        });


                        hScrollView.setOnTouchListener(new OnSwipeTouchListener(getActContext()) {
                            public void onSwipeTop() {

                            }

                            public void onSwipeRight() {
                                scrollWithanimation();
                            }

                            public void onSwipeLeft() {
                                scrollWithanimation();
                            }

                            public void onSwipeBottom() {

                            }

                        });

                    }

                    iTripId_str = iTripId;


                    if (generalFunc.getJsonValue("eWalletAmtAdjusted", message).equalsIgnoreCase("Yes")) {
                        walletNoteTxt.setVisibility(View.VISIBLE);
                        walletNoteTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WALLET_AMT_ADJUSTED") + " " + generalFunc.getJsonValue("fWalletAmountAdjusted", message));
                    }

                    if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_REQ_DATE"));
                    } else if (eType.equalsIgnoreCase("Deliver")) {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_DATE_TXT"));
                    } else {
                        dateVTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_DATE_TXT"));
                    }

                    JSONArray FareDetailsArrNewObj = null;

                    FareDetailsArrNewObj = generalFunc.getJsonArray("FareDetailsNewArr", message);

                    addFareDetailLayout(FareDetailsArrNewObj);

                    ((MTextView) findViewById(R.id.dateTxt)).setText(generalFunc.getDateFormatedType(FormattedTripDate, Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
                    ((MTextView) findViewById(R.id.sourceAddressTxt)).setText(generalFunc.getJsonValue("tSaddress", message));
                    if (generalFunc.getJsonValue("tDaddress", message).equals("")) {
                        ((LinearLayout) findViewById(R.id.destarea)).setVisibility(View.GONE);
                        ((ImageView) findViewById(R.id.imagedest)).setVisibility(View.GONE);
                        ((View) findViewById(R.id.destdivideview)).setVisibility(View.GONE);

                    } else {
                        ((LinearLayout) findViewById(R.id.destarea)).setVisibility(View.VISIBLE);
                        ((ImageView) findViewById(R.id.imagedest)).setVisibility(View.VISIBLE);
                        ((View) findViewById(R.id.destdivideview)).setVisibility(View.VISIBLE);
                        ((MTextView) findViewById(R.id.destAddressTxt)).setText(generalFunc.getJsonValue("tDaddress", message));
                    }

                    ((MTextView) findViewById(R.id.fareTxt)).setText(CurrencySymbol + " " + generalFunc.convertNumberWithRTL(TotalFare));

                    LinearLayout towTruckdestAddrArea = (LinearLayout) findViewById(R.id.towTruckdestAddrArea);

                    if (eType.equalsIgnoreCase("UberX")) {
                        uberXRatingLayoutArea.setVisibility(View.GONE);
                        mainRatingArea.setVisibility(View.VISIBLE);
                        new CreateRoundedView(Color.parseColor("#54A626"), Utils.dipToPixels(getActContext(), 9), 0, 0, findViewById(R.id.ufxPickArea));
                        ((MTextView) findViewById(R.id.sourceAddressTxt)).setText(generalFunc.getJsonValue("tSaddress", message));

                        if (generalFunc.getJsonValue("APP_DESTINATION_MODE", message).equalsIgnoreCase("Strict") || generalFunc.getJsonValue("APP_DESTINATION_MODE", message).equalsIgnoreCase("NonStrict")) {

                            if (towTruckdestAddrArea.getVisibility() == View.GONE) {
                                towTruckdestAddrArea.setVisibility(View.VISIBLE);
                                ((MTextView) findViewById(R.id.destAddressTxt)).setText(generalFunc.getJsonValue("tDaddress", message));
                            }
                        }
                        setImages("", "", generalFunc.getJsonValue("vLogoVehicleCategoryPath", message), generalFunc.getJsonValue("vLogoVehicleCategory", message));

                    } else {
                        mainRatingArea.setVisibility(View.VISIBLE);
                        uberXRatingLayoutArea.setVisibility(View.GONE);
                        setImages(carImageLogo, iVehicleTypeId, "", "");
                    }

                    ((MTextView) findViewById(R.id.carType)).setText(generalFunc.getJsonValue("vServiceDetailTitle", message));


                    if (eType.equals("Deliver")) {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_DELIVERY"));
                    } else if (eType.equals(Utils.CabGeneralType_UberX)) {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_YOUR_BOOKING"));
                    } else {
                        ratingHeaderTxt.setText(generalFunc.retrieveLangLBl("", "LBL_HOW_WAS_RIDE"));
                    }
                    String generalcommVal = "";
                    if (eCancelled.equals("Yes")) {
                        if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_JOB_CANCEL_PROVIDER");
                        } else if (eType.equalsIgnoreCase("Deliver")) {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_DELIVERY_CANCEL_DRIVER");
                        } else {
                            generalcommVal = generalFunc.retrieveLangLBl("", "LBL_PREFIX_TRIP_CANCEL_DRIVER");
                        }
                        generalCommentTxt.setText(generalcommVal
                                + " " + vCancelReason);
                        generalCommentTxt.setVisibility(View.VISIBLE);
                        generalCommentArea.setVisibility(View.VISIBLE);
                    } else {
                        generalCommentTxt.setVisibility(View.GONE);
                        generalCommentArea.setVisibility(View.GONE);
                    }
                    if (!fDiscount.equals("") && !fDiscount.equals("0") && !fDiscount.equals("0.00")) {
                        ((MTextView) findViewById(R.id.promoAppliedTxt)).setText(CurrencySymbol + generalFunc.convertNumberWithRTL(fDiscount));
                        (findViewById(R.id.promoView)).setVisibility(View.VISIBLE);
                    } else {
                        ((MTextView) findViewById(R.id.promoAppliedTxt)).setText("--");
                    }

                    if (generalFunc.getJsonValue("ENABLE_TIP_MODULE", message).equalsIgnoreCase("Yes")) {
                        isCollectTip = true;


                    } else {
                        isCollectTip = false;
                    }


                    container.setVisibility(View.VISIBLE);
                } else {
                    generateErrorView();
                }
            } else {
                generateErrorView();
            }
        });
        exeWebServer.execute();
    }


    private void addFareDetailLayout(JSONArray jobjArray) {


        for (int i = 0; i < jobjArray.length(); i++) {
            JSONObject jobject = generalFunc.getJsonObject(jobjArray, i);
            try {
                String data = jobject.names().getString(0);

                addFareDetailRow(data, jobject.get(data).toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

    }

    public void setImages(String carImageLogo, String iVehicleTypeId, String vLogoVehicleCategoryPath, String vLogoVehicleCategory) {
        String imageName = "";
        String size = "";
        switch (getResources().getDisplayMetrics().densityDpi) {
            case DisplayMetrics.DENSITY_LOW:
                imageName = "mdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "80";
                break;
            case DisplayMetrics.DENSITY_MEDIUM:
                imageName = "mdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "80";
                break;
            case DisplayMetrics.DENSITY_HIGH:
                imageName = "hdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "120";
                break;
            case DisplayMetrics.DENSITY_TV:
                imageName = "hdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "120";
                break;
            case DisplayMetrics.DENSITY_XHIGH:
                imageName = "xhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "160";
                break;
            case DisplayMetrics.DENSITY_XXHIGH:
                imageName = "xxhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "240";
                break;
            case DisplayMetrics.DENSITY_XXXHIGH:
                imageName = "xxxhdpi_" + (carImageLogo.equals("") ? vLogoVehicleCategory : carImageLogo);
                size = "320";
                break;
        }

    }

    public void submitRating() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "submitRating");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("tripID", iTripId_str);
        parameters.put("rating", "" + ratingBar.getRating());
        parameters.put("message", Utils.getText(commentBox));
        parameters.put("UserType", Utils.app_type);
        if (favSwitch!=null) {
            parameters.put("eFavDriver", favSwitch.isChecked() ? "Yes" : "No");
        }

        if (generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", userProfileJson).equalsIgnoreCase("Method-1")) {
            parameters.put("fAmount", tipamount);
            if (isCollectTip) {
                parameters.put("isCollectTip", "Yes");
            } else {
                parameters.put("isCollectTip", "No");

            }
        }


        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    btn_type2.setEnabled(true);

                    showBookingAlert(generalFunc.getJsonValue("eType", responseString));
                } else {
                    btn_type2.setEnabled(true);
                    if (generalFunc.getJsonValue(Utils.message_str, responseString).equalsIgnoreCase("LBL_REQUIRED_MINIMUM_AMOUT")) {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)) + " " + generalFunc.getJsonValue("minValue", responseString));

                    } else {


                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }
                }
            } else {
                btn_type2.setEnabled(true);
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }


    public void showBookingAlert(String eType) {
        android.support.v7.app.AlertDialog alertDialog;
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle("");
        builder.setCancelable(false);
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.dialog_booking_view, null);
        builder.setView(dialogView);

        final MTextView titleTxt = (MTextView) dialogView.findViewById(R.id.titleTxt);
        final MTextView mesasgeTxt = (MTextView) dialogView.findViewById(R.id.mesasgeTxt);
        if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleTxt.setText(generalFunc.retrieveLangLBl("Booking Successful", "LBL_JOB_FINISHED"));
            mesasgeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_JOB_FINISHED_TXT"));
        } else if (eType.equalsIgnoreCase("Deliver")) {
            titleTxt.setText(generalFunc.retrieveLangLBl("Booking Successful", "LBL_DELIVERY_SUCCESS_FINISHED"));
            mesasgeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_DELIVERY_FINISHED_TXT"));
        } else {
            titleTxt.setText(generalFunc.retrieveLangLBl("Booking Successful", "LBL_SUCCESS_FINISHED"));
            mesasgeTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TRIP_FINISHED_TXT"));
        }


        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_OK_THANKS"), (dialog, which) -> {
            dialog.cancel();

            // generalFunc.restartApp();

            if (isFirst) {
                MyApp.getInstance().restartWithGetDataApp();
                return;
            }
//                if (isUfx) {
//                    //  onBackPressed();
//                    MyApp.getInstance().restartWithGetDataApp();
//                } else {
            MyApp.getInstance().restartWithGetDataApp();
            //   }

        });


        alertDialog = builder.create();
        alertDialog.setCancelable(false);
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();

    }

    public void buildTipCollectMessage(String message, String positiveBtn, String negativeBtn) {

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.desgin_add_tip_layout, null);
        builder.setView(dialogView);

        final MaterialEditText tipAmountEditBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        tipAmountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        tipAmountEditBox.setVisibility(View.GONE);
        final MTextView giveTipTxtArea = (MTextView) dialogView.findViewById(R.id.giveTipTxtArea);
        final MTextView msgTxt = (MTextView) dialogView.findViewById(R.id.msgTxt);
        msgTxt.setVisibility(View.VISIBLE);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIP_TITLE_TXT"));
        msgTxt.setText(message);
        giveTipTxtArea.setText(negativeBtn);
        skipTxtArea.setText(positiveBtn);

        skipTxtArea.setOnClickListener(view -> {
            //generalFunc.restartApp();
            giveTipAlertDialog.dismiss();
            //  MyApp.getInstance().restartWithGetDataApp();
            tipamount = 0 + "";

            btn_type2.setEnabled(false);
            submitRating();
            isCollectTip = false;
        });

        giveTipTxtArea.setOnClickListener(view -> {
            giveTipAlertDialog.dismiss();
            showTipBox();

        });
        giveTipAlertDialog = builder.create();
        giveTipAlertDialog.setCancelable(true);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(giveTipAlertDialog);
        }
        giveTipAlertDialog.show();
    }

    public void showTipBox() {
        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View dialogView = inflater.inflate(R.layout.desgin_add_tip_layout, null);
        builder.setView(dialogView);

        final MaterialEditText tipAmountEditBox = (MaterialEditText) dialogView.findViewById(R.id.editBox);
        tipAmountEditBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        final MTextView giveTipTxtArea = (MTextView) dialogView.findViewById(R.id.giveTipTxtArea);
        final MTextView skipTxtArea = (MTextView) dialogView.findViewById(R.id.skipTxtArea);
        final MTextView titileTxt = (MTextView) dialogView.findViewById(R.id.titileTxt);
        titileTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TIP_AMOUNT_ENTER_TITLE"));
        giveTipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"));
        skipTxtArea.setText("" + generalFunc.retrieveLangLBl("", "LBL_SKIP_TXT"));

        skipTxtArea.setOnClickListener(view -> {
            Utils.hideKeyboard(getActContext());
            giveTipAlertDialog.dismiss();
            btn_type2.setEnabled(false);
            submitRating();

        });

        giveTipTxtArea.setOnClickListener(view -> {

            final boolean tipAmountEntered = Utils.checkText(tipAmountEditBox) ? true : Utils.setErrorFields(tipAmountEditBox, generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
            if (tipAmountEntered == false) {
                return;
            }
            Utils.hideKeyboard(getActContext());
            giveTipAlertDialog.dismiss();
            collectTip(Utils.getText(tipAmountEditBox));
            btn_type2.setEnabled(false);
            if (generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", userProfileJson).equalsIgnoreCase("Method-1")) {
                submitRating();
            } else if (!generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", userProfileJson).equalsIgnoreCase("Method-1")) {

                String url = CommonUtilities.PAYMENTLINK + "amount=" + Utils.getText(tipAmountEditBox) + "&iUserId=" + generalFunc.getMemberId() + "&UserType=" + Utils.app_type + "&vUserDeviceCountry=" +
                        generalFunc.retrieveValue(Utils.DefaultCountryCode) + "&ccode=" + generalFunc.getJsonValue("vCurrencyPassenger", userProfileJson) + "&UniqueCode=" + System.currentTimeMillis() + "&eForTip=Yes" + "&iTripId=" + iTripId_str;

                paymentWebview.setWebViewClient(new myWebClient());
                paymentWebview.getSettings().setJavaScriptEnabled(true);
                paymentWebview.loadUrl(url);
                paymentWebview.setFocusable(true);
                paymentWebview.setVisibility(View.VISIBLE);
                loaderView.setVisibility(View.VISIBLE);

            }


        });
        giveTipAlertDialog = builder.create();
        giveTipAlertDialog.setCancelable(true);
        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(giveTipAlertDialog);
        }
        giveTipAlertDialog.show();

    }

    private void collectTip(String tipAmount) {


        tipamount = tipAmount;


    }


    public void closeLoader() {
        if (loading.getVisibility() == View.VISIBLE) {
            loading.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getFare());
    }

    @Override
    public void onBackPressed() {


        if (paymentWebview.getVisibility() == View.VISIBLE) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_PAYMENT_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {
                if (buttonId == 1) {
                    paymentWebview.setVisibility(View.GONE);
                    paymentWebview.stopLoading();
                    loaderView.setVisibility(View.GONE);
                    btn_type2.setEnabled(true);
                }
            });

            return;
        }


        if (isFirst) {
            MyApp.getInstance().restartWithGetDataApp();
            return;
        }
        if (isUfx) {
            MyApp.getInstance().restartWithGetDataApp();

            // super.onBackPressed();
        } else {
            return;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private void addFareDetailRow(String row_name, String row_value) {
        View convertView = null;
        if (row_name.equalsIgnoreCase("eDisplaySeperator")) {
            convertView = new View(getActContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, Utils.dipToPixels(getActContext(), 1));
            params.setMarginStart(Utils.dipToPixels(getActContext(), 10));
            params.setMarginEnd(Utils.dipToPixels(getActContext(), 10));
            convertView.setBackgroundColor(Color.parseColor("#dedede"));
            convertView.setLayoutParams(params);
        } else {
            LayoutInflater infalInflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.design_fare_breakdown_row, null);

            convertView.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

            convertView.setMinimumHeight(Utils.dipToPixels(getActContext(), 40));

            MTextView titleHTxt = (MTextView) convertView.findViewById(R.id.titleHTxt);
            MTextView titleVTxt = (MTextView) convertView.findViewById(R.id.titleVTxt);

            titleHTxt.setText(generalFunc.convertNumberWithRTL(row_name));
            titleVTxt.setText(generalFunc.convertNumberWithRTL(row_value));

            titleHTxt.setTextColor(Color.parseColor("#303030"));
            titleVTxt.setTextColor(Color.parseColor("#111111"));
        }

        if (convertView != null)
            farecontainer.addView(convertView);
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            int i = view.getId();
            Utils.hideKeyboard(getActContext());
            if (i == submitBtnId) {
                if (ratingBar.getRating() < 0.5) {
                    generalFunc.showMessage(generalFunc.getCurrentView(RatingActivity.this), generalFunc.retrieveLangLBl("", "LBL_ERROR_RATING_DIALOG_TXT"));
                    return;
                }

                if (isCollectTip) {
                    buildTipCollectMessage(generalFunc.retrieveLangLBl("", "LBL_TIP_TXT"), generalFunc.retrieveLangLBl("No,Thanks", "LBL_NO_THANKS"),
                            generalFunc.retrieveLangLBl("Give Tip", "LBL_GIVE_TIP_TXT"));
                    return;
                } else {
                    btn_type2.setEnabled(false);
                    submitRating();
                }

            } else if (i == backImgView.getId()) {
                onBackPressed();
            } else if (i == fareindicatorImg.getId()) {
                fareHeadrtxt.performClick();


            } else if (i == fareHeadrtxt.getId()) {
                if (farecontainer.getVisibility() == View.GONE) {
                    fareindicatorImg.setImageResource(R.mipmap.ic_arrow_up);
                    farecontainer.setVisibility(View.VISIBLE);

                } else {
                    fareindicatorImg.setImageResource(R.mipmap.ic_arrow_down);
                    farecontainer.setVisibility(View.GONE);

                }
            } else if (i == R.id.lineArea) {
                showSetAsFavArea(false);
            } else if (i == R.id.hScrollView) {
                if (!ratingBar.isFocusable()) {
                    lineArea.performClick();
                }
            }
        }
    }

    public boolean isFavDriverEnabled(String eType)
    {
        String ENABLE_FAVORITE_DRIVER_MODULE=generalFunc.retrieveValue(Utils.ENABLE_FAVORITE_DRIVER_MODULE_KEY);
       return Utils.checkText(ENABLE_FAVORITE_DRIVER_MODULE) && ENABLE_FAVORITE_DRIVER_MODULE.equalsIgnoreCase("Yes") &&(eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)|| eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)|| eType.equalsIgnoreCase(Utils.CabGeneralType_Deliver)||eType.equalsIgnoreCase("Deliver")|| eType.equalsIgnoreCase(Utils.eType_Multi_Delivery));
    }

}
