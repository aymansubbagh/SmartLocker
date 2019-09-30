
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;



public class LoginActivity extends AppCompatActivity {

    SharedPreferences pref;
    protected EditText EmailID, Password;
    protected Button SignIn, SignUp;
    protected RelativeLayout relativeLayout;
    DatabaseHelper myHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        Boolean ans = weHavePermission();
        if (!ans) {
            requestforPermissionFirst();
        }

        getSupportActionBar().hide();

        pref = getSharedPreferences("SmartLocker", Context.MODE_PRIVATE);
        String userId_pref = pref.getString("UserId", "");

//        if (userId_pref.compareTo("") != 0) {
//            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//            startActivity(intent);
//            finish();
//        }

        init();

    }


    public void init() {

        myHelper = new DatabaseHelper(this);
        EmailID = (EditText) findViewById(R.id.loginUserName);
        Password = (EditText) findViewById(R.id.loginPassword);
        SignIn = (Button) findViewById(R.id.loginButton);
        SignUp = (Button) findViewById(R.id.signUp);

        relativeLayout = (RelativeLayout) findViewById(R.id.activity_login);

        SignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this,RegisterActivity.class);
                startActivity(intent);
            }
        });


        SignIn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (checkCriteria()) {

                            String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                            if (EmailID.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Email ID is required", Snackbar.LENGTH_SHORT).show();
                                EmailID.requestFocus();
                            } else if (!((EmailID.getText().toString()).trim()).matches(emailPattern)) {
                                Snackbar.make(relativeLayout, "Enter Valid Email Id", Snackbar.LENGTH_LONG).show();
                                EmailID.requestFocus();
                            } else if (Password.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Password is required", Snackbar.LENGTH_LONG).show();
                                Password.requestFocus();
                            } else {

                                boolean idExist = myHelper.LoginFuntion(EmailID.getText().toString(), Password.getText().toString());

                                if (idExist) {

                                    SharedPreferences.Editor editor = pref.edit();
                                    editor.putString("UserId", EmailID.getText().toString());
                                    editor.apply();
                                    editor.commit();

                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();

                                } else {
                                    Snackbar.make(relativeLayout, "Invalid Credential", Snackbar.LENGTH_LONG).show();
                                    EmailID.setText("");
                                    Password.setText("");
                                }

                            }

                        } else {
                            new AlertDialog.Builder(LoginActivity.this)
                                    .setMessage("All fields are mandatory. Please enter all details")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }
                }

        );


    }


    protected boolean checkCriteria() {
        boolean b = true;
        if ((EmailID.getText().toString()).equals("")) {
            b = false;
        }
        return b;
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
