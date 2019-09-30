
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton FileSelectionOption;
    DatabaseHelper mysql;
    ArrayList<String> data;
    RecyclerView list;
    SharedPreferences pref;
    CoordinatorLayout MainScreenLayout;

    private static final int TAKE_PICTURE = 0;
    private static final int RESULT_LOAD_IMG = 1;
    ImageData IData;
    String UserEmailPref = "";
    String AesKey;
    Authentication authetication;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pref = getSharedPreferences("SmartLocker", Context.MODE_PRIVATE);
        UserEmailPref = pref.getString("UserId", "");
        AesKey = pref.getString("AesKey", "");
        Intent intent = new Intent(MainActivity.this,UploadFileActivity.class);
        intent.putExtra("UserEmail",UserEmailPref);

        authetication = new Authentication(MainActivity.this);

        mysql = new DatabaseHelper(this);
        IData = new ImageData(MainActivity.this);
        list = (RecyclerView) findViewById(R.id.filelist);
        MainScreenLayout = (CoordinatorLayout) findViewById(R.id.main_activity_layout);
        FileSelectionOption = (FloatingActionButton) findViewById(R.id.addFloatButton);

        Boolean ans = weHavePermission();
        if (!ans) {
            requestforPermissionFirst();
        }


        FileSelectionOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Boolean ans = weHavePermission();
                if (!ans) {
                    requestforPermissionFirst();
                } else {

                    showDialog();
                }

            }
        });


        if (UserEmailPref.compareTo("") != 0) {
            Cursor res = mysql.getUserList(UserEmailPref);

            if (res.getCount() == 0) {
                Snackbar.make(list, "No Key", Snackbar.LENGTH_SHORT).show();

            } else {
                res.moveToFirst();
                String checkKey = res.getString(0);
                if (checkKey.compareTo("Na") == 0) {
                    showKeyDialog();
                } else {

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("AesKey", checkKey);
                    editor.apply();
                    editor.commit();
                }

            }
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.home_menu_panel, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.logout) {

            SharedPreferences pref = getSharedPreferences("SmartLocker", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();
        ShowFileList();
    }


    public void showDialog() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.type_selection_dialog);

        ImageView CameraOption = (ImageView) dialog.findViewById(R.id.camera_option);
        ImageView GalleryOption = (ImageView) dialog.findViewById(R.id.gallery_option);


        CameraOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture(view);
                dialog.dismiss();
            }
        });


        GalleryOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loadFiles(view);
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public void showKeyDialog() {

        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.key_dialog);

        final EditText KeyText = (EditText) dialog.findViewById(R.id.file_key);
        Button SubmitBtn = (Button) dialog.findViewById(R.id.SubmitBtn);


        SubmitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String Key = KeyText.getText().toString() + KeyText.getText().toString() + KeyText.getText().toString() +
                        KeyText.getText().toString();

                boolean insertAns = mysql.UpdateKey(UserEmailPref, Key);

                if (insertAns) {

                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("AesKey", Key);
                    editor.apply();
                    editor.commit();
                    dialog.dismiss();

                    Toast.makeText(MainActivity.this, "Key is updated", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(MainActivity.this, "Key Not Updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }


            }
        });

        dialog.show();
    }


    public void takePicture(View view) {
        Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(i, TAKE_PICTURE);
    }


    public void loadFiles(View view) {
        Intent i = new Intent();
        i.setAction(Intent.ACTION_GET_CONTENT);
        i.setType("*/*");
        startActivityForResult(i, 1);
    }


    public String getPath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor =getContentResolver().query(uri, projection,null,null,null);
        String cl_index ="";
        if(cursor != null){
            int column_index;
            column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            cl_index=  cursor.getString(column_index);
            cursor.close();
        }

        return  cl_index;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case TAKE_PICTURE:
                if (requestCode == TAKE_PICTURE && resultCode == RESULT_OK) {

                    Bundle extras = data.getExtras();
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    Uri uri = getImageUri(getApplicationContext(), imageBitmap);
                    String Filepath = getPath(uri);
                    byte[] soundBytes;

                    try {
                        if (Filepath.compareTo("") != 0) {
                            File file = new File(Filepath);
                            if (file.exists()) {

                                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(Filepath)));
                                soundBytes = new byte[inputStream.available()];
                                soundBytes = toByteArray(inputStream);
                                IData.setImageData(soundBytes);

                                Intent intent = new Intent(MainActivity.this, UploadFileActivity.class);
                                intent.putExtra("path", Filepath);
                                startActivity(intent);

                            } else {

                                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                ad.setTitle("Can't Upload!");
                                ad.setMessage("Either the file doesnt exists or the user is restricted to access the file");
                                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                ad.show();
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                }


            case RESULT_LOAD_IMG:

                if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    byte[] soundBytes;
                    try {

                        String Filepath = ImageFilePath.getPath(MainActivity.this, uri);
                        if (Filepath.compareTo("") != 0) {
                            File file = new File(Filepath);
                            if (file.exists()) {

                                InputStream inputStream = getContentResolver().openInputStream(Uri.fromFile(new File(Filepath)));
                                soundBytes = new byte[inputStream.available()];
                                soundBytes = toByteArray(inputStream);
                                IData.setImageData(soundBytes);

                                Intent intent = new Intent(MainActivity.this, UploadFileActivity.class);
                                intent.putExtra("path", Filepath);
                                startActivity(intent);

                            } else {

                                AlertDialog.Builder ad = new AlertDialog.Builder(MainActivity.this);
                                ad.setTitle("Can't Upload!");
                                ad.setMessage("Either the file doesnt exists or the user is restricted to access the file");
                                ad.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });
                                ad.show();
                            }
                        }
                    } catch (Exception e) {

                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }


        }
    }


    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }


    public byte[] getBytes(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len = 0;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }


    public void ShowFileList() {

        Cursor res = mysql.getFileDetails(UserEmailPref);

        data = new ArrayList<String>();

        if (res.getCount() == 0) {
            list.setAdapter(null);
            Snackbar.make(list, "There is No Data available!", Snackbar.LENGTH_SHORT).show();
        } else {
            while (res.moveToNext()) {

                data.add(res.getString(0) + "*" + res.getString(1) + "*" + res.getString(2) + "*" + res.getString(3)
                        + "*" + res.getString(4));
//                PhotoId.add(res.getString(0));
            }

            RecyclerView.LayoutManager lm = new GridLayoutManager(MainActivity.this, 2);
            list.setLayoutManager(lm);
            Adapter adapt = new Adapter(data);
            list.setAdapter(adapt);
        }
    }


    public class Adapter extends RecyclerView.Adapter<Adapter.ViewHolder> {

        ArrayList<String> dataset;
        Context con;

        public class ViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            public TextView DateTime;
            public CardView card;
            public ImageView Cardmenu_options, cardOptions;

            public ViewHolder(View v) {
                super(v);
                name = (TextView) v.findViewById(R.id.file_name);
                DateTime = (TextView) v.findViewById(R.id.datetime);
                card = (CardView) v.findViewById(R.id.card);
                Cardmenu_options = (ImageView) v.findViewById(R.id.card_view_options);
                cardOptions = (ImageView) v.findViewById(R.id.card_pic);
            }
        }

        public Adapter(ArrayList<String> d) {
            dataset = d;
        }

        @Override
        public Adapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.file_cardview, parent, false);
            ViewHolder vh = new ViewHolder(v);
            con = parent.getContext();
            return vh;
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            //FID*FNAME*FPATH*FEXTENSION*DATETIME

            final String[] temp = dataset.get(position).split("\\*");
            holder.name.setText(temp[1]);

            String type = temp[3];
            String FileFormat = FileType(type);

            holder.DateTime.setText(temp[4]);


            if (FileFormat.equals("Image")) {

                holder.cardOptions.setImageResource(R.drawable.image_type);

            } else if (FileFormat.equals("Document")) {

                holder.cardOptions.setImageResource(R.drawable.document_type);

            } else if (FileFormat.equals("Video")) {

                holder.cardOptions.setImageResource(R.drawable.video_type);

            } else if (FileFormat.equals("Music")) {

                holder.cardOptions.setImageResource(R.drawable.music_type);

            } else {

                holder.cardOptions.setImageResource(R.drawable.all_type);

            }

            holder.card.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(MainActivity.this, holder.Cardmenu_options);
                    popup.getMenuInflater().inflate(R.menu.cardview_menu_panel, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.download_file) {

                                if (!weHavePermission()) {
                                    requestforPermissionFirst();
                                } else {

                                    DownloadFile(temp[1], temp[3]);
                                }
                            }

                            if (item.getItemId() == R.id.delete_file) {

                                Boolean res = mysql.DeleteFile(temp[0]);
                                if (res == true) {
                                    Toast.makeText(MainActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                                    ShowFileList();
                                } else {
                                    Toast.makeText(MainActivity.this, "Problem in File Deletion", Toast.LENGTH_SHORT).show();
                                }

                            }
                            return true;
                        }
                    });

                    popup.show();

                }
            });


            holder.Cardmenu_options.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    PopupMenu popup = new PopupMenu(MainActivity.this, holder.Cardmenu_options);
                    popup.getMenuInflater().inflate(R.menu.cardview_menu_panel, popup.getMenu());
                    popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                        @SuppressLint("LongLogTag")
                        public boolean onMenuItemClick(MenuItem item) {

                            if (item.getItemId() == R.id.download_file) {

                                if (!weHavePermission()) {
                                    requestforPermissionFirst();
                                } else {

                                    DownloadFile(temp[1], temp[3]);
                                }
                            }

                            if (item.getItemId() == R.id.delete_file) {

                                Boolean res = mysql.DeleteFile(temp[0]);
                                Log.i("Email of the deleted file",UserEmailPref);
                                if (res == true) {
                                    Toast.makeText(MainActivity.this, "File Deleted", Toast.LENGTH_SHORT).show();
                                    ShowFileList();
                                } else {
                                    Toast.makeText(MainActivity.this, "Problem in File Deletion", Toast.LENGTH_SHORT).show();
                                }

                            }
                            return true;
                        }
                    });

                    popup.show();

                }
            });
        }

        @Override
        public int getItemCount() {
            return dataset.size();
        }
    }


    public String FileType(String type) {

        if (type.contains("jpg") || type.contains("tiff") || type.contains("jpeg") || type.contains("bmp") || type.contains("gif") || type.contains("png")) {

            return "Image";
        } else if (type.contains("doc") || type.contains("docx") || type.contains("odt") || type.contains("pdf") || type.contains("txt") || type.contains("xml")) {

            return "Document";
        } else if (type.contains("mkv") || type.contains("flv") || type.contains("avi") || type.contains("wmv") || type.contains("mp4") || type.contains("3gp")) {

            return "Video";
        } else if (type.contains("mp3") || type.contains("msv") || type.contains("wav") || type.contains("wma") || type.contains("ogg") || type.contains("ram")) {

            return "Music";
        } else {

            return "Other";
        }

    }


    public void DownloadFile(String Filename, String Filetype) {
            try {

                // AES Decryption
                File file1 = new File(Environment.getExternalStorageDirectory() + File.separator + "SmartLocker");
                if (file1.mkdirs()) {
                    Toast.makeText(this, "Files Created", Toast.LENGTH_SHORT).show();
                }
                File fileAES1 = new File(file1.getAbsolutePath(), Filename + ".txt");
                int flength = (int) fileAES1.length();
                byte[] AESEResbytes = new byte[flength];
                FileInputStream in = new FileInputStream(fileAES1);
                if (in.read(AESEResbytes) == -1) {
                    Toast.makeText(this, "No more to read", Toast.LENGTH_SHORT).show();
                }

                byte[] dec = authetication.BYTE_AESdecrypt(AESEResbytes, AesKey, 1);
                in.close();

                File dfile2 = new File(Environment.getExternalStorageDirectory() + File.separator + "SmartLocker");
                if (dfile2.mkdirs()) {
                    Toast.makeText(this, "Files 2 Created", Toast.LENGTH_SHORT).show();
                }
                File fileDES2 = new File(dfile2.getAbsolutePath(), Filename + Filetype);
                FileOutputStream dstream2 = new FileOutputStream(fileDES2);
                dstream2.write(dec);
                dstream2.close();


                Toast.makeText(this, "File Downloaded in Smart Locker Folder", Toast.LENGTH_SHORT).show();

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {

//

                        Uri selectedUri = Uri.parse(Environment.getExternalStorageDirectory() + File.separator + "SmartLocker");
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(selectedUri, "resource/folder");
                        if (intent.resolveActivityInfo(getPackageManager(), 0) != null) {
                            startActivity(intent);
                        } else {
                            // if you reach this place, it means there is no any file
                            // explorer app installed on your device
                        }
                    }
                }, 1500);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (BadPaddingException e) {
                e.printStackTrace();
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
            } catch (InvalidKeyException e) {
                e.printStackTrace();
            } catch (InvalidAlgorithmParameterException e) {
                e.printStackTrace();
            } catch (NoSuchPaddingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
            }
            Intent intent = new Intent(MainActivity.this, FingetPrintActivity.class);
            startActivity(intent);
    }



    public byte[] toByteArray(InputStream in) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            int read = 0;
            byte[] buffer = new byte[1024];
            while (read != -1) {
                read = in.read(buffer);
                if (read != -1)
                    out.write(buffer, 0, read);
            }
            out.close();
            return out.toByteArray();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            return null;
        }
    }


    //Android Runtime Permission
    private boolean weHavePermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestforPermissionFirst() {
        if ((ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) || (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))) {
            requestForResultContactsPermission();
        } else {
            requestForResultContactsPermission();
        }
    }

    private void requestForResultContactsPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 111);
    }


}
