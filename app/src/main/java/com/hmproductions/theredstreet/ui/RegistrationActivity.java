package com.hmproductions.theredstreet.ui;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hmproductions.theredstreet.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RegistrationActivity extends AppCompatActivity {

    @BindView(R.id.registration_toolbar)
    Toolbar registrationToolbar;

    @BindView(R.id.country_autoComplete)
    AutoCompleteTextView countryAutoComplete;

    @BindView(R.id.name_editText)
    EditText nameEditText;

    @BindView(R.id.password_editText)
    TextView passwordEditText;

    @BindView(R.id.confirmPassword_editText)
    EditText confirmPasswordEditText;

    @BindView(R.id.email_editText)
    EditText emailEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        ButterKnife.bind(this);

        setSupportActionBar(registrationToolbar);
        setTitle("One-Time Registration");

        String[] countries = getResources().getStringArray(R.array.countries_array);
        ArrayAdapter<String> countriesAdapter = new ArrayAdapter<>(this, R.layout.company_spinner_item, countries);
        countryAutoComplete.setAdapter(countriesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.registration_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.register_action) {

            if (nameEditText.getText().toString().isEmpty() || nameEditText.getText().toString().equals("")) {
                Toast.makeText(this, "Please enter your name", Toast.LENGTH_SHORT).show();
            } else if (passwordEditText.getText().toString().length() < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_SHORT).show();
            } else if (!passwordEditText.getText().toString().equals(confirmPasswordEditText.getText().toString())) {
                Toast.makeText(this, "Confirm password mismatch", Toast.LENGTH_SHORT).show();
            } else if (emailEditText.getText().toString().isEmpty() || emailEditText.getText().toString().equals("")) {
                Toast.makeText(this, "Please enter valid email ID", Toast.LENGTH_SHORT).show();
            } else {
                String country = "";

                if (!(countryAutoComplete.getText().toString().isEmpty() || countryAutoComplete.getText().toString().equals("")))
                    country = countryAutoComplete.getText().toString();

                // TODO : Send registration details
            }
        }

        return super.onOptionsItemSelected(item);
    }
}
