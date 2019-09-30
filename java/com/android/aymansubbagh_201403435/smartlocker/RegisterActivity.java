
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;



public class RegisterActivity extends AppCompatActivity {

    protected EditText Name, Contact, EmailID, Password, ConfirmPassord;
    protected Button RegisterBtn;
    protected RelativeLayout relativeLayout;
    DatabaseHelper myHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_activity);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle("Register");
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
        myHelper = new DatabaseHelper(this);
        Name = (EditText) findViewById(R.id.Name);
        Contact = (EditText) findViewById(R.id.phoneNumber);
        EmailID = (EditText) findViewById(R.id.emailID);
        Password = (EditText) findViewById(R.id.password);
        ConfirmPassord = (EditText) findViewById(R.id.confirmPassword);

        RegisterBtn = (Button) findViewById(R.id.registerButton);
        relativeLayout = (RelativeLayout) findViewById(R.id.activity_registration);


        RegisterBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (checkCriteria()) {

                            String match = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

                            if (Name.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Name is Required", Snackbar.LENGTH_SHORT).show();
                                Name.requestFocus();

                            } else if (Contact.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Contact is Required", Snackbar.LENGTH_SHORT).show();
                                Contact.requestFocus();

                            } else if (EmailID.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Email Id is required", Snackbar.LENGTH_SHORT).show();
                                EmailID.requestFocus();

                            } else if (!EmailID.getText().toString().matches(match)) {
                                Snackbar.make(relativeLayout, "Please Follow Email Standards", Snackbar.LENGTH_SHORT).show();
                                EmailID.requestFocus();

                            } else if (Password.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Password is Required", Snackbar.LENGTH_SHORT).show();
                                Password.requestFocus();

                            } else if (ConfirmPassord.getText().toString().equals("")) {
                                Snackbar.make(relativeLayout, "Confirm Password is Required", Snackbar.LENGTH_SHORT).show();
                                ConfirmPassord.requestFocus();

                            } else if (!Password.getText().toString().equals(ConfirmPassord.getText().toString())) {
                                Snackbar.make(relativeLayout, "Password Does not Match", Snackbar.LENGTH_SHORT).show();
                                Password.requestFocus();
                                Password.setText("");
                                ConfirmPassord.setText("");

                            }  else {

                                boolean idExist = myHelper.checkEmail(EmailID.getText().toString());
                                if (idExist) {
//                                    String name,String phone,String email,String password,String key

                                    boolean insertAns = myHelper.insertUser(Name.getText().toString(), Contact.getText().toString(),
                                            EmailID.getText().toString(), Password.getText().toString(), "Na");

                                    if (insertAns) {
                                        Snackbar.make(relativeLayout, "Registration Completed", Snackbar.LENGTH_LONG).show();
                                        finish();
                                    } else
                                        Snackbar.make(relativeLayout, "Error:Please Try Again", Snackbar.LENGTH_LONG).show();
                                } else {
                                    Snackbar.make(relativeLayout, "Email ID Already Exists", Snackbar.LENGTH_LONG).show();
                                }

                            }
                        } else {
                            new AlertDialog.Builder(RegisterActivity.this)
                                    .setMessage("")
                                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    })
                                    .show();
                        }
                    }


                });


    }

    protected boolean checkCriteria() {
        boolean b = true;
        if ((Name.getText().toString()).equals("")) {
            b = false;
        }
        return b;
    }


}
