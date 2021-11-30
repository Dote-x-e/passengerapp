package com.fragments;


import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ProgressBar;

import com.adapter.files.CustSpinnerAdapter;
import com.adapter.files.MyBookingsRecycleAdapter;
import com.levaeu.passenger.HistoryActivity;
import com.levaeu.passenger.R;
import com.datepicker.files.SlideDateTimeListener;
import com.datepicker.files.SlideDateTimePicker;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.Logger;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MButton;
import com.view.MTextView;
import com.view.MaterialRippleLayout;
import com.view.editBox.MaterialEditText;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

/**
 * A simple {@link Fragment} subclass.
 */
public class BookingFragment extends Fragment implements MyBookingsRecycleAdapter.OnItemClickListener {


    View view;

    ProgressBar loading_my_bookings;
    MTextView noRidesTxt;

    RecyclerView myBookingsRecyclerView;
    ErrorView errorView;

    MyBookingsRecycleAdapter myBookingsRecyclerAdapter;

    ArrayList<HashMap<String, String>> list;

    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;

    String next_page_str = "";
    String APP_TYPE = "";

    GeneralFunctions generalFunc;

    HistoryActivity myBookingAct;
    String selectedDateTime = "";
    String selectedDateTimeZone = "";
    android.support.v7.app.AlertDialog alertDialog_surgeConfirm;
    ArrayList<HashMap<String, String>> filterlist;
    AlertDialog dialog_declineOrder;
    BookingFragment bookingFragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking, container, false);

        loading_my_bookings = (ProgressBar) view.findViewById(R.id.loading_my_bookings);
        noRidesTxt = (MTextView) view.findViewById(R.id.noRidesTxt);
        myBookingsRecyclerView = (RecyclerView) view.findViewById(R.id.myBookingsRecyclerView);
        errorView = (ErrorView) view.findViewById(R.id.errorView);

        myBookingAct = (HistoryActivity) getActivity();
        generalFunc = myBookingAct.generalFunc;

        APP_TYPE = generalFunc.getJsonValue("APP_TYPE", generalFunc.retrieveValue(Utils.USER_PROFILE_JSON));

        list = new ArrayList<>();
        myBookingsRecyclerAdapter = new MyBookingsRecycleAdapter(getActContext(), list, generalFunc, false);
        myBookingsRecyclerView.setAdapter(myBookingsRecyclerAdapter);
        myBookingsRecyclerAdapter.setOnItemClickListener(this);
        bookingFragment = myBookingAct.getBookingFrag();


        myBookingsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {

                    mIsLoading = true;
                    myBookingsRecyclerAdapter.addFooterView();

                    getBookingsHistory(true);

                } else if (!isNextPageAvailable) {
                    myBookingsRecyclerAdapter.removeFooterView();
                }
            }
        });


        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getBookingsHistory(false);

    }

    public boolean isDeliver(String eType) {
        if (getArguments().getString("BOOKING_TYPE").equals(Utils.CabGeneralType_Deliver) || eType.equals("Deliver")) {
            return true;
        }
        return false;
    }

    int pos = 0;

    public void getDeclineReasonsList() {
        HashMap<String, String> parameters = new HashMap<>();

        parameters.put("type", "GetCancelReasons");
        parameters.put("iCabBookingId", list.get(pos).get("iCabBookingId"));
        parameters.put("iMemberId", generalFunc.getMemberId());
        parameters.put("eUserType", Utils.app_type);

        ExecuteWebServerUrl exeServerTask = new ExecuteWebServerUrl(myBookingAct.getActContext(), parameters);
        exeServerTask.setLoaderConfig(myBookingAct.getActContext(), true, generalFunc);
        exeServerTask.setDataResponseListener(responseString -> {

            if (!responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail) {
                    showDeclineReasonsAlert(responseString);
                } else {
                    generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
                }

            } else {
                generalFunc.showError();
            }

        });
        exeServerTask.execute();
    }

    String titleDailog = "";

    public void showDeclineReasonsAlert(String responseString) {
        String eType=list.get(pos).get("eType");

        if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_TRIP");
        } else if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
        } else {
            titleDailog = generalFunc.retrieveLangLBl("", "LBL_CANCEL_DELIVERY");
        }


        android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(myBookingAct);
        builder.setTitle(titleDailog);

        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.decline_order_dialog_design, null);
        builder.setView(dialogView);

        MaterialEditText reasonBox = (MaterialEditText) dialogView.findViewById(R.id.inputBox);
        reasonBox.setVisibility(View.GONE);

        reasonBox.setBothText("", generalFunc.retrieveLangLBl("", "LBL_ENTER_REASON"));

        builder.setPositiveButton(generalFunc.retrieveLangLBl("", "LBL_YES"), (dialog, which) -> {

        });

        builder.setNegativeButton(generalFunc.retrieveLangLBl("", "LBL_NO"), (dialog, which) -> {
        });

        ArrayList<HashMap<String, String>> list = new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map = new HashMap<>();
        map.put("title", "-- " + generalFunc.retrieveLangLBl("Select Reason", "LBL_SELECT_CANCEL_REASON") + " --");
        map.put("id", "");
        list.add(map);
        JSONArray arr_msg = generalFunc.getJsonArray(Utils.message_str, responseString);
        if (arr_msg != null) {

            for (int i = 0; i < arr_msg.length(); i++) {

                JSONObject obj_tmp = generalFunc.getJsonObject(arr_msg, i);


                HashMap<String, String> datamap = new HashMap<>();
                datamap.put("title", generalFunc.getJsonValueStr("vTitle", obj_tmp));
                datamap.put("id", generalFunc.getJsonValueStr("iCancelReasonId", obj_tmp));
                list.add(datamap);
            }

            HashMap<String, String> othermap = new HashMap<>();
            othermap.put("title", generalFunc.retrieveLangLBl("", "LBL_OTHER_TXT"));
            othermap.put("id", "");
            list.add(othermap);

            AppCompatSpinner spinner = (AppCompatSpinner) dialogView.findViewById(R.id.declineReasonsSpinner);
            CustSpinnerAdapter adapter = new CustSpinnerAdapter(getActContext(), list);
            spinner.setAdapter(adapter);

            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (spinner.getSelectedItemPosition() == (list.size() - 1)) {
                        reasonBox.setVisibility(View.VISIBLE);
                        //dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(myBookingAct.getResources().getColor(R.color.black));
                    } else if (spinner.getSelectedItemPosition() == 0) {
                        //dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(myBookingAct.getResources().getColor(R.color.gray));
                        reasonBox.setVisibility(View.GONE);
                    } else {
                        // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(true);
                        dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(myBookingAct.getResources().getColor(R.color.black));
                        reasonBox.setVisibility(View.GONE);
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            dialog_declineOrder = builder.create();
            dialog_declineOrder.show();
            // dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setClickable(false);
            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(myBookingAct.getResources().getColor(R.color.gray));

            dialog_declineOrder.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {


                if (spinner.getSelectedItemPosition() == 0) {
                    return;
                }

                if (!Utils.checkText(reasonBox) && spinner.getSelectedItemPosition() == (list.size() - 1)) {
                    reasonBox.setError(generalFunc.retrieveLangLBl("", "LBL_FEILD_REQUIRD_ERROR_TXT"));
                    return;
                }

                cancelBooking(this.list.get(pos).get("iCabBookingId"), list.get(spinner.getSelectedItemPosition()).get("id"), reasonBox.getText().toString().trim());

                dialog_declineOrder.dismiss();
            });

            dialog_declineOrder.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(view -> dialog_declineOrder.dismiss());
        } else {
            generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", "LBL_NO_DATA_AVAIL"));
        }
    }

    public void onItemClickList(View v, int position, boolean isSchedulebooking) {
        Utils.hideKeyboard(getActContext());

        if (isSchedulebooking) {
            rescheduleBooking(position);
        } else {
            if (list.get(position).get("eStatus").equalsIgnoreCase(generalFunc.retrieveLangLBl("", "LBL_DECLINE_TXT")) || list.get(position).get("eStatus").equalsIgnoreCase(generalFunc.retrieveLangLBl("", "LBL_CANCELLED"))) {

                rescheduleBooking(position);
            } else {
                pos = position;
                getDeclineReasonsList();
            }
        }
    }

    @Override
    public void onItemClickList(int position, boolean isSchedulebooking) {
        if (isSchedulebooking) {
            chooseDateTime(list.get(position).get("iCabBookingId"));
        } else {
            pos = position;
            getDeclineReasonsList();
        }

    }

    @Override
    public void onItemClickList(int position) {

        generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("", "LBL_BOOKING_CANCEL_REASON"), list.get(position).get("vCancelReason"));
    }

    @Override
    public void onViewServiceClickList(View v, int position) {
        Bundle bundle = new Bundle();
        bundle.putString("iCabBookingId", list.get(position).get("iCabBookingId"));
        bundle.putString("iDriverId", list.get(position).get("iDriverId"));

    }


    public void chooseDateTime(String iCabBookingId) {

        new SlideDateTimePicker.Builder(getActivity().getSupportFragmentManager())
                .setListener(new SlideDateTimeListener() {
                    @Override
                    public void onDateTimeSet(Date date) {

                        selectedDateTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);
                        selectedDateTimeZone = Calendar.getInstance().getTimeZone().getID();

                        if (!Utils.isValidTimeSelect(date, TimeUnit.HOURS.toMillis(1))) {

                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Invalid pickup time", "LBL_INVALID_PICKUP_TIME"),
                                    generalFunc.retrieveLangLBl("Please make sure that pickup time is after atleast an hour from now.", "LBL_INVALID_PICKUP_NOTE_MSG"));

                            return;
                        }

                        if (!Utils.isValidTimeSelectForLater(date, TimeUnit.DAYS.toMillis(30))) {

                            generalFunc.showGeneralMessage(generalFunc.retrieveLangLBl("Invalid pickup time", "LBL_INVALID_PICKUP_TIME"),
                                    generalFunc.retrieveLangLBl("Please make sure that pickup time is after atleast an 1 month from now.", "LBL_INVALID_PICKUP_NOTE_MONTH_MSG"));
                            return;
                        }

                        String selectedTime = Utils.convertDateToFormat("yyyy-MM-dd HH:mm:ss", date);

                        updateBookingDate(iCabBookingId, selectedTime, "No");
                    }

                    @Override
                    public void onDateTimeCancel() {

                    }

                })

                .setInitialDate(new Date())
                .setMinDate(Calendar.getInstance().getTime())
