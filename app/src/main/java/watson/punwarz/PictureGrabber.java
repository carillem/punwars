package watson.punwarz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

/**
 * @author Daniel Tetzlaff
 * @version 1.0
 * Created: 2017-11-07
 *
 * Description: This class returns a picture resource based on a method required. i.e. if the user allows the use of their
 *              Facebook profile picture this will make a call to the Facebook Graph API to retrieve said picture
 */

//TODO enhance documentation
public class PictureGrabber
{
    private Bitmap result;

    public Bitmap getUserPicture(Context c, final String userID) {
        ParseApplication parse = new ParseApplication();

        if (!parse.userPicBypass(userID))
            {
                result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar1), 150, 150, false);

                result = getFacebookProfilePicture(userID);

                return result;
            }
        else
            {
                int temp = parse.getUserPicture(userID);

                switch (temp) {
                    case 1:
                        result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar1), 150, 150, false);
                        break;
                    case 2:
                        result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar2), 150, 150, false);
                        break;
                    case 3:
                        result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar3), 150, 150, false);
                        break;
                    case 4:
                        result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar4), 150, 150, false);
                        break;
                    case 5:
                        result = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(c.getResources(), R.drawable.avatar5), 150, 150, false);
                        break;
                }
            }
            return result;
        }

    public static Bitmap getFacebookProfilePicture (String userID)
    {
        Bitmap bitmap = null;
        try {
            URL imageURL = new URL("https://graph.facebook.com/" + userID + "/picture?height=150&width=150");
            bitmap = BitmapFactory.decodeStream(imageURL.openConnection().getInputStream());
        } catch (IOException e) { }
        return bitmap;
    }


}
