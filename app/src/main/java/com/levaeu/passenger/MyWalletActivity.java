package com.levaeu.passenger;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.CardView;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.general.files.CustomDialog;
import com.general.files.DecimalDigitsInputFilter;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.InternetConnection;
import com.general.files.MyApp;
import com.general.files.StartActProcess;
import com.squareup.picasso.Picasso;
import com.utils.CommonUtilities;
import com.utils.Logger;
import com.utils.Utils;
import com.view.CreateRoundedView;
import com.view.ErrorView;
import com.view.GenerateAlertBox;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.SelectableRoundedImageView;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Timer;

/**
 * Created by Admin on 04-11-2016.
 */
public class MyWalletActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    private final long DELAY = 1000; // in ms
    public GeneralFunctions generalFunc;
    MTextView titleTxt;
    ImageView backImgView;
    ProgressBar loading_wallet_history;
    MTextView viewTransactionsTxt;
    ErrorView errorView;
    String required_str = "";
    String error_money_str = "";

    String userProfileJson = "";
    boolean mIsLoading = false;

    String next_page_str = "0";
    private ScrollView scrollView;
    private MaterialEditText rechargeBox;

    private MTextView policyTxt;
    private MTextView termsTxt;
    private MTextView yourBalTxt;
    private MButton btn_type1, btn_type2;
    private MTextView addMoneybtn1;
    private MTextView addMoneybtn2;
    private MTextView addMoneybtn3;
    private MTextView withDrawMoneyTxt;
    private MTextView addMoneyTagTxt;
    private MTextView addMoneyTxt;

    private Timer timer = new Timer();
    private static final int SEL_CARD = 004;
    public static final int TRANSFER_MONEY = 87;

    AppCompatCheckBox useBalChkBox;
    MTextView useBalanceTxt;

    InternetConnection intCheck;
    AVLoadingIndicatorView loaderView;
    WebView paymentWebview;

    // Go Pay view declaration start
    private LinearLayout addTransferArea;
    String transferState = "SEARCH";
    private MTextView userNameTxt;
    private MButton  btn_type4;
    String error_email_str = "";
    HashMap<String, String> transferMap = new HashMap<>();
    String error_verification_code = "";
    String isRegenerate = "No";
    boolean isClicked = false;
    // Go Pay view declaration end

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mywallet);

        generalFunc = MyApp.getInstance().getGeneralFun(getActContext());
        intCheck = new InternetConnection(getActContext());
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);

        loading_wallet_history = (ProgressBar) findViewById(R.id.loading_wallet_history);
        viewTransactionsTxt = (MTextView) findViewById(R.id.viewTransactionsTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);
        addMoneybtn1 = (MTextView) findViewById(R.id.addMoneybtn1);
        addMoneybtn2 = (MTextView) findViewById(R.id.addMoneybtn2);
        addMoneybtn3 = (MTextView) findViewById(R.id.addMoneybtn3);
        withDrawMoneyTxt = (MTextView) findViewById(R.id.withDrawMoneyTxt);
        addMoneyTxt = (MTextView) findViewById(R.id.addMoneyTxt);
        addMoneyTagTxt = (MTextView) findViewById(R.id.addMoneyTagTxt);
        errorView = (ErrorView) findViewById(R.id.errorView);
        rechargeBox = (MaterialEditText) findViewById(R.id.rechargeBox);
        termsTxt = (MTextView) findViewById(R.id.termsTxt);
        yourBalTxt = (MTextView) findViewById(R.id.yourBalTxt);
        policyTxt = (MTextView) findViewById(R.id.policyTxt);
        scrollView = (ScrollView) findViewById(R.id.scrollView);
        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();
        btn_type1 = ((MaterialRippleLayout) findViewById(R.id.btn_type1)).getChildView();



        /*Go Pay view initialization start*/

        userNameTxt = (MTextView) findViewById(R.id.userNameTxt);

        btn_type4 = ((MaterialRippleLayout) findViewById(R.id.btn_type4)).getChildView();
        addTransferArea = (LinearLayout) findViewById(R.id.addTransferArea);


        findViewById(R.id.viewTransactionsBtnArea).setOnClickListener(new setOnClickList());
        findViewById(R.id.addTransferBtnArea).setOnClickListener(new setOnClickList());




        /*Go Pay view initialization end*/

        new CreateRoundedView(Color.parseColor("#838383"), 5, 0, 0, addMoneybtn1);
        new CreateRoundedView(Color.parseColor("#676767"), 5, 0, 0, addMoneybtn2);
        new CreateRoundedView(Color.parseColor("#4d4d4d"), 5, 0, 0, addMoneybtn3);


        useBalanceTxt = (MTextView) findViewById(R.id.useBalanceTxt);
        useBalChkBox = (AppCompatCheckBox) findViewById(R.id.useBalChkBox);


        paymentWebview = (WebView) findViewById(R.id.paymentWebview);
        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);


        rechargeBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rechargeBox.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        backImgView.setOnClickListener(new setOnClickList());
        viewTransactionsTxt.setOnClickListener(new setOnClickList());
        addMoneybtn1.setOnClickListener(new setOnClickList());
        addMoneybtn2.setOnClickListener(new setOnClickList());
        addMoneybtn3.setOnClickListener(new setOnClickList());
        btn_type2.setId(Utils.generateViewId());
        btn_type2.setOnClickListener(new setOnClickList());
        btn_type1.setId(Utils.generateViewId());
        btn_type1.setOnClickListener(new setOnClickList());
        termsTxt.setOnClickListener(new setOnClickList());

        withDrawMoneyTxt.setMovementMethod(LinkMovementMethod.getInstance());

        setLabels();

        withDrawMoneyTxt.setVisibility(View.GONE);


        showHideButton("");

        rechargeBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (rechargeBox.getText().length() == 1) {
                    if (rechargeBox.getText().toString().contains(".")) {
                        rechargeBox.setText("0.");
                        rechargeBox.setSelection(rechargeBox.length());
                    }
                }

            }
        });


        useBalChkBox.setOnCheckedChangeListener(this);
        // remove service id
        if (getIntent().hasExtra("iServiceId")) {
            generalFunc.storeData(Utils.iServiceId_KEY, "");
        }

        getTransactionHistory(false);

        getWalletBalDetails();

    }

    private void showHideButton(String setView) {

        boolean isOnlyCashEnabled = generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash");
        /*Go Pay Enabled Or Not - Delete Start if you don't want gopay */
        boolean isTransferMoneyEnabled = generalFunc.retrieveValue(Utils.ENABLE_GOPAY_KEY).equalsIgnoreCase("Yes");
        removeValues(true);
        /*Go Pay Enabled Or Not - Delete End if you don't want gopay */

        if (TextUtils.isEmpty(setView)) {
            if (isOnlyCashEnabled) {
                addTransferArea.setVisibility(isTransferMoneyEnabled ? View.VISIBLE : View.GONE);
                ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.GONE);

                if (isTransferMoneyEnabled) {
                    transferState = "SEARCH";
                    configureView();

                }
                findViewById(R.id.addTransferBtnArea).setVisibility(View.GONE);
                btn_type4.setOnClickListener(null);
                btn_type4.setText(isTransferMoneyEnabled ? generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_TXT") : "");

            } else if (!isOnlyCashEnabled) {
                addTransferArea.setVisibility(View.VISIBLE);
                ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.VISIBLE);
                findViewById(R.id.addTransferBtnArea).setVisibility(isTransferMoneyEnabled ? View.VISIBLE : View.GONE);
                btn_type4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                btn_type1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
                btn_type4.setOnClickListener(isTransferMoneyEnabled ? new setOnClickList() : null);
                btn_type4.setText(generalFunc.retrieveLangLBl("", "LBL_TRANSFER_MONEY"));
            }
        }
        /*Go Pay Enabled Or Not - Delete Start if you don't want gopay */
        else if (setView.equalsIgnoreCase("add")) {
            rechargeBox.setText("");
            btn_type4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            btn_type1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            transferState = "SEARCH";
            configureView();

            ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.VISIBLE);
            btn_type4.setText(generalFunc.retrieveLangLBl("", "LBL_TRANSFER_MONEY"));
        } else if (setView.equalsIgnoreCase("transfer")) {
            btn_type4.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            btn_type1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
            ((LinearLayout) findViewById(R.id.addMoneyToWalletArea)).setVisibility(View.GONE);

            transferState = "SEARCH";
            configureView();
            btn_type4.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_TXT"));
        }
        /*Go Pay Enabled Or Not - Delete End if you don't want gopay */

    }

    // Go Pay  implementation start

    private void toWhomArea(boolean show) {
        if (show) {

        } else {

        }
    }


    private void showUserInfoArea(boolean show) {
        if (show) {

        } else {

        }
    }


    private void addAmountDetailArea(boolean show) {
        if (show) {

        } else {

        }
    }

    private void configureView() {
        hideShowViews(transferState);

        if (transferState.equalsIgnoreCase("SEARCH")) {

        } else if (transferState.equalsIgnoreCase("ENTER_AMOUNT")) {

            userNameTxt.setText(transferMap.containsKey("vName") ? transferMap.get("vName") : "");
            if (transferMap.containsKey("vImgName") && Utils.checkText(transferMap.get("vImgName"))) {

            }

        } else if (transferState.equalsIgnoreCase("VERIFY")) {

        }

    }

    public void goback() {


        onBackPressed();
    }

    private void hideShowViews(String transferState) {

        addAmountDetailArea(true);

        removeValues(transferState.equalsIgnoreCase("SEARCH"));

        showUserInfoArea(!transferState.equalsIgnoreCase("SEARCH"));

        toWhomArea(false);

    }


    private void removeValues(boolean removeValues) {
        if (removeValues) {

            transferMap.clear();
        }
    }

    private void transferMoneyToWallet() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("fromUserId", generalFunc.getMemberId());
        parameters.put("fromUserType", Utils.userType);
        parameters.put("UserType", Utils.userType);
        if (transferState.equalsIgnoreCase("SEARCH")) {
            parameters.put("type", "GopayCheckPhoneEmail");
        } else if (transferState.equalsIgnoreCase("ENTER_AMOUNT")) {
            parameters.put("type", "GoPayVerifyAmount");
            parameters.put("isRegenerate", isRegenerate);
            parameters.put("toUserId", transferMap.get("iUserId"));
            parameters.put("toUserType", transferMap.get("eUserType"));
        } else if (transferState.equalsIgnoreCase("VERIFY")) {
            parameters.put("type", "GoPayTransferAmount");
            parameters.put("toUserId", transferMap.get("iUserId"));
            parameters.put("toUserType", transferMap.get("eUserType"));
        }

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (isRegenerate.equalsIgnoreCase("Yes")) {
                isClicked = false;
            }

            if (responseString != null && !responseString.equals("")) {
                String action = generalFunc.getJsonValue(Utils.action_str, responseString);
                if (action.equals("1")) {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (transferState.equalsIgnoreCase("SEARCH")) {
                        transferMap = new HashMap<>();
                        transferMap.put("iUserId", generalFunc.getJsonValue("iUserId", message));
                        String eUserType = generalFunc.getJsonValue("eUserType", message);
                        transferMap.put("eUserType", eUserType);
                        transferMap.put("eUserTypeLBl", eUserType.equalsIgnoreCase("driver") ? generalFunc.retrieveLangLBl("", "LBL_DRIVER") : generalFunc.retrieveLangLBl("", "LBL_RIDER"));
                        transferMap.put("vEmail", generalFunc.getJsonValue("vEmail", message));
                        transferMap.put("vName", generalFunc.getJsonValue("vName", message));
                        transferMap.put("vImgName", generalFunc.getJsonValue("vImgName", message));
                        transferMap.put("vPhone", generalFunc.getJsonValue("vPhone", message));
                        transferState = "ENTER_AMOUNT";
                        configureView();
                    } else if (transferState.equalsIgnoreCase("ENTER_AMOUNT")) {
                        if (isRegenerate.equalsIgnoreCase("Yes")) {
                            isRegenerate = "No";
                        }
                        transferState = "VERIFY";
                        transferMap.put("verificationCode", generalFunc.getJsonValue("verificationCode", message));


                        configureView();
                    } else if (transferState.equalsIgnoreCase("VERIFY")) {
                        if (isRegenerate.equalsIgnoreCase("Yes")) {
                            isRegenerate = "No";
                        }
                        successDialog(generalFunc.retrieveLangLBl("", message), generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
                    }
                } else {

                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    String showAddMoney = generalFunc.getJsonValue("showAddMoney", responseString);

                    if (transferState.equalsIgnoreCase("ENTER_AMOUNT") && (message.equalsIgnoreCase("LBL_WALLET_AMOUNT_GREATER_THAN_ZERO") || showAddMoney.equalsIgnoreCase("Yes"))) {
                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", message));

                        boolean isOnlyCashEnabled = generalFunc.getJsonValue("APP_PAYMENT_MODE", userProfileJson).equalsIgnoreCase("Cash");

                        if (!isOnlyCashEnabled) {
                            generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY"));
                        }
                        generateAlert.setNegativeBtn(!isOnlyCashEnabled ? generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT") : generalFunc.retrieveLangLBl("", "LBL_OK"));

                        generateAlert.setBtnClickList(btn_id -> {

                            if (btn_id == 1) {
                                generateAlert.closeAlertBox();
                                btn_type4.performClick();
                            } else {
                                generateAlert.closeAlertBox();
                            }

                        });

                        generateAlert.showAlertBox();
                        return;
                    } else if (transferState.equalsIgnoreCase("VERIFY")) {

                        if (message.equalsIgnoreCase("LBL_OTP_EXPIRED")) {
                            isRegenerate = "Yes";
                        }
                        successDialog(action.equalsIgnoreCase("2") ? message : generalFunc.retrieveLangLBl("", message), generalFunc.retrieveLangLBl("Ok", "LBL_BTN_OK_TXT"));
                    } else {

                        generalFunc.showGeneralMessage("", action.equalsIgnoreCase("2") ? message : generalFunc.retrieveLangLBl("", message));
                    }


                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();

    }

    private void animateDialog(LinearLayout infoArea) {

        CustomDialog customDialog = new CustomDialog(getActContext());
        customDialog.setDetails(""/*generalFunc.retrieveLangLBl("","LBL_RETRIVE_OTP_TITLE_TXT")*/, generalFunc.retrieveLangLBl("", "LBL_TRANSFER_WALLET_OTP_INFO_TXT"), generalFunc.retrieveLangLBl("", "LBL_BTN_OK_TXT"), "", false, R.drawable.ic_normal_info, false, 2);
        customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
        customDialog.setRoundedViewBorderColor(R.color.white);
        customDialog.setImgStrokWidth(15);
        customDialog.setBtnRadius(10);
        customDialog.setIconTintColor(R.color.white);
        customDialog.setPositiveBtnBackColor(R.color.appThemeColor_2);
        customDialog.setPositiveBtnTextColor(R.color.white);
        customDialog.createDialog();
        customDialog.setPositiveButtonClick(new com.general.files.Closure() {
            @Override
            public void exec() {

            }
        });
        customDialog.setNegativeButtonClick(new com.general.files.Closure() {
            @Override
            public void exec() {

            }
        });
        customDialog.show();
    }

    private void successDialog(String message, String positiveBtnTxt) {
        if (isRegenerate.equalsIgnoreCase("yes")) {
            CustomDialog customDialog = new CustomDialog(getActContext());
            customDialog.setDetails(""/*generalFunc.retrieveLangLBl("","LBL_OTP_EXPIRED_TXT")*/, message, positiveBtnTxt, "", false, R.drawable.ic_hand_gesture, false, 2);
            customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
            customDialog.setRoundedViewBorderColor(R.color.white);
            customDialog.setImgStrokWidth(15);
            customDialog.setBtnRadius(10);
            customDialog.setIconTintColor(R.color.white);
            customDialog.setPositiveBtnBackColor(R.color.appThemeColor_2);
            customDialog.setPositiveBtnTextColor(R.color.white);
            customDialog.createDialog();
            customDialog.setPositiveButtonClick(new com.general.files.Closure() {
                @Override
                public void exec() {

                }
            });
            customDialog.setNegativeButtonClick(new com.general.files.Closure() {
                @Override
                public void exec() {

                }
            });
            customDialog.show();

        } else {
            CustomDialog customDialog = new CustomDialog(getActContext());
            customDialog.setDetails(""/*generalFunc.retrieveLangLBl("","LBL_MONEY_TRANSFER_CONFIRMATION_TITLE_TXT")*/, message, positiveBtnTxt, "", false, R.drawable.ic_correct, false, 2);
            customDialog.setRoundedViewBackgroundColor(R.color.appThemeColor_1);
            customDialog.setRoundedViewBorderColor(R.color.white);
            customDialog.setImgStrokWidth(15);
            customDialog.setBtnRadius(10);
            customDialog.setIconTintColor(R.color.white);
            customDialog.setPositiveBtnBackColor(R.color.appThemeColor_2);
            customDialog.setPositiveBtnTextColor(R.color.white);
            customDialog.createDialog();
            customDialog.setPositiveButtonClick(new com.general.files.Closure() {
                @Override
                public void exec() {
                    transferState = "SEARCH";
                    configureView();
                    generalFunc.storeData(Utils.ISWALLETBALNCECHANGE, "Yes");
                    getTransactionHistory(false);
                    getWalletBalDetails();
                }
            });
            customDialog.setNegativeButtonClick(new com.general.files.Closure() {
                @Override
                public void exec() {

                }
            });
            customDialog.show();
        }
    }

    // Go Pay  implementation end


    public class myWebClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }


        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            String data = url;
            Logger.d("WebData", "::" + data);
            data = data.substring(data.indexOf("data") + 5, data.length());
            try {

                String datajson = URLDecoder.decode(data, "UTF-8");
                Logger.d("WebData", "::2222222::" + datajson);
                loaderView.setVisibility(View.VISIBLE);

                view.setOnTouchListener(null);


                if (url.contains("success=1")) {

                    paymentWebview.setVisibility(View.GONE);
                    rechargeBox.setText("");

                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.retrieveLangLBl("", "LBL_WALLET_MONEY_CREDITED")), "", generalFunc.retrieveLangLBl("", "LBL_OK"), i -> {
//                        isFinish = true;

                        getWalletBalDetails();
                    });
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


    public void setLabels() {

        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_LEFT_MENU_WALLET"));
        yourBalTxt.setText(generalFunc.retrieveLangLBl("", "LBL_USER_BALANCE"));
        viewTransactionsTxt.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));
        btn_type1.setText(generalFunc.retrieveLangLBl("", "LBL_VIEW_TRANS_HISTORY"));

        rechargeBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_RECHARGE_AMOUNT_TXT"), generalFunc.retrieveLangLBl("", "LBL_RECHARGE_AMOUNT_TXT"));
        //  rechargeBox.setInputType(InputType.TYPE_CLASS_NUMBER);
        rechargeBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        rechargeBox.getLabelFocusAnimator().start();
        rechargeBox.setBackgroundResource(android.R.color.transparent);

        useBalanceTxt.setText(generalFunc.retrieveLangLBl("", "LBL_USE_WALLET_BALANCE_NOTE"));

        withDrawMoneyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_WITHDRAW_MONEY_TXT"));
        addMoneyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_TXT"));
        addMoneyTagTxt.setText(generalFunc.retrieveLangLBl("", "LBL_ADD_MONEY_TXT1"));
        policyTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY"));
        termsTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PRIVACY_POLICY1"));

        required_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT");
        error_money_str = generalFunc.retrieveLangLBl("", "LBL_ADD_CORRECT_DETAIL_TXT");

        addMoneybtn1.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_1", userProfileJson)));
        addMoneybtn2.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_2", userProfileJson)));
        addMoneybtn3.setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_3", userProfileJson)));


        if (generalFunc.getJsonValue("eWalletAdjustment", userProfileJson).equals("No")) {
            useBalChkBox.setChecked(false);
        } else {
            useBalChkBox.setChecked(true);
        }


        /*Go Pay Label Start*/

        //otpInfoTxt.setText(generalFunc.retrieveLangLBl("", "LBL_TRANSFER_WALLET_OTP_INFO_TXT"));

        String lblDriver = "LBL_DRIVER";
        if (Utils.app_type.equalsIgnoreCase(Utils.CabGeneralTypeRide_Delivery_UberX) || Utils.app_type.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            lblDriver = "LBL_PROVIDER";
        }

        error_email_str = generalFunc.retrieveLangLBl("", "LBL_FEILD_EMAIL_ERROR_TXT");
        error_verification_code = generalFunc.retrieveLangLBl("", "LBL_VERIFICATION_CODE_INVALID");

        btn_type4.setId(Utils.generateViewId());
        /*Go Pay Label End*/
    }


    public void UpdateUserWalletAdjustment(boolean value) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateUserWalletAdjustment");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("eWalletAdjustment", value == true ? "Yes" : "No");

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setCancelAble(false);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                closeLoader();

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);
                if (isDataAvail == true) {

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                    userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);

                    generalFunc.showMessage(generalFunc.getCurrentView((Activity) getActContext()), generalFunc.retrieveLangLBl("", "LBL_INFO_UPDATED_TXT"));

                } else {

                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));

                    useBalChkBox.setOnCheckedChangeListener(null);
                    useBalChkBox.setChecked(value == true ? false : true);
                    useBalChkBox.setOnCheckedChangeListener(this);
                }
            } else {
                generalFunc.showError();
                useBalChkBox.setOnCheckedChangeListener(null);
                useBalChkBox.setChecked(value == true ? false : true);
                useBalChkBox.setOnCheckedChangeListener(this);
            }
        });
        exeWebServer.execute();
    }

    private void filterAddMoneyPrice() {

        if (Utils.checkText(rechargeBox) == true) {
            String amount = "" + rechargeBox.getText().toString();
            Utils.removeInput(rechargeBox);
            rechargeBox.setText("" + generalFunc.addSemiColonToPrice(amount.trim()));
        }

    }

    public void checkValues() {

        Double moneyAdded = 0.0;

        if (Utils.checkText(rechargeBox) == true) {

            moneyAdded = generalFunc.parseDoubleValue(0, Utils.getText(rechargeBox));
        }
        boolean addMoneyAmountEntered = Utils.checkText(rechargeBox) ? (moneyAdded > 0 ? true : Utils.setErrorFields(rechargeBox, error_money_str))
                : Utils.setErrorFields(rechargeBox, required_str);

        if (addMoneyAmountEntered == false) {
            return;
        }

         /*if (generalFunc.isDeliverOnlyEnabled()) {
            Bundle bn = new Bundle();
            bn.putBoolean("isWallet", true);
            bn.putString("fAmount", Utils.getText(rechargeBox));
            new StartActProcess(getActContext()).startActForResult(PaymentCardActivity.class, bn, SEL_CARD);
        } else {*/

        if (generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", userProfileJson).equalsIgnoreCase("Method-1")) {

            if (generalFunc.isDeliverOnlyEnabled() ||(getIntent().hasExtra("isCheckout") && generalFunc.isDeliverOnlyEnabled()))
            {
                Bundle bn = new Bundle();
                bn.putString("fAmount", Utils.getText(rechargeBox));
                bn.putString("isCheckout", "");
                new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, SEL_CARD);
            }else
            {
                addMoneyToWallet();
            }

        } else if (!generalFunc.getJsonValue("SYSTEM_PAYMENT_FLOW", userProfileJson).equalsIgnoreCase("Method-1")) {


            String url = CommonUtilities.PAYMENTLINK + "amount=" + Utils.getText(rechargeBox) + "&iUserId=" + generalFunc.getMemberId() + "&UserType=" + Utils.app_type + "&vUserDeviceCountry=" +
                    generalFunc.retrieveValue(Utils.DefaultCountryCode) + "&ccode=" + generalFunc.getJsonValue("vCurrencyPassenger", userProfileJson) + "&UniqueCode=" + System.currentTimeMillis();

            paymentWebview.setWebViewClient(new myWebClient());
            paymentWebview.getSettings().setJavaScriptEnabled(true);
            paymentWebview.loadUrl(url);
            paymentWebview.setFocusable(true);
            paymentWebview.setVisibility(View.VISIBLE);
            loaderView.setVisibility(View.VISIBLE);

        }
        // }
    }

    private void addMoneyToWallet() {
        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "addMoneyUserWallet");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("fAmount", Utils.getText(rechargeBox));
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    rechargeBox.setText("");
                    String memberBalance = generalFunc.getJsonValue("MemberBalance", responseString);
                    ((MTextView) findViewById(R.id.walletamountTxt)).setText(generalFunc.convertNumberWithRTL(memberBalance));

                    generalFunc.storeData(Utils.USER_PROFILE_JSON, generalFunc.getJsonValue(Utils.message_str, responseString));
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str_one, responseString)));
                } else {
                    String message = generalFunc.getJsonValue(Utils.message_str, responseString);
                    if (message.equalsIgnoreCase("LBL_NO_CARD_AVAIL_NOTE")) {

                        final GenerateAlertBox generateAlert = new GenerateAlertBox(getActContext());
                        generateAlert.setContentMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                        generateAlert.setPositiveBtn(generalFunc.retrieveLangLBl("", "LBL_ADD_CARD"));
                        generateAlert.setNegativeBtn(generalFunc.retrieveLangLBl("", "LBL_CANCEL_TXT"));
                        generateAlert.setBtnClickList(btn_id -> {

                            if (btn_id == 1) {
                                generateAlert.closeAlertBox();
                                Bundle bn = new Bundle();
                                new StartActProcess(getActContext()).startActForResult(CardPaymentActivity.class, bn, Utils.CARD_PAYMENT_REQ_CODE);

                            } else {
                                generateAlert.closeAlertBox();
                            }

                        });

                        generateAlert.showAlertBox();

                    } else {
                        generalFunc.showGeneralMessage("",
                                generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                    }

                }
            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();

    }

    @Override
    public void onBackPressed() {

        if (paymentWebview.getVisibility() == View.VISIBLE) {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_CANCEL_PAYMENT_PROCESS"), generalFunc.retrieveLangLBl("", "LBL_NO"), generalFunc.retrieveLangLBl("", "LBL_YES"), buttonId -> {
                if (buttonId == 1) {
                    paymentWebview.setVisibility(View.GONE);
                    paymentWebview.stopLoading();
                    loaderView.setVisibility(View.GONE);
                }
            });

            return;
        }
        super.onBackPressed();
    }

    public void closeLoader() {
        if (loading_wallet_history.getVisibility() == View.VISIBLE) {
            loading_wallet_history.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }
        errorView.setOnRetryListener(() -> getTransactionHistory(false));
    }

    public void getTransactionHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
            scrollView.setVisibility(View.VISIBLE);
        }
        if (loading_wallet_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_wallet_history.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "getTransactionHistory");
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("page", next_page_str);
        //parameters.put("TimeZone", generalFunc.getTimezone());


        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                closeLoader();
                scrollView.setVisibility(View.VISIBLE);

                String LBL_BALANCE = generalFunc.getJsonValue("user_available_balance", responseString);

                ((MTextView) findViewById(R.id.yourBalTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_USER_BALANCE"));
                ((MTextView) findViewById(R.id.walletamountTxt)).setText(generalFunc.convertNumberWithRTL(LBL_BALANCE));


            } else {
                if (isLoadMore == false) {
                    generateErrorView();
                }

            }

            mIsLoading = false;
        });
        exeWebServer.execute();
    }

    public Context getActContext() {
        return MyWalletActivity.this;
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    @Override
    protected void onDestroy() {
        // store prev service id
        if (getIntent().hasExtra("iServiceId")) {
            generalFunc.storeData(Utils.iServiceId_KEY, getIntent().getStringExtra("iServiceId"));
        }
        super.onDestroy();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == SEL_CARD) {
            getTransactionHistory(false);
            rechargeBox.setText("");
        } else if (resultCode == RESULT_OK && requestCode == TRANSFER_MONEY) {
            getTransactionHistory(false);
            getWalletBalDetails();
        }
    }


    public void getWalletBalDetails() {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "GetMemberWalletBalance");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {


            if (responseString != null && !responseString.equals("")) {

                closeLoader();

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    try {

                        String userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
                        JSONObject object = generalFunc.getJsonObject(userProfileJson);

                        ((MTextView) findViewById(R.id.walletamountTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValue("MemberBalance", responseString)));

                        if (!generalFunc.getJsonValue("user_available_balance", userProfileJson).equalsIgnoreCase(generalFunc.getJsonValue("MemberBalance", responseString))) {
                            generalFunc.storeData(Utils.ISWALLETBALNCECHANGE, "Yes");
                        }


                    } catch (Exception e) {

                    }

                }

            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isCheck) {
        UpdateUserWalletAdjustment(isCheck);
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            if (view.getId() == btn_type2.getId()) {
                checkValues();
            } else if (view.getId() == btn_type1.getId()) {
                new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
            }
            /*Go Pay view Click handling Start*/
            else if (view.getId() == btn_type4.getId()) {

//                setBounceAnimation(findViewById(R.id.btn_type4), () -> {

//                });

            }
            /*Go Pay view Click handling End*/

            switch (view.getId()) {
                case R.id.backImgView:
                    onBackPressed();
                    break;

                case R.id.viewTransactionsTxt:
                    new StartActProcess(getActContext()).startAct(MyWalletHistoryActivity.class);
                    break;

                case R.id.addMoneybtn1:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_1", userProfileJson));
                    break;

                case R.id.addMoneybtn2:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_2", userProfileJson));
                    break;

                case R.id.addMoneybtn3:
                    rechargeBox.setText(generalFunc.getJsonValue("WALLET_FIXED_AMOUNT_3", userProfileJson));
                    break;

                case R.id.termsTxt:
                    Bundle bn = new Bundle();
                    bn.putString("staticpage", "4");
                    new StartActProcess(getActContext()).startActWithData(StaticPageActivity.class, bn);
                    break;


                case R.id.addTransferBtnArea:
                    btn_type4.performClick();
                    break;

                /*Go Pay view Click handling start*/

                case R.id.viewTransactionsBtnArea:
                    btn_type1.performClick();
                    break;




                /*Go Pay view Click handling end*/
            }
        }

    }


    private void setBounceAnimation(View view, BounceAnimListener bounceAnimListener) {
        Animation anim = AnimationUtils.loadAnimation(getActContext(), R.anim.bounce_interpolator);
        anim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {

                if (bounceAnimListener != null) {
                    bounceAnimListener.onAnimationFinished();
                }
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        view.startAnimation(anim);
    }

    private interface BounceAnimListener {
        void onAnimationFinished();
    }


}

