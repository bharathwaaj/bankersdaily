package in.bankersdaily.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.view.View;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import java.io.IOException;

import in.bankersdaily.R;
import in.testpress.core.TestpressCallback;
import in.testpress.core.TestpressException;
import in.testpress.core.TestpressSdk;
import in.testpress.core.TestpressSession;
import in.testpress.model.InstituteSettings;

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
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authenticate(loginResult.getAccessToken().getUserId(),
                        loginResult.getAccessToken().getToken(), TestpressSdk.Provider.FACEBOOK);
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

    private void authenticate(String userId, String accessToken, TestpressSdk.Provider provider) {
        InstituteSettings instituteSettings =
                new InstituteSettings(getString(R.string.testpress_base_url));

        TestpressSdk.initialize(this, instituteSettings, userId, accessToken, provider,
                new TestpressCallback<TestpressSession>() {
                    @Override
                    public void onSuccess(TestpressSession response) {
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
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

}
