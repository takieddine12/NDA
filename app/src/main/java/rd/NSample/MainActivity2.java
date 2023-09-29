package rd.NSample;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.ConsoleMessage;
import android.webkit.CookieManager;
import android.webkit.JavascriptInterface;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class MainActivity2 extends AppCompatActivity {
    private String[] stringArray;
    public static final int REQUEST_SELECT_FILE = 100;
    public ValueCallback<Uri[]> uploadMessage;
    private WebView webView;
    Handler handler = new Handler();
    Runnable runnable;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        webView = findViewById(R.id.webView);

        Intent intent = getIntent();
        stringArray = intent.getStringArrayExtra("values");


        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebViewClient(new MyWebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());
       // webView.loadUrl("https://beta-dealer.k4commu.co.th/services/card-reader/sim-register");
        webView.loadUrl("https://alrasab.com/test/register.php");

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

    public class MyWebViewClient extends WebViewClient {
        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            fillFormFields();
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
        webView.evaluateJavascript("(function() { return document.body.innerText; })();",
                html -> {
                    // Check if the HTML content contains the search text
                    if (html != null && html.contains(value)) {
                        // The text is present on the page
                        try {
                            fillFormFieldsForRasab();
                            handler.removeCallbacks(runnable);
                            handler.removeCallbacksAndMessages(null);
                        } catch (Exception ignored){
                        }
                    }
                }
        );
    }

    private void fillFormFieldsForRasab() {

        String firstNameScript = "document.getElementsByName('name')[0].value = '" + stringArray[2] + "';";
        String lastNameScript = "document.getElementsByName('lastname')[0].value = '" + stringArray[4] + "';";
        String personalIdScript = "document.getElementsByName('personalId')[0].value = '" + stringArray[0] + "';";
        String birthDateScript = "document.getElementsByName('birthDate')[0].value = '" + getGregorianDate(stringArray[18]) + "';";
        String addressScript = "document.getElementsByName('address')[0].value = '" + stringArray[9] + " " + stringArray[10] + "';";

        webView.evaluateJavascript("javascript:(function(){" +
                firstNameScript + lastNameScript + personalIdScript  + addressScript + "})()",null);

    }
    private void fillFormFieldsForK4() {

        String firstNameScript = "document.getElementsByName('firstname')[0].value = '" + stringArray[2] + "';";
        String lastNameScript = "document.getElementsByName('lastname')[0].value = '" + stringArray[4] + "';";
        String personalIdScript = "document.getElementsByName('personalId')[0].value = '" + stringArray[0] + "';";
        String birthDateScript = "document.getElementsByName('birthDate')[0].value = '" + getGregorianDate(stringArray[18]) + "';";
        String addressScript = "document.getElementsByName('address')[0].value = '" + stringArray[9] + " " + stringArray[10] + "';";

        webView.evaluateJavascript("javascript:(function(){" +
                firstNameScript + lastNameScript + personalIdScript + birthDateScript + addressScript + "})()",null);

    }


    @Override
    protected void onDestroy() {
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler.removeCallbacksAndMessages(null);
        }
        super.onDestroy();

    }
}




