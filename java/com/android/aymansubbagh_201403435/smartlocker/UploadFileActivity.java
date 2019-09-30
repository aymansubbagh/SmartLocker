
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class UploadFileActivity extends AppCompatActivity {

    String path, File_Size;
    EditText FileName, FilePath, FileSize;
    TextView FileType, TextLimit;
    Button UploadBtn;
    RelativeLayout relativeLayout;
    DecimalFormat df = new DecimalFormat("#.00");

    SimpleDateFormat sdfd = new SimpleDateFormat("yyyy/MM/dd");
    SimpleDateFormat sdft = new SimpleDateFormat("HH:mm");

    SharedPreferences pref;
    ImageData IData;
    byte[] FileArray;
    Authentication authetication;

    String Filename, Filetype;
    DatabaseHelper myHelper;
    String AesKey;
    CheckBox checkBox;
    boolean check = false;
    String AESEFilePath = "";
    String email;
    String userId_pref;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.file_upload_activity);

        Intent intent = getIntent();
        path = intent.getStringExtra("path");

        pref = getSharedPreferences("SmartLocker", Context.MODE_PRIVATE);
        userId_pref = pref.getString("UserId", "");
        AesKey = pref.getString("AesKey", "");

        authetication = new Authentication(UploadFileActivity.this);
        myHelper = new DatabaseHelper(this);

        IData = new ImageData(UploadFileActivity.this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Upload File");

        init();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    public void init() {

        FileName = (EditText) findViewById(R.id.file_name);
        FilePath = (EditText) findViewById(R.id.file_path);
        FileSize = (EditText) findViewById(R.id.file_size);

        FileType = (TextView) findViewById(R.id.file_type);
        TextLimit = (TextView) findViewById(R.id.remaining_size);
        UploadBtn = (Button) findViewById(R.id.upload_Button);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_fileUpload);

        checkBox = (CheckBox) findViewById(R.id.previousFileValue);

        FileArray = IData.getImageData();


        File file = new File(path);
        try {
            String fname = file.getName();
            String fnameDetails[] = fname.split("\\.");
            final String fileName = fnameDetails[0];
            final String fileType = "." + fnameDetails[1];
            for(int i =0; i < fnameDetails.length; i++){
                Log.i("file name Details: ",String.valueOf(fnameDetails[i]));
            }

            File_Size = String.valueOf(file.length());
            double file_size = Integer.parseInt(String.valueOf(file.length() / 1024));

            if (file_size >= 4) {
                file_size = file_size / 1024;
                FileSize.setText(df.format(file_size) + " Mb");
            } else {
                FileSize.setText(df.format(file_size) + " Kb");
            }

            Filename = fileName;
            Filetype = fileType;

            FileName.setText(fileName);
            FileName.setSelection(FileName.length());
            FileType.setText(fileType);
            FilePath.setText(path);

            int file_length = FileName.length();
            TextLimit.setText(file_length + "/25 Words");

        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }


        FileName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                int file_length = FileName.length();
                TextLimit.setText(file_length + "/25 Words");
            }
        });


        UploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (FileName.getText().toString().equals("")) {
                    Toast.makeText(UploadFileActivity.this, "File Name is required", Toast.LENGTH_SHORT).show();

                } else {

                    Date dt = new Date();
                    String date = sdfd.format(dt.getTime());
                    String time = sdft.format(dt.getTime());

                    if (checkBox.isChecked()) {
                        check = true;
                    } else {
                        check = false;
                    }


                    try {

//                      AES Encryption
                        byte[] AESEncryptionResult = authetication.BYTE_AESencrypt(FileArray);
                        File file = new File(Environment.getExternalStorageDirectory() + File.separator + "SmartLocker");
                        file.mkdirs();
                        File fileAES = new File(file.getAbsolutePath(), Filename+".txt");
                        Log.i("file absolutePath: ", file.getAbsolutePath());
                        Log.i("file absolutePath: ", fileAES.getAbsolutePath());
                        FileOutputStream stream = new FileOutputStream(fileAES);
                        stream.write(AESEncryptionResult);
                        stream.close();

                        AESEFilePath = fileAES.getAbsolutePath();
                        Log.i("file absolutePath: ", AESEFilePath);

                    } catch (Exception e) {

                        Toast.makeText(UploadFileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

//                        String FileName, String FilePath,String FileExtension,String DateTime

                    boolean insertAns = myHelper.insert_FileData(userId_pref,Filename, AESEFilePath, Filetype, date + " " + time);
                        Log.i("getExtra email:",email);
                    if (insertAns) {
                        Toast.makeText(UploadFileActivity.this, "File Uploaded", Toast.LENGTH_SHORT).show();

                        if (check) {

                            File files = new File(path);
                            boolean deleted = files.delete();

                            if (deleted) {
                                    Toast.makeText(UploadFileActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
                               finish();
                            } else {
                                    Toast.makeText(UploadFileActivity.this, "Please Try again!", Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        }

                        Intent intent = new Intent(UploadFileActivity.this, MainActivity.class);
                       startActivity(intent);
                        finish();
                    } else
                        Snackbar.make(relativeLayout, "Not Uploaded", Snackbar.LENGTH_LONG).show();


                }
            }
        });


    }
}
