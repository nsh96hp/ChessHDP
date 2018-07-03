/*
 * ChineseChess4Android
 *
 * Copyright (c) 2012 Zhijie Lee
 *
 * The MIT License (MIT)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.onezeros.chinesechess;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.chinesechess.R;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.kobakei.ratethisapp.RateThisApp;
import com.onezeros.chinesechess.ads.MyAdmobController;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;

import cn.domob.android.ads.DomobAdView;
import cn.domob.android.ads.DomobUpdater;

public class ChineseChessActivity extends AppCompatActivity {
    public static final String DOMOB_PUBLISHER_ID_STRING = "56OJyOeouMzH2P6sIM";
    public static String SHARE_APP_1 = "";
    public static String SHARE_APP_2 = "";

    ChessboardView mChessboardView;
    RelativeLayout mMainLayout;
    LinearLayout mMenuLayout;
    Button mNewGameButton;
    Button mContinueButton;
    Button mAbout, mExit, mRate, mShare, m2Player, mStatistical;
    ImageButton mUndo, mRedo, mLogout;
    TextView mInfoTextView;
    ImageView ic_com,img_logo;
    Adapter adapter;
    ArrayList<ChessResult> chessResults;
    ListView lv_statistical;

    int Measuredwidth = 0;
    int Measuredheight = 0;

    boolean mIsUIStart = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SHARE_APP_1 = "https://play.google.com/store/apps/details?id=" + getPackageName();

        SHARE_APP_2 = "market://details?id=" + getPackageName();

        setContentView(R.layout.main);

        mMainLayout = (RelativeLayout) findViewById(R.id.mainview);
        mMenuLayout = (LinearLayout) findViewById(R.id.menu_view);
        mNewGameButton = (Button) findViewById(R.id.new_game_btn);
        mAbout = (Button) findViewById(R.id.about_btn);
        mExit = (Button) findViewById(R.id.exit_btn);
        mRate = (Button) findViewById(R.id.rate_btn);
        mShare = (Button) findViewById(R.id.share_btn);
        mUndo = (ImageButton) findViewById(R.id.undo_btn);
        mRedo = (ImageButton) findViewById(R.id.redo_btn);
        mLogout = (ImageButton) findViewById(R.id.logout_btn);
        m2Player = (Button) findViewById(R.id.new_game_2_btn);
        img_logo=findViewById(R.id.img_logo);


        mContinueButton = (Button) findViewById(R.id.restore_game_btn);
        mStatistical = (Button) findViewById(R.id.statistical_btn);

        mChessboardView = (ChessboardView) findViewById(R.id.chessboard);

        mInfoTextView = (TextView) findViewById(R.id.info_tv);
        mChessboardView.setInfoTextview(mInfoTextView);

        //Tỉ lệ bàn cờ theo màn hình
        getDisplay();
        float ChessScale = (float) ((((float) Measuredheight / (float)Measuredwidth) / ((float)16 /(float) 9)) * 1.6);
        mChessboardView.getLayoutParams().height = (int) (Resources.getSystem().getDisplayMetrics().heightPixels / ChessScale);


        ic_com = findViewById(R.id.ic_com_thinking);
        mChessboardView.setInfoIconThinking(ic_com);


        mStatistical.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                dialog.setContentView(R.layout.statistical_dialog);
                TextView txt_total_1, txt_total_2, txt_total_3, txt_win_1, txt_win_2, txt_win_3, txt_lose_1, txt_lose_2, txt_lose_3;
                txt_lose_1 = dialog.findViewById(R.id.txt_lose_1);
                txt_lose_2 = dialog.findViewById(R.id.txt_lose_2);
                txt_lose_3 = dialog.findViewById(R.id.txt_lose_3);
                txt_total_1 = dialog.findViewById(R.id.txt_total_1);
                txt_total_2 = dialog.findViewById(R.id.txt_total_2);
                txt_total_3 = dialog.findViewById(R.id.txt_total_3);
                txt_win_1 = dialog.findViewById(R.id.txt_win_1);
                txt_win_2 = dialog.findViewById(R.id.txt_win_2);
                txt_win_3 = dialog.findViewById(R.id.txt_win_3);
                ImageButton exit = dialog.findViewById(R.id.exit_sta);
                exit.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                DBManager dbm = new DBManager(ChineseChessActivity.this);
                ArrayList<ChessResult> lstAll = new ArrayList();
                ArrayList<ChessResult> lst1 = new ArrayList();
                ArrayList<ChessResult> lst2 = new ArrayList();
                ArrayList<ChessResult> lst3 = new ArrayList();
                lstAll = dbm.getResultAll();
                lst1 = dbm.getResultWithLV(3);
                lst2 = dbm.getResultWithLV(4);
                lst3 = dbm.getResultWithLV(5);
                //Easy
                txt_total_1.setText(lst1.size() + "");
                int win = 0;
                for (int i = 0; i < lst1.size(); i++) {
                    if (lst1.get(i).getResult() == 1) {
                        win++;
                    }
                }
                txt_win_1.setText(win + "");
                txt_lose_1.setText(lst1.size() - win + "");
                //Medium
                txt_total_2.setText(lst2.size() + "");
                int win2 = 0;
                for (int i = 0; i < lst2.size(); i++) {
                    if (lst2.get(i).getResult() == 1) {
                        win2++;
                    }
                }
                txt_win_2.setText(win + "");
                txt_lose_2.setText(lst2.size() - win2 + "");
                //Hard
                txt_total_3.setText(lst3.size() + "");
                int win3 = 0;
                for (int i = 0; i < lst3.size(); i++) {
                    if (lst3.get(i).getResult() == 1) {
                        win3++;
                    }
                }
                txt_win_3.setText(win3 + "");
                txt_lose_3.setText(lst3.size() - win3 + "");


                chessResults = dbm.getResultAll();
                adapter = new Adapter(ChineseChessActivity.this, R.layout.item_tsa, chessResults);
                lv_statistical = dialog.findViewById(R.id.lv_statistical);
                lv_statistical.setAdapter(adapter);

                dialog.show();
            }
        });

        mNewGameButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                dialog.setContentView(R.layout.dialog_level);

                Button btn_lv1 = dialog.findViewById(R.id.btn_lv1);
                Button btn_lv2 = dialog.findViewById(R.id.btn_lv2);
                Button btn_lv3 = dialog.findViewById(R.id.btn_lv3);

                btn_lv1.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        mChessboardView.setAILevel(3);
                        mChessboardView.newGame();
                        switchViewTo(mMainLayout);
                        mIsUIStart = false;
                        mRedo.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                btn_lv2.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        mChessboardView.setAILevel(4);
                        mChessboardView.newGame();
                        switchViewTo(mMainLayout);
                        mIsUIStart = false;
                        mRedo.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                btn_lv3.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        mChessboardView.setAILevel(5);
                        mChessboardView.newGame();
                        switchViewTo(mMainLayout);
                        mIsUIStart = false;
                        mRedo.setVisibility(View.VISIBLE);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            }

        });

        m2Player.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                mChessboardView.newGame2Player();
                switchViewTo(mMainLayout);
                mIsUIStart = false;
            }
        });

        mAbout.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                dialog.setContentView(R.layout.dialog);
                dialog.show();


            }
        });
        mContinueButton.setOnClickListener(new OnClickListener() {

            public void onClick(View v) {
                if (mIsUIStart) {
                    mChessboardView.restoreGameStatus();
                }
                switchViewTo(mMainLayout);
                mIsUIStart = false;
            }
        });
        mExit.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                dialog.setContentView(R.layout.dialog_back);

                final Button btn_no = dialog.findViewById(R.id.btn_no);
                Button btn_yes = dialog.findViewById(R.id.btn_yes);

                btn_no.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });
                btn_yes.setOnClickListener(new OnClickListener() {
                    public void onClick(View view) {
                        dialog.dismiss();
                        finish();
                    }
                });
                dialog.show();
            }
        });

        mLogout.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mMainLayout.getVisibility() == View.VISIBLE) {
                    final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                    dialog.setContentView(R.layout.dialog_back);

                    final Button btn_no = dialog.findViewById(R.id.btn_no);
                    Button btn_yes = dialog.findViewById(R.id.btn_yes);

                    btn_no.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    btn_yes.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            if (mMainLayout.getVisibility() == View.VISIBLE) {
                                switchViewTo(mMenuLayout);
                            } else {
                                //super.onBackPressed();
                            }
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                }
            }
        });

        mRate.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Uri uri = Uri.parse(SHARE_APP_2);
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);

                if (!MyAppActivity(intent)) {
                    Uri uri1 = Uri.parse(SHARE_APP_1);
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, uri1);
                    if (!MyAppActivity(intent1)) {
                        Toast.makeText(getApplicationContext(), R.string.not_rate, Toast.LENGTH_LONG);
                    }
                }
            }
        });

        mShare.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_TEXT, SHARE_APP_1);
                try {
                    startActivity(Intent.createChooser(intent, getResources().getString(R.string.share)));
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getApplicationContext(), R.string.not_share, Toast.LENGTH_LONG);
                }
            }
        });

        mUndo.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if ((mChessboardView.keyBack >= 1 && mChessboardView.total_back > 0 && mChessboardView.mIsComputerThinking == false) || (mChessboardView.total_run >= 1 && mChessboardView.can_back > 0)) {
                    final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                    dialog.setContentView(R.layout.dialog_back);
                    TextView title_back = dialog.findViewById(R.id.title_back);
                    title_back.setText(R.string.you_want_undo);
                    final Button btn_no = dialog.findViewById(R.id.btn_no);
                    Button btn_yes = dialog.findViewById(R.id.btn_yes);

                    btn_no.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });
                    btn_yes.setOnClickListener(new OnClickListener() {
                        public void onClick(View view) {
                            mChessboardView.undo_back();
                            dialog.dismiss();
                        }
                    });
                    dialog.show();
                } else {
                    Toast.makeText(ChineseChessActivity.this, getResources().getString(R.string.you_cant_undo), Toast.LENGTH_SHORT).show();
                }
            }
        });
        mRedo.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mChessboardView.play_with_ai == 1) {
                    if (mChessboardView.key_redo > 0) {
                        final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                        dialog.setContentView(R.layout.dialog_back);
                        TextView title_back = dialog.findViewById(R.id.title_back);
                        title_back.setText(R.string.you_want_redo);
                        final Button btn_no = dialog.findViewById(R.id.btn_no);
                        Button btn_yes = dialog.findViewById(R.id.btn_yes);

                        btn_no.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        btn_yes.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                mChessboardView.redo_ai();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } else {
                        Toast.makeText(ChineseChessActivity.this, getResources().getString(R.string.you_cant_redo), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    if (mChessboardView.play_with_ai == 0 && mChessboardView.key_redo > 0) {
                        final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
                        dialog.setContentView(R.layout.dialog_back);
                        TextView title_back = dialog.findViewById(R.id.title_back);
                        title_back.setText(R.string.you_want_redo);
                        final Button btn_no = dialog.findViewById(R.id.btn_no);
                        Button btn_yes = dialog.findViewById(R.id.btn_yes);

                        btn_no.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                dialog.dismiss();
                            }
                        });
                        btn_yes.setOnClickListener(new OnClickListener() {
                            public void onClick(View view) {
                                mChessboardView.redo_player();
                                dialog.dismiss();
                            }
                        });
                        dialog.show();
                    } else {
                        Toast.makeText(ChineseChessActivity.this, getResources().getString(R.string.you_cant_redo), Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });


        // domob ad
        LinearLayout layout = (LinearLayout) findViewById(R.id.AdLinearLayout);
        DomobAdView adView = new DomobAdView(this, DOMOB_PUBLISHER_ID_STRING, DomobAdView.INLINE_SIZE_320X50);
        layout.addView(adView);

        DomobUpdater.checkUpdate(this, DOMOB_PUBLISHER_ID_STRING);


        //DuyLH - code Firebase Analytics

        FirebaseAnalytics anl = FirebaseAnalytics.getInstance(this);
        Bundle params = new Bundle();
        params.putString("action", "vao_app");

        anl.logEvent("vao_app", params);
        //end

        RateThisApp.Config config = new RateThisApp.Config(1, 5);
        config.setTitle(R.string.my_own_title);
        config.setMessage(R.string.my_own_message);
        config.setYesButtonText(R.string.my_own_rate);
        config.setNoButtonText(R.string.my_own_thanks);
        config.setCancelButtonText(R.string.my_own_cancel);

        String urlRate = "https://play.google.com/store/apps/details?id=" + getPackageName();
        config.setUrl(urlRate);
        RateThisApp.init(config);
        RateThisApp.onCreate(this);

        try {
            RateThisApp.showRateDialogIfNeeded(this);
        } catch (Exception e) {

        }
    }

    private boolean MyAppActivity(Intent intent) {
        try {
            startActivity(intent);
            return (true);
        } catch (ActivityNotFoundException e) {
            return false;
        }
    }

    public void getDisplay() {
        Point size = new Point();
        WindowManager w = getWindowManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            w.getDefaultDisplay().getSize(size);
            Measuredwidth = size.x;
            Measuredheight = size.y;
        } else {
            Display d = w.getDefaultDisplay();
            Measuredwidth = d.getWidth();
            Measuredheight = d.getHeight();
        }
    }


    void switchViewTo(View v) {
        if (v == mMainLayout) {
            mMenuLayout.setVisibility(View.INVISIBLE);
            mMainLayout.setVisibility(View.VISIBLE);
        } else if (v == mMenuLayout) {
            mMenuLayout.setVisibility(View.VISIBLE);
            mMainLayout.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onBackPressed() {

        if (mMainLayout.getVisibility() == View.VISIBLE) {
            final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
            dialog.setContentView(R.layout.dialog_back);

            final Button btn_no = dialog.findViewById(R.id.btn_no);
            Button btn_yes = dialog.findViewById(R.id.btn_yes);

            btn_no.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_yes.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (mMainLayout.getVisibility() == View.VISIBLE) {
                        switchViewTo(mMenuLayout);
                    } else {
                        //super.onBackPressed();
                    }
                    dialog.dismiss();
                }
            });
            dialog.show();
        } else {
            final Dialog dialog = new Dialog(ChineseChessActivity.this, R.style.mydialogstyle);
            dialog.setContentView(R.layout.dialog_back);

            final Button btn_no = dialog.findViewById(R.id.btn_no);
            Button btn_yes = dialog.findViewById(R.id.btn_yes);

            btn_no.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    dialog.dismiss();
                }
            });
            btn_yes.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    dialog.dismiss();

                    MyAdmobController.releaseQC_Callbacks();

                    finish();
                    //
                }
            });
            dialog.show();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
        mChessboardView.saveGameStatus();
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
}