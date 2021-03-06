package org.ezek.bible;

import org.ezek.bible.BibleProvider.NewTest;
import org.ezek.bible.BibleProvider.OldTest;

import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.admob.android.ads.AdView;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class BooksList extends ListActivity {

    public static final int DIALOG_IMPORTING_BIBLE = 0;

    private ArrayAdapter<CharSequence> mBooksArrayAdapter;
    private AdView mAdmobAdView;
    private String VERSION;
    private GoogleAnalyticsTracker mGaTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mGaTracker = GoogleAnalyticsTracker.getInstance();
        mGaTracker.start("UA-15698752-2", this);

        mGaTracker.trackPageView("/home");
        
        SharedPreferences settings = getSharedPreferences("settings", MODE_PRIVATE);
        if (!settings.getBoolean("bible_installed", false)) {
            new BibleImportThread(this, settings).execute();
        }

        LayoutInflater li = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        mAdmobAdView = (AdView) li.inflate(R.layout.admob_adview, null);
        mAdmobAdView.setKeywords("bible testament religion jesus god");
        getListView().addHeaderView(mAdmobAdView);

        Intent intent = getIntent();
        int books = R.array.new_books;
        VERSION = NewTest.TABLE;
        if(intent != null && intent.hasExtra("version")) {
            if(intent.getStringExtra("version").equals(OldTest.TABLE)) {
                books = R.array.old_books;
                VERSION = OldTest.TABLE;
            }
        }

        mBooksArrayAdapter = ArrayAdapter.createFromResource(this, books, R.layout.books_row);
        setListAdapter(mBooksArrayAdapter);
    }

    @Override
    protected void onPause() {
        mGaTracker.dispatch();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mAdmobAdView.requestFreshAd();
        super.onResume();
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        TextView view = (TextView) v;
        Intent intent = new Intent(this, BookActivity.class);
        intent.putExtra("version", VERSION);
        intent.putExtra(NewTest.Columns.BOOK, view.getText().toString());
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        intent = new Intent(this, BooksList.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        switch (item.getItemId()) {
            case R.id.menu_options_newtest:
                mGaTracker.trackPageView("/newtest");
                intent.putExtra("version", NewTest.TABLE);
                break;
            case R.id.menu_options_oldtest:
                mGaTracker.trackPageView("/oldtest");
                intent.putExtra("version", OldTest.TABLE);
                break;
            default:
                break;
        }

        startActivity(intent);

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_IMPORTING_BIBLE:
                ProgressDialog mProgressDialog;
                mProgressDialog = new ProgressDialog(this);
                mProgressDialog.setMessage(getResources().getString(R.string.dialog_importing_bible));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                return mProgressDialog;
            default:
                return null;
        }
    }
}
