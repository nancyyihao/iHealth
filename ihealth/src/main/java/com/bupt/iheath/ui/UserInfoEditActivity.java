package com.bupt.iheath.ui;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bupt.iheath.MyApplication;
import com.bupt.iheath.R;
import com.bupt.iheath.model.AccountInfo;
import com.bupt.iheath.ui.base.BaseActivity;
import com.bupt.iheath.utils.Constants;
import com.bupt.iheath.utils.NotifyUtils;
import com.bupt.iheath.widgets.CircleImageView;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rengwuxian.materialedittext.validation.METValidator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.listener.UpdateListener;
import cn.bmob.v3.listener.UploadFileListener;

public class UserInfoEditActivity extends BaseActivity {

    private String[] items = new String[] { "选择本地图片", "拍照" };
    /* 头像名称 */
    private static final String IMAGE_FILE_NAME = "faceImage.png";
    /* 请求码 */
    private static final int IMAGE_REQUEST_CODE = 0;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int RESULT_REQUEST_CODE = 2;

    private CircleImageView mImageHead ;
    private MaterialEditText mSexMET ;
    private MaterialEditText mAgeMET ;
    private MaterialEditText mSignatureMET ;
    private MaterialEditText mNicknameMET ;

    private String imagePath ;
    private Bitmap mBitmap ;
    private MaterialEditText mWeightMET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_edit);
        setupActionBar();

        mImageHead = (CircleImageView) findViewById(R.id.img_head);
        mImageHead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog();
            }
        });
        if (hasSdcard()) {
            imagePath = Environment.getExternalStorageDirectory()+File.separator
                    + IMAGE_FILE_NAME ;
        } else {
            imagePath = IMAGE_FILE_NAME ;
        }
        mSexMET = (MaterialEditText) findViewById(R.id.sex) ;
        mAgeMET = (MaterialEditText) findViewById(R.id.age);
        mSignatureMET = (MaterialEditText) findViewById(R.id.signature);
        mNicknameMET = (MaterialEditText) findViewById(R.id.nickname);
        mWeightMET = (MaterialEditText) findViewById(R.id.weight);

        AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser() ;
        BmobFile image = (BmobFile) getIntent().getSerializableExtra("photo");
        int age ;
        try {
            age = accountInfo.getAge() ;
        }catch (Exception e) {
            e.printStackTrace();
            age = 0 ;
        }
        String nickname = accountInfo.getNickName() ;
        String sign = accountInfo.getSignature() ;
        String sex = accountInfo.getSex() ;
        String weight = String.valueOf(accountInfo.getWeight()) ;
        setText(mAgeMET, String.valueOf(age));
        setText(mSexMET, sex);
        setText(mSignatureMET, sign);
        setText(mNicknameMET, nickname);
        setText(mWeightMET, weight);
        if (image != null) {
            image.loadImage(this, mImageHead);
        }

        mSexMET.addValidator(new METValidator("性别只能填男或女") {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
                if (text.equals("男") || text.equals("女")) {
                    return true;
                } else {
                    return false ;
                }
            }
        }) ;
        mAgeMET.addValidator(new METValidator("年龄只能是在1~150之间的整数") {
            @Override
            public boolean isValid(@NonNull CharSequence text, boolean isEmpty) {
                int age = 0 ;
                try {
                    age = Integer.parseInt((String) text) ;
                } catch (Exception e) {
                    e.printStackTrace();
                    age = 0 ;
                }

                if (1<=age && age<=150) {
                    return true ;
                } else {
                    return false;
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_user_info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true ;
        } else
        if (id == R.id.action_save) {
            // TODO 保存
            saveInfo();
            //NotifyUtils.showHints("路径名："+imagePath);
        }
        return super.onOptionsItemSelected(item);
    }

    private void setText(EditText et , String text) {
        if (!TextUtils.isEmpty(text)) {
            et.setText(text);
        }
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 显示返回箭头
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    private void saveInfo() {

        int age = Integer.parseInt(mAgeMET.getText().toString()) ;
        String sex = mSexMET.getText().toString() ;
        String nickname = mNicknameMET.getText().toString() ;
        String sign = mSignatureMET.getText().toString() ;
        String weight = mWeightMET.getText().toString() ;
//        if (!mSexMET.validate() || !mAgeMET.validate()) {
//            return;
//        }

        final AccountInfo accountInfo = MyApplication.getInstance().getCurrentUser() ;
        accountInfo.setSex(sex);
        accountInfo.setSignature(sign);
        accountInfo.setNickName(nickname);
        accountInfo.setAge(age);

        final AccountInfo newInfo = new AccountInfo() ;
        newInfo.setAge(age);
        newInfo.setNickName(nickname);
        newInfo.setSignature(sign);
        newInfo.setSex(sex);
        newInfo.setWeight(Float.parseFloat(weight));

        final ProgressDialog pd = ProgressDialog.show(this, "", "保存中，请稍候......");

        if (mBitmap != null) {
            saveBitmap(imagePath, mBitmap);
            final BmobFile image = new BmobFile(new File(imagePath));
            image.upload(UserInfoEditActivity.this, new UploadFileListener() {
                @Override
                public void onSuccess() {
                    newInfo.setImage(image);
                    accountInfo.setImage(image);
                    newInfo.update(UserInfoEditActivity.this, accountInfo.getObjectId(), new UpdateListener() {
                        @Override
                        public void onSuccess() {
                            NotifyUtils.showHints("上传成功！！！");
                            setResult(RESULT_OK);
                            finish();
                            pd.dismiss();
                        }

                        @Override
                        public void onFailure(int i, String s) {
                            NotifyUtils.showHints("上传失败，请重新尝试!"+i+s);
                            pd.dismiss();
                        }
                    });
                }

                @Override
                public void onFailure(int i, String s) {
                    NotifyUtils.showHints("上传失败，请重新尝试!"+i+s);
                    pd.dismiss();
                }
            });
        } else {
            newInfo.update(this, accountInfo.getObjectId(), new UpdateListener() {
                @Override
                public void onSuccess() {
                    NotifyUtils.showHints("上传成功！！！");
                    setResult(RESULT_OK);
                    finish();
                    pd.dismiss();
                }

                @Override
                public void onFailure(int i, String s) {
                    NotifyUtils.showHints("上传失败，请重新尝试!"+i+s);
                    pd.dismiss();
                }
            });
        }
    }


    private void showDialog() {

        new AlertDialog.Builder(this)
                .setItems(items, new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                startActivityForResult(intentFromGallery,
                                        IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (hasSdcard()) {
                                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                                    File file = new File(path,IMAGE_FILE_NAME);
                                    intentFromCapture.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(file));
                                }

                                startActivityForResult(intentFromCapture,CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //结果码不等于取消时候
        if (resultCode != RESULT_CANCELED) {

            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (hasSdcard()) {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File tempFile = new File(path,IMAGE_FILE_NAME);
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        Toast.makeText(this, "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CODE: //图片缩放完成后
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 200);
        intent.putExtra("outputY", 200);
        intent.putExtra("return-data", true);
        startActivityForResult(intent, RESULT_REQUEST_CODE);
    }

    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            mBitmap = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(getResources(),mBitmap);
            mImageHead.setImageDrawable(drawable);
        }
    }

    private boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }

    private void saveBitmap(String filePath, Bitmap mBitmap) {
        File f = new File(filePath);
        FileOutputStream fOut = null;
        try {
            //f.createNewFile();
            fOut = new FileOutputStream(f);
            mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fOut != null) {
                    fOut.close();
                }
                //Toast.makeText(this, "save success", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
