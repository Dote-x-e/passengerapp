package com.fragments;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.adapter.files.HistoryRecycleAdapter;
import com.levaeu.passenger.HistoryActivity;
import com.levaeu.passenger.HistoryDetailActivity;
import com.levaeu.passenger.R;
import com.general.files.ExecuteWebServerUrl;
import com.general.files.GeneralFunctions;
import com.general.files.StartActProcess;
import com.utils.Utils;
import com.view.ErrorView;
import com.view.MTextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 */
public class HistoryFragment extends Fragment implements HistoryRecycleAdapter.OnItemClickListener {

    View view;
    ProgressBar loading_ride_history;
    MTextView noRidesTxt;
    RecyclerView historyRecyclerView;
    ErrorView errorView;
    HistoryRecycleAdapter historyRecyclerAdapter;
    ArrayList<HashMap<String, String>> list;
    boolean mIsLoading = false;
    boolean isNextPageAvailable = false;
    String next_page_str = "";
    GeneralFunctions generalFunc;
    HistoryActivity historyAct;
    String userProfileJson = "";
    int HISTORYDETAILS = 1;
    String APP_TYPE;
    ArrayList<HashMap<String, String>> filterlist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_booking, container, false);

        loading_ride_history = (ProgressBar) view.findViewById(R.id.loading_my_bookings);
        noRidesTxt = (MTextView) view.findViewById(R.id.noRidesTxt);
        historyRecyclerView = (RecyclerView) view.findViewById(R.id.myBookingsRecyclerView);
        errorView = (ErrorView) view.findViewById(R.id.errorView);
        historyAct = (HistoryActivity) getActivity();
        generalFunc = historyAct.generalFunc;
        userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
        APP_TYPE = generalFunc.getJsonValue("APP_TYPE", userProfileJson);

        list = new ArrayList<>();
        historyRecyclerAdapter = new HistoryRecycleAdapter(getActContext(), list, generalFunc, false);
        historyRecyclerView.setAdapter(historyRecyclerAdapter);
        historyRecyclerAdapter.setOnItemClickListener(this);

        historyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int visibleItemCount = recyclerView.getLayoutManager().getChildCount();
                int totalItemCount = recyclerView.getLayoutManager().getItemCount();
                int firstVisibleItemPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();

                int lastInScreen = firstVisibleItemPosition + visibleItemCount;
                if ((lastInScreen == totalItemCount) && !(mIsLoading) && isNextPageAvailable) {

                    mIsLoading = true;
                    historyRecyclerAdapter.addFooterView();

                    getBookingsHistory(true);

                } else if (!isNextPageAvailable) {
                    historyRecyclerAdapter.removeFooterView();
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

    public boolean isDeliver() {
        if (getArguments().getString("HISTORY_TYPE").equals(Utils.CabGeneralType_Deliver)) {
            return true;
        }
        return false;
    }

    @Override
    public void onItemClickList(View v, int position) {
        Utils.hideKeyboard(getActivity());
        Bundle bn = new Bundle();
        bn.putString("TripData", list.get(position).get("JSON"));
        new StartActProcess(getActContext()).startActForResult(HistoryDetailActivity.class, bn, HISTORYDETAILS);
    }

    public void getBookingsHistory(final boolean isLoadMore) {
        if (errorView.getVisibility() == View.VISIBLE) {
            errorView.setVisibility(View.GONE);
        }
        if (loading_ride_history.getVisibility() != View.VISIBLE && isLoadMore == false) {
            loading_ride_history.setVisibility(View.VISIBLE);
        }

        if (!isLoadMore) {
            removeNextPageConfig();
            list.clear();
            historyRecyclerAdapter.notifyDataSetChanged();

        }

        final HashMap<String, String> parameters = new HashMap<String, String>();
        parameters.put("type", getArguments().getString("HISTORY_TYPE"));
        parameters.put("iUserId", generalFunc.getMemberId());
        parameters.put("UserType", Utils.app_type);
        parameters.put("vFilterParam", historyAct.selFilterType);
        //parameters.put("eType", getArguments().getString("HISTORY_TYPE"));
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
                        String LBL_JOB_LOCATION_TXT="",LBL_RENTAL_CATEGORY_TXT="",LBL_CANCELED_TXT="";
                        if (arr_rides.length()>0) {
                            LBL_JOB_LOCATION_TXT = generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT");
                            LBL_RENTAL_CATEGORY_TXT = generalFunc.retrieveLangLBl("", "LBL_RENTAL_CATEGORY_TXT");
                            LBL_CANCELED_TXT = generalFunc.retrieveLangLBl("", "LBL_CANCELED_TXT");
                        }

                        for (int i = 0; i < arr_rides.length(); i++) {
                            JSONObject obj_temp = generalFunc.getJsonObject(arr_rides, i);

                            HashMap<String, String> map = new HashMap<String, String>();

                            map.put("tTripRequestDateOrig", generalFunc.getJsonValueStr("tTripRequestDateOrig", obj_temp));
                            map.put("ConvertedTripRequestDateDate", generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("tTripRequestDateOrig", obj_temp), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));
                            map.put("CurrencySymbol", generalFunc.getJsonValueStr("CurrencySymbol", obj_temp));
                            map.put("tSaddress", generalFunc.getJsonValueStr("tSaddress", obj_temp));
                            map.put("tDaddress", generalFunc.getJsonValueStr("tDaddress", obj_temp));
                            map.put("vRideNo", generalFunc.convertNumberWithRTL(generalFunc.getJsonValueStr("vRideNo", obj_temp)));

                            map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                            map.put("LBL_Status", generalFunc.retrieveLangLBl("", "LBL_Status"));
                            map.put("is_rating", generalFunc.getJsonValueStr("is_rating", obj_temp));
                            map.put("iTripId", generalFunc.getJsonValueStr("iTripId", obj_temp));

                            String eType = generalFunc.getJsonValueStr("eType", obj_temp);
                            String eFly = generalFunc.getJsonValueStr("eFly", obj_temp);
                            String eFareType = generalFunc.getJsonValueStr("eFareType", obj_temp);

                            if (eType.equalsIgnoreCase("deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                // map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("Sender Location", "LBL_SENDER_LOCATION"));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("Receiver's Location", "LBL_RECEIVER_LOCATION"));
                            } else {
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_PICK_UP_LOCATION"));
                                // map.put("eType", generalFunc.getJsonValue("eType", obj_temp));
                                map.put("LBL_DEST_LOCATION", generalFunc.retrieveLangLBl("", "LBL_DEST_LOCATION"));
                            }

                            if (eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                map.put("tDaddress", "");
                            }

                            if (eType.equalsIgnoreCase("deliver") || eType.equalsIgnoreCase(Utils.eType_Multi_Delivery)) {
                                map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_DELIVERY"));
                            } else if (eType.equalsIgnoreCase(Utils.CabGeneralType_Ride)) {
                                map.put("eType", generalFunc.retrieveLangLBl("Delivery", "LBL_RIDE"));
                            }
                            else {
                                map.put("LBL_PICK_UP_LOCATION", generalFunc.retrieveLangLBl("", "LBL_JOB_LOCATION_TXT"));
                                map.put("eType", generalFunc.retrieveLangLBl("", "LBL_SERVICES"));
                            }

                            map.put("LBL_HEADER_RDU_FLY_RIDE", generalFunc.retrieveLangLBl("", "LBL_HEADER_RDU_FLY_RIDE"));

                            map.put("eFareType", eFareType);
                            map.put("eFly", eFly);

                            map.put("appType", APP_TYPE);
                            map.put("currenteType", eType);

                            map.put("LBL_JOB_LOCATION_TXT", LBL_JOB_LOCATION_TXT);
                            map.put("LBL_RENTAL_CATEGORY_TXT",LBL_RENTAL_CATEGORY_TXT);


                            String eStatus = generalFunc.getJsonValueStr("eStatus", obj_temp);
                            String iActive = generalFunc.getJsonValueStr("iActive", obj_temp);

                            if (generalFunc.getJsonValueStr("eCancelled", obj_temp).equals("Yes")) {
                                map.put("iActive", LBL_CANCELED_TXT);
                            } else {
                                if (iActive.equals("Canceled")) {
                                    map.put("iActive", LBL_CANCELED_TXT);
                                } else if (iActive.equals("Finished")) {
                                    map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_FINISHED_TXT"));
                                } else {

                                    if (iActive == null || iActive.equals("")) {
                                        if (eStatus.equalsIgnoreCase("Pending")) {
                                            map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_PENDING"));

                                        } else if (eStatus.equalsIgnoreCase("Cancel")) {
                                            map.put("iActive", LBL_CANCELED_TXT);

                                        } else if (eStatus.equalsIgnoreCase("Assign")) {
                                            map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_ASSIGNED"));

                                        } else if (eStatus.equalsIgnoreCase("Accepted")) {
                                            map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_ACCEPTED"));
                                        } else if (eStatus.equalsIgnoreCase("Declined")) {
                                            map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_DECLINED"));
                                        } else if (eStatus.equalsIgnoreCase("Failed")) {
                                            map.put("iActive", generalFunc.retrieveLangLBl("", "LBL_FAILED_TXT"));
                                        } else {
                                            map.put("iActive", eStatus);
                                        }
                                    }
                                }
                            }

                            if (isDeliver()) {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("Delivery No", "LBL_DELIVERY_NO"));
                                map.put("LBL_CANCEL_BOOKING", generalFunc.retrieveLangLBl("Cancel Delivery", "LBL_CANCEL_BOOKING"));
                            } else {
                                map.put("LBL_BOOKING_NO", generalFunc.retrieveLangLBl("", "LBL_BOOKING"));
                                map.put("LBL_CANCEL_BOOKING", generalFunc.retrieveLangLBl("", "LBL_CANCEL_BOOKING"));
                            }

                            if (generalFunc.retrieveValue(Utils.APP_DESTINATION_MODE).equalsIgnoreCase(Utils.NONE_DESTINATION)) {
                                map.put("DESTINATION", "No");
                            } else {
                                map.put("DESTINATION", "Yes");
                            }


                            map.put("JSON", obj_temp.toString());
                            map.put("APP_TYPE", APP_TYPE);

                            if (eType.equals(Utils.CabGeneralType_UberX) &&
                                    !eFareType.equalsIgnoreCase(Utils.CabFaretypeRegular)) {

                                map.put("SelectedVehicle", generalFunc.getJsonValueStr("carTypeName", obj_temp));
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleCategory", obj_temp));


                            } else {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vVehicleType", obj_temp));
                            }


                            map.put("eAutoAssign", generalFunc.getJsonValueStr("eAutoAssign", obj_temp));
                            map.put("vPackageName", generalFunc.getJsonValueStr("vPackageName", obj_temp));
                            if (eFareType.equalsIgnoreCase(Utils.CabFaretypeFixed) && generalFunc.getJsonValueStr("moreServices", obj_temp).equalsIgnoreCase("No")) {
                                map.put("SelectedCategory", generalFunc.getJsonValueStr("vCategory", obj_temp));

                            }

                            map.put("moreServices", generalFunc.getJsonValueStr("moreServices", obj_temp));
                            map.put("vServiceTitle", generalFunc.getJsonValueStr("vServiceTitle", obj_temp));

                            map.put("formattedListingDate", generalFunc.getDateFormatedType(generalFunc.getJsonValueStr("tTripRequestDateOrig", obj_temp), Utils.OriginalDateFormate, Utils.getDetailDateFormat(getActContext())));

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
                        historyAct.filterManage(filterlist);
                    }

                    if (!nextPage.equals("") && !nextPage.equals("0")) {
                        next_page_str = nextPage;
                        isNextPageAvailable = true;
                    } else {
                        removeNextPageConfig();
                    }

                    historyRecyclerAdapter.notifyDataSetChanged();

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
        historyRecyclerAdapter.removeFooterView();
    }

    public void closeLoader() {
        if (loading_ride_history.getVisibility() == View.VISIBLE) {
            loading_ride_history.setVisibility(View.GONE);
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


    public Context getActContext() {
        return historyAct.getActContext();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            next_page_str = "2";
            userProfileJson = generalFunc.retrieveValue(Utils.USER_PROFILE_JSON);
            list.clear();
            getBookingsHistory(true);

        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Utils.hideKeyboard(getActivity());
    }
}
