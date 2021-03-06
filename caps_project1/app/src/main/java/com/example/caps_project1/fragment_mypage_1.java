package com.example.caps_project1;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.example.caps_project1.database.UserData;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.jsoup.parser.Parser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class fragment_mypage_1 extends Fragment {

    // auth, database, storage
    private FirebaseAuth mAuth;
    private FirebaseDatabase mDatabase;
    private DatabaseReference mDBReference;
    private FirebaseUser user;

    private static final int REQUEST_CODE = 101;
    private static final int PERMISSON_CAMERA = 1111;
    private static final int CAPTURE_IMAGE = 2222;
    private static final int PICK_IMAGE = 3333;

    private static final int CROP_IMAGE = 4444;

    String mCurrentPhotoPath;
    Uri imageURI;
    Uri photoURI, albumURI;

    private int id_view;
    private ImageView iv_profile;
    private TextView tv_userName, tv_userEmail;

    private Context mContext;


    // popup ?????? ?????? ??????
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == 1) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }



    // onCreate : fragment??? ????????? ??? ???????????? ??????
    // onCreateView : onCreate ?????? ????????? ????????? ??? ??????
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState  ) {
        View view = inflater.inflate(R.layout.fragment_mypage_1, container, false);

        // DatabaseReference ????????? ?????????????????? ???????????? ????????? ??? ????????????.
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRootRef = FirebaseDatabase.getInstance().getReference();



        mDBReference = myRootRef.child("users").child("uid");
        user = FirebaseAuth.getInstance().getCurrentUser();


        iv_profile = view.findViewById(R.id.iv_profile);
        tv_userName = view.findViewById(R.id.userName);
//        tv_userEmail = view.findViewById(R.id.userEmail);


        if (user != null) {
            for (UserInfo profile : user.getProviderData()) {
                String name = profile.getDisplayName();

                tv_userName.setText(name);

            }
        }
//
//        mDBReference.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String userName = dataSnapshot.getValue(String.class);
//                tv_userName.setText(userName);
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError databaseError) {
//                // failed
//                Log.w(this.getClass().getSimpleName(), databaseError.toException());
//            }
//        });


        // ???????????? ?????? ??? ??????????????? ?????? ?????????.
//        iv_profile.setOnClickListener(new View.OnClickListener() {
//
//            // popup menu ??????
//            // inflater ??? ?????? popup menu??? ??????, ?????? ????????? ?????????????????? ????????? ?????? ?????? ????????????.
//            // ?????????, ????????? ?????? ???????????? ?????? ?????? ?????????
//            @Override
//            public void onClick(View v) {
//
//                PopupMenu pop = new PopupMenu(mContext, v);
//                pop.getMenuInflater().inflate(R.menu.menu_popup, pop.getMenu());
//
//                pop.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//                    @Override
//                    public boolean onMenuItemClick(MenuItem item) {
//                        switch (item.getItemId()) {
//                            // ?????????
//                            case R.id.one:
//                                captureCamera();
//                                break;
//
//                            // ??????????????? ?????? ????????????
//                            case R.id.two:
//                                getAlbum();
//                                break;
//
//                            // ?????? ???????????? ??????
//                            case R.id.three:
//                                iv_profile.setImageResource(R.drawable.user2);
//                        }
//                        return true;
//                    }
//                });
//                pop.show();
//                checkPermission();
//            }
//        });

        // ????????? ?????? ?????? ?????????
        iv_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPermission();

                photoDialogRadio();

//                startActivityForResult(intent, REQUEST_CODE);
            }
        });


        // ???????????? ??????
        Button updateProfileButton = view.findViewById(R.id.changeProfile);
        updateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startMemberInitActivity();

            }
        });

        // ????????????
        Button logoutButton = view.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogoutDialog();
            }
        });

        // ?????? ??????
        Button revokeButton = view.findViewById(R.id.revokeButton);
        revokeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RevokeDialog();
            }
        });

        return view;
    }


    // ?????? ?????? ?????????
    private void revokeAccess() {
        mAuth.getCurrentUser().delete();
    }


    // ???????????? ???????????????
    void LogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage("???????????? ???????????????????").setPositiveButton("??????",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(mContext, "???????????? ???????????????.", Toast.LENGTH_SHORT).show();

                        FirebaseAuth.getInstance().signOut();
                        startLoginActivity();

                    }
                }).setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }


    // ?????? ?????? ???????????????
    void RevokeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("?????? ??????").setMessage("?????? ???????????????????").setPositiveButton("???",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        Toast.makeText(mContext, "?????? ???????????????.", Toast.LENGTH_SHORT).show();

                        revokeAccess();

                        // ?????? ?????? ?????? ??????????????? ???????????????.
                        getActivity().finishAffinity();
                    }
                }).setNegativeButton("?????????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(mContext, "?????? ???????????????.", Toast.LENGTH_SHORT).show();
            }
        });
        builder.show();
    }

    // ???????????? or ?????? ?????? ???????????????
    void photoDialogRadio() {
        final CharSequence[] PhotoModels = {"GALLERY", "CAMERA"};
        AlertDialog.Builder alt = new AlertDialog.Builder(mContext);

        alt.setTitle("????????? ?????? ????????????");
        alt.setSingleChoiceItems(PhotoModels, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                Toast.makeText(mContext, PhotoModels[item] + " ?????? ???????????????.", Toast.LENGTH_SHORT).show();
                if (item == 0) {
                    // ?????????
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(intent, PICK_IMAGE);
                    dialog.dismiss();
                } else {
                    // ?????? ????????????
                    Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent, CAPTURE_IMAGE);
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alert = alt.create();
        alert.show();
    }


    // ActivityCompat.checkSelfPermission : ????????? ??? ?????? ????????? ????????? ????????? ??????
    private void checkPermission() {
        String temp = "";

        // ?????? ?????? ??????
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.READ_EXTERNAL_STORAGE + " ";
        }

        // ?????? ?????? ??????
        if (ContextCompat.checkSelfPermission(mContext,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            temp += Manifest.permission.WRITE_EXTERNAL_STORAGE + " ";
        }

        if (TextUtils.isEmpty(temp) == false) {
            // ?????? ??????
            ActivityCompat.requestPermissions(getActivity(), temp.trim().split(" "), 1);
        } else {
            // ?????? ??????
            Toast.makeText(mContext, "????????? ?????? ??????", Toast.LENGTH_SHORT).show();
        }


        // ?????? : PackageManager.PERMISSION_DENIED ??? ????????????.
//        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) !=
//                PackageManager.PERMISSION_GRANTED) {
//
//            if ((ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) ||
//                    (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA))) {
//                new AlertDialog.Builder(mContext).setTitle("??????").setMessage("????????? ????????? ?????????????????????.").setNeutralButton("??????", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int i) {
//                        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                        intent.setData(Uri.parse("package: " + getActivity().getPackageName()));
//                        startActivity(intent);
//                    }
//                }).setPositiveButton("??????", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int i) {
//                        getActivity().finish();
//                    }
//                }).setCancelable(false).create().show();
//            } else {
//                // ????????? ?????? , ??????????????? onRequestPermissionResults ?????? ??????
//                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, PERMISSON_CAMERA);
//            }
//        }

//        int permission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
//        if (permission == PackageManager.PERMISSION_DENIED) {
//            ActivityCompat.requestPermissions(getActivity(), new String[] {Manifest.permission.CAMERA}, 0);
//        }
//        else {
//            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(intent, 1);
//        }
    }


    // ActivityCompat.requestPermissions ??? ????????? ????????? ????????? ????????? ???????????? ?????????
    // ????????? ?????? ????????? ?????? ??? ???????????? ??????
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(this.getClass().getSimpleName(), "onRequestPermissionResult");

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
        grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Log.d(this.getClass().getSimpleName(), "Permission: " + permissions[0] + "was " + grantResults[0]);
        }
    }

