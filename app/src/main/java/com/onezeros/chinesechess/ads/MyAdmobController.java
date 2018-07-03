package com.onezeros.chinesechess.ads;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RelativeLayout;

import com.android.chinesechess.R;
import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdSize;
import com.facebook.ads.InterstitialAdListener;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;


/**
 * Created by BHM on 1/7/2017.
 */
public class MyAdmobController { //

    public static String KEY_QUANGCAO = "key_qc"; //

    public static void setTypeQuangCao(Context ctx) {
        isUseAdmob = PreferenceManager.getDefaultSharedPreferences(ctx).getBoolean(KEY_QUANGCAO, true);


        FB_BANNER_ID = "941360079348128_941360882681381";
        FB_MANHINH_ID = "941360079348128_941360962681373";
    }


    private static InterstitialAd mInterstitialAd;
    private static com.facebook.ads.InterstitialAd mFbInterstitialAd;
    private static int flagQC = 1;

    public static boolean isUseAdmob = true;


    //baomoi
    private static String FB_BANNER_ID = "";

    private static String FB_MANHINH_ID = "";
    //

    public static void listenNetworkChangeToRequestAdsFull(Activity ac) {

        if (h == null)
            h = new Handler();

        if (rCheckNetworkLoadQcFull == null)
            rCheckNetworkLoadQcFull = new RCheckNetworkLoadQcFull(ac);

        h.post(rCheckNetworkLoadQcFull);
    }

    static RCheckNetworkLoadQcFull rCheckNetworkLoadQcFull;

    static boolean lastNetworkAvailble = false;

    static class RCheckNetworkLoadQcFull implements Runnable {
        Activity ac;

        public RCheckNetworkLoadQcFull(Activity ac) {
            this.ac = ac;
        }

        @Override
        public void run() {
            if (!lastNetworkAvailble
                    && CheckInternet.isNetworkAvailable(ac)) {
                requestNewInterstitial();
            }

            if (CheckInternet.isNetworkAvailable(ac)) {
                lastNetworkAvailble = true;
            } else {
                lastNetworkAvailble = false;
            }

            h.postDelayed(RCheckNetworkLoadQcFull.this, 4000);
        }
    }


