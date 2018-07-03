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


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;

import com.android.chinesechess.R;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.drm.ProcessedData;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RectF;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

public class ChessboardView extends ImageView {
    static final int TIME_WAIT_COMPUTER = 1000; //Time computer thinking + min 500.
    int keyBack = 0;
    int total_back = 0;
    int can_back = 0;
    int total_run = 0;
    int check_back=0;

    int play_with_ai = 1;
    int key_redo = 0;


    int LV;
    DBManager dbManager;
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Calendar cal;

    static final int USER_COLOR = AI.LIGHT;
    static final int USER2_COLOR = AI.DARK;
    static final int MSG_USER_MOVE_DONE = 0;
    static final int MSG_COMPUTER_MOVE_DONE = 1;
    static final int MSG_COMPUTER_WIN = 2;
    static final int MSG_USER_WIN = 3;
    static final int MSG_USER_WIN_1 = 4;
    static final int MSG_USER_WIN_2 = 5;
    static final String SAVE_STATE_FILE_NAME = "bundledata.txt";
    static final String SAVE_STATE_FILE_NAME_TEMP = "bundledata1.txt";
    static final String SAVE_STATE_FILE_NAME1 = "bundledata2.txt";
    static final String SAVE_STATE_FILE_NAME_TEMP1 = "bundledata21.txt";
    static final String SAVE_STATE_FILE_NAME_REDO = "bundledata22.txt";
    static final String SAVE_STATE_FILE_NAME_REDO_2P_RED = "bundledata221.txt";
    static final String SAVE_STATE_FILE_NAME_REDO_2P_BLACK = "bundledata222.txt";
    Bitmap[][] mChessBitmaps = new Bitmap[2][7];
    Bitmap mSelectBitmap = null;
    float mLaticeLen = -1;
    float mLaticeLen2;
    float mChesslen;
    float mChessLen2;
    int mChessFrom = -1;
    int mChessTo = -1;
    float mStartBoardX;
    float mStartBoardY;
    boolean mIsComputerThinking = false;
    // back up of ai status for drawing
    int[] mPieces = new int[AI.BOARD_SIZE];
    int[] mColors = new int[AI.BOARD_SIZE];

    AI mAi = new AI();
    MessageHandler mMessageHandler = new MessageHandler();
    Context mContext;
    Dialog dialog;
    TextView title;
    TextView mInfoTextView;
    ImageView mIconThink;
    Animation anim_thinking;


    public ChessboardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public ChessboardView(Context context) {
        super(context);
        init(context);
    }

    public void setInfoTextview(TextView tv) {
        mInfoTextView = tv;
    }
    public void setInfoIconThinking(ImageView imgv) {
        mIconThink = imgv;
    }

