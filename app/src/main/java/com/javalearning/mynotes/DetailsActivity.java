package com.javalearning.mynotes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.javalearning.mynotes.databinding.ActivityDetailsBinding;

import java.io.ByteArrayOutputStream;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    //Bir islem olunca sonucunda yapılacak diğer işlemler
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerLauncher();

        database = this.openOrCreateDatabase("NotePages",MODE_PRIVATE,null);


        //Yeni bir ekleme mi yapılacak yoksa eski eklenen mi acılacak kontrol et
        Intent intent = getIntent();
        String info = intent.getStringExtra("info");
        if(info.equals("new")){
            //new note
            binding.nameText.setText("");
            binding.dateText.setText("");
            binding.descriptionText.setText("");
            binding.imageView.setImageResource(R.drawable.selectimage);
            binding.button.setVisibility(View.VISIBLE);



        }else{
            //id göre mevcut veriyi cekip ekranda göster
            int noteId = intent.getIntExtra("noteId",0);
            binding.button.setVisibility(View.INVISIBLE);



            try {
                                                        //? yerine noteId yerleştir
                Cursor cursor = database.rawQuery("SELECT * FROM notepages WHERE id = ?",new String[]{String.valueOf(noteId)});
                int noteTitleIndex = cursor.getColumnIndex("notetitle");
                int dateTimerIndex = cursor.getColumnIndex("dateTimer");
                int descriptionIndex = cursor.getColumnIndex("description");
                int imageIndex = cursor.getColumnIndex("image");


                while(cursor.moveToNext()){
                    binding.nameText.setText(cursor.getString(noteTitleIndex));
                    binding.dateText.setText(cursor.getString(dateTimerIndex));
                    binding.descriptionText.setText(cursor.getString(descriptionIndex));

                    byte[] bytes = cursor.getBlob(imageIndex);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                    binding.imageView.setImageBitmap(bitmap);
                }
                cursor.close();
            }catch (Exception e){
                e.printStackTrace();
            }

        }
    }


    public void update(View view){

        String name = binding.nameText.getText().toString();
        String date = binding.dateText.getText().toString();
        String description = binding.descriptionText.getText().toString();


        System.out.println(name);
        try {
            String sqlString = "INSERT INTO notepages (notetitle,dateTimer,description) VALUES(?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,date);
            sqLiteStatement.bindString(3,description);


            sqLiteStatement.execute();
        }catch (Exception e){

        }


    }

    public void save(View view){
        String name = binding.nameText.getText().toString();
        String date = binding.dateText.getText().toString();
        String description = binding.descriptionText.getText().toString();


        Bitmap smallImage = makeSmallerImage(selectedImage,400);
        //Resimi veriye cevir
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
        byte[] byteArray = outputStream.toByteArray();

        //Database oluştur
        try {


            database.execSQL("CREATE TABLE IF NOT EXISTS notepages (id INTEGER PRIMARY KEY, notetitle VARCHAR,dateTimer VARCHAR, description VARCHAR,image BLOB)");
            String sqlString = "INSERT INTO notepages (notetitle,dateTimer,description,image) VALUES(?,?,?,?)";
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,date);
            sqLiteStatement.bindString(3,description);
            sqLiteStatement.bindBlob(4,byteArray);

            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        //islem bitince tüm sayfaları kapat ve main sayfasına dön
        Intent intent = new Intent(DetailsActivity.this,MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Bütün aktiviteleri kapat
        startActivity(intent);

    }
    public Bitmap makeSmallerImage(Bitmap image, int maximumSize){
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;

        if(bitmapRatio > 1){
            //Landscape
            width = maximumSize;
            height = (int) (width / bitmapRatio);

        }else{
            //Portait image
            height = maximumSize;
            width = (int) (height * bitmapRatio);

        }
        return Bitmap.createScaledBitmap(image,width,height,true);
    }

    public void selectImage(View view){

        //Kullanıcıdan izin henüz alınmamıs mı?
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            //Izin verilmesse neden izin istendiğinin acıklamısını yap
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).
                        setAction("Give Permission", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                //İzin iste
                                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                            }
                        }).show();
            }else{ //Acıklamasız direk izin isteği yap hangisinin secilecegine android otomatik karar veriyor
                //İzin iste
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }else{
            //Izin verilmiş direk galeriye git
            Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            activityResultLauncher.launch(intentToGallery);

        }
    }

    private void registerLauncher(){
        //Kullanıcı galeriye gitti mi?
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                //Kullanıcı birsey secti mi
                if(result.getResultCode() == RESULT_OK){
                    Intent intentFromResult = result.getData();
                    if(intentFromResult != null){
                        Uri imageData = intentFromResult.getData(); //Secilen dosyanın yolu bilgisi

                        //resimi bitmape cevir
                        try {
                            if(Build.VERSION.SDK_INT >= 28){
                                ImageDecoder.Source source = ImageDecoder.createSource(getContentResolver(),imageData);
                                selectedImage = ImageDecoder.decodeBitmap(source);
                                binding.imageView.setImageBitmap(selectedImage);
                            }
                            else{
                                selectedImage = MediaStore.Images.Media.getBitmap(getContentResolver(),imageData);//28 altı API icin
                                binding.imageView.setImageBitmap(selectedImage);
                            }

                        }catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                }
            }
        });
        //Bir islemden sonra izin iste
        permissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {

            @Override
            public void onActivityResult(Boolean result) {

                if(result){
                    Intent intentToGallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    activityResultLauncher.launch(intentToGallery);
                }else {//izin verilmedi
                    Toast.makeText(DetailsActivity.this,"Permission Needed",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.edit_note){


        }
        return super.onOptionsItemSelected(item);
    }
}