    public static void XulyQCFull(final Activity ac) {
        // hiển thị quảng cáo

        // quảng cáo full màn hình
        if (mInterstitialAd == null)
            mInterstitialAd = new InterstitialAd(ac);

        String adUnitId = mInterstitialAd.getAdUnitId();

        if (adUnitId == null || adUnitId.equals("")) {

            if (isUseAdmob)
                mInterstitialAd.setAdUnitId(getManhinhAdsId(ac));
            else
                mInterstitialAd.setAdUnitId("11");


        }

        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                requestNewInterstitial();
                // abc

            }
        });


        //facebook ads
        mFbInterstitialAd = new com.facebook.ads.InterstitialAd(ac, FB_MANHINH_ID);

        requestNewInterstitial();


        h.post(rQuangcao);
    }


    public static void requestNewInterstitial() { // DuyLH - QC

        if (isUseAdmob) {
            AdRequest adRequest = MyAdmobController.getAdRequest();

            if (mInterstitialAd != null && !mInterstitialAd.isLoaded())
                mInterstitialAd.loadAd(adRequest);
        } else {
            try {
                if (mFbInterstitialAd != null && !mFbInterstitialAd.isAdLoaded())
                    mFbInterstitialAd.loadAd();
            } catch (Exception e) {

            }
        }


    }


    public static Handler h = new Handler();

    public static Runnable rQuangcao = new Runnable() {

        @Override
        public void run() {

            flagQC = 1;

            h.postDelayed(rQuangcao, TIME_GIANCACH_QC);
        }
    };

    protected static AdRequest getAdRequest() {
        AdRequest adRequest = new AdRequest.Builder().build();

        return adRequest;
    }

    static final int TIME_GIANCACH_QC = 60 * 1000 * 3;

    public static void showAdsFullBeforeDoAction(final Idelegate callback) {

        if (mInterstitialAd == null && mFbInterstitialAd == null) {
            callback.callBack(0, 0);
            return;
        }

        if (flagQC == 1) {

            if (isUseAdmob) {
                if (mInterstitialAd.isLoaded()) {

                    mInterstitialAd.setAdListener(new AdListener() {
                        @Override
                        public void onAdClosed() {

                            requestNewInterstitial();

                            flagQC = 0;

                            h.removeCallbacks(MyAdmobController.rQuangcao);

                            h.postDelayed(MyAdmobController.rQuangcao, TIME_GIANCACH_QC);

                            callback.callBack(0, 0);
                        }
                    });

                    mInterstitialAd.show();


                } else {
                    callback.callBack(0, 0);

                    requestNewInterstitial();
                }
            } else {
                if (mFbInterstitialAd.isAdLoaded()) {
                    mFbInterstitialAd.setAdListener(new InterstitialAdListener() {
                        @Override
                        public void onInterstitialDisplayed(Ad ad) {

                        }

                        @Override
                        public void onInterstitialDismissed(Ad ad) {

                            callback.callBack(0, 0);

                            requestNewInterstitial();

                            flagQC = 0;

                            h.removeCallbacks(MyAdmobController.rQuangcao);

                            h.postDelayed(MyAdmobController.rQuangcao, TIME_GIANCACH_QC);

                        }

                        @Override
                        public void onError(Ad ad, AdError adError) {

                        }

                        @Override
                        public void onAdLoaded(Ad ad) {

                        }

                        @Override
                        public void onAdClicked(Ad ad) {

                        }

                        @Override
                        public void onLoggingImpression(Ad ad) {

                        }
                    });

                    try {
                        mFbInterstitialAd.show();
                    } catch (Exception e) {
                        callback.callBack(0, 0);
                    }

                } else {
                    callback.callBack(0, 0);

                    requestNewInterstitial();
                }
            }

        } else {

            callback.callBack(0, 0);

            requestNewInterstitial();
        }
    }

    public static void HienThiQCBanner(final Activity ctx) {

        final AdView mAdViewBanner = new AdView(ctx);

//        if (MyIAPUtils.localCheckIsPurchase(ctx)) {
//            mAdViewBanner.setVisibility(View.GONE);
//
//            return;
//        }


        String adUnitId = mAdViewBanner.getAdUnitId();

        if (adUnitId == null || adUnitId.equals("")) {
            String adsId = getBannerAdsId(ctx);

            mAdViewBanner.setAdSize(com.google.android.gms.ads.AdSize.SMART_BANNER);

            mAdViewBanner.setAdUnitId(adsId);
        }

        // test
//        ctx.findViewById(R.id.root_adsfooter).setVisibility(View.GONE);
//        mAdViewBanner.setVisibility(View.GONE);
        //

        final RelativeLayout adViewContainer = (RelativeLayout) ctx.findViewById(R.id.adView_container);

        if (isUseAdmob) {

            adViewContainer.removeAllViews();

            try {
                adViewContainer.addView(mAdViewBanner);
            } catch (Exception e) {

            }


            AdRequest adRequest = new AdRequest.Builder().build();
            mAdViewBanner.loadAd(adRequest);
            mAdViewBanner.setAdListener(new AdListener() {
                @Override
                public void onAdFailedToLoad(int errorCode) {
                    super.onAdFailedToLoad(errorCode);

                    adViewContainer.removeAllViews();

//                    com.facebook.ads.AdView adViewFB = new com.facebook.ads.AdView(ctx, FB_BANNER_ID, AdSize.BANNER_HEIGHT_50);
//                    adViewContainer.addView(adViewFB);
//                    adViewFB.loadAd();
                }
            });
        } else {

            adViewContainer.removeAllViews();

            final com.facebook.ads.AdView adViewFB = new com.facebook.ads.AdView(ctx, FB_BANNER_ID, AdSize.BANNER_HEIGHT_50);

            try {
                adViewContainer.addView(adViewFB);
            } catch (Exception e) {

            }

            adViewFB.setAdListener(new com.facebook.ads.AdListener() {
                @Override
                public void onError(Ad ad, AdError adError) {

                    adViewFB.setVisibility(View.GONE);

                    adViewContainer.removeAllViews();

                    adViewContainer.addView(mAdViewBanner);

                    AdRequest adRequest = new AdRequest.Builder().build();
                    mAdViewBanner.loadAd(adRequest);
                }

                @Override
                public void onAdLoaded(Ad ad) {

                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            });

            adViewFB.loadAd();
        }
    }

    public static String getApplicationAdsId(Context ctx) {

        String packageName = ctx.getPackageName();

        if (packageName.equals("com.hdpsolution.cotuong")) {
            return "ca-app-pub-4044992062032501~2105645203";
        } else if (packageName.equals("com.hdpsolution.cotuong_eng")) {
            return "ca-app-pub-4044992062032501~3327512945";
        } else if (packageName.equals("com.hdpsolution.cotuong_zh")) {
            return "ca-app-pub-4044992062032501~3495033904";
        }

        return "ca-app-pub-4044992062032501~6844619276";
    }


    private static String getBannerAdsId(Context ctx) {

//        String packageName = ctx.getPackageName();
//
//        if (packageName.equals("com.hdpsolution.cotuong")) {
//            return "ca-app-pub-4044992062032501/7561986011";
//        } else if (packageName.equals("com.hdpsolution.cotuong_eng")) {
//            return "ca-app-pub-4044992062032501/7729792384";
//        } else if (packageName.equals("com.hdpsolution.cotuong_zh")) {
//            return "ca-app-pub-4044992062032501/7757427130";
//        }

        return "ca-app-pub-3940256099942544/6300978111";
    }

    private static String getManhinhAdsId(Context ctx) {

//        String packageName = ctx.getPackageName();
//
//        if (packageName.equals("com.hdpsolution.cotuong")) {
//            return "ca-app-pub-4044992062032501/7178842630";
//        } else if (packageName.equals("com.hdpsolution.cotuong_eng")) {
//            return "ca-app-pub-4044992062032501/2636962396";
//        } else if (packageName.equals("com.hdpsolution.cotuong_zh")) {
//            return "ca-app-pub-4044992062032501/9801136544";
//        }

        return "ca-app-pub-3940256099942544/1033173712";
    }

    public static void releaseQC_Callbacks() {
        if (h != null)
            h.removeCallbacksAndMessages(null);
        flagQC = 1;
    }
}
