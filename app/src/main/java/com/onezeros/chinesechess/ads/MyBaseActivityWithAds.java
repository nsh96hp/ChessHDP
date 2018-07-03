package com.onezeros.chinesechess.ads;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;


/**
 * Created by a1 on 10/6/17.
 */

public abstract class MyBaseActivityWithAds extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    protected boolean isHienThiQcBanner = true;

    @Override
    public void setContentView(int layoutResId) {
        super.setContentView(layoutResId);

        if (isHienThiQcBanner) {
            MyAdmobController.HienThiQCBanner(this);

            if (CheckInternet.isNetworkAvailable(getApplicationContext()))
                lastNetworkAvailble = true;

            hCheckLoadQcBanner.post(rNetworkHienthiQcBanner);
        }

    }

    Handler hCheckLoadQcBanner = new Handler();

    boolean lastNetworkAvailble = false;

    Runnable rNetworkHienthiQcBanner = new Runnable() {

        @Override
        public void run() {
            // TODO Auto-generated method stub

            if (!lastNetworkAvailble
                    && CheckInternet.isNetworkAvailable(getApplicationContext())) {
                MyAdmobController.HienThiQCBanner(MyBaseActivityWithAds.this);
            } else if (lastNetworkAvailble
                    && !CheckInternet.isNetworkAvailable(getApplicationContext())) {
                //ads.MyAdmobController.HienThiQCBanner(ads.MyBaseActivityWithAds.this);
            }

            if (CheckInternet.isNetworkAvailable(getApplicationContext())) {
                lastNetworkAvailble = true;
            } else {
                lastNetworkAvailble = false;
            }


            hCheckLoadQcBanner.postDelayed(rNetworkHienthiQcBanner, 4000);

        }
    };

    private boolean doubleBackToExitPressedOnce = false;

    public void showAdsFullWhenBackPressed () {
        hCheckLoadQcBanner.removeCallbacksAndMessages(null);

        MyAdmobController.showAdsFullBeforeDoAction(new Idelegate() {
            @Override
            public void callBack(Object value, int where) {

            }
        });
    }


    public void onBackPressed() {

        hCheckLoadQcBanner.removeCallbacksAndMessages(null);

        if (!(this instanceof MyBaseMainActivity)) {

            super.onBackPressed();

            MyAdmobController.showAdsFullBeforeDoAction(new Idelegate() {
                @Override
                public void callBack(Object value, int where) {

                }
            });


        } else {

            if (doubleBackToExitPressedOnce) {

                MyAdmobController.releaseQC_Callbacks(); // back thoat app

                super.onBackPressed();
                return;
            }

            doubleBackToExitPressedOnce = true;

            String toast = "Press again to exit";
            Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();

            Handler h = new Handler();
            h.postDelayed(new Runnable() {

                @Override
                public void run() {
                    // TODO Auto-generated method stub
                    doubleBackToExitPressedOnce = false;
                }
            }, 2000);


        }
    }

}
