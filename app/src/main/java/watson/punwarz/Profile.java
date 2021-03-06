package watson.punwarz;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import watson.punwarz.ImageView.RoundedImageView;
import watson.punwarz.ListView.CustomThemeAdapter;
import watson.punwarz.ListView.ListModel;
import watson.punwarz.ListView.PunModel;
import watson.punwarz.ListView.UserPunAdapter;
import watson.punwarz.ListView.UserTopPunAdapter;

import com.facebook.FacebookSdk;

import java.util.ArrayList;

/**
 * @author Daniel Tetzlaff (tetzlaffdanielj@gmail.com)
 * @version 1.2
 * Created: 2016-03-08
 * Updated: 2017-11-22
 * Description: Provides summary of user based on the userID provided via Intent, utilizes inheritance from Page.class
 */
public class Profile extends Page
{
    //initialization of variable names for holding items that we will update
    private TextView name;
    private TextView points;
    private TextView rankText;
    private RoundedImageView profilePic;
    private Bitmap tempPic;
    private com.facebook.Profile profile;
    private ParseApplication parse;
    private int userPoints;
    private String userID;
    private Boolean isFriend = false;
    private Boolean notSelf = false;
    private Boolean requestSent = false;
    private Boolean requestReceived = false;

    //initialization of listviews and corresponding adapters
    ListView themeList;
    ListView punList;
    ListView topPunList;
    CustomThemeAdapter themeAdapter;
    UserPunAdapter punAdapter;
    UserTopPunAdapter topPunAdapter;

    //array lists initialized for use with never-ending listviews
    public Profile CustomListView = null;
    public ArrayList<ListModel> CustomListViewValuesArrTheme = new ArrayList<ListModel>();
    public ArrayList<PunModel> CustomListViewValuesArrPun = new ArrayList<PunModel>();
    public ArrayList<PunModel> CustomListViewValuesArrTopPun = new ArrayList<PunModel>();

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //initialize DB
        parse = new ParseApplication();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        FacebookSdk.sdkInitialize(getApplicationContext());

        //Set textViews here
        name = (TextView)findViewById(R.id.name);
        points = (TextView)findViewById(R.id.points);
        rankText = (TextView)findViewById(R.id.rank);
        profilePic = (RoundedImageView)findViewById(R.id.profilePic);
        profile = com.facebook.Profile.getCurrentProfile();

        //get the UserID that was passed through intent
        userID = this.getIntent().getStringExtra("UserID");

