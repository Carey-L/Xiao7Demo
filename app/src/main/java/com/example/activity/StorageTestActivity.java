package com.example.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.example.R;
import com.example.xiao7demo.BaseActivity;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

/**
 * 存储测试界面
 *
 * @author laiweisheng
 * @date 2023/11/16
 */
public class StorageTestActivity extends BaseActivity implements View.OnClickListener {

    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

    private TextView saveFileTv;
    private TextView readFileTv;
    private TextView modifyFileTv;

    private String fileSavePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_storage_test);
        saveFileTv = findViewById(R.id.save_file_tv);
        readFileTv = findViewById(R.id.read_file_tv);
        modifyFileTv = findViewById(R.id.modify_file_tv);
        fileSavePath = getExternalCacheDir().getPath() + File.separator + "test";
        Log.e("lws", "fileSavePath:" + fileSavePath);
        initListener();
    }

    private void initListener() {
        saveFileTv.setOnClickListener(this);
        readFileTv.setOnClickListener(this);
        modifyFileTv.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == saveFileTv) {
            for (int i = 0; i < 10; i++) {
                writeDataToFile(fileSavePath, i + ".txt", String.valueOf(i));
            }
        } else if (v == readFileTv) {
            readDataByPath(fileSavePath);
        } else if (v == modifyFileTv) {
            Random random = new Random();
            int i = random.nextInt(10);
            Log.e("lws", "随机修改文件：" + i + ".txt");
            writeDataToFile(fileSavePath, i + ".txt", String.valueOf(i));
        }
    }

    private void readDataByPath(String path) {
        try {
            File directory = new File(path);
            if (directory.exists() && directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    // 按最近修改时间排序
                    Arrays.sort(files, new Comparator<File>() {
                        @Override
                        public int compare(File file1, File file2) {
                            return -Long.compare(file1.lastModified(), file2.lastModified());
                        }
                    });
                    for (File file : files) {
                        Log.e("lws", "fileName:fileLastModified -> " + file.getName() + ":" + simpleDateFormat.format(file.lastModified()));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void writeDataToFile(String folderPath, String fileName, String fileContent) {
        try {
            if (createPath(folderPath)) {
                File file = new File(folderPath, fileName);
                if (!file.isFile()) {
                    file.delete();
                    file.createNewFile();
                }
                try (FileOutputStream fileOutputStream = new FileOutputStream(file);
                     OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8);
                     BufferedWriter bufferedWriter = new BufferedWriter(outputStreamWriter)) {
                    bufferedWriter.write(fileContent);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建指定路径
     */
    private static boolean createPath(String path) {
        try {
            File file = new File(path);
            if (file.isFile()) {
                if (file.delete()) {
                    return file.mkdirs();
                }
            }
            if (file.isDirectory()) {
                return true;
            } else {
                return file.mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
