package om.sas.training;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.IOException;

import static androidx.core.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int REQUEST_STORAGE_PERMISSION = 200;
    private static final int REQUEST_CAMERA_PERMISSION = 201;

    private static final int REQUEST_IMAGE_CAPTURE = 101;
    private static final int REQUEST_GALLERY_IMAGE = 102;
    private Context mContext;

    public static String fileName;

    private ImageView ivImageOriginal;
    private ImageView ivResizedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ivImageOriginal = findViewById(R.id.iv_image_original);
        ivResizedImage = findViewById(R.id.iv_image_updated);

        mContext = getApplicationContext();
        requestForCameraAndStoragePermission();
    }


    void requestForCameraAndStoragePermission() {

        if(PackageManager.PERMISSION_GRANTED !=
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_STORAGE_PERMISSION);
            }
        }


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);


        }

    }

    public void getAnImageFromCameraOrGallery(View view) {
        showImagePickerOptionsDialogue();
    }

    private void showImagePickerOptionsDialogue() {
        //TODO - Can add Alter Dialogue to choose the Camera or Gallery Option


        // getImageFromCamera();
        getAnImageFromGallery();
    }

    private void getAnImageFromGallery() {

        Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto, REQUEST_GALLERY_IMAGE);
    }

    private void getImageFromCamera() {

        fileName = System.currentTimeMillis() + ".jpg";
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, getCacheImagePath(fileName));
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private Uri getCacheImagePath(String fileName) {
        File path = new File(getExternalCacheDir(), "camera");
        if (!path.exists()) path.mkdirs();
        File image = new File(path, fileName);
        return getUriForFile(MainActivity.this, getPackageName() + ".provider", image);

    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri imageUri = null;
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE:
                imageUri = getCacheImagePath(fileName);
                break;
            case REQUEST_GALLERY_IMAGE:
                 imageUri = data.getData();
                break;
        }

        Bitmap bitmap = null;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Glide.with(this).load(imageUri.toString())
                    .into(ivImageOriginal);
        getResizedBitmap(bitmap , (bitmap.getWidth()/2), (bitmap.getHeight()/2));
    }

    public void getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;

        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        ivResizedImage.setImageBitmap(resizedBitmap);

        //return resizedBitmap;
    }
}