//        if (requestCode == 0) {
//            if (grantResults[0] == 0) {
//                Toast.makeText(mContext, "????????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(mContext, "????????? ?????? ?????? ??????", Toast.LENGTH_SHORT).show();
//
//            }



    // ????????? ?????? ????????? ????????? ??????????????? ??????
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK && data.getData() != null) {
            try {
                // ??????????????? ???????????? ???????????? <????????????: url> ??? ????????? == data.getData()
                // String path ??? ??????????????? public String getPath(Uri uri){
                //  String[] proj = {MediaStore.Images.Media.DATA};
                //  CursorLoader cursorLoader = new CursorLoader(this, uri, proj, null, null, null);
                // Cursor cursor = cursorLoader.loadInBackground(); int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                // cursor.moveToFirst(); return cursor.getString(index);
                // }
                Bitmap img = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(),
                        data.getData());
//                InputStream in = getActivity().getContentResolver().openInputStream(data.getData());
//                Bitmap img = BitmapFactory.decodeStream(in);
//                in.close();

                // ???????????????
                iv_profile.setImageBitmap(img);
//                imageURI = Uri.parse(data.getData() + "");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAPTURE_IMAGE && resultCode == Activity.RESULT_OK && data.hasExtra("data")) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                iv_profile.setImageBitmap(bitmap);

            }
        }
    }
