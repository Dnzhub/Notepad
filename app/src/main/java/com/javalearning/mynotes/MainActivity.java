package com.javalearning.mynotes;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.javalearning.mynotes.databinding.ActivityMainBinding;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    ArrayList<Note> noteArrayList;
    NoteAdapter noteAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        noteArrayList = new ArrayList<Note>();

        //Recycler Viewi note adapter ile bagla
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(noteArrayList);
        binding.recyclerView.setAdapter(noteAdapter);

        getData();
    }
    private void getData(){
        //Datadan verileri cek
        try {
            SQLiteDatabase sqLiteDatabase = this.openOrCreateDatabase("NotePages",MODE_PRIVATE,null);
            Cursor cursor =  sqLiteDatabase.rawQuery("SELECT * FROM notepages",null);
            int nameIndex = cursor.getColumnIndex("notetitle");
            int idIndex = cursor.getColumnIndex("id");

            while(cursor.moveToNext()){
                String name = cursor.getString(nameIndex);
                int id = cursor.getInt(idIndex);
                Note noteScript = new Note(name,id);
                noteArrayList.add(noteScript);
            }
            //Yeni veri gelince recycler view güncelle
            noteAdapter.notifyDataSetChanged();

            cursor.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    //Menuyu activity bagla (inflater)
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.note_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Menuden birşey secilirse ne olacagını belirle
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.add_note){
            Intent intent = new Intent(this,DetailsActivity.class);
            intent.putExtra("info","new");
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }
}