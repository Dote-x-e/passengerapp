package com.levaeu.passenger;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.utils.Logger;
import com.utils.Utils;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.anim.loader.AVLoadingIndicatorView;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class SettingActivity extends AppCompatActivity {

    MTextView titleTxt;
    ImageView backImgView;
    MaterialEditText langBox;
    MaterialEditText currencyBox;
    GeneralFunctions generalFunc;
    MButton btn_type2;
    int submitBtnId;
    android.support.v7.app.AlertDialog list_language;

    ArrayList<String> items_txt_language = new ArrayList<String>();
    ArrayList<String> items_language_code = new ArrayList<String>();

    ArrayList<String> items_txt_currency = new ArrayList<String>();
    ArrayList<String> items_currency_symbol = new ArrayList<String>();
    String selected_language_code = "";
    String default_selected_language_code = "";

    FrameLayout langSelectArea, currencySelectArea;
    android.support.v7.app.AlertDialog list_currency;

    String selected_currency_symbol = "";
    String selected_currency = "";

    AVLoadingIndicatorView loaderView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        generalFunc = new GeneralFunctions(getActContext());

        titleTxt = (MTextView) findViewById(R.id.titleTxt);
        backImgView = (ImageView) findViewById(R.id.backImgView);
        backImgView.setOnClickListener(new setOnClickList());
        langBox = (MaterialEditText) findViewById(R.id.langBox);
        currencyBox = (MaterialEditText) findViewById(R.id.currencyBox);

        btn_type2 = ((MaterialRippleLayout) findViewById(R.id.btn_type2)).getChildView();

        currencySelectArea = (FrameLayout) findViewById(R.id.currencySelectArea);
        langSelectArea = (FrameLayout) findViewById(R.id.langSelectArea);

        loaderView = (AVLoadingIndicatorView) findViewById(R.id.loaderView);


        submitBtnId = Utils.generateViewId();
        btn_type2.setId(submitBtnId);
        btn_type2.setOnClickListener(new setOnClickList());
        langBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_LANGUAGE_TXT"));
        currencyBox.setBothText(generalFunc.retrieveLangLBl("", "LBL_CURRENCY_TXT"));
        titleTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PREFRANCE_TXT"));
        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_UPDATE"));
        langBox.setText(generalFunc.retrieveValue(Utils.DEFAULT_LANGUAGE_VALUE));
        currencyBox.setText(generalFunc.retrieveValue(Utils.DEFAULT_CURRENCY_VALUE));


        langBox.getLabelFocusAnimator().start();
        currencyBox.getLabelFocusAnimator().start();

        Utils.removeInput(langBox);
        Utils.removeInput(currencyBox);


        langBox.setOnTouchListener(new setOnTouchList());
        currencyBox.setOnTouchListener(new setOnTouchList());
        langBox.setOnClickListener(new setOnClickList());
        currencyBox.setOnClickListener(new setOnClickList());


        buildLanguageList();
    }

    public class setOnTouchList implements View.OnTouchListener {

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP && !view.hasFocus()) {
                view.performClick();
            }
            return true;
        }
    }

    public Context getActContext() {
        return SettingActivity.this;
    }

    public void buildLanguageList() {
        JSONArray languageList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.LANGUAGE_LIST_KEY));

        for (int i = 0; i < languageList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(languageList_arr, i);

            items_txt_language.add(generalFunc.getJsonValue("vTitle", obj_temp.toString()));
            items_language_code.add(generalFunc.getJsonValue("vCode", obj_temp.toString()));

            if ((generalFunc.retrieveValue(Utils.LANGUAGE_CODE_KEY)).equals(generalFunc.getJsonValue("vCode", obj_temp.toString()))) {
                selected_language_code = generalFunc.getJsonValue("vCode", obj_temp.toString());

                langBox.setText(generalFunc.getJsonValue("vTitle", obj_temp.toString()));
            }
        }

        CharSequence[] cs_languages_txt = items_txt_language.toArray(new CharSequence[items_txt_language.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());

        builder.setTitle(getSelectLangText());

        builder.setItems(cs_languages_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                if (list_language != null) {
                    list_language.dismiss();
                }
                selected_language_code = items_language_code.get(item);
                generalFunc.storeData(Utils.DEFAULT_LANGUAGE_VALUE, items_txt_language.get(item));

                langBox.setText(items_txt_language.get(item));

                generalFunc.storeData(Utils.LANGUAGE_CODE_KEY, selected_language_code);
                generalFunc.storeData(Utils.DEFAULT_LANGUAGE_VALUE, items_txt_language.get(item));

            }
        });

        list_language = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_language);
        }

        if (items_txt_language.size() < 2 || generalFunc.retrieveValue("LANGUAGE_OPTIONAL").equalsIgnoreCase("Yes")) {
            langSelectArea.setVisibility(View.GONE);
        }

        buildCurrencyList();

    }


    public void buildCurrencyList() {
        JSONArray currencyList_arr = generalFunc.getJsonArray(generalFunc.retrieveValue(Utils.CURRENCY_LIST_KEY));

        for (int i = 0; i < currencyList_arr.length(); i++) {
            JSONObject obj_temp = generalFunc.getJsonObject(currencyList_arr, i);

            items_txt_currency.add(generalFunc.getJsonValue("vName", obj_temp.toString()));
            items_currency_symbol.add(generalFunc.getJsonValue("vSymbol", obj_temp.toString()));
        }

        CharSequence[] cs_currency_txt = items_txt_currency.toArray(new CharSequence[items_txt_currency.size()]);

        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActContext());
        builder.setTitle(generalFunc.retrieveLangLBl("", "LBL_SELECT_CURRENCY"));

        builder.setItems(cs_currency_txt, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                // Do something with the selection

                if (list_currency != null) {
                    list_currency.dismiss();
                }
                selected_currency_symbol = items_currency_symbol.get(item);

                selected_currency = items_txt_currency.get(item);
                currencyBox.setText(items_txt_currency.get(item));

                generalFunc.storeData(Utils.DEFAULT_CURRENCY_VALUE, selected_currency);


            }
        });

        list_currency = builder.create();

        if (generalFunc.isRTLmode() == true) {
            generalFunc.forceRTLIfSupported(list_currency);
        }

        if (items_txt_currency.size() < 2 || generalFunc.retrieveValue("CURRENCY_OPTIONAL").equalsIgnoreCase("Yes")) {
            currencySelectArea.setVisibility(View.GONE);

      /*      if (items_language_code.size() < 2) {
                langSelectArea.setVisibility(View.GONE);
            }*/
        }

    }


    @Override
    public void onBackPressed() {

        if (loaderView.getVisibility() != View.VISIBLE) {
            super.onBackPressed();
        }
    }

    public String getSelectLangText() {
        return ("" + generalFunc.retrieveLangLBl("Select", "LBL_SELECT_LANGUAGE_HINT_TXT"));
    }

    public void showLanguageList() {
        list_language.show();
    }

    public void showCurrencyList() {
        list_currency.show();
    }

    public class setOnClickList implements View.OnClickListener {

        @Override
        public void onClick(View view) {
            Utils.hideKeyboard(getActContext());
            if (view.getId()
                    == backImgView.getId()) {
                onBackPressed();
            } else if (view.getId()
                    == langBox.getId()) {
                showLanguageList();
            } else if (view.getId()
                    == currencyBox.getId()) {
                showCurrencyList();
            } else if (view.getId() == submitBtnId) {
                changeLanguagedata(selected_language_code);
            }


        }
    }

    public void changeLanguagedata(String langcode) {

        loaderView.setVisibility(View.VISIBLE);

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "changelanguagelabel");
        parameters.put("vLang", langcode);
        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);

        exeWebServer.setDataResponseListener(new ExecuteWebServerUrl.SetDataResponse() {
            @Override
            public void setResponse(String responseString) {
                Logger.d("responseString", "::" + responseString);
                if (responseString != null && !responseString.equals("")) {

                    boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                    if (isDataAvail == true) {

                        generalFunc.storeData(Utils.languageLabelsKey, generalFunc.getJsonValue(Utils.message_str, responseString));
                        generalFunc.storeData(Utils.LANGUAGE_IS_RTL_KEY, generalFunc.getJsonValue("eType", responseString));
                        new Handler().postDelayed(new Runnable() {

                            @Override
                            public void run() {
                                loaderView.setVisibility(View.GONE);
                                generalFunc.restartApp();
                            }
                        }, 2000);


                    } else {
                        loaderView.setVisibility(View.GONE);
                    }
                } else {
                    loaderView.setVisibility(View.GONE);
                }

            }
        });
        exeWebServer.execute();
    }
}
