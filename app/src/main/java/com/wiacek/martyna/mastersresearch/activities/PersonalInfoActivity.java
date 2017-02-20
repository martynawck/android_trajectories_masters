package com.wiacek.martyna.mastersresearch.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.wiacek.martyna.mastersresearch.R;
import com.wiacek.martyna.mastersresearch.tasks.SendUserProfileTask;
import com.wiacek.martyna.mastersresearch.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;

public class PersonalInfoActivity extends AppCompatActivity {

    public Button button;
    public TextView yearOfBirthText, sexText, warsawText, professionText, educationText, relationshipText, carText;
    EditText yearOfBirth;
    Spinner sexSpinner, citySpinner, professionSpinner, educationSpinner, carSpinner, relationshipSpinner;
    ImageButton polishButton, englishButton;

    SessionManager sessionManager;
    String sex, location, profession, education, username, relationship, trasportation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_info);

        professionText = (TextView) this.findViewById(R.id.professionText);
        yearOfBirthText = (TextView) this.findViewById(R.id.yearOfBirthText);
        sexText = (TextView) this.findViewById(R.id.sexText);
        warsawText = (TextView) this.findViewById(R.id.warsawText);
        educationText = (TextView) this.findViewById(R.id.educationText);
        relationshipText = (TextView) this.findViewById(R.id.relationshipText);
        carText = (TextView) this.findViewById(R.id.carText);

        yearOfBirth = (EditText) this.findViewById(R.id.yearOfBirthEdit);
        sexSpinner = (Spinner) this.findViewById(R.id.sexSpinner);
        citySpinner = (Spinner) this.findViewById(R.id.warsawSpinner);
        educationSpinner = (Spinner) this.findViewById(R.id.educationSpinner);
        relationshipSpinner = (Spinner) this.findViewById(R.id.relationshipSpinner);
        carSpinner = (Spinner) this.findViewById(R.id.carSpinner);
        professionSpinner = (Spinner) findViewById(R.id.professionSpinner);

        button = (Button) this.findViewById(R.id.button);
        polishButton = (ImageButton) this.findViewById(R.id.polishButton);
        englishButton = (ImageButton) this.findViewById(R.id.englishButton);

        Intent i = getIntent();
        Bundle b = i.getBundleExtra("bundle");
        username = b.getString("username");

        changeLanguage("PL");

        polishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("PL");
            }
        });

        englishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeLanguage("EN");
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                SendUserProfileTask task = new SendUserProfileTask(getApplicationContext(), new String[]{username,sex,
                yearOfBirth.getText().toString(), location, education, profession, relationship, trasportation});
                task.runVolley();

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                sessionManager = new SessionManager(getApplicationContext());
                sessionManager.createSession(username);
                startActivity(intent);
                finish();

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                location = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        professionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                profession = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        educationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                education = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        sexSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sex = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        carSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                trasportation = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        relationshipSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                relationship = Integer.toString(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }


    public void changeLanguage(String language) {
        if (language == "PL") {
            this.setTitle(getString(R.string.userProfileDialogTitlePL));
            yearOfBirthText.setText(getString(R.string.yearOfBirthPL));
            sexText.setText(getString(R.string.sexTextPL));
            warsawText.setText(getString(R.string.cityTextPL));
            professionText.setText(getString(R.string.professionPL));
            educationText.setText(getString(R.string.educationPL));
            carText.setText(getString(R.string.carPL));
            relationshipText.setText(getString(R.string.relationshipPL));

            button.setText(getString(R.string.registerPL));

            List<String> list = new ArrayList<>();
            list.add("Warszawa");
            list.add("inne");

            ArrayAdapter<String> dataAdapterCity = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,list);
            dataAdapterCity.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(dataAdapterCity);

            List<String> listProfession = new ArrayList<>();
            listProfession.add("pełen etat");
            listProfession.add("część etatu");
            listProfession.add("osoba niepracująca");

            ArrayAdapter<String> dataAdapterProfession = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listProfession);
            dataAdapterProfession.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
                     professionSpinner.setAdapter(dataAdapterProfession);

            List<String> listEducation = new ArrayList<>();
            listEducation.add("średnie");
            listEducation.add("wyższe (I stopień)");
            listEducation.add("wyższe (II stopień)");

            ArrayAdapter<String> dataAdapterEducation = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listEducation);
            dataAdapterEducation.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
                    educationSpinner.setAdapter(dataAdapterEducation);

            List<String> listSex = new ArrayList<>();
            listSex.add("mężczyzna");
            listSex.add("kobieta");

            ArrayAdapter<String> dataAdapterSex = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listSex);
            dataAdapterSex.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            sexSpinner.setAdapter(dataAdapterSex);

            List<String> listRelationship = new ArrayList<>();
            listRelationship.add("wolny/a");
            listRelationship.add("zajęty/a");

            ArrayAdapter<String> dataAdapterRelationship = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listRelationship);
            dataAdapterRelationship.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            relationshipSpinner.setAdapter(dataAdapterRelationship);

            List<String> listWarsaw = new ArrayList<>();
            listWarsaw.add("samochód");
            listWarsaw.add("pieszo");
            listWarsaw.add("rower");
            listWarsaw.add("komunikacja miejska");

            ArrayAdapter<String> dataAdapterWarsaw = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listWarsaw);
            dataAdapterWarsaw.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            carSpinner.setAdapter(dataAdapterWarsaw);

        } else {
            this.setTitle(getString(R.string.userProfileDialogTitleEN));
            yearOfBirthText.setText(getString(R.string.yearOfBirthEN));
            sexText.setText(getString(R.string.sexTextEN));
            warsawText.setText(getString(R.string.cityTextEN));
            professionText.setText(getString(R.string.professionEN));
            educationText.setText(getString(R.string.educationEN));
            carText.setText(getString(R.string.carEN));
            relationshipText.setText(getString(R.string.relationshipEN));

            button.setText(getString(R.string.registerEN));

            List<String> list = new ArrayList<>();
            list.add("Warsaw");
            list.add("other");

            ArrayAdapter<String> dataAdapterCity = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,list);
            dataAdapterCity.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            citySpinner.setAdapter(dataAdapterCity);

            List<String> listProfession = new ArrayList<>();
            listProfession.add("full-time");
            listProfession.add("part-time");
            listProfession.add("not working");

            ArrayAdapter<String> dataAdapterProfession = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listProfession);
            dataAdapterProfession.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            professionSpinner.setAdapter(dataAdapterProfession);

            List<String> listEducation = new ArrayList<>();
            listEducation.add("secondary");
            listEducation.add("Bachelor's degree");
            listEducation.add("Master's degree");

            ArrayAdapter<String> dataAdapterEducation = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listEducation);
            dataAdapterEducation.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            educationSpinner.setAdapter(dataAdapterEducation);

            List<String> listSex = new ArrayList<>();
            listSex.add("male");
            listSex.add("female");

            ArrayAdapter<String> dataAdapterSex = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listSex);
            dataAdapterSex.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            sexSpinner.setAdapter(dataAdapterSex);

            List<String> listRelationship = new ArrayList<>();
            listRelationship.add("single");
            listRelationship.add("taken");

            ArrayAdapter<String> dataAdapterRelationship = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listRelationship);
            dataAdapterRelationship.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            relationshipSpinner.setAdapter(dataAdapterRelationship);

            List<String> listWarsaw = new ArrayList<>();
            listWarsaw.add("car");
            listWarsaw.add("on foot");
            listWarsaw.add("bike");
            listWarsaw.add("city transportation");

            ArrayAdapter<String> dataAdapterWarsaw = new ArrayAdapter<String>
                    (this, android.R.layout.simple_spinner_item,listWarsaw);
            dataAdapterWarsaw.setDropDownViewResource
                    (android.R.layout.simple_spinner_dropdown_item);
            carSpinner.setAdapter(dataAdapterWarsaw);
        }
    }
}
