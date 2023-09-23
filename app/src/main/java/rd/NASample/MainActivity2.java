package rd.NASample;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MainActivity2 extends AppCompatActivity {
    private String[] stringArray;
    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;
    private WebView webView;
    private Boolean isNotFound = false;
    Handler handler = new Handler();
    Runnable runnable;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        webView = findViewById(R.id.webView);

        Intent intent = getIntent();
        stringArray = intent.getStringArrayExtra("values");

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDatabaseEnabled(true);
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.loadUrl("https://beta-dealer.k4commu.co.th/services/sim-register");

        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);

    }

    private Date convertThaiBuddhistToGregorian(String thaiBuddhistDate) {
        SimpleDateFormat thaiBuddhistFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
        thaiBuddhistFormat.setTimeZone(TimeZone.getTimeZone("Asia/Bangkok"));

        // Parse the Thai Buddhist date
        Date parsedDate = null;
        try {
            parsedDate = thaiBuddhistFormat.parse(thaiBuddhistDate);

            // Create a Calendar object and set the parsed date
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(parsedDate);

            // Subtract 543 years to convert to Gregorian
            calendar.add(Calendar.YEAR, -543);

            // Get the converted Gregorian date
            return calendar.getTime();
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getGregorianDate(String thaiBuddhistDate) {
        Date gregorianDate = convertThaiBuddhistToGregorian(thaiBuddhistDate);
        SimpleDateFormat gregorianFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
        return gregorianFormat.format(gregorianDate);
    }
    public class MyWebChromeClient extends WebChromeClient {
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            // make sure there is no existing message
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(MainActivity2.this, "Cannot open file chooser", Toast.LENGTH_LONG).show();
                return false;
            }

            return true;
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data)  {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SELECT_FILE) {
            if (uploadMessage == null) return;
            uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, data));
            uploadMessage = null;


            runnable = new Runnable() {
                @Override
                public void run() {
                    checkForTextInWebView("กรอกข้อมูลส่วนบุคคล");
                    handler.postDelayed(this, 5000);
                }
            };
            handler.post(runnable);
        }
    }

    private void checkForTextInWebView(String value)  {
        webView.evaluateJavascript(
                "(function() { return document.body.innerText; })();",
                html -> {
                    // Check if the HTML content contains the search text
                    if (html != null && html.contains(value)) {
                        // The text is present on the page
                        try {
                            fillFormFields();
                        } catch (Exception ignored){
                        }
                    }
                }
        );
    }

    private void fillFormFields() {
        String javascript = "document.getElementsByName('firstname')[0].value = '" + stringArray[2] + "';" +
                "document.getElementsByName('lastname')[0].value = '" + stringArray[4] + "';" +
                "document.getElementsByName('personalId')[0].value = '" + stringArray[0] + "';" +
                "document.getElementsByName('birthDate')[0].value = '" + getGregorianDate(stringArray[14]) + "';" +
                "document.getElementsByName('address')[0].value = '" + stringArray[9] + " " + stringArray[10] + "';";

        webView.evaluateJavascript(javascript, null);
    }

    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
        super.onDestroy();

    }
}