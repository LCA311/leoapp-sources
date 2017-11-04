package de.slg.leoapp;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import de.slg.essensqr.EssensQRActivity;
import de.slg.messenger.MessengerActivity;
import de.slg.schwarzes_brett.SchwarzesBrettActivity;
import de.slg.startseite.MainActivity;
import de.slg.stimmungsbarometer.StimmungsbarometerActivity;
import de.slg.stundenplan.StundenplanActivity;

public class ProfileActivity extends AppCompatActivity {
    private DrawerLayout  drawerLayout;
    private TextView nameProfil;
    private TextView defaultNameProfil;
    private TextView stufeProfil;
    private TextView stimmungProfil;
    private ImageView profilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        initToolbar();
        initNavigationView();
        initProfil();
    }

    public void initProfil() {
        nameProfil = (TextView) findViewById(R.id.nameProfil);
        defaultNameProfil = (TextView)  findViewById(R.id.defaultName);
        stufeProfil = (TextView) findViewById(R.id.stufeProfil);
        stimmungProfil = (TextView) findViewById(R.id.stimmungProfil);
        profilePic = (ImageView) findViewById(R.id.profPic);

        nameProfil.setText("Name: "+Utils.getUserName());
        defaultNameProfil.setText("Default Name: "+Utils.getUserDefaultName());
        stufeProfil.setText("Stufe: "+Utils.getUserStufe());
        stimmungProfil.setText("Stimmung: "+getMood());
        this.setzeProfilBild();
        //profilePic.setImageResource(de.slg.stimmungsbarometer.Utils.getCurrentMoodRessource());
    }

    private void setzeProfilBild() {
        int i = Utils.getController().getPreferences().getInt("pref_key_general_vote_id",1);
        switch (i) {
            case 1:
                profilePic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_very_dissatisfied_white_24px));
                profilePic.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                break;
            case 2:
                profilePic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_satisfied_white_24px));
                profilePic.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                break;
            case 3:
                profilePic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_neutral_white_24px));
                profilePic.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                break;
            case 4:
                profilePic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_dissatisfied_white_24px));
                profilePic.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                break;
            case 5:
                profilePic.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_sentiment_very_dissatisfied_white_24px));
                profilePic.getDrawable().setColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY);
                break;
        }
    }

    private String getMood() {
        int i = Utils.getController().getPreferences().getInt("pref_key_general_vote_id",1);
        switch (i) {
            case 1:
                return getString(R.string.sg);
            case 2:
                return getString(R.string.g);
            case 3:
                return getString(R.string.m);
            case 4:
                return getString(R.string.s);
            case 5:
                return getString(R.string.ss);
        }
        return ""+i;
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(getApplicationContext(), android.R.color.white));
        toolbar.setTitle(R.string.profil);
        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_menu_white_24dp);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem mi) {
        if (mi.getItemId() == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        return true;
    }

    private void initNavigationView() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigationView);
        navigationView.getMenu().findItem(R.id.klausurplan).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.newsboard).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.messenger).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.klausurplan).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.stundenplan).setEnabled(Utils.isVerified());
        navigationView.getMenu().findItem(R.id.profile).setChecked(true);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayout.closeDrawers();
                Intent i;
                switch (menuItem.getItemId()) {
                    case R.id.foodmarks:
                        i = new Intent(getApplicationContext(), EssensQRActivity.class);
                        break;
                    case R.id.messenger:
                        i = new Intent(getApplicationContext(), MessengerActivity.class);
                        break;
                    case R.id.newsboard:
                        i = new Intent(getApplicationContext(), SchwarzesBrettActivity.class);
                        break;
                    case R.id.stundenplan:
                        i = new Intent(getApplicationContext(), StundenplanActivity.class);
                        break;
                    case R.id.barometer:
                        i = new Intent(getApplicationContext(), StimmungsbarometerActivity.class);
                        break;
                    case R.id.klausurplan:
                        return true;
                    case R.id.startseite:
                        i = null;
                        break;
                    case R.id.settings:
                        i = new Intent(getApplicationContext(), PreferenceActivity.class);
                        break;
                    case R.id.profile:
                        return true;
                    default:
                        i = new Intent(getApplicationContext(), MainActivity.class);
                        Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
                }
                if (i != null)
                    startActivity(i);
                finish();
                return true;
            }
        });
        TextView username = (TextView) navigationView.getHeaderView(0).findViewById(R.id.username);
        username.setText(Utils.getUserName());
        TextView grade = (TextView) navigationView.getHeaderView(0).findViewById(R.id.grade);
        if (Utils.getUserPermission() == 2)
            grade.setText(Utils.getLehrerKuerzel());
        else
            grade.setText(Utils.getUserStufe());
        ImageView mood = (ImageView) navigationView.getHeaderView(0).findViewById(R.id.profile_image);
        mood.setImageResource(de.slg.stimmungsbarometer.Utils.getCurrentMoodRessource());
    }


}
