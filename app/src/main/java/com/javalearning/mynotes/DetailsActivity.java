package com.javalearning.mynotes;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.TypedArrayUtils;

import android.Manifest;
import android.app.TimePickerDialog;
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
import android.provider.AlarmClock;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.javalearning.mynotes.databinding.ActivityDetailsBinding;

import java.io.ByteArrayOutputStream;
import java.util.Calendar;

public class DetailsActivity extends AppCompatActivity {

    private ActivityDetailsBinding binding;
    //Bir islem olunca sonucunda yapılacak diğer işlemler
    ActivityResultLauncher<Intent> activityResultLauncher;
    ActivityResultLauncher<String> permissionLauncher;
    Bitmap selectedImage;
    SQLiteDatabase database;
    TimePickerDialog timePickerDialog;
    Calendar calendar;

    int choosenHour;
    int choosenMinute;
    int currentHour;
    int currentMinute;
    String amPm;

    byte[] byteArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDetailsBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        binding.dateText.setEnabled(false);
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
            binding.imageView.setImageResource(R.drawable.select);
            binding.button.setVisibility(View.VISIBLE);
            binding.button2.setVisibility(View.INVISIBLE);
            binding.button3.setVisibility(View.INVISIBLE);
            binding.imageView.setClickable(true);


        }else{
            //id göre mevcut veriyi cekip ekranda göster
            int noteId = intent.getIntExtra("noteId",0);
            binding.button.setVisibility(View.INVISIBLE);
            binding.button2.setVisibility(View.VISIBLE);
            binding.button3.setVisibility(View.VISIBLE);
            binding.imageView.setClickable(false);

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
    public void chooseTime(View view){
        calendar = Calendar.getInstance();
        currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        currentMinute = calendar.get(Calendar.MINUTE);

        timePickerDialog = new TimePickerDialog(DetailsActivity.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hourOfDay, int minutes) {
                if(hourOfDay >= 12){
                    amPm = "PM";
                }else{
                    amPm = "AM";
                }
                binding.dateText.setText(String.format("%02d:%02d",hourOfDay,minutes) + amPm);
                choosenHour = hourOfDay;
                choosenMinute = minutes;
            }
        },currentHour,currentMinute,false);
        timePickerDialog.show();
    }
    public void setAlarm(View view){
        if(binding.dateText.getText().toString().isEmpty()){
            Toast.makeText(DetailsActivity.this,"Please choose a time",Toast.LENGTH_LONG).show();
        }
        else{
            Intent intent = new Intent(AlarmClock.ACTION_SET_ALARM);
            intent.putExtra(AlarmClock.EXTRA_HOUR,choosenHour);
            intent.putExtra(AlarmClock.EXTRA_MINUTES,choosenMinute);
            intent.putExtra(AlarmClock.EXTRA_MESSAGE,"This is Notepad alarm.");
            //gerekli aplikasyon (Saat) yuklu olup olmadığı kontrolu
            if(intent.resolveActivity(getPackageManager()) != null){
                startActivity(intent);
            }
            else{
                Toast.makeText(DetailsActivity.this,"We can not find a clock application on your device!",Toast.LENGTH_LONG).show();
            }
        }
    }

    public void delete(View view){
        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS notepages (id INTEGER PRIMARY KEY, notetitle VARCHAR,dateTimer VARCHAR, description VARCHAR,image BLOB)");
            String sqlString = ("DELETE FROM notepages WHERE notetitle = ?");
            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,binding.nameText.getText().toString());
            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        Intent intent = new Intent(DetailsActivity.this,MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Bütün aktiviteleri kapat
        startActivity(intent);
    }
    public void update(View view){
        Intent intent = getIntent();
        int noteId = intent.getIntExtra("noteId",0);
        String name = binding.nameText.getText().toString();
        String date = binding.dateText.getText().toString();
        String description = binding.descriptionText.getText().toString();


        try {
            database.execSQL("CREATE TABLE IF NOT EXISTS notepages (id INTEGER PRIMARY KEY, notetitle VARCHAR,dateTimer VARCHAR, description VARCHAR,image BLOB)");
            String sqlString = "UPDATE notepages SET notetitle = ?,dateTimer = ?,description = ? WHERE id = ?";

            SQLiteStatement sqLiteStatement = database.compileStatement(sqlString);
            sqLiteStatement.bindString(1,name);
            sqLiteStatement.bindString(2,date);
            sqLiteStatement.bindString(3,description);
            sqLiteStatement.bindDouble(4,noteId);


            sqLiteStatement.execute();
        }catch (Exception e){
            e.printStackTrace();
        }
        intent = new Intent(DetailsActivity.this,MainActivity.class);

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //Bütün aktiviteleri kapat
        startActivity(intent);
    }

    public void save(View view){
        String name = binding.nameText.getText().toString();
        String date = binding.dateText.getText().toString();
        String description = binding.descriptionText.getText().toString();
        if(name.isEmpty()){
            name = "No Title";
        }

            if(selectedImage != null){
                Bitmap smallImage = makeSmallerImage(selectedImage,500);
                //Resimi veriye cevir
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                smallImage.compress(Bitmap.CompressFormat.PNG,50,outputStream);
                byteArray = outputStream.toByteArray();
            }else{
                selectedImage = BitmapFactory.decodeResource(getResources(), R.drawable.select);
                Bitmap smallImagez = makeSmallerImage(selectedImage,500);
                ByteArrayOutputStream outputStreamz = new ByteArrayOutputStream();
                smallImagez.compress(Bitmap.CompressFormat.PNG,50,outputStreamz);
                byteArray = outputStreamz.toByteArray();
            }

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
        return Bitmap.createScaledBitmap(image,width,height,false);
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


}