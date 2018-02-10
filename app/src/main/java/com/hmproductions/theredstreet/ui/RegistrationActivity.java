//package com.hmproductions.theredstreet.ui;
//
//import android.content.Intent;
//import android.support.v4.app.LoaderManager;
//import android.support.v4.content.Loader;
//import android.support.v7.app.AlertDialog;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.support.v7.widget.Toolbar;
//import android.view.LayoutInflater;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.widget.ArrayAdapter;
//import android.widget.AutoCompleteTextView;
//import android.widget.EditText;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import com.hmproductions.theredstreet.R;
//import com.hmproductions.theredstreet.dagger.ContextModule;
//import com.hmproductions.theredstreet.dagger.DaggerDalalStreetApplicationComponent;
//import com.hmproductions.theredstreet.utils.Constants;
//
//import javax.inject.Inject;
//
//import butterknife.BindView;
//import butterknife.ButterKnife;
//import dalalstreet.api.DalalActionServiceGrpc;
//
//public class RegistrationActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<RegistrationResponse> {
//
//    @Inject
//    DalalActionServiceGrpc.DalalActionServiceBlockingStub actionServiceBlockingStub;
//
//    @BindView(R.id.registration_toolbar)
//    Toolbar registrationToolbar;
//
//    @BindView(R.id.country_autoComplete)
//    AutoCompleteTextView countryAutoComplete;
//
//    @BindView(R.id.name_editText)
//    EditText nameEditText;
//
//    @BindView(R.id.password_editText)
//    TextView passwordEditText;
//
//    @BindView(R.id.confirmPassword_editText)
//    EditText confirmPasswordEditText;
//
//    @BindView(R.id.email_editText)
//    EditText emailEditText;
//
//    private AlertDialog registrationAlertDialog;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_registration);
//
//        DaggerDalalStreetApplicationComponent.builder().contextModule(new ContextModule(this)).build().inject(this);
//        ButterKnife.bind(this);
//
//        View dialogView = LayoutInflater.from(this).inflate(R.layout.progress_dialog, null);
//        ((TextView) dialogView.findViewById(R.id.progressDialog_textView)).setText(R.string.getting_your_orders);
//        registrationAlertDialog = new AlertDialog.Builder(this).setView(dialogView).setCancelable(false).create();
//
//        setSupportActionBar(registrationToolbar);
//        if (getSupportActionBar() != null) {
//            getSupportActionBar().setHomeAsUpIndicator(R.drawable.clear_icon);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//            setTitle("One-Time Registration");
//        }
//
//        String[] countries = getResources().getStringArray(R.array.countries_array);
//        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<>(this, R.layout.company_spinner_item, countries);
//        countryAutoComplete.setAdapter(countriesAdapter);
//    }
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.registration_menu, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//
//        if (item.getItemId() == R.id.register_action) {
//
//            if (nameEditText.getText().toString().isEmpty() || nameEditText.getText().toString().equals("")) {
//                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
//            } else if (passwordEditText.getText().toString().length() < 8) {
//                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
//            } else if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
//                Toast.makeText(this, "Confirm password mismatch", Toast.LENGTH_SHORT).show();
//            } else if (emailEditText.getText().toString().isEmpty() || emailEditText.getText().toString().equals("")) {
//                Toast.makeText(this, "Please enter valid email ID", Toast.LENGTH_SHORT).show();
//            } else {
//                String country = "";
//
//                if (!(countryAutoComplete.getText().toString().isEmpty() || countryAutoComplete.getText().toString().equals("")))
//                    country = countryAutoComplete.getText().toString();
//
//                getSupportLoaderManager().restartLoader(Constants.REGISTRATION_LOADER_ID, null, this);
//            }
//        }
//        return super.onOptionsItemSelected(item);
//    }
//
//    @Override
//    public Loader<RegistrationResponse> onCreateLoader(int id, Bundle args) {
//
//        registrationAlertDialog.show();
//
//        return new RegistrationLoader(this, actionServiceBlockingStub, registrationRequest);
//    }
//
//    @Override
//    public void onLoadFinished(Loader<RegistrationResponse> loader, RegistrationResponse response) {
//
//        registrationAlertDialog.dismiss();
//
//        if (response == null) {
//            startActivity(new Intent(this, SplashActivity.class));
//            finish();
//        } else {
//            if (response.getStatusCode() == 0) {
//                // TODO : Do something with response
//            }
//        }
//    }
//
//    @Override
//    public void onLoaderReset(Loader<RegistrationResponse> loader) {
//        // Do nothing
//    }
//}