package watson.punwarz;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.*;
import com.facebook.Profile;
//TODO javadoc
/**
 * Author: Carille
 * Created: 2016-03-06
 * Desc: Handles adding new puns to the system
 */
public class AddPun extends AddTitle
{

    private String lobbyID;
    private String title;

    private EditText editText;
    private TextView countText;
    private final TextWatcher editTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            //set TextView to current count
            countText.setText(String.valueOf(s.length()) + "/" + getResources().getString(R.string.PUN_LIMIT));
        }
        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addpun);
        FacebookSdk.sdkInitialize(getApplicationContext());
        Toolbar toolbar = (Toolbar)findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);

        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        lobbyID = this.getIntent().getStringExtra("LOBBY_ID");
        title = this.getIntent().getStringExtra("THEME_TITLE");

        TextView titleView = (TextView)findViewById(R.id.lobbyTitle);
        editText = (EditText)findViewById(R.id.editText);
        countText = (TextView)findViewById(R.id.countText);

        countText.setText("0/" + getResources().getString(R.string.PUN_LIMIT));

        editText.addTextChangedListener(editTextWatcher);
        titleView.setText(title);


    }

    public void submitPun(View v)
    {
        ParseApplication parse = new ParseApplication();
        Profile profile = Profile.getCurrentProfile();

        EditText punText = (EditText)findViewById(R.id.editText);
        String pun = punText.getText().toString();

        if(!parse.doesPunExist(pun, lobbyID))
        {
            parse.createNewPun(profile.getId(), lobbyID, pun);

            Toast.makeText(getApplicationContext(), "Pun added Successfully!", Toast.LENGTH_SHORT).show();
            Intent i = new Intent(AddPun.this, Puns.class);
            Intent intent = getIntent();
            i.putExtra("LOBBY_ID", lobbyID);
            i.putExtra("THEME_TITLE", title);
            i.putExtra("THEME_DESC", intent.getStringExtra("THEME_DESC"));
            i.putExtra("THEME_AUTHOR", intent.getStringExtra("THEME_AUTHOR"));
            i.putExtra("THEME_EXPIRE", intent.getStringExtra("THEME_EXPIRE"));

            destroyKeyboard();
            startActivity(i);
        }
        else
        {
            Toast.makeText(getApplicationContext(), "Pun already exists!", Toast.LENGTH_SHORT).show();
            destroyKeyboard();
        }

    }

    public void cancelEvent(View v)
    {
        Intent i = new Intent(AddPun.this, Lobby.class);
        destroyKeyboard();
        startActivity(i);
    }

    public void destroyKeyboard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
