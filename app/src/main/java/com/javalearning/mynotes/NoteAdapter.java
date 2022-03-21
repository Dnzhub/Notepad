package com.javalearning.mynotes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.javalearning.mynotes.databinding.RecyclerRowBinding;

import java.util.ArrayList;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.NoteHolder>{

    ArrayList<Note> noteArrayList;
    SQLiteDatabase database;
    public NoteAdapter(ArrayList<Note> noteArrayList){
        this.noteArrayList = noteArrayList;
    }
    @NonNull
    @Override
    public NoteHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //Classı layout(XML) ile bağla
        RecyclerRowBinding recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new NoteHolder(recyclerRowBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteHolder holder, int position) {
        //Holderın ismini alıp ekranda göster
        holder.binding.recyclerViewTextView.setText(noteArrayList.get(holder.getAdapterPosition()).name);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(holder.itemView.getContext(),DetailsActivity.class);
                intent.putExtra("info","old");
                intent.putExtra("noteId",noteArrayList.get(holder.getAdapterPosition()).id);
                holder.itemView.getContext().startActivity(intent);
            }
        });

    }

    @Override
    public int getItemCount() {
        //Kac adet item olusturulacak
        return noteArrayList.size();
    }

    public class NoteHolder extends RecyclerView.ViewHolder{
        private RecyclerRowBinding binding;

        public NoteHolder(RecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
