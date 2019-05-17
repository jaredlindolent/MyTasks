package com.jared.mytasks.Activties;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.numberprogressbar.NumberProgressBar;
import com.jared.mytasks.Classes.Tasks;
import com.jared.mytasks.R;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class MyTasksActivity extends AppCompatActivity {
    EditText newItemText;
    Button addNewitem;
    ArrayList<Tasks> tcl;
    Context context;
    ImageView taskImage;

    private RecyclerView recycler;

    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_tasks);
        this.context = this;

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, R.color.darkviolet)));
        }

        taskImage = findViewById(R.id.taskImage);
        tcl = new ArrayList<>();
        newItemText = findViewById(R.id.newItemText);
        addNewitem = findViewById(R.id.addNewItem);
        addNewitem.setTextColor(Color.parseColor("#FFFFFF"));

        file = new File(this.getFilesDir().getAbsolutePath() + "/taskData.dat");
        if (file.exists()) {
            taskImage.setVisibility(View.GONE);
            getTasksOnFile(file);
        }

        addNewitem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!newItemText.getText().toString().isEmpty()) {
                    taskImage.setVisibility(View.GONE);
                    Tasks task = new Tasks(newItemText.getText().toString());
                    tcl.add(task);
                    setupTaskCards(tcl);
                    saveTaskToFile(file, task);
                    newItemText.getText().clear();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.taskRequired) , Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void setupTaskCards(ArrayList<Tasks> tList) {
        recycler = findViewById(R.id.taskCards);
        recycler.setAdapter(new TasksAdapter(this, tList));
        recycler.setLayoutManager(new LinearLayoutManager(this));
    }

    public void saveTaskToFile(File file, Tasks task) {
        if (!file.exists()) {
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(task.getTask().getBytes());
                fos.write("\n".getBytes());
                fos.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        } else {
            try {
                FileWriter fw = new FileWriter(file, true);
                BufferedWriter bw = new BufferedWriter(fw);
                PrintWriter pw = new PrintWriter(bw);
                pw.write(task.getTask());
                pw.write("\n");
                pw.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }

        }
    }

    public void getTasksOnFile(File file){
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null){
                Tasks task = new Tasks(line);
                tcl.add(task);
            }
            setupTaskCards(tcl);
            br.close();
        } catch (IOException ioe){
            ioe.printStackTrace();
        }
    }
}

class TasksAdapter<T> extends RecyclerView.Adapter<TasksAdapter.ViewHolder>{
    public static ArrayList<Tasks> items;
    private Context context;
    int totalSelected = 0;
    NumberProgressBar progressBar;

    public TasksAdapter(Context context, ArrayList<Tasks> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.task_card, viewGroup, false);
        CheckBox taskCheckBox;
        taskCheckBox = view.findViewById(R.id.taskCheckBox);
        progressBar = ((MyTasksActivity)context).findViewById(R.id.number_progress_bar);

        taskCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                if (checked) {
                    updateProgressBar("checked");
                } else {
                    updateProgressBar("unchecked");
                }
            }
        });

        return new ViewHolder(view);
    }

    public void updateProgressBar(String action) {
        if (action == "checked") {
            totalSelected += 1;
        } else if (action == "unchecked") {
            totalSelected -= 1;
        }
        int size = getItemCount();
        double total = (double) totalSelected / (double) size * 100;
        int value = (int) Math.round(total);
        progressBar.setProgress(value);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }


    @Override
    public void onBindViewHolder(TasksAdapter.ViewHolder viewHolder, int i) {
        viewHolder.taskDetail.setText(items.get(i).getTask());
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView taskDetail;
        CheckBox taskCheckBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            taskDetail = itemView.findViewById(R.id.taskDetail);
            taskCheckBox = itemView.findViewById(R.id.taskCheckBox);


            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickSelectedCard(v);
                }
            };
            View.OnLongClickListener longClickListener = new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View v) {
                    showRemoveDialog("Remove Task", "You are about to remove a task");
                    return true;
                }
            };
            itemView.setOnClickListener(clickListener);
            itemView.setOnLongClickListener(longClickListener);
        }


        public void clickSelectedCard(View v) {
            if (taskCheckBox.isChecked()){
                updateProgressBar("unchecked");
            } else {
                updateProgressBar("checked");
            }
            taskCheckBox.toggle();
        }

        public void updateProgressBar(String action) {
            if (action == "checked") {
                totalSelected += 1;
            } else if (action == "unchecked") {
                totalSelected -= 1;
            }
            int size = getItemCount();
            double total = (double) totalSelected / (double) size * 100;
            int value = (int) Math.round(total);
            progressBar.setProgress(value);
        }

        /*
         * Alert dialog control
         */
        private void showRemoveDialog(String title, String message) {
            AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext());
            builder.setMessage(message);
            builder.setTitle(title);
            builder.setPositiveButton("CANCEL", null);
            builder.setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    TasksAdapter.items.remove(getAdapterPosition());
//                    removeFromFile(getAdapterPosition());
                    notifyDataSetChanged();
                }
            });
            builder.show();

        }
    }
}