        // if/else ONLY changes isFriend when it is verified that a relation already exists
        if (userID == null) //if this was null, nothing passed through intent ∴ user is viewing their own profile
        {
            //get current user's ID
            userID = profile.getId();
            isFriend = true;
        }
        else if (userID.equals(profile.getId())) //check if user selected their own profile from list
        {
            isFriend = true;
        }
        else
        {
            //check if a friendship or request already exists for pair
            if (parse.doesFriendRequestToExist(profile.getId(), userID))
            {
                isFriend = true;
                requestSent = true;
            }
            else if (parse.doesFriendRequestToExist(userID, profile.getId()))
            {
                isFriend = true;
                requestReceived = true;
            }
            else if (parse.doesFriendshipExist(profile.getId(), userID))
            {
                isFriend = true;
                notSelf = true;
            }
        }

        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar); //set toolbar

        setSupportActionBar(toolbar);

        CustomListView = this;

        setName();
        setPoints();
        setRank();

        //AsyncTaskss that make DB calls that may be longer and could cause delays if not async
        new setProfilePic().execute(userID);
        new SetThemeList().execute(userID);
        new SetPunList().execute(userID);
        new SetTopPunList().execute(userID);

        Resources res = getResources();
        themeList = ( ListView )findViewById( R.id.user_themes_list );
        punList = ( ListView )findViewById( R.id.user_puns_list );
        topPunList = ( ListView )findViewById(R.id.user_topPuns_list);

        themeAdapter = new CustomThemeAdapter( CustomListView, CustomListViewValuesArrTheme, res);
        themeList.setAdapter( themeAdapter );

        punAdapter = new UserPunAdapter( CustomListView, CustomListViewValuesArrPun, res);
        punList.setAdapter(punAdapter);

        topPunAdapter = new UserTopPunAdapter( CustomListView, CustomListViewValuesArrTopPun, res);
        topPunList.setAdapter(topPunAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_profile, menu);
        if (isFriend) { menu.findItem(R.id.addfriend_settings).setVisible(false); }
        if (notSelf)  { menu.findItem(R.id.removefriend_settings).setVisible(true); }
        if (requestSent) { menu.findItem(R.id.removerequest_settings).setVisible(true); }
        if (requestReceived) { menu.findItem(R.id.acceptrequest_settings).setVisible(true); }

        return true;
    }

    private void setName(){
        name.setText(parse.getUserName(userID));
    }

    private void setPoints()
    {
        userPoints = parse.getUserPoints(userID);
        points.setText("Points: " + Integer.toString(userPoints));
    }

    private void setRank()
    {
        Ranks rank = new Ranks();
        rankText.setText("Rank: " + rank.getRankName(userPoints));
    }

    private class setProfilePic extends AsyncTask<String, Integer, Long>
    {
        @Override
        protected Long doInBackground(String... userID)
        {
            PictureGrabber pic = new PictureGrabber();
            tempPic = pic.getUserPicture(getApplicationContext(), userID[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Long result) {
            profilePic.setImageBitmap(tempPic);
        }
    }

    private class SetThemeList extends AsyncTask<String, Integer, Long>
    {
        @Override
        protected Long doInBackground(String... userID)
        {
//            try {
//                Thread.sleep(5000);
//            } catch (InterruptedException e) { }
//            Sleep to test loading spinner

            ArrayList<ArrayList<String>> themes = parse.getUserThemes(userID[0]);

            for (int i = 0; i < themes.size(); i++)
            {
                ArrayList<String> current = themes.get(i);
                final ListModel sched = new ListModel();

                sched.setLobbyTitle(current.get(0));
                sched.setExpireDate(current.get(1));
                sched.setLobbyDes(current.get(2));
                sched.setTopPun(current.get(3));
                sched.setLobbyAuthor(current.get(4));
                sched.setLobbyID(current.get(5));

                CustomListViewValuesArrTheme.add(sched);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result)
        {
            findViewById(R.id.themeProgressBar).setVisibility(View.INVISIBLE);
            themeAdapter.notifyDataSetChanged();
        }
    }

    private class SetPunList extends AsyncTask<String, Integer, Long>
    {
        @Override
        protected Long doInBackground(String... userID)
        {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//            Sleep to test loading spinner

            ArrayList<ArrayList<String>> puns = parse.getUserPuns(userID[0]);

            for (int i = 0; i < puns.size(); i++)
            {
                ArrayList<String> current = puns.get(i);
                final PunModel sched = new PunModel();

                sched.setPun(current.get(0));
                sched.setPunVotes(current.get(1));
                sched.setThemeTitle(current.get(2));
                sched.setThemeID(current.get(3));
                sched.setThemeAuth(current.get(4));
                sched.setThemeDesc(current.get(5));
                sched.setThemeExp(current.get(6));

                CustomListViewValuesArrPun.add(sched);
            }
            return null;
        }
        @Override
        protected void onPostExecute(Long result)
        {
            findViewById(R.id.punProgressBar).setVisibility(View.INVISIBLE);
            punAdapter.notifyDataSetChanged();
        }
    }

    private class SetTopPunList extends AsyncTask<String, Integer, Long>
    {
        @Override
        protected Long doInBackground(String... userID)
        {
//            try {
//                Thread.sleep(3000);
//            } catch (InterruptedException e) {
//            Sleep to test loading spinner

            ArrayList<ArrayList<String>> puns = parse.getUserTopPuns(userID[0]);

            for (int i = 0; i < puns.size(); i++)
            {
                ArrayList<String> current = puns.get(i);
                final PunModel sched = new PunModel();

                sched.setPun(current.get(0));
                sched.setPunVotes(current.get(1));
                sched.setThemeTitle(current.get(2));

                CustomListViewValuesArrTopPun.add(sched);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Long result)
        {
            findViewById(R.id.topPunProgressBar).setVisibility(View.INVISIBLE);
            topPunAdapter.notifyDataSetChanged();
        }
    }

    public void onThemeItemClick(int mPosition)
    {
        ListModel tempValues = ( ListModel ) CustomListViewValuesArrTheme.get(mPosition);

        Intent i = new Intent(Profile.this, Puns.class);
        i.putExtra("LOBBY_ID", tempValues.getLobbyID());
        i.putExtra("THEME_TITLE", tempValues.getLobbyTitle());
        i.putExtra("THEME_DESC", tempValues.getLobbyDes());
        i.putExtra("THEME_AUTHOR", tempValues.getLobbyAuthor());
        i.putExtra("THEME_EXPIRE", tempValues.getExpireDate());
        startActivity(i);
    }

    public void onPunItemClick(int mPosition)
    {
        PunModel tempValues = ( PunModel ) CustomListViewValuesArrPun.get(mPosition);

        Intent i = new Intent(Profile.this, Puns.class);
        i.putExtra("LOBBY_ID", tempValues.getThemeID());
        i.putExtra("THEME_TITLE", tempValues.getThemeTitle());
        i.putExtra("THEME_DESC", tempValues.getThemeDesc());
        i.putExtra("THEME_AUTHOR", tempValues.getThemeAuth());
        i.putExtra("THEME_EXPIRE", tempValues.getThemeExp());
        startActivity(i);
    }

    @Override
    public void sendFriendRequest (MenuItem item)
    {
        String tempFriendOne = profile.getId();
        String tempFriendTwo = userID;

        parse.createFriendRequest(tempFriendOne, tempFriendTwo);
        isFriend = true;
        requestSent = true;
        this.invalidateOptionsMenu();
        Toast.makeText(getApplicationContext(), "Request sent to: " + name.getText(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeFriend (MenuItem item)
    {
        String tempFriendOne = profile.getId();
        String tempFriendTwo = userID;

        parse.removeFriendship(tempFriendOne, tempFriendTwo);
        isFriend = false;
        requestSent = false;
        this.invalidateOptionsMenu();
        Toast.makeText(getApplicationContext(), "Friend: " + name.getText() + " removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void removeRequest (MenuItem item)
    {
        String tempRequestFrom = profile.getId();
        String tempRequestTo = userID;

        parse.removeFriendRequest(tempRequestFrom, tempRequestTo);
        isFriend = false;
        requestSent = false;
        this.invalidateOptionsMenu();
        Toast.makeText(getApplicationContext(), "Request to: " + name.getText() + " removed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void acceptRequest (MenuItem item)
    {
        String tempFriendOne = userID;
        String tempFriendTwo = profile.getId();

        parse.removeFriendRequest(tempFriendOne, tempFriendTwo);
        parse.createFriendRelation(tempFriendOne, tempFriendTwo);
        isFriend = true;
        notSelf = true;
        this.invalidateOptionsMenu();
        Toast.makeText(getApplicationContext(), "Request from: " + name.getText() + " accepted", Toast.LENGTH_SHORT).show();
    }
}
