package com.example.dhiraj.mcproject;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Compress {

    private static final int BUFFER = 2048;
    private String[] _files;
    private String _zipFile;
    public Compress() {
        _files = null;
        _zipFile = null;

    }

    public Compress(String[] files, String zipFile) {
        _files = files;
        _zipFile = zipFile;
    }

    public void zip() {
        try {
            BufferedInputStream origin = null;
            FileOutputStream dest = new FileOutputStream(_zipFile+".drs");
            ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
                    dest));
            byte data[] = new byte[BUFFER];
            for (int i = 0; i < _files.length; i++) {
                Log.v("Compress", "Adding: " + _files[i]);
                FileInputStream fi = new FileInputStream(_files[i]);
                origin = new BufferedInputStream(fi, BUFFER);
                ZipEntry entry = new ZipEntry(_files[i].substring(_files[i]
                        .lastIndexOf("/") + 1));
                out.putNextEntry(entry);
                int count;
                while ((count = origin.read(data, 0, BUFFER)) != -1) {
                    out.write(data, 0, count);
                }
                origin.close();
            }
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public boolean unpackZip(String path)
    {
        InputStream is;
        ZipInputStream zis;
        int index = path.indexOf("/");
        for(int i = path.length() - 1; i >=0 ; i--){
            if(path.charAt(i) == '/'){
                index = i;
                break;
            }
        }
        index = index + 1;
        try
        {
            String filename;
            is = new FileInputStream(path);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;
            System.out.print("path + filename");
            while ((ze = zis.getNextEntry()) != null)
            {
                // zapis do souboru
                filename = ze.getName();
                Log.v("DeCompress", "Adding: " + filename);

                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    path = path.substring(0, index);
                    File fmd = new File(path + filename);
                    Log.v("DeCompress file in", path + filename);
                    System.out.print(path + filename);
                    fmd.mkdirs();
                    continue;
                }
                path = path.substring(0, index);
                Log.v("DeCompress file in", path + filename);
                FileOutputStream fout = new FileOutputStream(path + filename);

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }
    void deleteFiles(String[] files)
    {
        for (int i = 0; i<files.length ; i++) {
            File fdelete = new File(files[i]);
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    Log.d("deleteFileS",files[i]);
                } else {
                    Log.d("deleteFileF", files[i]);
                }
            }
        }
    }

}