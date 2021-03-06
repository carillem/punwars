package watson.punwarz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.Menu;
import android.view.MenuInflater;

import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import watson.punwarz.ListView.PunAdapter;
import watson.punwarz.ListView.PunModel;
import com.facebook.FacebookSdk;

import java.util.ArrayList;

/**
 * Author:  Daniel Tetzlaff
 * Created: 2016-03-01
 * Description: This class takes care of the displaying and managing the puns
 */
public class Puns extends Page
{
    private final String LOBBY_ID = "LOBBY_ID";
    ListView list;
    PunAdapter adapter;
    public  Puns CustomListView = null;
    public  ArrayList<PunModel> CustomListViewValuesArr = new ArrayList<>();
    private String lobbyID;
    private ParseApplication parse;
    private View header;

    public SwipeRefreshLayout punRefresh = null;

    private String title = "";
    private String desc = "";
    private String author = "";
    private String expDate = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_puns);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        header = ( (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) ).inflate(R.layout.themeheader, null, false);

        parse = new ParseApplication();

        Intent intent = getIntent();
        lobbyID = intent.getStringExtra("LOBBY_ID");
        getExtras(intent);

        CustomListView = this;
        punRefresh = ( SwipeRefreshLayout )findViewById( R.id.punrefresh );
        punRefresh.setRefreshing(true);
        setListData.run();

        Resources res = getResources();
        list = ( ListView )findViewById( R.id.list );

        adapter = new PunAdapter( CustomListView, CustomListViewValuesArr,res);
        list.setAdapter( adapter );

        //list.addHeaderView(header);
        setHeader();

        punRefresh.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        Log.d("REFRESH", "onRefresh called from SwipeRefreshLayout");
                        doRefresh();
                    }
                }
        );
    }

    public void onItemClick(int position)
    {
        PunModel tempValues = (PunModel) CustomListViewValuesArr.get(position);
        
        int res = parse.voteOnPost(com.facebook.Profile.getCurrentProfile().getId(),tempValues.getPunID(), lobbyID, tempValues.getPunAuthID());
        switch(res)
        {
            case 0:
                deletionConfirm(tempValues.getPunID());
                //Toast.makeText(getApplicationContext(), "Vote failed. You can't vote on your own pun.", Toast.LENGTH_SHORT).show();
                break;
            case 1: Toast.makeText(getApplicationContext(), "You have already voted for this pun.", Toast.LENGTH_SHORT).show();
                break;
            case 2: Toast.makeText(getApplicationContext(), "Vote received!", Toast.LENGTH_SHORT).show();
                    tempValues.setPunVotes(Integer.toString(Integer.valueOf(tempValues.getPunVotes()) + 1));
                    adapter.notifyDataSetChanged();
                break;
            default:break;
        }

    }

    public void deletionConfirm(final String punID)
    {
        AlertDialog alert = new AlertDialog.Builder(this).create();
        alert.setTitle("Delete Confirmation");
        alert.setMessage("Are you sure you want to delete this pun?");
        alert.setCancelable(false);
        alert.setButton(Dialog.BUTTON_POSITIVE, "Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                parse.deletePun(punID);
                Intent i = getIntent();
                finish();
                startActivity(i);
            }
        });
        alert.setButton(Dialog.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }});

        alert.show();
    }

    public Runnable setListData = new Runnable()
    {
        @Override
                public void run() {
            ArrayList<ArrayList<String>> puns = parse.getPuns(lobbyID);


            for (int i = 0; i < puns.size(); i++) {
                ArrayList<String> current = puns.get(i);
                final PunModel sched = new PunModel();

                sched.setPunID(current.get(2));
                sched.setPunAuth("By: " + current.get(1));
                sched.setPun(current.get(0));
                sched.setPunVotes(current.get(4));
                sched.setPunAuthID(current.get(3));


                CustomListViewValuesArr.add(sched);
            }

            punRefresh.setRefreshing(false);
        }
    };


    public void setHeader(){
        TextView titleView = (TextView)findViewById(R.id.lobbyTitle);
        TextView descView = (TextView)findViewById(R.id.lobbyDes);
        TextView authView = (TextView)findViewById(R.id.lobbyAuthor);
        TextView expView = (TextView)findViewById(R.id.expireDate);

        titleView.setText(title);
        descView.setText(desc);
        authView.setText(author);
        expView.setText(expDate);
    }

    private void getExtras(Intent intent){
        title = intent.getStringExtra("THEME_TITLE");
        desc = intent.getStringExtra("THEME_DESC");
        expDate = intent.getStringExtra("THEME_EXPIRE");
        author = intent.getStringExtra("THEME_AUTHOR");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_puns, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        if(id == R.id.lobby_settings)
        {
            goToLobby(item);
            return true;
        }
        else if(id == R.id.logout_settings)
        {
            logOut(item);
            return true;
        }
        else if(id == R.id.profile_settings)
        {
            goToProfile(item);
            return true;
        }
        else if(id == R.id.addPun_settings)
        {
            addAPun(item);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void goToProfile(MenuItem item)
    {
        Intent i = new Intent(Puns.this, Profile.class);
        startActivity(i);
    }

    public void addAPun(MenuItem item)
    {
        Intent i = new Intent(Puns.this, AddPun.class);
        i.putExtra(LOBBY_ID, lobbyID);
        i.putExtra("THEME_TITLE", title);
        i.putExtra("THEME_DESC", desc);
        i.putExtra("THEME_AUTHOR", author);
        i.putExtra("THEME_EXPIRE", expDate);
        startActivity(i);
    }

    public void doRefresh(){
        CustomListViewValuesArr.clear();
        adapter.notifyDataSetChanged();
        setListData.run();
    }
}