    void init(Context context) {
        mContext = context;

        dialog = new Dialog(mContext, R.style.mydialogstyle);
        dialog.setContentView(R.layout.dialog_back);

        title = dialog.findViewById(R.id.title_back);

        Button btn_no = dialog.findViewById(R.id.btn_no);
        Button btn_yes = dialog.findViewById(R.id.btn_yes);

        btn_no.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {

                dialog.dismiss();
            }
        });
        btn_yes.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                keyBack = 0;
                if (play_with_ai == 0) {
                    newGame2Player();
                } else {
                    newGame();
                }
                dialog.dismiss();
            }
        });
    }

    //
    private int canvasCoord2ChessIndex(PointF point) {
        Point logicPoint = new Point((int) ((point.x - mStartBoardX + mLaticeLen2) / mLaticeLen), (int) ((point.y - mStartBoardY + mLaticeLen2) / mLaticeLen));
        int index = logicPoint.x + logicPoint.y * 9;
        if (index >= AI.BOARD_SIZE || index < 0) {
            return -1;
        }
        return index;
    }

    private PointF chessIndex2CanvasCoord(int i) {
        PointF point = new PointF(chessIndex2LogicPoint(i));
        point.x *= mLaticeLen;
        point.x += mStartBoardX;
        point.y *= mLaticeLen;
        point.y += mStartBoardY;
        return point;
    }

    private Point chessIndex2LogicPoint(int i) {
        return new Point(i % 9, i / 9);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mLaticeLen < 0) {
            // load chess images
            Bitmap chessBitmap;
            Bitmap chessBitmap1 = BitmapFactory.decodeResource(getResources(), R.drawable.do_vua);
            Bitmap chessBitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.do_si);
            Bitmap chessBitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.do_tinh);
            Bitmap chessBitmap4 = BitmapFactory.decodeResource(getResources(), R.drawable.do_ma);
            Bitmap chessBitmap5 = BitmapFactory.decodeResource(getResources(), R.drawable.do_xe);
            Bitmap chessBitmap6 = BitmapFactory.decodeResource(getResources(), R.drawable.do_phao);
            Bitmap chessBitmap7 = BitmapFactory.decodeResource(getResources(), R.drawable.do_tot);
            Bitmap chessBitmap8 = BitmapFactory.decodeResource(getResources(), R.drawable.den_tuong);
            Bitmap chessBitmap9 = BitmapFactory.decodeResource(getResources(), R.drawable.den_si);
            Bitmap chessBitmap10 = BitmapFactory.decodeResource(getResources(), R.drawable.den_tinh);
            Bitmap chessBitmap11 = BitmapFactory.decodeResource(getResources(), R.drawable.den_ma);
            Bitmap chessBitmap12 = BitmapFactory.decodeResource(getResources(), R.drawable.den_xe);
            Bitmap chessBitmap13 = BitmapFactory.decodeResource(getResources(), R.drawable.den_phao);
            Bitmap chessBitmap14 = BitmapFactory.decodeResource(getResources(), R.drawable.den_tot);

            mLaticeLen = getWidth() * 35 / 320.0f;
            mChesslen = mLaticeLen * 19.0f / 20;
            mLaticeLen2 = mLaticeLen / 2.0f;
            mChessLen2 = mChesslen / 2.0f;

            mStartBoardX = getWidth() * 20.0f / 320;
            mStartBoardY = getHeight() * 20.0f / 354;

            //Hình ảnh quân cờ gán vào mảng
            mChessBitmaps[AI.LIGHT][0] = Bitmap.createBitmap(chessBitmap1, 0, 0, chessBitmap1.getWidth(), chessBitmap1.getHeight());
            mChessBitmaps[AI.LIGHT][1] = Bitmap.createBitmap(chessBitmap2, 0, 0, chessBitmap2.getWidth(), chessBitmap2.getHeight());
            mChessBitmaps[AI.LIGHT][2] = Bitmap.createBitmap(chessBitmap3, 0, 0, chessBitmap3.getWidth(), chessBitmap3.getHeight());
            mChessBitmaps[AI.LIGHT][3] = Bitmap.createBitmap(chessBitmap4, 0, 0, chessBitmap4.getWidth(), chessBitmap4.getHeight());
            mChessBitmaps[AI.LIGHT][4] = Bitmap.createBitmap(chessBitmap5, 0, 0, chessBitmap5.getWidth(), chessBitmap5.getHeight());
            mChessBitmaps[AI.LIGHT][5] = Bitmap.createBitmap(chessBitmap6, 0, 0, chessBitmap6.getWidth(), chessBitmap6.getHeight());
            mChessBitmaps[AI.LIGHT][6] = Bitmap.createBitmap(chessBitmap7, 0, 0, chessBitmap7.getWidth(), chessBitmap7.getHeight());

            mChessBitmaps[AI.DARK][0] = Bitmap.createBitmap(chessBitmap8, 0, 0, chessBitmap8.getWidth(), chessBitmap8.getHeight());
            mChessBitmaps[AI.DARK][1] = Bitmap.createBitmap(chessBitmap9, 0, 0, chessBitmap9.getWidth(), chessBitmap9.getHeight());
            mChessBitmaps[AI.DARK][2] = Bitmap.createBitmap(chessBitmap10, 0, 0, chessBitmap10.getWidth(), chessBitmap10.getHeight());
            mChessBitmaps[AI.DARK][3] = Bitmap.createBitmap(chessBitmap11, 0, 0, chessBitmap11.getWidth(), chessBitmap11.getHeight());
            mChessBitmaps[AI.DARK][4] = Bitmap.createBitmap(chessBitmap12, 0, 0, chessBitmap12.getWidth(), chessBitmap12.getHeight());
            mChessBitmaps[AI.DARK][5] = Bitmap.createBitmap(chessBitmap13, 0, 0, chessBitmap13.getWidth(), chessBitmap13.getHeight());
            mChessBitmaps[AI.DARK][6] = Bitmap.createBitmap(chessBitmap14, 0, 0, chessBitmap14.getWidth(), chessBitmap14.getHeight());


            chessBitmap = mChessBitmaps[0][0];
            mChessBitmaps[0][0] = mChessBitmaps[0][6];
            mChessBitmaps[0][6] = chessBitmap;
            chessBitmap = mChessBitmaps[1][0];
            mChessBitmaps[1][0] = mChessBitmaps[1][6];
            mChessBitmaps[1][6] = chessBitmap;
            chessBitmap = mChessBitmaps[0][4];
            mChessBitmaps[0][4] = mChessBitmaps[0][5];
            mChessBitmaps[0][5] = chessBitmap;
            chessBitmap = mChessBitmaps[1][4];
            mChessBitmaps[1][4] = mChessBitmaps[1][5];
            mChessBitmaps[1][5] = chessBitmap;

            // Hình ảnh chọn quân cờ
            mSelectBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.sel);
            //Bàn cờ ở backgrund của com.onezeros.chinesechess.ChessboardView trong layout Main

        }

        // draw each chess
        for (int i = 0; i < AI.BOARD_SIZE; i++) {
            if (mPieces[i] != AI.EMPTY) {
                PointF point = chessIndex2CanvasCoord(i);
                Bitmap bmp = mChessBitmaps[mColors[i]][mPieces[i]];
                canvas.drawBitmap(bmp, null, new RectF
                        (point.x - mChessLen2, point.y - mChessLen2,
                                point.x + mChessLen2, point.y + mChessLen2),
                        null);
            }
        }

        // draw selected positions
        if (mChessFrom >= 0) {
            PointF point = chessIndex2CanvasCoord(mChessFrom);
            canvas.drawBitmap(mSelectBitmap, null, new RectF(point.x - mChessLen2, point.y - mChessLen2, point.x + mChessLen2, point.y + mChessLen2), null);
        }
        if (mChessTo >= 0) {
            PointF point = chessIndex2CanvasCoord(mChessTo);
            canvas.drawBitmap(mSelectBitmap, null, new RectF(point.x - mChessLen2, point.y - mChessLen2, point.x + mChessLen2, point.y + mChessLen2), null);
        }
        super.onDraw(canvas);
    }

    public void setAILevel(int depth) {
        mAi.setSearchDepth(depth);
        LV=depth;
    }

    void newGame() {
        mAi.init();
        mChessFrom = -1;
        mChessTo = -1;
        mIsComputerThinking = false;
        play_with_ai = 1;
        key_redo=0;
        keyBack = 0;
        total_back = 0;
        can_back = 0;
        total_run = 0;
        check_back=0;


        mIconThink.setVisibility(VISIBLE);
        System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
        System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
        mInfoTextView.setText(getResources().getString(R.string.welcome));
        postInvalidate();

        final ProgressDialog progressDialog=new ProgressDialog(mContext,ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setMessage(getResources().getString(R.string.waiting));
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            public void run() {
                saveGameStatus();
                saveGameStatusTemp();
                progressDialog.dismiss();
            }
        }).start();

    }

    void newGame2Player() {
        mAi.init();
        mChessFrom = -1;
        mChessTo = -1;
        mIsComputerThinking = false;
        play_with_ai = 0;
        key_redo=0;
        keyBack = 0;
        total_back = 0;
        can_back = 0;
        check_back=0;
        total_run = 0;
        mIconThink.setVisibility(INVISIBLE);

        System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
        System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
        mInfoTextView.setText(getResources().getString(R.string.player1));
        postInvalidate();
        final ProgressDialog progressDialog=new ProgressDialog(mContext,ProgressDialog.THEME_HOLO_DARK);
        progressDialog.setMessage(getResources().getString(R.string.waiting));
        progressDialog.setCancelable(false);
        progressDialog.show();
        new Thread(new Runnable() {
            @Override
            public void run() {
                saveGameStatus();
                progressDialog.dismiss();
            }
        }).start();


    }

    void saveGameStatus() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatusTemp() {
        //
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME_TEMP, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatusTempBlack() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME_TEMP1, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatusBlack() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME1, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatus_Redo() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME_REDO, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatus_Redo_2P_RED() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME_REDO_2P_RED, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);


            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    void saveGameStatus_Redo_2P_BLACK() {
        try {
            FileOutputStream fos = mContext.openFileOutput(SAVE_STATE_FILE_NAME_REDO_2P_BLACK, Context.MODE_PRIVATE);
            DataOutputStream dos = new DataOutputStream(fos);

            dos.writeInt(mChessFrom);
            dos.writeInt(mChessTo);

            mAi.saveStatus(dos);

            dos.close();
            fos.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    void restoreGameStatus() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGameStatusTemp() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME_TEMP);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGameStatusBlack() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME1);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGameStatusTempBlack() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME_TEMP1);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGame_Redo() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME_REDO);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGame_Redo_2P_RED() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME_REDO_2P_RED);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    void restoreGame_Redo_2P_BLACK() {
        try {
            FileInputStream fis = mContext.openFileInput(SAVE_STATE_FILE_NAME_REDO_2P_BLACK);
            DataInputStream dis = new DataInputStream(fis);

            mChessFrom = dis.readInt();
            mChessTo = dis.readInt();

            mAi.restoreStatus(dis);

            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
            mIsComputerThinking = false;
        } catch (FileNotFoundException e) {
            newGame();
            e.printStackTrace();
        } catch (IOException e) {
            newGame();
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (play_with_ai == 1) {
            if (!mIsComputerThinking && event.getAction() == MotionEvent.ACTION_DOWN) {
                final int chess = canvasCoord2ChessIndex(new PointF(event.getX(), event.getY()));
//                Log.e("Hung ", "chess index clicked : " + chess);
                if (chess >= 0) {
                    if (mChessFrom >= 0 && mChessTo >= 0 || mChessFrom < 0) {
                        if (mAi.color[chess] == USER_COLOR) {
                            mChessFrom = chess; //Diem cu nguoi dc chon
                            mChessTo = -1;
                            invalidate();

                            new Thread(new Runnable() {
                                public void run() {
                                    if (keyBack == 0) {
                                        saveGameStatusTemp();
                                        keyBack = 1;
                                    } else {
                                        if (keyBack == 1) {
                                            saveGameStatus();
                                        } else {
                                            if (keyBack == 2) {
                                                saveGameStatusTemp();
                                            }
                                        }
                                    }
                                }
                            }).start();

                        }
                    } else if (mChessFrom >= 0 && mChessTo < 0 && mAi.color[chess] == USER_COLOR) {
                        mChessFrom = chess;
                        invalidate();
                        new Thread(new Runnable() {
                            public void run() {
                                if (keyBack == 0) {
                                    saveGameStatusTemp();
                                    keyBack = 1;
                                } else {
                                    if (keyBack == 1) {
                                        saveGameStatus();
                                    } else {
                                        if (keyBack == 2) {
                                            saveGameStatusTemp();
                                        }
                                    }
                                }
                            }
                        }).start();


                    } else if (mChessTo < 0) {
                        final int ret = mAi.takeAMove(mChessFrom, chess);
                        if (ret == AI.MOVE_OK) {
                            mChessTo = chess;

                            System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
                            System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
                            invalidate();

                            if (keyBack == 2) {
                                keyBack = 1;
                            } else {
                                keyBack = 2;
                            }
                            total_back = 1;
                            mIconThink.setVisibility(VISIBLE);
                            anim_thinking=AnimationUtils.loadAnimation(mContext,R.anim.thinking);
                            mIconThink.startAnimation(anim_thinking);

                            mIsComputerThinking = true;
                            key_redo=0;
                            mInfoTextView.setText(getResources().getString(R.string.computer_move));
                            new Thread(new Runnable() {
                                public void run() {
                                    // computer move
                                    try {
                                        Random random = new Random();
                                        int time_sleep = random.nextInt(TIME_WAIT_COMPUTER) + 500;
                                        Thread.sleep(time_sleep);
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                    int ret = mAi.computerMove();
                                    Move move = mAi.getComputerMove();
                                    mChessFrom = move.from;
                                    mChessTo = move.dest;
                                    System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
                                    System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
                                    postInvalidate();
                                    mIsComputerThinking = false;
                                    Message msg = mMessageHandler.obtainMessage(MSG_COMPUTER_MOVE_DONE);
                                    mMessageHandler.sendMessage(msg);

                                    if (ret == AI.MOVE_WIN) {
                                        msg = mMessageHandler.obtainMessage(MSG_COMPUTER_WIN);
                                        mMessageHandler.sendMessage(msg);
                                    }
                                    if (mIsComputerThinking == false) {
                                        saveGameStatus_Redo();
                                    }
                                }
                            }).start();

                        } else if (ret == AI.MOVE_WIN) {
                            Message msg = mMessageHandler.obtainMessage(MSG_USER_WIN);
                            mMessageHandler.sendMessage(msg);
                        }
                    }
                }
            }
        } else {
            if (total_run % 2 == 0) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final int chess = canvasCoord2ChessIndex(new PointF(event.getX(), event.getY()));
                    if (chess >= 0) {
                        if (mChessFrom >= 0 && mChessTo >= 0 || mChessFrom < 0) {
                            if (mAi.color[chess] == USER_COLOR) {
                                mChessFrom = chess;
                                mChessTo = -1;
                                invalidate();
                                new Thread(new Runnable() {
                                    public void run() {
                                        if (total_run % 4 == 0) {
                                            saveGameStatus();
                                        }
                                        if (total_run % 4 == 2) {
                                            saveGameStatusTemp();
                                        }
                                    }
                                }).start();
                                if (can_back == 4) {
                                    can_back = 3;
                                    Log.e("Canback fix"," "+can_back);
                                }

                            }
                        } else if (mChessFrom >= 0 && mChessTo < 0 && mAi.color[chess] == USER_COLOR) {
                            mChessFrom = chess;
                            invalidate();
                            new Thread(new Runnable() {
                                public void run() {
                                    if (total_run % 4 == 0) {
                                        saveGameStatus();
                                    }
                                    if (total_run % 4 == 2) {
                                        saveGameStatusTemp();
                                    }
                                }
                            }).start();
                            if (can_back == 4) {
                                can_back = 3;
                                Log.e("Canback fix"," "+can_back);
                            }

                        } else if (mChessTo < 0) {
                            final int ret = mAi.takeAMove(mChessFrom, chess);
                            if (ret == AI.MOVE_OK) {
                                mChessTo = chess;

                                System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
                                System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
                                invalidate();
                                mInfoTextView.setText(getResources().getString(R.string.player2));


                                total_run++;
                                key_redo=0;
                                Log.e("Totol_run fix 1:"," "+total_run);
                                if (total_run <= 4) {
                                    can_back = total_run;
                                } else {
                                    can_back = 4;
                                }

                                new Thread(new Runnable() {
                                    public void run() {
                                        saveGameStatus_Redo_2P_RED();
                                    }
                                }).start();
                            } else if (ret == AI.MOVE_WIN) {
                                Message msg = mMessageHandler.obtainMessage(MSG_USER_WIN_1);
                                mMessageHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            } else {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    final int chess = canvasCoord2ChessIndex(new PointF(event.getX(), event.getY()));
                    if (chess >= 0) {
                        if (mChessFrom >= 0 && mChessTo >= 0 || mChessFrom < 0) {
                            if (mAi.color[chess] == USER2_COLOR) {
                                mChessFrom = chess;
                                mChessTo = -1;
                                invalidate();
                                new Thread(new Runnable() {
                                    public void run() {
                                        if (total_run % 4 == 1) {
                                            saveGameStatusBlack();
                                        }
                                        if (total_run % 4 == 3) {
                                            saveGameStatusTempBlack();
                                        }
                                    }
                                }).start();
                                if (can_back == 4) {
                                    can_back = 3;
                                    Log.e("Canback fix"," "+can_back);
                                }

                            }
                        } else if (mChessFrom >= 0 && mChessTo < 0 && mAi.color[chess] == USER2_COLOR) {
                            mChessFrom = chess;
                            invalidate();
                            new Thread(new Runnable() {
                                public void run() {
                                    if (total_run % 4 == 1) {
                                        saveGameStatusBlack();
                                    }
                                    if (total_run % 4 == 3) {
                                        saveGameStatusTempBlack();
                                    }
                                }
                            }).start();
                            if (can_back == 4) {
                                can_back = 3;
                                Log.e("Canback fix"," "+can_back);
                            }


                        } else if (mChessTo < 0) {
                            final int ret = mAi.takeAMove(mChessFrom, chess);
                            if (ret == AI.MOVE_OK) {
                                mChessTo = chess;
                                System.arraycopy(mAi.piece, 0, mPieces, 0, mPieces.length);
                                System.arraycopy(mAi.color, 0, mColors, 0, mColors.length);
                                invalidate();


                                total_run++;
                                key_redo=0;
                                Log.e("Totol_run fix 2:"," "+total_run);
                                if (total_run <= 4) {
                                    can_back = total_run;
                                } else {
                                    can_back = 4;
                                }
                                new Thread(new Runnable() {
                                    public void run() {
                                        saveGameStatus_Redo_2P_BLACK();
                                    }
                                }).start();
                                mInfoTextView.setText(getResources().getString(R.string.player1));

                            } else if (ret == AI.MOVE_WIN) {
                                Message msg = mMessageHandler.obtainMessage(MSG_USER_WIN_2);
                                mMessageHandler.sendMessage(msg);
                            }
                        }
                    }
                }
            }
        }
        return super.onTouchEvent(event);
    }

    public void undo_back() {
        if (play_with_ai == 1) {
            if (mIsComputerThinking == false && keyBack == 1) {
                invalidate();
                postInvalidate();
                restoreGameStatusTemp();
                invalidate();
                key_redo=1;
                --total_back;
                Log.e("Key Back undo", keyBack + "");
            } else {
                if (mIsComputerThinking == false && keyBack == 2) {
                    invalidate();
                    postInvalidate();
                    restoreGameStatus();
                    invalidate();
                    key_redo=1;
                    --total_back;
                    Log.e("Key Back undo", keyBack + "");
                }
            }
        }
        if (can_back > 0) {
            if (play_with_ai == 0 && total_run > 0) {
                if (total_run % 4 == 1) {
                    invalidate();
                    postInvalidate();
                    restoreGameStatus();
                    invalidate();
                    --total_run;
                    can_back--;
                    key_redo++;
                    if (key_redo > 4) {
                        key_redo = 4;
                    }
                    check_back=1;
                    mInfoTextView.setText(getResources().getString(R.string.player1));
                    Log.e("Back vo 1","Canback="+can_back+"-key_redo:"+key_redo+"-total_run: "+total_run);
                    return;
                }
                if (total_run % 4 == 2) {
                    invalidate();
                    postInvalidate();
                    restoreGameStatusBlack();
                    invalidate();

                    --total_run;
                    can_back--;
                    key_redo++;
                    if (key_redo > 4) {
                        key_redo = 4;
                    }
                    check_back=1;
                    mInfoTextView.setText(getResources().getString(R.string.player2));
                    Log.e("Back vo 2","Canback="+can_back+"-key_redo:"+key_redo+"-total_run: "+total_run);
                    return;
                }
                if (total_run % 4 == 3) {
                    invalidate();
                    postInvalidate();
                    restoreGameStatusTemp();
                    invalidate();

                    --total_run;
                    can_back--;
                    key_redo++;
                    if (key_redo > 4) {
                        key_redo = 4;
                    }
                    check_back=1;
                    Log.e("Back vo 3","Canback="+can_back+"-key_redo:"+key_redo+"-total_run: "+total_run);
                    mInfoTextView.setText(getResources().getString(R.string.player1));
                    return;
                }
                if (total_run % 4 == 0) {
                    invalidate();
                    postInvalidate();
                    restoreGameStatusTempBlack();
                    invalidate();

                    --total_run;
                    can_back--;
                    key_redo++;
                    if (key_redo > 4) {
                        key_redo = 4;
                    }
                    check_back=1;
                    mInfoTextView.setText(getResources().getString(R.string.player2));
                    Log.e("Back vo 4","Canback="+can_back+"-key_redo:"+key_redo+"-total_run: "+total_run);
                    return;
                }
            }
        }
    }

    public void redo_ai() {
        if(key_redo>0&&!mIsComputerThinking){
            invalidate();
            postInvalidate();
            restoreGame_Redo();
            invalidate();
            key_redo--;
            total_back++;
        }
    }

    public void redo_player() {
        //
        if (play_with_ai == 0 && key_redo > 0) {
            if (total_run % 4 == 1 && key_redo > 1) {
                invalidate();
                postInvalidate();
                restoreGameStatusTemp();
                invalidate();
                key_redo--;
                total_run++;
                can_back++;
                Log.e("Chay vao 1", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                mInfoTextView.setText(getResources().getString(R.string.player1));
                return;
            }

            if (total_run % 4 == 2 && key_redo > 1) {
                invalidate();
                postInvalidate();
                restoreGameStatusTempBlack();
                invalidate();
                key_redo--;
                total_run++;
                can_back++;
                Log.e("Chay vao 2", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                mInfoTextView.setText(getResources().getString(R.string.player2));
                return;
            }
            if (total_run % 4 == 3 && key_redo > 1) {
                invalidate();
                postInvalidate();
                restoreGameStatus();
                invalidate();
                key_redo--;
                total_run++;
                can_back++;
                Log.e("Chay vao 3", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                mInfoTextView.setText(getResources().getString(R.string.player1));
                return;
            }
            if (total_run % 4 == 0 && key_redo > 1) {
                invalidate();
                postInvalidate();
                restoreGameStatusBlack();
                invalidate();
                total_run++;
                can_back++;
                key_redo--;
                Log.e("Chay vao 4", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                mInfoTextView.setText(getResources().getString(R.string.player2));
                return;
            }

            if (key_redo == 1) {
                if (total_run % 2 == 0) {
                    invalidate();
                    postInvalidate();
                    restoreGame_Redo_2P_RED();
                    invalidate();
                    total_run++;
                    can_back++;
                    key_redo--;
                    Log.e("Chay vao 5", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                    mInfoTextView.setText(getResources().getString(R.string.player2));
                    return;
                } else {
                    invalidate();
                    postInvalidate();
                    restoreGame_Redo_2P_BLACK();
                    invalidate();
                    total_run++;
                    can_back++;
                    key_redo--;
                    Log.e("Chay vao 6", "total_run=" + total_run + "-can_back=" + can_back + "-key_redo=" + key_redo);
                    mInfoTextView.setText(getResources().getString(R.string.player1));
                    return;
                }
            }
        }else {
            Toast.makeText(mContext, getResources().getString(R.string.you_cant_redo), Toast.LENGTH_SHORT).show();
        }
    }


    class MessageHandler extends Handler {
        @TargetApi(Build.VERSION_CODES.FROYO)
        public void handleMessage(Message msg) {
            Log.d("lzj", "message hander : msg.what = " + msg.what);
            switch (msg.what) {
                case MSG_USER_MOVE_DONE:
                    break;
                case MSG_COMPUTER_MOVE_DONE:
                    invalidate();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                        anim_thinking.cancel();
                    }
                    mInfoTextView.setText(getResources().getString(R.string.user_move));
                    break;
                case MSG_COMPUTER_WIN:
                    keyBack = 0;
                    title.setText(getResources().getString(R.string.computer_win));
                    dbManager= new DBManager(mContext);
                    ChessResult c1= new ChessResult();

                    cal = Calendar.getInstance();
                    c1.setTime(dateFormat.format(cal.getTime()));
                    c1.setLevel(LV);
                    c1.setResult(0);
                    dbManager.Add_RESULT(c1);
                    dialog.show();
                    break;
                case MSG_USER_WIN:
                    keyBack = 0;

                    title.setText(getResources().getString(R.string.user_win));
                    dbManager= new DBManager(mContext);
                    cal = Calendar.getInstance();
                    ChessResult c2= new ChessResult();
                    c2.setTime(dateFormat.format(cal.getTime()));
                    c2.setLevel(LV);
                    c2.setResult(1);
                    dbManager.Add_RESULT(c2);
                    dialog.show();
                    break;
                case MSG_USER_WIN_1:
                    keyBack = 0;
                    title.setText(getResources().getString(R.string.player1_win));
                    dialog.show();
                    break;
                case MSG_USER_WIN_2:
                    keyBack = 0;
                    title.setText(getResources().getString(R.string.player2_win));
                    dialog.show();
                    break;
                default:
                    break;
            }
        }
    }
}