//        switch (requestCode) {
//            case TAKE_PHOTO:
//                if (requestCode == Activity.RESULT_OK) {
//                    try {
//                        Log.i("REQUEST_TAKE_PHOTO", "OK");
//                        galleryAddPic();
//
//                        iv_profile.setImageURI(imageURI);
//                    } catch (Exception e) {
//                        Log.e("REQUEST_TAKE_PHOTO", e.toString());
//                    }
//                } else {
//                    Toast.makeText(mContext, "??????????????? ????????? ??? ????????????.", Toast.LENGTH_SHORT).show();
//                }
//                break;
//
//            case TAKE_ALBUM:
//                if (requestCode == Activity.RESULT_OK) {
//                    if (data.getData() != null) {
//                        try {
//                            File albumFile = null;
//                            albumFile = createImageFile();
//                            photoURI = data.getData();
//                            albumURI = Uri.fromFile(albumFile);
//                            cropImage();
//
//                        } catch (IOException e) {
//                            Log.e("TAKE_ALBUM_SINGLE_ERROR", e.toString());
//                        }
//                    }
//                }
//                break;
//
//            case CROP_IMAGE:
//                if (requestCode == Activity.RESULT_OK) {
//                    galleryAddPic();
//
//                    // ?????? ?????? ??????
//                    iv_profile.setImageURI(albumURI);
//                }
//                break;
//        }
//    }

    // ???????????? ????????? ???????????? ????????? ??? ??????.
//    private void getAlbum() {
//        Intent intent = new Intent(Intent.ACTION_PICK);
//
//        intent.setType("image/*");
//        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
//
//        startActivityForResult(intent, TAKE_ALBUM);
//    }


    // ?????? ?????? ??????
//    private void captureCamera() {
//        String state = Environment.getExternalStorageState();
//        if (Environment.MEDIA_MOUNTED.equals(state)) {
//            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//
//            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
//                File photoFile = null;
//                try {
//                    photoFile = createImageFile();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                if (photoFile != null) {
//                    Uri providerUri = FileProvider.getUriForFile(mContext, getActivity().getPackageName(), photoFile);
//                    imageURI = providerUri;
//
//                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, providerUri);
//                    startActivityForResult(takePictureIntent, TAKE_PHOTO);
//                }
//            } else {
//                Toast.makeText(mContext, "?????? ????????? ?????????", Toast.LENGTH_SHORT).show();
//                return;
//            }
//        }
//    }


    // ??????, ????????? ????????? ?????? ????????? ?????? ??????
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + ".jpg";
        File imageFile = null;
        File storageDir = new File (Environment.getExternalStorageDirectory() + "/Pictures");

        if (!storageDir.exists()) {
            storageDir.mkdir();
        }
        imageFile = new File(storageDir, imageFileName);
        mCurrentPhotoPath = imageFile.getAbsolutePath();

        return imageFile;
    }
    private void saveFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH);
        String filename = sdf.format(new Date());

    }




    // ????????? ???????????? ??????
    // ????????? 1:1 ??? ??????, ????????? ????????? ???????????? ????????? ????????? ????????? ??? ?????? ??????.
    public void cropImage() {
        Intent cropIntent = new Intent("com.android.camera.action.CROP");
        cropIntent.setFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                .setDataAndType(photoURI, "image/*");
        cropIntent.putExtra("aspectx", 1)
                .putExtra("aspecty", 1)
                .putExtra("scale", true)
                .putExtra("output", albumURI);
        startActivityForResult(cropIntent, CROP_IMAGE);
    }


    // ???????????? ?????? ?????? ??????
    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File file = new File(mCurrentPhotoPath);
        Uri contentURI = Uri.fromFile(file);
        mediaScanIntent.setData(contentURI);

        mContext.sendBroadcast(mediaScanIntent);
        Toast.makeText(mContext, "????????? ?????????????????????", Toast.LENGTH_SHORT).show();
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }
    private void startMemberInitActivity() {
        Intent intent = new Intent(getActivity(), MemberInitActivity.class);
        startActivity(intent);
    }


    // save a jpeg
    private static class ImageUpLoader implements Runnable {

        private final Image mImage;
        ImageUpLoader(Image image) {
            mImage = image;
        }

        @Override
        public void run() {
            ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
            byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);

            // Cloud Storage ?????? ?????????
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            StorageReference mountainImageRef = storageRef.child("images/mountains.jpg");


            // ??????????????? ?????????
            UploadTask uploadTask = mountainImageRef.putBytes(bytes);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("??????", "??????");

                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Log.e("??????", "??????");
                }
            });
        }
    }

}