//                .setMinDate(getCurrentDate1hrAfter())
                //.setMaxDate(maxDate)
//                .setIs24HourTime(true)
                .setIs24HourTime(false)
                //.setTheme(SlideDateTimePicker.HOLO_DARK)
                .setIndicatorColor(getResources().getColor(R.color.appThemeColor_2))
                .build()
                .show();
    }

    public void updateBookingDate(String iCabBookingId, String selDate, String eConfirmByUser) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "UpdateBookingDateRideDelivery");
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("scheduleDate", selDate);
        parameters.put("eConfirmByUser", eConfirmByUser);
        parameters.put("iUserId", generalFunc.getMemberId());

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseObj);

                if (isDataAvail) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);


                } else {

                    if (generalFunc.getJsonValueStr("SurgePrice", responseObj) != null && !generalFunc.getJsonValueStr("SurgePrice", responseObj).equalsIgnoreCase("")) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(getActContext());
                        builder.setTitle("");
                        builder.setCancelable(false);
                        LayoutInflater inflater = (LayoutInflater) getActContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        View dialogView = inflater.inflate(R.layout.surge_confirm_design, null);
                        builder.setView(dialogView);

                        MTextView payableAmountTxt;
                        MTextView payableTxt;

                        ((MTextView) dialogView.findViewById(R.id.headerMsgTxt)).setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                        ((MTextView) dialogView.findViewById(R.id.surgePriceTxt)).setText(generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("SurgePrice", responseObj)));

                        ((MTextView) dialogView.findViewById(R.id.tryLaterTxt)).setText(generalFunc.retrieveLangLBl("", "LBL_TRY_LATER"));

                        payableTxt = (MTextView) dialogView.findViewById(R.id.payableTxt);
                        payableAmountTxt = (MTextView) dialogView.findViewById(R.id.payableAmountTxt);
                        payableTxt.setText(generalFunc.retrieveLangLBl("", "LBL_PAYABLE_AMOUNT"));
                        payableAmountTxt.setVisibility(View.GONE);
                        payableTxt.setVisibility(View.VISIBLE);

                        String total_fare=generalFunc.getJsonValueStr("total_fare", responseObj);
                        if (total_fare != null && !total_fare.equalsIgnoreCase("")) {
                            payableAmountTxt.setVisibility(View.VISIBLE);
                            payableTxt.setVisibility(View.GONE);
                            payableAmountTxt.setText(generalFunc.retrieveLangLBl("Approx payable amount", "LBL_APPROX_PAY_AMOUNT") + ": " + total_fare);

                        }


                        MButton btn_type2 = ((MaterialRippleLayout) dialogView.findViewById(R.id.btn_type2)).getChildView();
                        btn_type2.setText(generalFunc.retrieveLangLBl("", "LBL_ACCEPT_SURGE"));
                        btn_type2.setId(Utils.generateViewId());

                        btn_type2.setOnClickListener(view -> {
                            updateBookingDate(iCabBookingId, selDate, "Yes");
                            alertDialog_surgeConfirm.dismiss();
                        });
                        (dialogView.findViewById(R.id.tryLaterTxt)).setOnClickListener(view -> alertDialog_surgeConfirm.dismiss());
                        alertDialog_surgeConfirm = builder.create();
                        alertDialog_surgeConfirm.setCancelable(false);
                        alertDialog_surgeConfirm.setCanceledOnTouchOutside(false);
                        if (generalFunc.isRTLmode()) {
                            generalFunc.forceRTLIfSupported(alertDialog_surgeConfirm);
                        }

                        alertDialog_surgeConfirm.show();


                    } else {
                        generalFunc.showGeneralMessage("", generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                    }
                }


            } else {
                generalFunc.showError();
            }
        });
        exeWebServer.execute();
    }

    public void rescheduleBooking(int position) {
        HashMap<String, String> posData = list.get(position);
        Bundle bundle = new Bundle();
        bundle.putString("SelectedVehicleTypeId", posData.get("SelectedCategoryId"));
        bundle.putBoolean("isufx", true);
        bundle.putString("latitude", posData.get("vSourceLatitude"));
        bundle.putString("longitude", posData.get("vSourceLongitude"));
        bundle.putString("address", posData.get("vSourceAddresss"));
        bundle.putString("SelectDate", posData.get("selecteddatetime"));
        bundle.putString("SelectvVehicleType", posData.get("SelectedVehicle"));
        String SelectedPrice= posData.get("SelectedPrice");

        bundle.putString("SelectvVehiclePrice", SelectedPrice);
        bundle.putString("iUserAddressId", posData.get("iUserAddressId"));
        bundle.putString("type", Utils.CabReqType_Later);
        bundle.putString("Sdate", generalFunc.getDateFormatedType(posData.get("dBooking_dateOrig"), Utils.OriginalDateFormate, Utils.dateFormateForBooking));
        bundle.putString("Stime", posData.get("selectedtime"));


        if (posData.get("SelectedAllowQty").equalsIgnoreCase("yes")) {

            bundle.putString("Quantity", posData.get("iQty"));
            bundle.putString("Quantityprice", posData.get("SelectedCurrencySymbol") + "" + (GeneralFunctions.parseIntegerValue(1, posData.get("iQty"))) * (GeneralFunctions.parseIntegerValue(1, SelectedPrice)) + "");
        } else {
            bundle.putString("Quantityprice", posData.get("SelectedCurrencySymbol") + "" + SelectedPrice);
            bundle.putString("Quantity", "0");
        }

        bundle.putString("iCabBookingId", posData.get("iCabBookingId"));
        bundle.putBoolean("isRebooking", true);
        bundle.putString("iDriverId", posData.get("iDriverId"));
        bundle.putBoolean("isList", true);

    }

    public void getBookingsHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_my_bookings.getVisibility() != View.VISIBLE && !isLoadMore) {
            loading_my_bookings.setVisibility(View.VISIBLE);
        }

        if (!isLoadMore) {
            removeNextPageConfig();
            list.clear();
            myBookingsRecyclerAdapter.notifyDataSetChanged();
        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "checkBookings");
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("bookingType", getArguments().getString("BOOKING_TYPE"));
        parameters.put("vFilterParam", myBookingAct.selFilterType);
        if (isLoadMore) {
            parameters.put("page", next_page_str);
        }

        noRidesTxt.setVisibility(View.GONE);

        final ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setDataResponseListener(responseString -> {

            noRidesTxt.setVisibility(View.GONE);
            JSONObject responseObj=generalFunc.getJsonObject(responseString);

            if (responseObj != null && !responseObj.equals("")) {

                closeLoader();

                if (GeneralFunctions.checkDataAvail(Utils.action_str, responseObj)) {

                    if (!isLoadMore) {
                        list.clear();

                    }

                    String nextPage = generalFunc.getJsonValueStr("NextPage", responseObj);
                    JSONArray arr_rides = generalFunc.getJsonArray(Utils.message_str, responseObj);

                    if (arr_rides != null && arr_rides.length() > 0) {

                        String LBL_DELIVERY="",LBL_RIDE="",LBL_SERVICES="",LBL_VIEW_REASON="",LBL_REBOOKING="",LBL_CANCEL_BOOKING="",LBL_RESCHEDULE="",LBL_CANCELLED="",LBL_VIEW_REQUESTED_SERVICES="",LBL_Status="",
                                LBL_FLY="";

                        if (arr_rides.length()>0) {
                            LBL_DELIVERY = generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY");
                            LBL_RIDE = generalFunc.retrieveLangLBl("", "LBL_RIDE");
                            LBL_SERVICES = generalFunc.retrieveLangLBl("", "LBL_SERVICES");
                            LBL_VIEW_REASON = generalFunc.retrieveLangLBl("", "LBL_VIEW_REASON");
                            LBL_REBOOKING = generalFunc.retrieveLangLBl("", "LBL_REBOOKING");
                            LBL_CANCEL_BOOKING = generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING");
                            LBL_RESCHEDULE = generalFunc.retrieveLangLBl("", "LBL_RESCHEDULE");
                            LBL_CANCELLED = generalFunc.retrieveLangLBl("", "LBL_CANCELLED");
                            LBL_VIEW_REQUESTED_SERVICES = generalFunc.retrieveLangLBl("", "LBL_VIEW_REQUESTED_SERVICES");
                            LBL_Status = generalFunc.retrieveLangLBl("", "LBL_Status");
                            LBL_FLY = generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_FLY_RIDE");

                        }


                        for (int i = 0; i < arr_rides.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);

                            HashMap<String, String> map = new HashMap<String, String>();


                            map.put("dBooking_dateOrig", generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp));
                            map.put("vSourceAddresss", generalFunc.getJsonValueStr("vSourceAddresss", obj_temp));
                            map.put("tDestAddress", generalFunc.getJsonValueStr("tDestAddress", obj_temp));
                            String vBookingNo = generalFunc.getJsonValueStr("vBookingNo", obj_temp);
                            map.put("vBookingNo", vBookingNo);
                            map.put("formattedVBookingNo", generalFunc.convertNumberWithRTL(vBookingNo));
                            map.put("eStatus", generalFunc.getJsonValueStr("eStatus", obj_temp));
                            map.put("eStatusV", generalFunc.getJsonValueStr("eStatus", obj_temp));
                            map.put("iCabBookingId", generalFunc.getJsonValueStr("iCabBookingId", obj_temp));

                            map.put("eType", generalFunc.getJsonValueStr("eType", obj_temp));
                            map.put("eFly", generalFunc.getJsonValueStr("eFly", obj_temp));

                            map.put("LBL_DELIVERY",LBL_DELIVERY);
                            map.put("LBL_RIDE",LBL_RIDE);
                            map.put("LBL_FLY",LBL_FLY);
                            map.put("LBL_SERVICES", LBL_SERVICES);
                            map.put("LBL_VIEW_REASON",LBL_VIEW_REASON);
                            map.put("LBL_REBOOKING",LBL_REBOOKING);
                            map.put("LBL_CANCEL_BOOKING",LBL_CANCEL_BOOKING);
                            map.put("LBL_RESCHEDULE",LBL_RESCHEDULE);


                            map.put("appType", APP_TYPE);

                            String eType = generalFunc.getJsonValueStr("eType", obj_temp);
                            String eCancelBy = generalFunc.getJsonValueStr("eCancelBy", obj_temp);
                            String eStatus = map.get("eStatus");


                            if (eStatus.equals("Completed")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));
                            } else if (eStatus.equals("Cancel")) {
                                if (eType.equals(Utils.CabGeneralType_UberX) && !generalFunc.getJsonValueStr("eFareType", obj_temp).equals(Utils.CabFaretypeRegular)) {
                                    map.put("eStatus", LBL_CANCELLED);
                                } else {
                                    map.put("eStatus", LBL_CANCELLED);
                                }
                            } else if (eStatus.equals("Pending")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("Pending", "LBL_PENDING"));
                            } else if (eStatus.equals("Declined")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_DECLINED"));

                            } else if (eStatus.equals("Accepted")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ACCEPTED"));

                            } else if (eStatus.equalsIgnoreCase("Assign")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));
                            }


                            if (eCancelBy.equals("Driver")) {

                                if (eType.equals(Utils.CabGeneralType_UberX) && !generalFunc.getJsonValueStr("eFareType", obj_temp).equals(Utils.CabFaretypeRegular)) {
                                    map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_PROVIDER"));
                                } else {
                                    if (eType.equals(Utils.CabGeneralType_Ride)) {
                                        map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_DRIVER"));
                                    } else if (eType.equalsIgnoreCase("deliver")) {
                                        map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_CARRIER"));
                                    }

                                }
                            }
                            if (eCancelBy.equals("Admin")) {
                                map.put("eStatus", generalFunc.retrieveLangLBl("", "LBL_CANCELLED_BY_ADMIN"));
                            }

                            if (isDeliver(eType)) {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("Sender Location", "LBL_SENDER_LOCATION"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("Receiver's Location", "LBL_RECEIVER_LOCATION"));
                            } else {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                                map.put("LBL_JOB_LOCATION_TXT", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION"));

                            }

                            map.put("LBL_Status", LBL_Status);
                            map.put("JSON", obj_temp.toString());


                            if (eType.equalsIgnoreCase(Utils.CabGeneralType_UberX)) {
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
                            }


                            if (eType.equals(Utils.CabGeneralType_UberX) /*&&
                                    !generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeRegular)*/) {
                                map.put("selectedtime", generalFunc.getJsonValueStr("selectedtime", obj_temp));

                                map.put("iVehicleTypeId", generalFunc.getJsonValueStr("iVehicleTypeId", obj_temp));
                                map.put("SelectedCategoryId", generalFunc.getJsonValueStr("SelectedCategoryId", obj_temp));

                                map.put("iQty", generalFunc.getJsonValueStr("iQty", obj_temp));

                                map.put("vSourceLatitude", generalFunc.getJsonValueStr("vSourceLatitude", obj_temp));
                                map.put("iDriverId", generalFunc.getJsonValueStr("iDriverId", obj_temp));

                                map.put("vSourceLongitude", generalFunc.getJsonValueStr("vSourceLongitude", obj_temp));

                                map.put("iUserAddressId", generalFunc.getJsonValueStr("iUserAddressId", obj_temp));

                                map.put("dBooking_dateOrig", generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp));

                                map.put("selecteddatetime", generalFunc.getJsonValueStr("selecteddatetime", obj_temp));

                                map.put("SelectedCurrencySymbol", generalFunc.getJsonValueStr("SelectedCurrencySymbol", obj_temp));

                                map.put("SelectedAllowQty", generalFunc.getJsonValueStr("SelectedAllowQty", obj_temp));

                                map.put("SelectedPrice", generalFunc.getJsonValueStr("SelectedPrice", obj_temp));

                                map.put("SelectedVehicle", generalFunc.getJsonValueStr("SelectedVehicle", obj_temp));
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("SelectedCategory", obj_temp));
                            } else {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleType", obj_temp));
                            }

                            if (generalFunc.getJsonValueStr("eFareType", obj_temp).equalsIgnoreCase(Utils.CabFaretypeFixed) && generalFunc.getJsonValueStr("moreServices", obj_temp).equalsIgnoreCase("No")) {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));

                            }
                            map.put("eAutoAssign", generalFunc.getJsonValueStr("eAutoAssign", obj_temp));


                            map.put("vCancelReason", generalFunc.getJsonValueStr("vCancelReason", obj_temp));
                            map.put("LBL_VIEW_REQUESTED_SERVICES", LBL_VIEW_REQUESTED_SERVICES);

                            map.put("listingFormattedDate", generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("dBooking_dateOrig", obj_temp), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
                            map.put("eFareType", generalFunc.getJsonValueStr("eFareType", obj_temp));
                            map.put("moreServices", generalFunc.getJsonValueStr("moreServices", obj_temp));
                            map.put("vServiceTitle", generalFunc.getJsonValueStr("vServiceTitle", obj_temp));
                            map.put("vServiceDetailTitle", generalFunc.getJsonValueStr("vServiceDetailTitle", obj_temp));

                            list.add(map);

                        }
                    }


                    JSONArray arr_type_filter = generalFunc.getJsonArray("AppTypeFilterArr", responseObj);

                    if (arr_type_filter != null && arr_type_filter.length() > 0) {
                        filterlist = new ArrayList<>();
                        for (int i = 0; i < arr_type_filter.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_type_filter, i);

                            HashMap<String, String> map = new HashMap<String, String>();
                            map.put("vTitle", generalFunc.getJsonValueStr("vTitle", obj_temp));
                            map.put("vFilterParam", generalFunc.getJsonValueStr("vFilterParam", obj_temp));
                            filterlist.add(map);
                        }
                    }


                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    myBookingsRecyclerAdapter.notifyDataSetChanged();

                } else {
                    if (list.size() == 0) {
                        removeNextPageConfig();
                        noRidesTxt.setText(generalFunc.retrieveLangLBl("", generalFunc.getJsonValueStr(Utils.message_str, responseObj)));
                        noRidesTxt.setVisibility(View.VISIBLE);
                    }
                }
            } else {
                if (isLoadMore == false) {
                    removeNextPageConfig();
                    generateErrorView();
                }

            }

            mIsLoading = false;
        });
        exeWebServer.execute();
    }

    public void removeNextPageConfig() {
        next_page_str = "";
        isNextPageAvailable = false;
        mIsLoading = false;
        myBookingsRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_my_bookings.getVisibility() == View.VISIBLE) {
            loading_my_bookings.setVisibility(View.GONE);
        }
    }

    public void generateErrorView() {

        closeLoader();

        generalFunc.generateErrorView(errorView, "LBL_ERROR_TXT", "LBL_NO_INTERNET_TXT");

        if (errorView.getVisibility() != View.VISIBLE) {
            errorView.setVisibility(View.VISIBLE);
        }
        errorView.setOnRetryListener(() -> getBookingsHistory(false));
    }


    public void cancelBooking(String iCabBookingId, String iCancelReasonId, String reason) {

        HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", "cancelBooking");
        parameters.put("UserType", Utils.app_type);
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("iCabBookingId", iCabBookingId);
        parameters.put("iCancelReasonId", iCancelReasonId);
        parameters.put("Reason", reason);

        ExecuteWebServerUrl exeWebServer = new ExecuteWebServerUrl(getActContext(), parameters);
        exeWebServer.setLoaderConfig(getActContext(), true, generalFunc);
        exeWebServer.setDataResponseListener(responseString -> {

            if (responseString != null && !responseString.equals("")) {

                boolean isDataAvail = GeneralFunctions.checkDataAvail(Utils.action_str, responseString);

                if (isDataAvail == true) {
                    list.clear();
                    myBookingsRecyclerAdapter.notifyDataSetChanged();
                    getBookingsHistory(false);
                    generalFunc.showGeneralMessage("",
                            generalFunc.retrieveLangLBl("", generalFunc.getJsonValue(Utils.message_str, responseString)));
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
        return myBookingAct.getActContext();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActContext());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //  super.onActivityResult(requestCode, resultCode, data);
        Logger.d("onActivityResult", "::called");

        if (resultCode == getActivity().RESULT_OK) {
            updateBookingDate(data.getStringExtra("iCabBookingId"), data.getStringExtra("SelectDate"), "No");
        }
    }
}
