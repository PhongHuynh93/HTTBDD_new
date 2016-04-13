package dhbk.android.testgooglesearchreturn;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Created by Thien Nhan on 3/31/2016.
 */
public class ListTripActivity extends AppCompatActivity {
    private File galleryFoler;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_trip);
        setGalleryFolerFromExtras();
        recyclerView = (RecyclerView) findViewById(R.id.recycleView);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 1);


        recyclerView.setLayoutManager(layoutManager);

        RecyclerView.Adapter imageAdapter = new ListAdapter(sortFile(galleryFoler));
        recyclerView.setAdapter(imageAdapter);
    }

    //sorting function
    private File[] sortFile(File fileImageDir) {
        File[] files = fileImageDir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return Long.valueOf(rhs.lastModified()).compareTo(Long.valueOf(lhs.lastModified()));
            }
        });
        return files;
    }

    private void setGalleryFolerFromExtras() {
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (intent != null) {
            String galleryLocation = bundle.getString("galleryLocation");
            File storageDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM + "/Camera"); //path to folder gallery
            galleryFoler = new File(storageDirectory, galleryLocation);
        }

    }

    public void show(View v) {
        Toast.makeText(this, "show", Toast.LENGTH_LONG).show();
    }

}
