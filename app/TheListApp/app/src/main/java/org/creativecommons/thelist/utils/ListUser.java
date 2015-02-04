/* The List powered by Creative Commons

   Copyright (C) 2014 Creative Commons

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU Affero General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU Affero General Public License for more details.

   You should have received a copy of the GNU Affero General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/

package org.creativecommons.thelist.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.creativecommons.thelist.R;
import org.creativecommons.thelist.StartActivity;
import org.creativecommons.thelist.authentication.ServerAuthenticate;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ListUser implements ServerAuthenticate {
    public static final String TAG = ListUser.class.getSimpleName();
    private String userName;
    private String userID;
    private String sessionToken;
    private boolean logInState;
    private Context mContext;
    private RequestMethods requestMethods;
    private SharedPreferencesMethods sharedPreferencesMethods;
    //private ArrayList<MainListItem> userItems;
    //private ArrayList<MainListItem> userCategories;

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";
    public static final String PARAM_USER_PASS = "USER_PASS";

    public ListUser(Context mc) {
        mContext = mc;
        requestMethods = new RequestMethods(mContext);
        sharedPreferencesMethods = new SharedPreferencesMethods(mContext);
    }

    public ListUser() {
    }

    public ListUser(String name, String id) {
        this.userName = name;
        this.userID = id;
        this.logInState = false;
    }

    public boolean isUser() {
        //TODO: Check if User exists
        return false;
    }

    public boolean isLoggedIn() {
        SharedPreferences sharedPref = mContext.getSharedPreferences
                (SharedPreferencesMethods.APP_PREFERENCES_KEY, Context.MODE_PRIVATE);

        //TODO: what if this fail?
        logInState = sharedPref.contains(SharedPreferencesMethods.USER_ID_PREFERENCE_KEY)
                && sharedPreferencesMethods.getUserId() != null;

        return logInState;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String name) {
        this.userName = name;
    }

    //TODO: get rid of this eventually
    public void setLogInState(boolean bol) {
        logInState = bol;
    }

    public String getUserID() {
        //TODO: actually get ID
        userID = sharedPreferencesMethods.getUserId();
        //See if sharedPreference methods contains userID
        //If yes: get and return userID; else: return null
        if (userID == null) {
            Log.v(TAG, "You don’t got no userID, man");
            return null;
        } else {
            return userID;
        }
    }

    //TODO: might not need with sharedPreferences
    public void setUserID(String id) {
        this.userID = id;
    }

    public String getSessionToken(){
        //TODO: actually get sessionToken
        //sessionToken = //TODO: GET SESION TOKEN;

        //TODO: IF THERE IS NO SESSION TOKEN? WILL THE AUTHENTICATOR DO THIS?
        return sessionToken;
    }

    public void setSessionToken(String token){
        sessionToken = token;
    }

    public void logOut() {
        //TODO: Figure out what logOut even means…
        //LogOut User
        //Destroy session token?
        userName = null;
        userID = null;
        logInState = false;

        //Clear all sharedPreferences
        sharedPreferencesMethods.ClearAllSharedPreferences();

        //TODO: take you back to startActivity?
        Intent intent = new Intent(mContext, StartActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mContext.startActivity(intent);
    }

    @Override
    public String userSignIn(final String user, final String pass, String authType) throws Exception {
        RequestQueue queue = Volley.newRequestQueue(mContext);
        String url = ApiConstants.LOGIN_USER;
        //Bundle data = new Bundle();

        StringRequest userSignInRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //Get Response
                        if(response == null || response.equals("null")) {
                            Log.v("RESPONSE IS NULL IF YOU ARE HERE", response);
                            requestMethods.showErrorDialog(mContext, "YOU SHALL NOT PASS",
                                    "Sure you got your email/password combo right?");
                        } else {
                            Log.v("THIS IS THE RESPONSE FOR LOGIN: ", response);
                            try {
                                JSONObject res = new JSONObject(response);
                                //TODO: remove when endpoints work without ID
                                userID = res.getString(ApiConstants.USER_ID);
                                sessionToken = res.getString(ApiConstants.USER_TOKEN);
                                //Save userID in sharedPreferences
                                sharedPreferencesMethods.SaveSharedPreference
                                        (SharedPreferencesMethods.USER_ID_PREFERENCE_KEY, userID);
                                //Add items chosen before login to userlist
                                addSavedItemsToUserList();
                                //TODO: also add category preferences
                                //pass userID to the activity
                                //TODO: figure out if this is still needed
                                //listener.UserLoggedIn(userID);
                            } catch (JSONException e) {
                                Log.v(TAG,e.getMessage());
                                //TODO: add proper error message
                                requestMethods.showErrorDialog(mContext, mContext.getString
                                                (R.string.login_error_exception_title),
                                        mContext.getString(R.string.login_error_exception_message));
                            }
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                requestMethods.showErrorDialog(mContext,
                        mContext.getString(R.string.login_error_title),
                        mContext.getString(R.string.login_error_message));
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", user);
                params.put(ApiConstants.USER_PASSWORD, pass);

                return params;
            }
        };
        queue.add(userSignInRequest);
        return sessionToken;
    }


    @Override
    public String userSignUp(String name, String email, String pass, String authType) throws Exception {




        return null;
    }



//    public void logIn(final String username, final String password, final AccountFragment.LoginClickListener listener) {
//        RequestQueue queue = Volley.newRequestQueue(mContext);
//        String url = ApiConstants.LOGIN_USER;
//
//        StringRequest logInUserRequest = new StringRequest(Request.Method.POST, url,
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        //Get Response
//                        if(response == null || response.equals("null")) {
//                            Log.v("RESPONSE IS NULL IF YOU ARE HERE", response);
//                            requestMethods.showErrorDialog(mContext, "YOU SHALL NOT PASS",
//                                    "Sure you got your email/password combo right?");
//                        } else {
//                            Log.v("THIS IS THE RESPONSE FOR LOGIN: ", response);
//                            try {
//                                JSONObject res = new JSONObject(response);
//                                userID = res.getString(ApiConstants.USER_ID);
//                                String skey = res.getString(ApiConstants.USER_TOKEN);
//
//                                //Save userID in sharedPreferences
//                                sharedPreferencesMethods.SaveSharedPreference
//                                        (SharedPreferencesMethods.USER_ID_PREFERENCE_KEY, userID);
//                                sharedPreferencesMethods.SaveSharedPreference
//                                        (SharedPreferencesMethods.USER_TOKEN_PREFERENCE_KEY, skey);
//
//
//                                //TODO: Save session token in sharedPreferences (googleAM may handle this)
//                                //Add items chosen before login to userlist
//                                //TODO: also add category preferences
//                                JSONArray listItemPref;
//                                listItemPref = sharedPreferencesMethods.RetrieveUserItemPreference();
//
//                                if (listItemPref != null && listItemPref.length() > 0) {
//                                    Log.v("HEY THERE LIST ITEM PREF: ", listItemPref.toString());
//                                    for (int i = 0; i < listItemPref.length(); i++) {
//                                        Log.v("ITEMS", "ARE BEING ADDED");
//                                        addItemToUserList(listItemPref.getString(i));
//                                    }
//                                }
//                                //pass userID to the activity
//                                listener.UserLoggedIn(userID);
//                            } catch (JSONException e) {
//                                Log.v(TAG,e.getMessage());
//                                //TODO: add proper error message
//                                requestMethods.showErrorDialog(mContext, mContext.getString
//                                        (R.string.login_error_exception_title),
//                                        mContext.getString(R.string.login_error_exception_message));
//                            }
//                        }
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                requestMethods.showErrorDialog(mContext,
//                        mContext.getString(R.string.login_error_title),
//                        mContext.getString(R.string.login_error_message));
//            }
//        }) {
//            @Override
//            protected Map<String, String> getParams() {
//                Map<String, String> params = new HashMap<String, String>();
//                params.put("username", username);
//                params.put(ApiConstants.USER_PASSWORD, password);
//
//                return params;
//            }
//        };
//        queue.add(logInUserRequest);
//    }

    //Add all list items to userlist
    public void addSavedItemsToUserList(){
        JSONArray listItemPref;
        listItemPref = sharedPreferencesMethods.RetrieveUserItemPreference();

        try{
            if (listItemPref != null && listItemPref.length() > 0) {
                Log.v("HEY THERE LIST ITEM PREF: ", listItemPref.toString());
                for (int i = 0; i < listItemPref.length(); i++) {
                    Log.v("ITEMS", "ARE BEING ADDED");
                    addItemToUserList(listItemPref.getString(i));
                }
            }
        } catch(JSONException e){
            Log.d(TAG, e.getMessage());
        }
    } //addSavedItemsToUserList

    //Add all categories to userlist
    public void addSavedCategoriesToUserAccount(){

    }

    //Add SINGLE random item to user list
    public void addItemToUserList(final String itemID) {
        RequestQueue queue = Volley.newRequestQueue(mContext);

        //TODO: session token will know which user this is?
        String url = ApiConstants.ADD_ITEM + getUserID() + "/" + itemID;

        //Add single item to user list
        StringRequest postItemRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        //get Response
                        Log.v("Response: ", response);
                        Log.v(TAG,"AN ITEM IS BEING ADDED");
                        //TODO: do something with response?
                        //TODO: on success remove the item from the sharedPreferences

                        //Toast: Confirm List Item has been added
                        final Toast toast = Toast.makeText(mContext,
                                "Added to Your List", Toast.LENGTH_SHORT);
                        toast.show();
                        new android.os.Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                toast.cancel();
                            }
                        }, 1000);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse (VolleyError error){
                //TODO: Add “not successful“ toast
                requestMethods.showErrorDialog(mContext,
                        mContext.getString(R.string.error_title),
                        mContext.getString(R.string.error_message));
                Log.v("ERROR ADDING AN ITEM: ", "THIS IS THE ERROR BEING DISPLAYED");
            }
        });
        queue.add(postItemRequest);
    } //addItemToUserList

    //REMOVE SINGLE item from user list
    //TODO: FILL IN WITH REAL API INFO
    public void removeItemFromUserList(final String itemID){
        RequestQueue queue = Volley.newRequestQueue(mContext);

        if(!logInState){
            //If not logged in, remove item from sharedPreferences
            sharedPreferencesMethods.RemoveUserItemPreference(itemID);

        } else { //If logged in, remove from DB
            String url = ApiConstants.REMOVE_ITEM + getUserID() + "/" + itemID;
            final String skey = sharedPreferencesMethods.RetrieveUserToken();

            StringRequest deleteItemRequest = new StringRequest(Request.Method.POST, url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //get Response
                            Log.v("Response: ", response);
                            Log.v(TAG, "AN ITEM IS BEING REMOVED");
                            //TODO: do something with response?
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    //TODO: Add “not successful“ toast
                    requestMethods.showErrorDialog(mContext,
                            mContext.getString(R.string.error_title),
                            mContext.getString(R.string.error_message));
                    Log.v("ERROR DELETING AN ITEM: ", "THIS IS THE ERROR BEING DISPLAYED");
                }
            }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    //TODO: get sessionToken from AccountManager
                    params.put(ApiConstants.USER_TOKEN, sessionToken);

                    return params;
                }
            };
            queue.add(deleteItemRequest);
        }
    } //removeItemFromUserList

} //ListUser
