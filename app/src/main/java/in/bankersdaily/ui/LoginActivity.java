package in.bankersdaily.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.material.snackbar.Snackbar;

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
import in.testpress.models.InstituteSettings;
import in.testpress.util.UIUtils;

import static in.bankersdaily.network.ApiClient.ACCESS_TOKEN;
import static in.bankersdaily.network.ApiClient.COOL;
import static in.bankersdaily.network.ApiClient.ID_TOKEN;
import static in.bankersdaily.network.ApiClient.INSECURE;
import static in.bankersdaily.util.Preferences.KEY_AUTH_SHARED_PREFS;
import static in.bankersdaily.util.Preferences.KEY_WORDPRESS_TOKEN;
import static in.bankersdaily.util.Preferences.KEY_WORDPRESS_USERNAME;

public class LoginActivity extends BaseToolBarActivity {

    public static final int AUTHENTICATE_REQUEST_CODE = 1111;
    public static final int REQUEST_CODE_GOOGLE_SIGN_IN = 2222;

    private View loginView;
    private CallbackManager callbackManager;
    private AccessToken facebookAccessToken;
    private GoogleApiClient googleApiClient;
    private GoogleSignInAccount googleSignInAccount;

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
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(getString(R.string.server_client_id))
                .build();
        googleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (connectionResult.getErrorMessage() != null) {
                            Snackbar.make(loginView, connectionResult.getErrorMessage(),
                                    Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(loginView, connectionResult.toString(),
                                    Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, googleSignInOptions)
                .build();
        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setStyle(SignInButton.SIZE_WIDE, SignInButton.COLOR_AUTO);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
                startActivityForResult(signInIntent, REQUEST_CODE_GOOGLE_SIGN_IN);
            }
        });
        callbackManager = CallbackManager.Factory.create();
        LoginButton loginButton = findViewById(R.id.fb_login_button);
        loginButton.setReadPermissions("email");
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                facebookAccessToken = loginResult.getAccessToken();
                loginToWordPress();
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

    private void loginToWordPress() {
        Map<String, Object> queryParams = new LinkedHashMap<String, Object>();
        String url;
        if (facebookAccessToken != null) {
            queryParams.put(ACCESS_TOKEN, facebookAccessToken.getToken());
            url = ApiClient.FB_LOGIN_PATH;
        } else {
            queryParams.put(ID_TOKEN, googleSignInAccount.getIdToken());
            url = ApiClient.GOOGLE_LOGIN_PATH;
        }
        queryParams.put(INSECURE, COOL);
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.testpress_please_wait));
        progressDialog.setCancelable(false);
        UIUtils.setIndeterminateDrawable(this, progressDialog, 4);
        progressDialog.show();
        new ApiClient(this).authenticate(url, queryParams)
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
                        editor.putString(KEY_WORDPRESS_USERNAME, response.getUserLogin());
                        editor.apply();
                        authenticate();
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

    private void authenticate() {
        if (facebookAccessToken != null) {
            BankersDailyApp.getInstance().trackEvent(
                    getScreenName(),
                    BankersDailyApp.LOGGED_IN_VIA_FB,
                    null
            );
            authenticate(facebookAccessToken.getUserId(), facebookAccessToken.getToken(),
                    TestpressSdk.Provider.FACEBOOK);

        } else if (googleSignInAccount != null) {
            BankersDailyApp.getInstance().trackEvent(
                    getScreenName(),
                    BankersDailyApp.LOGGED_IN_VIA_GOOGLE,
                    null
            );
            authenticate(googleSignInAccount.getId(), googleSignInAccount.getIdToken(),
                    TestpressSdk.Provider.GOOGLE);
        }
    }

    private void authenticate(String userId, String accessToken, TestpressSdk.Provider provider) {
        InstituteSettings instituteSettings =
                new InstituteSettings(getString(R.string.testpress_base_url));

        BankersDailyApp.updateInstituteSettings(instituteSettings);
        TestpressSdk.initialize(this, instituteSettings, userId, accessToken, provider,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
                        BankersDailyApp.getInstance().setLoginState(true);
                        BankersDailyApp.getInstance().trackEvent(
                                getScreenName(),
                                BankersDailyApp.LOGGED_IN,
                                null
                        );
                        if (getCallingActivity() != null) {
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
        if (requestCode == REQUEST_CODE_GOOGLE_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                //noinspection ConstantConditions
                googleSignInAccount = result.getSignInAccount();
                loginToWordPress();
            } else if (result.getStatus().getStatusCode() == CommonStatusCodes.NETWORK_ERROR) {
                showAlert(R.string.no_internet_try_again);
            } else if (result.getStatus().getStatusCode() == CommonStatusCodes.DEVELOPER_ERROR) {
                showAlert(R.string.google_sign_in_wrong_hash);
            } else if (result.getStatus().getStatusCode() == 12501) {
                Log.e("Google sign in error", "Might be wrong app certificate SHA1");
                Snackbar.make(loginView, R.string.testpress_some_thing_went_wrong_try_again,
                        Snackbar.LENGTH_LONG).show();
            } else {
                Log.e("Google sign in error", result.getStatus().toString());
            }
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    void showAlert(int message) {
        new AlertDialog.Builder(this, R.style.TestpressAppCompatAlertDialogStyle)
                .setMessage(message)
                .setNeutralButton(R.string.ok, null)
                .show();
    }

    @Override
    protected String getScreenName() {
        return BankersDailyApp.LOGIN_SCREEN;
    }
}
