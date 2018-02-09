package in.bankersdaily.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import in.bankersdaily.BankersDailyApp;
import in.bankersdaily.R;
import in.bankersdaily.model.LoginResponse;
import in.bankersdaily.network.ApiClient;
import in.bankersdaily.network.RetrofitCallback;
import in.bankersdaily.network.RetrofitException;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.model.InstituteSettings;
import in.testpress.util.UIUtils;

import static in.bankersdaily.network.ApiClient.ACCESS_TOKEN;
import static in.bankersdaily.network.ApiClient.COOL;
import static in.bankersdaily.network.ApiClient.INSECURE;
import static in.bankersdaily.util.Preferences.KEY_AUTH_SHARED_PREFS;
import static in.bankersdaily.util.Preferences.KEY_WORDPRESS_TOKEN;

public class LoginActivity extends BaseToolBarActivity {

    public static final int AUTHENTICATE_REQUEST_CODE = 1111;

    private View loginView;
    private CallbackManager callbackManager;

    @SuppressWarnings("ConstantConditions")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getCallingActivity() != null) {
            // Activity started by startActivityForResult()
            getSupportActionBar().hide();
        } else {
            // Activity started by startActivity
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                loginToWordPress(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                if (error.getCause() instanceof IOException) {
                    Snackbar.make(loginView, R.string.no_internet_try_again, Snackbar.LENGTH_LONG)
                            .show();
                } else {
                    Snackbar.make(loginView, R.string.facebook_login_error, Snackbar.LENGTH_LONG)
                            .show();
                }
            }
        });
        loginView = findViewById(R.id.scroll_view);
    }

    private void loginToWordPress(final AccessToken facebookAccessToken) {
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        queryParams.put(ACCESS_TOKEN, facebookAccessToken.getToken());
        queryParams.put(INSECURE, COOL);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(this, progressDialog, 4);
        progressDialog.show();
        new ApiClient(this).authenticate(queryParams)
                .enqueue(new RetrofitCallback<LoginResponse>() {
                    @Override
                    public void onSuccess(LoginResponse response) {
                        progressDialog.dismiss();
                        if (response.getCookie() == null) {
                            BankersDailyApp.getInstance().trackEvent(
                                    getScreenName(),
                                    BankersDailyApp.EMAIL_NOT_FOUND,
                                    null
                            );
                            Snackbar.make(loginView, R.string.email_cannot_retrieve,
                                    Snackbar.LENGTH_LONG).show();

                            return;
                        }
                        SharedPreferences.Editor editor = getSharedPreferences(
                                KEY_AUTH_SHARED_PREFS, Context.MODE_PRIVATE).edit();

                        editor.putString(KEY_WORDPRESS_TOKEN, response.getCookie());
                        editor.apply();
                        authenticate(facebookAccessToken.getUserId(), facebookAccessToken.getToken(),
                                TestpressSdk.Provider.FACEBOOK);
                    }

                    @Override
                    public void onException(RetrofitException exception) {
                        progressDialog.dismiss();
                        if (exception.isNetworkError()) {
                            Snackbar.make(loginView, R.string.no_internet_try_again,
                                    Snackbar.LENGTH_LONG).show();
                        } else if (exception.isClientError()) {
                            Snackbar.make(loginView, exception.getMessage(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(loginView, R.string.some_thing_went_wrong_try_again,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                });

    }

    private void authenticate(String userId, String accessToken, TestpressSdk.Provider provider) {
        InstituteSettings instituteSettings =
                new InstituteSettings(getString(R.string.testpress_base_url));

        TestpressSdk.initialize(this, instituteSettings, userId, accessToken, provider,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
                        if (getCallingActivity() != null) {
                            BankersDailyApp.getInstance().trackEvent(
                                    getScreenName(),
                                    BankersDailyApp.LOGGED_IN,
                                    null
                            );
                            setResult(RESULT_OK);
                            finish();
                        }
                    }

                    @Override
                    public void onException(TestpressException e) {
                        if (e.isNetworkError()) {
                            Snackbar.make(loginView, R.string.no_internet_try_again,
                                    Snackbar.LENGTH_LONG).show();
                        } else if (e.isClientError()) {
                            Snackbar.make(loginView, e.getMessage(), Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(loginView, R.string.some_thing_went_wrong_try_again,
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected String getScreenName() {
        return BankersDailyApp.LOGIN_SCREEN;
    }
}
