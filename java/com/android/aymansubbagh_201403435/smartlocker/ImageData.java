
import android.content.Context;
import android.widget.Toast;


public class ImageData {


    static byte[] ImageArray;
    Context context;

    ImageData(Context context) {

        this.context = context;
    }


    public void setImageData(byte[] ImageData) {

        this.ImageArray = ImageData;
        Toast.makeText(context, ImageData.length+"\n"+getImageData().length, Toast.LENGTH_LONG).show();

    }

    public byte[] getImageData() {

        return ImageArray;
    }

}
